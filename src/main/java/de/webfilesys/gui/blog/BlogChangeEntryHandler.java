package de.webfilesys.gui.blog;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.MetaInfManager;
import de.webfilesys.gui.user.UserRequestHandler;
import de.webfilesys.util.CommonUtils;

/**
 * @author Frank Hoehnel
 */
public class BlogChangeEntryHandler extends UserRequestHandler
{
	protected HttpServletRequest req = null;

	protected HttpServletResponse resp = null;
	
	public BlogChangeEntryHandler(
			HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid)
	{
        super(req, resp, session, output, uid);
        this.req = req;
        this.resp = resp;
	}

	protected void process()
	{
		if (!checkWriteAccess())
		{
			return;
		}

		String currentPath = getCwd();

		if ((currentPath == null) || (currentPath.trim().length() == 0))
		{
			currentPath = userMgr.getDocumentRoot(uid).replace('/',  File.separatorChar);
		}

		if (!checkAccess(currentPath))
		{
			return;
		}

		String fileName = req.getParameter("fileName");
		
		if (CommonUtils.isEmpty(fileName)) {
	        Logger.getLogger(getClass()).error("missing parameter fileName");
            return;
		}

        File oldFile = new File(currentPath, fileName);
        if ((!oldFile.exists()) || (!oldFile.isFile()) || (!oldFile.canWrite())) {
	        Logger.getLogger(getClass()).error("blog entry file not found: " + fileName);
	        return;
        }
		
        String newFileName = fileName;
        
		String fileNamePrefixFromDate = getFileNamePrefixFromDate();
		
		if (!fileNamePrefixFromDate.equals(fileName.substring(0, 10))) {
	        Logger.getLogger(getClass()).debug("date has changed");

	        newFileName = fileNamePrefixFromDate + fileName.substring(10);
	        
	        File newFile = new File(currentPath, newFileName);
	        
	        if (!oldFile.renameTo(newFile)) {
		        Logger.getLogger(getClass()).error("failed to rename blog file " + fileName + " to " + newFile.getName());
		        return;
	        }
		}
		
		String blogText = req.getParameter("blogText");
		
		MetaInfManager metaInfMgr = MetaInfManager.getInstance();

		if (!CommonUtils.isEmpty(blogText)) {
			metaInfMgr.setDescription(currentPath, newFileName, blogText);
		} else {
			metaInfMgr.setDescription(currentPath, newFileName, "");
		}

		(new BlogListHandler(req, resp, session, output, uid)).handleRequest(); 
	}
	
    private String getFileNamePrefixFromDate() {
		String dateYear = req.getParameter("dateYear");
		String dateMonth = req.getParameter("dateMonth");
		String dateDay = req.getParameter("dateDay");
		
		return dateYear + "-" + dateMonth + "-" + dateDay;
    }
}
