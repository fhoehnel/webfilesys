package de.webfilesys.gui.google;

import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.webfilesys.GeoTag;
import de.webfilesys.MetaInfManager;
import de.webfilesys.WebFileSys;
import de.webfilesys.graphics.CameraExifData;
import de.webfilesys.gui.user.UserRequestHandler;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public abstract class GoogleEarthHandlerBase extends UserRequestHandler
{
    protected static final String GOOGLE_KML_CONTENT_TYPE = "application/vnd.google-earth.kml+xml";
    
	protected Document doc;

	private DocumentBuilder builder;

	private HttpServletResponse resp = null;
	
	public GoogleEarthHandlerBase(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid)
	{
        super(req, resp, session, output, uid);
        
        this.req = req;
        this.resp = resp;

		builder = null;

		try
		{
			builder = WebFileSys.getInstance().getDocFactory().newDocumentBuilder();

			doc = builder.newDocument();
		}
		catch (ParserConfigurationException pcex)
		{
			Logger.getLogger(getClass()).error(pcex.toString());
			System.out.println(pcex.toString());
		}
	}

	protected void process()
	{
		resp.setContentType(GOOGLE_KML_CONTENT_TYPE);

		resp.setHeader("Content-Disposition", "attachment; filename=WebFileSysPlacemarks.kml");

		BufferedWriter xmlOutFile = new BufferedWriter(output);
                
        Element kmlElement = doc.createElementNS("http://www.opengis.net/kml/2.2", "kml");
        doc.appendChild(kmlElement);
		
		Element googleDocElement = doc.createElement("Document");
        kmlElement.appendChild(googleDocElement);
        
        XmlUtil.setChildText(googleDocElement, "name", "WebFileSys Placemarks");
		
		List placemarkElementList = createPlacemarkXml();
		
		for (int i = 0; i < placemarkElementList.size(); i++)
		{
		    Element placemarkElement = (Element) placemarkElementList.get(i);
		    googleDocElement.appendChild(placemarkElement);
		}
		
        XmlUtil.writeToStream(doc, xmlOutFile);

		output.flush();
	}

	/**
     * @return List of Placemark Element objects
	 */
    protected abstract ArrayList createPlacemarkXml(); 
	
	protected Element createPlacemark(String imgPath) 
	{
        MetaInfManager metaInfMgr = MetaInfManager.getInstance();
	    
        String description = metaInfMgr.getDescription(imgPath);

        StringBuffer coordinatesBuff = new StringBuffer();
        
        GeoTag geoTag = metaInfMgr.getGeoTag(imgPath);

        if (geoTag != null)
        {
            coordinatesBuff.append(Float.toString(geoTag.getLongitude()));
            coordinatesBuff.append(',');
            coordinatesBuff.append(Float.toString(geoTag.getLatitude()));
            coordinatesBuff.append(',');
            coordinatesBuff.append('0');
            
            String infoText = geoTag.getInfoText();
            
            if ((infoText != null) && (infoText.trim().length() > 0))
            {
                description = infoText;
            }
        }
        else 
        {
            String fileExt = CommonUtils.getFileExtension(imgPath);
            
            if (fileExt.equals(".jpg") || fileExt.equals(".jpeg"))
            {
                // use GPS coordinates from Exif data if present in the JPEG file
                CameraExifData exifData = new CameraExifData(imgPath);

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
                        
                        coordinatesBuff.append(Float.toString(gpsLongitude));
                        coordinatesBuff.append(',');
                        coordinatesBuff.append(Float.toString(gpsLatitude));
                        coordinatesBuff.append(',');
                        coordinatesBuff.append('0');
                    }
                }
            }
        }

        if (coordinatesBuff.length() == 0)
        {
            return null;
        }
        
        Element placemarkElement = doc.createElement("Placemark");
        
        if ((description != null) && (description.length() > 0))
        {
            XmlUtil.setChildText(placemarkElement, "name", CommonUtils.shortName(description, 32));
            
            XmlUtil.setChildText(placemarkElement, "description", description, true);
        }
        else
        {
            XmlUtil.setChildText(placemarkElement, "name", CommonUtils.extractFileName(imgPath));
        }
        
        Element pointElement = doc.createElement("Point");
        
        placemarkElement.appendChild(pointElement);

        XmlUtil.setChildText(pointElement, "coordinates", coordinatesBuff.toString());
        
        return placemarkElement;
	}
}
