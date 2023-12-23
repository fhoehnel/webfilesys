package de.webfilesys.gui.user;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


import de.webfilesys.WebFileSys;
import de.webfilesys.user.UserManager;
import de.webfilesys.user.UserMgmtException;
import de.webfilesys.util.CommonUtils;

/**
 * @author Frank Hoehnel
 */
public class ActivateUserRequestHandler extends UserRequestHandler {
    protected HttpServletRequest req = null;

    protected HttpServletResponse resp = null;

    public ActivateUserRequestHandler(HttpServletRequest req, HttpServletResponse resp) {
        super(req, resp, null, null, null);

        this.req = req;
        this.resp = resp;
    }

    protected void process() {
        String activationCode = getParameter("code");

        if (CommonUtils.isEmpty(activationCode)) {
            LogManager.getLogger(getClass()).warn("missing activation code");
            return;
        }
        
        String userAgent = req.getHeader("user-agent");
        if ((userAgent != null) && userAgent.contains("BingPreview")) {
            // the hotmail/outlook web client fetches the activation link automatically when opening the registration e-mail
            // the user's click on the activation link is the second one - and fails
            // what a bullshit!
        	return;
        }
        
        UserManager userMgr = WebFileSys.getInstance().getUserMgr();

        try {
            userMgr.activateUser(activationCode);

            HttpSession session = req.getSession();
            if (session != null) {
                session.invalidate();
            }

            try {
                resp.sendRedirect(req.getContextPath() + "/servlet?command=loginForm&activationSuccess=true");
            } catch (IOException ex) {
                LogManager.getLogger(getClass()).warn("failed to redirect to blog handler", ex);
            }

            // TODO
        } catch (UserMgmtException ex) {
            LogManager.getLogger(getClass()).warn("user activation attempt failed", ex);

            /*
            try {
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            } catch (Exception ioex) {
            }
            */

            String redirectUrl = req.getContextPath() + "/servlet?command=loginForm";

            try {
                resp.sendRedirect(redirectUrl);
            } catch (IOException ex2) {
                LogManager.getLogger(getClass()).warn("redirect failed", ex2);
            }
            
        }
    }
}
