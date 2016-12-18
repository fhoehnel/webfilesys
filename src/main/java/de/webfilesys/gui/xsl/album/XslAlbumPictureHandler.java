package de.webfilesys.gui.xsl.album;

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
import de.webfilesys.FileComparator;
import de.webfilesys.FileContainer;
import de.webfilesys.FileContainerComparator;
import de.webfilesys.FileLinkSelector;
import de.webfilesys.FileSelectionStatus;
import de.webfilesys.GeoTag;
import de.webfilesys.InvitationManager;
import de.webfilesys.LanguageManager;
import de.webfilesys.MetaInfManager;
import de.webfilesys.PictureRating;
import de.webfilesys.graphics.CameraExifData;
import de.webfilesys.graphics.ScaledImage;
import de.webfilesys.gui.xsl.XslRequestHandlerBase;
import de.webfilesys.servlet.VisitorServlet;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.UTF8URLEncoder;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslAlbumPictureHandler extends XslRequestHandlerBase
{
	public XslAlbumPictureHandler(
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
		
		boolean isLastImg = false;

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
			actPath = getCwd();

			imgName = getParameter("imgName");
			
			if (CommonUtils.isEmpty(imgName)) {
				String afterParam = getParameter("after");
				if (!CommonUtils.isEmpty(afterParam)) {
					imgName = getAdjacentPicture(actPath, afterParam);
					if (imgName.equals(afterParam)) {
						isLastImg = true;
					} 
				}
			}
		
			if (CommonUtils.isEmpty(imgName)) {
				Logger.getLogger(getClass()).error("missing imageName or after parameter");
				return;
			}
				
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
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/album/albumPicture.xsl\"");

		doc.insertBefore(xslRef, imageDataElement);

	    XmlUtil.setChildText(imageDataElement, "language", language, false);
		
		XmlUtil.setChildText(imageDataElement, "css", userMgr.getCSS(uid), false);
		XmlUtil.setChildText(imageDataElement, "imagePath", imgPath, false);
		XmlUtil.setChildText(imageDataElement, "imageName", imgName, false);
		XmlUtil.setChildText(imageDataElement, "encodedPath", UTF8URLEncoder.encode(imgPath), false);
		XmlUtil.setChildText(imageDataElement, "encodedName", UTF8URLEncoder.encode(imgName), false);
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

		if (req.getParameter("fromStory") != null) 
		{
			XmlUtil.setChildText(imageDataElement, "fromStory", "true", false);
		}
		
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
			scaledImage = new ScaledImage(imgPath, winWidth-50, winHeight-86);
		}
		catch (IOException io1)
		{
			Logger.getLogger(getClass()).error(io1.toString());
			this.processResponse("album/albumPicture.xsl", true);
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
			
			int myRating = (-1);
			String visitorId = (String) session.getAttribute(VisitorServlet.SESSION_ATTRIB_VISITOR_ID);
			if (visitorId != null) {
			    myRating = metaInfMgr.getIdentifiedVisitorRating(visitorId, imgPathOS);

			    if (myRating > 0) {
				    XmlUtil.setChildText(imageDataElement, "myRating", Integer.toString(myRating));
			    }
			}
				
			if (!alreadyRated) {
				if (myRating > 0) {
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
	        
	        // the reason for this is historic: previous google maps api version required an API key
			XmlUtil.setChildText(imageDataElement, "googleMaps", "true", false);
		}
        
        if (!isLastImg) {
            Integer sortBy = (Integer) session.getAttribute("sortField");
            if ((sortBy == null) || (sortBy.intValue() == FileContainerComparator.SORT_BY_FILENAME)) 
            {
                XmlUtil.setChildText(imageDataElement, "nextLink", "true", false);
            }
        }
        
        if (req.getParameter("rating") != null) 
        {
            XmlUtil.setChildText(imageDataElement, "voteAccepted", "true", false);
        }
        
		this.processResponse("album/albumPicture.xsl", true);
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
    
    public String getAdjacentPicture(String imgPath, String afterFileName) {
	    
        FileLinkSelector fileSelector = new FileLinkSelector(imgPath, FileComparator.SORT_BY_FILENAME);

        FileSelectionStatus selectionStatus = fileSelector.selectFiles(Constants.imgFileMasks, 4096, null, null);

        Vector imageFiles = selectionStatus.getSelectedFiles();

        String nextImg = null;
        
        if (imageFiles != null) {
            boolean found = false;
            
            for (int i = 0; (!found) && (i < imageFiles.size()); i++) {
                FileContainer fileCont = (FileContainer) imageFiles.elementAt(i);
                
                if (!fileCont.isLink()) {
                	if (fileCont.getName().compareToIgnoreCase(afterFileName) > 0) {
                        nextImg = fileCont.getName();
                        found = true;
                	}
                }
            }
        }

        if (nextImg != null) {
            return nextImg;
        }

        return afterFileName;
    }
}