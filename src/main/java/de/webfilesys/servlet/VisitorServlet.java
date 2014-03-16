package de.webfilesys.servlet;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.WebFileSys;
import de.webfilesys.gui.xsl.XslPictureStoryHandler;
import de.webfilesys.gui.xsl.album.XslAlbumSlideShowHandler;
import de.webfilesys.gui.xsl.album.XslPictureAlbumHandler;
import de.webfilesys.mail.Email;
import de.webfilesys.user.UserManager;

/**
 * Visitor access to published picture albums.
 * The parameters are provided in the URI path (no URL parameters) to make it search engine friendly. 
 * 
 * request path syntax:
 * 
 * /webfilesys/visitor/<userid>/<password>/<viewType>
 * 
 * parameter viewType is optional, default is picture album
 */
public class VisitorServlet extends WebFileSysServlet {
	private static final long serialVersionUID = 1L;

	private static int REQUEST_PATH_LENGTH = "/webfilesys/visitor".length();
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, java.io.IOException {
        // prevent caching
		resp.setDateHeader("expires", 0l); 
		
		// done in XslRequestHandlerBase
		// resp.setContentType("text/xml");
		
		resp.setCharacterEncoding("UTF-8");

		String requestPath = req.getRequestURI();		
		
		if (requestPath.length() <= REQUEST_PATH_LENGTH + 1) {
        	Logger.getLogger(getClass()).error("missing parameters");
        	sendErrorPage(resp, "missing parameters");
        	return;
		}
	
		String params = requestPath.substring(REQUEST_PATH_LENGTH + 1);
		
		String visitorUserId = null;
		
		String password = null;
		
		String viewType = null;
		
		StringTokenizer paramParser = new StringTokenizer(params, "/");
		if (paramParser.hasMoreTokens()) {
			visitorUserId = paramParser.nextToken();
			if (paramParser.hasMoreTokens()) {
				password = paramParser.nextToken();
				if (paramParser.hasMoreTokens()) {
					viewType = paramParser.nextToken();
				}
			} else {
	        	Logger.getLogger(getClass()).error("missing parameter password");
	        	sendErrorPage(resp, "missing parameter");
	        	return;
			}
		} else {
        	Logger.getLogger(getClass()).error("missing parameter userid");
        	sendErrorPage(resp, "missing parameter");
        	return;
		}
		
    	UserManager userMgr = WebFileSys.getInstance().getUserMgr();

        String clientIP = req.getRemoteAddr();

        String logEntry = null;

        HttpSession session = null;
        
        // if (userMgr.checkReadonlyPassword(visitorUserId, password)) {
        if (userMgr.checkPassword(visitorUserId, password)) {
            session = req.getSession(true);

            session.setAttribute("userid", visitorUserId);
        
            session.setAttribute("loginEvent", "true");

            session.setAttribute("readonly", "true");

            session.setAttribute("cwd", userMgr.getDocumentRoot(visitorUserId));

            session.removeAttribute("startIdx");

            logEntry = clientIP + ": visitor login user " + visitorUserId;
            
            String userAgent = req.getHeader("User-Agent");
            
            if (userAgent != null) {
                logEntry = logEntry + " [" + userAgent + "]";
            }

            Logger.getLogger(getClass()).info(logEntry);

            if ((WebFileSys.getInstance().getMailHost() != null) && WebFileSys.getInstance().isMailNotifyLogin()) {
                (new Email(WebFileSys.getInstance().getUserMgr().getAdminUserEmails(),
                           "visitor login successful",
                           WebFileSys.getInstance().getLogDateFormat().format(new Date()) + " " + logEntry))
                           .send();
            }

            PrintWriter output = new PrintWriter(new OutputStreamWriter(resp.getOutputStream(), "UTF-8"));            
            
            if ((viewType != null) && viewType.equals("slideshow")) {
                (new XslAlbumSlideShowHandler(req, resp, session, output, visitorUserId)).handleRequest();
            } else if ((viewType != null) && viewType.equals("story")) {
            	req.setAttribute("mode", "pictureBook");
                (new XslPictureStoryHandler(req, resp, session, output, visitorUserId)).handleRequest();
            } else {
                (new XslPictureAlbumHandler(req, resp, session, output, visitorUserId)).handleRequest();
            }
        } else {
        	sendNotAuthorizedPage(resp);
        }
    }
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
		    throws ServletException, java.io.IOException {
    	
		doGet(req, resp);
	}

	private void sendNotAuthorizedPage(HttpServletResponse resp)
	throws IOException {
		resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
		
		PrintWriter out = resp.getWriter();
		out.println("authorization failed");
	    out.flush();
	}
	
	private void sendErrorPage(HttpServletResponse resp, String msg)
	throws IOException {
		resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
		
		PrintWriter out = resp.getWriter();
		out.println(msg);
	    out.flush();
	}
}


