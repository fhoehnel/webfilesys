package de.webfilesys.gui.ajax;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


import de.webfilesys.util.UTF8URLDecoder;

/**
 * @author Frank Hoehnel
 */
public class XmlMultiFileRequestHandler extends XmlRequestHandlerBase
{
	String actPath = null;
	
	protected ArrayList<String> selectedFiles = null;

	boolean delConfirmed = false;
	
	protected String cmd = null;
	
	public XmlMultiFileRequestHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid)
	{
        super(req, resp, session, output, uid);
		
		selectedFiles = new ArrayList<String>();

		// Enumeration allKeys=requestParms.keys();

		Enumeration allKeys = req.getParameterNames();
		
		while (allKeys.hasMoreElements())
		{
			String parmKey=(String) allKeys.nextElement();
			
			String parmValue = req.getParameter(parmKey);
			
			if (parmKey.equals("cmd"))
			{
				cmd=parmValue;
			}
			else if (parmKey.equals("cb-confirm"))
			{
				delConfirmed=true;
			}
			else if (parmKey.equals("actpath"))
			{
				actPath=parmValue;
			}
			else if ((!parmKey.equals("cb-setAll")) && (!parmKey.equals("command")))
			{
				try
				{
					String fileName = UTF8URLDecoder.decode(parmKey);
					selectedFiles.add(fileName); 
				}
				catch (Exception ue1)
				{
					LogManager.getLogger(getClass()).error(ue1);
				}
			}
		}

		session.setAttribute("selectedFiles", selectedFiles);
		
        if (actPath == null) 
        {
            actPath = getCwd();
        }
        else
        {
            if (isMobile()) {
                actPath = getAbsolutePath(actPath);
            }
        }
	}

	public void handleRequest()
	{
		if (!accessAllowed(actPath))
		{
			LogManager.getLogger(getClass()).warn("user " + uid + " tried to access folder outside of it's document root: " + actPath);
			return;
		}
		
		process();
	}
}
