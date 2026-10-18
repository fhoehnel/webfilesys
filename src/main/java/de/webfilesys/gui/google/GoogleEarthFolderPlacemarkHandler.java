package de.webfilesys.gui.google;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * @author Frank Hoehnel
 */
public class GoogleEarthFolderPlacemarkHandler extends GoogleEarthHandlerBase
{
	public GoogleEarthFolderPlacemarkHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid)
	{
        super(req, resp, session, output, uid);
 	}

	protected ArrayList createPlacemarkXml() 
	{
        String folderPath = getCwd();
        
        if (folderPath.endsWith(File.separator)) 
        {
            folderPath = folderPath + ".";
        }
        else
        {
            folderPath = folderPath + File.separator + ".";
        }
        
        ArrayList placemarkElementList = new ArrayList();
        
        placemarkElementList.add(createPlacemark(folderPath));
        
        return placemarkElementList;
	}
}
