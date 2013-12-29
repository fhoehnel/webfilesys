package de.webfilesys.gui.user;

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

import org.apache.log4j.Logger;

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
        	Logger.getLogger(getClass()).warn(errorMsg);
        	
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
            	Logger.getLogger(getClass()).warn(ioEx);
            }
        }

        resp.setContentType("application/zip");

        resp.setHeader("Content-Disposition", "attachment; filename=" + dirName + ".zip");

        OutputStream byteOut = null;
        ZipOutputStream zipOut = null;
        
		try
		{
			byteOut = resp.getOutputStream();

			zipOut = new ZipOutputStream(byteOut);
			
			zipFolderTree(path, "", zipOut);
		}
        catch (IOException ioEx)
        {
        	Logger.getLogger(getClass()).warn(ioEx);
        }
        finally
        {
            try
            {
                if (zipOut != null) 
                {
                    zipOut.close();
                }
                if (byteOut != null) 
                {
                    byteOut.close();
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

        if (fileList.length==0)
        {
            return;
        }

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

                    FileInputStream inStream = null;

                    try
                    {
                        File originalFile = new File(fullFileName);

                        inStream = new FileInputStream(originalFile);

                        byte buff[] = new byte[4096];
                        int count;

                        while ((count = inStream.read(buff)) >= 0)
                        {
                            zipOut.write(buff,0,count);
                        }
                    }
                    catch (Exception zioe)
                    {
                        Logger.getLogger(getClass()).warn("failed to zip file " + fullFileName, zioe);
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
                    Logger.getLogger(getClass()).error("failed to zip file " + fullFileName, ioex);
                }
            }
        }
    }
}
