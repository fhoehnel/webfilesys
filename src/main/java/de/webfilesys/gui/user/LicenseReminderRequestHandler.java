package de.webfilesys.gui.user;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author Frank Hoehnel
 */
public class LicenseReminderRequestHandler extends UserRequestHandler
{
	public LicenseReminderRequestHandler(
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
        output.println("<HTML>");
        output.println("<HEAD>");
        output.println("<TITLE> WebFileSys License Reminder </TITLE>");

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/css/" + userMgr.getCSS(uid) + ".css\">");

        output.println("</HEAD>");
        output.println("<BODY>");

        headLine("license reminder");

        output.println("<br>Your copy of WebFileSys has not been registered.<br>");
        output.println(
            "Please register for free by sending an email to the author:<br><br>");

        output.println(
            "<a href=\"mailto:frank_hoehnel@hotmail.com\">frank_hoehnel@hotmail.com</a><br><br>");

        output.println("</BODY></html>");
        output.flush();
	}
}
