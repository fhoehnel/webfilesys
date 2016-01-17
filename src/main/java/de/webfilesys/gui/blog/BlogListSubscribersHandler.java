package de.webfilesys.gui.blog;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;

import de.webfilesys.InvitationManager;
import de.webfilesys.gui.ajax.XmlRequestHandlerBase;
import de.webfilesys.util.XmlUtil;

public class BlogListSubscribersHandler extends XmlRequestHandlerBase {
	
	public BlogListSubscribersHandler(
			HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid) {
        super(req, resp, session, output, uid);
	}

	protected void process() {
		
		Element blogElement = doc.createElement("blog");
			
		doc.appendChild(blogElement);

		String currentPath = userMgr.getDocumentRoot(uid).replace('/',  File.separatorChar);
		
        ArrayList<String> subscriberList = InvitationManager.getInstance().listSubscribers(uid, currentPath);

        Element subscriberListElem = doc.createElement("subscriberList");
        
        blogElement.appendChild(subscriberListElem);
        
        for (String subscriber : subscriberList) {
            Element subscriberElem = doc.createElement("subscriber");
            XmlUtil.setElementText(subscriberElem, subscriber);
            subscriberListElem.appendChild(subscriberElem);
        }
		
		processResponse();
    }

}
