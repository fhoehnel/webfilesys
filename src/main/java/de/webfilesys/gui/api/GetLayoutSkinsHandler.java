package de.webfilesys.gui.api;

import de.webfilesys.gui.CSSManager;
import de.webfilesys.gui.ajax.XmlRequestHandlerBase;
import de.webfilesys.util.XmlUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.PrintWriter;

public class GetLayoutSkinsHandler extends XmlRequestHandlerBase {

    private static final Logger LOG = LogManager.getLogger(GetLayoutSkinsHandler.class);

	public GetLayoutSkinsHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid) {
        super(req, resp, session, output, uid);
	}
	
	protected void process() {
        Element resultElement = doc.createElement("skins");
        CSSManager.getInstance().getAvailableCss().forEach((css) -> {
            Element skinElement = doc.createElement("skin");
            XmlUtil.setElementText(skinElement, css);
            resultElement.appendChild(skinElement);
        });
        doc.appendChild(resultElement);
		processResponse();
	}
}
