package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
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
        
        if (CommonUtils.isEmpty(path)) {
            return;
        }
        
        boolean invalidPathSeparator = false;
        
        if ((File.separatorChar == '\\') && (path.indexOf('/') >= 0)) {
        	invalidPathSeparator = true;
        }
        
        if (!accessAllowed(path)) {
			Logger.getLogger(getClass()).warn("unauthorized access to path " + path);
			return;
        }
        
        File fileToCheck = new File(path);
        
        Element resultElement = doc.createElement("result");
        
        boolean dirExists = (!invalidPathSeparator) && fileToCheck.exists() && fileToCheck.isDirectory() && fileToCheck.canRead();
        
        XmlUtil.setElementText(resultElement, Boolean.toString(dirExists));
        
        doc.appendChild(resultElement);
		
		processResponse();
	}
}
