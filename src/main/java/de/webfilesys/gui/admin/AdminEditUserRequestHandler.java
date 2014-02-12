package de.webfilesys.gui.admin;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.webfilesys.LanguageManager;
import de.webfilesys.gui.CSSManager;

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

		output.print("<HTML>");
		output.print("<HEAD>");

		output.print("<TITLE> WebFileSys Administration: Edit User </TITLE>");

		if (errorMsg!=null)
		{
			output.println("<script language=\"javascript\">");
			output.println("alert('" + errorMsg + "');");
			output.println("</script>");
		}

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/common.css\">");
		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/skins/" + userMgr.getCSS(uid) + ".css\">");

		output.println("<script language=\"javascript\">");
		output.println("function selectDocRoot()");
		output.println("{docRootWin=open('/webfilesys/servlet?command=admin&cmd=selectDocRoot','docRootWin','status=no,toolbar=no,menu=no,width=550,height=500,resizable=yes,scrollbars=yes,left=100,top=50,screenX=100,screenY=50');docRootWin.focus();}");
		output.println("</script>");

		output.print("</HEAD>");
		output.println("<BODY>");

		headLine("WebFileSys Administration: Edit User " + login);

		output.println("<br>");

		output.println("<form accept-charset=\"utf-8\" method=\"post\" action=\"/webfilesys/servlet\">");
		output.println("<input type=\"hidden\" name=\"command\" value=\"admin\">");
		output.println("<input type=\"hidden\" name=\"cmd\" value=\"changeUser\">");
		output.println("<input type=\"hidden\" name=\"username\" value=\"" + login + "\">");

		output.println("<table class=\"dataForm\" width=\"100%\">");
        
        output.println("<tr>");
        output.println("<td class=\"formParm1\"><b>userid / login</b></td>");
		output.println("<td class=\"formParm2\">" + login + "</td>");
        output.println("</tr>");

        output.println("<tr>");
        output.println("<td class=\"formParm1\"><b>password</b></td>");
		output.println("<td class=\"formParm2\"><input type=\"password\" name=\"password\" maxlength=\"30\" value=\"\"></td>");
        output.println("</tr>");

        output.println("<tr>");
        output.println("<td class=\"formParm1\"><b>password confirmation</b></td>");
		output.println("<td class=\"formParm2\"><input type=\"password\" name=\"pwconfirm\" maxlength=\"30\" value=\"\"></td>");
        output.println("</tr>");

        output.println("<tr>");
		output.println("<td class=\"formParm1\">read-only password</td>");
        output.println("<td class=\"formParm2\"><input type=\"password\" name=\"ropassword\" maxlength=\"30\" VALUE=\"\"></td>");
        output.println("</tr>");

        output.println("<tr>");
        output.println("<td class=\"formParm1\">read-only password confirmation</td>");
		output.println("<td class=\"formParm2\"><input type=\"password\" name=\"ropwconfirm\" maxlength=\"30\" value=\"\"></td>");
        output.println("</tr>");

        String userDocRoot = null;
        
        if (errorMsg != null)
        {
            userDocRoot = req.getParameter("documentRoot");
        }
        else
        {
            userDocRoot = userMgr.getDocumentRoot(login);
        }
        
        if (userDocRoot == null)
        {
            userDocRoot = "";
        }
        
        output.println("<tr>");
        output.print("<td class=\"formParm1\"><b>document root");
        if (File.separatorChar=='\\')
        {
            output.println(" (&quot; *: &quot for all drives)");
        }
        output.println("</b></td>");
		output.println("<td class=\"formParm2\"><input type=\"text\" name=\"documentRoot\" maxlength=\"255\" value=\"" + userDocRoot + "\" style=\"width:300px\">");
		output.print("&nbsp;");
		output.println("<input type=\"button\" value=\" ... \" onclick=\"javascript:selectDocRoot()\"></td>");
        output.println("</tr>");

        boolean readonly = false;
        
        if (errorMsg != null)
        {
            readonly = (req.getParameter("readonly") != null);
        }
        else
        {
            readonly = userMgr.isReadonly(login);
        }
        
        output.println("<tr>");
        output.println("</td><td class=\"formParm1\">");
        output.println("readonly access");
        output.println("</td>");
		output.println("<td class=\"formParm2\">");
		output.print("<input type=\"checkbox\" name=\"readonly\" class=\"cb3\"");
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
            firstName = userMgr.getFirstName(login);
        }
        
        if (firstName == null)
        {
            firstName = "";
        }
        
        output.println("<tr>");
        output.println("<td class=\"formParm1\">first name</td>");
		output.println("<td class=\"formParm2\"><input type=\"text\" name=\"firstName\" maxlength=\"120\" value=\"" + firstName + "\"></td>");
        output.println("</tr>");

        String lastName = null;
        
        if (errorMsg != null)
        {
            lastName = req.getParameter("lastName");
        }
        else
        {
            lastName = userMgr.getLastName(login);
        }
        
        if (lastName == null)
        {
            lastName = "";
        }
        
        output.println("<tr>");
        output.println("<td class=\"formParm1\">last name</td>");
		output.println("<td class=\"formParm2\"><input type=\"text\" name=\"lastName\" maxlength=\"120\" value=\"" + lastName + "\"></td>");
        output.println("</tr>");

        String val = null;

        if (errorMsg != null)
        {
            val = req.getParameter("email");
        }
        else
        {
            val = userMgr.getEmail(login);
        }
        
		if (val == null)
		{
			val = "";
		}
        output.println("<tr>");
        output.println("<td class=\"formParm1\"><b>e-mail address</b></td>");
		output.println("<td class=\"formParm2\"><input type=\"text\" name=\"email\" maxlength=\"255\" value=\"" + val + "\" style=\"width:300px\"></td>");
        output.println("</tr>");

        if (errorMsg != null)
        {
            val = req.getParameter("phone");
        }
        else
        {
            val = userMgr.getPhone(login);
        }
        
		if (val == null)
		{
			val = "";
		}
        output.println("<tr>");
        output.println("<td class=\"formParm1\">phone</td>");
		output.println("<td class=\"formParm2\"><input type=\"text\" name=\"phone\" maxlength=\"30\" value=\"" + val + "\"></td>");
        output.println("</tr>");

		long diskQuota = userMgr.getDiskQuota(login);
        
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
		output.print("<input type=\"checkbox\" name=\"checkDiskQuota\" class=\"cb3\"");
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
		output.println("<td class=\"formParm2\"><input type=\"text\" name=\"diskQuota\" maxlength=\"12\" value=\"" + val + "\"></td>");
        output.println("</tr>");

        output.println("<tr>");
        output.println("<td class=\"formParm1\"><b>role</b></td>");
		output.println("<td class=\"formParm2\"><select name=\"role\" size=\"1\">");

        String role = null;

        if (errorMsg != null)
        {
            role = getParameter("role");
        }
        else 
        {
            role = userMgr.getRole(login);
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
            userLanguage = userMgr.getLanguage(login);
        }
        
		if (userLanguage == null)
		{
			userLanguage = LanguageManager.DEFAULT_LANGUAGE;
		}

		Vector languages = LanguageManager.getInstance().getAvailableLanguages();

        output.println("<tr>");
        output.println("<td class=\"formParm1\"><b>language</b></td>");
		output.println("<td class=\"formParm2\"><select name=\"language\" size=\"1\">");

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
			userCss = userMgr.getCSS(login);
		}

		if (userCss == null)
		{
			userCss=CSSManager.DEFAULT_LAYOUT;
		}

		ArrayList<String> cssList = CSSManager.getInstance().getAvailableCss();

        output.println("<tr>");
        output.println("<td class=\"formParm1\"><b>layout (CSS file)</b></td>");
		output.println("<td class=\"formParm2\"><select name=\"css\" size=\"1\">");

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
		output.println("<input type=\"submit\" name=\"changebutton\" value=\"&nbsp;Save&nbsp;\">");
		output.println("</td><td class=\"formButton\" align=\"right\">");
		output.println("<input type=\"button\" value=\"&nbsp;Cancel&nbsp;\" onclick=\"javascript:window.location.href='/webfilesys/servlet?command=admin&cmd=userList'\">");
		output.println("</td></tr>");    

		output.println("</table>");

		output.println("</form>");

		output.println("</body></html>");
		output.flush();
	}

}
