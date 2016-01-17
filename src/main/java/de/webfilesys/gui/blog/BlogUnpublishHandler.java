package de.webfilesys.gui.blog;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;

import de.webfilesys.InvitationManager;
import de.webfilesys.gui.ajax.XmlRequestHandlerBase;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class BlogUnpublishHandler extends XmlRequestHandlerBase {
	
	public BlogUnpublishHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid) {
        super(req, resp, session, output, uid);
	}
	
	protected void process() {

		if (!checkWriteAccess()) {
			return;
		}
		
		String currentPath = userMgr.getDocumentRoot(uid).replace('/',  File.separatorChar);

		boolean removed = false;
		
		ArrayList<String> publishCodes = InvitationManager.getInstance().getInvitationsByOwner(uid);

        if (publishCodes != null) {
			for (int i = 0; (i < publishCodes.size()) && (!removed); i++) {
				String accessCode= (String) publishCodes.get(i);

				String path = InvitationManager.getInstance().getInvitationPath(accessCode);

				if (path != null) { // not expired
				    if (path.equals(currentPath)) {
				        String virtualUser = InvitationManager.getInstance().getVirtualUser(accessCode);
				        if (virtualUser != null) {
				        	removed = userMgr.removeUser(virtualUser);
					        InvitationManager.getInstance().removeInvitation(accessCode);
				        }
				    }
				}
			}
        }
		
        Element resultElement = doc.createElement("result");
        
        XmlUtil.setChildText(resultElement, "success", Boolean.toString(removed));
        
        doc.appendChild(resultElement);
		
		processResponse();
	}
}
