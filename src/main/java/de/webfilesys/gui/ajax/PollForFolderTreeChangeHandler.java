package de.webfilesys.gui.ajax;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;

import de.webfilesys.Constants;
import de.webfilesys.DirTreeStatus;
import de.webfilesys.DirTreeStatusInspector;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class PollForFolderTreeChangeHandler extends XmlRequestHandlerBase
{
	public PollForFolderTreeChangeHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid) {
        super(req, resp, session, output, uid);
	}
	
	protected void process() {
        boolean modified = false;
        
		DirTreeStatus dirTreeStatus = (DirTreeStatus) session.getAttribute(Constants.SESSION_KEY_DIR_TREE_STATUS);
		
		if (dirTreeStatus != null) {
			
			modified = (new DirTreeStatusInspector(dirTreeStatus)).isFolderTreeStructureChanged();
		}
        
        Element resultElement = doc.createElement("result");

        XmlUtil.setElementText(resultElement, Boolean.toString(modified));
        
        doc.appendChild(resultElement);
		
		processResponse();
	}
}
