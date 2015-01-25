package de.webfilesys.gui.blog;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.MetaInfManager;
import de.webfilesys.gui.xsl.XslRequestHandlerBase;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class BlogShowSettingsHandler extends XslRequestHandlerBase
{
	public BlogShowSettingsHandler(
			HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid)
	{
        super(req, resp, session, output, uid);
	}
	  
	protected void process()
	{
        if (!this.checkWriteAccess()) {
        	return;
        }
		
		Element settingsElement = doc.createElement("settings");
			
		doc.appendChild(settingsElement);

		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/blog/settings.xsl\"");

		doc.insertBefore(xslRef, settingsElement);

		String currentPath = userMgr.getDocumentRoot(uid).replace('/',  File.separatorChar);
		
		MetaInfManager metaInfMgr = MetaInfManager.getInstance();
		
		String blogTitle = metaInfMgr.getDescription(currentPath, ".");

		XmlUtil.setChildText(settingsElement, "blogTitleText", blogTitle, true);

		int daysPerPage = BlogListHandler.DEFAULT_PAGE_SIZE;
	    
		int pageSize = userMgr.getPageSize(uid);
		
		if (pageSize >= 1) {
			daysPerPage = pageSize;
		}
		
		XmlUtil.setChildText(settingsElement, "daysPerPage", Integer.toString(daysPerPage), false);
			
		this.processResponse("blog/settings.xsl", false);
    }
}