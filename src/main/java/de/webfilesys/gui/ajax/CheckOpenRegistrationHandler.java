package de.webfilesys.gui.ajax;

import de.webfilesys.WebFileSysConfig;
import de.webfilesys.util.XmlUtil;
import org.w3c.dom.Element;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.PrintWriter;

public class CheckOpenRegistrationHandler extends XmlRequestHandlerBase {
	public CheckOpenRegistrationHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid) {
        super(req, resp, session, output, uid);
	}
	
	protected void process() {
        Element resultElement = doc.createElement("result");
        XmlUtil.setElementText(resultElement, Boolean.toString(WebFileSysConfig.getInstance().isOpenRegistration()));
        doc.appendChild(resultElement);
		processResponse();
	}
}
