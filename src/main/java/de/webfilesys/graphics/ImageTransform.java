package de.webfilesys.graphics;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

import mediautil.image.jpeg.LLJTran;
import mediautil.image.jpeg.LLJTranException;

import org.apache.log4j.Logger;

import de.webfilesys.WebFileSys;
import de.webfilesys.util.CommonUtils;

/**
 * Lossless image transformation using mediautil (http://mediachest.sourceforge.net/mediautil).
 */
public class ImageTransform
{
    private String sourceFilePath;

    private String action;

    private String degrees;

    ScaledImage sourceImage=null;
    
    public ImageTransform(String sourceFilePath)
    {
        this.sourceFilePath = sourceFilePath;
    }

    public ImageTransform(String sourceFileName, String action, String degrees)
    {
        this.sourceFilePath=sourceFileName;
        this.action=action;
        this.degrees=degrees;
    }

    public void setAction(String action)
    {
        this.action=action;
    }

    public void setDegrees(String degrees)
    {
        this.degrees=degrees;
    }

    public String execute(boolean keepSource)
    {
        try
        {
            sourceImage=new ScaledImage(sourceFilePath,1000,1000);
        }
        catch (IOException ioex)
        {
            Logger.getLogger(getClass()).error("ImageTransformation.execute: " + ioex);
            return(null);
        }

        if (sourceImage.getImageType()==ScaledImage.IMG_TYPE_BMP)
        {
            Logger.getLogger(getClass()).debug("ImageTransformation: ignoring BMP file " + sourceFilePath);
            return(null);
        }
        
        if ((sourceImage.getImageType() == ScaledImage.IMG_TYPE_PNG) ||
            (sourceImage.getImageType() == ScaledImage.IMG_TYPE_GIF))
        {
            return(rotateLossy());
        }

        if ((!degrees.equals("90")) && (!degrees.equals("180")) && (!degrees.equals("270"))) {
            return(rotateLossy());
        }
        
        return(transformLossless(keepSource));
    }

    public String transformLossless(boolean keepSource)
    {
        String fileNameAppendix=null;

        int operation = 0;
        if (action.equals("rotate")) {
            CameraExifData origExifData = new CameraExifData(sourceFilePath);
            
            if (origExifData.getOrientation() != CameraExifData.ORIENTATION_UNKNOWN) {
            	String resultFilePath = rotateExifOrientationOnly(origExifData, degrees);
                if (resultFilePath != null) {
                	replaceOldExternalThumbnail(resultFilePath, false);
                    return CommonUtils.extractFileName(resultFilePath);
            	}
            }    	
        	
        	if (degrees.equals("90")) {
        		operation = 5;
        	} else if (degrees.equals("180")) {
        		operation = 6;
        	} else if (degrees.equals("270")) {
        		operation = 7;
        	}
            fileNameAppendix = "-r" + degrees;
        } else if (action.equals("flipHorizontal")) {
        	operation = 1;
            fileNameAppendix = "-fh";
        } else if (action.equals("flipVertical")) {
        	operation = 2;
            fileNameAppendix = "-fv";
        }
        
        File sourceFile = new File(sourceFilePath);

        String actPath = sourceFilePath.substring(0,sourceFilePath.lastIndexOf(File.separatorChar));

        String fileName = sourceFilePath.substring(sourceFilePath.lastIndexOf(File.separatorChar)+1);

        int extIdx=fileName.lastIndexOf(".");

        String resultFileName=null;

        if (extIdx<0)
        {
             resultFileName=fileName + fileNameAppendix + ".jpg";
        }
        else
        {
             resultFileName=fileName.substring(0,extIdx) + fileNameAppendix + fileName.substring(extIdx);
        }

        String destFilePath = actPath + File.separator + resultFileName;

        
        BufferedOutputStream output = null;
        LLJTran llj = null;
        
        try {
            llj = new LLJTran(sourceFile);
            // If you pass the 2nd parameter as false, Exif information is not
            // loaded and hence will not be written.
            llj.read(LLJTran.READ_ALL, true);
            
            // int options = LLJTran.OPT_DEFAULTS | LLJTran.OPT_XFORM_ORIENTATION | LLJTran.OPT_XFORM_THUMBNAIL ;
            int options = LLJTran.OPT_DEFAULTS | LLJTran.OPT_XFORM_THUMBNAIL ;
            
            llj.transform(operation, options);    
            
            output = new BufferedOutputStream(new FileOutputStream(destFilePath));
            llj.save(output, LLJTran.OPT_WRITE_ALL);
            
        	replaceOldExternalThumbnail(destFilePath, false);
        } catch (LLJTranException ex) {
        	Logger.getLogger(getClass()).error("failed to transform image " + sourceFilePath, ex);
        } catch (IOException ioex) {
        	Logger.getLogger(getClass()).error("failed to transform image " + sourceFilePath, ioex);
        } finally {
            if (output != null) {
            	try {
            		output.close();
            	} catch (Exception ex) {
            	}
            }

        	if (llj != null) {
                llj.freeMemory();        	
        	}
        }

        if (!keepSource)
        {
            if ((sourceImage.getRealWidth() % 8 == 0) &&
                (sourceImage.getRealHeight() % 8 == 0))
            {
                try
                {
                    ScaledImage destImage=new ScaledImage(destFilePath,1000,1000);

                    if (((sourceImage.getRealWidth() == destImage.getRealHeight()) &&
                         (sourceImage.getRealHeight() == destImage.getRealWidth())) ||
                        ((sourceImage.getRealWidth() == destImage.getRealWidth()) &&
                         (sourceImage.getRealHeight() == destImage.getRealHeight())))
                    {
                        if (!sourceFile.delete())
                        {
                            Logger.getLogger(getClass()).error("cannot delete source file " + sourceFilePath + " after transformation");
                        }
                    }
                }
                catch (IOException ioex2)
                {
                    Logger.getLogger(getClass()).error("ImageTransformation.execute: " + ioex2);
                    return(resultFileName);
                }
            }
        }

		if (WebFileSys.getInstance().isAutoCreateThumbs())
		{
			AutoThumbnailCreator.getInstance().queuePath(destFilePath, AutoThumbnailCreator.SCOPE_FILE);
		}

        return(resultFileName);
    }

    private String rotateExifOrientationOnly(CameraExifData origExifData, String degrees) {
    	
        File sourceFile = new File(sourceFilePath);

        int newOrientationValue = calculateNewExifOrientation(origExifData.getOrientation(), degrees);
        if (!ExifUtil.setExifOrientation(sourceFile, newOrientationValue)) {
        	Logger.getLogger(getClass()).error("failed to set exif orientation for file " + sourceFilePath);
            return null;
        }
        
        String targetPath = sourceFilePath.substring(0, sourceFilePath.lastIndexOf('.')) + "-r" + degrees + sourceFilePath.substring(sourceFilePath.lastIndexOf('.'));
        File targetFile = new File(targetPath);
        if (!sourceFile.renameTo(targetFile)) {
        	Logger.getLogger(getClass()).error("failed to rename rotated picture file to " + targetPath);
        }
        
        return targetPath;
    }

    private int calculateNewExifOrientation(int oldOrientation, String degrees) {
    	if ("270".equals(degrees)) {
    		if (oldOrientation == 1) {
    			return 8;
    		}
    		if (oldOrientation == 6) {
    			return 1;
    		}
    		if (oldOrientation == 3) {
    			return 6;
    		}
    		if (oldOrientation == 8) {
    			return 3;
    		}
    	} else if ("90".equals(degrees)) {
    		if (oldOrientation == 1) {
    			return 6;
    		}
    		if (oldOrientation == 6) {
    			return 3;
    		}
    		if (oldOrientation == 3) {
    			return 8;
    		}
    		if (oldOrientation == 8) {
    			return 1;
    		}
    	} else if ("180".equals(degrees)) {
    		if (oldOrientation == 1) {
    			return 3;
    		}
    		if (oldOrientation == 6) {
    			return 8;
    		}
    		if (oldOrientation == 3) {
    			return 1;
    		}
    		if (oldOrientation == 8) {
    			return 6;
    		}
    	} 
  		return oldOrientation;
    }
    
    protected String rotateLossy()
    {
        String fileNameAppendix = "-r" + degrees;
        double numericDegrees;
        
        try {
        	numericDegrees = 360 - Double.parseDouble(degrees);
        } catch (NumberFormatException numEx) {
            Logger.getLogger(getClass()).warn("invalid value for rotation degrees: " + degrees);
            return null;
        }
    	
        String actPath=sourceFilePath.substring(0,sourceFilePath.lastIndexOf(File.separatorChar));

        String fileName=sourceFilePath.substring(sourceFilePath.lastIndexOf(File.separatorChar)+1);

        int extIdx=fileName.lastIndexOf(".");

        String resultFileName=null;

        if (extIdx<0)
        {
            if (sourceImage.getImageType()==ScaledImage.IMG_TYPE_JPEG)
            {
                resultFileName=fileName + fileNameAppendix + ".jpg";
            }
            else
            {
                resultFileName=fileName + fileNameAppendix + ".png";
            }
        }
        else
        {
            String ext=fileName.substring(extIdx);

            if (ext.equalsIgnoreCase(".GIF"))
            {
                resultFileName=fileName.substring(0,extIdx) + fileNameAppendix + ".png";
            }
            else
            {
                resultFileName=fileName.substring(0,extIdx) + fileNameAppendix + fileName.substring(extIdx);
            }
        }

        String destFilePath = actPath + File.separator + resultFileName;

        rotateImage(sourceFilePath, destFilePath , numericDegrees);

		if (WebFileSys.getInstance().isAutoCreateThumbs()) {
			AutoThumbnailCreator.getInstance().queuePath(destFilePath , AutoThumbnailCreator.SCOPE_FILE);
		} else {
        	replaceOldExternalThumbnail(destFilePath, true);
		}

        return(resultFileName);
    }

    protected void rotateImage(String imgFileName,String resultFileName,double degree) {
        int imageWidth = sourceImage.getRealWidth();
        int imageHeight = sourceImage.getRealHeight();

        int newWidth;
        int newHeight;
        
        int cropBorderWidth = (-1);
        int cropBorderHeight = (-1);
        
        if ((degree == 90) || (degree == 270)) {
            newWidth = imageHeight;
            newHeight = imageWidth;
        } else if (degree == 180) {
            newWidth = imageWidth;
            newHeight = imageHeight;
        } else {
        	double radiant = (360 - degree) * Math.PI / 180;
        	
        	double absSinus = Math.abs(Math.sin(radiant));
        	double absCosinus = Math.abs(Math.cos(radiant));
        	
            double rotatedWidth = (imageHeight * absSinus) + (imageWidth * absCosinus);
            double rotatedHeight = (imageWidth * absSinus) + (imageHeight * absCosinus);
            
            newWidth = (int) Math.round(rotatedWidth);
            newHeight = (int) Math.round(rotatedHeight);
            
            if ((degree <= 5) || (degree >= 355)) {
                cropBorderWidth = (int) Math.round(imageHeight * absSinus);
                cropBorderHeight = (int) Math.round(imageWidth * absSinus);
                
                newWidth = newWidth - (2 * cropBorderWidth);
                newHeight = newHeight - (2 * cropBorderHeight);
            }
        }
        
        Image origImage = Toolkit.getDefaultToolkit().createImage(imgFileName);

        Canvas dummyComponent=new Canvas();

        MediaTracker tracker = new MediaTracker(dummyComponent);
        tracker.addImage (origImage,0);

        try {
            tracker.waitForAll();
        } catch (Exception ex) {
            Logger.getLogger(getClass()).warn("failed to load image for rotation", ex);
        }

        tracker.removeImage(origImage);
        
        Image rotatedImg;

        try {
            ImageFilter filter = new RotateFilter((Math.PI / 180) * degree);
            ImageProducer producer = new FilteredImageSource(origImage.getSource(),filter);
            rotatedImg = dummyComponent.createImage(producer);

            tracker.addImage (rotatedImg,1);

            try {
                tracker.waitForAll();
            } catch(InterruptedException intEx2) {
               Logger.getLogger(getClass()).error("rotateImage: " + intEx2);
            }

            tracker.removeImage(rotatedImg);

            origImage.flush();
            
            BufferedImage bufferedImg = null;

            Graphics g = null;

            bufferedImg = new BufferedImage(newWidth,newHeight,BufferedImage.TYPE_INT_RGB);

            g = bufferedImg.getGraphics();
                
            if (cropBorderWidth > 0) {
                g.drawImage(rotatedImg, 
                            0, 0, newWidth, newHeight, 
                            cropBorderWidth, cropBorderHeight, cropBorderWidth + newWidth, cropBorderHeight + newHeight,
                            Color.BLACK, null);
                	
            } else {
                g.drawImage(rotatedImg,0,0,null);
            }

            g.dispose();

            rotatedImg.flush();
            
            FileOutputStream rotatedFile = null;
            
            if (sourceImage.getImageType() == ScaledImage.IMG_TYPE_JPEG) {
                BufferedOutputStream rotatedOut = null;
                
                try {
                    Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpg");
                    ImageWriter imgWriter = (ImageWriter) iter.next();
                    rotatedFile = new FileOutputStream(resultFileName);
                    rotatedOut = new BufferedOutputStream(rotatedFile);
                    ImageOutputStream ios = ImageIO.createImageOutputStream(rotatedOut);
                    imgWriter.setOutput(ios);
                    ImageWriteParam iwparam = new JPEGImageWriteParam(Locale.getDefault());
                    iwparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    iwparam.setCompressionQuality(0.9f);
                    imgWriter.write(null, new IIOImage(bufferedImg, null, null), iwparam);
                    ios.flush();
                    imgWriter.dispose();
                } catch (IOException ioex) {
                    Logger.getLogger(ImageTransform.class).error("error writing rotated JPEG file " + resultFileName, ioex);
                } finally {
                    if (rotatedOut != null) {
                        try {
                            rotatedOut.close();
                        } catch (Exception ex) {
                            Logger.getLogger(ImageTransform.class).error("error closing rotated JPEG file", ex);
                        }
                    }
                }
            } else {
                try {
                    rotatedFile = new FileOutputStream(resultFileName);
                    
                    byte[] pngBytes;
                    com.keypoint.PngEncoder pngEncoder = new com.keypoint.PngEncoderB(bufferedImg);

                    // pngEncoder.setCompressionLevel(1);
                    // pngEncoder.setFilter(com.keypoint.PngEncoder.FILTER_LAST);

                    pngBytes = pngEncoder.pngEncode();

                    if (pngBytes == null) {
                        Logger.getLogger(getClass()).warn("PNG Encoder : Null image");
                    } else {
                        rotatedFile.write(pngBytes);
                    }
                    
                    rotatedFile.flush();
                } catch (IOException ioex1) {
                    Logger.getLogger(getClass()).error("rotateImage: " + ioex1);
                    return;
                } finally {
                    if (rotatedFile != null) {
                        try {
                        	rotatedFile.close();
                        } catch (Exception ex) {
                        }
                    }
                }
            }

            if (bufferedImg != null) {
                bufferedImg.flush();
            }
        } catch (OutOfMemoryError memErr) {
            Logger.getLogger(getClass()).error("not enough memory for image rotation", memErr);
            return;
        }
    }

    /**
     * Convenience method that returns a scaled instance of the provided
     * {@code BufferedImage}. Code by Chris Campbell
     * (http://today.java.net/pub/a/today/2007/04/03/perils-of-image-getscaledinstance.html).
     * 
     * @param img
     *            the original image to be scaled
     * @param targetWidth
     *            the desired width of the scaled instance, in pixels
     * @param targetHeight
     *            the desired height of the scaled instance, in pixels
     * @param hint
     *            one of the rendering hints that corresponds to
     *            {@code RenderingHints.KEY_INTERPOLATION} (e.g.
     *            {@code RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR},
     *            {@code RenderingHints.VALUE_INTERPOLATION_BILINEAR},
     *            {@code RenderingHints.VALUE_INTERPOLATION_BICUBIC})
     * @param higherQuality
     *            if true, this method will use a multi-step scaling technique
     *            that provides higher quality than the usual one-step technique
     *            (only useful in downscaling cases, where {@code targetWidth}
     *            or {@code targetHeight} is smaller than the original
     *            dimensions, and generally only when the {@code BILINEAR} hint
     *            is specified)
     * @return a scaled version of the original {@code BufferedImage}
     */
    public static BufferedImage getScaledInstance(BufferedImage img, int targetWidth,
            int targetHeight, Object hint, boolean higherQuality)
    {
        int type = (img.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB
                : BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = (BufferedImage) img;
        int w, h;
        if (higherQuality)
        {
            // Use multi-step technique: start with original size, then
            // scale down in multiple passes with drawImage()
            // until the target size is reached
            w = img.getWidth();
            h = img.getHeight();
        }
        else
        {
            // Use one-step technique: scale directly from original
            // size to target size with a single drawImage() call
            w = targetWidth;
            h = targetHeight;
        }

        do
        {
            if (higherQuality && w > targetWidth)
            {
                w /= 2;
                if (w < targetWidth)
                {
                    w = targetWidth;
                }
            }

            if (higherQuality && h > targetHeight)
            {
                h /= 2;
                if (h < targetHeight)
                {
                    h = targetHeight;
                }
            }

            BufferedImage tmp = new BufferedImage(w, h, type);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.drawImage(ret, 0, 0, w, h, null);
            g2.dispose();

            ret = tmp;
        }
        while (w != targetWidth || h != targetHeight);

        return ret;
    }
    
	private boolean replaceOldExternalThumbnail(String targetFilePath, boolean keepOld) {
		String thumbnailFilePath = ThumbnailCreatorBase.getThumbnailPath(sourceFilePath);
		if (thumbnailFilePath == null) {
			return false;
		}
		
		File thumbnailFile = new File(thumbnailFilePath);
		if (!thumbnailFile.exists() || !thumbnailFile.isFile()) {
			return false;
		}
		if (!keepOld) {
			if (!thumbnailFile.delete()) {
	            Logger.getLogger(getClass()).warn("failed to delete old thumbnail file " + thumbnailFile.getAbsolutePath());
				return false;
			}
		}
		
		AutoThumbnailCreator.getInstance().queuePath(targetFilePath, AutoThumbnailCreator.SCOPE_FILE);
		
		return true;
	}

}
