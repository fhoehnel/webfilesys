package de.webfilesys.gui.xsl;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.WebFileSys;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslMenuBarHandler extends XslRequestHandlerBase
{
	public XslMenuBarHandler(
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
		Element menuBarElement = doc.createElement("menubar");
			
		doc.appendChild(menuBarElement);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/menuBar.xsl\"");

		doc.insertBefore(xslRef, menuBarElement);

		XmlUtil.setChildText(menuBarElement, "css", userMgr.getCSS(uid), false);
		XmlUtil.setChildText(menuBarElement, "userid", uid, false);
		XmlUtil.setChildText(menuBarElement, "role", userMgr.getRole(uid), false);
		XmlUtil.setChildText(menuBarElement, "helpLanguage", userMgr.getLanguage(uid), false);
		
	    XmlUtil.setChildText(menuBarElement, "language", language, false);
	    
	    XmlUtil.setChildText(menuBarElement, "hostname", WebFileSys.getInstance().getLocalHostName(), false);		
		
	    if (((File.separatorChar == '/') && isAdminUser(false)) ||
			(userMgr.getDocumentRoot(uid).equals("/") && (!isWebspaceUser())))
		{
			XmlUtil.setChildText(menuBarElement, "unixAdmin", "true", false);
		}
		
		if (WebFileSys.getInstance().isOpenRegistration())
		{
			XmlUtil.setChildText(menuBarElement, "registrationType", "open", false);
		}
		else
		{
			XmlUtil.setChildText(menuBarElement, "registrationType", "closed", false);
		}
		
		if (userMgr.getDiskQuota(uid) > (-1L))
		{
			XmlUtil.setChildText(menuBarElement, "diskQuota", "true", false);
		}

		if (WebFileSys.getInstance().isMaintananceMode())
		{
			XmlUtil.setChildText(menuBarElement, "maintananceMode", "true", false);
		}

		if (readonly)
		{
			XmlUtil.setChildText(menuBarElement, "readonly", "true", false);
		}
		else
		{
			XmlUtil.setChildText(menuBarElement, "readonly", "false", false);
		}
		
        if ((File.separatorChar == '\\') && userMgr.getDocumentRoot(uid).equals("*:"))
        {
            XmlUtil.setChildText(menuBarElement, "queryDrives", "true", false);
        }

        if (WebFileSys.getInstance().isEnableCalendar())
        {
            XmlUtil.setChildText(menuBarElement, "calendarEnabled", "true", false);
        }
        
		processResponse("menuBar.xsl", false);
    }
}