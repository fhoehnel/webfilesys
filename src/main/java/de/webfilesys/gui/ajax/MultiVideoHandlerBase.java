package de.webfilesys.gui.ajax;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author Frank Hoehnel
 */
public abstract class MultiVideoHandlerBase extends XmlRequestHandlerBase {
	
	public MultiVideoHandlerBase(HttpServletRequest req, HttpServletResponse resp, HttpSession session,
			PrintWriter output, String uid) {
		super(req, resp, session, output, uid);
	}

	// moved to ProtectedRequestHandler
	/*
	protected List<String> getSelectedFiles() {
		ArrayList<String> selectedFiles = new ArrayList<String>();

        Enumeration allKeys = req.getParameterNames();
		
		while (allKeys.hasMoreElements()) {
			String paramKey =(String) allKeys.nextElement();

            if (paramKey.startsWith(LIST_PREFIX)) {
				selectedFiles.add(paramKey.substring(LIST_PREFIX_LENGTH)); 
            }
		}
		return selectedFiles;
	}
	*/
	
}
