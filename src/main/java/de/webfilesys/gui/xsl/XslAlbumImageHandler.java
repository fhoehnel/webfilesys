package de.webfilesys.gui.xsl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.Comment;
import de.webfilesys.Constants;
import de.webfilesys.GeoTag;
import de.webfilesys.InvitationManager;
import de.webfilesys.LanguageManager;
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
public class XslAlbumImageHandler extends XslRequestHandlerBase
{
	public XslAlbumImageHandler(
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
		String realPath = getParameter("realPath");
		
		String imgName = null;
		
		String actPath = null;
		
		String imgPath = null;

		if (realPath != null)
		{
			imgPath = realPath;
			
			int sepIdx = realPath.lastIndexOf(File.separator);
			
			if ((sepIdx > 0) && (sepIdx < realPath.length() - 1))
			{
				imgName = realPath.substring(sepIdx + 1);
			}
		}
		else
		{
			imgName = getParameter("imgName");
		
			actPath = getCwd();
		
			if (actPath.endsWith(File.separator))
			{
				imgPath = actPath + imgName;
			}
			else
			{
				imgPath = actPath + File.separator + imgName;
			}
		}

		if (!this.checkAccess(imgPath))
		{
			return;
		}

		int winWidth = 0;

		String windowWidth = getParameter("windowWidth");
        
		if (windowWidth != null)
		{
			try
			{
				winWidth = Integer.parseInt(windowWidth);        	
			}
			catch (NumberFormatException nfe)
			{
				Logger.getLogger(getClass()).error(nfe);
			}
		}
        
		if (winWidth == 0)
		{
			Integer screenWidth = (Integer) session.getAttribute("screenWidth");
			
			if (screenWidth != null)
			{
				winWidth = screenWidth.intValue();
			}
			else
			{
				winWidth = Constants.DEFAULT_SCREEN_WIDTH;
			}
		}

		int winHeight = 0;

		String windowHeight = getParameter("windowHeight");
        
		if (windowHeight != null)
		{
			try
			{
				winHeight = Integer.parseInt(windowHeight);        	
			}
			catch (NumberFormatException nfe)
			{
				Logger.getLogger(getClass()).error(nfe);
			}
		}
        
		if (winHeight == 0)
		{
			Integer screenHeight = (Integer) session.getAttribute("screenHeight");
			
			if (screenHeight != null)
			{
				winWidth = screenHeight.intValue();
			}
			else
			{
				winWidth = Constants.DEFAULT_SCREEN_HEIGHT;
			}
		}

		MetaInfManager metaInfMgr = MetaInfManager.getInstance();

		Element imageDataElement = doc.createElement("imageData");
			
		doc.appendChild(imageDataElement);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/albumImage.xsl\"");

		doc.insertBefore(xslRef, imageDataElement);

		XmlUtil.setChildText(imageDataElement, "css", userMgr.getCSS(uid), false);
		XmlUtil.setChildText(imageDataElement, "imagePath", imgPath, false);
		XmlUtil.setChildText(imageDataElement, "imageName", imgName, false);
		XmlUtil.setChildText(imageDataElement, "encodedPath", UTF8URLEncoder.encode(imgPath), false);
		XmlUtil.setChildText(imageDataElement, "pathForScript", insertDoubleBackslash(imgPath), false);
		XmlUtil.setChildText(imageDataElement, "relativePath", this.getHeadlinePath(imgPath), false);
		XmlUtil.setChildText(imageDataElement, "userid", uid, false);
		
		if (readonly)
		{
			XmlUtil.setChildText(imageDataElement, "readonly", "true", false);
		}
		else
		{
			XmlUtil.setChildText(imageDataElement, "readonly", "false", false);
		}
		
		addMsgResource("label.albumTitle", getResource("label.albumTitle","WebFileSys Picture Album"));
		
		addMsgResource("label.about", getResource("label.about","About WebFileSys"));

		addMsgResource("confirm.print", getResource("confirm.print","Do you want to print this picture?"));

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

		addMsgResource("showPictureOrigSize", getResource("showPictureOrigSize","show picture in full size"));

		addMsgResource("label.description", getResource("label.description","Description"));

		addMsgResource("label.selectFunction", getResource("label.selectFunction","- select function -"));

		addMsgResource("label.addcomment", getResource("label.addcomment","Add your comment"));
		addMsgResource("label.commentAuthor", getResource("label.commentAuthor", "your name"));
		addMsgResource("button.addComment", getResource("button.addComment","Add Comment"));
		addMsgResource("button.return", getResource("button.return","Return"));
		addMsgResource("button.returnToAlbum", getResource("button.returnToAlbum", "Return to Album"));

		// path section
		
		String docRoot = userMgr.getDocumentRoot(uid);

		if (actPath == null)
		{
			actPath = userMgr.getDocumentRoot(uid);
		}
			
		String relativePath = actPath.substring(docRoot.length());
		
		Element currentPathElem = doc.createElement("currentPath");
		
		imageDataElement.appendChild(currentPathElem);
		
		currentPathElem.setAttribute("path", relativePath);
		
		Element partOfPathElem = doc.createElement("pathElem");
			
		currentPathElem.appendChild(partOfPathElem);
			
		partOfPathElem.setAttribute("name", uid);
			
		partOfPathElem.setAttribute("path", File.separator);
		
		StringTokenizer pathParser = new StringTokenizer(relativePath, File.separator);
		
		StringBuffer partialPath = new StringBuffer();
		
		while (pathParser.hasMoreTokens())
		{
			String partOfPath = pathParser.nextToken();
			
			partialPath.append(partOfPath);
			
			if (pathParser.hasMoreTokens())
			{
				partialPath.append(File.separatorChar);		
			}
			
			partOfPathElem = doc.createElement("pathElem");
			
			currentPathElem.appendChild(partOfPathElem);
			
			partOfPathElem.setAttribute("name", partOfPath);
			
			partOfPathElem.setAttribute("path", UTF8URLEncoder.encode(partialPath.toString()));
		}
		
		// end path section

		String description = metaInfMgr.getDescription(imgPath);

        if ((description != null) && (description.length() > 0))
        {
			XmlUtil.setChildText(imageDataElement, "description", description, true);
        }
		
		/*
		int numberOfComments = metaInfMgr.countComments(imgPath);

		XmlUtil.setChildText(imageDataElement, "commentCount", Integer.toString(numberOfComments), false);
        */
		
		String srcFileName = "/webfilesys/servlet?command=getFile&filePath=" + UTF8URLEncoder.encode(imgPath);

		XmlUtil.setChildText(imageDataElement, "imageSource", srcFileName, false);

		ScaledImage scaledImage=null;

		try
		{
			scaledImage = new ScaledImage(imgPath, winWidth-50, winHeight-80);
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

		int xDisplay = scaledImage.getScaledWidth();
		int yDisplay = scaledImage.getScaledHeight();
		
		XmlUtil.setChildText(imageDataElement, "imageWidth", Integer.toString(xsize), false);
		XmlUtil.setChildText(imageDataElement, "imageHeight", Integer.toString(ysize), false);

		XmlUtil.setChildText(imageDataElement, "displayWidth", Integer.toString(xDisplay), false);
		XmlUtil.setChildText(imageDataElement, "displayHeight", Integer.toString(yDisplay), false);

		if (xDisplay < scaledImage.getRealWidth())
        {
			XmlUtil.setChildText(imageDataElement, "scaled", "true", false);
        }
        
		String imgPathOS = imgPath.replace('/', File.separatorChar);
		
		PictureRating pictureRating = metaInfMgr.getPictureRating(imgPathOS);
		
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

		if (realPath == null)
		{
			boolean alreadyRated = false;
			
			Hashtable ratedPictures = (Hashtable) session.getAttribute("ratedPictures");
			
			if (ratedPictures != null)
			{
				if (ratedPictures.get(imgPathOS) != null)
				{
					alreadyRated = true;
				}
			}
			
			boolean ratingAllowed = (!readonly) || (!alreadyRated);
        
			if (ratingAllowed)
			{
				XmlUtil.setChildText(imageDataElement, "ratingAllowed", "true");
			}

			boolean addCommentsAllowed = true;

			if (userMgr.getUserType(uid).equals("virtual"))
			{
				addCommentsAllowed = InvitationManager.getInstance().commentsAllowed(uid);
			}

			if (addCommentsAllowed)
			{
				XmlUtil.setChildText(imageDataElement, "addCommentsAllowed", "true");
			}
		}

		Vector listOfComments = MetaInfManager.getInstance().getListOfComments(imgPath);

        if ((listOfComments != null) && (listOfComments.size() > 0))
        {        
        	Element commentListElement = doc.createElement("comments");
        	
        	imageDataElement.appendChild(commentListElement);
        	
        	commentListElement.setAttribute("count", Integer.toString(listOfComments.size()));
        	
			SimpleDateFormat dateFormat = LanguageManager.getInstance().getDateFormat(language);

			for (int i = 0; i < listOfComments.size(); i++)
			{
				Comment comment=(Comment) listOfComments.elementAt(i);

				Element commentElement = doc.createElement("comment");
        	
				commentListElement.appendChild(commentElement);

				String login = comment.getUser();

				StringBuffer userString=new StringBuffer();

				if (!userMgr.userExists(login))
				{
					// anonymous guest who entered his name
					userString.append(login);
				}
				else if (userMgr.getUserType(login).equals("virtual"))
				{
					userString.append(getResource("label.guestuser","Guest"));
				}
				else
				{
					String firstName = userMgr.getFirstName(login);
					String lastName = userMgr.getLastName(login);

					if ((lastName!=null) && (lastName.trim().length()>0))
					{
						if (firstName!=null)
						{
							userString.append(firstName);
							userString.append(" ");
						}

						userString.append(lastName);
					}
					else
					{
						userString.append(login);
					}
				}

				XmlUtil.setChildText(commentElement, "user", userString.toString());

				XmlUtil.setChildText(commentElement, "date", dateFormat.format(comment.getCreationDate()));

				XmlUtil.setChildText(commentElement, "msg", comment.getMessage(), true);
			}
        }
        
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
		}
        
        if (req.getParameter("rating") != null) 
        {
            XmlUtil.setChildText(imageDataElement, "voteAccepted", "true", false);
            addMsgResource("rating.confirm", getResource("vote.confirm", "The vote has been accepted."));
        }
        
		this.processResponse("albumImage.xsl", true);
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