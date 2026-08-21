package de.webfilesys.gui.user;

import java.io.PrintWriter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

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
