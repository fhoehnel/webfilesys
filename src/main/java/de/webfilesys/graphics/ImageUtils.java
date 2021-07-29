package de.webfilesys.graphics;

import org.apache.log4j.Logger;

public class ImageUtils {

	public static ImageDimensions getScaledDimensions(int origWidth, int origHeight, int maxWidth, int maxHeight) {
		int yscale = 100000;
		int xscale = 100000;

		if (origHeight > maxHeight) {
			yscale = maxHeight * 100000 / origHeight;
		}

		if (origWidth > maxWidth) {
			xscale = maxWidth * 100000 / origWidth;
		}

		if (yscale < xscale) {
			return new ImageDimensions(origWidth * yscale / 100000 + 1, maxHeight);
		} 

		return new ImageDimensions(maxWidth, origHeight * xscale / 100000 + 1);
	}

	public static ImageDimensions getScaledImageDimensions(String imgPath, int maxWidth, int maxHeight) {
		
		ImageDimensions scaledDim = null;

    	CameraExifData exifData = new CameraExifData(imgPath);

		if (exifData.hasExifData() && (exifData.getImageHeigth() > 0)) {
            int orientation = exifData.getOrientation();
            if (orientation == 6 || orientation == 8) {
            	// portrait - the browser will rotate the picture for display
            	scaledDim = ImageUtils.getScaledDimensions(exifData.getImageHeigth(), exifData.getImageWidth(), maxWidth, maxHeight);
                scaledDim.setOrigWidth(exifData.getImageHeigth());
                scaledDim.setOrigHeight(exifData.getImageWidth());
            } else {
            	scaledDim = ImageUtils.getScaledDimensions(exifData.getImageWidth(), exifData.getImageHeigth(), maxWidth, maxHeight);
                scaledDim.setOrigWidth(exifData.getImageWidth());
                scaledDim.setOrigHeight(exifData.getImageHeigth());
            }
        } else {
        	try {
                ScaledImage scaledImage = new ScaledImage(imgPath, maxWidth, maxHeight);
        		scaledDim = new ImageDimensions(scaledImage.getScaledWidth(), scaledImage.getScaledHeight());
                scaledDim.setOrigWidth(scaledImage.getRealWidth());
                scaledDim.setOrigHeight(scaledImage.getRealHeight());
        	} catch (Exception ex) {
                Logger.getLogger(ImageUtils.class).error("failed to get image dimension for file " + imgPath, ex);
        		scaledDim = new ImageDimensions(100, 100);
        	}
        }
		
		return scaledDim;
	}
	
}
