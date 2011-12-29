package de.webfilesys.gui.user;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.webfilesys.gui.xsl.XslFileListHandler;
import de.webfilesys.mail.EmailUtils;

/**
 * @author Frank Hoehnel
 */
public class SelfChangeUserRequestHandler extends UserRequestHandler
{
	public SelfChangeUserRequestHandler(
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

		String login=uid;

		StringBuffer errorMsg=new StringBuffer();

		String temp=null;

		String password=getParameter("password");
		String pwconfirm=getParameter("pwconfirm");

		if ((password!=null) && (password.trim().length()>0) ||
			(pwconfirm!=null) && (pwconfirm.trim().length()>0))
		{
			if ((password==null) || (password.trim().length()<5))
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

		String email=getParameter("email");

		if ((email==null) || (!EmailUtils.emailSyntaxOk(email)))
		{
			temp=getResource("error.email","a valid e-mail address is required");
			errorMsg.append(temp + "\\n");
		}

		if (errorMsg.length()>0)
		{
			(new SelfEditUserRequestHandler(req, resp, session, output, uid, errorMsg.toString())).handleRequest();

			return;
		}

		if ((password!=null) && (password.trim().length()>0))
		{
			userMgr.setPassword(login,password);
		}

		if (ropassword.length() > 0)
		{
			userMgr.setReadonlyPassword(login,ropassword);
		}

		userMgr.setEmail(login,email);

		String firstName=getParameter("firstName");

		if (firstName!=null)
		{
			userMgr.setFirstName(login,firstName);
		}

		String lastName=getParameter("lastName");

		if (lastName!=null)
		{
			userMgr.setLastName(login,lastName);
		}

		String phone=getParameter("phone");

		if (phone!=null)
		{
			userMgr.setPhone(login,phone);
		}

		String userLanguage=getParameter("language");

		if (userLanguage!=null)
		{
			userMgr.setLanguage(login,userLanguage);
		}

		String css=getParameter("css");

		if (css!=null)
		{
			userMgr.setCSS(login,css);
		}

		(new XslFileListHandler(req, resp, session, output, uid, true)).handleRequest();
	}

}
