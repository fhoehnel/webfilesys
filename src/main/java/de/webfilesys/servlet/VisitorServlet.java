package de.webfilesys.servlet;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.WebFileSys;
import de.webfilesys.gui.blog.BlogListHandler;
import de.webfilesys.gui.xsl.XslPictureStoryHandler;
import de.webfilesys.gui.xsl.album.XslAlbumSlideShowHandler;
import de.webfilesys.gui.xsl.album.XslPictureAlbumHandler;
import de.webfilesys.mail.SmtpEmail;
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
	
	public static final String VISITOR_COOKIE_NAME = "webfilesys-visitor";
	
	public static final String VISITOR_COOKIE_PATH = "/webfilesys/visitor";
	
	public static final String SESSION_ATTRIB_VISITOR_ID = "visitorId";
	
	private static final int VISITOR_COOKIE_MAX_AGE = 120 * 24 * 60 * 60; // expires after 120 days
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, java.io.IOException {
        // prevent caching
		resp.setDateHeader("expires", 0l); 
		resp.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate, no-store");
		
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
            
		    setSessionInfo(req, session);

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
            	
            	ArrayList<String> adminUserEmailList = userMgr.getAdminUserEmails();
                
                (new SmtpEmail(adminUserEmailList,
                           "visitor login successful",
                           WebFileSys.getInstance().getLogDateFormat().format(new Date()) + " " + logEntry))
                           .send();
            }

            String visitorId = getVisitorIdFromCookie(req);
            
            if (visitorId == null) {
            	visitorId = UUID.randomUUID().toString();
                resp.addCookie(createVisitorCookie(visitorId));
            }
            
            req.getSession(true).setAttribute(SESSION_ATTRIB_VISITOR_ID, visitorId);
            
            PrintWriter output = new PrintWriter(new OutputStreamWriter(resp.getOutputStream(), "UTF-8"));            
            
            if (userMgr.getRole(visitorUserId).equals("blog")) {
    			(new BlogListHandler(req, resp, session, output, visitorUserId)).handleRequest(); 
            } else if ((viewType != null) && viewType.equals("slideshow")) {
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

	private String getVisitorIdFromCookie(HttpServletRequest req) {
		Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				if (cookies[i].getName().equals(VISITOR_COOKIE_NAME)) {
					return cookies[i].getValue();
				}
			}
		}
		return null;
	}
	
	private Cookie createVisitorCookie(String visitorId) {
		Cookie cookie = new Cookie(VISITOR_COOKIE_NAME, visitorId);
		cookie.setPath(VISITOR_COOKIE_PATH);
		cookie.setMaxAge(VISITOR_COOKIE_MAX_AGE);
		return cookie;
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


