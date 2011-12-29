package de.webfilesys.gui.user;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.MetaInfManager;
import de.webfilesys.WebFileSys;
import de.webfilesys.graphics.ThumbnailThread;

/**
 * @author Frank Hoehnel
 */
public class DeleteImageRequestHandler extends UserRequestHandler
{
	public DeleteImageRequestHandler(
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

		output.println("<HTML>");
		output.println("<HEAD>");

		File imgFile=new File(imgFileName);
		if (!imgFile.exists())
		{
			javascriptAlert("Image file does not exist !");
		}
		else
		{
			if ((!imgFile.canWrite()) || (!imgFile.delete()))
			{
				javascriptAlert(getResource("alert.delete.failed","Cannot delete image file:") + "\\n" + insertDoubleBackslash(imgFileName));
			}
            else
            {
                MetaInfManager metaInfMgr = MetaInfManager.getInstance();
                
                if (WebFileSys.getInstance().isReverseFileLinkingEnabled())
                {
                    metaInfMgr.updateLinksAfterMove(imgFileName, null, uid);
                }
                
                metaInfMgr.removeMetaInf(imgFileName);
            	
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

		output.println("<SCRIPT LANGUAGE=\"JavaScript\">");
		output.println("function closeSelf() {self.close();} </SCRIPT>");
		output.println("</HEAD>"); 
		output.println("<body onload=\"closeSelf()\"> </body></html>");
		output.flush();
	}
}
