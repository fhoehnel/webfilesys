package de.webfilesys.gui.user;

import java.io.PrintWriter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

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
