package de.webfilesys.gui.admin;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.webfilesys.WebFileSys;

/**
 * @author Frank Hoehnel
 */
public class AdminMenuRequestHandler extends AdminRequestHandler
{
	public AdminMenuRequestHandler(
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
		output.println("<TITLE> WebFileSys Administration </TITLE>");

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/common.css\">");

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/skins/" + userMgr.getCSS(uid) + ".css\">");

		output.println("</HEAD>");
		output.println("<BODY>");

		headLine("WebFileSys Administration");

		output.println("<br>");        

		output.println("<table border=\"0\" cellspacing=\"5\" cellpadding=\"5\">");

		output.println("<tr>");
		output.println("<td align=\"left\"><a href=\"/webfilesys/servlet?command=admin&cmd=userList&initial=true\"><font color=\"maroon\" face=\"arial\" size=\"3\"><b>User Management</b></font></a></td></tr>");
		output.println("<tr>");
		output.println("<td align=left><a href=\"/webfilesys/servlet?command=admin&cmd=sessionList\"><font color=maroon face=arial size=3><b>Active Sessions</b></font></a></td></tr>");

		if (WebFileSys.getInstance().getMailHost() !=null)
		{
			output.println("<tr>");
			output.println("<td align=left><a href=\"/webfilesys/servlet?command=admin&cmd=broadcast\"><font color=maroon face=arial size=3><b>Broadcast e-mail</b></font></a></td></tr>");
		}

		if (WebFileSys.getInstance().isMaintananceMode())
		{
			output.println("<tr>");
			output.println("<td align=left><a href=\"/webfilesys/servlet?command=admin&cmd=switchMode\"><font color=maroon face=arial size=3><b>Back to normal Operation</b></font></a></td></tr>");
		}
		else
		{
			output.println("<tr>");
			output.println("<td align=left><a href=\"/webfilesys/servlet?command=admin&cmd=switchMode\"><font color=maroon face=arial size=3><b>Switch to Maintanance Mode</b></font></a></td></tr>");
		}

		output.println("<tr>");
		output.println("<td align=left><a href=\"/webfilesys/servlet?command=admin&cmd=viewLog\"><font color=maroon face=arial size=3><b>View Event Log</b></font></a></td></tr>");

		output.println("<tr>");
		output.println("<td align=left><a href=\"/webfilesys/servlet?command=admin&cmd=loginHistory\"><font color=maroon face=arial size=3><b>Login/Logout Events</b></font></a></td></tr>");

		output.println("</table><br>");

		output.println("<form>");

		output.println("<input type=\"button\" value=\"Logout\" onclick=\"window.location.href='/webfilesys/servlet?command=logout'\">");

		output.println("&nbsp;&nbsp;&nbsp;");

		output.println("<input type=\"button\" value=\"Return\" onclick=\"window.location.href='/webfilesys/servlet'\">");

		output.println("</form>");

		output.println("</body></html>");
		output.flush();
	}
}
