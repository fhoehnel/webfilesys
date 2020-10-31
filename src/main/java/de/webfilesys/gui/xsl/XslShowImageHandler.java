package de.webfilesys.gui.xsl;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.Constants;
import de.webfilesys.GeoTag;
import de.webfilesys.MetaInfManager;
import de.webfilesys.PictureRating;
import de.webfilesys.graphics.CameraExifData;
import de.webfilesys.graphics.ScaledImage;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.UTF8URLEncoder;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslShowImageHandler extends XslRequestHandlerBase
{
	public XslShowImageHandler(
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
		String imgPath = getParameter("imgname");

		if (!this.checkAccess(imgPath))
		{
			return;
		}

		MetaInfManager metaInfMgr = MetaInfManager.getInstance();

		Element imageDataElement = doc.createElement("imageData");
			
		doc.appendChild(imageDataElement);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/showImage.xsl\"");

		doc.insertBefore(xslRef, imageDataElement);

		XmlUtil.setChildText(imageDataElement, "imagePath", imgPath, false);
		XmlUtil.setChildText(imageDataElement, "encodedPath", UTF8URLEncoder.encode(imgPath), false);
		XmlUtil.setChildText(imageDataElement, "pathForScript", escapeForJavascript(imgPath), false);
		XmlUtil.setChildText(imageDataElement, "relativePath", getHeadlinePath(imgPath), false);
		
		if (readonly)
		{
			XmlUtil.setChildText(imageDataElement, "readonly", "true", false);
		}
		else
		{
			XmlUtil.setChildText(imageDataElement, "readonly", "false", false);
		}

		String description = metaInfMgr.getDescription(imgPath);

        if ((description != null) && (description.length() > 0))
        {
			XmlUtil.setChildText(imageDataElement, "description", CommonUtils.readyForJavascript(description), true);
        }
		
		int numberOfComments = metaInfMgr.countComments(imgPath);

		XmlUtil.setChildText(imageDataElement, "commentCount", Integer.toString(numberOfComments), false);

		String srcFileName = "/webfilesys/servlet?command=getFile&filePath=" + UTF8URLEncoder.encode(imgPath);
		
		XmlUtil.setChildText(imageDataElement, "imageSource", srcFileName, false);
		
		ScaledImage scaledImage = null;

		try
		{
			scaledImage = new ScaledImage(imgPath, 1000, 1000);
		}
		catch (IOException ioEx)
		{
			Logger.getLogger(getClass()).error(ioEx.toString());
			XmlUtil.setChildText(imageDataElement, "error", ioEx.toString(), false);
			processResponse("xsl/showImage.xsl");
			return;
		}

		XmlUtil.setChildText(imageDataElement, "imageType", Integer.toString(scaledImage.getImageType()), false);
		
		
		int imageWidth = scaledImage.getRealWidth();
		int imageHeight = scaledImage.getRealHeight();

		if (scaledImage.getImageType() == ScaledImage.IMG_TYPE_JPEG) {
        	CameraExifData exifData = new CameraExifData(imgPath);
            int orientation = exifData.getOrientation();
            if (orientation == 6 || orientation == 8) {
            	// the browser will rotate the picture for display, so we have to shape the browser window accordingly
            	int swap = imageWidth;
            	imageWidth = imageHeight;
            	imageHeight = swap;
            }
		}		
		
		XmlUtil.setChildText(imageDataElement, "imageWidth", Integer.toString(imageWidth), false);
		XmlUtil.setChildText(imageDataElement, "imageHeight", Integer.toString(imageHeight), false);

        PictureRating pictureRating = metaInfMgr.getPictureRating(imgPath);
        
        if (pictureRating != null)
        {
            if (pictureRating.getOwnerRating() >= 0)
            {
                XmlUtil.setChildText(imageDataElement, "ownerRating", Integer.toString(pictureRating.getOwnerRating()), false);
            }
            
            if (pictureRating.getNumberOfVotes() > 0)
            {
                XmlUtil.setChildText(imageDataElement, "visitorRating", Integer.toString(pictureRating.getAverageVisitorRating()), false);
                XmlUtil.setChildText(imageDataElement, "voteCount", Integer.toString(pictureRating.getNumberOfVotes()), false);
            }
        }
		
		boolean alreadyRated = false;
		
		Hashtable ratedPictures = (Hashtable) session.getAttribute("ratedPictures");
		
		if (ratedPictures != null)
		{
			if (ratedPictures.get(imgPath) != null)
			{
				alreadyRated = true;
			}
		}
		
	    boolean ratingAllowed = (!readonly) || (!alreadyRated);
        
        String ratingParm = "false";
        
        if (ratingAllowed)
        {
        	ratingParm = "true";
        }
        
		XmlUtil.setChildText(imageDataElement, "ratingAllowed", ratingParm, false);

		GeoTag geoTag = metaInfMgr.getGeoTag(imgPath);
		
		boolean exifGpsDataPresent = false;
		
		if (geoTag == null)
		{
		    exifGpsDataPresent = hasGpsExifData(imgPath);
		}
		
		if ((geoTag != null) || exifGpsDataPresent)
		{
			XmlUtil.setChildText(imageDataElement, "geoTag", "true", false);
		}
		
        // the reason for this is historic: previous google maps api version required an API key
		XmlUtil.setChildText(imageDataElement, "googleMaps", "true", false);
		
		processResponse("showImage.xsl");
    }
	
	private boolean hasGpsExifData(String path)
	{
        String fileExt = CommonUtils.getFileExtension(path);
        
        if (fileExt.equals(".jpg") || fileExt.equals(".jpeg"))
        {
            CameraExifData exifData = new CameraExifData(path);

            if (exifData.hasExifData())
            {
                float gpsLatitude = exifData.getGpsLatitude();
                float gpsLongitude = exifData.getGpsLongitude();
                
                return((gpsLatitude >= 0.0f) && (gpsLongitude >= 0.0f));
            }
        }
	  
        return false;
	}
}