package de.webfilesys.gui.admin;

import java.io.PrintWriter;
import java.util.Date;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.SessionHandler;
import de.webfilesys.WebFileSys;

public class SessionListHandler extends AdminRequestHandler
{
	public SessionListHandler(
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
		output.println("<META HTTP-EQUIV=\"REFRESH\" CONTENT=\"30; URL=/webfilesys/servlet?command=admin&cmd=sessionList\">");

		String title = "WebFileSys Administration: Active Sessions";
		
		output.print("<TITLE>" + title + "</TITLE>");

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/css/" + userMgr.getCSS(uid) + ".css\">");

		output.println("</HEAD>");
		output.println("<BODY>");

		headLine(title);

		output.println("<br>");

		output.println("<table width=\"100%\" border=\"1\" cellspacing=\"0\">");
		output.println("<tr>");
		output.println("<th class=\"datahead\">user</th><th class=\"datahead\">host/IP</th><th class=\"datahead\">session id</th><th class=\"datahead\">creation time</th><th class=\"datahead\">last active time</th><th class=\"datahead\">inactive (min:sec)</th><th class=\"datahead\">browser</th><th class=\"datahead\">protocol</th></tr>");

		Enumeration sessionList = SessionHandler.getSessions();

		while (sessionList.hasMoreElements())
		{
			HttpSession session =(HttpSession) sessionList.nextElement();

			try
			{
				String userid = (String) session.getAttribute("userid");

				output.println("<tr>");

				if (userid == null)
				{
					userid = "[unknown]";
				}

				output.print("<td class=\"data\" align=\"left\" valign=\"top\">");
				output.print(userid);
				output.println("</td>");
				
				String hostIP = (String) session.getAttribute("clientAddress");
				
				if ((hostIP == null) || (hostIP.trim().length() == 0))
				{
					hostIP = "(unknown)";
				}
				
				output.print("<td class=\"data\" align=\"left\" valign=\"top\">");
				output.print(hostIP);
				output.println("</td>");
				
				output.print("<td class=\"data\" align=\"left\" valign=\"top\">");
				output.print("<font class=\"small\">");
				output.print(session.getId());
				output.print("</font>");
				output.println("</td>");

				output.print("<td class=\"data\" align=\"left\" valign=\"top\">");
				output.print(WebFileSys.getInstance().getLogDateFormat().format(new Date(session.getCreationTime())));
				output.println("</td>");

				output.print("<td class=\"data\" align=\"left\" valign=\"top\">");
				output.print(WebFileSys.getInstance().getLogDateFormat().format(new Date(session.getLastAccessedTime())));
				output.println("</td>");

				long inactiveTime=System.currentTimeMillis() - session.getLastAccessedTime();
				long inactiveMinutes=inactiveTime / 60000l;
				long inactiveSeconds=(inactiveTime % 60000l) / 1000l;

				output.print("<td class=\"data\" align=\"center\" valign=\"top\">");
				if (inactiveMinutes==0l)
				{
					output.print("0");
				}
				else
				{
					output.print(inactiveMinutes);
				}

				output.print(":");

				if (inactiveSeconds < 10l)
				{
					output.print("0");

					if (inactiveSeconds==0l)
					{
						output.print("0");
					}
					else
					{
						output.print(inactiveSeconds);
					}
				}
				else
				{
					output.print(inactiveSeconds);
				}

				output.println("</td>");

				String userAgent = (String) session.getAttribute("userAgent");

				output.println("<td class=\"data\" valign=\"top\">");

				if ((userAgent != null) && (userAgent.trim().length() > 0))
				{
					output.print("<font class=\"small\">");
					output.println(userAgent);
					output.println("</font>");
				}
				else
				{
					output.println("unknown");
				}

				output.println("</td>");

				output.println("<td class=\"data\" valign=\"top\">");
				
	           	output.print(session.getAttribute("protocol"));

	           	output.println("</td>");

				output.println("</tr>");
			}
			catch (IllegalStateException iex)
			{
				Logger.getLogger(getClass()).debug(iex);
			}
		}

		output.println("</table><br>");

		output.println("<form>");

		output.println("<input type=\"button\" value=\"Return\" onclick=\"window.location.href='/webfilesys/servlet?command=admin&cmd=menu'\">");

		output.println("</form>");

		output.println("</body></html>");
		output.flush();
	}
}
