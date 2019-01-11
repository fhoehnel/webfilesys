package de.webfilesys.gui.user;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.MetaInfManager;
import de.webfilesys.WebFileSys;

/**
 * @author Frank Hoehnel
 */
public class MultiFileDownloadHandler extends MultiFileRequestHandler
{
	protected HttpServletResponse resp = null;

	public MultiFileDownloadHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid)
	{
        super(req, resp, session, output, uid);

	    this.resp = resp;
	}

	protected void process()
	{
		ArrayList<String> selectedFiles = (ArrayList<String>) session.getAttribute("selectedFiles");
		
		if ((selectedFiles == null) || (selectedFiles.size() == 0))
		{
			Logger.getLogger(getClass()).debug("MultiFileDownloadHandler: no files selected");
			
			return;
		}
		
		String actPath = getCwd();
		
		if (actPath == null)
		{
			Logger.getLogger(getClass()).error("MultiFileDownloadHandler: actPath is null");
			
			return;
		}
		
		if (isMobile()) 
		{
		    actPath = this.getAbsolutePath(actPath);
		}
		
		if (!checkAccess(actPath)) 
		{
		    return;
		}
		
		File tempFile = null;
		
		FileInputStream fin = null;

		try
		{
			tempFile = File.createTempFile("fmweb",null);
			
			ZipOutputStream zip_out=null;

			zip_out = new ZipOutputStream(new FileOutputStream(tempFile));

			int count=0;

			byte buffer[] = new byte[16192];

			for (String selectedFile : selectedFiles) 
			{
				FileInputStream inFile = null;
				
				try
				{
					zip_out.putNextEntry(new ZipEntry(selectedFile));

					inFile = new FileInputStream(new File(actPath, selectedFile));

					count=0;

					while (( count = inFile.read(buffer)) >= 0 )
					{
						zip_out.write(buffer,0,count);
					}
				}
				catch (Exception zioe)
				{
					Logger.getLogger(getClass()).warn("failed to add file to temporary zip archive", zioe);
					return;
				}
		        finally 
		        {
		        	if (inFile != null) 
		        	{
		        		try 
		        		{
		        			inFile.close();
		        		}
		        		catch (IOException ioex2)
		        		{
		        			Logger.getLogger(getClass()).error("failed to close file", ioex2);
		        		}
		        	}
		        }
			}
		
			zip_out.close();
			
			resp.setContentType("application/zip");

			resp.setHeader("Content-Disposition", "attachment; filename=fmwebDownload.zip");
			
			resp.setContentLength((int) tempFile.length());

			OutputStream byteOut = resp.getOutputStream();

			fin = new FileInputStream(tempFile);

			while ((count = fin.read(buffer)) >= 0)
			{
				byteOut.write(buffer, 0, count);
			}

			fin.close();

			byteOut.flush();

			buffer = null;
			
			tempFile.delete();
			
			if (WebFileSys.getInstance().isDownloadStatistics())
			{
				for (String selectedFile : selectedFiles) 
				{
                    String fullPath = null;
                     
                    if (actPath.endsWith(File.separator))
                    {
					    fullPath = actPath + selectedFile;
                    }
                    else
                    {
					    fullPath = actPath + File.separator + selectedFile;
                    }
				
				    MetaInfManager.getInstance().incrementDownloads(fullPath);
				}
			}
		}
        catch (IOException ioex)
        {
        	Logger.getLogger(getClass()).error(ioex);
        	return;
        }
        finally 
        {
        	if (fin != null) 
        	{
        		try 
        		{
        			fin.close();
        		}
        		catch (IOException ioex2)
        		{
        			Logger.getLogger(getClass()).error("failed to close ZIP-File", ioex2);
        		}
        	}
        }
        
        session.removeAttribute("selectedFiles");
	}
}
