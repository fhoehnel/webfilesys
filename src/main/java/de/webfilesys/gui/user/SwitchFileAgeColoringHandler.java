package de.webfilesys.gui.user;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.webfilesys.gui.xsl.XslFileListHandler;

/**
 * @author Frank Hoehnel
 */
public class SwitchFileAgeColoringHandler extends UserRequestHandler {

	public static final String SESSION_KEY_FILE_AGE_COLORING = "fileAgeColoring";
	
	protected HttpServletRequest req = null;

	protected HttpServletResponse resp = null;
	
	public SwitchFileAgeColoringHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid) {
        super(req, resp, session, output, uid);

        this.req = req;
        this.resp = resp;
	}

	protected void process() {
		
		Boolean fileAgeColoringActive = (Boolean) session.getAttribute(SESSION_KEY_FILE_AGE_COLORING);
		
		if (fileAgeColoringActive != null) {
			session.removeAttribute(SESSION_KEY_FILE_AGE_COLORING);
		} else {
			session.setAttribute(SESSION_KEY_FILE_AGE_COLORING, Boolean.TRUE);
		}
		
        (new XslFileListHandler(req, resp, session, output, uid, true)).handleRequest();
	}
}
