package de.webfilesys.gui.xsl;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.LanguageManager;
import de.webfilesys.WebFileSys;
import de.webfilesys.gui.CSSManager;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslLogonHandler extends XslRequestHandlerBase
{
	boolean authFailed = false;
	
	public XslLogonHandler(HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
    		PrintWriter output, 
			boolean authFailed)
	{
		super(req, resp, session, output, null);
		
		this.authFailed = authFailed;
	}
	  
	protected void process()
	{ 
		Element loginElement = doc.createElement("login");
			
		doc.appendChild(loginElement);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/login.xsl\"");

		doc.insertBefore(xslRef, loginElement);

		XmlUtil.setChildText(loginElement, "css", CSSManager.DEFAULT_LAYOUT, false);
		XmlUtil.setChildText(loginElement, "localHost", WebFileSys.getInstance().getLocalHostName(), false);
		XmlUtil.setChildText(loginElement, "operatingSystem", WebFileSys.getInstance().getOpSysName(), false);
		XmlUtil.setChildText(loginElement, "version", WebFileSys.VERSION, false);

		LanguageManager langMgr = LanguageManager.getInstance();

		if (authFailed)
		{
			addMsgResource("alert.invalidlogin", 
						   langMgr.getResource(WebFileSys.getInstance().getPrimaryLanguage(),
											   "alert.invalidlogin",
											   "userid or password invalid"));
		}

		addMsgResource("label.login.title", 
		               langMgr.getResource(WebFileSys.getInstance().getPrimaryLanguage(),
                                           "label.login.title",
                                           "WebFileSys Login"));
		
		addMsgResource("label.userid", 
					   langMgr.getResource(WebFileSys.getInstance().getPrimaryLanguage(),
										   "label.userid",
										   "userid"));

		addMsgResource("label.password", 
					   langMgr.getResource(WebFileSys.getInstance().getPrimaryLanguage(),
										   "label.password",
										   "password"));

		addMsgResource("label.logon", 
					   langMgr.getResource(WebFileSys.getInstance().getPrimaryLanguage(),
										   "label.logon",
										   "Logon"));

		if (WebFileSys.getInstance().isOpenRegistration())
		{
			addMsgResource("label.registerself", 
						   langMgr.getResource(WebFileSys.getInstance().getPrimaryLanguage(),
											   "label.registerself",
											   "I am a new user"));
		}

		this.processResponse("login.xsl", false);
    }
}