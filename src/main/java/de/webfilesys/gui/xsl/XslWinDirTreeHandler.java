package de.webfilesys.gui.xsl;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;

import de.webfilesys.ClipBoard;
import de.webfilesys.DirTreeStatusInspector;
import de.webfilesys.WebFileSys;
import de.webfilesys.WinDriveManager;
import de.webfilesys.util.UTF8URLEncoder;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslWinDirTreeHandler extends XslDirTreeHandler
{
	boolean clientIsLocal = false;
	
	public XslWinDirTreeHandler(
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
		String docRoot = userMgr.getDocumentRoot(uid);

		char docRootDriveChar = docRoot.charAt(0);

		int docRootDriveNum=0;

		if (docRootDriveChar!='*')
		{
			if (docRootDriveChar > 'Z')
			{
				docRootDriveNum=docRootDriveChar - 'a' + 1;
			}
			else
			{
				docRootDriveNum=docRootDriveChar - 'A' + 1;
			}
		}

		String currentPath = actPath;
		
		// Logger.getLogger(getClass()).debug("current path: " + currentPath);

		if (!accessAllowed(currentPath))
		{
			currentPath = docRoot;
		}

        XmlUtil.setChildText(folderTreeElement, "currentPath", currentPath);

		XmlUtil.setChildText(folderTreeElement, "encodedPath", UTF8URLEncoder.encode(currentPath));

		if (currentPath.charAt(0) > 'Z')
		{
			char driveChar = (char) ('A' + (currentPath.charAt(0)-'a'));

			actPath=driveChar + currentPath.substring(1);
		}
		else
		{
			actPath=currentPath;
		}
		
		ClipBoard clipBoard = (ClipBoard) session.getAttribute("clipBoard");
		
		if ((clipBoard == null) || clipBoard.isEmpty())
		{
			XmlUtil.setChildText(folderTreeElement, "clipBoardEmpty", "true", false);
		}
        else
        {
			XmlUtil.setChildText(folderTreeElement, "clipBoardEmpty", "false", false);
        }
		
        Element computerElement = doc.createElement("computer");

        folderTreeElement.appendChild(computerElement);
        
        computerElement.setAttribute("name", WebFileSys.getInstance().getLocalHostName());

		ArrayList<Integer> existingDrives = new ArrayList();

		if (docRootDriveChar=='*')
        {
	        for (int i = 1; i <= 26; i++)
	        {
	            String driveLabel = WinDriveManager.getInstance().getDriveLabel(i);

	            if (driveLabel != null)
	            {
                    existingDrives.add(new Integer(i));
	            }
	        }
        } 
		else
		{
            String driveLabel = WinDriveManager.getInstance().getDriveLabel(docRootDriveNum);

            if (driveLabel != null)
            {
                existingDrives.add(new Integer(docRootDriveNum));
            }
		}
		
		for (Integer drive : existingDrives) {
		
			int driveNum = drive.intValue();

			String driveLabel = WinDriveManager.getInstance().getDriveLabel(driveNum);

			if (driveLabel != null)
			{
				char driveChar = 'A';
				driveChar += (driveNum - 1);

				String subdirPath = driveChar + ":" + File.separator;

				Element parentElement = computerElement;

				boolean access = accessAllowed(subdirPath);

				if (access)
				{
					dirCounter++;
					
					if (subdirPath.equals(actPath))
					{
						currentDirNum = dirCounter;
					}

					// boolean isActPath=subdirPath.equals(actPath);

					String encodedPath = UTF8URLEncoder.encode(subdirPath);

					Element driveElement = doc.createElement("folder");

                    if (driveNum < 3)
                    {
						driveElement.setAttribute("type", "floppy");
                    }
                    else
                    {
						driveElement.setAttribute("type", "drive");
                    }
                    
					driveElement.setAttribute("name", subdirPath);
					
					driveElement.setAttribute("id", Integer.toString(dirCounter));

					driveElement.setAttribute("path", encodedPath);

					driveElement.setAttribute("menuPath", insertDoubleBackslash(subdirPath));

					driveElement.setAttribute("label", driveLabel);
					// XmlUtil.setChildText(driveElement, "label", driveLabel);

                    computerElement.appendChild(driveElement);
                    
                    parentElement = driveElement;
				}

				if (dirTreeStatus.dirExpanded(subdirPath))
				{
					dirSubTree(parentElement, actPath, subdirPath, access);
				}
			}
		}

		String fastPath = getParameter("fastPath");

		if (fastPath != null)
		{
			XmlUtil.setChildText(folderTreeElement, "fastPath", insertDoubleBackslash(actPath));
		}

		int pollInterval = WebFileSys.getInstance().getPollFilesysChangesInterval();
		if (pollInterval > 0) {
			XmlUtil.setChildText(folderTreeElement, "pollInterval", Integer.toString(pollInterval));
		}
		
        this.processResponse("folderTree.xsl");

        if (pollInterval > 0) {
        	(new DirTreeStatusInspector(dirTreeStatus)).start();
        }
	}
}