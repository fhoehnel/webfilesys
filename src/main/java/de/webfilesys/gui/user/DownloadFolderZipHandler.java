package de.webfilesys.gui.user;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/**
 * @author Frank Hoehnel
 */
public class DownloadFolderZipHandler extends UserRequestHandler
{
	public DownloadFolderZipHandler(
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
		String path = getParameter("path");

		if (!checkAccess(path))
		{
		    return;	
		}

		String errorMsg = null;
		
        File folderFile = new File(path);
        
        if ((!folderFile.exists()) || (!folderFile.isDirectory()) || (!folderFile.canRead()))
        {
            errorMsg = "folder is not a readable directory: " + path;
        }

        String dirName = null;
        
        int lastSepIdx = path.lastIndexOf(File.separatorChar);
        
        if (lastSepIdx < 0) 
        {
            lastSepIdx = path.lastIndexOf('/');
        }
        
        if ((lastSepIdx < 0) || (lastSepIdx == path.length() - 1))
        {
            errorMsg = "invalid path for folder download: " + path;
        }
        else
        {
            dirName = path.substring(lastSepIdx + 1);
        }
        
        if (errorMsg != null)
        {
        	LogManager.getLogger(getClass()).warn(errorMsg);
        	
            resp.setStatus(404);

            try
    		{
    			PrintWriter output = new PrintWriter(resp.getWriter());
    			
    			output.println(errorMsg);
    			
    			output.flush();
    			
    			return;
    		}
            catch (IOException ioEx)
            {
            	LogManager.getLogger(getClass()).warn(ioEx);
            }
        }

        resp.setContentType("application/zip");

        resp.setHeader("Content-Disposition", "attachment; filename=" + dirName + ".zip");

        BufferedOutputStream buffOut = null;
        ZipOutputStream zipOut = null;
        
		try
		{
			buffOut = new BufferedOutputStream(resp.getOutputStream());
			
			zipOut =  new ZipOutputStream(buffOut);
			
			zipFolderTree(path, "", zipOut);
			
			buffOut.flush();
		}
        catch (IOException ioEx)
        {
        	LogManager.getLogger(getClass()).warn(ioEx);
        }
        finally
        {
            try
            {
                if (zipOut != null) 
                {
                    zipOut.close();
                }
                if (buffOut != null) 
                {
                	buffOut.close();
                }
            }
            catch (Exception ex)
            {
            }
        }
	}
	
    private void zipFolderTree(String actPath, String relativePath, ZipOutputStream zipOut)
    {
        File actDir = new File(actPath);

        String fileList[]=actDir.list();

        if ((fileList == null) || (fileList.length == 0))
        {
            return;
        }

        byte buff[] = new byte[4096];

        for (int i = 0; i < fileList.length; i++)
        {
            File tempFile = new File(actPath + File.separator + fileList[i]);

            if (tempFile.isDirectory())
            {
                zipFolderTree(actPath + File.separator + fileList[i],
                                   relativePath + fileList[i] + "/",
                                   zipOut);
            }
            else
            {
                String fullFileName = actPath + File.separator + fileList[i];
                String relativeFileName = relativePath + fileList[i];

                try
                {
                    ZipEntry newZipEntry = new ZipEntry(relativeFileName);

                    zipOut.putNextEntry(newZipEntry);

                    BufferedInputStream inStream = null;

                    try
                    {
                        File originalFile = new File(fullFileName);

                        inStream = new BufferedInputStream(new FileInputStream(originalFile));

                        int count;

                        while ((count = inStream.read(buff)) >= 0)
                        {
                            zipOut.write(buff,0,count);
                        }
                    }
                    catch (Exception zioe)
                    {
                        LogManager.getLogger(getClass()).warn("failed to zip file " + fullFileName, zioe);
                    }
                    finally
                    {
                        if (inStream != null)
                        {
                            try
                            {
                                inStream.close();
                            }
                            catch (Exception ex)
                            {
                            }
                        }
                    }
                }
                catch (IOException ioex)
                {
                    LogManager.getLogger(getClass()).error("failed to zip file " + fullFileName, ioex);
                }
            }
        }
    }
}
