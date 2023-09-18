package de.webfilesys.gui.xsl;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Element;

import de.webfilesys.GeoTag;
import de.webfilesys.MetaInfManager;
import de.webfilesys.WebFileSys;
import de.webfilesys.decoration.Decoration;
import de.webfilesys.decoration.DecorationManager;
import de.webfilesys.graphics.CameraExifData;
import de.webfilesys.graphics.ImageDimensions;
import de.webfilesys.graphics.ImageUtils;
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
	                String cwd = getCwd();
	                
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
	                    LogManager.getLogger(getClass()).warn("missing cwd");
	                }
	            }
	        }
		}
		
		if (!accessAllowed(path))
		{
			LogManager.getLogger(getClass()).warn("user " + uid + " tried to access folder outside of his document root: " + path);
			
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
		
		String tags = req.getParameter("tags");
		
	    if (tags != null) {
			String[] newTags = tags.trim().split(",");
			metaInfMgr.setTags(path, newTags);
	    }
		
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
		
		boolean colorChanged = false;
		boolean iconChanged = false;
		
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
					colorChanged = true;
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
					colorChanged = true;
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
					    iconChanged = true;
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
					    iconChanged = true;
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
			if (path.endsWith(".") && (colorChanged || iconChanged)) {
				String encodedPath  = UTF8URLEncoder.encode(path.substring(0, path.length() - 2));
	            output.println("if (window.opener) {window.opener.parent.frames[1].location.href='/webfilesys/servlet?command=exp&expandPath=" + encodedPath + "&fastPath=true'};");
		    } else {
		    	String[] partsOfPath = CommonUtils.splitPath(path);
	            output.println("if (window.opener) {window.opener.parent.frames[2].location.href='/webfilesys/servlet?command=listFiles&keepListStatus=true&scrollTo=" + UTF8URLEncoder.encode(partsOfPath[1]) + "'};");
		    }
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
			LogManager.getLogger(getClass()).error("file not found: " + path);
			
			return;
		}
		
        String headLinePath = this.getHeadlinePath(path);

		String shortPath = CommonUtils.shortName(headLinePath, 50);
		
		if (folderOrFile.isDirectory()) {
			if (shortPath.length() > 2) {
				shortPath = shortPath.substring(0,  shortPath.length() - 2);
			}
		}
		
		Element metaInfElement = doc.createElement("metaInf");
			
		doc.appendChild(metaInfElement);

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
		
        if (path.endsWith(".")) {
			XmlUtil.setChildText(metaInfElement, "folder", "true", true);
        }		
		
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
		
		if (folderOrFile.isFile()) {
			String tags = "";
			if (errorMsg != null) {
				tags = req.getParameter("tags");
			} else {
				ArrayList<String> tagList = metaInfMgr.getTags(path);
				if (tagList != null) {
					StringBuilder buff = new StringBuilder();
					boolean firstTag = true;
				    for (String tag : tagList) {
						if (firstTag) {
							firstTag = false;
						} else {
							buff.append(", ");
						}
						buff.append(tag);
					}
				    tags = buff.toString();
				}
			}
			Element tagsElem = doc.createElement("tags");
			XmlUtil.setElementText(tagsElem, tags, true);
		    metaInfElement.appendChild(tagsElem);
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
			
        boolean mapSelection = false;
		
		if ((req.getParameter("geoTag") != null) ||
		    (req.getParameter("zoomFactor") != null)) // returned to input form because of validation error
		{
	        mapSelection = true;
		}
		
	    if (mapSelection)
	    {
            XmlUtil.setChildText(geoTagElement, "mapSelection", "true", false);

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
			
	        for (String icon : DecorationManager.getInstance().getAvailableIcons()) {
				Element iconElement = doc.createElement("icon");
				availableIconsElement.appendChild(iconElement);
				XmlUtil.setElementText(iconElement, icon);
			}
		}
	    
        String googleMapsAPIKey = null;
		if (req.getScheme().equalsIgnoreCase("https")) {
			googleMapsAPIKey = WebFileSys.getInstance().getGoogleMapsAPIKeyHTTPS();
		} else {
			googleMapsAPIKey = WebFileSys.getInstance().getGoogleMapsAPIKeyHTTP();
		}
		
		if (!CommonUtils.isEmpty(googleMapsAPIKey)) {
		    XmlUtil.setChildText(metaInfElement, "googleMapsAPIKey", googleMapsAPIKey, false);
		}
		
		// when XSLT processing is done by the browser, the Firefox browser and MSIE 7.0 hang up forever
		// when loading the Google maps API Javascript functions from the Google server
		// so we have to do the XSLT processing always on server side
		
		processResponse("editMetaInf.xsl", true);
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
	    
	    ImageDimensions scaledDim = ImageUtils.getScaledImageDimensions(filePath, 140, 120);
	    
		Element thumbnailElement = doc.createElement("thumbnail");
		
		XmlUtil.setChildText(thumbnailElement, "thumbnailWidth", Integer.toString(scaledDim.getWidth()));
		XmlUtil.setChildText(thumbnailElement, "thumbnailHeight", Integer.toString(scaledDim.getHeight()));

   	    String srcFileName = "/webfilesys/servlet?command=picThumb&imgFile=" + UTF8URLEncoder.encode(CommonUtils.extractFileName(filePath));
		XmlUtil.setChildText(thumbnailElement, "imgPath", srcFileName);
		
        return(thumbnailElement);	    
	}
}