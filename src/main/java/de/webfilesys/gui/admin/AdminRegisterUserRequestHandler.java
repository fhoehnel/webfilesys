package de.webfilesys.gui.admin;
import java.io.File;
import java.io.PrintWriter;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.webfilesys.LanguageManager;
import de.webfilesys.WebFileSys;
import de.webfilesys.gui.CSSManager;

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

		if (errorMsg!=null)
		{
			output.println("<script language=\"javascript\">");
			output.println("alert('" + errorMsg + "');");
			output.println("</script>");
		}

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/css/" + userMgr.getCSS(uid) + ".css\">");

		output.println("<script language=\"javascript\">");
		output.println("function selectDocRoot()");
		output.println("{docRootWin=open('/webfilesys/servlet?command=admin&cmd=selectDocRoot','docRootWin','status=no,toolbar=no,menu=no,width=550,height=500,resizable=yes,scrollbars=yes,left=100,top=50,screenX=100,screenY=50');docRootWin.focus();}");
		output.println("</script>");

		output.println("</head>");
		output.println("<body>");

		headLine("WebFileSys Administration: Add new User");

		output.println("<br>");

		output.println("<form accept-charset=\"utf-8\" method=\"post\" action=\"/webfilesys/servlet\">");

		output.println("<input type=\"hidden\" name=\"command\" value=\"admin\">");
		output.println("<input type=\"hidden\" name=\"cmd\" value=\"addUser\">");

        output.println("<table class=\"dataForm\" width=\"100%\">");

		String val = "";
		if (errorMsg != null)
		{
			val = getParameter("username");
		}

        output.println("<tr>");
        output.println("<td class=\"formParm1\"><b>userid / login (no spaces!)</b></td>");
		output.println("<td class=\"formParm2\"><input type=\"text\" name=\"username\" maxlength=\"30\" value=\"" + val + "\"></td>");
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
		output.println("<td class=\"formParm2\"><input type=\"password\" name=\"ropassword\" maxlength=\"30\" value=\"\"></td>");
        output.println("</tr>");

        output.println("<tr>");
        output.println("<td class=\"formParm1\">read-only password confirmation</td>");
		output.println("<td class=\"formParm2\"><input type=\"password\" name=\"ropwconfirm\" maxlength=\"30\" value=\"\"></td>");
        output.println("</tr>");

		val = "";
		if (errorMsg != null)
		{
			val = getParameter("documentRoot");
		}

        output.println("<tr>");
        output.print("<td class=\"formParm1\"><b>document root");
        if (File.separatorChar=='\\')
        {
            output.println(" (&quot; *: &quot for all drives)");
        }
        output.println("</b></td>");
		output.println("<td class=\"formParm2\"><input type=\"text\" name=\"documentRoot\" maxlength=\"255\" value=\"" + val + "\" style=\"width:300px\">");
		output.print("&nbsp;");
		output.println("<input type=\"button\" value=\" ... \" onclick=\"javascript:selectDocRoot()\"></td>");
        output.println("</tr>");

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
		output.println("<input type=\"checkbox\" name=\"readonly\"" + val + " class=\"cb3\">");
		output.println("</td>");
        output.println("</tr>");

		val = "";
		if (errorMsg != null)
		{
			val = getParameter("firstName");
		}

        output.println("<tr>");
        output.println("<td class=\"formParm1\">first name</td>");
		output.println("<td class=\"formParm2\"><input type=\"text\" name=\"firstName\" maxlength=\"120\" value=\"" + val + "\"></td>");
        output.println("</tr>");

		val = "";
		if (errorMsg != null)
		{
			val = getParameter("lastName");
		}

        output.println("<tr>");
        output.println("<td class=\"formParm1\">last name</td>");
		output.println("<td class=\"formParm2\"><input type=\"text\" name=\"lastName\" maxlength=\"120\" value=\"" + val + "\"></td>");
        output.println("</tr>");

		val = "";
		if (errorMsg != null)
		{
			val = getParameter("email");
		}

        output.println("<tr>");
        output.println("<td class=\"formParm1\"><b>e-mail address</b></td>");
		output.println("<td class=\"formParm2\"><input type=\"text\" name=\"email\" maxlength=\"120\" value=\"" + val + "\" style=\"width:300px\"></td>");
        output.println("</tr>");

		val = "";
		if (errorMsg != null)
		{
			val = getParameter("phone");
		}

        output.println("<tr>");
        output.println("<td class=\"formParm1\">phone</td>");
		output.println("<td class=\"formParm2\"><input type=\"text\" name=\"phone\" maxlength=\"30\" value=\"" + val + "\"></td>");
        output.println("</tr>");

		val = "";
		if (errorMsg != null)
		{
			if (getParameter("checkDiskQuota")!=null)
			{
				val = " checked";
			}
		}

        output.println("<tr>");
        output.println("<td class=\"formParm1\">");
        output.println("check disk quota");
        output.println("</td>");
		output.println("<td class=\"formParm2\">");
		output.println("<input type=\"checkbox\" name=\"checkDiskQuota\"" + val + " class=\"cb3\">");
		output.println("</td>");
        output.println("</tr>");

		val = "";
		if (errorMsg != null)
		{
			val = getParameter("diskQuota");
		}

        output.println("<tr>");
        output.println("<td class=\"formParm1\">disk quota (MBytes)</td>");
		output.println("<td class=\"formParm2\"><input type=\"text\" name=\"diskQuota\" maxlength=\"12\" value=\"" + val + "\"></td>");
        output.println("</tr>");

        output.println("<tr>");
        output.println("<td class=\"formParm1\"><b>role</b></td>");
		output.println("<td class=\"formParm2\"><select name=\"role\" size=\"1\">");

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

		if ((errorMsg != null) && getParameter("role").equals("webspace"))
		{
			output.println("<option selected>webspace</option>");
		}
		else
		{
			output.println("<option>webspace</option>");
		}
		output.println("</select></td>");
        output.println("</tr>");

		Vector languages = LanguageManager.getInstance().getAvailableLanguages();

        output.println("<tr>");
        output.println("<td class=\"formParm1\"><b>language</b></td>");
		output.println("<td class=\"formParm2\"><select name=\"language\" size=\"1\">");

		output.println("<option selected=\"selected\">" + LanguageManager.DEFAULT_LANGUAGE + "</option>");

		for (int i = 0; i < languages.size(); i++)
		{
			if ((errorMsg != null) && getParameter("language").equals(languages.elementAt(i)))
			{
				output.println("<option selected>" + languages.elementAt(i) + "</option>");
			}
			else
			{
				output.println("<option>" + languages.elementAt(i) + "</option>");
			}
		}
		output.println("</select></td>");
        output.println("</tr>");

		Vector cssList=CSSManager.getInstance().getAvailableCss();

        output.println("<tr>");
        output.println("<td class=\"formParm1\"><b>layout (CSS file)</b></td>");
		output.println("<td class=\"formParm2\"><select name=\"css\" size=\"1\">");

		for (int i = 0; i < cssList.size(); i++)
		{
			if (((errorMsg != null) && getParameter("css").equals(cssList.elementAt(i))) ||
				((errorMsg == null) && cssList.elementAt(i).equals(CSSManager.DEFAULT_LAYOUT)))
			{
				output.println("<option selected=\"selected\">" + cssList.elementAt(i) + "</option>");
			}
			else
			{
				output.println("<option>" + cssList.elementAt(i) + "</option>");
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
            output.println("<input type=\"checkbox\" name=\"sendWelcomeMail\"" + val + " class=\"cb3\">");
			output.println("</td>");
            output.println("</tr>");
		}

		output.println("<tr><td colspan=\"2\">&nbsp;</td></tr>");

		output.println("<tr><td class=\"formButton\">");
		output.println("<input type=\"submit\" name=\"addbutton\" value=\"Add new user\">");
		output.println("</td><td class=\"formButton\" align=\"right\">");
		output.println("<input type=\"button\" value=\"Cancel\" onclick=\"javascript:window.location.href='/webfilesys/servlet?command=admin&cmd=userList'\">");
		output.println("</td></tr>");    

		output.println("</table>");

		output.println("</form>");

		output.println("</body></html>");
		output.flush();
	}

}
