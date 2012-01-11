package de.webfilesys.gui.xsl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.GeoTag;
import de.webfilesys.MetaInfManager;
import de.webfilesys.WebFileSys;
import de.webfilesys.decoration.Decoration;
import de.webfilesys.decoration.DecorationManager;
import de.webfilesys.graphics.CameraExifData;
import de.webfilesys.graphics.ScaledImage;
import de.webfilesys.graphics.ThumbnailThread;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.UTF8URLEncoder;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslEditMetaInfHandler extends XslRequestHandlerBase
{
	public XslEditMetaInfHandler(
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
		if (!checkWriteAccess())
		{
			return;
		}
		
		String path = getParameter("path");
		
		String relPath = req.getParameter("relPath");
		
		if (relPath != null)
		{
		    path = this.getAbsolutePath(relPath);
		}
		else
		{
	        if (path == null)
	        {
	            String fileName = getParameter("fileName");
	            
	            if (fileName != null) 
	            {
	                String cwd = (String) session.getAttribute("cwd");
	                
	                if (cwd != null) 
	                {
	                    if (cwd.endsWith(File.separator)) 
	                    {
	                        path = cwd + fileName;
	                    }
	                    else
	                    {
	                        path = cwd + File.separator + fileName;
	                    }
	                }
	                else
	                {
	                    Logger.getLogger(getClass()).warn("missing cwd");
	                }
	            }
	        }
		}
		
		if (!accessAllowed(path))
		{
			Logger.getLogger(getClass()).warn("user " + uid + " tried to access folder outside of his document root: " + path);
			
			return;
		}
		
		String description = req.getParameter("description");
		
		if (description == null)
		{
			showEditForm(path, null);
			
			return;
		}
		
		String errorMsg = null;
		
		boolean geoDataExist = false;
		
		float latitude = 0f;
		
		String latitudeParm = req.getParameter("latitude");
		
		if ((latitudeParm != null) && (latitudeParm.trim().length() > 0))
		{
			try
			{
				latitude = Float.parseFloat(latitudeParm);
				
				if ((latitude < -90.0f) || (latitude > 90.0f))
				{
					errorMsg = getResource("error.latitudeInvalid", "latitude must be a number between -90.0 and 90.0");
				}
				else
				{
					geoDataExist = true;
				}
			}
			catch (NumberFormatException nfex)
			{
				errorMsg = getResource("error.latitudeInvalid", "latitude must be a number between -90.0 and 90.0");
			}
		}

		float longitude = 0f;
		
		String longitudeParm = req.getParameter("longitude");
		
		if ((longitudeParm != null) && (longitudeParm.trim().length() > 0))
		{
			try
			{
				longitude = Float.parseFloat(longitudeParm);

				if ((longitude < -180.0f) || (longitude > 180.0f))
				{
					errorMsg = getResource("error.longitudeInvalid", "longitude must be a number between -180.0 and 180.0");
				}
				else
				{
					geoDataExist = true;
				}
			}
			catch (NumberFormatException nfex)
			{
				errorMsg = getResource("error.longitudeInvalid", "longitude must be a number between -180.0 and 180.0");
			}
		}

		int zoomFactor = 10;
		
		String zoomFactorParm = req.getParameter("zoomFactor");
		
		if ((zoomFactorParm != null) && (zoomFactorParm.trim().length() > 0))
		{
			try
			{
				zoomFactor = Integer.parseInt(zoomFactorParm);
			}
			catch (NumberFormatException nfex)
			{
			}
		}
		
		if (errorMsg != null)
		{
			showEditForm(path, errorMsg);
			
			return;
		}
		
		MetaInfManager metaInfMgr = MetaInfManager.getInstance();

		metaInfMgr.setDescription(path, description);
		
		if (geoDataExist)
		{
			GeoTag geoTag = new GeoTag(latitude, longitude, zoomFactor);
			
			String infoText = req.getParameter("infoText");
			
			if (infoText != null)
			{
				geoTag.setInfotext(infoText);
			}

			metaInfMgr.setGeoTag(path, geoTag);
		}
		else
		{
			if ((latitudeParm != null) || (longitudeParm != null))
			{
				// fields exist but are left empty
				metaInfMgr.removeGeoTag(path);
			}
		}
		
		if (path.endsWith(".")) {
			String normalizedPath = path.substring(0, path.length() - 2);

			String defaultColor = req.getParameter("defaultColor");
			if (defaultColor != null) 
			{
				Decoration deco = DecorationManager.getInstance().getDecoration(normalizedPath);
				if (deco != null) 
				{
					deco.setTextColor(null);
					DecorationManager.getInstance().setDecoration(normalizedPath, deco);
				}
			}
			else
			{
				String textColor = req.getParameter("textColor");
				if ((textColor != null) && (textColor.trim().length() > 0)) 
				{
					Decoration deco = DecorationManager.getInstance().getDecoration(normalizedPath);
					if (deco == null) 
					{
						deco = new Decoration();
					}
					deco.setTextColor("#" + textColor);
					DecorationManager.getInstance().setDecoration(normalizedPath, deco);
				}
			}
			
			String icon = req.getParameter("icon");
			if (icon != null) {
				Decoration deco = DecorationManager.getInstance().getDecoration(normalizedPath);
				if (icon.equals("none")) 
				{
					if (deco != null) 
					{
						deco.setIcon(null);
						DecorationManager.getInstance().setDecoration(normalizedPath, deco);
					}
				} 
				else
				{
					if (icon.trim().length() > 0)
					{
						if (deco == null) 
						{
							deco = new Decoration();
						}
						deco.setIcon(icon);
						DecorationManager.getInstance().setDecoration(normalizedPath, deco);
					}
				}
			}
		}
		
		output.println("<html>");
		output.println("<head>");
		output.println("<script language=\"javascript\">");
		
        if (isMobile()) 
		{
		    output.println("window.location.href='/webfilesys/servlet?command=mobile&cmd=folderFileList&keepListStatus=true';");
		}
		else
		{
            output.println("if (window.opener) {window.opener.parent.frames[2].location.href='/webfilesys/servlet?command=listFiles&keepListStatus=true'};");
		}
		
		output.println("setTimeout('self.close()',1000);");
		output.println("</script>");
		output.println("</head>");
		output.println("</html>");
		output.flush();
	}
	
	private void showEditForm(String path, String errorMsg)
	{
		File folderOrFile = new File(path);
		
		if (!folderOrFile.exists())
		{
			Logger.getLogger(getClass()).error("file not found: " + path);
			
			return;
		}
		
        String headLinePath = this.getHeadlinePath(path);

		String shortPath = CommonUtils.shortName(headLinePath, 32);
		
		Element metaInfElement = doc.createElement("metaInf");
			
		doc.appendChild(metaInfElement);

		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/editMetaInf.xsl\"");

		doc.insertBefore(xslRef, metaInfElement);

		XmlUtil.setChildText(metaInfElement, "css", userMgr.getCSS(uid), false);
		
        XmlUtil.setChildText(metaInfElement, "path", path, false);
		
		XmlUtil.setChildText(metaInfElement, "shortPath", shortPath, false);
		
		if (isMobile())
		{
	        XmlUtil.setChildText(metaInfElement, "mobile", "true", false);
		}

		if (errorMsg != null)
		{
			XmlUtil.setChildText(metaInfElement, "error", errorMsg, true);
		}
		
		addMsgResource("label.editMetaInfo", getResource("label.editMetaInfo", "Edit Meta Information"));
		addMsgResource("label.description", getResource("label.description", "Description"));
		addMsgResource("alert.descriptionTooLong", getResource("alert.descriptionTooLong", "The description text is too long (max 1024 characters allowed)!"));

		String googleMapsAPIKey = null;
		
		if ((req.getParameter("geoTag") != null) ||
		    (req.getParameter("zoomFactor") != null)) // returned to input form because of validation error
		{
			if (req.getScheme().equalsIgnoreCase("https"))
			{
				googleMapsAPIKey = WebFileSys.getInstance().getGoogleMapsAPIKeyHTTPS();
			}
			else
			{
				googleMapsAPIKey = WebFileSys.getInstance().getGoogleMapsAPIKeyHTTP();
			}
		}
		
        addMsgResource("label.geoTag", getResource("label.geoTag", "Geo tag data"));
        addMsgResource("label.latitude", getResource("label.latitude", "latitude (example: 13.75 for Dresden)"));
        addMsgResource("label.longitude", getResource("label.longitude", "longitude (example: 51.05 for Dresden)"));

        if (googleMapsAPIKey != null)
		{
			addMsgResource("label.zoomFactor", getResource("label.zoomFactor", "zoom factor (1 = far away view, 16 = closest view)"));
			addMsgResource("label.geoTagInfoText", getResource("label.geoTagInfoText", "Text for Info Window on the map"));
			addMsgResource("label.hintGoogleMapSelect", getResource("label.hintGoogleMapSelect", "Double click to select geographic coordinates!"));

			addMsgResource("alert.missingLatitude", getResource("alert.missingLatitude", "latitude is a required field"));
			addMsgResource("alert.missingLongitude", getResource("alert.missingLongitude", "longitude is a required field"));
			addMsgResource("button.test", getResource("button.test","Test"));
			addMsgResource("button.selectFromMap", getResource("button.selectFromMap","Select on map"));
		}
		
        if (path.endsWith(".")) {
    		addMsgResource("label.textColor", getResource("label.textColor","text color"));
    		addMsgResource("label.folderIcon", getResource("label.folderIcon","folder icon"));
    		addMsgResource("noCustomIcon", getResource("noCustomIcon","no custom icon"));
    		addMsgResource("noCustomColor", getResource("noCustomColor","no custom color"));
        }

        addMsgResource("button.save", getResource("button.save","Save"));
		addMsgResource("button.cancel", getResource("button.cancel","Cancel"));
		
		if (folderOrFile.isFile())
		{
			Element thumbnailElement = getThumbnailData(path);
			
			if (thumbnailElement != null)
			{
	            metaInfElement.appendChild(thumbnailElement);
			}
		}
		MetaInfManager metaInfMgr = MetaInfManager.getInstance();

		String description = null;
		
		if (errorMsg != null) 
		{
		    description = req.getParameter("description");
		}
		else
		{
		    description = metaInfMgr.getDescription(path);
		}
		
		if (description != null)
		{
			XmlUtil.setChildText(metaInfElement, "description", description, true);
		}
		
        Element geoTagElement = doc.createElement("geoTag");

        if (errorMsg != null) 
        {
            String latitudeParm = req.getParameter("latitude"); 
            XmlUtil.setChildText(geoTagElement, "latitude", latitudeParm, false);

            String longitudeParm = req.getParameter("longitude"); 
            XmlUtil.setChildText(geoTagElement, "longitude", longitudeParm, false);
            
            String infoTextParm = req.getParameter("infoText");
            XmlUtil.setChildText(geoTagElement, "infoText", infoTextParm, false);
        }
        else
        {
            GeoTag geoTag = metaInfMgr.getGeoTag(path);

            if (geoTag != null)
            {
                XmlUtil.setChildText(geoTagElement, "latitude", Float.toString(geoTag.getLatitude()), false);
                XmlUtil.setChildText(geoTagElement, "longitude", Float.toString(geoTag.getLongitude()), false);
                
                String infoText = geoTag.getInfoText();
                
                if (infoText != null)
                {
                    XmlUtil.setChildText(geoTagElement, "infoText", infoText, false);
                }
            }
            else 
            {
                String fileExt = CommonUtils.getFileExtension(path);
                
                if (fileExt.equals(".jpg") || fileExt.equals(".jpeg"))
                {
                    // use GPS coordinates from Exif data if present in the JPEG file
                    CameraExifData exifData = new CameraExifData(path);

                    if (exifData.hasExifData())
                    {
                        float gpsLatitude = exifData.getGpsLatitude();
                        float gpsLongitude = exifData.getGpsLongitude();
                        
                        if ((gpsLatitude >= 0.0f) && (gpsLongitude >= 0.0f))
                        {
                            String latitudeRef = exifData.getGpsLatitudeRef();
                            
                            if ((latitudeRef != null) && latitudeRef.equalsIgnoreCase("S")) 
                            {
                                gpsLatitude = (-gpsLatitude);
                            }
                            
                            String longitudeRef = exifData.getGpsLongitudeRef();

                            if ((longitudeRef != null) && longitudeRef.equalsIgnoreCase("W")) 
                            {
                                gpsLongitude = (-gpsLongitude);
                            } 
                            
                            XmlUtil.setChildText(geoTagElement, "latitude", Float.toString(gpsLatitude), false);
                            XmlUtil.setChildText(geoTagElement, "longitude", Float.toString(gpsLongitude), false);
                        }
                    }
                }
            }
        }
			
	    if (googleMapsAPIKey != null)
	    {
            XmlUtil.setChildText(geoTagElement, "googleMapsAPIKey", googleMapsAPIKey, false);

            int zoomFactor = 10;

            if (errorMsg != null)
	        {
	            try
	            {
	                zoomFactor = Integer.parseInt(req.getParameter("zoomFactor"));
	            }
	            catch (Exception ex)
	            {
	            }
	        }
            else 
            {
                GeoTag geoTag = metaInfMgr.getGeoTag(path);

                if (geoTag != null)
                {
                    zoomFactor = geoTag.getZoomFactor();
                }
            }
	        
            Element zoomLevelElement = doc.createElement("zoomLevel");
			
			geoTagElement.appendChild(zoomLevelElement);
			
			for (int i = 0; i < 16; i++)
			{
				Element zoomFactorElement = doc.createElement("zoomFactor");
				
				XmlUtil.setElementText(zoomFactorElement, Integer.toString(i));
				
				if (i == zoomFactor)
				{
					zoomFactorElement.setAttribute("current", "true");
				}
				
				zoomLevelElement.appendChild(zoomFactorElement);
			}
		}

	    metaInfElement.appendChild(geoTagElement);
		
		if (path.endsWith(".")) 
		{
			String normalizedPath = path.substring(0, path.length() - 2);
			Decoration deco = DecorationManager.getInstance().getDecoration(normalizedPath);
			
			if (deco != null) 
			{
				String textColor = deco.getTextColor();
				if (textColor != null) 
				{
					if (textColor.startsWith("#")) 
					{
						textColor = textColor.substring(1);
					}

					XmlUtil.setChildText(metaInfElement, "textColor", textColor);
				}
				
				String icon = deco.getIcon();
				if (icon != null) {
					XmlUtil.setChildText(metaInfElement, "icon", icon);
				}
			}
			
	        Element availableIconsElement = doc.createElement("availableIcons");

	        metaInfElement.appendChild(availableIconsElement);
			
			Iterator iconIter = DecorationManager.getInstance().getAvailableIcons().iterator();
			
			while (iconIter.hasNext()) 
			{
				String icon = (String) iconIter.next();
				Element iconElement = doc.createElement("icon");
				availableIconsElement.appendChild(iconElement);
				XmlUtil.setElementText(iconElement, icon);
			}
		}
	    
		// when XSLT processing is done by the browser, the Firefox browser and MSIE 7.0 hang up forever
		// when loading the Google maps API Javascript functions from the Google server
		// so we have to do the XSLT processing always on server side
		
		this.processResponse("editMetaInf.xsl");
    }
	
	/**
	 * We have to do the XSLT processing always on server side. See explanation above.
	 */
	public void processResponse(String xslFile)
    {
		String xslPath = WebFileSys.getInstance().getWebAppRootDir() + "xsl" + File.separator + xslFile;
    	
		TransformerFactory tf = TransformerFactory.newInstance();
	
		try
		{
			Transformer t =
					 tf.newTransformer(new StreamSource(new File(xslPath)));

			long start = System.currentTimeMillis();

			t.transform(new DOMSource(doc),
						new StreamResult(output));
	 		    
			long end = System.currentTimeMillis();
    
			Logger.getLogger(getClass()).debug("XSLTC transformation in " + (end - start) + " ms");
		}
		catch (TransformerConfigurationException tex)
		{
			Logger.getLogger(getClass()).warn(tex);
		}
		catch (TransformerException tex)
		{
			Logger.getLogger(getClass()).warn(tex);
		}

		output.flush();
    }
	
	private Element getThumbnailData(String filePath)
	{
		String lowerCaseFilePath = filePath.toLowerCase();
		
	    if (!(lowerCaseFilePath.endsWith(".jpg") || lowerCaseFilePath.endsWith(".jpeg") ||
	    	  lowerCaseFilePath.endsWith(".gif") || lowerCaseFilePath.endsWith(".png") ||
	    	  lowerCaseFilePath.endsWith(".bmp")))
	    {
	    	// no picture file
	    	return(null);
	    }
	    
		ScaledImage scaledImage = null;

		try
		{
			scaledImage = new ScaledImage(filePath, 100, 100);
		}
		catch (IOException ioEx)
		{
			Logger.getLogger(getClass()).error(ioEx);
			return(null);                
		}
		
		Element thumbnailElement = doc.createElement("thumbnail");
		
		int thumbnailSize = 100;
		
		int thumbWidth = 0;
		int thumbHeight = 0;

		if (scaledImage.getRealHeight() > scaledImage.getRealWidth())
		{
			thumbHeight = thumbnailSize;
			thumbWidth = scaledImage.getRealWidth() * thumbnailSize / scaledImage.getRealHeight();
		}
		else
		{
			thumbWidth = thumbnailSize;
			thumbHeight = scaledImage.getRealHeight() * thumbnailSize / scaledImage.getRealWidth();
		}
		
		String srcFileName = filePath;
		
		boolean useThumb = false;
		boolean useExif = false;

		CameraExifData exifData = null;

		String thumbFileName = ThumbnailThread.getThumbnailPath(filePath);

		File thumbnailFile = new File(thumbFileName);
		
		if (thumbnailFile.exists())
		{
			srcFileName = "/webfilesys/servlet?command=getThumb&imgFile=" + UTF8URLEncoder.encode(filePath);

			useThumb = true;
		}
		else
		{
			int sizeBorder = 500;

			if ((scaledImage.getImageType() == ScaledImage.IMG_TYPE_JPEG) && 
				((scaledImage.getRealWidth() > sizeBorder) ||
				 (scaledImage.getRealHeight() > sizeBorder)))
			{
				exifData = new CameraExifData(filePath);

				if (exifData.getThumbnailLength() > 0)
				{
					if (scaledImage.getRealWidth() >= scaledImage.getRealHeight())
					{
						useExif = true;
					}
					else
					{
						if (exifData.getThumbOrientation() == CameraExifData.ORIENTATION_PORTRAIT)
						{
							useExif = true;
						}
					}

					if (useExif)
					{
						int exifThumbWidth = exifData.getThumbWidth();
						int exifThumbHeight = exifData.getThumbHeight();
						
						if (exifThumbHeight > exifThumbWidth)
						{
							thumbHeight = thumbnailSize;
							thumbWidth = exifThumbWidth * thumbnailSize / exifThumbHeight;
						}
						else
						{
							thumbWidth = thumbnailSize;
							thumbHeight = exifThumbHeight * thumbnailSize / exifThumbWidth;
						}
						
						srcFileName = "/webfilesys/servlet?command=exifThumb&imgFile=" + UTF8URLEncoder.encode(filePath);
						
						useThumb = true;
					}
				}
			}
		}

		XmlUtil.setChildText(thumbnailElement, "thumbnailWidth", Integer.toString(thumbWidth));
		XmlUtil.setChildText(thumbnailElement, "thumbnailHeight", Integer.toString(thumbHeight));

		if (!useThumb)
		{
			srcFileName = "/webfilesys/servlet?command=getFile&filePath=" + UTF8URLEncoder.encode(srcFileName);
		}
		
		XmlUtil.setChildText(thumbnailElement, "imgPath", srcFileName);
		
        return(thumbnailElement);	    
	}
}