package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import de.webfilesys.FastPathManager;
import de.webfilesys.MetaInfManager;
import de.webfilesys.SubdirExistCache;
import de.webfilesys.TestSubDirThread;
import de.webfilesys.util.XmlUtil;

/**
 * Delete a folder tree.
 * Non-mobile version.
 */
public class XmlRemoveDirHandler extends XmlRequestHandlerBase
{
	public XmlRemoveDirHandler(
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
		
		String path = getParameter("path");

		if (!checkAccess(path))
		{
			return;
		}

		Element resultElement = doc.createElement("result");

		String success = null;
		
		String resultMsg = "";

		String lowerCaseDocRoot = userMgr.getLowerCaseDocRoot(uid);

		if (path.toLowerCase().replace('\\','/').equals(lowerCaseDocRoot))
		{
			success = "error";

		    resultMsg = getResource("alert.delhomedir", "The home directory may not be deleted!");
		}
		else
		{
			File dirToBeDeleted = new File(path);
			
			if (!dirToBeDeleted.canWrite() || (!dirToBeDeleted.isDirectory()))
			{
				Logger.getLogger(getClass()).warn(dirToBeDeleted + " cannot be deleted (is not a writable directory)");
				
			    success = "error";

			    resultMsg = getResource("alert.delDirError", "could not be deleted!");
			}
			else
			{
                File parentDir = dirToBeDeleted.getParentFile();
                
                if (delDirTree(path))
                {
                    MetaInfManager.getInstance().removePath(path);
                    
                    SubdirExistCache.getInstance().cleanupExistSubdir(path);
                    
                    FastPathManager.getInstance().removeTree(uid, path);
                    
                    success = "deleted";

                    XmlUtil.setChildText(resultElement, "parentPath", parentDir.getAbsolutePath());
                }
                else
                {
                    success = "error";
                    
                    resultMsg = getResource("alert.removeDirError", "Directory could not be deleted!");

                    XmlUtil.setChildText(resultElement, "path", path);
                }

                // even if only a part of the tree could be deleted we have to refresh the
                // subdir status
                
                (new TestSubDirThread(parentDir.getAbsolutePath())).start();
			}
		}
		
		XmlUtil.setChildText(resultElement, "success", success);
		
		XmlUtil.setChildText(resultElement, "message", resultMsg);

		XmlUtil.setChildText(resultElement, "path", path);

		doc.appendChild(resultElement);
		
		this.processResponse();
	}
}
