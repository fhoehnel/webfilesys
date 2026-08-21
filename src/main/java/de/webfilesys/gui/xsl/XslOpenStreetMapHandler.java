package de.webfilesys.gui.xsl;

import java.io.File;
import java.io.PrintWriter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.GeoTag;
import de.webfilesys.MetaInfManager;
import de.webfilesys.graphics.CameraExifData;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * Show geographic location in open street map.
 * @author Frank Hoehnel
 */
public class XslOpenStreetMapHandler extends XslRequestHandlerBase
{
	public XslOpenStreetMapHandler(
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
		String path = getParameter("path");

		if (!accessAllowed(path))
		{
			LogManager.getLogger(getClass()).warn("user " + uid + " tried to access folder outside of his document root: " + path);
			
			return;
		}
		
		File folderOrFile = new File(path);
		
		if (!folderOrFile.exists())
		{
			LogManager.getLogger(getClass()).error("file not found: " + path);
			
			return;
		}
		
		String metaInfPath = path;
		
		if (folderOrFile.isDirectory())
		{
			if (path.endsWith(File.separator))
			{
				metaInfPath = path + ".";
			}
			else
			{
				metaInfPath = path + File.separatorChar + ".";
			}
		}
		
        String shortPath = CommonUtils.shortName(getHeadlinePath(path), 50);
		
		Element geoTagElement = doc.createElement("geoTag");
			
		doc.appendChild(geoTagElement);

		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/openStreetMap.xsl\"");

		doc.insertBefore(xslRef, geoTagElement);

		XmlUtil.setChildText(geoTagElement, "path", path, false);
		XmlUtil.setChildText(geoTagElement, "pathForScript", insertDoubleBackslash(path), false);
		XmlUtil.setChildText(geoTagElement, "shortPath", shortPath, false);
			
		MetaInfManager metaInfMgr = MetaInfManager.getInstance();
		
		boolean geoLocationDefined = false;
		
		GeoTag geoTag = metaInfMgr.getGeoTag(metaInfPath);

		if (geoTag != null)
		{
	        XmlUtil.setChildText(geoTagElement, "latitude", Float.toString(geoTag.getLatitude()), false);
	        XmlUtil.setChildText(geoTagElement, "longitude", Float.toString(geoTag.getLongitude()), false);

	        XmlUtil.setChildText(geoTagElement, "zoomFactor", Integer.toString(geoTag.getZoomFactor()), false);
	        
	        String infoText = geoTag.getInfoText();
	        
	        if (infoText != null)
	        {
	            XmlUtil.setChildText(geoTagElement, "infoText", infoText, false);
	        }
	        
	        geoLocationDefined = true;
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
                        
                        XmlUtil.setChildText(geoTagElement, "zoomFactor", "10", false);
                        
                        geoLocationDefined = true;
                    }
                }
            }
		}
		
        if (!geoLocationDefined)
        {
            LogManager.getLogger(getClass()).error("No Geo Tag / GPS Exif data exists for file/folder " + path);
            
            return;
        }
		
		processResponse("openStreetMap.xsl");
    }
	
}