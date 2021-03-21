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
		output.println("<html>");
		output.println("<head>");
		output.println("<title> WebFileSys Administration </title>");

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/common.css\">");
		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/admin.css\">");
		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/icons.css\">");
		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/skins/fmweb.css\">");

		output.println("</head>");
		output.println("<body>");

		headLine("WebFileSys Administration");

		output.println("<br/>");        

		output.println("<div class=\"adminMenuEntry\">");
		output.println("<span class=\"icon-font icon-user iconAdminMenu\"></span>");
		output.println("<a href=\"/webfilesys/servlet?command=admin&cmd=userList&initial=true\">User Management</a>");
		output.println("</div>");
		
		output.println("<div class=\"adminMenuEntry\">");
		output.println("<span class=\"icon-font icon-watch iconAdminMenu\"></span>");
		output.println("<a href=\"/webfilesys/servlet?command=admin&cmd=sessionList\">Active Sessions</a>");
		output.println("</div>");

		if (WebFileSys.getInstance().getMailHost() !=null)
		{
			output.println("<div class=\"adminMenuEntry\">");
			output.println("<span class=\"icon-font icon-mail iconAdminMenu\"></span>");
			output.println("<a href=\"/webfilesys/servlet?command=admin&cmd=broadcast\">Broadcast e-mail</a>");
			output.println("</div>");
		}

		output.println("<div class=\"adminMenuEntry\">");
		if (WebFileSys.getInstance().isMaintananceMode())
		{
			output.println("<span class=\"icon-font icon-tool iconAdminMenu\"></span>");
			output.println("<a href=\"/webfilesys/servlet?command=admin&cmd=switchMode\">Back to normal Operation</a>");
		}
		else
		{
			output.println("<span class=\"icon-font icon-tool iconAdminMenu\"></span>");
			output.println("<a href=\"/webfilesys/servlet?command=admin&cmd=switchMode\">Switch to Maintanance Mode</a>");
		}
		output.println("</div>");

		output.println("<div class=\"adminMenuEntry\">");
		output.println("<span class=\"icon-font icon-hddrive iconAdminMenu\"></span>");
		output.println("<a href=\"/webfilesys/servlet?command=admin&cmd=viewLog\">View Event Log</a>");
		output.println("</div>");

		output.println("<div class=\"adminMenuEntry\">");
		output.println("<span class=\"icon-font icon-exit iconAdminMenu\"></span>");
		output.println("<a href=\"/webfilesys/servlet?command=admin&cmd=loginHistory\">Login/Logout Events</a>");
		output.println("</div>");

		output.println("<div style=\"padding-top:16px\">");
		output.println("<input type=\"button\" value=\"Logout\" onclick=\"window.location.href='/webfilesys/servlet?command=logout'\">");
		
		output.println("<span style=\"width:32px;display:inline-block\"></span>");
		
		output.println("<input type=\"button\" value=\"Return\" onclick=\"window.location.href='/webfilesys/servlet'\">");
		output.println("</div>");

		output.println("</body></html>");
		output.flush();
	}
}
