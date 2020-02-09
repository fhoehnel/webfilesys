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
public class XslGoogleMapMultiHandler extends XslRequestHandlerBase {
	private static final Logger LOG = Logger.getLogger(XslGoogleMapMultiHandler.class);
	
	private static final int INFO_TEXT_FROM_DESCR_MAX_LENGTH = 80;
	
	public XslGoogleMapMultiHandler(
			HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid) {
        super(req, resp, session, output, uid);
	}
	  
	protected void process() {
		String path = getParameter("path");
		
		if ((path == null) || path.isEmpty()) {
			path = getCwd();
		}

		if (!accessAllowed(path)) {
			LOG.warn("user " + uid + " tried to access folder outside of his document root: " + path);
			return;
		}
		
		File folderFile = new File(path);
		
		if ((!folderFile.exists()) || (!folderFile.isDirectory())) {
			LOG.error("folder not found or not readable: " + path);
			return;
		}
		
		Element geoDataElement = doc.createElement("geoData");
			
		doc.appendChild(geoDataElement);

		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/googleMapMulti.xsl\"");

		doc.insertBefore(xslRef, geoDataElement);

		Element mapDataElement = doc.createElement("mapData");

		geoDataElement.appendChild(mapDataElement);
		
		XmlUtil.setChildText(mapDataElement, "zoomLevel", Integer.toString(3));
		
		MetaInfManager metaInfMgr = MetaInfManager.getInstance();

		Element markersElement = doc.createElement("markers");

		geoDataElement.appendChild(markersElement);
		
		String metaInfPath = path + File.separatorChar + ".";
		
		GeoTag geoTag = metaInfMgr.getGeoTag(metaInfPath);

		if (geoTag != null) {
	        addMarker(markersElement, geoTag.getLatitude(), geoTag.getLongitude(), geoTag.getInfoText(), null);	        
		}

		File[] fileList = folderFile.listFiles();
		
		for (File file : fileList) {
			geoTag = metaInfMgr.getGeoTag(file.getAbsolutePath());
			
			if (geoTag != null) {
                String infoText = geoTag.getInfoText();
                
                /*
                if (CommonUtils.isEmpty(infoText)) {
                    infoText = metaInfMgr.getDescription(file.getAbsolutePath());
                    if (!CommonUtils.isEmpty(infoText)) {
                    	infoText = removeEmojis(infoText);
                    	if (infoText.length() > INFO_TEXT_FROM_DESCR_MAX_LENGTH) {
                    		infoText = infoText.substring(0, INFO_TEXT_FROM_DESCR_MAX_LENGTH - 4) + " ...";
                    	}
                    }
                }
                */
				
		        addMarker(markersElement, geoTag.getLatitude(), geoTag.getLongitude(), infoText, file.getName());	        
			} else {
	            String fileExt = CommonUtils.getFileExtension(file.getName());
	            
	            if (fileExt.equals(".jpg") || fileExt.equals(".jpeg")) {
	                // use GPS coordinates from Exif data if present in the JPEG file
                    
                    CameraExifData exifData = new CameraExifData(file.getAbsolutePath());

	                if (exifData.hasExifData()) {

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
	                        
	                        /*
                            String infoText = metaInfMgr.getDescription(file.getAbsolutePath());
                            if (!CommonUtils.isEmpty(infoText)) {
                            	infoText = removeEmojis(infoText);
                            	if (infoText.length() > INFO_TEXT_FROM_DESCR_MAX_LENGTH) {
                            		infoText = infoText.substring(0, INFO_TEXT_FROM_DESCR_MAX_LENGTH - 4) + " ...";
                            	}
                            }
	                        
	        		        addMarker(markersElement, gpsLatitude, gpsLongitude, infoText);	    
	        		        */
	                        
	        		        addMarker(markersElement, gpsLatitude, gpsLongitude, null, file.getName());	        
	                    }
	                }
	            }
			}
		}
		
        String googleMapsAPIKey = null;
		if (req.getScheme().equalsIgnoreCase("https")) {
			googleMapsAPIKey = WebFileSys.getInstance().getGoogleMapsAPIKeyHTTPS();
		} else {
			googleMapsAPIKey = WebFileSys.getInstance().getGoogleMapsAPIKeyHTTP();
		}
		
		if (!CommonUtils.isEmpty(googleMapsAPIKey)) {
		    XmlUtil.setChildText(geoDataElement, "googleMapsAPIKey", googleMapsAPIKey, false);
		}
		
		// when XSLT processing is done by the browser, the Firefox browser and MSIE 7.0 hang up forever
		// when loading the Google maps API Javascript functions from the Google server
		// so we have to do the XSLT processing always on server side
		
		processResponse("googleMapMulti.xsl", true);
    }
	
	private void addMarker(Element markersElement, float latitude, float longitude, String infoText, String fileName) {
		Element markerElement = doc.createElement("marker");

		markersElement.appendChild(markerElement);
		
		XmlUtil.setChildText(markerElement, "latitude", Float.toString(latitude), false);
	    XmlUtil.setChildText(markerElement, "longitude", Float.toString(longitude), false);

	    if ((infoText != null) && (!infoText.isEmpty())) {
            XmlUtil.setChildText(markerElement, "infoText", infoText.replace('\'', '´'), false);
	    }

	    if (fileName != null) {
		    XmlUtil.setChildText(markerElement, "fileName", fileName, false);
	    }
	}
	
	private String removeEmojis(String infoText) {
		if ((infoText.indexOf('{') < 0) || (infoText.indexOf('}') < 0)) {
			return infoText;
		}
		
		StringBuffer buff = new StringBuffer(infoText.length());
		
		boolean ignore = false;
		
		for (int i = 0; i < infoText.length(); i++) {
		     char c = infoText.charAt(i);
		     
		     if (c == '{') {
		    	 ignore = true;
		     } else if (c == '}') {
	    		 ignore = false;
		     } else {
		    	 if (!ignore) {
		    		 buff.append(c);
		    	 }
		     }
		}
		
		return buff.toString();
	}
	
}