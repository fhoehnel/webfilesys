package de.webfilesys.gui.google;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;

import de.webfilesys.Constants;
import de.webfilesys.FileComparator;
import de.webfilesys.FileContainer;
import de.webfilesys.FileLinkSelector;
import de.webfilesys.FileSelectionStatus;

/**
 * @author Frank Hoehnel
 *
 */
public class GoogleEarthDirPlacemarkHandler extends GoogleEarthHandlerBase
{
    private static final int MAX_FILE_NUM = 100;
    
    public GoogleEarthDirPlacemarkHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid)
	{
        super(req, resp, session, output, uid);
 	}

    /**
     * @return List of placemark Element objects
     */
	protected ArrayList createPlacemarkXml() 
	{
        String path = getCwd();
        
        FileLinkSelector fileSelector = new FileLinkSelector(path, FileComparator.SORT_BY_FILENAME, true);

        FileSelectionStatus selectionStatus = fileSelector.selectFiles(Constants.JPEG_FILE_MASKS, -1, MAX_FILE_NUM, 0);

        ArrayList placemarkList = new ArrayList();
        
        Vector selectedFiles = selectionStatus.getSelectedFiles();
        
        if (selectedFiles != null)
        {
            for (int i = 0; i < selectedFiles.size(); i++)
            {
                FileContainer fileCont = (FileContainer) selectedFiles.elementAt(i);
                
                File imgFile = fileCont.getRealFile();
                
                Element placemarkElement = createPlacemark(imgFile.getAbsolutePath());
                
                if (placemarkElement != null)
                {
                    placemarkList.add(placemarkElement);
                }
            }
        }        
        
        return placemarkList;
	}
}
