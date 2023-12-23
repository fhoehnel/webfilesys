package de.webfilesys.gui.xsl;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.LanguageManager;
import de.webfilesys.gui.CSSManager;
import de.webfilesys.user.TransientUser;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslUserSettingsHandler extends XslRequestHandlerBase {
	
	private String errorMsg = null;
	
	public XslUserSettingsHandler(HttpServletRequest req, HttpServletResponse resp, HttpSession session,
			PrintWriter output, String uid, String errorMsg) {
		super(req, resp, session, output, uid);
		
        this.errorMsg = errorMsg;
	}

	protected void process() {

		if (!checkWriteAccess()) {
			return;
		}
		
		Element userSettingsElem = doc.createElement("userSettings");

		doc.appendChild(userSettingsElem);

		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet",
				"type=\"text/xsl\" href=\"/webfilesys/xsl/userSettings.xsl\"");

		doc.insertBefore(xslRef, userSettingsElem);

		if (errorMsg != null) {
			XmlUtil.setChildText(userSettingsElem, "errorMsg", errorMsg, false);
		}
		
		XmlUtil.setChildText(userSettingsElem, "login", uid, false);

		TransientUser user = userMgr.getUser(uid);
		if (user == null) {
        	LogManager.getLogger(getClass()).error("user not found: " + uid);
        	return;
		}
		
		XmlUtil.setChildText(userSettingsElem, "firstName", errorMsg == null ? user.getFirstName() : req.getParameter("firstName"), false);
		
		XmlUtil.setChildText(userSettingsElem, "lastName", errorMsg == null ? user.getLastName() : req.getParameter("lastName"), false);

		XmlUtil.setChildText(userSettingsElem, "email", errorMsg == null ? user.getEmail() : req.getParameter("email"), false);

		XmlUtil.setChildText(userSettingsElem, "phone", errorMsg == null ? user.getPhone() : req.getParameter("phone"), false);

		XmlUtil.setChildText(userSettingsElem, "newCss", errorMsg == null ? user.getCss() : req.getParameter("css"), false);
		
		String selectedLanguage = (errorMsg == null) ? user.getLanguage() : req.getParameter("language");
		
		Element availableLanguagesElem = doc.createElement("availableLanguages");
		userSettingsElem.appendChild(availableLanguagesElem);
		
		for (String availableLanguage : LanguageManager.getInstance().getAvailableLanguages()) {
			Element languageElem = doc.createElement("lang");
			XmlUtil.setElementText(languageElem, availableLanguage);
			availableLanguagesElem.appendChild(languageElem);
			if (availableLanguage.equals(selectedLanguage)) {
				languageElem.setAttribute("selected", "true");
			}
		}

		String selectedSkin = (errorMsg == null) ? user.getCss() : req.getParameter("css");
		
		Element availableSkinsElem = doc.createElement("availableSkins");
		userSettingsElem.appendChild(availableSkinsElem);
		
		for (String availableSkin : CSSManager.getInstance().getAvailableCss()) {
			Element skinElem = doc.createElement("skin");
			XmlUtil.setElementText(skinElem, availableSkin);
			availableSkinsElem.appendChild(skinElem);
			if (availableSkin.equals(selectedSkin)) {
				skinElem.setAttribute("selected", "true");
			}
		}
		
		processResponse("userSettings.xsl");
	}
}