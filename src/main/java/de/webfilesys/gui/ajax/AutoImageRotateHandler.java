package de.webfilesys.gui.ajax;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import de.webfilesys.FileComparator;
import de.webfilesys.FileContainer;
import de.webfilesys.FileLinkSelector;
import de.webfilesys.FileSelectionStatus;
import de.webfilesys.graphics.CameraExifData;
import de.webfilesys.graphics.ImageTransformation;
import de.webfilesys.graphics.ScaledImage;
import de.webfilesys.util.XmlUtil;

/**
 * Auto-Rotation of images of a folder according to the orientation value stored
 * in the Exif data by the digicam.
 * @author Frank Hoehnel
 */
public class AutoImageRotateHandler extends XmlRequestHandlerBase
{
	public static final String imgFileMasks[] = {"*.jpg","*.jpeg"};

	public AutoImageRotateHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid)
	{
        super(req, resp, session, output, uid);
	}
	
	protected void process()
	{
		String path = getCwd();

		boolean anyImageRotated = false;
		
		FileLinkSelector fileSelector = new FileLinkSelector(path, FileComparator.SORT_BY_FILENAME, true);

		FileSelectionStatus selectionStatus = fileSelector.selectFiles(imgFileMasks, 4096, 0);

		Vector selectedFiles = selectionStatus.getSelectedFiles();
		
		if (selectedFiles != null)
		{
			for (int i = 0; i < selectedFiles.size(); i++)
			{
				FileContainer fileCont = (FileContainer) selectedFiles.elementAt(i);

				if (!fileCont.isLink()) {
					String imgFilename = fileCont.getName();
					
					String imgPath = fileCont.getRealFile().getAbsolutePath();

					ScaledImage scaledImage = null;
					
			        try
			        {
			        	scaledImage = new ScaledImage(imgPath, 1000, 1000);
			        	if (scaledImage.getImageType() == ScaledImage.IMG_TYPE_JPEG) 
			        	{
			        		if ((scaledImage.getRealWidth() % 8 == 0) && 
			        			(scaledImage.getRealHeight() % 8 == 0)) 
			        		{
			        			// only if lossless rotation possible
			        			
			        			if (rotateImageToMatchExifOrientation(imgPath, scaledImage))
			        			{
			        				anyImageRotated = true;
			        			}
			        		}
			        	}
			        }
			        catch (IOException ioex)
			        {
			            Logger.getLogger(getClass()).error(ioex);
			        }
				}
			}
		}
				
		Element resultElement = doc.createElement("result");

		doc.appendChild(resultElement);
		
		XmlUtil.setChildText(resultElement, "anyImageRotated", Boolean.toString(anyImageRotated));
		
		this.processResponse();
	}
	
	/**
	 * Extract the orientation from the EXIF data and if the orientation of the image is different
	 * try to rotate the image so that both orientations match.
	 * 
	 * @param imgPath filesystem path of the picture file
	 * @param scaledImage the scaled img object
	 * @return true if successful rotated
	 */
	private boolean rotateImageToMatchExifOrientation(String imgPath, ScaledImage scaledImage)
	{
        CameraExifData exifData = new CameraExifData(imgPath);

        if (!exifData.hasExifData())
        {
            return false;	
        }
        
        int orientation = exifData.getOrientation();
        
        if (orientation != CameraExifData.ORIENTATION_UNKNOWN)
        {
        	if ((orientation == 6) || (orientation == 8)) {
        		if (scaledImage.getRealWidth() > scaledImage.getRealHeight()) {
        			// image is landscape, but EXIF orientation is portrait
        			
        			String degrees = null;
        			if (orientation == 6) {
        				degrees = "90";
        			} else {
        				degrees = "270";
        			}
        			
        			ImageTransformation imgTrans = new ImageTransformation(imgPath, "rotate", degrees);

        			imgTrans.execute(false);
        			
        			return true;
        		}
        	}
        }
        
        return false;
	}
}
