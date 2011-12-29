package de.webfilesys.gui.user;

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
public class MultiImageRequestHandler extends UserRequestHandler
{
	public static final String LIST_PREFIX = "list-";
	
	private static final int prefixLength = LIST_PREFIX.length();
	
	protected String actPath = null;
	
	protected Vector selectedFiles = null;

    protected String cmd = null;
	
	public MultiImageRequestHandler(
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
		if (actPath == null)
		{
			actPath = getCwd();
			
			if (actPath == null)
			{
				Logger.getLogger(getClass()).warn("current path cannot be determined");
				return;
			}
		}
		
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
