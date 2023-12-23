package de.webfilesys.gui.user;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


import de.webfilesys.ClipBoard;
import de.webfilesys.Constants;
import de.webfilesys.FileLink;
import de.webfilesys.MetaInfManager;
import de.webfilesys.gui.xsl.XslFileListHandler;
import de.webfilesys.gui.xsl.XslThumbnailHandler;
import de.webfilesys.gui.xsl.mobile.MobileFolderFileListHandler;

/**
 * @author Frank Hoehnel
 */
public class PasteAsLinkRequestHandler extends UserRequestHandler
{
	protected HttpServletRequest req = null;

	protected HttpServletResponse resp = null;
	
	protected boolean clientIsLocal = false;

	public PasteAsLinkRequestHandler(
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

		String actPath=getParameter("actpath");

		if ((actPath == null) || (actPath.length() == 0))
		{
			actPath = getCwd();
		}

		if (!checkAccess(actPath))
		{
			return;
		}

		ClipBoard clipBoard = (ClipBoard) session.getAttribute("clipBoard");
		
		if (clipBoard == null)
		{
			LogManager.getLogger(getClass()).warn("clipboard is empty in paste operation");

		    return;
		}

		ArrayList<String> clipFiles = clipBoard.getAllFiles();

		if (clipFiles!=null)
		{
		    MetaInfManager metaInfMgr = MetaInfManager.getInstance();
		
			String destDir=actPath;

			if (!destDir.endsWith(File.separator))
			{
				destDir=destDir + File.separator;
			}

			for (String sourceFile : clipFiles)
			{
				int lastSepIdx=sourceFile.lastIndexOf(File.separatorChar);
                
                try
                {
					metaInfMgr.createLink(actPath, new FileLink(sourceFile.substring(lastSepIdx+1), sourceFile, uid));
                }
                catch (FileNotFoundException fnfex)
                {
                	LogManager.getLogger(getClass()).warn(fnfex.toString());
                }
			}
		}

		int viewMode = Constants.VIEW_MODE_LIST;
    	
	    Integer sessionViewMode = (Integer) session.getAttribute("viewMode");
	    
	    if (sessionViewMode != null)
	    {
	    	viewMode = sessionViewMode.intValue();
	    }
    	
        String mobile = (String) session.getAttribute("mobile");
        
        if (mobile == null) 
        {
            if (viewMode == Constants.VIEW_MODE_THUMBS)
            {
                (new XslThumbnailHandler(req, resp, session, output, uid, clientIsLocal)).handleRequest(); 

            }
            else
            {
                (new XslFileListHandler(req, resp, session, output, uid)).handleRequest();
            }
        }
        else
        {
            (new MobileFolderFileListHandler(req, resp, session, output, uid)).handleRequest(); 
        }
	}

}
