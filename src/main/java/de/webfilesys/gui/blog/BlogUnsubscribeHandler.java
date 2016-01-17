package de.webfilesys.gui.blog;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.InvitationManager;
import de.webfilesys.MetaInfManager;
import de.webfilesys.gui.xsl.XslRequestHandlerBase;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

public class BlogUnsubscribeHandler extends XslRequestHandlerBase {
	
	public BlogUnsubscribeHandler(
			HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid) {
        super(req, resp, session, output, uid);
	}

	protected void process() {

		String virtualUser = req.getParameter("virtualUser");
		
		String email = req.getParameter("email");
		
		String code = req.getParameter("code");
		
		boolean success = false;
		
		if ((!CommonUtils.isEmpty(virtualUser)) && (!CommonUtils.isEmpty(email)) && (!CommonUtils.isEmpty(code))) {
			if (InvitationManager.getInstance().unsubscribe(virtualUser, email, code)) {
				success = true;
			}
		} else {
        	Logger.getLogger(getClass()).warn("missing parameters for blog unsubscribe, virtualUser=" + virtualUser + " email=" + email + " code=" + code);
		}
		
		Element blogElement = doc.createElement("blog");
			
		doc.appendChild(blogElement);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/blog/unsubscribe.xsl\"");

		doc.insertBefore(xslRef, blogElement);

		XmlUtil.setChildText(blogElement, "css", userMgr.getCSS(virtualUser), false);
		
		XmlUtil.setChildText(blogElement, "language", userMgr.getLanguage(virtualUser), false);

		String currentPath = userMgr.getDocumentRoot(virtualUser).replace('/',  File.separatorChar);
		
		String blogTitle = MetaInfManager.getInstance().getDescription(currentPath, ".");
		
		XmlUtil.setChildText(blogElement, "blogTitle", blogTitle, false);
        
		if (success) {
			XmlUtil.setChildText(blogElement, "success", "true");
		}
		
		processResponse("blog/unsubscribe.xsl", true);
    }
}
