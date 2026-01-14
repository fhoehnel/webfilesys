package de.webfilesys;

import java.io.File;
import java.util.HashMap;

import de.webfilesys.graphics.ThumbnailThread;

public class SubdirExistCache {
	
    private final HashMap<String, Integer> subdirExists;

    private static SubdirExistCache instance = null;
    
    private SubdirExistCache() 
    {
    	subdirExists = new HashMap<>(100);
    }
    
    public static SubdirExistCache getInstance() {
    	if (instance == null) {
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
        return subdirExists.get(path);
    }
    
    /**
     * Are there any subfolders in the folder with the given path?
     * @param path filesystem path of the folder
     * @param newVal 1 if subfolders exist, 0 if NO subfolders exist
     */
    public void setExistsSubdir(String path, Integer newVal) {
    	synchronized (subdirExists) {
    	    subdirExists.put(path, newVal);
    	}
    }
    
    /**
     * Remove a folder and all subfolders from the subdir exist cache.
     * @param path the root of the folder tree
     */
    public void cleanupExistSubdir(String path) {
    	synchronized (subdirExists) {
            subdirExists.entrySet().removeIf(entry -> {
                String key = entry.getKey();
                return key.equals(path) ||
                        (key.startsWith(path) &&
                                (key.charAt(path.length()) == '/' ||
                                 key.charAt(path.length()) == File.separatorChar));
            });
    	}
    }
    
    public void initialReadSubdirs(int operatingSystemType) {
        String rootDirPath;
        if ((operatingSystemType == WebFileSys.OS_OS2) || (operatingSystemType == WebFileSys.OS_WIN)) {
            rootDirPath = "C:\\";
        } else {
            rootDirPath = "/";
        }

        File rootDir = new File(rootDirPath);
        File[] rootFileList = rootDir.listFiles();
        if (rootFileList != null) {
        	synchronized (subdirExists) {
                for (File tempFile : rootFileList) {
                    if (tempFile.isDirectory()) {
                        File subDir = tempFile;
                        File[] subFileList = subDir.listFiles();

                        boolean hasSubdirs = false;
                        if (subFileList != null) {
                            for (int k = 0; (!hasSubdirs) && (k < subFileList.length); k++) {
                                if (subFileList[k].isDirectory()) {
                                    if (!subFileList[k].getName().equals(ThumbnailThread.THUMBNAIL_SUBDIR)) {
                                        hasSubdirs = true;
                                    }
                                }
                            }
                        }
                        if (hasSubdirs) {
                            setExistsSubdir(subDir.getAbsolutePath(), 1);
                        } else {
                            setExistsSubdir(subDir.getAbsolutePath(), 0);
                        }
                    }
                }
        	}
        }
    }
}
