package de.webfilesys.gui.admin;
import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


import de.webfilesys.WebFileSys;
import de.webfilesys.mail.EmailUtils;
import de.webfilesys.user.TransientUser;
import de.webfilesys.user.UserMgmtException;

/**
 * @author Frank Hoehnel
 */
public class AdminAddUserRequestHandler extends AdminRequestHandler
{
	public AdminAddUserRequestHandler(
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
		StringBuffer errorMsg=new StringBuffer();

		String login=getParameter("username");

		if ((login==null) || (login.trim().length()<3))
		{
			errorMsg.append("the minimum length of the login name is 3 characters\\n");
		}
		else
		{
			if (login.indexOf(' ')>0)
			{
				errorMsg.append("the login name must not contain spaces\\n");
			}
		}

		String password=getParameter("password");
		String pwconfirm=getParameter("pwconfirm");

		if ((password==null) || (password.trim().length()<5))
		{
			errorMsg.append("the minimum password length is 5 characters\\n");
		}
		else
		{
			if (password.indexOf(' ')>0)
			{
				errorMsg.append("the password must not contain spaces\\n");
			}
			else
			{
				if ((pwconfirm==null) || (!pwconfirm.equals(password)))
				{
					errorMsg.append("the password and the password confirmation are not equal\\n");
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
				errorMsg.append("the minimum password length is 5 characters\\n");
			}
			else
			{
				if (ropassword.indexOf(' ') >= 0)
				{
					errorMsg.append("the password must not contain spaces\\n");
				}
				else
				{
					if (!ropassword.equals(ropwconfirm))
					{
						errorMsg.append("the password and the password confirmation are not equal\\n");
					}
				}
			}
		}

		String documentRoot=getParameter("documentRoot");
		
        if (File.separatorChar == '\\')
        {
    		if (getParameter("allDrives") != null) {
    			documentRoot = "*:";
    		}
        }

		if ((documentRoot==null) || (documentRoot.trim().length()==0))
		{
			errorMsg.append("the document root is required\\n");
		}
		else
		{
			if ((documentRoot.charAt(0)!='*') || (File.separatorChar=='/'))
			{
				File docRootFile=new File(documentRoot);

				if ((!docRootFile.exists()) || (!docRootFile.isDirectory()))
				{
					errorMsg.append("the document root directory " + insertDoubleBackslash(documentRoot) + " does not exist\\n");
				}
			}
		}

		String email=getParameter("email");

		if ((email==null) || (!EmailUtils.emailSyntaxOk(email)))
		{
			errorMsg.append("a valid e-mail address is required\\n");
		}

		String userLanguage=getParameter("language");

        if ((userLanguage == null) || userLanguage.length() == 0)
        {
			errorMsg.append("please select a language\\n");
        }
		
		int diskQuotaMB=(-1);

		String checkDiskQuota=getParameter("checkDiskQuota");

		String diskQuotaString=getParameter("diskQuota");

		if (checkDiskQuota!=null)
		{
			try
			{
				diskQuotaMB=Integer.parseInt(diskQuotaString);
			}
			catch (NumberFormatException nfex)
			{
				errorMsg.append("the disk quota value is invalid\\n"); 
			}
		}
		else
		{
			if ((diskQuotaString!=null) && (diskQuotaString.trim().length()>0))
			{
				errorMsg.append("check the checkbox to set a disk quota");
			}
		}

		if (errorMsg.length()>0)
		{
			(new AdminRegisterUserRequestHandler(req, resp, session, output, uid, errorMsg.toString())).handleRequest(); 

			return;
		}

		if (userMgr.userExists(login))
		{
			errorMsg.append("user " + login + " already exists");

			(new AdminRegisterUserRequestHandler(req, resp, session, output, uid, errorMsg.toString())).handleRequest(); 

			return;
		}
		
		TransientUser newUser = new TransientUser();
		
		newUser.setUserid(login);
		newUser.setPassword(password);
		newUser.setReadonlyPassword(ropassword);
		newUser.setDocumentRoot(documentRoot);
		newUser.setEmail(email);
		newUser.setReadonly(getParameter("readonly") != null);
        newUser.setRole(getParameter("role"));
        newUser.setFirstName(getParameter("firstName"));
        newUser.setLastName(getParameter("lastName"));
        newUser.setPhone(getParameter("phone"));
        newUser.setCss(getParameter("css"));
        newUser.setLanguage(userLanguage);

		if (checkDiskQuota != null)
		{
			long diskQuota = ((long) diskQuotaMB) * (1024l) * (1024l); 
			newUser.setDiskQuota(diskQuota);
		}

		try {
			userMgr.createUser(newUser);
		} catch (UserMgmtException ex) {
        	LogManager.getLogger(getClass()).warn("failed to create new user " + newUser.getUserid(), ex);
			errorMsg.append("failed to create new user " + newUser.getUserid());
			(new AdminRegisterUserRequestHandler(req, resp, session, output, uid, errorMsg.toString())).handleRequest(); 
			return;
		}
		
		String sendWelcomeMail = getParameter("sendWelcomeMail");

		if ((WebFileSys.getInstance().getMailHost() != null) && (sendWelcomeMail != null))
		{
			EmailUtils.sendWelcomeMail(email, newUser.getFirstName(), newUser.getLastName(), login, password, null, userLanguage); 
		}

		(new UserListRequestHandler(req, resp, session, output, uid)).handleRequest(); 
	}

}
