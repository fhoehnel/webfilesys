package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import de.webfilesys.graphics.ThumbnailThread;
import de.webfilesys.gui.user.ZipDirRequestHandler;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class TestSubdirExistHandler extends XmlRequestHandlerBase {
	
    private static final Logger LOG = Logger.getLogger(TestSubdirExistHandler.class);
	
	public TestSubdirExistHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid) {
        super(req, resp, session, output, uid);
	}
	
	protected void process() {
        String path = getParameter("path");

        if (CommonUtils.isEmpty(path)) {
        	LOG.error("parameter path missing");
        	return;
        }
        
		if (!checkAccess(path)) {
			return;
		}
        
		boolean subdirExists = false;
		
        File folder = new File(path);
        
        if (folder.exists() && folder.isDirectory() && folder.canRead()) {
        	File[] files = folder.listFiles();
        	if (files != null) {
            	for (int i = 0; (!subdirExists) && (i < files.length); i++) {
            	    if (files[i].isDirectory()) {
						if (!files[i].getName().equals(ThumbnailThread.THUMBNAIL_SUBDIR)) {
            	    	    subdirExists = true;
						}
            	    }
            	}
        	}
        } else {
        	LOG.warn("folder to check for subdirs is not a readable directory: " + path);
        }
        	
        Element resultElement = doc.createElement("result");
        
        XmlUtil.setElementText(resultElement, Boolean.toString(subdirExists));
        
        doc.appendChild(resultElement);
		
		processResponse();
	}
}
