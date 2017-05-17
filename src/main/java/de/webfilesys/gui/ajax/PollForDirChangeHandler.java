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
        String lastSizeSumParam = getParameter("lastSizeSum");
        
        long lastDirStatusTime;
        long lastSizeSum;
        
        try {
        	lastDirStatusTime = Long.parseLong(lastDirStatusTimeParam);
        	lastSizeSum = Long.parseLong(lastSizeSumParam);
        } catch (NumberFormatException numEx) {
            Logger.getLogger(getClass()).error("invalid parameter: lastDirStatusTime: " + lastDirStatusTimeParam + " lastSizeSum: " + lastSizeSumParam);
            return;
        }
        
        File dirFile = new File(currentPath);
        
        long lastModified = dirFile.lastModified();
        
        long currentSizeSum = 0l;
        
        File[] files = dirFile.listFiles();
        if (files != null) {
            for (File file : files) {
            	if (file.isFile()) {
            		currentSizeSum += file.length();
            	}
            }
        }
        
        Element resultElement = doc.createElement("result");

        boolean modified = (lastModified > lastDirStatusTime) || (currentSizeSum != lastSizeSum);
        
        XmlUtil.setElementText(resultElement, Boolean.toString(modified));
        
        doc.appendChild(resultElement);
		
		processResponse();
	}
}
