package de.webfilesys.gui.user;

import java.io.File;
import java.io.PrintWriter;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.Constants;
import de.webfilesys.FileComparator;
import de.webfilesys.FileContainer;
import de.webfilesys.FileLinkSelector;
import de.webfilesys.FileSelectionStatus;
import de.webfilesys.GeoTag;
import de.webfilesys.MetaInfManager;
import de.webfilesys.graphics.CameraExifData;
import de.webfilesys.util.CommonUtils;

/**
 * Points of interest for all picture files of the directory for Open Street Maps.
 * @author Frank Hoehnel
 */
public class OpenStreetMapFilesPOIHandler extends UserRequestHandler
{
    private static final int MAX_FILE_NUM = 10000;
	
	public OpenStreetMapFilesPOIHandler(
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
		
		File folder = new File(path);
		
		if (!folder.exists())
		{
			Logger.getLogger(getClass()).error("folder not found: " + path);
			
			return;
		}
		
		MetaInfManager metaInfMgr = MetaInfManager.getInstance();
		
        resp.setContentType("text/plain");
        output.println("lat\tlon\ttitle\tdescription\ticon\ticonSize\ticonOffset");
		
        FileLinkSelector fileSelector = new FileLinkSelector(path, FileComparator.SORT_BY_FILENAME, true);

        FileSelectionStatus selectionStatus = fileSelector.selectFiles(Constants.JPEG_FILE_MASKS, -1, MAX_FILE_NUM, 0);

        Vector selectedFiles = selectionStatus.getSelectedFiles();
        
        if (selectedFiles != null)
        {
            for (int i = 0; i < selectedFiles.size(); i++)
            {
                FileContainer fileCont = (FileContainer) selectedFiles.elementAt(i);
                
                File imgFile = fileCont.getRealFile();

        		GeoTag geoTag = metaInfMgr.getGeoTag(imgFile.getAbsolutePath());

        		boolean geoDataExist = false;
        		
        		float latitude = Float.NEGATIVE_INFINITY;
        		float longitude = Float.NEGATIVE_INFINITY;
        		String infoText = null;
        		String description = metaInfMgr.getDescription(imgFile.getAbsolutePath());
        		if ((description == null) || (description.trim().length() == 0)) {
        			description = fileCont.getName();
        		}
        		
        		if (geoTag != null)
        		{
        	        latitude = geoTag.getLatitude();
        	        longitude = geoTag.getLongitude();
        	        infoText = geoTag.getInfoText();
        	        geoDataExist = true;
        		}
        		else
        		{
                    String fileExt = CommonUtils.getFileExtension(imgFile.getAbsolutePath());
                    
                    if (fileExt.equals(".jpg") || fileExt.equals(".jpeg"))
                    {
                        // use GPS coordinates from Exif data if present in the JPEG file
                        CameraExifData exifData = new CameraExifData(imgFile.getAbsolutePath());

                        if (exifData.hasExifData())
                        {
                            latitude = exifData.getGpsLatitude();
                            longitude = exifData.getGpsLongitude();
                            
                            if ((latitude >= 0.0f) && (longitude >= 0.0f))
                            {
                                geoDataExist = true;
                                
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
        		
        		if ((infoText == null) || (infoText.trim().length() == 0)) {
        			infoText = fileCont.getName();
        		}
        		
                if (geoDataExist)
                {
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
                }
            }
        }

        output.flush();
    }
	
}