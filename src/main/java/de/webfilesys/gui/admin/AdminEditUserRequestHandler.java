package de.webfilesys.gui.admin;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.LanguageManager;
import de.webfilesys.gui.CSSManager;
import de.webfilesys.user.TransientUser;

/**
 * Administrator edits the account of an user.
 * 
 * @author Frank Hoehnel
 */
public class AdminEditUserRequestHandler extends AdminRequestHandler
{
	String errorMsg = null;
	
	public AdminEditUserRequestHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output,
            String uid,
            String errorMsg)
	{
        super(req, resp, session, output, uid);
		
        this.errorMsg = errorMsg;
	}
	
	protected void process()
	{
		String login = getParameter("username");

		TransientUser user = userMgr.getUser(login);
		if (user == null) {
        	Logger.getLogger(getClass()).error("user not found: " + login);
        	return;
		}
		
		output.print("<html>");
		output.print("<head>");

		output.print("<title> WebFileSys Administration: Edit User </title>");

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/common.css\">");
		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/admin.css\">");
		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/skins/" + userMgr.getCSS(uid) + ".css\">");

		output.println("<script src=\"/webfilesys/javascript/admin.js\" type=\"text/javascript\"></script>");
		output.println("<script src=\"/webfilesys/javascript/util.js\" type=\"text/javascript\"></script>");
		
		output.print("</head>");
		output.print("<body");
        if (File.separatorChar=='\\')
        {
        	output.print(" onload=\"switchAllDrivesAccess(document.getElementById('allDrives'))\"");
        }        	
		output.println(">");

		headLine("WebFileSys Administration: Edit User " + login);

		output.println("<form id=\"userForm\" accept-charset=\"utf-8\" method=\"post\" action=\"/webfilesys/servlet\">");
		output.println("<input type=\"hidden\" name=\"command\" value=\"admin\">");
		output.println("<input type=\"hidden\" name=\"cmd\" value=\"changeUser\">");
		output.println("<input type=\"hidden\" name=\"username\" value=\"" + login + "\">");

		output.println("<div id=\"validationErrorCont\">");
		output.println("<ul id=\"validationErrorList\"></ul>");
		output.println("</div>");
		
		output.println("<table class=\"dataForm\" width=\"100%\">");
        
        output.println("<tr>");
        output.println("<td class=\"formParm1\"><b>userid / login</b></td>");
		output.println("<td class=\"formParm2\">" + login + "</td>");
        output.println("</tr>");

        output.println("<tr>");
        output.println("<td class=\"formParm1\">new password</td>");
		output.println("<td class=\"formParm2\"><input type=\"password\" id=\"password\" name=\"password\" maxlength=\"30\" value=\"\"></td>");
        output.println("</tr>");

        output.println("<tr>");
        output.println("<td class=\"formParm1\">password confirmation</td>");
		output.println("<td class=\"formParm2\"><input type=\"password\" id=\"pwconfirm\" name=\"pwconfirm\" maxlength=\"30\" value=\"\"></td>");
        output.println("</tr>");

        output.println("<tr>");
		output.println("<td class=\"formParm1\">read-only password</td>");
        output.println("<td class=\"formParm2\"><input type=\"password\" id=\"ropassword\" name=\"ropassword\" maxlength=\"30\" VALUE=\"\"></td>");
        output.println("</tr>");

        output.println("<tr>");
        output.println("<td class=\"formParm1\">read-only password confirmation</td>");
		output.println("<td class=\"formParm2\"><input type=\"password\" id=\"ropwconfirm\" name=\"ropwconfirm\" maxlength=\"30\" value=\"\"></td>");
        output.println("</tr>");

        String allDrives = null;
        if (File.separatorChar=='\\') 
        {
            allDrives = getParameter("allDrives");
        }
        
        String userDocRoot = null;
        
        if (errorMsg != null)
        {
            userDocRoot = req.getParameter("documentRoot");
        }
        else
        {
        	userDocRoot = user.getDocumentRoot();
            if (File.separatorChar == '\\') 
            {
            	if (userDocRoot.equals("*:")) 
            	{
            		allDrives = "true";
            		userDocRoot = "";
            	}
            }
        }
        
        if (userDocRoot == null)
        {
            userDocRoot = "";
        }
        
        output.println("<tr>");
        output.println("<td class=\"formParm1\"><b>document root</b></td>");
		output.println("<td class=\"formParm2\">");
		output.println("<input type=\"text\" name=\"documentRoot\" id=\"documentRoot\" maxlength=\"255\" value=\"" + userDocRoot + "\">");
		output.print("&nbsp;");
		output.println("<input type=\"button\" id=\"docRootButton\" value=\" ... \" onclick=\"javascript:selectDocRoot()\"></td>");
        output.println("</tr>");

        if (File.separatorChar=='\\')
        {
    		String val = "";
    		if (errorMsg != null)
    		{
    			if (getParameter("allDrives") != null)
    			{
    				val = " checked";
    			}
    		} 
    		else
    		{
    			if (allDrives != null) 
    			{
    				val = " checked";
    			}
    		}

    		output.println("<tr>");
            output.println("<td class=\"formParm1\" style=\"padding-left:40px\">full access to all drives</td>");
    		output.println("<td class=\"formParm2\">");
    		output.println("<input type=\"checkbox\" id=\"allDrives\" name=\"allDrives\"" + val + " class=\"cb3\" onclick=\"switchAllDrivesAccess(this)\">");
    		output.println("</td>");
            output.println("</tr>");
        }
        
        boolean readonly = false;
        
        if (errorMsg != null)
        {
            readonly = (req.getParameter("readonly") != null);
        }
        else
        {
        	readonly = user.isReadonly();
        }
        
        output.println("<tr>");
        output.println("<td class=\"formParm1\">");
        output.println("readonly access");
        output.println("</td>");
		output.println("<td class=\"formParm2\">");
		output.print("<input type=\"checkbox\" id=\"readonly\" name=\"readonly\" class=\"cb3\"");
		if (readonly)
		{
			output.print(" checked");
		}
		output.println(">");
        output.println("</tr>");

        String firstName = null;
        
        if (errorMsg != null)
        {
            firstName = req.getParameter("firstName");
        }
        else
        {
        	firstName = user.getFirstName();
        }
        
        if (firstName == null)
        {
            firstName = "";
        }
        
        output.println("<tr>");
        output.println("<td class=\"formParm1\">first name</td>");
		output.println("<td class=\"formParm2\"><input type=\"text\" id=\"firstName\" name=\"firstName\" maxlength=\"120\" value=\"" + firstName + "\"></td>");
        output.println("</tr>");

        String lastName = null;
        
        if (errorMsg != null)
        {
            lastName = req.getParameter("lastName");
        }
        else
        {
        	lastName = user.getLastName();
        }
        
        if (lastName == null)
        {
            lastName = "";
        }
        
        output.println("<tr>");
        output.println("<td class=\"formParm1\">last name</td>");
		output.println("<td class=\"formParm2\"><input type=\"text\" id=\"lastName\" name=\"lastName\" maxlength=\"120\" value=\"" + lastName + "\"></td>");
        output.println("</tr>");

        String val = null;

        if (errorMsg != null)
        {
            val = req.getParameter("email");
        }
        else
        {
        	val = user.getEmail();
        }
        
		if (val == null)
		{
			val = "";
		}
        output.println("<tr>");
        output.println("<td class=\"formParm1\"><b>e-mail address</b></td>");
		output.println("<td class=\"formParm2\"><input type=\"text\" id=\"email\" name=\"email\" maxlength=\"120\" value=\"" + val + "\"></td>");
        output.println("</tr>");

        if (errorMsg != null)
        {
            val = req.getParameter("phone");
        }
        else
        {
        	val = user.getPhone();
        }
        
		if (val == null)
		{
			val = "";
		}
        output.println("<tr>");
        output.println("<td class=\"formParm1\">phone</td>");
		output.println("<td class=\"formParm2\"><input type=\"text\" id=\"phone\" name=\"phone\" maxlength=\"30\" value=\"" + val + "\"></td>");
        output.println("</tr>");

		long diskQuota = user.getDiskQuota();
        
        boolean checkDiskQuota = false;
        
        if (errorMsg != null)
        {
            checkDiskQuota = (req.getParameter("checkDiskQuota") != null);
        }
        else
        {
            checkDiskQuota = (diskQuota > 0l);
        }
        
        output.println("<tr>");
        output.println("<td class=\"formParm1\">");
        output.println("check disk quota");
        output.println("</td>");
		output.println("<td class=\"formParm2\">");
		output.print("<input type=\"checkbox\" id=\"checkDiskQuota\" name=\"checkDiskQuota\" class=\"cb3\" onclick=\"switchDiskQuota(this)\"");
		if (checkDiskQuota)
		{
			output.print(" checked");
		}
		output.println(">");
		output.println("</td>");
        output.println("</tr>");

        val = null;

        if (errorMsg != null)
        {
            val = getParameter("diskQuota");
        }
        else
        {
            if (diskQuota > 0l)
            {
                val = "" + (diskQuota / (1024l * 1024l)) ;
            }
        }
        
        if (val == null)
        {
            val = "";
        }
        
        output.println("<tr>");
        output.println("<td class=\"formParm1\">disk quota (MBytes)</td>");
		output.println("<td class=\"formParm2\"><input type=\"text\" id=\"diskQuota\" name=\"diskQuota\" maxlength=\"12\" value=\"" + val + "\"");
		if (!checkDiskQuota) 
		{
			output.print(" disabled=\"disabled\"");
		}
		output.println(">");
		output.println("</td>");
        output.println("</tr>");

        output.println("<tr>");
        output.println("<td class=\"formParm1\"><b>role</b></td>");
		output.println("<td class=\"formParm2\"><select id=\"role\" name=\"role\" size=\"1\">");

        String role = null;

        if (errorMsg != null)
        {
            role = getParameter("role");
        }
        else 
        {
        	role = user.getRole();
        }

        if ((role == null) || role.equals("user"))
        {
            output.println("<option selected>user</option>");
        }
        else 
        {
            output.println("<option>user</option>");
        }
        
        if ((role != null) && role.equals("admin"))
        {
            output.println("<option selected>admin</option>");
        }
        else 
        {
            output.println("<option>admin</option>");
        }
        
        if ((role != null) && role.equals("webspace"))
        {
            output.println("<option selected>webspace</option>");
        }
        else 
        {
            output.println("<option>webspace</option>");
        }
        
		output.println("</select></td>");
        output.println("</tr>");
        
        String userLanguage = null;

        if (errorMsg != null)
        {
            userLanguage = getParameter("language");
        }
        else
        {
            userLanguage = user.getLanguage();
        }
        
		if (userLanguage == null)
		{
			userLanguage = LanguageManager.DEFAULT_LANGUAGE;
		}

		Vector languages = LanguageManager.getInstance().getAvailableLanguages();

        output.println("<tr>");
        output.println("<td class=\"formParm1\"><b>language</b></td>");
		output.println("<td class=\"formParm2\"><select id=\"language\" name=\"language\" size=\"1\">");

		for (int i=0;i<languages.size();i++)
		{
			String lang=(String) languages.elementAt(i);

			output.print("<option");

			if (lang.equals(userLanguage))
			{
				output.print(" selected=\"selected\"");
			}

			output.println(">" + lang + "</option>");
		}
		output.println("</select></td>");
        output.println("</tr>");

		String userCss = null;

		if (errorMsg != null)
		{
			userCss = getParameter("css");
		}
		else
		{
			userCss = user.getCss();
		}

		if (userCss == null)
		{
			userCss = CSSManager.DEFAULT_LAYOUT;
		}

		ArrayList<String> cssList = CSSManager.getInstance().getAvailableCss();

        output.println("<tr>");
        output.println("<td class=\"formParm1\"><b>layout (CSS file)</b></td>");
		output.println("<td class=\"formParm2\"><select id=\"css\" name=\"css\" size=\"1\">");

		for (int i = 0; i < cssList.size(); i++)
		{
			String css = (String) cssList.get(i);

			if (!css.equals("mobile")) 
			{
	            output.print("<option");

	            if (css.equals(userCss))
	            {
	                output.print(" selected=\"selected\"");
	            }

	            output.println(">" + css + "</option>");
			}
		}
		output.println("</select></td>");
        output.println("</tr>");

		output.println("<tr><td colspan=\"2\">&nbsp;</td></tr>");
		output.println("<tr><td class=\"formButton\">");
		output.println("<input type=\"button\" name=\"changebutton\" value=\"&nbsp;Save&nbsp;\" onclick=\"validateUser(true);\">");
		output.println("</td><td class=\"formButton\" align=\"right\">");
		output.println("<input type=\"button\" value=\"Cancel\" onclick=\"javascript:window.location.href='/webfilesys/servlet?command=admin&cmd=userList'\">");
		output.println("</td></tr>");    

		output.println("</table>");

		output.println("</form>");

		output.println("</body>");
		
		if (errorMsg != null)
		{
			output.println("<script language=\"javascript\">");
			output.println("addValidationError(null, '" + errorMsg + "');");
			output.println("</script>");
		}

		output.println("</html>");
		output.flush();
	}

}
