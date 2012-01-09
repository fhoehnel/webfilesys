package de.webfilesys;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import de.webfilesys.graphics.ThumbnailThread;

public class SubdirExistCache {
	
    private HashMap<String, Integer> subdirExists = null;

    private static SubdirExistCache instance = null;
    
    private SubdirExistCache() 
    {
    	subdirExists = new HashMap<String, Integer>(100);
    }
    
    public static SubdirExistCache getInstance() 
    {
    	if (instance == null)
    	{
    		instance = new SubdirExistCache();
    	}
    	return instance;
    }
    
    /**
     * Does a (sub)folder with the given path exist?
     * @param path filesystem path of the folder
     * @return null if not known, Integer(1) if folder exists, Integer(0) if folder does NOT exist
     */
    public Integer existsSubdir(String path)
    {
        return((Integer) subdirExists.get(path));	
    }
    
    /**
     * Are there any subfolders in the folder with the given path?
     * @param path filesystem path of the folder
     * @param newVal 1 if subfolders exist, 0 if NO subfolders exist
     */
    public void setExistsSubdir(String path, Integer newVal)
    {
    	synchronized (subdirExists)
    	{
    	    subdirExists.put(path, newVal);
    	}
    }
    
    /**
     * Remove a folder and all subfolders from the subdir exist cache.
     * @param path the root of the folder tree
     */
    public void cleanupExistSubdir(String path)
    {
    	synchronized (subdirExists)
    	{
    		Iterator<String> keyIter = subdirExists.keySet().iterator();
    		
    		ArrayList<String> keysToRemove = new ArrayList<String>();
    		
    		while (keyIter.hasNext())
    		{
    			String key = keyIter.next();
    			
                if (key.startsWith(path))
                {
                	if (key.equals(path) || 
                		(key.charAt(path.length()) == '/') ||
                		(key.charAt(path.length()) == File.separatorChar))
            		{
                         keysToRemove.add(key);                		
            		}
                }
    		}
    		
    		for (int i = keysToRemove.size() - 1; i >= 0; i--)
    		{
    			subdirExists.remove(keysToRemove.get(i));
    		}
    	}
    }
    
    public void initialReadSubdirs(int operatingSystemType)
    {
        String rootDirPath;
        if ((operatingSystemType == WebFileSys.OS_OS2) || (operatingSystemType == WebFileSys.OS_WIN))
        {
            rootDirPath = new String("C:\\");
        }
        else
        {
            rootDirPath = new String("/");
        }

        File rootDir = new File(rootDirPath);

        File[] rootFileList = rootDir.listFiles();
        if (rootFileList != null)
        {
        	synchronized (subdirExists)
        	{
                for (int i = 0; i < rootFileList.length; i++)
                {
                    File tempFile = rootFileList[i];

                    if (tempFile.isDirectory())
                    {
                        File subDir = tempFile;
                        File[] subFileList = subDir.listFiles();
                        
                        boolean hasSubdirs = false;
                        if (subFileList != null) 
                        {
                            for (int k = 0; (!hasSubdirs) && (k < subFileList.length); k++) 
                            {
                            	if (subFileList[k].isDirectory())
                            	{
                					if (!subFileList[k].getName().equals(ThumbnailThread.THUMBNAIL_SUBDIR))
                					{
                                		hasSubdirs = true;
                					}
                            	}
                            }
                        }
                        if (hasSubdirs)
                        {
                        	setExistsSubdir(subDir.getAbsolutePath(), new Integer(1));
                        }
                        else
                        {
                        	setExistsSubdir(subDir.getAbsolutePath(), new Integer(0));
                        }
                    }
                }
        	}
        }
    }
}
