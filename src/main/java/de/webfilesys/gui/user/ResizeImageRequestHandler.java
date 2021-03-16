package de.webfilesys.gui.user;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.SubdirExistCache;
import de.webfilesys.WebFileSys;
import de.webfilesys.graphics.AutoThumbnailCreator;
import de.webfilesys.graphics.CameraExifData;
import de.webfilesys.graphics.ExifUtil;
import de.webfilesys.graphics.GifQuantizer;
import de.webfilesys.graphics.ImageTextStamp;
import de.webfilesys.graphics.ImageTransform;
import de.webfilesys.graphics.RotateFilter;
import de.webfilesys.graphics.ScaledImage;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.UTF8URLDecoder;
import de.webfilesys.util.UTF8URLEncoder;

/**
 * Resize/convert and/or add a copyright text to a single picture or selected
 * pictures.
 * 
 * @author Frank Hoehnel
 */
public class ResizeImageRequestHandler extends UserRequestHandler
{
	private static final int MIN_TARGET_SIZE = 8;
	private static final int MAX_TARGET_SIZE = 32000;
	
    public ResizeImageRequestHandler(HttpServletRequest req,
            HttpServletResponse resp, HttpSession session, PrintWriter output,
            String uid)
    {
        super(req, resp, session, output, uid);
    }

    protected void process()
    {
        if (!checkWriteAccess())
        {
            return;
        }

        String actPath = null;

        String imgFileName = null;

        String pictureFileName = getParameter("imgFile");

        if (pictureFileName != null)
        {
            // single file
            imgFileName = pictureFileName.replace('\\', '/');

            actPath = imgFileName.substring(0, imgFileName.lastIndexOf('/'));
        }
        else
        {
            // multiple selected files
            actPath = getParameter("actPath");
        }

        if (!checkAccess(actPath))
        {
            return;
        }

        String pathWithSlash = actPath;

        if (!actPath.endsWith(File.separator))
        {
            pathWithSlash = actPath + "/";
        }

        String cropAreaLeft = req.getParameter("cropAreaLeft");
        String cropAreaTop = req.getParameter("cropAreaTop");
        String cropAreaWidth = req.getParameter("cropAreaWidth");
        String cropAreaHeight = req.getParameter("cropAreaHeight");
        
        // System.out.println("crop area: left=" + cropAreaLeft + " top=" + cropAreaTop + " width=" + cropAreaWidth + " height=" + cropAreaHeight);
        
        String newSizeString = getParameter("newSize");

        if (newSizeString.equals("-1"))
        {
        	newSizeString = req.getParameter("targetSize");
        }
        
        boolean invalidNewSize = false;
        
        int newSize = 200;

        try
        {
            newSize = Integer.parseInt(newSizeString);
            if ((newSize != 0) && (newSize < MIN_TARGET_SIZE) || (newSize > MAX_TARGET_SIZE))
            {
                invalidNewSize = true;
            }
        }
        catch (NumberFormatException nfe)
        {
            invalidNewSize = true;
        }

        boolean keepExifData = getParameter("keepExifData") != null;
        
        String copyRightText = getParameter("copyRightText");

        int copyRightPos = ImageTextStamp.TEXT_POS_LOWER_RIGHT;

        String temp = getParameter("copyRightPos");

        if (temp != null)
        {
            try
            {
                copyRightPos = Integer.parseInt(temp);
            }
            catch (NumberFormatException nfex)
            {

            }
        }

        int copyRightFontSize = 10;

        temp = getParameter("copyRightFontSize");

        if (temp != null)
        {
            try
            {
                copyRightFontSize = Integer.parseInt(temp);
            }
            catch (NumberFormatException nfex)
            {

            }
        }

        Color copyRightColor = new Color(255, 255, 255); // white

        temp = getParameter("copyRightColor");

        if (temp != null)
        {
            try
            {
                int color = Integer.parseInt(temp, 16);

                copyRightColor = new Color(color);
            }
            catch (NumberFormatException nfex)
            {
    			Logger.getLogger(getClass()).error("parameter copyRightColor invalid", nfex);
            }
        }

        String scaledImageFolder = null;

        if (newSize > 0)
        {
            scaledImageFolder = pathWithSlash.replace('/', File.separatorChar)
                    + newSize;
        }
        else
        {
            scaledImageFolder = actPath.replace('/', File.separatorChar);
        }

        String outputFormat = getParameter("format");

        output.println("<HTML>");
        output.println("<HEAD>");
        output.println("<title>WebFileSys - "
                + getResource("label.resizetitle", "resize images")
                + "</title>");

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/common.css\">");
        output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/skins/" + userMgr.getCSS(uid) + ".css\">");

        output.println("<script src=\"/webfilesys/javascript/errorHandling.js\" type=\"text/javascript\"></script>");
        
        output.println("<script language=\"javascript\">");
        
        output.println("function returnToPictures()");
        output.println("{");
        output
                .println("  parent.DirectoryPath.location.href='/webfilesys/servlet?command=exp&expand="
                        + UTF8URLEncoder.encode(actPath.replace('/',
                                File.separatorChar)) + "';");
        output
                .println("  window.location.href='/webfilesys/servlet?command=listFiles';");
        output.println("}");
        output.println("function gotoScaledPictures()");
        output.println("{");
        output
                .println("  parent.DirectoryPath.location.href='/webfilesys/servlet?command=exp&expandPath="
                        + UTF8URLEncoder.encode(scaledImageFolder)
                        + "&fastPath=true';");
        output.println("}");

        output.println("</script>");

        output.println("</head>");
        output.println("<body class=\"editPict\">");

        headLine(getResource("label.resizetitle", "resize images"));

        output.println("<br/>");
        
        output.println("<ul id=\"errorMsgs\" class=\"errorMsg\" style=\"display:none\">");
        output.println("</ul>");

        boolean success = false;
        
        if (invalidNewSize) 
        {
            output.println("<script type=\"text/javascript\">");
            output.println("addErrorMsg('" + getResource("error.scaleTargetSize", "the new image size value is invalid") + ": " + newSizeString + "')");
            output.println("</script>");
        }
        else
        {
            String shortPath = CommonUtils.shortName(
                    getHeadlinePath(scaledImageFolder), 50);

            output.println("<form accept-charset=\"utf-8\" name=\"form1\">");

            output.println("<table class=\"dataForm\" width=\"100%\" border=\"0\">");
            if (newSize > 0)
            {
                output.println("<tr><td class=\"formParm1\">");
                output.println(getResource("label.scaledFolder",
                        "Scaled Images are stored in folder")
                        + ":");
                output.println("</td></tr>");
                output.println("<tr><td class=\"formParm2\">");
                output.println(shortPath);
                output.println("</td></tr>");
            }
            
            output.println("<tr><td colspan=\"2\"class=\"formParm1\">");
            output.println(getResource("label.picTransformed", "picture file scaled/transformed") + ":");
            output.println("</td></tr>");

            output.println("<tr><td colspan=\"2\" class=\"formParm2\">");
            output.println("<span id=\"currentFile\"/>");

            output.println("</td></tr></table>");
            output.println("</form>");

            output.flush();

            if (newSize > 0)
            {
                File scaledDir = new File(pathWithSlash + newSize);

                if (!scaledDir.exists())
                {
                    if (!scaledDir.mkdir())
                    {
                        output.println("cannot create dir for scaled images");
                        output.println("</body></html>");
                        output.flush();
                        Logger.getLogger(getClass()).error("cannot create dir for scaled images: " + scaledDir);
                        return;
                    }
                    else
                    {
                    	SubdirExistCache.getInstance().setExistsSubdir(scaledImageFolder, new Integer(0));
                    	SubdirExistCache.getInstance().setExistsSubdir(actPath.replace('/', File.separatorChar), new Integer(1));
                    }
                }
            }

            int cropLeft = (-1);
            int cropTop = (-1);
            int cropWidth = (-1);
            int cropHeight = (-1);

            if (pictureFileName != null)
            {
                // single file

                String imgFile = imgFileName
                        .substring(imgFileName.lastIndexOf('/') + 1);

                String crop = req.getParameter("crop");
                
                if (crop != null) {
                    try 
                    {
                        cropLeft = Integer.parseInt(cropAreaLeft);
                        cropTop = Integer.parseInt(cropAreaTop);
                        cropWidth = Integer.parseInt(cropAreaWidth);
                        cropHeight = Integer.parseInt(cropAreaHeight);
                    }
                    catch (NumberFormatException numEx) 
                    {
                        Logger.getLogger(getClass()).error(numEx);
                    }
                }
                
                success = createScaledImage(pathWithSlash, imgFile, outputFormat,
                        newSize, keepExifData, copyRightText, copyRightPos, copyRightColor,
                        copyRightFontSize, cropLeft, cropTop, cropWidth, cropHeight);
            }
            else
            {
                // multiple selected files

                ArrayList<String> selectedFiles = (ArrayList<String>) session.getAttribute("selectedFiles");

                if (selectedFiles != null)
                {
                	for (String selectedFile : selectedFiles) 
                    {
                        String actFileName = UTF8URLDecoder.decode(selectedFile);

                        if (createScaledImage(pathWithSlash, actFileName,
                                outputFormat, newSize, keepExifData, copyRightText, copyRightPos,
                                copyRightColor, copyRightFontSize, 
                                cropLeft, cropTop, cropWidth, cropHeight))
                        {
                            success = true;
                        }

                        output.flush();
                    }
                }
            }
        }

        // resize in popup window (called from showImage) ?
        String popup = getParameter("popup");

        output.println("<form accept-charset=\"utf-8\" name=\"form2\">");

        if (popup != null)
        {
        	output.println("<div class=\"closeWinButtonCont\">");
            output.println("<input type=\"button\" value=\""
                    + getResource("button.closewin", "Close Window")
                    + "\" onclick=\"self.close()\"/>");
        	output.println("</div>");
        }
        else
        {
            if (newSize == 0) {
            	output.println("<script type=\"text/javascript\">");
            	output.println("returnToPictures();");
            	output.println("</script>");
            } else {
            	output.println("<div class=\"buttonCont\">");
            	
                output.println("<input type=\"button\" value=\""
                        + getResource("button.return", "Return")
                        + "\" onclick=\"returnToPictures()\">");

                if ((newSize > 0) && (success)) {
                    output.println("&nbsp;&nbsp;&nbsp;");
                    output
                            .println("<input type=\"button\" value=\""
                                    + getResource("button.gotoScaled",
                                            "View Scaled Pictures")
                                    + "\" onclick=\"gotoScaledPictures()\">");
                }

                output.println("</div>");
            }
        }

        output.println("</form>");
        output.println("</body>");
        output.println("</html>");
        output.flush();
    }

    protected boolean createScaledImage(String actPath, String fileName,
            String format, int newSize, boolean keepExifData, String copyRightText, int copyRightPos,
            Color copyRightColor, int copyRightFontSize,
            int cropAreaLeft, int cropAreaTop, int cropAreaWidth, int cropAreaHeight)
    {
        ScaledImage scaledImg = null;

        String imgFileName = actPath + fileName;

        try
        {
            scaledImg = new ScaledImage(imgFileName, newSize, newSize);
        }
        catch (IOException ioex3)
        {
            Logger.getLogger(getClass()).error(ioex3);

            javascriptAlert("cannot read image file");

            return false;
        }
        
        if ((scaledImg.getRealHeight() < newSize) && (scaledImg.getRealWidth() < newSize))
        {
            output.println("<script type=\"text/javascript\">");
            output.println("addErrorMsg('" + fileName + ": " + getResource("error.scaleSize", "size of original image is less than target size") + "')");
            output.println("</script>");
        	return false;
        }

        String destFileName = fileName;

        if (fileName.indexOf('.') > 0)
        {
            destFileName = fileName.substring(0, fileName.lastIndexOf('.'));
        }

        if (format.equals("JPEG"))
        {
            destFileName = destFileName + ".jpg";
        }
        else
        {
            if (format.equals("PNG"))
            {
                destFileName = destFileName + ".png";
            }
            else
            {
                if (format.equals("GIF"))
                {
                    destFileName = destFileName + ".gif";
                }
            }
        }

        String thumbFileName = null;

        if (newSize == 0) // no resize
        {
            thumbFileName = actPath + destFileName;
        }
        else
        {
            thumbFileName = actPath + newSize + File.separator + destFileName;
        }

        thumbFileName = thumbFileName.replace('/', File.separatorChar);

        File existingDestFile = new File(thumbFileName);

        if (existingDestFile.exists())
        {
            thumbFileName = getModifiedFileName(thumbFileName);
        }

        int scaledWidth;
        int scaledHeight;

        if (newSize == 0)
        {
            scaledWidth = scaledImg.getRealWidth();
            scaledHeight = scaledImg.getRealHeight();
        }
        else
        {
            scaledWidth = scaledImg.getScaledWidth();
            scaledHeight = scaledImg.getScaledHeight();
        }

        Image origImage = null;
        BufferedImage bufferedImg = null;

        FileOutputStream thumbFile = null;

        try
        {
            long startTime = (new Date()).getTime();

            Canvas imgObserver = new Canvas();

            origImage = Toolkit.getDefaultToolkit().createImage(imgFileName);

            Canvas dummyComponent = new Canvas();

            MediaTracker tracker = new MediaTracker(dummyComponent);
            tracker.addImage (origImage, 0);

            try {
                tracker.waitForAll();
            } catch (Exception ex) {
                Logger.getLogger(getClass()).warn("failed to load image " + imgFileName, ex);
            }

            tracker.removeImage(origImage);

            long endTime;

            if (cropAreaTop >= 0) {
                
                try
                {
                    ScaledImage cropSrcImg = new ScaledImage(imgFileName, 400, 400);
                    
                    int croppedImgLeft = cropAreaLeft * cropSrcImg.getRealWidth() / cropSrcImg.getScaledWidth();
                    int croppedImgTop = cropAreaTop * cropSrcImg.getRealHeight() / cropSrcImg.getScaledHeight();
                    int croppedImgWidth = cropAreaWidth * cropSrcImg.getRealWidth() / cropSrcImg.getScaledWidth();
                    int croppedImgHeight = cropAreaHeight * cropSrcImg.getRealHeight() / cropSrcImg.getScaledHeight();

    				CameraExifData exifData = new CameraExifData(imgFileName);
    				if ((exifData.getOrientation() == 6) || (exifData.getOrientation() == 8)) {
    					int savedWith = croppedImgWidth;
    					croppedImgWidth = croppedImgHeight;
    					croppedImgHeight = savedWith;
    				}
    				if (exifData.getOrientation() == 6) {
                        int savedCroppedImgLeft = croppedImgLeft;
                        croppedImgLeft = croppedImgTop;
    					croppedImgTop = cropSrcImg.getRealHeight() - savedCroppedImgLeft - croppedImgHeight;
    				} else if (exifData.getOrientation() == 8) {
    					int savedCroppedImgLeft = croppedImgLeft;
                        croppedImgLeft = cropSrcImg.getRealWidth() - croppedImgTop - croppedImgWidth;
    					croppedImgTop = savedCroppedImgLeft;
    				}
    				
                    bufferedImg = new BufferedImage(croppedImgWidth, croppedImgHeight, BufferedImage.TYPE_INT_RGB);

                    bufferedImg.getGraphics().drawImage(origImage, 
                                                        0, 0, croppedImgWidth -1, croppedImgHeight - 1, 
                                                        croppedImgLeft, croppedImgTop, croppedImgLeft + croppedImgWidth, croppedImgTop + croppedImgHeight,
                                                        Color.white, null);
                    
    				if (exifData.getOrientation() == 6) {
    					bufferedImg = rotateImage(bufferedImg, 270);
    				} else if (exifData.getOrientation() == 8) {
    					bufferedImg = rotateImage(bufferedImg, 90);
    				}

    				if ((exifData.getOrientation() == 6) || (exifData.getOrientation() == 8)) {
    					int savedWith = croppedImgWidth;
    					croppedImgWidth = croppedImgHeight;
    					croppedImgHeight = savedWith;
    				}
                    
                    if ((croppedImgWidth < newSize) && (croppedImgHeight < newSize))
                    {
                        scaledWidth = croppedImgWidth;
                        scaledHeight = croppedImgHeight;
                    }
                    else
                    {
                        if (croppedImgWidth > croppedImgHeight) {
                            scaledWidth = newSize;
                            scaledHeight = croppedImgHeight * newSize / croppedImgWidth;
                        }
                        else 
                        {
                            scaledHeight = newSize;
                            scaledWidth = croppedImgWidth * newSize / croppedImgHeight;
                        }
                    }
                }
                catch (IOException ioEx)
                {
                    Logger.getLogger(getClass()).error(ioEx);
                    return (false);
                }
            } 
            else
            {
                bufferedImg = new BufferedImage(scaledImg.getRealWidth(),
                        scaledImg.getRealHeight(), 
                        BufferedImage.TYPE_INT_RGB);

                bufferedImg.getGraphics().drawImage(origImage, 0, 0,
                        imgObserver);
            }
            
            if (newSize != 0)
            {
                bufferedImg = ImageTransform.getScaledInstance(bufferedImg, scaledWidth, scaledHeight,
                                                                    RenderingHints.VALUE_INTERPOLATION_BICUBIC, true);
            }

            // add copyright text stamp
            if ((copyRightText != null) && (copyRightText.trim().length() > 0))
            {
                ImageTextStamp.stampText(bufferedImg, copyRightText,
                        copyRightFontSize, copyRightColor, Font.PLAIN,
                        copyRightPos);
            }

            thumbFile = new FileOutputStream(thumbFileName);

            if (format.equals("JPEG"))
            {
                Iterator iter = ImageIO.getImageWritersByFormatName("jpg");
                ImageWriter imgWriter = (ImageWriter) iter.next();
                ImageOutputStream ios = ImageIO
                        .createImageOutputStream(thumbFile);
                imgWriter.setOutput(ios);
                ImageWriteParam iwparam = new JPEGImageWriteParam(Locale
                        .getDefault());
                iwparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                iwparam.setCompressionQuality(0.9f);
                imgWriter.write(null, new IIOImage(bufferedImg, null, null),
                        iwparam);
                ios.flush();
                imgWriter.dispose();

                thumbFile.close();

                endTime = (new Date()).getTime();

                output.println("<script language=\"javascript\">");
                output.println("document.getElementById('currentFile').innerHTML = '"
                        + insertDoubleBackslash(fileName) + " ("
                        + (endTime - startTime) + " ms)';");
                output.println("</script>");
                output.flush();

                output.flush();
                bufferedImg.flush();
            }

            if (format.equals("PNG"))
            {
                byte[] pngBytes;
                com.keypoint.PngEncoder pngEncoder = new com.keypoint.PngEncoderB(
                        bufferedImg);

                // pngEncoder.setCompressionLevel(1);
                // pngEncoder.setFilter(com.keypoint.PngEncoder.FILTER_LAST);

                pngBytes = pngEncoder.pngEncode();

                if (pngBytes == null)
                {
                    Logger.getLogger(getClass()).error("PNG Encoder : Null image");
                }
                else
                {
                    thumbFile.write(pngBytes);
                }
                thumbFile.flush();
                thumbFile.close();

                endTime = (new Date()).getTime();

                output.println("<script language=\"javascript\">");
                
                output.println("document.getElementById('currentFile').innerHTML = '"
                        + insertDoubleBackslash(fileName) + " ("
                        + (endTime - startTime) + " ms)';");
                
                output.println("</script>");
                output.flush();

                output.flush();
                bufferedImg.flush();
            }

            if (format.equals("GIF"))
            {
                BufferedImage quantizedImg = GifQuantizer.process(bufferedImg);

                Acme.JPM.Encoders.GifEncoder gifEncoder = new Acme.JPM.Encoders.GifEncoder(
                        quantizedImg, thumbFile);

                gifEncoder.encode();

                thumbFile.flush();
                thumbFile.close();

                endTime = (new Date()).getTime();

                output.println("<script language=\"javascript\">");
                output.println("document.getElementById('currentFile').innerHTML = '"
                        + insertDoubleBackslash(fileName) + " ("
                        + (endTime - startTime) + " ms)';");
                output.println("</script>");
                output.flush();

                output.flush();
                bufferedImg.flush();
            }

        }
        catch (OutOfMemoryError memEx)
        {
            javascriptAlert(getResource("alert.outOfMemory",
                    "insufficient memory to perform the requested operation"));

            Logger.getLogger(getClass()).error(memEx.toString());

            output.flush();

            try
            {
                if (thumbFile != null) {
                    thumbFile.close();
                }
            }
            catch (IOException ioEx)
            {
            }

            File abortedFile = new File(thumbFileName);

            abortedFile.delete();

            return (false);
        }
        catch (IOException ioEx)
        {
            Logger.getLogger(getClass()).warn(ioEx);

            javascriptAlert("error writing output file: " + ioEx);

            return (false);
        }
        finally
        {
            if (origImage != null)
            {
                origImage.flush();
            }
            
            if (bufferedImg != null)
            {
                bufferedImg.flush();
            }
        }

        if (keepExifData) {
        	if (format.equals("JPEG")) {
                File sourceFile = new File(imgFileName);
                File destFile = new File(thumbFileName);
                ExifUtil.copyExifData(sourceFile, destFile, null);        
        	}
        }
        
        if (WebFileSys.getInstance().isAutoCreateThumbs())
        {
            if (format.equals("JPEG") || format.equals("PNG"))
            {
                AutoThumbnailCreator.getInstance().queuePath(thumbFileName,
                        AutoThumbnailCreator.SCOPE_FILE);
            }
        }

        return (true);
    }

    private String getModifiedFileName(String origFileName)
    {
        String modifiedFileName = origFileName;

        String fileName = origFileName;

        String ext = "";

        int sepIdx = origFileName.lastIndexOf(".");

        if (sepIdx > 0)
        {
            fileName = origFileName.substring(0, sepIdx);

            if (sepIdx < origFileName.length() - 1)
            {
                ext = origFileName.substring(sepIdx + 1);
            }
        }

        boolean destFileExists = true;

        for (int j = 0; destFileExists; j++)
        {
            modifiedFileName = fileName + "_" + j + "." + ext;

            File destFile = new File(modifiedFileName);

            if (!destFile.exists())
            {
                destFileExists = false;
            }
        }

        return (modifiedFileName);
    }
    
    private BufferedImage rotateImage(BufferedImage origImage, double degree) {
    	
    	int newWidth = origImage.getHeight();
    	int newHeight = origImage.getWidth();
    	
        try {
            ImageFilter filter = new RotateFilter((Math.PI / 180) * degree);
            ImageProducer producer = new FilteredImageSource(origImage.getSource(), filter);
            Canvas dummyComponent = new Canvas();
            Image rotatedImg = dummyComponent.createImage(producer);

            MediaTracker tracker = new MediaTracker(dummyComponent);
            tracker.addImage(rotatedImg, 1);

            try {
                tracker.waitForAll();
            } catch(InterruptedException ex) {
               Logger.getLogger(getClass()).error("failed to rotate image", ex);
            }

            tracker.removeImage(rotatedImg);

            origImage.flush();
            
            BufferedImage bufferedImg = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);

            Graphics g = bufferedImg.getGraphics();
                
            g.drawImage(rotatedImg, 0, 0, dummyComponent);

            g.dispose();

            rotatedImg.flush();

            return bufferedImg;
        } catch (OutOfMemoryError memErr) {
            Logger.getLogger(getClass()).error("not enough memory for image rotation", memErr);
            return null;
        }
    }
}
