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
import de.webfilesys.WebFileSys;
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
		
		addMsgResource("confirm.delfile", getResource("confirm.delfile","Are you sure you want to delete this picture?"));
		addMsgResource("confirm.print", getResource("confirm.print","Do you want to print this picture?"));

		addMsgResource("rating.owner", getResource("rating.owner","Rating by owner"));
		addMsgResource("rating.visitor", getResource("rating.visitor","Rating by visitors"));

		addMsgResource("rating.notYetRated", getResource("rating.notYetRated","not yet rated"));
		addMsgResource("rating.rateNow", getResource("rating.rateNow","Rate now"));

		addMsgResource("rating.1star", getResource("rating.1star","1 star - worst"));
		addMsgResource("rating.2stars", getResource("rating.2stars","2 stars"));
		addMsgResource("rating.3stars", getResource("rating.3stars","3 stars"));
		addMsgResource("rating.4stars", getResource("rating.4stars","4 stars"));
		addMsgResource("rating.5stars", getResource("rating.5stars","5 stars - best"));

		addMsgResource("label.comments", getResource("label.comments","Comments"));
		addMsgResource("label.picture", getResource("label.picture","Bild"));

		addMsgResource("label.origSize", getResource("label.origSize","Original Size"));
		addMsgResource("label.delete", getResource("label.delete","Delete"));
		addMsgResource("label.editPicture", getResource("label.editPicture", "Edit/Convert"));
		addMsgResource("alt.cameradata", getResource("alt.cameradata","Camera Data"));
		addMsgResource("alt.printpict", getResource("alt.printpict","Print Picture"));

		addMsgResource("label.selectFunction", getResource("label.selectFunction","- select function -"));

		String description = metaInfMgr.getDescription(imgPath);

        if ((description != null) && (description.length() > 0))
        {
			XmlUtil.setChildText(imageDataElement, "description", CommonUtils.readyForJavascript(description), true);
        }
		
		int numberOfComments = metaInfMgr.countComments(imgPath);

		XmlUtil.setChildText(imageDataElement, "commentCount", Integer.toString(numberOfComments), false);

		String srcFileName = "/webfilesys/servlet?command=getFile&filePath=" + UTF8URLEncoder.encode(imgPath);
		
		XmlUtil.setChildText(imageDataElement, "imageSource", srcFileName, false);

		int screenWidth = Constants.DEFAULT_SCREEN_WIDTH;
		int screenHeight = Constants.DEFAULT_SCREEN_HEIGHT;
		
		Integer screenWidthSession = (Integer) session.getAttribute("screenWidth");
		
		if (screenWidthSession != null)
		{
			screenWidth = screenWidthSession.intValue();
		}
		
		Integer screenHeightSession = (Integer) session.getAttribute("screenHeight");
		
		if (screenHeightSession != null)
		{
			screenHeight = screenHeightSession.intValue();
		}
		
		// int maxDisplayWidth = screenWidth-100;
		int maxDisplayWidth = screenWidth-40;
		
		int maxDisplayHeight = screenHeight-155;
		
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
			
	        addMsgResource("label.geoMapLink", getResource("label.geoMapLink", "Show geographic location on map"));
	        addMsgResource("selectMapType", getResource("selectMapType", "- select map type -"));
	        addMsgResource("mapTypeOSM", getResource("mapTypeOSM", "Open Stree Maps"));
	        addMsgResource("mapTypeGoogleMap", getResource("mapTypeGoogleMap", "Google Maps"));
	        addMsgResource("mapTypeGoogleEarth", getResource("mapTypeGoogleEarth", "Google Earth"));
		}
		
		String googleMapsAPIKey = null;
		if (req.getScheme().equalsIgnoreCase("https"))
		{
			googleMapsAPIKey = WebFileSys.getInstance().getGoogleMapsAPIKeyHTTPS();
		}
		else
		{
			googleMapsAPIKey = WebFileSys.getInstance().getGoogleMapsAPIKeyHTTP();
		}
		if (googleMapsAPIKey != null) {
			XmlUtil.setChildText(imageDataElement, "googleMaps", "true", false);
		}
		
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