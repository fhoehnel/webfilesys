package de.webfilesys.gui.admin;
import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.mail.EmailUtils;
import de.webfilesys.user.TransientUser;
import de.webfilesys.user.UserMgmtException;
import de.webfilesys.util.CommonUtils;

/**
 * @author Frank Hoehnel
 */
public class AdminChangeUserRequestHandler extends AdminRequestHandler {
	public AdminChangeUserRequestHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid) {
        super(req, resp, session, output, uid);
	}
	
	protected void process() {
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
		
        if (File.separatorChar == '\\')
        {
    		if (getParameter("allDrives") != null) {
    			documentRoot = "*:";
    		}
        }
		

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

		TransientUser changedUser = userMgr.getUser(login);
		
		if (changedUser == null) {
            Logger.getLogger(getClass()).error("user for update not found: " + login);
			errorMsg.append("user for update not found: " + login);
			(new AdminEditUserRequestHandler(req, resp, session, output, uid, errorMsg.toString())).handleRequest(); 
			return;
		}
		
		if (!CommonUtils.isEmpty(password)) {
			changedUser.setPassword(password);
		}

		if (!CommonUtils.isEmpty(ropassword)) {
			changedUser.setReadonlyPassword(ropassword);
		}

		if (!CommonUtils.isEmpty(documentRoot)) {
			changedUser.setDocumentRoot(documentRoot);
		}

		changedUser.setReadonly(getParameter("readonly") != null);

		changedUser.setEmail(email);

		String role = getParameter("role");

		if (!CommonUtils.isEmpty(role)) {
			changedUser.setRole(role);
		}

		String firstName = getParameter("firstName");

		if (!CommonUtils.isEmpty(firstName)) {
			changedUser.setFirstName(firstName);
		}

		String lastName = getParameter("lastName");

		if (!CommonUtils.isEmpty(lastName)) {
			changedUser.setLastName(lastName);
		}

		String phone = getParameter("phone");

		if (!CommonUtils.isEmpty(phone)) {
			changedUser.setPhone(phone);
		}

		String css = getParameter("css");

		if (!CommonUtils.isEmpty(css)) {
			changedUser.setCss(css);
		}

		if (checkDiskQuota!=null) {
			long diskQuota=((long) diskQuotaMB) * (1024l) * (1024l); 
            changedUser.setDiskQuota(diskQuota);
		} else {
            changedUser.setDiskQuota(-1);
		}

		String userLanguage = getParameter("language");

		if (userLanguage != null) {
			changedUser.setLanguage(userLanguage);
		}
		
		try {
			userMgr.updateUser(changedUser);
		} catch (UserMgmtException ex) {
            Logger.getLogger(getClass()).error("failed to update user " + login, ex);
			errorMsg.append("failed to update user " + login);
			(new AdminEditUserRequestHandler(req, resp, session, output, uid, errorMsg.toString())).handleRequest(); 
			return;
		}

		(new UserListRequestHandler(req, resp, session, output, uid)).handleRequest(); 
	}

}
