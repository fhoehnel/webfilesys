package de.webfilesys.gui.user;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.webfilesys.graphics.ThumbnailThread;

/**
 * @author Frank Hoehnel
 */
public class GetThumbRequestHandler extends GetFileRequestHandler
{
	protected HttpServletResponse resp = null;
	
	public GetThumbRequestHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid)
	{
        super(req, resp, session, output, uid);
        
		String imgFile = req.getParameter("imgFile");

		if (imgFile != null)
		{
			String filePath = ThumbnailThread.getThumbnailPath(imgFile);
			
			setParameter("filePath", filePath);
		}
	}
}
