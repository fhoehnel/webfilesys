package de.webfilesys.gui.anonymous;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


import de.webfilesys.InvitationManager;
import de.webfilesys.MetaInfManager;
import de.webfilesys.WebFileSys;
import de.webfilesys.graphics.ThumbnailThread;
import de.webfilesys.gui.RequestHandler;
import de.webfilesys.util.MimeTypeMap;

/**
 * @author Frank Hoehnel
 */
public class VisitorFileRequestHandler extends RequestHandler
{
	public VisitorFileRequestHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output)
	{
        super(req, resp, session, output);
	}

	protected void process()
	{
		boolean error = false;
		
		String accessCode = req.getParameter("accessCode");

		if (accessCode == null)
		{
			LogManager.getLogger(getClass()).warn("invalid visitor access code");
			return;
		}

		String filePath = InvitationManager.getInstance().getInvitationPath(accessCode);

		if (filePath == null)
		{
			LogManager.getLogger(getClass()).warn("visitor access with invalid access code");
			return;
		}
		
        File fileToSend = new File(filePath);
        
        if (!fileToSend.exists())
        {
        	LogManager.getLogger(getClass()).warn("requested file does not exist: " + filePath);
        	
        	error = true;
        }
        else if ((!fileToSend.isFile()) || (!fileToSend.canRead()))
        {
        	LogManager.getLogger(getClass()).warn("requested file is not a readable file: " + filePath);
        	
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
            	LogManager.getLogger(getClass()).warn(ioEx);
            }
        }

		String mimeType = MimeTypeMap.getInstance().getMimeType(filePath);
		
		resp.setContentType(mimeType);
		
		String cached = getParameter("cached");
		
		if ((cached != null) && (cached.equals("true")))
		{
            // overwrite the no chache headers already set in WebFileSysServlet
			resp.setHeader("Cache-Control", "public, max-age=3600, s-maxage=3600");
			resp.setDateHeader("expires", System.currentTimeMillis() + (60 * 60 * 1000)); // now + 10 hours
		}

		String disposition = getParameter("disposition");
		
		if ((disposition != null) && disposition.equals(("download")))
		{
			resp.setHeader("Content-Disposition", "attachment; filename=" + fileToSend.getName());
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
		
        FileInputStream fileInput = null;
        
		try
		{
			OutputStream byteOut = resp.getOutputStream();

			fileInput = new FileInputStream(fileToSend);
			
			int count = 0;
			long bytesWritten = 0;
			
            while ((count = fileInput.read(buffer)) >= 0)
            {
                byteOut.write(buffer, 0, count);
                
                bytesWritten += count;
            }

	        if (bytesWritten != fileSize)
	        {
	            LogManager.getLogger(getClass()).warn(
	                "only " + bytesWritten + " bytes of " + fileSize + " have been written to output");
	        }

	        byteOut.flush();
	        
	        buffer = null;

			if (WebFileSys.getInstance().isDownloadStatistics() && (filePath.indexOf(ThumbnailThread.THUMBNAIL_SUBDIR) < 0))
			{
				MetaInfManager.getInstance().incrementDownloads(filePath);
			}
		}
        catch (IOException ioEx)
        {
        	LogManager.getLogger(getClass()).warn(ioEx);
        }
		finally {
			if (fileInput != null) 
			{
				try
				{
					fileInput.close();
				}
				catch (IOException ex2)
				{
				}
			}
		}
	}
}
