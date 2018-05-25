package de.webfilesys.gui.user;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

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
            Logger.getLogger(getClass()).warn("missing activation code");
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
                Logger.getLogger(getClass()).warn("failed to redirect to blog handler", ex);
            }

            // TODO
        } catch (UserMgmtException ex) {
            Logger.getLogger(getClass()).warn("user activation attempt failed", ex);

            // the hotmail web client fetches the activation link automatically when opening the registration e-mail
            // the user's click on the activation link is the second one - and fails
            // what a bullshit!
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
                Logger.getLogger(getClass()).warn("redirect failed", ex2);
            }
            
        }
    }
}
