package de.webfilesys.gui.user;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.webfilesys.gui.xsl.XslMultiDownloadPromptHandler;

/**
 * @author Frank Hoehnel
 */
public class MultiFileDownloadPromptHandler extends MultiFileRequestHandler
{
	HttpServletRequest req = null;
	HttpServletResponse resp = null;

	public MultiFileDownloadPromptHandler(
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

	public void process()
	{
		(new XslMultiDownloadPromptHandler(req, resp, session, output, uid))
			.handleRequest();
	}
}
