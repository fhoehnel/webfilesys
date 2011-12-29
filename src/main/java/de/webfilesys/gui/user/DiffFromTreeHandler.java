package de.webfilesys.gui.user;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author Frank Hoehnel
 */
public class DiffFromTreeHandler extends UserRequestHandler
{
	public DiffFromTreeHandler(
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
        String diffSourceFile = req.getParameter("sourceFile");
        String diffTargetPath = req.getParameter("targetFile");
	    
        setParameter("file1Path", diffSourceFile);
        setParameter("file2Path", diffTargetPath);
        
        (new DiffCompareBase(req, resp, session, output, uid)).handleRequest(); 
	}
}
