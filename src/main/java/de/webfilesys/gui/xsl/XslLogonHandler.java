package de.webfilesys.gui.xsl;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

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
		
	    XmlUtil.setChildText(loginElement, "language", WebFileSys.getInstance().getPrimaryLanguage(), false);

		if (authFailed)
		{
			XmlUtil.setChildText(loginElement, "authFailed", "true", false);
		}

		if (getParameter("activationSuccess") != null) {
            XmlUtil.setChildText(loginElement, "activationSuccess", "true", false);
        }

		if (WebFileSys.getInstance().isOpenRegistration())
		{
			XmlUtil.setChildText(loginElement, "openRegistration", "true", false);
		}

		processResponse("login.xsl", false);
    }
}