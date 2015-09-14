package de.webfilesys.gui.ajax;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;
import de.webfilesys.watch.FolderWatchManager;

/**
 * @author Frank Hoehnel
 */
public class XslWatchFolderHandler extends XmlRequestHandlerBase
{
	public XslWatchFolderHandler(
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

		if (!accessAllowed(path))
		{
			Logger.getLogger(getClass()).warn("user " + uid + " tried to access folder outside of his document root: " + path);
			
			return;
		}

		String shortPath = CommonUtils.shortName(getHeadlinePath(path), 50);
		
		Element folderWatchElement = doc.createElement("folderWatch");
			
		doc.appendChild(folderWatchElement);

		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/folderWatch.xsl\"");

		doc.insertBefore(xslRef, folderWatchElement);

		XmlUtil.setChildText(folderWatchElement, "css", userMgr.getCSS(uid), false);
		XmlUtil.setChildText(folderWatchElement, "path", path.replace('\\', '/'), false);
		XmlUtil.setChildText(folderWatchElement, "shortPath", shortPath, false);
		
		if (FolderWatchManager.getInstance().isListener(path, uid))
		{
	        XmlUtil.setChildText(folderWatchElement, "watched", "true");
		}

		processResponse();
    }
}