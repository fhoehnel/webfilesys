package de.webfilesys.gui.api;

import de.webfilesys.LanguageManager;
import de.webfilesys.gui.ajax.XmlRequestHandlerBase;
import de.webfilesys.util.XmlUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;

public class GetAvailableLanguagesHandler extends XmlRequestHandlerBase {

    private static final Logger LOG = LogManager.getLogger(GetAvailableLanguagesHandler.class);

	public GetAvailableLanguagesHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid) {
        super(req, resp, session, output, uid);
	}
	
	protected void process() {
        Element resultElement = doc.createElement("languages");
        LanguageManager.getInstance().getAvailableLanguages().forEach((language) -> {
            Element languageElement = doc.createElement("language");
            XmlUtil.setElementText(languageElement, language);
            resultElement.appendChild(languageElement);
        });
        doc.appendChild(resultElement);
		processResponse();
	}
}
