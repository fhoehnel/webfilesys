package de.webfilesys.gui.user;

import java.io.PrintWriter;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.webfilesys.LanguageManager;
import de.webfilesys.gui.CSSManager;

/**
 * @author Frank Hoehnel
 */
public class SelfEditUserRequestHandler extends UserRequestHandler
{
	private String errorMsg = null;
	
	public SelfEditUserRequestHandler(
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
		if (!checkWriteAccess())
		{
			return;
		}

		String login=uid;

		output.println("<html>");
		output.println("<head>");

        output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/css/" + userMgr.getCSS(uid) + ".css\">");

        if (errorMsg != null)
		{
			javascriptAlert(errorMsg);
		}

		output.println("</head>");
		output.println("<body>");

		headLine(getResource("label.editregistration","edit user data"));

		output.println("<br/>");

		output.println("<form accept-charset=\"utf-8\" method=\"post\" action=\"/webfilesys/servlet\">");

		output.println("<input type=\"hidden\" name=\"command\" value=\"selfChangeUser\">");

        output.println("<table class=\"dataForm\" width=\"100%\">");

        output.println("<tr>");
        output.println("<td class=\"formParm1\"><b>" + getResource("label.login","userid/login") + "</b></td>");
        output.println("<td class=\"formParm2\">" + login + "</td>");
        output.println("</tr>");
        
        output.println("<tr>");
        output.println("<td class=\"formParm1\"><b>" + getResource("label.password","password") + "</b></td>");
		output.println("<td class=\"formParm2\"><input type=\"password\" name=\"password\" maxlength=\"30\" value=\"\"></td>");
        output.println("</tr>");

        output.println("<tr>");
        output.println("<td class=\"formParm1\"><b>" + getResource("label.passwordconfirm","password confirmation") + "</b></td>");
		output.println("<td class=\"formParm2\"><input type=\"password\" name=\"pwconfirm\" maxlength=\"30\" value=\"\"></td>");
        output.println("</tr>");

        output.println("<tr>");
        output.println("<td class=\"formParm1\">" + getResource("label.ropassword","read-only password") + "</td>");
		output.println("<td class=\"formParm2\"><input type=\"password\" name=\"ropassword\" maxlength=\"30\" VALUE=\"\"></td>");
        output.println("</tr>");

        output.println("<tr>");
        output.println("<td class=\"formParm1\">" + getResource("label.ropwconfirm","read-only password confirmation") + "</td>");
		output.println("<td class=\"formParm2\"><input type=\"password\" name=\"ropwconfirm\" maxlength=\"30\" value=\"\"></td>");
        output.println("</tr>");

        String val = null;
        if (errorMsg != null)
        {
            val = req.getParameter("firstName");
        }
        else
        {
            val = userMgr.getFirstName(login);
        }
        
        output.println("<tr>");
        output.println("<td class=\"formParm1\">" + getResource("label.firstname","first name") + "</td>");
		output.println("<td class=\"formParm2\"><input type=\"text\" name=\"firstName\" maxlength=\"120\" VALUE=\"" + val + "\"></td>");
        output.println("</tr>");

        if (errorMsg != null)
        {
            val = req.getParameter("lastName");
        }
        else
        {
            val = userMgr.getLastName(login);
        }

        output.println("<tr>");
        output.println("<td class=\"formParm1\">" + getResource("label.lastname","last name") + "</td>");
		output.println("<td class=\"formParm2\"><input type=\"text\" name=\"lastName\" maxlength=\"120\" value=\"" + val + "\"></td>");
        output.println("</tr>");
        
        if (errorMsg != null)
        {
            val = req.getParameter("email");
        }
        else
        {
            val = userMgr.getEmail(login);
            if (val == null)
            {
                val = "";
            }
        }
        
        output.println("<tr>");
        output.println("<td class=\"formParm1\"><b>" + getResource("label.email","e-mail address") + "</b></td>");
		output.println("<td class=\"formParm2\"><input type=\"text\" name=\"email\" maxlength=\"120\" value=\"" + val + "\"></td>");
        output.println("</tr>");

        if (errorMsg != null)
        {
            val = req.getParameter("phone");
        }
        else
        {
            val = userMgr.getPhone(login);
            if (val == null)
            {
                val = "";
            }
        }

        output.println("<tr>");
        output.println("<td class=\"formParm1\">" + getResource("label.phone","phone") + "</td>");
		output.println("<td class=\"formParm2\"><input type=\"text\" name=\"phone\" maxlength=\"30\" VALUE=\"" + val + "\"></td>");
        output.println("</tr>");

        String userLang = null;

        if (errorMsg != null) 
        {
            userLang = req.getParameter("language");
        }
        else
        {
            userLang = userMgr.getLanguage(login);

            if (userLang == null)
            {
                userLang = LanguageManager.DEFAULT_LANGUAGE;
            }
        }

		Vector languages = LanguageManager.getInstance().getAvailableLanguages();

        output.println("<tr>");
        output.println("<td class=\"formParm1\"><b>" + getResource("label.language","language") + "</b></td>");
		output.println("<td class=\"formParm2\"><select name=\"language\" size=\"1\">");

		for (int i = 0; i < languages.size(); i++)
		{
			String lang = (String) languages.elementAt(i);

			output.print("<option");

			if (lang.equals(userLang))
			{
				output.print(" selected=\"true\"");
			}

			output.println(">" + lang + "</option>");
		}
		output.println("</select></td>");
        output.println("</tr>");

        String userCss = null;

        if (errorMsg != null) 
        {
            userCss = req.getParameter("css");
        }
        else
        {
            userCss = userMgr.getCSS(login);

            if (userCss == null)
            {
                userCss = CSSManager.DEFAULT_LAYOUT;
            }
        }

		Vector cssList = CSSManager.getInstance().getAvailableCss();

        output.println("<tr>");
        output.println("<td class=\"formParm1\"><b>" + getResource("label.css","layout") + "</b></td>");
		output.println("<td class=\"formParm2\"><select name=\"css\" size=\"1\">");

		for (int i = 0; i < cssList.size(); i++)
		{
			String css = (String) cssList.elementAt(i);

			if (!css.equals("mobile"))
			{
	            output.print("<option");

	            if (css.equals(userCss))
	            {
	                output.print(" selected=\"true\"");
	            }

	            output.println(">" + css + "</option>");
			}
		}
		output.println("</select></td>");
        output.println("</tr>");
        
		output.println("<tr><td colspan=\"2\" class=\"formParm1\">&nbsp;</td></tr>");

        output.println("<tr>");
        output.println("<td class=\"formButton\">");
		output.println("<input type=\"submit\" name=\"changebutton\" value=\"" + getResource("button.save","&nbsp;Save&nbsp;") + "\">");
		output.println("</td><td class=\"formButton\" style=\"text-align:right\">");
		output.println("<input type=\"button\" value=\"" + getResource("button.cancel","&nbsp;Cancel&nbsp;") + "\" onclick=\"window.location.href='/webfilesys/servlet?command=listFiles';\">");
		output.println("</td>");
        output.println("</tr>");    

		output.println("</table>");

		output.println("</form>");

		output.println("</body>");
        output.println("</html>");
		output.flush();
	}

}
