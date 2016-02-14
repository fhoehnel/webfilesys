package de.webfilesys.gui.xsl;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.LanguageManager;
import de.webfilesys.WebFileSys;
import de.webfilesys.gui.CSSManager;
import de.webfilesys.mail.EmailUtils;
import de.webfilesys.mail.SmtpEmail;
import de.webfilesys.user.TransientUser;
import de.webfilesys.user.UserManager;
import de.webfilesys.user.UserMgmtException;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslSelfRegistrationHandler extends XslRequestHandlerBase
{
	public XslSelfRegistrationHandler(HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
    		PrintWriter output)
	{
		super(req, resp, session, output, null);
		
		if (!WebFileSys.getInstance().isOpenRegistration())
		{
			return;
		}
		
		String login = getParameter("username");

        if (login == null)
        {
        	selfRegistrationForm(req, session);
        	
        	return;
        }

		LanguageManager langMgr=LanguageManager.getInstance();

        String primaryLanguage = WebFileSys.getInstance().getPrimaryLanguage();
        
		if (login.trim().length() < 3)
		{
			this.addValidationError("username", langMgr.getResource(primaryLanguage, "error.missinglogin", "the minimum length of the login name is 3 characters"));
		}
		else if (login.indexOf(' ')>0)
		{
			this.addValidationError("username", langMgr.getResource(primaryLanguage, "error.spacesinlogin", "the login name must not contain spaces"));
		}
		
		String password=getParameter("password");
		String pwconfirm=getParameter("pwconfirm");

		if ((password == null) || (password.trim().length() < 5))
		{
			this.addValidationError("password", langMgr.getResource(primaryLanguage, "error.passwordlength", "the minimum password length is 5 characters"));
		}		
		else if (password.indexOf(' ')>0)
		{
			this.addValidationError("password", langMgr.getResource(primaryLanguage, "error.spacesinpw","the password must not contain spaces"));
		}
		else if ((pwconfirm == null) || (!pwconfirm.equals(password)))
		{
			this.addValidationError("pwconfirm", langMgr.getResource(primaryLanguage, "error.pwmissmatch","password and password confirmation are not equal"));
		}
		
		String ropassword = getParameter("ropassword");
		String ropwconfirm = getParameter("ropwconfirm");

		if (ropassword == null)
		{
			ropassword = "";
		}
		else
		{
			ropassword = ropassword.trim();
		}

		if (ropwconfirm == null)
		{
			ropwconfirm = "";
		}
		else
		{
			ropwconfirm = ropwconfirm.trim();
		}

		if ((ropassword.length() > 0) || (ropwconfirm.length() > 0))
		{
			if (ropassword.length() < 5)
			{
				this.addValidationError("ropassword", langMgr.getResource(primaryLanguage, "error.passwordlength", "the minimum password length is 5 characters"));
			}
			else if (ropassword.indexOf(' ') >= 0)
			{
				this.addValidationError("ropassword", langMgr.getResource(primaryLanguage, "error.spacesinpw", "the password must not contain spaces"));
			}
			else if (!ropassword.equals(ropwconfirm))
			{
				this.addValidationError("ropwconfirm", langMgr.getResource(primaryLanguage, "error.pwmissmatch","password and password confirmation do not match"));
			}
		}
		
		String email = getParameter("email");

		if ((email == null) || (!EmailUtils.emailSyntaxOk(email)))
		{
			this.addValidationError("email", langMgr.getResource(primaryLanguage, "error.email","a valid e-mail address is required"));
		}
		
		String userLanguage = getParameter("language");

        if ((userLanguage == null) || (userLanguage.length() == 0))
        {
			this.addValidationError("language", langMgr.getResource(primaryLanguage, "error.missingLanguage","language is required"));
        }
		
        String userRole = getParameter("userRole");

        if ((userRole == null) || ((!userRole.equals("webspace")) && (!userRole.equals("blog")))) {
			Logger.getLogger(getClass()).error("invalid user role: " + userRole);
			return;
        }
        
		if (validationElement != null)
		{
			selfRegistrationForm(req, session);
			
			return;
		}
		
		UserManager userMgr = WebFileSys.getInstance().getUserMgr();

		if (userMgr.userExists(login)) {
			addValidationError("username", langMgr.getResource(primaryLanguage, "error.duplicatelogin", "an user with this name already exists"));
			
			selfRegistrationForm(req, session);

			return;
		}

		// user input validated - no errors
		
		String docRoot = WebFileSys.getInstance().getUserDocRoot() + File.separator + login;

		File docRootFile = new File(docRoot);

		if (!docRootFile.exists())
		{
			if (!docRootFile.mkdir())
			{
				Logger.getLogger(getClass()).error("cannot create home directory for new user " + login + ": " + docRoot);
			}
		}
		
		TransientUser newUser = new TransientUser();
		
		newUser.setUserid(login);
		newUser.setPassword(password);
		newUser.setReadonlyPassword(ropassword);
		newUser.setDocumentRoot(docRoot);
		newUser.setEmail(email);
		newUser.setReadonly(false);
        newUser.setRole(userRole);
        newUser.setFirstName(getParameter("firstName"));
        newUser.setLastName(getParameter("lastName"));
        newUser.setPhone(getParameter("phone"));
        newUser.setCss(getParameter("css"));
        newUser.setLanguage(userLanguage);

		newUser.setDiskQuota(WebFileSys.getInstance().getDefaultDiskQuota());

		try {
			userMgr.createUser(newUser);
		} catch (UserMgmtException ex) {
        	Logger.getLogger(getClass()).warn("failed to create new user " + login, ex);
			addValidationError("username", "failed to create new user");
			selfRegistrationForm(req, session);
			return;
		}

		Logger.getLogger(getClass()).info(req.getRemoteAddr() + ": new user " + login + " registered");

		if ((WebFileSys.getInstance().getMailHost() != null) && WebFileSys.getInstance().isMailNotifyRegister())
		{
			ArrayList<String> adminUserEmailList = userMgr.getAdminUserEmails();
            
			(new SmtpEmail(adminUserEmailList, "new user self-registration",
					   WebFileSys.getInstance().getLogDateFormat().format(new Date()) + " " + req.getRemoteAddr() + ": new user " + login + " registered")).send();
		}

		if ((WebFileSys.getInstance().getMailHost() != null) && WebFileSys.getInstance().isMailNotifyWelcome())
		{
			EmailUtils.sendWelcomeMail(email, newUser.getFirstName(), newUser.getLastName(), login, password, userLanguage); 
		}

		if (session != null)
		{
			session.removeAttribute("userid");

	    	session.invalidate();
		}
        
		output.println("<HTML>");
		output.println("<HEAD>");

		javascriptAlert(langMgr.getResource(userLanguage, "alert.regsuccess", "New user has been registered successfully.")); 

		output.println("<META HTTP-EQUIV=\"REFRESH\" CONTENT=\"0; URL=/webfilesys/servlet\">");

		output.println("</HEAD></HTML>");
		output.flush();
	}
	  
	protected void selfRegistrationForm(HttpServletRequest req, HttpSession session)
	{ 
        // in case a user is still logged on in this session, the session must be removed
		if (session != null)
		{
			session.removeAttribute("userid");

	    	session.invalidate();
		}

		Element rootElement = doc.createElement("registration");
			
		doc.appendChild(rootElement);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/registerUser.xsl\"");

		doc.insertBefore(xslRef, rootElement);

		if (validationElement != null)
		{
			// validation errors occured
			rootElement.appendChild(validationElement);
		}
		
		XmlUtil.setChildText(rootElement, "css", CSSManager.DEFAULT_LAYOUT, false);

		String selectedLanguage = req.getParameter("language");
		
		LanguageManager langMgr = LanguageManager.getInstance();

		Element languagesElement = doc.createElement("languages");
		
		rootElement.appendChild(languagesElement);
		
		Vector languageList = langMgr.getAvailableLanguages();
		
		for (int i = 0; i < languageList.size(); i++ )
		{
			String languageName = (String) languageList.elementAt(i);
			
			Element languageElement = doc.createElement("language");
			
			XmlUtil.setElementText(languageElement, languageName);
			
			if (selectedLanguage != null)
		    {
				if (selectedLanguage.equals(languageName))
  				{
					languageElement.setAttribute("selected", "true");
				}
		    }
			else
			{
				if (languageName.equals(LanguageManager.DEFAULT_LANGUAGE))
				{
					languageElement.setAttribute("selected", "true");
				}
			}
			
			languagesElement.appendChild(languageElement);
		}

		String selectedCss = req.getParameter("css");
		
		Element layoutsElement = doc.createElement("layouts");
		
		rootElement.appendChild(layoutsElement);
		
		ArrayList<String> cssList = CSSManager.getInstance().getAvailableCss();

		for (int i = 0; i < cssList.size(); i++)
		{
			String css = (String) cssList.get(i);
			
            if (!css.equals("mobile")) 
            {
                Element layoutElement = doc.createElement("layout");
                
                XmlUtil.setElementText(layoutElement, css);
                
                if (selectedCss != null)
                {
                    if (selectedCss.equals(css))
                    {
                        layoutElement.setAttribute("selected", "true");
                    }
                }
                else
                {
                    if (css.equals(CSSManager.DEFAULT_LAYOUT))
                    {
                        layoutElement.setAttribute("selected", "true");
                    }
                }
                
                layoutsElement.appendChild(layoutElement);
            }			
		}
		
		addRequestParameter("username");
		addRequestParameter("password");
		addRequestParameter("pwconfirm");
		addRequestParameter("ropassword");
		addRequestParameter("ropwconfirm");
		addRequestParameter("firstName");
		addRequestParameter("lastName");
		addRequestParameter("email");
		addRequestParameter("phone");
		
		String primaryLanguage = WebFileSys.getInstance().getPrimaryLanguage();		
		
		addMsgResource("label.regtitle", 
		               langMgr.getResource(primaryLanguage,
                                           "label.regtitle",
                                           "new user registration"));
		addMsgResource("label.login", 
					   langMgr.getResource(primaryLanguage,
										   "label.login",
										   "userid/login"));

		addMsgResource("label.password", 
					   langMgr.getResource(primaryLanguage,
										   "label.password",
										   "password"));

		addMsgResource("label.passwordconfirm", 
					   langMgr.getResource(primaryLanguage,
										   "label.passwordconfirm",
										   "confirm password"));
		
		addMsgResource("label.ropassword", 
				       langMgr.getResource(primaryLanguage,
									   "label.ropassword",
									   "read-only password"));

		addMsgResource("label.ropwconfirm", 
				   langMgr.getResource(primaryLanguage,
									   "label.ropwconfirm",
									   "confirm read-only password"));

		addMsgResource("label.firstname", 
				   langMgr.getResource(primaryLanguage,
									   "label.firstname",
									   "first name"));

		addMsgResource("label.lastname", 
				   langMgr.getResource(primaryLanguage,
									   "label.lastname",
									   "last name"));
		
		addMsgResource("label.email", 
				   langMgr.getResource(primaryLanguage,
									   "label.email",
									   "e-mail address"));
		
		addMsgResource("label.phone", 
				   langMgr.getResource(primaryLanguage,
									   "label.phone",
									   "phone"));

		addMsgResource("label.language", 
				   langMgr.getResource(primaryLanguage,
									   "label.language",
									   "language"));

		addMsgResource("label.css", 
				   langMgr.getResource(primaryLanguage,
									   "label.css",
									   "layout"));

		addMsgResource("button.register", 
				   langMgr.getResource(primaryLanguage,
									   "button.register",
									   "Register"));

		addMsgResource("button.cancel", 
				   langMgr.getResource(primaryLanguage,
									   "button.cancel",
									   "Cancel"));

		addMsgResource("label.selectLanguage", 
				   langMgr.getResource(primaryLanguage,
									   "label.selectLanguage",
									   "- select language -"));
		
		addMsgResource("label.userRole", 
				   langMgr.getResource(primaryLanguage,
									   "label.userRole",
									   "user role"));
		
		
		processResponse("registerUser.xsl", true);
    }
}