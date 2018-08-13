package de.webfilesys.gui.admin;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.webfilesys.LanguageManager;
import de.webfilesys.WebFileSys;
import de.webfilesys.gui.CSSManager;
import de.webfilesys.util.CommonUtils;

/**
 * Admin form to register a new user.
 * 
 * @author Frank Hoehnel
 */
public class AdminRegisterUserRequestHandler extends AdminRequestHandler
{
	String errorMsg = null;
	
	public AdminRegisterUserRequestHandler(
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
		output.println("<html>");
		output.println("<head>");

		output.println("<title>WebFileSys Administration: Add new User </title>");

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/common.css\">");
		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/admin.css\">");
		// output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/skins/" + userMgr.getCSS(uid) + ".css\">");
		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/skins/fmweb.css\">");

		output.println("<script src=\"/webfilesys/javascript/admin.js\" type=\"text/javascript\"></script>");
		output.println("<script src=\"/webfilesys/javascript/util.js\" type=\"text/javascript\"></script>");

		output.println("</head>");
		output.print("<body");
        if (File.separatorChar=='\\')
        {
        	output.print(" onload=\"switchAllDrivesAccess(document.getElementById('allDrives'), true)\"");
        }        	
		output.println(">");

		headLine("WebFileSys Administration: Add new User");

		output.println("<form id=\"userForm\" accept-charset=\"utf-8\" method=\"post\" action=\"/webfilesys/servlet\">");

		output.println("<input type=\"hidden\" name=\"command\" value=\"admin\">");
		output.println("<input type=\"hidden\" name=\"cmd\" value=\"addUser\">");

		output.println("<div id=\"validationErrorCont\">");
		output.println("<ul id=\"validationErrorList\"></ul>");
		output.println("</div>");
		
        output.println("<table class=\"dataForm\" width=\"100%\">");
        
		String val = "";
		if (errorMsg != null)
		{
			val = getParameter("username");
		}

        output.println("<tr>");
        output.println("<td class=\"formParm1\"><b>userid/login</b></td>");
		output.println("<td class=\"formParm2\"><input type=\"text\" id=\"username\" name=\"username\" required=\"required\" maxlength=\"32\" value=\"" + val + "\"></td>");
        output.println("</tr>");

        output.println("<tr>");
        output.println("<td class=\"formParm1\"><b>password</b></td>");
		output.println("<td class=\"formParm2\"><input type=\"password\" id=\"password\" name=\"password\" required=\"required\" maxlength=\"30\" value=\"\"></td>");
        output.println("</tr>");

        output.println("<tr>");
        output.println("<td class=\"formParm1\"><b>password confirmation</b></td>");
		output.println("<td class=\"formParm2\"><input type=\"password\" id=\"pwconfirm\" name=\"pwconfirm\" required=\"required\" maxlength=\"30\" value=\"\"></td>");
        output.println("</tr>");

        output.println("<tr>");
        output.println("<td class=\"formParm1\">read-only password</td>");
		output.println("<td class=\"formParm2\"><input type=\"password\" id=\"ropassword\" name=\"ropassword\" maxlength=\"30\" value=\"\"></td>");
        output.println("</tr>");

        output.println("<tr>");
        output.println("<td class=\"formParm1\">read-only password confirmation</td>");
		output.println("<td class=\"formParm2\"><input type=\"password\" id=\"ropwconfirm\" name=\"ropwconfirm\" maxlength=\"30\" value=\"\"></td>");
        output.println("</tr>");

		val = "";
		if (errorMsg != null)
		{
			val = getParameter("documentRoot");
			if (val == null) {
				val = "";
			}
		}

        output.println("<tr>");
        output.println("<td class=\"formParm1\"><b>document root</b></td>");
		output.println("<td class=\"formParm2\"><input type=\"text\" id=\"documentRoot\" name=\"documentRoot\" maxlength=\"255\" value=\"" + val + "\">");
		output.print("&nbsp;");
		output.println("<input type=\"button\" id=\"docRootButton\" value=\" ... \" onclick=\"javascript:selectDocRoot()\"></td>");
        output.println("</tr>");

        if (File.separatorChar=='\\')
        {
    		val = "";
    		if (errorMsg!=null)
    		{
    			if (getParameter("allDrives") != null)
    			{
    				val=" checked";
    			}
    		}

    		output.println("<tr>");
            output.println("<td class=\"formParm1\" style=\"padding-left:40px\">full access to all drives</td>");
    		output.println("<td class=\"formParm2\">");
    		output.println("<input type=\"checkbox\" id=\"allDrives\" name=\"allDrives\"" + val + " class=\"cb3\" onclick=\"switchAllDrivesAccess(this)\">");
    		output.println("</td>");
            output.println("</tr>");
        }
        
		val = "";
		if (errorMsg!=null)
		{
			if (getParameter("readonly") != null)
			{
				val=" checked";
			}
		}

        output.println("<tr>");
        output.println("<td class=\"formParm1\">");
        output.println("readonly access");
        output.println("</td>");
		output.println("<td class=\"formParm2\">");
		output.println("<input type=\"checkbox\" id=\"readonly\" name=\"readonly\"" + val + " class=\"cb3\">");
		output.println("</td>");
        output.println("</tr>");

		val = "";
		if (errorMsg != null)
		{
			val = getParameter("firstName");
		}

        output.println("<tr>");
        output.println("<td class=\"formParm1\">first name</td>");
		output.println("<td class=\"formParm2\"><input type=\"text\" id=\"firstName\" name=\"firstName\" maxlength=\"120\" value=\"" + val + "\"></td>");
        output.println("</tr>");

		val = "";
		if (errorMsg != null)
		{
			val = getParameter("lastName");
		}

        output.println("<tr>");
        output.println("<td class=\"formParm1\">last name</td>");
		output.println("<td class=\"formParm2\"><input type=\"text\" id=\"lastName\" name=\"lastName\" maxlength=\"120\" value=\"" + val + "\"></td>");
        output.println("</tr>");

		val = "";
		if (errorMsg != null)
		{
			val = getParameter("email");
		}

        output.println("<tr>");
        output.println("<td class=\"formParm1\"><b>e-mail address</b></td>");
		output.println("<td class=\"formParm2\"><input type=\"email\" id=\"email\" name=\"email\" required=\"required\" maxlength=\"120\" value=\"" + val + "\"></td>");
        output.println("</tr>");

		val = "";
		if (errorMsg != null)
		{
			val = getParameter("phone");
		}

        output.println("<tr>");
        output.println("<td class=\"formParm1\">phone</td>");
		output.println("<td class=\"formParm2\"><input type=\"text\" id=\"phone\" name=\"phone\" maxlength=\"30\" value=\"" + val + "\"></td>");
        output.println("</tr>");

        boolean diskQuotaChecked = false;
        
		val = "";
		if (errorMsg != null)
		{
			if (getParameter("checkDiskQuota")!=null)
			{
				val = " checked";
				diskQuotaChecked = true;
			}
		}

        output.println("<tr>");
        output.println("<td class=\"formParm1\">");
        output.println("check disk quota");
        output.println("</td>");
		output.println("<td class=\"formParm2\">");
		output.println("<input type=\"checkbox\" id=\"checkDiskQuota\" name=\"checkDiskQuota\"" + val + " class=\"cb3\" onclick=\"switchDiskQuota(this)\">");
		output.println("</td>");
        output.println("</tr>");

		val = "";
		if (diskQuotaChecked)
		{
			val = getParameter("diskQuota");
		}

        output.println("<tr>");
        output.println("<td class=\"formParm1\">disk quota (MBytes)</td>");
		output.println("<td class=\"formParm2\">");
		output.print("<input type=\"text\" id=\"diskQuota\" name=\"diskQuota\" maxlength=\"12\" value=\"" + val + "\"");
		if (!diskQuotaChecked) 
		{
			output.print(" disabled=\"disabled\"");
		}
		output.println(">");
		output.println("</td>");
        output.println("</tr>");

        output.println("<tr>");
        output.println("<td class=\"formParm1\"><b>role</b></td>");
		output.println("<td class=\"formParm2\"><select id=\"role\" name=\"role\" size=\"1\">");

		if ((errorMsg != null) && getParameter("role").equals("webspace"))
		{
			output.println("<option selected>webspace</option>");
		}
		else
		{
			output.println("<option>webspace</option>");
		}

		if ((errorMsg != null) && getParameter("role").equals("user"))
		{
			output.println("<option selected>user</option>");
		}
		else
		{
			output.println("<option>user</option>");
		}

		if ((errorMsg != null) && getParameter("role").equals("admin"))
		{
			output.println("<option selected>admin</option>");
		}
		else
		{
			output.println("<option>admin</option>");
		}
		
		output.println("</select></td>");
        output.println("</tr>");

        ArrayList<String> languages = LanguageManager.getInstance().getAvailableLanguages();

        output.println("<tr>");
        output.println("<td class=\"formParm1\"><b>language</b></td>");
		output.println("<td class=\"formParm2\"><select id=\"language\" name=\"language\" size=\"1\">");

		output.println("<option value=\"\">- select -</option>");

		for (int i = 0; i < languages.size(); i++)
		{
			if ((errorMsg != null) && getParameter("language").equals(languages.get(i)))
			{
				output.println("<option selected>" + languages.get(i) + "</option>");
			}
			else
			{
				output.println("<option>" + languages.get(i) + "</option>");
			}
		}
		output.println("</select></td>");
        output.println("</tr>");

		ArrayList<String> cssList = CSSManager.getInstance().getAvailableCss();

        output.println("<tr>");
        output.println("<td class=\"formParm1\"><b>layout (CSS file)</b></td>");
		output.println("<td class=\"formParm2\"><select id=\"css\" name=\"css\" size=\"1\">");

		for (int i = 0; i < cssList.size(); i++)
		{
			if (((errorMsg != null) && getParameter("css").equals(cssList.get(i))) ||
				((errorMsg == null) && cssList.get(i).equals(CSSManager.DEFAULT_LAYOUT)))
			{
				output.println("<option selected=\"selected\">" + cssList.get(i) + "</option>");
			}
			else
			{
				output.println("<option>" + cssList.get(i) + "</option>");
			}
		}
		output.println("</select></td>");
        output.println("</tr>");

		if (WebFileSys.getInstance().getMailHost() != null)
		{
			val = "";
			if (errorMsg != null)
			{
				if (getParameter("sendWelcomeMail")!=null)
				{
					val = " checked";
				}
			}

            output.println("<tr>");
			output.println("<td class=\"formParm1\">");
            output.println("send welcome mail");
            output.println("</td>");
            output.println("<td class=\"formParm2\">");
            output.println("<input type=\"checkbox\" id=\"sendWelcomeMail\" name=\"sendWelcomeMail\"" + val + " class=\"cb3\">");
			output.println("</td>");
            output.println("</tr>");
		}

		output.println("<tr><td colspan=\"2\">&nbsp;</td></tr>");

		output.println("<tr><td class=\"formButton\">");
		output.println("<input type=\"button\" name=\"addbutton\" value=\"Add new user\" onclick=\"validateUser();\">");
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
