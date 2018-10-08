package de.webfilesys.gui.user;

import java.io.PrintWriter;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.webfilesys.InvitationManager;
import de.webfilesys.LanguageManager;
import de.webfilesys.WebFileSys;
import de.webfilesys.util.CommonUtils;

/**
 * @author Frank Hoehnel
 */
public class PublishRequestHandler extends UserRequestHandler
{
	private boolean ssl = false;
	
	int serverPort = 80;
	
	public PublishRequestHandler(
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
		String expiration=getParameter("expiration");

        if (expiration == null)
        {
        	publishForm();
        	
        	return;
        }
        
		String actPath=getParameter("actPath");

		if ((!accessAllowed(actPath)) || (!checkWriteAccess()))
		{
			return;
		}

		String typeOfContent=getParameter("type");

		if (typeOfContent == null)
		{
			typeOfContent = "tree";
		}

		String includeSubdirs=getParameter("includeSub");

		int expDays=InvitationManager.EXPIRATION;

		if (expiration.trim().length() > 0)
		{
			try
			{
				expDays=Integer.parseInt(expiration);
			}
			catch (NumberFormatException nfex)
			{
			}
		}

		String invitationType=null;

		if (typeOfContent.equals("tree"))
		{
			invitationType=InvitationManager.INVITATION_TYPE_TREE;
		}
		else
		{
			if (typeOfContent.equals("pictures"))
			{
				invitationType=InvitationManager.INVITATION_TYPE_PICTURE;
			}
			else
			{
				if (includeSubdirs!=null)
				{
					invitationType=InvitationManager.INVITATION_TYPE_TREE;
				}
				else
				{
					invitationType=InvitationManager.INVITATION_TYPE_COMMON;
				}
			}
		}

		String publishType = getParameter("publishType");

		String commentsParm=getParameter("allowComments");

		boolean allowComments=(commentsParm!=null);  

		String virtualUser=null;
		
		if (invitationType.equals(InvitationManager.INVITATION_TYPE_TREE))
		{
			String role = "webspace";
            
			if ((publishType != null) && publishType.equals("album"))
			{
				role = "album";
			}
			
			virtualUser = userMgr.createVirtualUser(uid, actPath, role, expDays, getParameter("language"));
		}

		String accessCode=InvitationManager.getInstance().addInvitation(uid,actPath,expDays,invitationType,allowComments,virtualUser);

		if (invitationType.equals(InvitationManager.INVITATION_TYPE_TREE))
		{
			userMgr.setPassword(virtualUser, accessCode);
		}

		StringBuffer secretURL=new StringBuffer();

		if (ssl)
		{
			secretURL.append("https://");
		}
		else
		{
			secretURL.append("http://");
		}

		if (WebFileSys.getInstance().getServerDNS() != null)
		{
			secretURL.append(WebFileSys.getInstance().getServerDNS());
		}
		else
		{
			secretURL.append(WebFileSys.getInstance().getLocalIPAddress());
		}

		secretURL.append(":");

		secretURL.append(serverPort);

		if (invitationType.equals(InvitationManager.INVITATION_TYPE_TREE))
		{
			secretURL.append("/webfilesys/servlet?command=silentLogin&");
			secretURL.append(virtualUser);
			secretURL.append('=');
			secretURL.append(accessCode);

			/*
			if ((publishType != null) && publishType.equals("album"))
			{
				secretURL.append("&cmd=album");
			}
			*/
		}
		else
		{
			if (typeOfContent.equals("pictures"))
			{
				secretURL.append("/webfilesys/servlet?command=visitor&accessCode=");
			}
			else
			{
				secretURL.append("/webfilesys/servlet?command=public?accessCode=");
			}

			secretURL.append(accessCode);
		}

		output.println("<html>");
		output.println("<head>");

		output.println("<title>" + getResource("label.publishhead","Publish folder content") + "</title>");

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/common.css\">");
		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/skins/" + userMgr.getCSS(uid) + ".css\">");

		output.println("</head>"); 
		output.println("<body class=\"publish\">");

		headLine(getResource("label.published","Folder has been published"));

		output.println("<br/>");

		output.println("<form>");

		output.println("<table class=\"dataForm\" width=\"100%\">");

		output.println("<tr>");
        output.println("<td class=\"formParm1\">");
		output.println(getResource("label.directory","Folder") + ":");
		output.println("</td>");
		output.println("<td class=\"formParm2\">");
		output.println(CommonUtils.shortName(getHeadlinePath(actPath), 50));
		output.println("</td></tr>");

		output.println("<tr><td>&nbsp;</td></tr>");

		output.println("<tr>");
        output.println("<td colspan=\"2\" class=\"formParm1\">");
		output.println(getResource("label.accesscode","URL with access code") + ":");
		output.println("</td>");
        output.println("</tr>");
		output.println("<tr>");
        output.println("<td colspan=\"2\" class=\"formParm2\">");

		if (!invitationType.equals(InvitationManager.INVITATION_TYPE_TREE))
		{
			output.println("<a href=\"" + secretURL.toString() + "\" target=\"_blank\">");
		}

		output.println("<font class=\"small\">");

		output.println(secretURL.toString());

		output.println("</font>");

		if (!invitationType.equals(InvitationManager.INVITATION_TYPE_TREE))
		{
			output.println("</a>");
		}

        output.println("</td>");
        output.println("</tr>");

		output.println("<tr>");
        output.println("<td colspan=\"2\">&nbsp;</td>");
        output.println("</tr>");

        output.println("<tr>");
        output.println("<td colspan=\"2\" class=\"formButton\" style=\"text-align:center\">");
		output.println("<input type=\"button\" value=\"" + getResource("button.closewin","Close Window") + "\" onclick=\"self.close();\">");
        output.println("</td>");
        output.println("</tr>");

		output.println("</table>");

		output.println("</form>");

        output.println("</body>");
        output.println("</html>");
		output.flush();
	}
	
	protected void publishForm()
	{
		
		String actPath=getParameter("actPath");
		if ((actPath==null) || (actPath.length()==0))
		{
			actPath = getCwd();
		}

		if ((!accessAllowed(actPath)) || (!checkWriteAccess()))
		{
			return;
		}

		String typeOfContent=getParameter("type");

		output.println("<html>");
		output.println("<head>");
		output.println("<title>" + getResource("label.publishhead","Publish folder content") + "</title>");

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/common.css\">");
		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/skins/" + userMgr.getCSS(uid) + ".css\">");

		output.println("<script language=\"JavaScript\" src=\"javascript/fmweb.js\" type=\"text/javascript\"></script>"); 

		output.println("</head>"); 
		output.println("<body class=\"publish\">");

		headLine(getResource("label.publishhead","Publish folder content"));
        
        output.println("<br/>");

		output.println("<form accept-charset=\"utf-8\" name=\"form1\" method=\"post\" action=\"/webfilesys/servlet\">");

		output.println("<input type=\"hidden\" name=\"command\" value=\"publishFolder\">");

		output.println("<input type=\"hidden\" name=\"actPath\" value=\"" + actPath + "\">");
		output.println("<input type=\"hidden\" name=\"type\" value=\"" + typeOfContent + "\">");
		output.println("<input type=\"hidden\" name=\"includeSub\" value=\"true\">");

		String viewMode = getParameter("viewMode");
        
		if (viewMode != null)
		{
			output.println("<input type=\"hidden\" name=\"viewMode\" value=\"" + viewMode + "\">");
		}

		output.println("<table class=\"dataForm\" width=\"100%\">");

		output.println("<tr>");
        output.println("<td class=\"formParm1\">");
		output.println(getResource("label.directory","Folder") + ":");
		output.println("</td><td class=\"formParm2\">");
		output.println(CommonUtils.shortName(getHeadlinePath(actPath), 50));
		output.println("</td></tr>");

		String publishType = "explorer";
        
		if (viewMode != null)
		{
			publishType = "album";
		}
        
		output.println("<tr>");
        output.println("<td class=\"formParm1\">");
		output.println(getResource("label.publishType", "publish as") + ":");
		output.println("</td>");
		output.println("<td class=\"formParm2\">");
		output.println("<input type=\"radio\" class=\"cb3\" name=\"publishType\" value=\"explorer\"");
		if (publishType.equals("explorer"))
		{
			 output.print(" checked");
		}
		output.print(">");
		output.println(getResource("label.publishTypeExplorer", "file explorer"));
		output.println("&nbsp;&nbsp;&nbsp;");
		output.print("<input type=\"radio\" class=\"cb3\" name=\"publishType\" value=\"album\"");
		if (publishType.equals("album"))
		{
			 output.print(" checked");
		}
		// output.print(" onclick=\"setRelatedCheckbox(this, document.form1.includeSub)\"");		output.print(">");
		output.println(getResource("label.publishTypeAlbum", "picture album"));
		output.println("</td>");
		output.println("</tr>");

		output.println("<tr>");
        output.println("<td colspan=\"2\" class=\"formParm1\">");
		output.println("<input type=\"checkbox\" class=\"cb3\" name=\"allowComments\">");
		
		output.println(getResource("label.allowcomments","allow visitors to add comments"));
        output.println("</td>");
        output.println("</tr>");

		output.println("<tr>");
        output.println("<td class=\"formParm1\">");
		output.println(getResource("label.expiration","Expires after days") + ": ");
        output.println("</td>");
        output.println("<td class=\"formParm2\">");
		output.println("<input type=\"text\" name=\"expiration\" value=\"" + InvitationManager.EXPIRATION + "\" size=\"4\" maxlength=\"4\">");
        output.println("</td>");
        output.println("</tr>");

		if (!typeOfContent.equals("pictures"))
		{
			String userLanguage = userMgr.getLanguage(uid);

			if (userLanguage==null)
			{
				userLanguage=LanguageManager.DEFAULT_LANGUAGE;
			}

			ArrayList<String> languages = LanguageManager.getInstance().getAvailableLanguages();

            output.println("<tr>");
            output.println("<td class=\"formParm1\">");
			output.println(getResource("label.language",language) + ":");
            output.println("</td>");
            output.println("<td class=\"formParm2\">");
			output.println("<select name=\"language\" size=\"1\">");

			if (userLanguage.equals(LanguageManager.DEFAULT_LANGUAGE))
			{
				output.print("<option selected=\"true\">" + LanguageManager.DEFAULT_LANGUAGE + "</option>");
			}
			else
			{
				output.print("<option>" + LanguageManager.DEFAULT_LANGUAGE + "</option>");
			}

			for (int i=0;i<languages.size();i++)
			{
				String lang=(String) languages.get(i);

				output.print("<option");

				if (lang.equals(userLanguage))
				{
					output.print(" selected=\"true\"");
				}

				output.println(">" + lang + "</option>");
			}
			output.println("</select>");
		}

        output.println("</td>");
        output.println("</tr>");

		output.println("<tr><td colspan=\"2\">&nbsp;</td></tr>");

		output.println("<tr>");
        output.println("<td class=\"formButton\" colspan=\"2\">");
		output.println("<input type=\"submit\" value=\"" + getResource("button.publish","Publish") + "\">");
		output.println("<input type=\"button\" onclick=\"self.close();\" value=\"" + getResource("button.cancel","Cancel") + "\" style=\"float:right\">");
        output.println("</td>");
        output.println("</tr>");

		output.println("</table>");

		output.println("</form>");

		output.println("</body>");
        output.println("</html>");
		output.flush();
	}
}
