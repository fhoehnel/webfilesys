package de.webfilesys.gui.ajax;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;

/**
 * @author Frank Hoehnel
 */
public class XmlSetScreenSizeHandler extends XmlRequestHandlerBase
{
	public XmlSetScreenSizeHandler(
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
        String screenWidth = req.getParameter("screenWidth");
        String screenHeight = req.getParameter("screenHeight");

        try
        {
            if (screenWidth != null)
            {
                session.setAttribute("screenWidth", Integer.parseInt(screenWidth));
            }
            if (screenHeight != null)
            {
                session.setAttribute("screenHeight", Integer.parseInt(screenHeight));
            }
        }
        catch (NumberFormatException ex)
        {
        }

        Element resultElement = doc.createElement("result");
        
        doc.appendChild(resultElement);
		
		processResponse();
	}
}
