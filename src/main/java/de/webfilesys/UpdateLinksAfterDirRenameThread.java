package de.webfilesys;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Updates the links to all files in a renamed folder tree.
 * Makes only sense with reverse file linking enabled.
 * 
 * @author Frank Hoehnel
 */
public class UpdateLinksAfterDirRenameThread extends Thread
{
    private String newDirPath = null;   
    
    private String uid = null;
    
    private MetaInfManager metaInfMgr = null;
    
    public UpdateLinksAfterDirRenameThread(String newDirPath, String uid)
    {
        this.newDirPath = newDirPath;   
        this.uid = uid;
        metaInfMgr = MetaInfManager.getInstance();
    }
    
    public void run()
    {
         updateLinks(newDirPath);
    }

    private void updateLinks(String newDirPath) 
    {
        File folder = new File(newDirPath);
        
        File[] fileList = folder.listFiles();
        
        if (fileList == null)
        {
            return;
        }
        
        for (int i = 0; i < fileList.length; i++)
        {
            File file = fileList[i];
            
            if (file.isFile() && file.canRead()) 
            {
                String newFilePath = file.getAbsolutePath();
                
                ArrayList linkingFiles = metaInfMgr.getLinkingFiles(newFilePath);
                
                if (linkingFiles != null)
                {
                    Iterator iter = linkingFiles.iterator();
                    
                    while (iter.hasNext())
                    {
                        String linkingFilePath = (String) iter.next();
                        
                        metaInfMgr.updateLinkTarget(linkingFilePath, newFilePath, uid);                        
                    }
                }
            }
            else
            {
                if (file.isDirectory() && file.canRead())
                {
                    String newSubDirPath = null;
                    if (newDirPath.endsWith(File.separator))
                    {
                        newSubDirPath = newDirPath + file.getName();
                    }
                    else
                    {
                        newSubDirPath = newDirPath + File.separator + file.getName();
                    }
                    
                    updateLinks(newSubDirPath); 
                }
            }
        }
    }
}
