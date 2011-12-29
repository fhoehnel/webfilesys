package de.webfilesys.gui.admin;
import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.webfilesys.mail.EmailUtils;

/**
 * @author Frank Hoehnel
 */
public class AdminChangeUserRequestHandler extends AdminRequestHandler
{
	public AdminChangeUserRequestHandler(
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
		StringBuffer errorMsg = new StringBuffer();

		String login=getParameter("username");

		String password=getParameter("password");
		String pwconfirm=getParameter("pwconfirm");

		if ((password!=null) && (password.trim().length()>0) ||
			(pwconfirm!=null) && (pwconfirm.trim().length()>0))
		{
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

		if ((documentRoot!=null) && (documentRoot.trim().length()>0))
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
			(new AdminEditUserRequestHandler(req, resp, session, output, uid, errorMsg.toString())).handleRequest(); 

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

		if ((documentRoot!=null) && (documentRoot.trim().length()>0))
		{
			userMgr.setDocumentRoot(login,documentRoot);
		}

		String readonly=getParameter("readonly");

		userMgr.setReadonly(login,(readonly!=null));

		userMgr.setEmail(login,email);

		String role=getParameter("role");

		if (role!=null)
		{
			userMgr.setRole(login,role);
		}

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

		String css=getParameter("css");

		if (css!=null)
		{
			userMgr.setCSS(login,css);
		}

		if (checkDiskQuota!=null)
		{
			long diskQuota=((long) diskQuotaMB) * (1024l) * (1024l); 

			userMgr.setDiskQuota(login,diskQuota);
		}
		else
		{
			userMgr.setDiskQuota(login,(-1));
		}

		String userLanguage=getParameter("language");

		if (userLanguage!=null)
		{
			userMgr.setLanguage(login,userLanguage);
		}

		(new UserListRequestHandler(req, resp, session, output, uid)).handleRequest(); 
	}

}
