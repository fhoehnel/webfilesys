package de.webfilesys.gui.user;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.webfilesys.Constants;
import de.webfilesys.MetaInfManager;
import de.webfilesys.gui.xsl.XslFileListStatsHandler;

/**
 * Shows the statistcis for a directory tree.
 * @author Frank Hoehnel
 */
public class ResetStatisticsRequestHandler extends UserRequestHandler
{
	private HttpServletRequest req = null; 
	private HttpServletResponse resp = null;
    private String uid = null;
	
	public ResetStatisticsRequestHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid)
	{
        super(req, resp, session, output, uid);
        
        this.req = req;
        this.resp = resp;
        this.uid = uid;
	}

	protected void process()
	{
		if (!checkWriteAccess())
		{
			return;
		}

		session.setAttribute("viewMode", new Integer(Constants.VIEW_MODE_STATS));

		String actPath = getParameter("actPath");

		MetaInfManager.getInstance().resetStatistics(actPath);

		setParameter("mask", "*");

		(new XslFileListStatsHandler(req, resp, session, output, uid)).handleRequest();
	}

}
