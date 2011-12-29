package de.webfilesys.gui.user;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.webfilesys.MetaInfManager;
import de.webfilesys.gui.xsl.XslListCommentsHandler;

/**
 * @author Frank Hoehnel
 */
public class DeleteCommentsRequestHandler extends UserRequestHandler
{
	protected HttpServletRequest req = null;

	protected HttpServletResponse resp = null;

	public DeleteCommentsRequestHandler(
			HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid)
	{
        super(req, resp, session, output, uid);

        this.req = req;
        
        this.resp = resp;
	}

	protected void process()
	{
		if (!checkWriteAccess())
		{
			return;
		}

		String actPath=getParameter("actPath");
		
		if (!checkAccess(actPath))
		{
			return;
		}

		MetaInfManager.getInstance().removeComments(actPath);

		(new XslListCommentsHandler(req, resp, session, output, uid)).handleRequest();
	}

}
