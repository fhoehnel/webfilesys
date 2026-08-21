package de.webfilesys.gui.admin;
import java.io.PrintWriter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

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
