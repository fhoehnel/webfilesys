package de.webfilesys.gui.user;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.webfilesys.InvitationManager;
import de.webfilesys.LanguageManager;
import de.webfilesys.WebFileSys;
import de.webfilesys.mail.EmailUtils;
import de.webfilesys.mail.SmtpEmail;
import de.webfilesys.util.CommonUtils;

/**
 * @author Frank Hoehnel
 */
public class PublishMailRequestHandler extends UserRequestHandler
{
	private boolean ssl = false;
	
	private int serverPort = 80;
	
	public PublishMailRequestHandler(
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
        	publishForm(null);
        	
        	return;		
        }
        
		String actPath=getParameter("actPath");

		if ((!accessAllowed(actPath)) || (!checkWriteAccess()))
		{
			return;
		}

		StringBuffer errorMsg=new StringBuffer();

		String msgText = null;
		String emailList = null;
        String subject = null;
		ArrayList<String> mailReceivers = null;

		String invite = getParameter("invite");

        if (invite != null)
        {
			msgText=getParameter("msgText");

			mailReceivers = new ArrayList<String>();

			emailList=getParameter("receiver");

			if (emailList.trim().length()==0)
			{
				errorMsg.append(getResource("alert.noreceiver","Enter at least one receiver"));
				errorMsg.append("\\n");
			}
			else
			{
				StringTokenizer emailParser=new StringTokenizer(emailList,",");

				while (emailParser.hasMoreTokens())
				{
					String email=emailParser.nextToken();

					if (!EmailUtils.emailSyntaxOk(email))
					{
						errorMsg.append(getResource("alert.emailsyntax","invalid e-mail address"));
						errorMsg.append(": ");
						errorMsg.append(email);
						errorMsg.append("\\n");
					}
					else
					{
						mailReceivers.add(email);
					}
				}
			}

			if (msgText.trim().length()==0)
			{
				errorMsg.append(getResource("alert.nomsgtext","Please enter the invitation text"));
				errorMsg.append("\\n");
			}
			
			subject=getParameter("subject");

			if ((subject==null) || (subject.trim().length()==0))
			{
				subject=getResource("subject.publish","Invitation to visit my web site");
			}
        }

		int expDays=InvitationManager.EXPIRATION;

		if (expiration.trim().length() > 0)
		{
			try
			{
				expDays=Integer.parseInt(expiration);
			}
			catch (NumberFormatException nfex)
			{
				errorMsg.append(getResource("alert.expinvalid","Expiration must be a number"));
				errorMsg.append("\\n");
			}
		}

		int pageSize = userMgr.getPageSize(uid);
		String pageSizeParm = getParameter("pageSize");
		if (!CommonUtils.isEmpty(pageSizeParm)) 
		{
			try 
			{
			    pageSize = Integer.parseInt(pageSizeParm);	
			}
			catch (NumberFormatException nfex)
			{
				errorMsg.append(getResource("alert.pageSizeInvalid","page size must be a number"));
				errorMsg.append("\\n");
			}
		}
		
		String includeSubdirs=getParameter("includeSub");

		String commentsParm=getParameter("allowComments");

		/*
		if ((includeSubdirs==null) && (commentsParm!=null))
		{
			errorMsg.append(getResource("alert.commentsForbidden","To allow comments subdirectories must be included"));
			errorMsg.append("\\n");
		}
		*/

		if (errorMsg.length() > 0)
		{
			publishForm(errorMsg.toString());
			return;
		}

		boolean allowComments = (commentsParm != null);  

		String typeOfContent = getParameter("type");
		
		if (typeOfContent == null)
		{
			typeOfContent = "tree";
		}

		String virtualUser = null;
		
		String invitationType = null;

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

		if (includeSubdirs!=null)
		{
            String role = "webspace";
            
			if ((publishType != null) && publishType.equals("album"))
			{
				role = "album";
			}

			virtualUser = userMgr.createVirtualUser(uid, actPath, role, expDays, getParameter("language"));
			
			userMgr.setPageSize(virtualUser, pageSize);
		}

		String accessCode=InvitationManager.getInstance().addInvitation(uid,actPath,expDays,invitationType,allowComments,virtualUser);

		if (includeSubdirs!=null)
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
			if ((publishType != null) && publishType.equals("album"))
			{
				secretURL.append("/webfilesys/visitor/");
				secretURL.append(virtualUser);
				secretURL.append('/');
				secretURL.append(accessCode);
			} 
			else 
			{
				secretURL.append("/webfilesys/servlet?command=silentLogin&");
				secretURL.append(virtualUser);
				secretURL.append('=');
				secretURL.append(accessCode);
			}
			
			/*
			if ((publishType != null) && publishType.equals("album"))
			{
				secretURL.append("&cmd=pictureAlbum");
			}
			*/
		}
		else
		{
			secretURL.append("/webfilesys/servlet?command=public&accessCode=");

			secretURL.append(accessCode);
		}

        if (invite != null)
        {
			StringBuffer content = new StringBuffer(msgText);
			content.append("\r\n\r\n");

            content.append(secretURL.toString());

			content.append("\r\n\r\n\r\n\r\n");

			SmtpEmail message=new SmtpEmail(mailReceivers,subject,content.toString());

			StringBuffer mailSenderName=new StringBuffer();

			String firstName=userMgr.getFirstName(uid);
			String lastName=userMgr.getLastName(uid);

			if ((firstName!=null) && (firstName.trim().length()>0))
			{
				mailSenderName.append(firstName);
				mailSenderName.append(' ');
			}

			if ((lastName!=null) && (lastName.trim().length()>0))
			{
				mailSenderName.append(lastName);
			}

			if (mailSenderName.length()==0)
			{
				mailSenderName.append(uid);
			}

			message.setMailSenderName(mailSenderName.toString());

			String mailSenderAddress=userMgr.getEmail(uid);

			if ((mailSenderAddress!=null) && (mailSenderAddress.trim().length()>0))
			{
				message.setMailSenderAddress(mailSenderAddress);
			}

			message.send();
        }

		output.println("<html>");
		output.println("<head>");

		output.println("<title>" + getResource("label.publishhead","Publish folder content") + "</title>");

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/common.css\">");
		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/skins/" + userMgr.getCSS(uid) + ".css\">");

		output.println("<script src=\"/webfilesys/javascript/publish.js\" type=\"text/javascript\"></script>");
		
		output.println("</head>"); 
		output.println("<body class=\"publish\">");

		headLine(getResource("label.published","Folder has been published"));

		output.println("<br/>");

        output.println("<table class=\"dataForm\" width=\"100%\">");

        output.println("<tr>");
        output.println("<td class=\"formParm1\">");
		output.println(getResource("label.directory","Folder") + ":");
		output.println("</td>");
        output.println("<td class=\"formParm2\">");
        output.println(CommonUtils.shortName(getHeadlinePath(actPath), 50));
        output.println("</td>");
        output.println("</tr>");

		output.println("<tr><td>&nbsp;</td></tr>");

		output.println("<tr>");
        output.println("<td colspan=\"2\" class=\"formParm1\">");
		output.println(getResource("label.accesscode","URL with access code") + ":");
        output.println("</td>");
        output.println("</tr>");
        output.println("<tr>");
        output.println("<td colspan=\"2\" class=\"formParm2\">");

		if (!invitationType.equals(InvitationManager.INVITATION_TYPE_TREE)) {
			output.println("<a href=\"" + secretURL.toString() + "\" target=\"_blank\">");

			output.println("<font class=\"small\">");

			output.println(secretURL.toString());

			output.println("</font>");

			output.println("</a>");
		} else {
			output.print("<textarea id=\"publicLinkCont\" readonly=\"readonly\" style=\"height:40px;width:100%\">");
			output.print(secretURL.toString());
			output.println("</textarea>");
		}

        output.println("</td>");
        output.println("</tr>");

		output.println("<tr><td>&nbsp;</td></tr>");

        output.println("<tr>");
        output.println("<td class=\"formButton\">");
		output.println("<input type=\"button\" value=\"" + getResource("button.closewin","Close Window") + "\" onclick=\"self.close();\">");
        output.println("</td>");
        output.println("<td class=\"formButton\" style=\"text-align:right\">");
		output.println("<input type=\"button\" value=\"" + getResource("button.copyPublicUrl","Copy to clipboard") + "\" onclick=\"copyPublicUrl();\">");
        output.println("</td>");
        output.println("</tr>");

		output.println("</table>");

		output.println("</form>");

        output.println("</body>");
        output.println("</html>");
		output.flush();
	}
	
	protected void publishForm(String errorMsg)
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

		output.println("<html>");
		output.println("<head>");

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/common.css\">");
		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/skins/" + userMgr.getCSS(uid) + ".css\">");

        output.println("<title>" + getResource("label.publishhead","Publish folder content")+ "</title>");

        if (errorMsg!=null)
		{
			javascriptAlert(errorMsg);
		}

		output.println("<script src=\"/webfilesys/javascript/fmweb.js\" type=\"text/javascript\"></script>"); 
		output.println("<script src=\"/webfilesys/javascript/publish.js\" type=\"text/javascript\"></script>"); 

		String initialPageSize = Integer.toString(userMgr.getPageSize(uid));
		output.println("<script type=\"text/javascript\">"); 
		output.println("var initialPageSize = " + initialPageSize + ";"); 
		output.println("</script>"); 
		
		output.println("</head>"); 
		
		output.println("<body class=\"publish\">");

		headLine(getResource("label.publishhead","Publish folder content"));
        
        output.println("<br/>");

		output.println("<form accept-charset=\"utf-8\" name=\"form1\" method=\"post\" action=\"/webfilesys/servlet\">");

		output.println("<input type=\"hidden\" name=\"command\" value=\"publish\">");

		output.println("<input type=\"hidden\" name=\"actPath\" value=\"" + actPath + "\">");

		output.println("<input type=\"hidden\" name=\"includeSub\" value=\"true\">");
		
		String typeOfContent=getParameter("type");

		output.println("<input type=\"hidden\" name=\"type\" value=\"" + typeOfContent + "\">");

		String viewMode = getParameter("viewMode");
        
		if (viewMode != null)
		{
			output.println("<input type=\"hidden\" name=\"viewMode\" value=\"" + viewMode + "\">");
		}

        output.println("<table class=\"dataForm\" width=\"100%\">");

        output.println("<tr>");
        output.println("<td class=\"formParm1\">");
		output.println(getResource("label.directory","Folder") + ":");
		output.println("</td>");
		output.println("<td class=\"formParm2\">");
        output.println(CommonUtils.shortName(getHeadlinePath(actPath), 50));
		output.println("</td></tr>");

        String publishType = "explorer";
        
        if (viewMode != null)
        {
        	publishType = "album";
        }
        
        if (errorMsg != null)
        {
        	publishType = getParameter("publishType");
        }

		output.println("<tr>");
        output.println("<td class=\"formParm1\">");
		output.println(getResource("label.publishType", "publish as") + ":");
		output.println("</td>");
		output.println("<td class=\"formParm2\">");
		output.println("<input type=\"radio\" class=\"cb3\" name=\"publishType\" value=\"explorer\" onclick=\"switchShowPageSize()\"");
		if (publishType.equals("explorer"))
		{
			 output.print(" checked");
		}
		output.print(">");
		output.println(getResource("label.publishTypeExplorer", "file explorer"));
        output.println("&nbsp;&nbsp;&nbsp;");
		output.print("<input type=\"radio\" class=\"cb3\" name=\"publishType\" value=\"album\" onclick=\"switchShowPageSize()\"");
		if (publishType.equals("album"))
		{
			 output.print(" checked");
		}
		// output.print(" onclick=\"setRelatedCheckbox(this, document.form1.includeSub)\"");
		output.print(">");
		output.println(getResource("label.publishTypeAlbum", "picture album"));
		output.println("</td>");
		output.println("</tr>");

		String pageSize = "";
		if (errorMsg != null)
		{
			pageSize = getParameter("pageSize");
		}
		
		output.println("<tr>");
        output.println("<td class=\"formParm1\">");
		output.println(getResource("albumPageSize", "pictures per page") + ":");
		output.println("</td>");
		output.println("<td class=\"formParm2\">");
		output.println("<input id=\"pageSize\" type=\"text\" name=\"pageSize\" size=\"4\" maxlength=\"3\" value=\"" + pageSize + "\"");
		if (!publishType.equals("album"))
		{
			 output.print(" disabled=\"disabled\"");
		}
		output.println(" />");
		output.println("</td>");
		output.println("</tr>");
		
		/*
		output.println("<tr><td colspan=\"2\" class=\"prompt\">");
		output.print("<input type=\"checkbox\" class=\"cb\" name=\"includeSub\" onclick=\"checkPublishType()\"");
		if ((errorMsg==null) || (getParameter("includeSub")!=null))
		{
			output.print(" checked");
		}
		output.println(" onclick=\"setDependendCheckbox(document.form1.includeSub,document.form1.allowComments)\">");
		output.println(getResource("label.publishtree","publish folder tree including subfolders"));
		output.println("</td></tr>");
		*/

		output.println("<tr><td colspan=\"2\" class=\"formParm1\">");
		output.println("<input type=\"checkbox\" class=\"cb3\" name=\"allowComments\"");
		if ((errorMsg!=null) && (getParameter("allowComments")!=null))
		{
			output.print(" checked");
		}
		// output.println(" onclick=\"setDependendCheckbox(document.form1.includeSub,document.form1.allowComments)\">");
		output.println(">");
		output.println(getResource("label.allowcomments","allow visitors to add comments"));
		output.println("</td></tr>");

        String invite = getParameter("invite");

		output.println("<tr>");
        output.println("<td colspan=\"2\" class=\"formParm2\">");
        
        // start section email
        output.println("<table class=\"dataForm\" width=\"100%\">");
        output.println("<tr>");
        output.println("<td colspan=\"2\" class=\"formParm2\">");
        
		output.println("<input type=\"checkbox\" class=\"cb3\" name=\"invite\"");
		if ((errorMsg!=null) && (invite != null))
		{
			output.print(" checked");
		}
		output.println(" onclick=\"switchInviteFlag()\">");
		output.println(getResource("label.sendInvitationMail","send invitation e-mail"));
        output.println("</td>");
        output.println("</tr>");

		output.println("<tr><td colspan=\"2\" class=\"formParm1\">");
		output.println(getResource("label.receiver","Receiver(s) (comma-separated list of e-mail addresses)") + ":");
		output.println("</td></tr>");

		String temp = null;

		if (errorMsg!=null)
		{
			temp=getParameter("receiver");
		}

		if (temp==null)
		{
			temp="nobody@nowhere.com";
		}

		output.println("<tr><td colspan=\"2\" class=\"formParm2\">");
		output.print("<input type=\"text\" name=\"receiver\" value=\"" + temp + "\" maxlength=\"256\"");
		if (invite == null)
		{
		    output.print(" disabled=\"true\"");
		}		output.println(" style=\"width:100%;\">");
		output.println("</td></tr>");

		output.println("<tr><td colspan=\"2\" class=\"formParm1\">");
		output.println(getResource("label.subject","Subject") + ":");
        output.println("</td>");
        output.println("</tr>");

		temp=null;

		if (errorMsg!=null)
		{
			temp=getParameter("subject");
		}

		if (temp==null)
		{
			temp=getResource("subject.publish","Invitation to visit my web site");
		}

		output.println("<tr><td colspan=\"2\" class=\"formParm2\">");
		output.print("<input type=\"text\" name=\"subject\" value=\"" + temp + "\" maxlength=\"60\"");
		if (invite == null)
		{
			output.print(" disabled=\"true\"");
		}
		output.println(" style=\"width:100%;\">");
        output.println("</td>");
        output.println("</tr>");

		temp="";

		if (errorMsg!=null)
		{
			temp=getParameter("msgText");
		}

		output.println("<tr><td colspan=\"2\" class=\"formParm1\">");
		output.println(getResource("label.invitationtext","The invitation text") + ":");
		output.println("</td></tr>");

		output.println("<tr><td colspan=\"2\" class=\"formParm2\">");
		output.print("<textarea name=\"msgText\" rows=\"3\"");
		if (invite == null)
		{
			output.print(" disabled=\"true\"");
		}
		output.println(" style=\"width:100%\">" + temp + "</textarea>");
        output.println("</td>");
        output.println("</tr>");

        output.println("</table>");
        output.println("</td>");
        output.println("</tr>");
        // end section e-mail
        
        output.println("<tr>");
        output.println("<td class=\"formParm1\">");
		output.println(getResource("label.expiration","Expires after days") + ":");
        output.println("</td>");

		temp = null;

		if (errorMsg != null)
		{
			temp = getParameter("expiration");
		}

		if (temp == null)
		{
			temp = Integer.toString(InvitationManager.EXPIRATION);
		}

		output.println("<td class=\"formParm2\">");
        output.println("<input type=\"text\" name=\"expiration\" value=\"" + temp + "\" size=\"4\" maxlength=\"4\">");
        output.println("</td>");
        output.println("</tr>");

		String userLanguage = null;

		if (errorMsg != null)
		{
			userLanguage = getParameter("language");
		}
		else
		{
			userLanguage = userMgr.getLanguage(uid);

			if (userLanguage == null)
			{
				userLanguage = LanguageManager.DEFAULT_LANGUAGE;
			}
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
        output.println("</td>");
        output.println("</tr>");

		output.println("<tr><td colspan=\"2\">&nbsp;</td></tr>");

        output.println("<tr>");
        output.println("<td class=\"formButton\">");
		output.println("<input type=\"submit\" value=\"" + getResource("button.publish","Publish") + "\">");
		output.println("</td>");
        output.println("<td class=\"formButton\" style=\"text-align:right\">");
		output.println("<input type=\"button\" onclick=\"self.close();\" value=\"" + getResource("button.cancel","Cancel") + "\">");
        output.println("</td>");
        output.println("</tr>");

		output.println("</table>");

		output.println("</form>");

        output.println("</body>");
        output.println("</html>");
		output.flush();
	}
}
