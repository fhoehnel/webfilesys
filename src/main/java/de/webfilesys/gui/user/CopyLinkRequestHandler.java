package de.webfilesys.gui.user;

import java.io.File;
import java.io.PrintWriter;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.Constants;
import de.webfilesys.FileComparator;
import de.webfilesys.FileContainer;
import de.webfilesys.FileLinkSelector;
import de.webfilesys.FileSelectionStatus;
import de.webfilesys.MetaInfManager;
import de.webfilesys.gui.xsl.XslFileListHandler;
import de.webfilesys.gui.xsl.XslThumbnailHandler;

/**
 * Replace all file links in the current directory by a copy of the linked original file.
 * 
 * @author Frank Hoehnel
 */
public class CopyLinkRequestHandler extends UserRequestHandler
{
	protected HttpServletRequest req = null;

	protected HttpServletResponse resp = null;
	
	protected boolean clientIsLocal = false;

	public CopyLinkRequestHandler(
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

		String actPath = getParameter("actPath");

		if ((actPath == null) || (actPath.length() == 0))
		{
			actPath = getCwd();
		}

		if (!checkAccess(actPath))
		{
			return;
		}

		String[] fileMasks = null;
		
		Integer viewMode = (Integer) session.getAttribute("viewMode");
		
		if ((viewMode != null) && (viewMode.intValue() == Constants.VIEW_MODE_THUMBS))
		{
		    fileMasks = Constants.imgFileMasks;
		}
		else
		{
		    fileMasks = new String[1];
		    fileMasks[0] = "*";
		}
		
		FileLinkSelector fileSelector = new FileLinkSelector(actPath, FileComparator.SORT_BY_FILENAME, true);

		FileSelectionStatus selectionStatus = fileSelector.selectFiles(Constants.imgFileMasks, Constants.MAX_FILE_NUM, 0);

		Vector selectedFiles = selectionStatus.getSelectedFiles();

		if (selectedFiles != null)
		{
            MetaInfManager metaInfMgr = MetaInfManager.getInstance();
			
			for (int i = 0; i < selectedFiles.size(); i++)
			{
				FileContainer fileCont = (FileContainer) selectedFiles.elementAt(i);
				
				if (fileCont.isLink())
				{
					String linkName = fileCont.getName();

					String targetPath = null;
					
					if (actPath.endsWith(File.separator))
					{
						targetPath = actPath + linkName;
					}
					else
					{
						targetPath = actPath + File.separatorChar + linkName;
					}
					
					if (copy_file(fileCont.getRealFile().getAbsolutePath(), targetPath, false))
					{
						metaInfMgr.removeLink(actPath, linkName);
						Logger.getLogger(getClass()).debug("link " + linkName + " replaced by a copy of original file " + fileCont.getRealFile().getAbsolutePath());
					}
					else
					{
						Logger.getLogger(getClass()).error("failed to replace link " + linkName + " by a copy of original file " + fileCont.getRealFile().getAbsolutePath());
					}
				}
			}
		}

        if ((viewMode != null) && (viewMode.intValue() == Constants.VIEW_MODE_THUMBS))
        {
            (new XslThumbnailHandler(req, resp, session, output, uid, clientIsLocal)).handleRequest(); 
        }
        else
        {
            (new XslFileListHandler(req, resp, session, output, uid, clientIsLocal)).handleRequest(); 
        }
	}
}
