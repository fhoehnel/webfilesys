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

		XmlUtil.setChildText(imageDataElement, "css", userMgr.getCSS(uid), false);
	    XmlUtil.setChildText(imageDataElement, "language", language, false);
		
		XmlUtil.setChildText(imageDataElement, "imagePath", imgPath, false);
		XmlUtil.setChildText(imageDataElement, "encodedPath", UTF8URLEncoder.encode(imgPath), false);
		XmlUtil.setChildText(imageDataElement, "pathForScript", insertDoubleBackslash(imgPath), false);
		XmlUtil.setChildText(imageDataElement, "relativePath", this.getHeadlinePath(imgPath), false);
		
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

		int screenWidth = 0;
		int screenHeight = 0;
		
		String screenWidthParam = getParameter("screenWidth");
        
		if (screenWidthParam != null)
		{
			try
			{
				screenWidth = Integer.parseInt(screenWidthParam);        	
				session.setAttribute("screenWidth", new Integer(screenWidth));
			}
			catch (NumberFormatException nfe)
			{
				Logger.getLogger(getClass()).error(nfe);
			}
		}
		
		String screenHeightParam = getParameter("screenHeight");
        
		if (screenHeightParam != null)
		{
			try
			{
				screenHeight = Integer.parseInt(screenHeightParam);        	
				session.setAttribute("screenHeight", new Integer(screenHeight));
			}
			catch (NumberFormatException nfe)
			{
				Logger.getLogger(getClass()).error(nfe);
			}
		}
		
		if (screenWidth == 0) {
			Integer screenWidthSession = (Integer) session.getAttribute("screenWidth");
			if (screenWidthSession != null)
			{
				screenWidth = screenWidthSession.intValue();
			}
			else
			{
				screenWidth = Constants.DEFAULT_SCREEN_WIDTH;
			}
		}
		
		if (screenHeight == 0) {
			Integer screenHeightSession = (Integer) session.getAttribute("screenHeight");
			
			if (screenHeightSession != null)
			{
				screenHeight = screenHeightSession.intValue();
			}
			else
			{
				screenHeight = Constants.DEFAULT_SCREEN_HEIGHT;
			}
		}
		
		// int maxDisplayWidth = screenWidth-100;
		int maxDisplayWidth = screenWidth-40;
		
		int maxDisplayHeight = screenHeight - 166;
		
		ScaledImage scaledImage=null;

		try
		{
			scaledImage = new ScaledImage(imgPath, maxDisplayWidth, maxDisplayHeight);
		}
		catch (IOException io1)
		{
			Logger.getLogger(getClass()).error(io1.toString());
			this.processResponse("xsl/showImage.xsl", true);
			return;
		}

		XmlUtil.setChildText(imageDataElement, "imageType", Integer.toString(scaledImage.getImageType()), false);
		
		int xsize = scaledImage.getRealWidth();
		int ysize = scaledImage.getRealHeight();

		int xDisplay = xsize;
		int yDisplay = ysize;

		if ((xsize > maxDisplayWidth) || (ysize > maxDisplayHeight))
		{
			xDisplay = scaledImage.getScaledWidth();
			yDisplay = scaledImage.getScaledHeight();
		}
		
		XmlUtil.setChildText(imageDataElement, "imageWidth", Integer.toString(xsize), false);
		XmlUtil.setChildText(imageDataElement, "imageHeight", Integer.toString(ysize), false);

		XmlUtil.setChildText(imageDataElement, "displayWidth", Integer.toString(xDisplay), false);
		XmlUtil.setChildText(imageDataElement, "displayHeight", Integer.toString(yDisplay), false);

		if (xDisplay < scaledImage.getRealWidth())
        {
			XmlUtil.setChildText(imageDataElement, "scaled", "true", false);
        }
		
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
		
		processResponse("showImage.xsl", true);
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