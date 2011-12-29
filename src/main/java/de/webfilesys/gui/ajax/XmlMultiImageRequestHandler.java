package de.webfilesys.gui.ajax;

import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

/**
 * @author Frank Hoehnel
 */
public class XmlMultiImageRequestHandler extends XmlRequestHandlerBase
{
	public static final String LIST_PREFIX = "list-";
	
	private static final int prefixLength = LIST_PREFIX.length();
	
	protected String actPath = null;
	
	protected Vector selectedFiles = null;

	protected boolean delConfirmed = false;
    
    protected String cmd = null;
	
	public XmlMultiImageRequestHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid)
	{
        super(req, resp, session, output, uid);
		
		selectedFiles=new Vector();

		Enumeration allKeys = req.getParameterNames();

		while (allKeys.hasMoreElements())
		{
			String parmKey=(String) allKeys.nextElement();

            if (parmKey.startsWith(LIST_PREFIX))
            {
				String fileName = parmKey.substring(prefixLength);
				
				selectedFiles.add(fileName); 
            }
			else if (parmKey.equals("cb-confirm"))
			{
				delConfirmed=true;
			}
			else if (parmKey.equals("actpath"))
			{
				actPath = req.getParameter(parmKey);
			}
            else if (parmKey.equals("cmd"))
            {
                cmd = req.getParameter(parmKey);
            }
		}
		
		session.setAttribute("selectedFiles", selectedFiles);
	}

    public void handleRequest()
    {
        if (!accessAllowed(actPath))
        {
            Logger.getLogger(getClass()).warn("user " + uid + " tried to access folder outside of it's document root: " + actPath);
            return;
        }
        
        process();
    }
}
