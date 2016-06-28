package de.webfilesys.gui.blog;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import de.webfilesys.MetaInfManager;
import de.webfilesys.graphics.BlogThumbnailHandler;
import de.webfilesys.gui.ajax.XmlRequestHandlerBase;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * Delete a folder tree.
 * Mobile version.
 */
public class BlogDeleteEntryHandler extends XmlRequestHandlerBase
{
	public BlogDeleteEntryHandler(
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
		
		String fileName = getParameter("fileName");

		if (CommonUtils.isEmpty(fileName)) {
			Logger.getLogger(getClass()).error("missing parameter fileName");
			return;
		}
		
		String currentPath = userMgr.getDocumentRoot(uid).replace('/',  File.separatorChar);

		Element resultElement = doc.createElement("result");

		String success = null;
		
		File fileToBeDeleted = new File(currentPath, fileName);
		
		String deletedFilePath = fileToBeDeleted.getAbsolutePath();
		
		if ((!fileToBeDeleted.exists()) || (!fileToBeDeleted.isFile()) || (!fileToBeDeleted.canWrite())) {
			Logger.getLogger(getClass()).error("blog entry file to be deleted is not a writable file: " + fileToBeDeleted.getAbsolutePath());
		} else {
			if (fileToBeDeleted.delete()) {
				MetaInfManager.getInstance().removeMetaInf(currentPath, fileName);
				BlogThumbnailHandler.getInstance().deleteThumbnail(deletedFilePath);
				success = "deleted";
			} else {
				Logger.getLogger(getClass()).error("failed to delete blog entry file " + fileToBeDeleted.getAbsolutePath());
			}
		}

		if (success == null) {
		    success = "false";
		}

		XmlUtil.setChildText(resultElement, "success", success);
		
		doc.appendChild(resultElement);
		
		this.processResponse();
	}
	
}
