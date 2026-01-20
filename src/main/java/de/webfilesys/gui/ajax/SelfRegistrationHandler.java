package de.webfilesys.gui.ajax;

import de.webfilesys.WebFileSys;
import de.webfilesys.WebFileSysConfig;
import de.webfilesys.mail.EmailUtils;
import de.webfilesys.mail.SmtpEmail;
import de.webfilesys.user.TransientUser;
import de.webfilesys.user.UserManager;
import de.webfilesys.user.UserMgmtException;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Element;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;

public class SelfRegistrationHandler extends XmlRequestHandlerBase {

    private static final int MIN_USER_NAME_LENGTH = 3;
    private static final int MAX_USER_NAME_LENGTH = 64;
    private static final int MIN_PASSWORD_LENGTH = 5;
    private static final int MAX_PASSWORD_LENGTH = 64;

	public SelfRegistrationHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid) {
        super(req, resp, session, output, uid);
	}
	
	protected void process() {

        try {
            if (!WebFileSysConfig.getInstance().isOpenRegistration()) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            String userName = getParameter("username");
            if (!validateUserName(userName)) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            if (userMgr.userExists(userName)) {
                LogManager.getLogger(getClass()).warn("user with this name already exists: {}", userName);
                resp.sendError(HttpServletResponse.SC_CONFLICT);
                return;
            }
            String password = getParameter("password");
            String pwconfirm = getParameter("pwconfirm");
            if (!validatePasswords(password, pwconfirm, false)) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            String ropassword = getParameter("ropassword");
            String ropwconfirm = getParameter("ropwconfirm");
            if (!validatePasswords(ropassword, ropwconfirm, true)) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            String email = getParameter("email");
            if (email == null || !EmailUtils.emailSyntaxOk(email)) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            String userLanguage = getParameter("language");
            if (userLanguage == null || userLanguage.length() == 0) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            String userRole = getParameter("userRole");
            if (userRole == null || !userRole.equals("webspace")) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            String docRoot = WebFileSys.getInstance().getUserDocRoot() + File.separator + userName;
            File docRootFile = new File(docRoot);
            if (docRootFile.exists() || !docRootFile.mkdir()) {
                LogManager.getLogger(getClass()).error("cannot create home directory for new user {}: {}", userName, docRoot);
                resp.sendError(HttpServletResponse.SC_CONFLICT);
                return;
            }
            TransientUser newUser = new TransientUser();
            newUser.setUserid(userName);
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
            newUser.setDiskQuota(WebFileSysConfig.getInstance().getDefaultDiskQuota());
            newUser.setActivationCode(CommonUtils.generateAccessCode());
            newUser.setActivationCodeExpiration(System.currentTimeMillis() + UserManager.ACTIVATION_CODE_EXPIRATION);

            try {
                userMgr.createUser(newUser);
            } catch (UserMgmtException ex) {
                LogManager.getLogger(getClass()).warn("failed to create new user " + userName, ex);
                resp.sendError(HttpServletResponse.SC_CONFLICT);
                return;
            }
            if (LogManager.getLogger(getClass()).isInfoEnabled()) {
                LogManager.getLogger(getClass()).info(req.getRemoteAddr() + ": new user " + userName + " registered (not activated)");
            }
            if (WebFileSysConfig.getInstance().getMailHost() != null) {
                if (WebFileSysConfig.getInstance().isMailNotifyRegister()) {
                    ArrayList<String> adminUserEmailList = userMgr.getAdminUserEmails();
                    (new SmtpEmail(adminUserEmailList, "new user self-registration",
                            WebFileSys.getInstance().getLogDateFormat().format(new Date()) + " " + req.getRemoteAddr() + ": new user " + userName + " registered")).send();
                }
                sendActivationLink(newUser);
            }
        } catch (IOException ex) {
            return;
        }

        Element resultElement = doc.createElement("result");
    	XmlUtil.setChildText(resultElement, "success", "true");
        if (WebFileSysConfig.getInstance().getMailHost() == null) {
            XmlUtil.setChildText(resultElement, "activationByAdminRequired", "true", false);
        }
		doc.appendChild(resultElement);
		processResponse();
	}

    private void sendActivationLink(TransientUser newUser) {
        StringBuilder activationLink = new StringBuilder();
        if (req.getScheme().toLowerCase().startsWith("https")) {
            activationLink.append("https://");
        } else {
            activationLink.append("http://");
        }
        if (WebFileSysConfig.getInstance().getServerDNS() != null) {
            activationLink.append(WebFileSysConfig.getInstance().getServerDNS());
        } else {
            activationLink.append(WebFileSys.getInstance().getLocalIPAddress());
        }
        activationLink.append(":");
        activationLink.append(req.getServerPort());
        activationLink.append(req.getContextPath());
        activationLink.append("/servlet?command=activateUser&code=");
        activationLink.append(newUser.getActivationCode());
        EmailUtils.sendWelcomeMail(newUser.getEmail(), newUser.getFirstName(), newUser.getLastName(),
                newUser.getUserid(), null, activationLink.toString(), newUser.getLanguage());
    }

    private boolean validateUserName(String login) {
        if (login.trim().length() < MIN_USER_NAME_LENGTH || login.trim().length() > MAX_USER_NAME_LENGTH) {
            return false;
        }
        if (login.indexOf(' ') > 0) {
            return false;
        }
        for (int i = 0; i < login.length(); i++) {
            char c = login.charAt(i);
            if (!Character.isAlphabetic(c) && !Character.isDigit(c) && c != '-' && c != '_' && c != '.' && c != '@') {
                return false;
            }
        }
        return true;
    }

    private boolean validatePasswords(String password, String pwconfirm, boolean emptyAllowed) {
        if (emptyAllowed) {
            if (CommonUtils.isEmpty(password)) {
                return CommonUtils.isEmpty(pwconfirm);
            }
        } else {
            if (CommonUtils.isEmpty(password)) {
                return false;
            }
        }
        if (password.trim().length() < MIN_PASSWORD_LENGTH || password.trim().length() > MAX_PASSWORD_LENGTH) {
            return false;
        }
        if (password.indexOf(' ') > 0) {
            return false;
        }
        if (pwconfirm == null || !pwconfirm.equals(password)) {
            return false;
        }
        return true;
    }
}
