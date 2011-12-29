package de.webfilesys.gui.admin;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.webfilesys.FastPathManager;
import de.webfilesys.FileSysBookmarkManager;

/**
 * @author Frank Hoehnel
 */
public class DeleteUserRequestHandler extends AdminRequestHandler
{
	public DeleteUserRequestHandler(
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
		String userToBeDeleted = getParameter("userToBeDeleted");

		if ((userToBeDeleted!=null) && (userToBeDeleted.length()>0))
		{
			userMgr.removeUser(userToBeDeleted);
			FastPathManager.getInstance().deleteUser(userToBeDeleted);
            FileSysBookmarkManager.getInstance().deleteUser(userToBeDeleted);
		}

		(new UserListRequestHandler(req, resp, session, output, uid)).handleRequest(); 
	}

}
