package de.webfilesys.gui.xsl;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.Constants;
import de.webfilesys.DirTreeStatus;
import de.webfilesys.FastPathManager;
import de.webfilesys.MetaInfManager;
import de.webfilesys.SubdirExistCache;
import de.webfilesys.UpdateLinksAfterDirRenameThread;
import de.webfilesys.WebFileSys;
import de.webfilesys.decoration.Decoration;
import de.webfilesys.decoration.DecorationManager;
import de.webfilesys.gui.xsl.mobile.MobileFolderFileListHandler;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.UTF8URLEncoder;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslRenameDirHandler extends XslRequestHandlerBase
{
	static final int ERROR_DEST_EXISTS   = 1;
	static final int ERROR_RENAME_FAILED = 2;
	static final int ERROR_MISSING_DEST  = 3;
	
	boolean clientIsLocal = false;
	
	public XslRenameDirHandler(
			HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid,
	        boolean clientIsLocal)
	{
        super(req, resp, session, output, uid);
		
		this.clientIsLocal = clientIsLocal;
	}
	  
	protected void process()
	{
		if (!checkWriteAccess())
		{
			return;
		}

		String currentPath = getParameter("path");

		if (!checkAccess(currentPath))
		{
			return;
		}

		String lowerCaseDocRoot = userMgr.getLowerCaseDocRoot(uid);

		if (currentPath.toLowerCase().replace('\\','/').equals(lowerCaseDocRoot))
		{
			Element errorElement = doc.createElement("error");
			
			doc.appendChild(errorElement);
			
			ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/errorMsgFolder.xsl\"");

			doc.insertBefore(xslRef, errorElement);
			
			XmlUtil.setChildText(errorElement, "errorMsg", getResource("alert.renamehomedir", "You cannot rename your home directory!"), false);

			XmlUtil.setChildText(errorElement, "currentPath", UTF8URLEncoder.encode(currentPath) , false);

			processResponse("errorMsgFolder.xsl");

            return;
		}
		
		int errorCode = 0;

		String newPath = null;

		String parentDir = currentPath.substring(0,currentPath.lastIndexOf(File.separatorChar));

		if (parentDir.endsWith(":"))
		{
			parentDir = parentDir + File.separator;
		}
		
		String oldName = currentPath.substring(currentPath.lastIndexOf(File.separatorChar) + 1);

		String newDirName = getParameter("NewDirName");
        
		if (newDirName != null)
		{
			newDirName = newDirName.trim();
        	
			if (newDirName.length() > 0)
			{
				newPath = parentDir + File.separator + newDirName; 

				File oldDir = new File(currentPath);
				File newDir = new File(newPath);

				if (newDir.exists())
				{
					errorCode = ERROR_DEST_EXISTS;
				}
				else
				{
					Decoration savedDeco = DecorationManager.getInstance().getDecoration(currentPath);
					
					MetaInfManager.getInstance().saveMetaInfFile(currentPath);
					
					if (!oldDir.renameTo(newDir))
					{
						errorCode = ERROR_RENAME_FAILED;
					}
					else
					{
                        MetaInfManager.getInstance().releaseMetaInf(currentPath);
                        
					    if (WebFileSys.getInstance().isReverseFileLinkingEnabled())
					    {
	                        (new UpdateLinksAfterDirRenameThread(newPath, uid)).start();
					    }
					    
					    if (savedDeco != null) {
						    DecorationManager.getInstance().setDecoration(newPath, savedDeco);					    
					    }
					    
					    String mobile = (String) session.getAttribute("mobile");
					    
					    if (mobile != null) {
                            (new MobileFolderFileListHandler(req, resp, session, output, uid)).handleRequest();

                            return;
					    }
					    
						DirTreeStatus dirTreeStatus = (DirTreeStatus) session.getAttribute(Constants.SESSION_KEY_DIR_TREE_STATUS);
						
						if (dirTreeStatus == null)
						{
							dirTreeStatus = new DirTreeStatus();
							
							session.setAttribute(Constants.SESSION_KEY_DIR_TREE_STATUS, dirTreeStatus);
						}
						
						dirTreeStatus.expandDir(newPath);
						
						setParameter("actPath", newPath);
						setParameter("expand", newPath);
						
						String cwd = getCwd();
						
						if ((cwd != null) && cwd.equals(currentPath)) {
							setParameter("fastPath",  "true");
						}
						
						SubdirExistCache.getInstance().setExistsSubdir(newPath, new Integer(0));
						
						SubdirExistCache.getInstance().cleanupExistSubdir(currentPath);
						
						FastPathManager.getInstance().removeTree(uid, currentPath);
						
			    		if (File.separatorChar == '/')
			    		{
			    			(new XslUnixDirTreeHandler(req, resp, session, output, uid, clientIsLocal)).handleRequest();
			    		}
			    		else
			    		{
			    			(new XslWinDirTreeHandler(req, resp, session, output, uid, clientIsLocal)).handleRequest();
			    		}
						
						return;
					}
				}
			}
			else
			{
				errorCode = ERROR_MISSING_DEST;
			}
		}

		String errorMsg = "";
		
        if (errorCode == ERROR_DEST_EXISTS)
        {
			errorMsg = getResource("alert.destIsDir", "a directory with this name already exists!");
		}
        else if (errorCode == ERROR_RENAME_FAILED)
        {
			errorMsg = getResource("label.directory", "folder") + " " + oldName + " " + getResource("error.renameFailed", "could not be renamed to") + " " + newDirName;
        }
		
		Element errorElement = doc.createElement("error");
		
		doc.appendChild(errorElement);
		
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/errorMsgFolder.xsl\"");

		doc.insertBefore(xslRef, errorElement);
		
		XmlUtil.setChildText(errorElement, "errorMsg", errorMsg, false);

		XmlUtil.setChildText(errorElement, "currentPath", UTF8URLEncoder.encode(currentPath) , false);

		this.processResponse("errorMsgFolder.xsl");
    }
}