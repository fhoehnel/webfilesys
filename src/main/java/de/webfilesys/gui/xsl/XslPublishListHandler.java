package de.webfilesys.gui.xsl;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.InvitationManager;
import de.webfilesys.LanguageManager;
import de.webfilesys.WebFileSys;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.UTF8URLEncoder;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslPublishListHandler extends XslRequestHandlerBase {
    
	private boolean ssl = false;
	
	private int serverPort = 80;
	
	public XslPublishListHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid) {
        super(req, resp, session, output, uid);
        
		String protocol = req.getScheme();
		
		if (protocol.toLowerCase().startsWith("https")) {
			ssl = true;
		}
		
		serverPort = req.getServerPort();
	}
	
	protected void process() {
		
		Element publishListElem = doc.createElement("publishList");
			
		doc.appendChild(publishListElem);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/publishList.xsl\"");

		doc.insertBefore(xslRef, publishListElem);

		XmlUtil.setChildText(publishListElem, "css", userMgr.getCSS(uid), false);

	    XmlUtil.setChildText(publishListElem, "language", language, false);
		
        Element publicationListElem = doc.createElement("publications");
        
        publishListElem.appendChild(publicationListElem);
	    
		ArrayList<String> publishCodes = InvitationManager.getInstance().getInvitationsByOwner(uid);

		SimpleDateFormat dateFormat = LanguageManager.getInstance().getDateFormat(language);

        if (publishCodes != null) {
        	for (String accessCode : publishCodes) {

				String path = InvitationManager.getInstance().getInvitationPath(accessCode);

				if (path != null) { // not yet expired

					Element publicationElem = doc.createElement("publication");
					
					publicationListElem.appendChild(publicationElem);
					
					XmlUtil.setChildText(publicationElem, "accessCode", accessCode);

					XmlUtil.setChildText(publicationElem, "relativePath", getHeadlinePath(path));
					
					Date expTime = InvitationManager.getInstance().getExpirationTime(accessCode);

					XmlUtil.setChildText(publicationElem, "expires", dateFormat.format(expTime));

					StringBuffer secretLink = new StringBuffer();

					String baseUrl = WebFileSys.getInstance().getClientUrl();
					
					if (CommonUtils.isEmpty(baseUrl)) {
						
						if (ssl) {
							secretLink.append("https://");
						} else {
							secretLink.append("http://");
						}

						if (WebFileSys.getInstance().getServerDNS() != null) {
							secretLink.append(WebFileSys.getInstance().getServerDNS());
						} else {
							secretLink.append(WebFileSys.getInstance().getLocalIPAddress());
						}

						if (serverPort != 80) {
							secretLink.append(":");
							secretLink.append(serverPort);
						}
						secretLink.append("/webfilesys");
					} else {
						secretLink.append(baseUrl);
					}
					
					String type = InvitationManager.getInstance().getInvitationType(accessCode);

					XmlUtil.setChildText(publicationElem, "type", type);
					
					if (type.equals(InvitationManager.INVITATION_TYPE_TREE)) {
					    String virtualUserId = InvitationManager.getInstance().getVirtualUser(accessCode);

						if (userMgr.getRole(virtualUserId).equals("album")) {
							secretLink.append("/visitor/");
							secretLink.append(virtualUserId);
							secretLink.append('/');
							secretLink.append(accessCode);
						} else {
						    secretLink.append("/servlet?command=silentLogin&");
						    secretLink.append(virtualUserId);
						    secretLink.append('=');
						    secretLink.append(accessCode);
						}
					}
					else {
						secretLink.append("/servlet?command=visitorFile");

						secretLink.append("&accessCode=");
						secretLink.append(accessCode);
					}

					XmlUtil.setChildText(publicationElem, "secretUrl", secretLink.toString());
					
					XmlUtil.setChildText(publicationElem, "invitationType", type);
					
					XmlUtil.setChildText(publicationElem, "cancelUrl", "/webfilesys/servlet?command=cancelPublish&accessCode=" + UTF8URLEncoder.encode(accessCode));
				}
			}
        }
        
		processResponse("publishList.xsl", false);
    }

}