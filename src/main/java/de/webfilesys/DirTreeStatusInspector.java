package de.webfilesys;

import java.io.File;
import java.util.ArrayList;

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
    
    public boolean isFolderTreeStructureChanged() {
		ArrayList<String> expandedFolders = dirTreeStatus.getExpandedFolders();
		
		for (String path : expandedFolders) {
			File folderFile = new File(path);
			if (folderFile.exists() && folderFile.isDirectory()) {
				long lastKnownNameLengthSum = dirTreeStatus.getSubdirNameLenghtSum(path);
				if (lastKnownNameLengthSum >= 0) {
					long currentNameLengthSum = getSubdirNameLengthSum(folderFile);
					if (lastKnownNameLengthSum != currentNameLengthSum) {
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
					subdirNameLengthSum += file.getName().length();
				}
			}
		}
		
		return subdirNameLengthSum;
	}
    
}
