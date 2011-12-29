package de.webfilesys.gui.ajax;

import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.util.UTF8URLDecoder;

/**
 * @author Frank Hoehnel
 */
public class XslMultiFileRequestHandler extends XmlRequestHandlerBase
{
	String actPath = null;
	
	protected Vector selectedFiles = null;

	boolean delConfirmed = false;
	
	protected String cmd = null;
	
	public XslMultiFileRequestHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid)
	{
        super(req, resp, session, output, uid);
		
		selectedFiles = new Vector();

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
					Logger.getLogger(getClass()).error(ue1);
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
			Logger.getLogger(getClass()).warn("user " + uid + " tried to access folder outside of it's document root: " + actPath);
			return;
		}
		
		process();
	}
}
