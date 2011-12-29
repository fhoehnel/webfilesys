package de.webfilesys.gui.ajax;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;

import de.webfilesys.util.XmlUtil;
import de.webfilesys.watch.FolderWatchManager;

/**
 * Add/Remove this user as listener for folder change notification.
 * @author Frank Hoehnel
 */
public class XmlSwitchWatchFolderHandler extends XmlRequestHandlerBase
{
	public XmlSwitchWatchFolderHandler(
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
        
		if (FolderWatchManager.getInstance().isListener(path, uid))
		{
		    FolderWatchManager.getInstance().removeFolderChangeListener(path, uid);
		}
		else
		{
            FolderWatchManager.getInstance().addFolderChangeListener(path, uid);
		}
		
        Element resultElement = doc.createElement("result");
        
		XmlUtil.setChildText(resultElement, "success", "true");
		
		doc.appendChild(resultElement);
		
		processResponse();
	}
}
