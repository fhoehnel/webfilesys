package de.webfilesys.gui.admin;

import java.io.PrintWriter;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.webfilesys.mail.Email;

/**
 * Send an email to users of a given role or to all users.
 * @author Frank Hoehnel
 */
public class AdminSendEmailRequestHandler extends AdminRequestHandler
{
    public AdminSendEmailRequestHandler(
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
    	String errorMsg = null;
    	
        String receiverRole = getParameter("role");

        if ((receiverRole == null) || (receiverRole.trim().length() == 0))
        {
            receiverRole = "all";
        }

        String subject = getParameter("subject");

        if ((subject == null) || (subject.trim().length() == 0))
        {
            errorMsg = "subject is a required field";
        }

        String content = getParameter("content");

        if ((content == null) || (content.trim().length() == 0))
        {
            errorMsg = "the e-mail has no content";
        }

        if (errorMsg != null)
        {
			(new BroadcastRequestHandler(req, resp, session, output, uid, errorMsg)).handleRequest(); 
			
			return;
        }
        
        Vector mailReceivers = null;

        if (receiverRole.equals("all"))
        {
            mailReceivers = userMgr.getAllMailAddresses();
        }
        else
        {
            mailReceivers = userMgr.getMailAddressesByRole(receiverRole);
        }

        (new Email(mailReceivers, subject, content)).send();

		(new AdminMenuRequestHandler(req, resp, session, output, uid)).handleRequest(); 
    }
}
