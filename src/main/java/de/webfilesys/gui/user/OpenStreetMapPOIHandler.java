package de.webfilesys.gui.user;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.GeoTag;
import de.webfilesys.MetaInfManager;
import de.webfilesys.graphics.CameraExifData;
import de.webfilesys.util.CommonUtils;

/**
 * Points of interest for Open Street Maps.
 * @author Frank Hoehnel
 */
public class OpenStreetMapPOIHandler extends UserRequestHandler
{
	public OpenStreetMapPOIHandler(
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
		
		MetaInfManager metaInfMgr = MetaInfManager.getInstance();
		
		GeoTag geoTag = metaInfMgr.getGeoTag(metaInfPath);

		float latitude = Float.NEGATIVE_INFINITY;
		float longitude = Float.NEGATIVE_INFINITY;
		String infoText = "";
		String description = metaInfMgr.getDescription(metaInfPath);
		if ((description == null) || (description.trim().length() == 0)) {
			description = folderOrFile.getName();
		}
		
		if (geoTag != null)
		{
	        latitude = geoTag.getLatitude();
	        longitude = geoTag.getLongitude();
	        infoText = geoTag.getInfoText();
	        if ((infoText == null) || (infoText.trim().length() == 0)) {
	        	infoText = folderOrFile.getName();
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
                    latitude = exifData.getGpsLatitude();
                    longitude = exifData.getGpsLongitude();
                    
                    if ((latitude >= 0.0f) && (longitude >= 0.0f))
                    {
                        String latitudeRef = exifData.getGpsLatitudeRef();
                        
                        if ((latitudeRef != null) && latitudeRef.equalsIgnoreCase("S")) 
                        {
                            latitude = (-latitude);
                        }
                        
                        String longitudeRef = exifData.getGpsLongitudeRef();

                        if ((longitudeRef != null) && longitudeRef.equalsIgnoreCase("W")) 
                        {
                            longitude = (-longitude);
                        } 
                    }
                }
            }
		}
		
        if ((longitude == Float.NEGATIVE_INFINITY) || (latitude == Float.NEGATIVE_INFINITY))
        {
            Logger.getLogger(getClass()).error("No Geo Tag / GPS Exif data exists for file/folder " + path);
            
            return;
        }

        resp.setContentType("text/plain");
        output.println("lat\tlon\ttitle\tdescription\ticon\ticonSize\ticonOffset");
        output.print(latitude);
        output.print('\t');
        output.print(longitude);
        output.print('\t');
        output.print(infoText);
        output.print('\t');
        output.print(description);
        output.print('\t');
        output.print("/webfilesys/images/OSMaps.png");
        output.print('\t');
        output.print("32,32");
        output.print('\t');
        output.println("-16,-16");
        output.flush();
    }
	
}