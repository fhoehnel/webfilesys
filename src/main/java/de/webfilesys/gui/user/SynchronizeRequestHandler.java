package de.webfilesys.gui.user;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.WebFileSys;
import de.webfilesys.gui.xsl.XslSyncCompareHandler;
import de.webfilesys.sync.SyncItem;

/**
 * @author Frank Hoehnel
 */
public class SynchronizeRequestHandler extends UserRequestHandler
{
    boolean createMissingTarget;
    boolean createMissingSource;
    boolean removeExtraTarget;
    boolean copyNewerToTarget;
    boolean copyNewerToSource;
    boolean copyDateChangeToTarget;
    boolean copySizeChangeToTarget;
    boolean copyAccessRights;

    public SynchronizeRequestHandler(
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
		if (!checkWriteAccess())
		{
			return;
		}

        createMissingTarget = (req.getParameter("createMissingTarget") != null);
        createMissingSource = (req.getParameter("createMissingSource") != null);
        removeExtraTarget = (req.getParameter("removeExtraTarget") != null);
        copyNewerToTarget = (req.getParameter("copyNewerToTarget") != null);
        copyNewerToSource = (req.getParameter("copyNewerToSource") != null);
        copyDateChangeToTarget = (req.getParameter("copyDateChangeToTarget") != null);
        copySizeChangeToTarget = (req.getParameter("copySizeChangeToTarget") != null);
        copyAccessRights = (req.getParameter("copyAccessRights") != null);
        
        output.println("<HTML>");
		output.println("<HEAD>");

        output.println("<script src=\"/webfilesys/javascript/ajaxCommon.js\" type=\"text/javascript\"></script>");
        output.println("<script src=\"/webfilesys/javascript/ajaxFolder.js\" type=\"text/javascript\"></script>");
        
		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/css/" + userMgr.getCSS(uid) + ".css\">");

		output.println("</HEAD>");

		output.println("<BODY>");
        
        headLine(getResource("headline.syncResult", "Synchronize Folders"));
        
        output.println("<br/>");
       
        ArrayList syncItemList = (ArrayList) session.getAttribute(XslSyncCompareHandler.SESSION_ATTRIB_SYNCHRONIZE_ITEMS);
        
        if (syncItemList == null)
        {
            // should never happen
            Logger.getLogger(getClass()).error("sync item list not found in session");
            return;
        }
        
        for (int i = 0; i < syncItemList.size(); i++)
        {
            SyncItem syncItem = (SyncItem) syncItemList.get(i);
            
            synchronize(syncItem);
        }
        
        output.println("<form accept-charset=\"utf-8\" name=\"form1\" style=\"padding-top:15px;\">");
        output.println("<input type=\"button\" value=\"" + getResource("button.closewin", "close window") + "\" onclick=\"deselectSyncFolders()\" />");
        output.println("</form>");
        
        output.println("<script language=\"javascript\">");
        output.println("alert('" + getResource("sync.complete", "folder synchronization complete") + "');");
        output.println("scrollTo(1,50000);");
        output.println("</script>");
        
		output.println("</body></html>");
		output.flush();
		
		session.removeAttribute(XslSyncCompareHandler.SESSION_ATTRIB_SYNCHRONIZE_ITEMS);	
	}
    
    private void synchronize(SyncItem syncItem)
    {
        if (syncItem.getDiffType() == SyncItem.DIFF_TYPE_MISSING_TARGET_FILE)
        {
            if (createMissingTarget)
            {
                createDirIfMissing(syncItem.getTarget().getPath(), false);
                copyFile(syncItem.getSource().getPath(), syncItem.getTarget().getPath());
            }
        }
        else if (syncItem.getDiffType() == SyncItem.DIFF_TYPE_MISSING_TARGET_DIR)
        {
            if (createMissingTarget)
            {
                createDirIfMissing(syncItem.getTarget().getPath(), true);
            }
        }
        else if (syncItem.getDiffType() == SyncItem.DIFF_TYPE_MISSING_SOURCE_FILE)
        {
            if (createMissingSource)
            {
                createDirIfMissing(syncItem.getSource().getPath(), false);
                copyFile(syncItem.getTarget().getPath(), syncItem.getSource().getPath());
            }
            else if (removeExtraTarget)
            {
               deleteFile(syncItem.getTarget().getPath()); 
            }
        }
        else if (syncItem.getDiffType() == SyncItem.DIFF_TYPE_MISSING_SOURCE_DIR)
        {
            if (createMissingSource)
            {
                createDirIfMissing(syncItem.getSource().getPath(), true);
            }
            else if (removeExtraTarget)
            {
               deleteDirTree(syncItem.getTarget().getPath()); 
            }
        }
        else if (syncItem.getDiffType() == SyncItem.DIFF_TYPE_SIZE)
        {
            if (copySizeChangeToTarget)
            {
                copyFile(syncItem.getSource().getPath(), syncItem.getTarget().getPath());
            }
        }
        else if (syncItem.getDiffType() == SyncItem.DIFF_TYPE_MODIFICATION_TIME)
        {
            if (syncItem.getSource().getModificationTime() > syncItem.getTarget().getModificationTime())
            {
                if (copyNewerToTarget || copyDateChangeToTarget)
                {
                    copyFile(syncItem.getSource().getPath(), syncItem.getTarget().getPath());
                }
            }
            else // target newer
            {
                if (copyNewerToSource)
                {
                    copyFile(syncItem.getTarget().getPath(), syncItem.getSource().getPath());
                }                
                else if (copyDateChangeToTarget)
                {
                    copyFile(syncItem.getSource().getPath(), syncItem.getTarget().getPath());
                }
            }
        }
        else if (syncItem.getDiffType() == SyncItem.DIFF_TYPE_SIZE_TIME)
        {
            if (copySizeChangeToTarget)
            {
                copyFile(syncItem.getSource().getPath(), syncItem.getTarget().getPath());
            }
            else
            {
                if (syncItem.getSource().getModificationTime() > syncItem.getTarget().getModificationTime())
                {
                    if (copyNewerToTarget || copyDateChangeToTarget)
                    {
                        copyFile(syncItem.getSource().getPath(), syncItem.getTarget().getPath());
                    }
                }
                else // target newer
                {
                    if (copyNewerToSource)
                    {
                        copyFile(syncItem.getTarget().getPath(), syncItem.getSource().getPath());
                    }   
                    else if (copyDateChangeToTarget)
                    {
                        copyFile(syncItem.getSource().getPath(), syncItem.getTarget().getPath());
                    }
                }
            }
        }
        else if (syncItem.getDiffType() == SyncItem.DIFF_TYPE_ACCESS_RIGHTS)
        {
            if (copyAccessRights)
            {
                File sourceFile = new File(syncItem.getSource().getPath());
                
                if (sourceFile.canWrite())
                {
                    setReadWrite(syncItem.getTarget().getPath());
                }
                else
                {
                    setReadonly(syncItem.getTarget().getPath());
                }
            }
        }
    }
    
    private void createDirIfMissing(String path, boolean isDir)
    {
        String folderPath;

        if (isDir)
        {
            folderPath = path;
        }
        else
        {
            folderPath = path
                    .substring(0, path.lastIndexOf(File.separatorChar));
        }

        File newFolder = new File(folderPath);

        if (newFolder.exists())
        {
            return;
        }

        newFolder.mkdirs();
        
        output.println("<nobr>" + getResource("sync.folderCreated", "folder created") + ": " + getHeadlinePath(folderPath) + "</nobr><br>");
    }
    
    private boolean copyFile(String sourceFileName, String targetFileName)
    {
        File sourceFile = new File(sourceFileName);
        
        long lastChangeDate = sourceFile.lastModified();

        boolean copyFailed = false;

        byte buff[] = new byte[4096];

        try
        {
            BufferedInputStream fin = new BufferedInputStream(new FileInputStream(sourceFileName));
            BufferedOutputStream fout = new BufferedOutputStream(new FileOutputStream(targetFileName));

            int count;
            while (( count = fin.read(buff)) >= 0 )
            {
                fout.write(buff, 0, count);
            }

            fin.close();
            fout.close();
        }
        catch (Throwable e)
        {
            Logger.getLogger(getClass()).error(e);
            copyFailed = true;
        }

        if (!copyFailed)
        {
            File destFile = new File(targetFileName);
            destFile.setLastModified(lastChangeDate);
        }

        if (copyFailed)
        {
            output.println("<nobr>" + getResource("sync.fileCopiedFailed", "failed to copy file") + ": " + getHeadlinePath(sourceFileName) + "</nobr><br>");
        }
        else
        {
            output.println("<nobr>" + getResource("sync.fileCopied", "file copied") + ": " + getHeadlinePath(sourceFileName) + "</nobr><br>");
        }
        
        output.flush();

        return(!copyFailed);
    }
    
    private void deleteFile(String path)
    {
        File delFile = new File(path);
     
        if (!delFile.exists())
        {
            return;
        }
        
        if (delFile.canWrite())
        {
            if (delFile.delete())
            {
                output.println("<nobr>" + getResource("sync.fileDeleted", "file deleted") + ": " + getHeadlinePath(path) + "</nobr><br>");
            }
            else
            {
                output.println("<nobr>" + getResource("sync.fileDeleteError", "failed to delete file") + ": " + getHeadlinePath(path) + "</nobr><br>");
            }
        }
    }
    
    private boolean deleteDirTree(String path)
    { 
        boolean deleteError=false;

        File dirToBeDeleted = new File(path);
        
        if (!dirToBeDeleted.exists())
        {
            return false;
        }
        
        String fileList[] = dirToBeDeleted.list();

        if (fileList != null)
        {
            for (int i = 0; i < fileList.length; i++)
            {
                File tempFile=new File(path + File.separator + fileList[i]);
                if (tempFile.isDirectory())
                {
                    if (!deleteDirTree(path + File.separator + fileList[i]))
                        deleteError=true;
                }
                else
                {
                    if (tempFile.delete())
                    {
                        output.println("<nobr>" + getResource("sync.fileDeleted", "file deleted") + ": " + getHeadlinePath(tempFile.getAbsolutePath()) + "</nobr><br>");
                    }
                    else
                    {
                        deleteError=true;
                        output.println("<nobr>" + getResource("sync.fileDeleteError", "failed to delete file") + ": " + getHeadlinePath(tempFile.getAbsolutePath()) + "</nobr><br>");
                        Logger.getLogger(getClass()).warn("failed to delete file " + tempFile);
                    }
                }
            }
        }

        if (dirToBeDeleted.delete())
        {
            output.println("<nobr>" + getResource("sync.dirDeleted", "directory deleted") + ": " + getHeadlinePath(path) + "</nobr><br>");
        }
        else
        {
            deleteError=true;
            output.println("<nobr>" + getResource("sync.dirDeleteError", "failed to delete directory") + ": " + getHeadlinePath(path) + "</nobr><br>");
            Logger.getLogger(getClass()).warn("failed to delete directory " + path);
        }

        return(!(deleteError));
    }
    
    private void setReadonly(String path)
    {
        File file = new File(path);

        if (file.canWrite())
        {
            file.setReadOnly();
            output.println("<nobr>" + getResource("sync.switchedToReadonly", "access rights changed to read-only") + ": " + getHeadlinePath(path) + "</nobr><br>");
        }
    }
    
    private void setReadWrite(String path)
    {
        String execString;

        if (WebFileSys.getInstance().is32bitWindows())
        {
            execString = "cmd /c attrib -R " + path;
        }
        else
        {
            execString = "attrib -R " + path;
        }

        Process attribProcess = null;

        try
        {
            attribProcess = Runtime.getRuntime().exec(execString);
        }
        catch (IOException rte)
        {
            Logger.getLogger(getClass()).error(rte);
        }

        try
        {
            attribProcess.waitFor();
        }
        catch (InterruptedException iex)
        {
            Logger.getLogger(getClass()).error(iex);
        }
        
        output.println("<nobr>" + getResource("sync.switchedToReadWrite", "access rights changed to writable") + ": " + getHeadlinePath(path) + "</nobr><br>");
    }
}
