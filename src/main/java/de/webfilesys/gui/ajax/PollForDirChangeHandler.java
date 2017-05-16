package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class PollForDirChangeHandler extends XmlRequestHandlerBase
{
	public PollForDirChangeHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid) {
        super(req, resp, session, output, uid);
	}
	
	protected void process() {
        String currentPath = getCwd();

        String lastDirStatusTimeParam = getParameter("lastDirStatusTime");
        
        long lastDirStatusTime;
        
        try {
        	lastDirStatusTime = Long.parseLong(lastDirStatusTimeParam);
        } catch (NumberFormatException numEx) {
            Logger.getLogger(getClass()).error("invalid parameter lastDirStatusTime: " + lastDirStatusTimeParam);
            return;
        }
        
        File dirFile = new File(currentPath);
        
        long lastModified = dirFile.lastModified();
        
        Element resultElement = doc.createElement("result");

        XmlUtil.setElementText(resultElement, Boolean.toString(lastModified > lastDirStatusTime));
        
        doc.appendChild(resultElement);
		
		processResponse();
	}
}
