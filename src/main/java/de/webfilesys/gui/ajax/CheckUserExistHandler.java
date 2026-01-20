package de.webfilesys.gui.ajax;

import de.webfilesys.WebFileSys;
import de.webfilesys.WebFileSysConfig;
import de.webfilesys.user.UserManager;
import de.webfilesys.util.XmlUtil;
import org.w3c.dom.Element;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Frank Hoehnel
 */
public class CheckUserExistHandler extends XmlRequestHandlerBase {
	public CheckUserExistHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid) {
        super(req, resp, session, output, uid);
	}
	
	protected void process() {
        if (!WebFileSysConfig.getInstance().isOpenRegistration()) {
            try {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            } catch (IOException ex) {
            }
            return;
        }
        String userName = getParameter("userName");
        if (userName == null) {
            return;
        }
        UserManager userMgr = WebFileSys.getInstance().getUserMgr();
        Element resultElement = doc.createElement("result");
        XmlUtil.setElementText(resultElement, Boolean.toString(userMgr.userExists(userName)));
        doc.appendChild(resultElement);
		processResponse();
	}
}
