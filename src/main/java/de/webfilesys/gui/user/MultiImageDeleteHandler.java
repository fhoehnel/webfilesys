package de.webfilesys.gui.user;

import java.io.File;
import java.io.PrintWriter;

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
public class MultiImageDeleteHandler extends MultiImageRequestHandler
{
    boolean clientIsLocal = false;

    public MultiImageDeleteHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid,
            boolean clientIsLocal)
    {
        super(req, resp, session, output, uid);

        this.clientIsLocal = clientIsLocal;
    }

    protected void process()
    {
        if (!checkWriteAccess())
        {
            return;
        }

        StringBuffer alertText = new StringBuffer();

        MetaInfManager metaInfMgr = MetaInfManager.getInstance();

        for (int i = 0; i < selectedFiles.size(); i++)
        {
            String filePath = null;
            
            if (actPath.endsWith(File.separator))
            {
                filePath = actPath + selectedFiles.elementAt(i);
            }
            else
            {
                filePath = actPath + File.separator + selectedFiles.elementAt(i);
            }
            
            File delFile = new File(filePath);

            if ((!delFile.canWrite()) || (!delFile.delete()))
            {
                alertText.append(getResource("alert.delete.failed","cannot delete file"));
                alertText.append("\\n");
                alertText.append(insertDoubleBackslash(filePath));
                alertText.append("\\n");
            }
            else
            {
                metaInfMgr.removeMetaInf(actPath, (String) selectedFiles.elementAt(i));
                
                String thumbnailPath = ThumbnailThread.getThumbnailPath(filePath);
            
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

        String alert = alertText.toString();

        if (alert.length() > 0)
        {
            output.println("<HTML>");
            output.println("<HEAD>");

            this.javascriptAlert(alert);

            output.println(
                "<META HTTP-EQUIV=\"REFRESH\" CONTENT=\"0; URL=/webfilesys/servlet?command=thumbnail\">");
            output.println("</HEAD>");
            output.println("</html>");
            output.flush();

            return;
        }

	    (new XslThumbnailHandler(req, resp, session, output, uid, clientIsLocal)).handleRequest(); 
    }

}
