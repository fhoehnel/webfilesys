package de.webfilesys.gui.admin;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author Frank Hoehnel
 */
public class LoginLogoutHistoryHandler extends LogRequestHandlerBase
{
	public LoginLogoutHistoryHandler(
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
        String logFileName = getSystemLogFilePath();

		String title = "WebFileSys Administration: Login/Logout History";
		
		output.println("<HTML>");
		output.println("<HEAD>");
		output.println("<TITLE>" + title + "</TITLE>");

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/css/" + userMgr.getCSS(uid) + ".css\">");

		output.println("</HEAD>"); 
		output.println("<body>");

		headLine(title);

        File logFile = null;
        if (logFileName != null) 
        {
            logFile = new File(logFileName);
            if (!logFile.exists()) 
            {
                logFile = null;
            }
        }
        
        if (logFile == null) {
            output.println("WebFileSys system log file could not be located! Check the log4j configuration.");
            output.println("</body></html>");
            output.flush();
            return;
        }

		output.println("<pre>");

		BufferedReader logIn = null;

		try
		{
			logIn = new BufferedReader(new FileReader(logFileName));

			String logLine = null;

			while ((logLine = logIn.readLine()) != null)
			{
				if (isLoginLogoutEvent(logLine))
			    {
					output.print("<span class=\"logNone\">");
					output.println(logLine);
					output.print("</span>");
			    }
			}
		}
		catch (IOException ioe)
		{
			System.out.println(ioe);
			output.println(ioe);
		}

		output.println("</pre>");

		output.println("<form>");

		output.println("<input type=\"button\" value=\"Return\" onclick=\"window.location.href='/webfilesys/servlet?command=admin&cmd=menu'\">");

		output.println("</form>");

		output.println("</body></html>");
		output.flush();
	}
	
	private boolean isLoginLogoutEvent(String logLine)
	{
		if (logLine.indexOf("login user") >= 0)
		{
			return(true);
		}
		
		if (logLine.indexOf("logout user") >= 0)
		{
			return(true);
		}
		
		if (logLine.indexOf("session expired") >= 0)
		{
			return(true);
		}
		
		return(false);
	}
}
