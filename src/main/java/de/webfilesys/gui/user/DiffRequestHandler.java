package de.webfilesys.gui.user;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author Frank Hoehnel
 */
public class DiffRequestHandler extends MultiFileRequestHandler
{
	public DiffRequestHandler(
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
        String file1Path = actPath + File.separator + selectedFiles.elementAt(1);
        String file2Path = actPath + File.separator + selectedFiles.elementAt(0);

        setParameter("file1Path", file1Path);
        setParameter("file2Path", file2Path);
        
        (new DiffCompareBase(req, resp, session, output, uid)).handleRequest(); 
    }
	
}
