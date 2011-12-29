package de.webfilesys;

import java.io.File;

import de.webfilesys.graphics.ThumbnailThread;

/**
 * Checks if subdirectories exist in a directory.
 * 
 * @author Frank Hoehnel
 */
public class TestSubDirThread extends Thread
{
	String parentDirPath = null;

	public TestSubDirThread(String parentPath)
	{
		parentDirPath = parentPath;
	}

	public void run()
	{
		setPriority(1);
		
        File rootDir = new File(parentDirPath);

        File[] rootFileList = rootDir.listFiles();
        if (rootFileList != null)
        {
        	boolean hasSubdirs = false;
        	
            for (int i = 0; (!hasSubdirs) && (i < rootFileList.length); i++)
            {
                File tempFile = rootFileList[i];

                if (tempFile.isDirectory())
                {
					if (!tempFile.getName().equals(ThumbnailThread.THUMBNAIL_SUBDIR))
					{
						hasSubdirs = true;
					}
                }
            }
            if (hasSubdirs)
            {
             	SubdirExistCache.getInstance().setExistsSubdir(parentDirPath, new Integer(1));
            }
            else
            {
            	SubdirExistCache.getInstance().setExistsSubdir(parentDirPath, new Integer(0));
            }
        }
        else
        {
        	// TODO: set ExistsSubdir to null ?
        	SubdirExistCache.getInstance().setExistsSubdir(parentDirPath, new Integer(0));
        }
	}
}
