package de.webfilesys.gui.user;

import java.io.PrintWriter;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.webfilesys.gui.CSSManager;
import de.webfilesys.gui.xsl.XslFileListHandler;

/**
 * @author Frank Hoehnel
 */
public class UserSettingsRequestHandler extends UserRequestHandler
{
	public UserSettingsRequestHandler(
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
		if (!checkWriteAccess())
		{
			return;
		}

        String password = getParameter("password");
        
        if (password == null)
        {
        	userForm(null);
        	
        	return;
        }
        
		String login=uid;

		StringBuffer errorMsg=new StringBuffer();

		String temp=null;

		String oldPassword=getParameter("oldpw");

		if ((oldPassword!=null) && (oldPassword.trim().length() > 0))
		{
			if (!userMgr.checkPassword(login,oldPassword))
			{
				temp=getResource("error.invalidpassword","the current password is invalid");
				errorMsg.append(temp + "\\n");
			}
		}

		String pwconfirm=getParameter("pwconfirm");

		if (((password!=null) && (password.trim().length() > 0)) ||
			((pwconfirm!=null) && (pwconfirm.trim().length() > 0)))
		{
			if ((oldPassword==null) || (oldPassword.trim().length()==0))
			{
				temp=getResource("error.invalidpassword","the current password is invalid");
				errorMsg.append(temp + "\\n");
			}

			if (password==null)
			{
				password="";
			}
			else
			{
				password=password.trim();
			}

			if (password.length() < 5)
			{
				temp=getResource("error.passwordlength","the minimum password length is 5 characters");
				errorMsg.append(temp + "\\n");
			}
			else
			{
				if (password.indexOf(' ')>0)
				{
					temp=getResource("error.spacesinpw","the password must not contain spaces");
					errorMsg.append(temp + "\\n");
				}
				else
				{
					if ((pwconfirm==null) || (!pwconfirm.equals(password)))
					{
						temp=getResource("error.pwmissmatch","the password and the password confirmation are not equal");
						errorMsg.append(temp + "\\n");
					}
				}
			}
		}

		String ropassword=getParameter("ropassword");
		String ropwconfirm=getParameter("ropwconfirm");

		if (((ropassword!=null) && (ropassword.trim().length() > 0)) ||
			((ropwconfirm!=null) && (ropwconfirm.trim().length() > 0)))
		{
			if ((oldPassword==null) || (oldPassword.trim().length()==0))
			{
				temp=getResource("error.invalidpassword","the current password is invalid");
				errorMsg.append(temp + "\\n");
			}

			if (ropassword==null)
			{
				ropassword="";
			}
			else
			{
				ropassword=ropassword.trim();
			}

			if (ropwconfirm==null)
			{
				ropwconfirm="";
			}
			else
			{
				ropwconfirm=ropwconfirm.trim();
			}

			if ((ropassword.length() > 0) || (ropwconfirm.length() > 0))
			{
				if (ropassword.length() < 5)
				{
					temp=getResource("error.passwordlength","the minimum password length is 5 characters");
					errorMsg.append(temp + "\\n");
				}
				else
				{
					if (ropassword.indexOf(' ') >= 0)
					{
						temp=getResource("error.spacesinpw","the password must not contain spaces");
						errorMsg.append(temp + "\\n");
					}
					else
					{
						if (!ropassword.equals(ropwconfirm))
						{
							temp=getResource("error.pwmissmatch","password and password confirmation do not match");
							errorMsg.append(temp + "\\n");
						}
					}
				}
			}
		}

		if (errorMsg.length()>0)
		{
			userForm(errorMsg.toString());

			return;
		}

		if (password.length() > 0)
		{
			userMgr.setPassword(login,password);
		}

		if (ropassword.length() > 0)
		{
			userMgr.setReadonlyPassword(login,ropassword);
		}

		String css=getParameter("css");

		if (css!=null)
		{
			userMgr.setCSS(login,css);
		}

		(new XslFileListHandler(req, resp, session, output, uid, true)).handleRequest();
	}
	
	protected void userForm(String errorMsg)
    {
		String login=uid;

		output.println("<HTML>");
		output.println("<HEAD>");

		output.println("<TITLE> WebFileSys : settings </TITLE>");

		if (errorMsg!=null)
		{
			javascriptAlert(errorMsg);
		}

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/css/" + userMgr.getCSS(uid) + ".css\">");

		output.println("</HEAD>");
		output.println("<BODY>");

		headLine(getResource("label.settings","Settings"));

		output.println("<br>");

		output.println("<form accept-charset=\"utf-8\" method=\"post\" action=\"/webfilesys/servlet\">");
		output.println("<input type=\"hidden\" name=\"command\" value=\"changePw\">");

		output.println("<table border=0>");
		output.println("<tr><td class=\"value\">" + login + "</td>");
		output.println("<td class=\"prompt\"><b>" + getResource("label.login","userid/login") + "</b></td></tr>");

		output.println("<tr><td><INPUT TYPE=\"PASSWORD\" NAME=\"oldpw\" SIZE=20 MAXLENGTH=30 VALUE=\"\"></td>");
		output.println("<td class=\"prompt\">" + getResource("label.oldpassword","current password") + "</td></tr>");


		output.println("<tr><td><INPUT TYPE=\"PASSWORD\" NAME=\"password\" SIZE=20 MAXLENGTH=30 VALUE=\"\"></td>");
		output.println("<td class=\"prompt\">" + getResource("label.newpassword","new password") + "</td></tr>");

		output.println("<tr><td><INPUT TYPE=\"PASSWORD\" NAME=\"pwconfirm\" SIZE=20 MAXLENGTH=30 VALUE=\"\"></td>");
		output.println("<td class=\"prompt\">" + getResource("label.passwordconfirm","password confirmation") + "</td></tr>");

		output.println("<tr><td><INPUT TYPE=\"PASSWORD\" NAME=\"ropassword\" SIZE=20 MAXLENGTH=30 VALUE=\"\"></td>");
		output.println("<td class=\"prompt\">" + getResource("label.ropassword","read-only password") + "</td></tr>");

		output.println("<tr><td><INPUT TYPE=\"PASSWORD\" NAME=\"ropwconfirm\" SIZE=20 MAXLENGTH=30 VALUE=\"\"></td>");
		output.println("<td class=\"prompt\">" + getResource("label.ropwconfirm","read-only password confirmation") + "</td></tr>");

        String userCss = null;
        
        if (errorMsg != null)
        {
        	userCss = getParameter("css");
        }
        else
        {
			userCss = userMgr.getCSS(login);

			if (userCss==null)
			{
				userCss=CSSManager.DEFAULT_LAYOUT;
			}
        }

		Vector cssList = CSSManager.getInstance().getAvailableCss();

		output.println("<tr><td><select name=\"css\" size=\"1\">");

		for (int i=0;i < cssList.size();i++)
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
		output.println("<td class=\"prompt\"><b>" + getResource("label.css","layout") + "</b></td></tr>");

		output.println("<tr><td colspan=2>&nbsp;</td></tr>");
		output.println("<tr><td align=left>");
		output.println("<input type=\"submit\" name=\"changebutton\" value=\"" + getResource("button.save","&nbsp;Save&nbsp;") + "\">");
		output.println("</td><td align=right>");
		output.println("<input type=\"button\" value=\"" + getResource("button.cancel","&nbsp;Cancel&nbsp;") + "\" onclick=\"window.location.href='/webfilesys/servlet?command=listFiles';\">");
		output.println("</td></tr>");    

		output.println("</table>");

		output.println("</form>");

		output.println("</body></html>");
		output.flush();
	}
}
