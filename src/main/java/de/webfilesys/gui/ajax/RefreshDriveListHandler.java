package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;

import de.webfilesys.WinDriveManager;
import de.webfilesys.util.XmlUtil;

/**
 * Check for addedd/removed drives.
 * @author Frank Hoehnel
 */
public class RefreshDriveListHandler extends XmlRequestHandlerBase
{
	public RefreshDriveListHandler(
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
        if ((File.separatorChar == '\\') && userMgr.getDocumentRoot(uid).equals("*:"))
	    {
            WinDriveManager.getInstance().queryDrives();
            
	        Element resultElement = doc.createElement("result");
	        
	        XmlUtil.setElementText(resultElement, Boolean.TRUE.toString());
	        
	        doc.appendChild(resultElement);
	        
	        processResponse();
	    }
	}
}
