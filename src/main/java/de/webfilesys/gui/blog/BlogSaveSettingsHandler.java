package de.webfilesys.gui.blog;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import de.webfilesys.MetaInfManager;
import de.webfilesys.gui.ajax.XmlRequestHandlerBase;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * Delete a folder tree.
 * Mobile version.
 */
public class BlogSaveSettingsHandler extends XmlRequestHandlerBase
{
	public BlogSaveSettingsHandler(
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
		if (!checkWriteAccess())
		{
			return;
		}
		
    	boolean blogTitleChanged = false;

    	String newBlogTitle = req.getParameter("blogTitle");
		
		if (!CommonUtils.isEmpty(newBlogTitle)) {
			String currentPath = userMgr.getDocumentRoot(uid).replace('/',  File.separatorChar);
			
			MetaInfManager metaInfMgr = MetaInfManager.getInstance();
			
			String oldBlogTitle = metaInfMgr.getDescription(currentPath, ".");

		    if (!newBlogTitle.equals(oldBlogTitle)) {
		    	metaInfMgr.setDescription(currentPath, ".", newBlogTitle);
		    	blogTitleChanged = true;
		    }
		}
		
		boolean pageSizeChanged = false;
		
		String daysPerPage = req.getParameter("daysPerPage");
		
		if (!CommonUtils.isEmpty(daysPerPage)) {
			int pageSize = 0;
			try {
				pageSize = Integer.parseInt(daysPerPage);
				
				if (userMgr.getPageSize(uid) != pageSize) {
					pageSizeChanged = true;
				}
				
				userMgr.setPageSize(uid, pageSize);
			} catch (NumberFormatException numEx) {
		        Logger.getLogger(getClass()).error("invalid blog page size: " + daysPerPage);
			}
		} else {
	        Logger.getLogger(getClass()).warn("missing parameter blog page size");
		}
		
        String newPassword = req.getParameter("newPassword");		
        String newPasswdConfirm = req.getParameter("newPasswdConfirm");
        
        if (CommonUtils.isEmpty(newPassword)) {
	        Logger.getLogger(getClass()).warn("missing parameter newPassword");
        }
        if (CommonUtils.isEmpty(newPasswdConfirm)) {
	        Logger.getLogger(getClass()).warn("missing parameter newPasswdConfirm");
        }
        
        if ((!CommonUtils.isEmpty(newPassword)) && (!CommonUtils.isEmpty(newPasswdConfirm))) {
        	if (!newPassword.equals(newPasswdConfirm)) {
	            Logger.getLogger(getClass()).warn("password and password confirmation do not match");
            } else {
        	    userMgr.setPassword(uid, newPassword);
            }
        }
		
		Element resultElement = doc.createElement("result");

		XmlUtil.setChildText(resultElement, "success", "true");
		XmlUtil.setChildText(resultElement, "pageSizeChanged", Boolean.toString(pageSizeChanged));
		XmlUtil.setChildText(resultElement, "blogTitleChanged", Boolean.toString(blogTitleChanged));
				
		doc.appendChild(resultElement);
		
		this.processResponse();
	}
}
