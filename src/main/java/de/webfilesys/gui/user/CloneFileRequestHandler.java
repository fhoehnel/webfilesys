package de.webfilesys.gui.user;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.webfilesys.gui.xsl.XslFileListHandler;

/**
 * @author Frank Hoehnel
 */
public class CloneFileRequestHandler extends UserRequestHandler
{
	protected HttpServletRequest req = null;

	protected HttpServletResponse resp = null;

	public CloneFileRequestHandler(
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

		String newFileName = getParameter("newFileName");

		String sourceFileName=getParameter("sourceFileName");

		String path = getCwd();

		String oldFilePath = null;

		String newFilePath = null;

		if (path.endsWith(File.separator))
		{
			oldFilePath = path + sourceFileName;     
			newFilePath = path + newFileName;     
		}
		else
		{
			oldFilePath = path + File.separator + sourceFileName;
			newFilePath = path + File.separator + newFileName;
		}

		if (!checkAccess(oldFilePath))
		{
			return;
		}
		
		File destFile = new File(newFilePath);

		String errorMsg = null;
		
		if (destFile.exists()) 
		{
		    errorMsg = getResource("alert.cloneTargetExists", "Failed to clone file, target file already exists");		    
		}
		else
		{
		    if (!copyFile(oldFilePath, newFilePath))
		    {
                errorMsg = getResource("alert.cloneFailed", "Failed to clone file.");
		    }
		}
		
		if (errorMsg != null)
		{
			setParameter("errorMsg", errorMsg);
		}

		setParameter("actpath", getCwd());

		setParameter("mask","*");
		
        (new XslFileListHandler(req, resp, session, output, uid)).handleRequest();
	}
}
