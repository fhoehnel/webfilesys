package de.webfilesys.gui.user;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.webfilesys.MetaInfManager;
import de.webfilesys.gui.xsl.XslThumbnailHandler;

/**
 * @author Frank Hoehnel
 */
public class DeleteImageLinkHandler extends UserRequestHandler
{
	protected boolean clientIsLocal = false;

	public DeleteImageLinkHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid,
	        boolean clientIsLocal)
	{
        super(req, resp, session, output, uid);

		this.clientIsLocal = clientIsLocal;
	}

	protected void process()
	{
		if (!checkWriteAccess())
		{
			return;
		}

		String linkName = getParameter("linkName");

		String actPath = getCwd();

		if (!checkAccess(actPath))
		{
			return;
		}

		MetaInfManager.getInstance().removeLink(actPath, linkName);

	    (new XslThumbnailHandler(req, resp, session, output, uid, clientIsLocal)).handleRequest(); 
	}
}
