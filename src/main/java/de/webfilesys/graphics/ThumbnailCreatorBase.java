package de.webfilesys.graphics;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

import org.apache.log4j.Logger;

import de.webfilesys.Constants;
import de.webfilesys.FileComparator;
import de.webfilesys.FileSelectionStatus;
import de.webfilesys.FileSelector;

public class ThumbnailCreatorBase
{
    public static final int SCALE_MIN_SIZE = 300;
    
    public static final String THUMBNAIL_SUBDIR = "_thumbnails";

    public String imgFileMasks[]={"*.jpg","*.jpeg","*.png"};

    public static final int SCOPE_FILE = 1;
    public static final int SCOPE_DIR  = 2;
    public static final int SCOPE_TREE = 3;
    
    protected void createScaledImage(String imgFileName)
    {
        ScaledImage scaledImg = null;

        try
        {
            scaledImg = new ScaledImage(imgFileName, Constants.THUMBNAIL_SIZE, Constants.THUMBNAIL_SIZE);
        }
        catch (IOException ioex)
        {
            Logger.getLogger(getClass()).error(ioex);
            return;
        }
        
        if (scaledImg.getRealWidth() + scaledImg.getRealHeight() < 2 * SCALE_MIN_SIZE)
        {
            return;
        }

        if (scaledImg.getImageType() == ScaledImage.IMG_TYPE_JPEG)
        {
            CameraExifData exifData = new CameraExifData(imgFileName);

            if (exifData.getThumbnailLength() > 0)
            {
                if (scaledImg.getRealWidth() < scaledImg.getRealHeight())
                {
                    // portrait orientation
                    if (exifData.getThumbOrientation() == CameraExifData.ORIENTATION_PORTRAIT)
                    {
                        // we can use the thumbnail from the Exif data
                        return;
                    }
                    else
                    {
                        if ((exifData.getOrientation() == 6) || (exifData.getOrientation() == 8))
                        {
                            // we can use a rotated version of the thumbnail from the Exif data
                            return;
                        }
                    }
                }
                else
                {
                    // landscape orientation
                    if (exifData.getThumbOrientation() == CameraExifData.ORIENTATION_LANDSCAPE)
                    {
                        // we can use the thumbnail from the Exif data
                        return;
                    }
                    else
                    {
                        if ((exifData.getOrientation() == 6) || (exifData.getOrientation() == 8)) 
                        {
                            // we can use a rotated version of the thumbnail from the Exif data
                            return;
                        }
                    }
                }
            }
        }

        String thumbDirPath = imgFileName.substring(0,imgFileName.lastIndexOf(File.separatorChar)+1) + THUMBNAIL_SUBDIR;

        File thumbDir = new File(thumbDirPath);

        if (!thumbDir.exists())
        {
            if (!thumbDir.mkdir())
            {
                Logger.getLogger(getClass()).error("cannot create thumbnail dir: " + thumbDir);
                return;                   
            }
        }
        else
        {
            if ((!thumbDir.isDirectory()) || (!thumbDir.canWrite()))
            {
                Logger.getLogger(getClass()).error("cannot write to thumbnail dir: " + thumbDir);
                return;                   
            }
        }   

        String thumbFileName = getThumbnailPath(imgFileName);

        int scaledWidth = scaledImg.getScaledWidth();
        int scaledHeight = scaledImg.getScaledHeight();
        
        long startTime = System.currentTimeMillis();

        Canvas imgObserver = new Canvas();

        Image origImage = Toolkit.getDefaultToolkit().createImage(imgFileName);

        imgObserver.prepareImage(origImage, imgObserver);

        int timeoutCounter = 900;

        while ((imgObserver.checkImage(origImage, imgObserver) & ImageObserver.ALLBITS) != ImageObserver.ALLBITS)
        {
            try
            {
                Thread.sleep(100);

                timeoutCounter--;

                if (timeoutCounter == 0)
                {
                    Logger.getLogger(getClass()).error(
                            "picture load timeout for image " + imgFileName);

                    origImage.flush();

                    return;
                }
            }
            catch (InterruptedException iex)
            {
                Logger.getLogger(getClass()).error(iex);
                origImage.flush();
                return;
            }
        }

        BufferedImage bufferedImg = new BufferedImage(scaledImg.getRealWidth(), scaledImg.getRealHeight(), BufferedImage.TYPE_INT_RGB);

        Graphics g = bufferedImg.getGraphics();
        g.drawImage(origImage, 0, 0, imgObserver);

        bufferedImg = ImageTransformation.getScaledInstance(bufferedImg, scaledWidth, scaledHeight,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC, true);
        
        if (scaledImg.getImageType() == ScaledImage.IMG_TYPE_JPEG)
        {
            // JPEG thumbnails
            boolean success = false;
            BufferedOutputStream thumbOut = null;
            
            try 
            {
                Iterator iter = ImageIO.getImageWritersByFormatName("jpg");
                ImageWriter imgWriter = (ImageWriter) iter.next();
                thumbOut = new BufferedOutputStream(new FileOutputStream(thumbFileName));
                ImageOutputStream ios = ImageIO.createImageOutputStream(thumbOut);
                imgWriter.setOutput(ios);
                ImageWriteParam iwparam = new JPEGImageWriteParam(Locale.getDefault());
                iwparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                iwparam.setCompressionQuality(0.9f);
                imgWriter.write(null, new IIOImage(bufferedImg, null, null), iwparam);
                ios.flush();
                imgWriter.dispose();
                long endTime = System.currentTimeMillis();
                Logger.getLogger(getClass()).debug("JPEG Thumbnail created for " + imgFileName + " (" + (endTime - startTime) + " ms)");
                success = true;
            }
            catch (IOException ioex) 
            {
                Logger.getLogger(getClass()).error("error writing thumbnail file " + thumbFileName, ioex);
            }
            catch (OutOfMemoryError memEx)
            {
                Logger.getLogger(getClass()).error("insufficient memory for thumbnail creation", memEx);
            }
            finally
            {
                if (thumbOut != null) {
                    try
                    {
                        thumbOut.close();
                    }
                    catch (Exception ex)
                    {
                        Logger.getLogger(getClass()).error("error closing thumbnail file", ex);
                    }
                }
            }

            if (!success) {
                cleanupThumbnail(thumbFileName);
            }
        }
        else  
        {
            // PNG thumbnails
            
            byte[] pngBytes;
            com.keypoint.PngEncoder pngEncoder = new com.keypoint.PngEncoder(bufferedImg);

            /*
            pngEncoder.setCompressionLevel(9);
            pngEncoder.setFilter(com.keypoint.PngEncoder.FILTER_UP);
            */

            boolean success = false;
            BufferedOutputStream thumbOut = null;
            
            try
            {
                thumbOut = new BufferedOutputStream(new FileOutputStream(thumbFileName));

                pngBytes = pngEncoder.pngEncode();

                if (pngBytes == null)
                {
                    Logger.getLogger(getClass()).warn("PNG Encoder : Null image");
                }
                else
                {
                    thumbOut.write(pngBytes);
                }
                
                thumbOut.flush();

                long endTime = System.currentTimeMillis();
                Logger.getLogger(getClass()).debug("PNG Thumbnail created for " + imgFileName + " (" + (endTime - startTime) + " ms)");
                
                success = true;
            }
            catch (IOException ioex)
            {
                Logger.getLogger(getClass()).error("cannot create PNG thumbnail for " + imgFileName, ioex);
            }
            finally
            {
                if (thumbOut != null) {
                    try
                    {
                        thumbOut.close();
                    }
                    catch (Exception ex)
                    {
                        Logger.getLogger(getClass()).error("error closing thumbnail file", ex);
                    }
                }
            }

            if (!success) {
                cleanupThumbnail(thumbFileName);
            }
            
            pngBytes = null;

            // end PNG
        }
        
        g.dispose();
        bufferedImg.flush();

        origImage.flush();
    }

    public boolean thumbnailUpToDate(String imgPath)
    {
        File imgFile=new File(imgPath);
        if (!imgFile.exists())
        {
            return(true);
        }

        String thumbFileName=getThumbnailPath(imgPath);

        File thumbnailFile=new File(thumbFileName);

        boolean upToDate=true;

        if (!thumbnailFile.exists())
        {
            upToDate=false;
        }
        else
        {
            long thumbDate=thumbnailFile.lastModified();
            long imgDate=imgFile.lastModified();

            if (imgDate>thumbDate)
            {
                upToDate=false;
            }
        }

        return(upToDate);
    }
    
    public boolean createThumbnail(String imagePath)
    {
        if (!thumbnailUpToDate(imagePath))
        {
            createScaledImage(imagePath);
        }

        return(true);
    }
    
    public void updateThumbnails(String currentPath,boolean recurse)
    {
        String actPath=currentPath;

        if (!actPath.endsWith(File.separator))
        {
            actPath=actPath + File.separator;
        }

        if (recurse)
        {
            // depth first - to prevent recursive thumbnail directories

            File dirFile=new File(currentPath);

            String fileList[]=dirFile.list();

            if ((fileList!=null) && (fileList.length > 0))
            {
                for (int i=0;i<fileList.length;i++)
                {
                    String fileName=fileList[i];

                    String fullPath=actPath + fileName;

                    File tempFile=new File(fullPath);

                    if (tempFile.exists() && tempFile.isDirectory())
                    {
                        updateThumbnails(fullPath,true);
                    }
                }
            }
        }

        FileSelector fileSelector=new FileSelector(actPath,FileComparator.SORT_BY_FILENAME);

        FileSelectionStatus selectionStatus=fileSelector.selectFiles(imgFileMasks,2048,null,null);

        Vector selectedFiles=selectionStatus.getSelectedFiles();

        if ((selectedFiles!=null) && (selectedFiles.size()>0))
        {
            for (int i=0;i<selectedFiles.size();i++)
            {
                String actFileName=(String) selectedFiles.elementAt(i);

                if (!thumbnailUpToDate(actPath + actFileName))
                {
                    createScaledImage(actPath + actFileName);
                }
            }
        }
    }
    
    protected void cleanupThumbnail(String thumbFileName)
    {
        File tempFile = new File(thumbFileName);

        if (!tempFile.delete())
        {
            Logger.getLogger(getClass()).error("cannot cleanup thumbnail file " + thumbFileName);
        }
    }

    public static String getThumbnailPath(String imgPath)
    {
        int sepIdx = imgPath.lastIndexOf(File.separator);

        if (sepIdx < 0)
        {
            Logger.getLogger(ThumbnailCreatorBase.class).error("incorrect image path: " + imgPath);
            return(null); 
        }

        String basePath=imgPath.substring(0,sepIdx+1);

        String thumbPath=basePath + THUMBNAIL_SUBDIR + File.separator;

        String imgFileName=imgPath.substring(sepIdx+1);

        return(thumbPath + imgFileName);
    }
}
