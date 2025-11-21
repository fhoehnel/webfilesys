package de.webfilesys.gui.xsl;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.webfilesys.WebFileSysConfig;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.Category;
import de.webfilesys.CategoryManager;
import de.webfilesys.WebFileSys;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslSearchGPSParmsHandler extends XslRequestHandlerBase {
	public XslSearchGPSParmsHandler(
			HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid) {
        super(req, resp, session, output, uid);
	}
	  
	protected void process() {
		String currentPath = getParameter("actpath");

		if (isMobile()) {
            currentPath = getAbsolutePath(currentPath);
        } else {
            if ((currentPath == null) || (currentPath.trim().length() == 0)) {
                currentPath = getCwd();
            }
        }

		if (!accessAllowed(currentPath)) {
			LogManager.getLogger(getClass()).warn("user " + uid + " tried to access folder outside of his document root: " + currentPath);
			return;
		}

		String relativePath = this.getHeadlinePath(currentPath);

		Element searchParmsElement = doc.createElement("searchParms");
			
		doc.appendChild(searchParmsElement);

		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/searchGPSParms.xsl\"");

		doc.insertBefore(xslRef, searchParmsElement);

		XmlUtil.setChildText(searchParmsElement, "currentPath", currentPath, false);
		XmlUtil.setChildText(searchParmsElement, "relativePath", relativePath, false);
		
		Date now = new Date();
		
		Element currentDateElement = doc.createElement("currentDate");
			
		searchParmsElement.appendChild(currentDateElement);
		
		XmlUtil.setChildText(currentDateElement, "year", Integer.toString(now.getYear() + 1900));	
		XmlUtil.setChildText(currentDateElement, "month", Integer.toString(now.getMonth() + 1));	
		XmlUtil.setChildText(currentDateElement, "day", Integer.toString(now.getDate()));	
			
        String googleMapsAPIKey = null;
		if (req.getScheme().equalsIgnoreCase("https")) {
			googleMapsAPIKey = WebFileSysConfig.getInstance().getGoogleMapsAPIKeyHTTPS();
		} else {
			googleMapsAPIKey = WebFileSysConfig.getInstance().getGoogleMapsAPIKeyHTTP();
		}
		
		if (!CommonUtils.isEmpty(googleMapsAPIKey)) {
			XmlUtil.setChildText(searchParmsElement, "googleMapsAPIKey", googleMapsAPIKey, false);
		}
		
		this.processResponse("searchGPSParms.xsl");
    }
}