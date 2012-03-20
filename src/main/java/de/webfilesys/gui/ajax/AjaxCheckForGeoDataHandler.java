package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.PrintWriter;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import de.webfilesys.Constants;
import de.webfilesys.FileComparator;
import de.webfilesys.FileContainer;
import de.webfilesys.FileLinkSelector;
import de.webfilesys.FileSelectionStatus;
import de.webfilesys.GeoTag;
import de.webfilesys.MetaInfManager;
import de.webfilesys.graphics.CameraExifData;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 *
 */
public class AjaxCheckForGeoDataHandler extends XmlRequestHandlerBase
{
    private static final int MAX_FILE_NUM = 10000;
    
	public AjaxCheckForGeoDataHandler(
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
        String path = getCwd();

        boolean geoDataExist = false;
        
        FileLinkSelector fileSelector = new FileLinkSelector(path, FileComparator.SORT_BY_FILENAME, true);

        FileSelectionStatus selectionStatus = fileSelector.selectFiles(Constants.JPEG_FILE_MASKS, -1, MAX_FILE_NUM, 0);

        Vector selectedFiles = selectionStatus.getSelectedFiles();
        
        if (selectedFiles != null)
        {
            for (int i = 0; (!geoDataExist) && (i < selectedFiles.size()); i++)
            {
                FileContainer fileCont = (FileContainer) selectedFiles.elementAt(i);
                
                File imgFile = fileCont.getRealFile();
                
                if (hasGeoData(imgFile.getAbsolutePath()))
                {
                    geoDataExist = true;    
                }
            }
        }        

        Element resultElement = doc.createElement("result");
        
        XmlUtil.setElementText(resultElement, Boolean.toString(geoDataExist));
        
        doc.appendChild(resultElement);
		
		processResponse();
	}
	
	private boolean hasGeoData(String imgPath)
	{
        GeoTag geoTag = MetaInfManager.getInstance().getGeoTag(imgPath);

        if (geoTag != null)
        {
            return true;
        }
        
        String fileExt = CommonUtils.getFileExtension(imgPath);
        
        if (fileExt.equals(".jpg") || fileExt.equals(".jpeg"))
        {
            CameraExifData exifData = new CameraExifData(imgPath);

            if (exifData.hasExifData())
            {
                float gpsLatitude = exifData.getGpsLatitude();
                float gpsLongitude = exifData.getGpsLongitude();
                
                if ((gpsLatitude >= 0.0f) && (gpsLongitude >= 0.0f))
                {
                    return true;
                }
            }
        }
        
        return false;
    }
}
