package de.webfilesys.gui.admin;

import java.io.File;
import java.io.PrintWriter;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;

import de.webfilesys.WebFileSys;
import de.webfilesys.WinDriveManager;
import de.webfilesys.util.UTF8URLEncoder;
import de.webfilesys.util.XmlUtil;

/**
 * Select a folder for an administrative task, for example to select the document root for an user.
 * Windows version.
 * 
 * @author Frank Hoehnel
 */
public class AdminSelectWinFolderHandler extends AdminSelectFolderHandler
{
	boolean clientIsLocal = false;
	
	public AdminSelectWinFolderHandler(
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
        if (!isAdminUser(true))
        {
            return;
        }
        
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

        if (actPath == null) 
        {
            actPath = "C:\\";
        }
        
		String currentPath = actPath;

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

		XmlUtil.setChildText(folderTreeElement, "css", userMgr.getCSS(uid), false);

        Element computerElement = doc.createElement("computer");

        folderTreeElement.appendChild(computerElement);
        
        computerElement.setAttribute("name", WebFileSys.getInstance().getLocalHostName());

		Vector existingDrives=new Vector();

		for (int i=1;i<=26;i++)
		{
			String driveLabel = WinDriveManager.getInstance().getDriveLabel(i);

			if (driveLabel!=null)
			{
				if ((docRootDriveChar=='*') || (i==docRootDriveNum))
				{
					existingDrives.add(new Integer(i));
				}
			}
		}

		for (int i=0;i<existingDrives.size();i++)
		{
			int driveNum=((Integer) existingDrives.elementAt(i)).intValue();

			String driveLabel = WinDriveManager.getInstance().getDriveLabel(driveNum);

			if (driveLabel!=null)
			{
				char driveChar='A';
				driveChar+=(driveNum-1);

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

					driveElement.setAttribute("label", driveLabel);

                    computerElement.appendChild(driveElement);
                    
                    parentElement = driveElement;
				}

				if (dirTreeStatus.dirExpanded(subdirPath))
				{
					dirSubTree(parentElement, actPath, subdirPath, access);
				}
			}
		}

		int topOfScreenDir=0;

		if (currentDirNum > 5)
		{
			topOfScreenDir=currentDirNum - 5;
		}

		int scrollPos;
		
		if (browserManufacturer == BROWSER_MSIE)
		{
			scrollPos = topOfScreenDir * 17;  // pixels per line
		}
		else
		{
			scrollPos = topOfScreenDir * 18;  // pixels per line
		}

		XmlUtil.setChildText(folderTreeElement, "scrollPos", "" + scrollPos);

        this.processResponse("adminSelectFolder.xsl");
	}
}
