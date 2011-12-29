package de.webfilesys.gui.user;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.Constants;
import de.webfilesys.MetaInfManager;
import de.webfilesys.SystemCmdParms;
import de.webfilesys.WebFileSys;
import de.webfilesys.graphics.ThumbnailThread;
import de.webfilesys.gui.xsl.XslFileListHandler;
import de.webfilesys.gui.xsl.XslThumbnailHandler;
import de.webfilesys.gui.xsl.mobile.MobileFolderFileListHandler;

/**
 * @author Frank Hoehnel
 */
public class DeleteFileRequestHandler extends UserRequestHandler
{
	protected boolean confirmed = false;
	
    boolean clientIsLocal = false;
	
	protected HttpServletRequest req = null;

	protected HttpServletResponse resp = null;
	
	public DeleteFileRequestHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid,
            boolean clientIsLocal,
	        boolean confirmed)
	{
        super(req, resp, session, output, uid);
        
        this.req = req;
        
        this.resp = resp;
		
		this.confirmed = confirmed;
		
        this.clientIsLocal = clientIsLocal;
	}

	protected void process()
	{
		if (!checkWriteAccess())
		{
			return;
		}

		String actPath = getCwd();

		String fileName = getParameter("fileName");
        
        String filePath = null;

		if (actPath.endsWith(File.separator))
		{
			filePath = actPath + fileName;
		}
		else
		{
			filePath = actPath + File.separator + fileName;
		}

		if (!accessAllowed(filePath))
		{
			Logger.getLogger(getClass()).warn("user " + uid + " tried to delete file outside of it's document root: " + filePath);
			return;
		}

		File delFile = new File(filePath);

		if (!delFile.canWrite())
		{
            if ((WebFileSys.getInstance().getOpSysType() == WebFileSys.OS_OS2)  ||
                    (WebFileSys.getInstance().getOpSysType() == WebFileSys.OS_WIN))
            {
                SystemCmdParms sys_cmd_parm = new SystemCmdParms("attrib","-R " + delFile);
                sys_cmd_parm.start();
            }
        }
        
        if (!delFile.delete())
        {
            String delDir = getCwd();

            if (File.separatorChar=='\\')
            {
                delDir = insertDoubleBackslash(delDir);
            }

            deleteFailed(fileName, delDir);         
        }
        else
        {
            MetaInfManager metaInfMgr = MetaInfManager.getInstance();

            if (WebFileSys.getInstance().isReverseFileLinkingEnabled())
            {
                metaInfMgr.updateLinksAfterMove(filePath, null, uid);
            }
            
            metaInfMgr.removeMetaInf(actPath,fileName);

            String thumbnailPath = ThumbnailThread.getThumbnailPath(filePath);
            
            File thumbnailFile = new File(thumbnailPath);
            
            if (thumbnailFile.exists())
            {
                if (!thumbnailFile.delete())
                {
                    Logger.getLogger(getClass()).warn("cannot remove thumbnail file " + thumbnailPath);
                }
            }

            String closeWin = req.getParameter("closeWin");
            
            if ((closeWin != null) && closeWin.equals("true"))
            {
                closeWin();
            }
            else
            {
                String mobile = (String) session.getAttribute("mobile");
                
                if (mobile == null) 
                {
                    int viewMode = Constants.VIEW_MODE_LIST;        
                    
                    Integer sessionViewMode = (Integer) session.getAttribute("viewMode");
                    
                    if (sessionViewMode != null)
                    {
                        viewMode = sessionViewMode.intValue();
                    }

                    if (viewMode == Constants.VIEW_MODE_THUMBS)
                    {
                        (new XslThumbnailHandler(req, resp, session, output, uid, clientIsLocal)).handleRequest(); 
                    }
                    else
                    {
                        (new XslFileListHandler(req, resp, session, output, uid, true)).handleRequest();
                    }
                }
                else
                {
                    (new MobileFolderFileListHandler(req, resp, session, output, uid)).handleRequest(); 
                }
            }
            
        }
	}
    
	private void closeWin() {
        output.println("<html>");
        output.println("<head>");

        output.println("<script language=\"javascript\">");
        output.println("window.opener.location.href = '/webfilesys/servlet?command=listFiles&keepListStatus=true';");
        output.println("setTimeout(\"self.close()\", 500);"); 
        output.println("</script>");

        output.println("</head>");
        output.println("</html>");
        output.flush();
	}
	
    private void deleteFailed(String fileName, String delDir)
    {
        output.println("<HTML>");
        output.println("<HEAD>");

        StringBuffer errorMsg = new StringBuffer();
        errorMsg.append(getResource("alert.delFileFailedPart1", "Failed to delete the file"));
        errorMsg.append("\\n");
        errorMsg.append(fileName);
        errorMsg.append("\\n");
        errorMsg.append(getResource("alert.delFileFailedPart2", "in folder"));
        errorMsg.append("\\n");
        
        String relPath = getHeadlinePath(delDir);
        
        if (relPath.startsWith("\\t"))
        {
            relPath = "\\" + relPath;
        }
        
        errorMsg.append(relPath);
        errorMsg.append("\\n");
        errorMsg.append(getResource("alert.delFileFailedPart3", "!"));
        
        output.println("<script language=\"javascript\">");
        output.println("alert('" + errorMsg.toString() + "');");
        output.println("window.location=\"/webfilesys/servlet?command=listFiles&keepListStatus=true\";");
        output.println("</script>");

        output.println("</head>");
        
        output.println("</html>");
        output.flush();
    }
}
