package de.webfilesys.gui.admin;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.webfilesys.gui.ProtectedRequestHandler;

/**
 * @author Frank Hoehnel
 *
 */
public class AdminRequestHandler extends ProtectedRequestHandler
{
	public AdminRequestHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid)
	{
        super(req, resp, session, output, uid);
	}
	
	public void handleRequest()
	{
		if (!isAdminUser(true))
		{
			return;
		}
		
		process();
	}
}
