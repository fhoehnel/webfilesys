package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.PrintWriter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Element;

import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class AjaxCheckFolderExistHandler extends XmlRequestHandlerBase {
	public AjaxCheckFolderExistHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid) {
        super(req, resp, session, output, uid);
	}
	
	protected void process() {
        String path = getParameter("param1");
        
        boolean pathInvalid = false;
        
        if (CommonUtils.isEmpty(path)) {
            pathInvalid = true;
        } else {
            if ((File.separatorChar == '\\') && (path.indexOf('/') >= 0)) {
            	pathInvalid = true;
            } else {
                if (!accessAllowed(path)) {
        			LogManager.getLogger(getClass()).warn("unauthorized access to path " + path);
                	pathInvalid = true;
                } else {
                    File fileToCheck = new File(path);
                    if ((!fileToCheck.exists()) || (!fileToCheck.isDirectory()) || (!fileToCheck.canRead())) {
                    	pathInvalid = true;
                    }
                }
            }
        }

        Element resultElement = doc.createElement("result");
        
        XmlUtil.setElementText(resultElement, Boolean.toString(!pathInvalid));
        
        doc.appendChild(resultElement);
		
		processResponse();
	}
}
