package de.webfilesys.gui.user;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.ViewHandlerConfig;
import de.webfilesys.ViewHandlerManager;
import de.webfilesys.util.FileEncodingMap;
import de.webfilesys.util.MimeTypeMap;

/**
 * View a file contained in a ZIP archive.
 * @author Frank Hoehnel
 */
public class ZipContentFileRequestHandler extends UserRequestHandler
{
	public ZipContentFileRequestHandler(
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
        String zipFilePath = getParameter("zipFilePath");
        String zipContentPath = getParameter("zipContentPath");

		if ((zipFilePath == null) || (zipContentPath == null))
		{
            return;
		}
		
		if (!checkAccess(zipFilePath))
		{
			return;
		}

		zipContentPath = zipContentPath.replace("\\", "/");
        
        ZipFile zipFile = null;

        try
        {
            zipFile = new ZipFile(zipFilePath);
                
            ZipEntry zipEntry = zipFile.getEntry(zipContentPath);
                
            if (zipEntry != null) 
            {
                InputStream zipInFile = zipFile.getInputStream(zipEntry);

                ViewHandlerConfig viewHandlerConfig = ViewHandlerManager.getInstance().getViewHandlerConfig(zipContentPath);

                if (viewHandlerConfig != null)
                {
                    String viewHandlerClassName = viewHandlerConfig.getHandlerClass();
                    
                    if (viewHandlerClassName != null)
                    {
                        if (delegateToViewHandler(viewHandlerConfig, zipEntry.getName(), zipInFile))
                        {
                            return;
                        }
                    }
                }
                
                String mimeType = MimeTypeMap.getInstance().getMimeType(zipContentPath);
                
                resp.setContentType(mimeType);

                String encoding = FileEncodingMap.getInstance().getFileEncoding(zipContentPath);
                
                if (encoding != null) {
                    resp.setCharacterEncoding(encoding);
                }
                
                long fileSize = zipEntry.getSize();
                
                resp.setContentLength((int) fileSize);

                byte buffer[] = null;
                
                if (fileSize < 16192)
                {
                    buffer = new byte[(int) fileSize];
                }
                else
                {
                    buffer = new byte[65536];
                }
                
                OutputStream respOut = resp.getOutputStream();

                int count = 0;
                long bytesWritten = 0;
                
                while ((count = zipInFile.read(buffer)) >= 0)
                {
                    respOut.write(buffer, 0, count);
                    
                    bytesWritten += count;
                }

                zipInFile.close();
                    
                if (bytesWritten != fileSize)
                {
                    Logger.getLogger(getClass()).warn(
                        "only " + bytesWritten + " bytes of " + fileSize + " have been written to output");
                }

                respOut.flush();
                
                buffer = null;
            }
        }
        catch (IOException ioex)
        {
            Logger.getLogger(getClass()).error("cannot read ZIP file content " + zipContentPath + " from " + zipFilePath, ioex);
        }
        finally
        {
            if (zipFile != null) 
            {
                try 
                {
                    zipFile.close();
                }
                catch (IOException ioex)
                {
                }
            }
        }
    }
}
