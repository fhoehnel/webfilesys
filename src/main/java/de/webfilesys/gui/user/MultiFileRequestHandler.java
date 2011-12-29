package de.webfilesys.gui.user;

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
public class MultiFileRequestHandler extends UserRequestHandler
{
	protected String actPath = null;
	
	protected Vector selectedFiles = null;

	protected String cmd = null;
	
	public MultiFileRequestHandler(
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
			String parm_key=(String) allKeys.nextElement();

			String parm_value = req.getParameter(parm_key);
			
			if (parm_key.equals("cmd"))
			{
				cmd=parm_value;
			}
			else if (parm_key.equals("actpath"))
			{
				actPath = parm_value;
			}
			else if ((!parm_key.equals("cb-setAll")) && (!parm_key.equals("command")))
			{
				try
				{
					String fileName = UTF8URLDecoder.decode(parm_key);
					selectedFiles.add(fileName); 
				}
				catch (Exception ue1)
				{
					System.out.println(ue1);
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
		
		if (selectedFiles.size()==0)
		{
			output.print("<HTML>");
			output.print("<HEAD>");

			javascriptAlert(getResource("alert.noFilesSelected","No files have been selected"));

			output.println("<script language=\"javascript\">");
			output.println("window.location.href='/webfilesys/servlet?command=listFiles';");
			output.println("</script>");
			output.println("</HEAD></HTML>");
			output.flush();
			return;
		}

		process();
	}
}
