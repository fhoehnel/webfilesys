package de.webfilesys.gui.admin;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.webfilesys.util.CommonUtils;

/**
 * @author Frank Hoehnel
 */
public class ViewLogRequestHandler extends LogRequestHandlerBase
{
	public ViewLogRequestHandler(
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
		
		String title = "WebFileSys Administration - Event Log";
		
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
		
		BufferedReader logIn=null;

		try
		{
			logIn=new BufferedReader(new FileReader(logFileName));

			String rawLine;

			while ((rawLine=logIn.readLine())!=null)
			{
				output.print("<span class=\"");

				if (rawLine.indexOf(" DEBUG ") > 0)
				{
					output.print("logDebug");
				}
				else
				{
					if (rawLine.indexOf(" INFO ") > 0)
					{
						output.print("logInfo");
					}
					else
					{
						if (rawLine.indexOf(" WARN ") > 0)
						{
							output.print("logWarn");
						}
						else
						{
							if (rawLine.indexOf(" ERROR ") > 0)
							{
								output.print("logError");
							}
							else
							{
								if (rawLine.indexOf(" FATAL ") > 0)
								{
									output.print("logFatal");
								}
								else
								{
									output.print("logNone");
								}
							}
						}
					}
				}

				output.print("\">");

				output.print(CommonUtils.escapeHTML(rawLine));

				output.println("</span>");
			}
		}
		catch (IOException ioe)
		{
			System.out.println(ioe);
			output.println(ioe);
		}

		output.println("</pre>");

		output.println("<script language=\"javascript\">");
		output.println("setTimeout(\"window.scrollTo(0,1000000)\",1000);");
		output.println("</script>");

		output.println("<form>");

		output.println("<input type=\"button\" value=\"Return\" onclick=\"window.location.href='/webfilesys/servlet?command=admin&cmd=menu'\">");

		output.println("</form>");

		output.println("</body></html>");
		output.flush();
	}
}
