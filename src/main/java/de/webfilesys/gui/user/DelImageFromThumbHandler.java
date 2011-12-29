package de.webfilesys.gui.user;

import java.io.File;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.MetaInfManager;
import de.webfilesys.graphics.ThumbnailThread;
import de.webfilesys.gui.xsl.XslThumbnailHandler;

/**
 * @author Frank Hoehnel
 */
public class DelImageFromThumbHandler extends UserRequestHandler
{
	protected boolean clientIsLocal = false;
	
	protected HttpServletRequest req = null;

	protected HttpServletResponse resp = null;
	
	public DelImageFromThumbHandler(
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
		if (!checkWriteAccess())
		{
			return;
		}

		String imgFileName=getParameter("imgName");

		if (!accessAllowed(imgFileName))
		{
			Logger.getLogger(getClass()).warn("user " + uid + " tried to delete file outside of it's document root: " + imgFileName);
			return;
		}

		if (File.separatorChar=='\\')
		{
			imgFileName=imgFileName.replace('/','\\');
		}

		String alertText=null;

		File imgFile=new File(imgFileName);
		if (!imgFile.exists())
		{
			alertText="Image file does not exist !";
		}
		else
		{
			if ((!imgFile.canWrite()) || (!imgFile.delete()))
			{
				alertText=getResource("alert.delete.failed","Cannot delete image file:") + "\\n" + insertDoubleBackslash(imgFileName);
			}
			else
			{
				MetaInfManager.getInstance().removeMetaInf(imgFileName);
				
				String thumbnailPath = ThumbnailThread.getThumbnailPath(imgFileName);
				
				File thumbnailFile = new File(thumbnailPath);
				
				if (thumbnailFile.exists())
				{
					if (!thumbnailFile.delete())
					{
						Logger.getLogger(getClass()).debug("cannot remove thumbnail file " + thumbnailPath);
					}
				}
			}
		}

		if (alertText!=null)
		{
			output.println("<HTML>");
			output.println("<HEAD>");

			javascriptAlert(alertText);

			output.println("<META HTTP-EQUIV=\"REFRESH\" CONTENT=\"0; URL=/webfilesys/servlet?command=thumbnail&zoom=no&random=" + (new Date()).getTime() + "\">");

			output.println("</head></html>");
			output.flush();
			return;
		}

	    (new XslThumbnailHandler(req, resp, session, output, uid, clientIsLocal)).handleRequest(); 
	}
}
