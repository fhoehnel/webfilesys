package de.webfilesys;

import java.io.File;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import de.webfilesys.graphics.ThumbnailThread;

public class DirTreeStatusInspector extends Thread {
	
	DirTreeStatus dirTreeStatus;
	
	public DirTreeStatusInspector(DirTreeStatus dirTreeStatus) {
		this.dirTreeStatus = dirTreeStatus;
	}
	
    public synchronized void run() {
        setPriority(1);

        rememberFolderTreeStructure();
    }

    public void rememberFolderTreeStructure() {
		ArrayList<String> expandedFolders = dirTreeStatus.getExpandedFolders();
		
		for (String path : expandedFolders) {
			File folderFile = new File(path);
			if (folderFile.exists() && folderFile.isDirectory()) {
				long nameLengthSum = getSubdirNameLengthSum(folderFile);
				dirTreeStatus.setSubdirNameLengthSum(path, nameLengthSum);
			}
		}
    }
    
    public void rememberPathStatus(String path) {
		File folderFile = new File(path);
		if (folderFile.exists() && folderFile.isDirectory()) {
			long nameLengthSum = getSubdirNameLengthSum(folderFile);
			dirTreeStatus.setSubdirNameLengthSum(path, nameLengthSum);
		}
    }
    
    public boolean isFolderTreeStructureChanged() {
		ArrayList<String> expandedFolders = dirTreeStatus.getExpandedFolders();
		
		for (String path : expandedFolders) {
			File folderFile = new File(path);
			if (folderFile.exists() && folderFile.isDirectory()) {
				long lastKnownNameLengthSum = dirTreeStatus.getSubdirNameLenghtSum(path);
				if (lastKnownNameLengthSum >= 0) {
					long currentNameLengthSum = getSubdirNameLengthSum(folderFile);
					if (lastKnownNameLengthSum != currentNameLengthSum) {
					    
					    if (Logger.getLogger(getClass()).isDebugEnabled()) {
					        Logger.getLogger(getClass()).debug("folder tree changed: " + path);
					    }
					    
						return true;
					}
				}
			}
		}
		
		return false;
    }
    
	private long getSubdirNameLengthSum(File folderFile) {
		long subdirNameLengthSum = 0;
		
		File[] files = folderFile.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					if (!file.getName().startsWith(Constants.SEARCH_RESULT_FOLDER_PREFIX)) {
						if (!file.getName().equals(ThumbnailThread.THUMBNAIL_SUBDIR)) {
							subdirNameLengthSum += file.getName().length();
						}
					}
				}
			}
		}
		
		return subdirNameLengthSum;
	}
    
}
