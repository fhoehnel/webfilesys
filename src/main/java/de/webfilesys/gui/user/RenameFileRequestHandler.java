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
import de.webfilesys.gui.xsl.XslFileListHandler;
import de.webfilesys.gui.xsl.mobile.MobileFolderFileListHandler;
import de.webfilesys.util.CommonUtils;

/**
 * @author Frank Hoehnel
 */
public class RenameFileRequestHandler extends UserRequestHandler
{
	protected HttpServletRequest req = null;

	protected HttpServletResponse resp = null;

	public RenameFileRequestHandler(
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

		String newFileName = getParameter("newFileName");

		String oldFileName=getParameter("fileName");

		String path = getCwd();

		String oldFilePath=null;

		String newFilePath=null;

		if (path.endsWith(File.separator))
		{
			oldFilePath=path + oldFileName;     

			newFilePath=path + newFileName;     
		}
		else
		{
			oldFilePath=path + File.separator + oldFileName;

			newFilePath=path + File.separator + newFileName;
		}

		if (!checkAccess(oldFilePath))
		{
			return;
		}
		
        String mobile = getParameter("mobile");

		File source=new File(oldFilePath);

		File dest=new File(newFilePath);

		if ((newFileName.indexOf("..") >= 0) || (!source.renameTo(dest)))
		{
			output.println("<html>");
			output.println("<head>");
			output.println("<script language=\"javascript\">");

			String errorMsg=insertDoubleBackslash(oldFilePath) + "\\n" 
							+ getResource("error.renameFailed","could not be renamed to")
							+ "\\n" + insertDoubleBackslash(newFilePath);

			output.println("alert('" + errorMsg + "');");
			
			if (mobile != null) 
			{
                output.println("window.location.href='/webfilesys/servlet?command=mobile&cmd=folderFileList';");
			}
			else
			{
	            output.println("window.location.href='/webfilesys/servlet?command=listFiles&keepListStatus=true';");
			}
			output.println("</script>");
			output.println("</head>");
			output.println("</html>");
			output.flush();
			return;
		}

		MetaInfManager metaInfMgr=MetaInfManager.getInstance();

		String description=metaInfMgr.getDescription(oldFilePath);

		if ((description!=null) && (description.trim().length()>0))
		{
			metaInfMgr.setDescription(newFilePath,description);
		}
		
		Vector assignedCategories = metaInfMgr.getListOfCategories(oldFilePath);
		
		if (assignedCategories != null)
		{
			for (int i=0;i<assignedCategories.size();i++)
			{
				Category cat = (Category) assignedCategories.elementAt(i);
				
				metaInfMgr.addCategory(newFilePath, cat);
			}
		}

        if (WebFileSys.getInstance().isReverseFileLinkingEnabled())
        {
            metaInfMgr.updateLinksAfterMove(oldFilePath, newFilePath, uid);
        }
		
		metaInfMgr.removeMetaInf(oldFilePath);
		
		String thumbnailPath = ThumbnailThread.getThumbnailPath(oldFilePath);
				
		File thumbnailFile = new File(thumbnailPath);
				
		if (thumbnailFile.exists())
		{
			if (!thumbnailFile.delete())
			{
				Logger.getLogger(getClass()).debug("cannot remove thumbnail file " + thumbnailPath);
			}
		}

		setParameter("actpath", getCwd());

		setParameter("mask","*");
		
        if (mobile == null) 
        {
            (new XslFileListHandler(req, resp, session, output, uid, false)).handleRequest();
        }
        else
        {
            (new MobileFolderFileListHandler(req, resp, session, output, uid)).handleRequest(); 
        }
		
		if (WebFileSys.getInstance().isAutoCreateThumbs())
		{
			String ext = CommonUtils.getFileExtension(newFilePath);

			if (ext.equals(".jpg") || ext.equals(".jpeg") || (ext.equals("png")))
			{
				AutoThumbnailCreator.getInstance().queuePath(newFilePath, AutoThumbnailCreator.SCOPE_FILE);
			}
		}
	}
}
