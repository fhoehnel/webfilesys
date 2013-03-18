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
            addMsgResource("label.refreshDrives", getResource("label.refreshDrives","Check for added/removed drives"));
        }

        if (WebFileSys.getInstance().isEnableCalendar())
        {
            XmlUtil.setChildText(menuBarElement, "calendarEnabled", "true", false);
    		addMsgResource("label.calendar", getResource("label.calendar","Calendar"));
        }
        
		addMsgResource("label.fastpath", getResource("label.fastpath","Fast Path (last visited directories)"));
		addMsgResource("label.bookmarks", getResource("label.bookmarks","Bookmarked Folders"));
        addMsgResource("label.returnToPrevDir", getResource("label.returnToPrevDir","Return to last visited folder"));
		
		addMsgResource("label.processes", getResource("label.processes","Operating System Processes"));

		addMsgResource("label.fsstat", getResource("label.fsstat","File System Statistics"));
		addMsgResource("alert.iframeSupport", getResource("alert.iframeSupport","Your browser does not support IFRAMEs.\\nYou cannot use the download function.\\nCheck your browser settings!"));

		addMsgResource("label.oscmd", getResource("label.oscmd","Run Operating System Cmd"));
		addMsgResource("label.admin", getResource("label.admin","Administration"));

		addMsgResource("label.editregistration", getResource("label.editregistration","Edit User Data"));
		addMsgResource("label.settings", getResource("label.settings","Settings"));
		addMsgResource("label.publishList", getResource("label.publishList","List published folders"));
		addMsgResource("label.diskQuotaUsage", getResource("label.diskQuotaUsage","Account size usage"));
		addMsgResource("label.search", getResource("label.search","Search"));

		addMsgResource("label.slideshow", getResource("label.slideshow","Picture Slideshow"));
		addMsgResource("label.story", getResource("label.story","Picture Story"));

		addMsgResource("label.ftpBackup", getResource("label.ftpBackup","FTP Backup"));
		addMsgResource("label.help", getResource("label.help","Help"));
		addMsgResource("label.about", getResource("label.about", "About WebFileSys"));
		addMsgResource("label.logout", getResource("label.logout","Logout"));

		this.processResponse("menuBar.xsl", false);
    }
}