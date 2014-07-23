package de.webfilesys.gui.user;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.MetaInfManager;
import de.webfilesys.ViewHandlerConfig;
import de.webfilesys.ViewHandlerManager;
import de.webfilesys.WebFileSys;
import de.webfilesys.graphics.ThumbnailThread;
import de.webfilesys.util.FileEncodingMap;
import de.webfilesys.util.MimeTypeMap;

/**
 * @author Frank Hoehnel
 */
public class GetFileRequestHandler extends UserRequestHandler
{
	public GetFileRequestHandler(
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
		String filePath = getParameter("filePath");

		if (!this.checkAccess(filePath))
		{
		    return;	
		}

		boolean error = false;
		
        File fileToSend = new File(filePath);
        
        if (!fileToSend.exists())
        {
        	Logger.getLogger(getClass()).warn("requested file does not exist: " + filePath);
        	
        	error = true;
        }
        else if ((!fileToSend.isFile()) || (!fileToSend.canRead()))
        {
        	Logger.getLogger(getClass()).warn("requested file is not a readable file: " + filePath);
        	
        	error = true;
        }

        if (error)
        {
            resp.setStatus(404);

            try
    		{
    			PrintWriter output = new PrintWriter(resp.getWriter());
    			
    			output.println("File not found or not readable: " + filePath);
    			
    			output.flush();
    			
    			return;
    		}
            catch (IOException ioEx)
            {
            	Logger.getLogger(getClass()).warn(ioEx);
            }
        }

        String disposition = getParameter("disposition");

        if (disposition == null)
        {
        	ViewHandlerConfig viewHandlerConfig = ViewHandlerManager.getInstance().getViewHandlerConfig(fileToSend.getName());

        	if (viewHandlerConfig != null)
        	{
            	String viewHandlerClassName = viewHandlerConfig.getHandlerClass();
            	
                if (viewHandlerClassName != null)
            	{
            		if (delegateToViewHandler(viewHandlerConfig, filePath, null))
            		{
                        return;
            		}
            	}
        	}
        }

		String mimeType = MimeTypeMap.getInstance().getMimeType(filePath);
		
		resp.setContentType(mimeType);
		
		String cached = getParameter("cached");
		
		if ((cached != null) && (cached.equals("true")))
		{
            // overwrite the no chache headers already set in WebFileSysServlet
			resp.setHeader("Cache-Control", null);
			resp.setDateHeader("expires", System.currentTimeMillis() + (10 * 60 * 60 * 1000)); // now + 10 hours
		}

		if ((disposition != null) && disposition.equals(("download")))
		{
			resp.setHeader("Content-Disposition", "attachment; filename=" + fileToSend.getName());
		}
		
		String encoding = FileEncodingMap.getInstance().getFileEncoding(filePath);
		
		if (encoding != null) {
	        resp.setCharacterEncoding(encoding);
		}
		
		long fileSize = fileToSend.length();
		
		resp.setContentLength((int) fileSize);

		byte buffer[] = null;
		
        if (fileSize < 16192)
        {
            buffer = new byte[16192];
        }
        else
        {
            buffer = new byte[65536];
        }
		
		try
		{
			OutputStream byteOut = resp.getOutputStream();

			FileInputStream fileInput = new FileInputStream(fileToSend);
			
			int count = 0;
			long bytesWritten = 0;
			
            while ((count = fileInput.read(buffer)) >= 0)
            {
                byteOut.write(buffer, 0, count);

                bytesWritten += count;
            }

            fileInput.close();
	            
	        if (bytesWritten != fileSize)
	        {
	            Logger.getLogger(getClass()).warn(
	                "only " + bytesWritten + " bytes of " + fileSize + " have been written to output");
	        } 

	        byteOut.flush();
	        
	        buffer = null;

	        // if (incrDownload)
			if (WebFileSys.getInstance().isDownloadStatistics() && (filePath.indexOf(ThumbnailThread.THUMBNAIL_SUBDIR) < 0))
			{
				MetaInfManager.getInstance().incrementDownloads(filePath);
			}
		}
        catch (IOException ioEx)
        {
        	Logger.getLogger(getClass()).warn(ioEx);
        }
	}
	
}
