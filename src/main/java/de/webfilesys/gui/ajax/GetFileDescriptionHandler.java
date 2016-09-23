package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import de.webfilesys.MetaInfManager;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class GetFileDescriptionHandler extends XmlRequestHandlerBase {
	public GetFileDescriptionHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid) {
        super(req, resp, session, output, uid);
	}
	
	protected void process() {
		String filePath = getParameter("filePath");

		if (filePath == null) {
			String fileName = getParameter("fileName");
			if (fileName != null) {
				String cwdPath = getCwd();
				if (cwdPath != null) {
					if (cwdPath.endsWith(File.separator)) {
						filePath = cwdPath + fileName;
					} else {
						filePath = cwdPath + File.separatorChar + fileName;
					}
				}
			}
		}

        String description = null;
        
        if (!accessAllowed(filePath)) {
			Logger.getLogger(getClass()).warn("unauthorized access to path " + filePath);
        } else {
        	description = MetaInfManager.getInstance().getDescription(filePath);
        }

        Element resultElement = doc.createElement("result");
        
        XmlUtil.setElementText(resultElement, (description == null ? "" : description));
        
        doc.appendChild(resultElement);
		
		processResponse();
	}
}
