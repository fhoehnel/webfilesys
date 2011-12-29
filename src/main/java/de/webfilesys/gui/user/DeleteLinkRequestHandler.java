package de.webfilesys.gui.user;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.webfilesys.MetaInfManager;
import de.webfilesys.gui.xsl.XslFileListHandler;
import de.webfilesys.gui.xsl.mobile.MobileFolderFileListHandler;

/**
 * @author Frank Hoehnel
 */
public class DeleteLinkRequestHandler extends UserRequestHandler
{
	protected HttpServletRequest req = null;

	protected HttpServletResponse resp = null;

	public DeleteLinkRequestHandler(
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

		String linkName = getParameter("linkName");

        String actPath = getCwd();

		if (!checkAccess(actPath))
		{
			return;
		}

        MetaInfManager.getInstance().removeLink(actPath, linkName);
        
		this.setParameter("initial","true");
		
        String mobile = (String) session.getAttribute("mobile");
        
        if (mobile != null) 
        {
            (new MobileFolderFileListHandler(req, resp, session, output, uid)).handleRequest(); 
        }
        else
        {
            (new XslFileListHandler(req, resp, session, output, uid, false)).handleRequest();
        }
	}
}
