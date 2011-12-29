package de.webfilesys.gui.user;

import java.io.File;
import java.io.PrintWriter;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.Category;
import de.webfilesys.MetaInfManager;
import de.webfilesys.WebFileSys;
import de.webfilesys.graphics.AutoThumbnailCreator;
import de.webfilesys.graphics.ThumbnailThread;
import de.webfilesys.gui.xsl.XslThumbnailHandler;
import de.webfilesys.util.CommonUtils;

/**
 * @author Frank Hoehnel
 */
public class RenameImageRequestHandler extends UserRequestHandler
{
	boolean fileNameKnown = false;

	public RenameImageRequestHandler(
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

		String newFileName = getParameter("newFileName");
		
		if (newFileName == null)
		{
			Logger.getLogger(getClass()).error("required parameter newFileName missing");
			
			return;
		}
		
		String imagePath=getParameter("imagePath");

		if (!checkAccess(imagePath))
		{
			return;
		}
		
		int lastSepIdx=imagePath.lastIndexOf(File.separatorChar);

		String pathBase="";

		if (lastSepIdx>0)
		{
			pathBase=imagePath.substring(0,lastSepIdx);
		}

		String newImagePath=pathBase + File.separator + newFileName;

		File source = new File(imagePath);

		File dest = new File(newImagePath);

		if ((newFileName.indexOf("..") >= 0) || (!source.renameTo(dest)))
		{
			output.println("<html>");
			output.println("<head>");
			output.println("<script language=\"javascript\">");

			String errorMsg = insertDoubleBackslash(source.getAbsolutePath()) + "\\n" 
							  + getResource("error.renameFailed","could not be renamed to")
							  + "\\n" + insertDoubleBackslash(dest.getAbsolutePath());

			output.println("alert('" + errorMsg + "');");
			output.println("window.location.href='/webfilesys/servlet?command=thumbnail';");
			output.println("</script>");
			output.println("</head>");
			output.println("</html>");
			output.flush();
			return;
		}
		
		MetaInfManager metaInfMgr=MetaInfManager.getInstance();

		String description=metaInfMgr.getDescription(imagePath);

		if ((description!=null) && (description.trim().length()>0))
		{
			metaInfMgr.setDescription(newImagePath,description);
		}

		Vector assignedCategories = metaInfMgr.getListOfCategories(imagePath);
	
		if (assignedCategories != null)
		{
			for (int i=0;i<assignedCategories.size();i++)
			{
				Category cat = (Category) assignedCategories.elementAt(i);
			
				metaInfMgr.addCategory(newImagePath, cat);
			}
		}

        if (WebFileSys.getInstance().isReverseFileLinkingEnabled())
        {
            metaInfMgr.updateLinksAfterMove(imagePath, newImagePath, uid);
        }
		
		metaInfMgr.removeMetaInf(imagePath);
		
		String thumbnailPath = ThumbnailThread.getThumbnailPath(imagePath);
			
		File thumbnailFile = new File(thumbnailPath);
			
		if (thumbnailFile.exists())
		{
			if (!thumbnailFile.delete())
			{
				Logger.getLogger(getClass()).debug("cannot remove thumbnail file " + thumbnailPath);
			}
		}

		if (WebFileSys.getInstance().isAutoCreateThumbs())
		{
			String ext = CommonUtils.getFileExtension(newImagePath);

			if (ext.equals(".jpg") || ext.equals(".jpeg") || (ext.equals("png")))
			{
				AutoThumbnailCreator.getInstance().queuePath(newImagePath, AutoThumbnailCreator.SCOPE_FILE);
			}
		}

		(new XslThumbnailHandler(req, resp, session, output, uid, false)).handleRequest();
	}
}
