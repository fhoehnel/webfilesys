package de.webfilesys.gui.user;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.webfilesys.gui.ajax.XmlSelectDiffFileHandler;

/**
 * @author Frank Hoehnel
 */
public class DiffCompareHandler extends UserRequestHandler
{
	public DiffCompareHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid)
	{
        super(req, resp, session, output, uid);
	}

	protected void process()
	{
        String diffSourceFile = (String) session.getAttribute(XmlSelectDiffFileHandler.SESSION_ATTRIB_DIFF_SOURCE);
        String diffTargetPath = (String) session.getAttribute(XmlSelectDiffFileHandler.SESSION_ATTRIB_DIFF_TARGET);
	    
        setParameter("file1Path", diffSourceFile);
        setParameter("file2Path", diffTargetPath);
        
        (new DiffCompareBase(req, resp, session, output, uid)).handleRequest(); 
        
        session.removeAttribute(XmlSelectDiffFileHandler.SESSION_ATTRIB_DIFF_SOURCE);
        session.removeAttribute(XmlSelectDiffFileHandler.SESSION_ATTRIB_DIFF_TARGET);
	}
}
