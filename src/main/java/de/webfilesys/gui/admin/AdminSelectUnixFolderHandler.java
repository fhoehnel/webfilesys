package de.webfilesys.gui.admin;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;

import de.webfilesys.WebFileSys;
import de.webfilesys.util.UTF8URLEncoder;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class AdminSelectUnixFolderHandler extends AdminSelectFolderHandler
{
	boolean clientIsLocal = false;
	
	public AdminSelectUnixFolderHandler(
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
        
        if (actPath == null) 
        {
            actPath = "/";
        }
        
		XmlUtil.setChildText(folderTreeElement, "currentPath", actPath);

		XmlUtil.setChildText(folderTreeElement, "encodedPath", UTF8URLEncoder.encode(actPath));

		XmlUtil.setChildText(folderTreeElement, "css", userMgr.getCSS(uid), false);

		Element computerElement = doc.createElement("computer");

		folderTreeElement.appendChild(computerElement);
        
        computerElement.setAttribute("name", WebFileSys.getInstance().getLocalHostName());

        Element parentElement = computerElement;

		boolean access = accessAllowed("/");

		if (access)
		{
			dirCounter++;
					
			if (actPath.equals("/"))
			{
				currentDirNum = dirCounter;
			}

			Element rootElement = doc.createElement("folder");

			rootElement.setAttribute("name", "/");

			rootElement.setAttribute("id", Integer.toString(dirCounter));

			rootElement.setAttribute("path", "/");

			computerElement.appendChild(rootElement);
                    
			parentElement = rootElement;
		}

		if (dirTreeStatus.dirExpanded("/"))
		{
			dirSubTree(parentElement, actPath, "/", access);
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
