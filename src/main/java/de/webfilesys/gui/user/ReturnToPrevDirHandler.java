package de.webfilesys.gui.user;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.webfilesys.FastPathManager;

/**
 * @author Frank Hoehnel
 */
public class ReturnToPrevDirHandler extends UserRequestHandler
{
	protected HttpServletRequest req = null;

	protected HttpServletResponse resp = null;
	
    protected boolean clientIsLocal = false;

	public ReturnToPrevDirHandler(
			HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid,
            boolean clientIsLocal)
	{
        super(req, resp, session, output, uid);

        this.req = req;
        
        this.resp = resp;
        
        this.clientIsLocal = clientIsLocal;
	}

	protected void process()
	{
        String lastVisitedDir = FastPathManager.getInstance().returnToPreviousDir(uid);
        
        if (lastVisitedDir != null)
        {
            setParameter("actPath", lastVisitedDir);
        }
        
        setParameter("fastPath", "true");
        
        (new MainFrameSetHandler(req, resp, session, output, uid, clientIsLocal)).handleRequest();
	}
}
