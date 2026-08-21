package de.webfilesys.gui.google;

import java.io.PrintWriter;
import java.util.ArrayList;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * @author Frank Hoehnel
 */
public class GoogleEarthSinglePlacemarkHandler extends GoogleEarthHandlerBase
{
	public GoogleEarthSinglePlacemarkHandler(
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
        String imgPath = req.getParameter("path");
        
        ArrayList placemarkElementList = new ArrayList();
        
        placemarkElementList.add(createPlacemark(imgPath));
        
        return placemarkElementList;
	}
}
