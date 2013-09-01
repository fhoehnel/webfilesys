package de.webfilesys.gui.user;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.webfilesys.InvitationManager;
import de.webfilesys.LanguageManager;
import de.webfilesys.WebFileSys;
import de.webfilesys.util.UTF8URLEncoder;

/**
 * @author Frank Hoehnel
 */
public class PublishListRequestHandler extends UserRequestHandler
{
	private boolean ssl = false;
	
	private int serverPort = 80;
	
	public PublishListRequestHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid)
	{
        super(req, resp, session, output, uid);
		
		String protocol = req.getScheme();
		
		if (protocol.toLowerCase().startsWith("https"))
		{
			ssl = true;
		}
		
		serverPort = req.getServerPort();
	}

	protected void process()
	{
		output.println("<HTML>");
		output.println("<HEAD>");

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/common.css\">");
		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/skins/" + userMgr.getCSS(uid) + ".css\">");

		output.println("<title>" + getResource("label.publishList","Published Folders and Files") + "</title>");

		output.println("</HEAD>"); 
		output.println("<BODY>");

		headLine(getResource("label.publishList","Published Folders and Files"));

		output.println("<br>");

		Vector publishCodes = InvitationManager.getInstance().getInvitationsByOwner(uid);

		SimpleDateFormat dateFormat = LanguageManager.getInstance().getDateFormat(language);

        boolean tableHeadPrinted = false;

        if (publishCodes != null)
        {
			for (int i=0;i<publishCodes.size();i++)
			{
				String accessCode=(String) publishCodes.elementAt(i);

				String path=InvitationManager.getInstance().getInvitationPath(accessCode);

				if (path!=null) // not expired
				{
					if (!tableHeadPrinted)
					{
						output.println("<table border=\"1\" width=\"100%\" cellpadding=\"2\" cellspacing=\"0\">");

						output.println("<tr>");
						output.println("<th class=\"datahead\">" + getResource("label.path","Path") + "</th>");
						output.println("<th class=\"datahead\">" + getResource("label.expires","Expires") + "</th>");
						output.println("<th class=\"datahead\">" + getResource("label.accesscode","Access Code") + "</th>");
						output.println("<th class=\"datahead\">&nbsp;</th>");
						output.println("</tr>");

						tableHeadPrinted = true;
					}

					output.println("<tr>");

					String relativePath = this.getHeadlinePath(path);
				
					Date expTime=InvitationManager.getInstance().getExpirationTime(accessCode);

					output.println("<td class=\"data\">" + relativePath + "</td>");
					output.println("<td class=\"data\">" + dateFormat.format(expTime) + "</td>");

					StringBuffer secretLink=new StringBuffer();

					if (ssl)
					{
						secretLink.append("https://");
					}
					else
					{
						secretLink.append("http://");
					}

					if (WebFileSys.getInstance().getServerDNS() != null)
					{
						secretLink.append(WebFileSys.getInstance().getServerDNS());
					}
					else
					{
						secretLink.append(WebFileSys.getInstance().getLocalIPAddress());
					}

					if (serverPort != 80)
					{
						secretLink.append(":");

						secretLink.append(serverPort);
					}
					
					String type = InvitationManager.getInstance().getInvitationType(accessCode);

					if (type.equals(InvitationManager.INVITATION_TYPE_TREE))
					{
						secretLink.append("/webfilesys/servlet?command=silentLogin&");
						
						String virtualUserId = InvitationManager.getInstance().getVirtualUser(accessCode);
						
						secretLink.append(virtualUserId);
						secretLink.append('=');
						secretLink.append(accessCode);
					}
					else
					{
						secretLink.append("/webfilesys/servlet?command=visitorFile");

						secretLink.append("&accessCode=");
						secretLink.append(accessCode);
					}

					output.println("<td class=\"data\">");

					if (type.equals(InvitationManager.INVITATION_TYPE_TREE))
					{
						output.println("<font class=\"small\">" + secretLink.toString() + "</font>");
					}
					else
					{
						output.println("<a href=\"" + secretLink.toString() + "\" target=\"_blank\">");
						output.println("<font class=\"small\">" + secretLink.toString() + "</font></a>");
					}
					output.println("</td>");

					output.print("<td class=\"data\"><a href=\"/webfilesys/servlet?command=cancelPublish&accessCode=" + UTF8URLEncoder.encode(accessCode) + "\">");
					output.println("<img src=\"/webfilesys/images/trash.gif\" alt=\"" + getResource("label.cancelpublish","Cancel publish") + "\" border=\"0\"></a></td>");

					output.println("</tr>");
				}
			}
        }

        if (tableHeadPrinted)
        {
			output.println("</table>");
        }
        else
		{
			output.println("<table border=\"0\"><tr>");
			output.println("<td class=\"value\">");
			
			output.println(getResource("alert.notpublished","There are no published folders"));

			output.println("</td></tr></table>");
		}

		output.println("<br><form accept-charset=\"utf-8\" name=\"form1\">");
		output.println("<center>");
		output.println("<input type=\"button\" value=\"" + getResource("button.closewin","Close Window") + "\" href=\"#\" onclick=\"javascript:self.close()\">");
		output.println("</form>");

		output.println("</body></html>");

		output.flush();
	}

}
