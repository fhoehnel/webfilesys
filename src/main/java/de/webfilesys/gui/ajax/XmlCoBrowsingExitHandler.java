package de.webfilesys.gui.ajax;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;

import de.webfilesys.CoBrowsingManager;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XmlCoBrowsingExitHandler extends XmlRequestHandlerBase
{
	public static final String SESSION_KEY_SLIDESHOW_BUFFER = "slideshowBuffer";

	public XmlCoBrowsingExitHandler(
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
		CoBrowsingManager.getInstance().terminateCoBrowsing(uid);
		
		Element resultElement = doc.createElement("result");
		
		XmlUtil.setChildText(resultElement, "success", "true");
		
		XmlUtil.setChildText(resultElement, "message", "");

		doc.appendChild(resultElement);
		
		this.processResponse();
	}
	
}
