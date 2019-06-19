package de.webfilesys.gui.xsl;

import java.io.File;
import java.io.PrintWriter;

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
import de.webfilesys.graphics.CameraExifData;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslGoogleMapHandler extends XslRequestHandlerBase
{
	public XslGoogleMapHandler(
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
			Logger.getLogger(getClass()).warn("user " + uid + " tried to access folder outside of his document root: " + path);
			
			return;
		}
		
		File folderOrFile = new File(path);
		
		if (!folderOrFile.exists())
		{
			Logger.getLogger(getClass()).error("file not found: " + path);
			
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
		
		String headLinePath = this.getHeadlinePath(path);

        String shortPath = CommonUtils.shortName(headLinePath, 50);
		
		Element geoTagElement = doc.createElement("geoTag");
			
		doc.appendChild(geoTagElement);

		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/googleMap.xsl\"");

		doc.insertBefore(xslRef, geoTagElement);

		XmlUtil.setChildText(geoTagElement, "path", path, false);
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
	        
		    if ((infoText != null) && (!infoText.isEmpty())) {
	            XmlUtil.setChildText(geoTagElement, "infoText", infoText.replace('\'', '´'), false);
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
            Logger.getLogger(getClass()).error("No Geo Tag / GPS Exif data exists for file/folder " + path);
            
            return;
        }
		
        String googleMapsAPIKey = null;
		if (req.getScheme().equalsIgnoreCase("https")) {
			googleMapsAPIKey = WebFileSys.getInstance().getGoogleMapsAPIKeyHTTPS();
		} else {
			googleMapsAPIKey = WebFileSys.getInstance().getGoogleMapsAPIKeyHTTP();
		}
		
		if (!CommonUtils.isEmpty(googleMapsAPIKey)) {
		    XmlUtil.setChildText(geoTagElement, "googleMapsAPIKey", googleMapsAPIKey, false);
		}
        
		// addMsgResource("label.hintGoogleMapSelect", getResource("label.hintGoogleMapSelect","Double click to select geographic coordinates!"));
		
		// when XSLT processing is done by the browser, the Firefox browser and MSIE 7.0 hang up forever
		// when loading the Google maps API Javascript functions from the Google server
		// so we have to do the XSLT processing always on server side
		
		processResponse("googleMap.xsl", true);
    }
	
}