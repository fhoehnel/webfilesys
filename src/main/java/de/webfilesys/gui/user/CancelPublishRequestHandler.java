package de.webfilesys.gui.user;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.webfilesys.InvitationManager;

/**
 * @author Frank Hoehnel
 */
public class CancelPublishRequestHandler extends UserRequestHandler
{
	public CancelPublishRequestHandler(
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
        if (!checkWriteAccess())
        {
            return;
        }

        String accessCode = getParameter("accessCode");

        if ((accessCode == null) || (accessCode.trim().length() == 0))
        {
            return;
        }

        InvitationManager.getInstance().removeInvitation(accessCode);

	    (new PublishListRequestHandler(req, resp, session, output, uid)).handleRequest(); 
	}

}
