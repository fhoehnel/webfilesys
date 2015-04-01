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
public class LoginLogoutHistoryHandler extends LogRequestHandlerBase {
	public LoginLogoutHistoryHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid) {
        super(req, resp, session, output, uid);
	}
	
	protected void process() {
		String title = "WebFileSys Administration: Login/Logout History";
		
		output.println("<html>");
		output.println("<head>");
		output.println("<title>" + title + "</title>");

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/common.css\">");
		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/skins/" + userMgr.getCSS(uid) + ".css\">");

		output.println("</head>"); 
		output.println("<body>");

		headLine(title);

		String logFileName = getSystemLogFilePath();
		if (logFileName != null) {
	        File logFile = new File(logFileName);
	        if ((!logFile.exists()) || (!logFile.isFile()) || (!logFile.canRead())) {
	            output.println("WebFileSys system log file " + logFileName + " could not be located. Check the log4j configuration!");
	            output.println("</body></html>");
	            output.flush();
	            return;
	        }
		}

		output.println("<pre>");

		BufferedReader logIn = null;

		try {
			logIn = new BufferedReader(new FileReader(logFileName));

			String logLine = null;

			while ((logLine = logIn.readLine()) != null) {
				if (isLoginLogoutEvent(logLine)) {
					output.print("<span class=\"logNone\">");
					output.print(logLine);
					output.println("</span>");
			    }
			}
		} catch (IOException ioe) {
			System.out.println(ioe);
			output.println(ioe);
		} finally {
			if (logIn != null) {
				try {
					logIn.close();
				} catch (IOException ioex) {
				}
			}
		}

		output.println("</pre>");

		output.println("<form>");

		output.println("<input type=\"button\" value=\"Return\" onclick=\"window.location.href='/webfilesys/servlet?command=admin&cmd=menu'\">");

		output.println("</form>");

		output.println("</body></html>");
		output.flush();
	}
	
	private boolean isLoginLogoutEvent(String logLine) {
		if (logLine.indexOf("login user") >= 0) {
			return(true);
		}
		
		if (logLine.indexOf("logout user") >= 0) {
			return(true);
		}
		
		if (logLine.indexOf("session expired") >= 0) {
			return(true);
		}
		
		return(false);
	}
}
