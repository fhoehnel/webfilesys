package de.webfilesys.stats;

import java.io.File;
import java.util.ArrayList;

public class DirStatsBySize {
	
	private static final long SIZE_MAX = 4L * 1024L * 1024L * 1024L;

	private static final long KB = 1024;
	private static final long MB = 1024 * KB;
	private static final long GB = 1024 * MB;
	
	private static final int SIZE_STEP_FACTOR = 4;
	
	private ArrayList sizeCategories = null;
	
	private long filesInTree = 0L;
	
	private long treeFileSize = 0L;
	
	public DirStatsBySize(String rootPath) 
	{
		sizeCategories = new ArrayList();
		
		long minSize = 0;
		long maxSize = 1024;
		
		for (int i = 0; maxSize <= SIZE_MAX; i++) 
		{
			sizeCategories.add(new SizeCategory(minSize, maxSize));
			minSize = maxSize + 1;
			maxSize = maxSize * SIZE_STEP_FACTOR;
		}
		
		filesInTree = 0L;
		
		walkThroughFolderTree(rootPath);
		
		calculatePercentage();
	}

	private void walkThroughFolderTree(String path)
	{
		File dirFile = new File(path);
		
		if ((!dirFile.exists()) ||(!dirFile.isDirectory()) || (!dirFile.canRead())) {
			return;
		}
		
		File fileList[] = dirFile.listFiles();
		
		if (fileList == null) {
			return;
		}
		
		for (int i = 0; i < fileList.length; i++) {
			if (fileList[i].isDirectory()) {
				walkThroughFolderTree(fileList[i].getAbsolutePath());
			} else {
				if (fileList[i].isFile()) {
					addToStats(fileList[i].length());
					filesInTree++;
					treeFileSize += fileList[i].length();
				}
			}
		}
	}
	
	private void addToStats(long fileSize) {
		
		boolean stop = false;
		
		for (int i = 0; (!stop) && (i < sizeCategories.size()); i++) {
			SizeCategory sizeCat = (SizeCategory) sizeCategories.get(i);
			
			if ((fileSize >= sizeCat.getMinSize()) && (fileSize <= sizeCat.getMaxSize())) {
				sizeCat.addFile(fileSize);
				stop = true;
			}
		}
	}
	
	private void calculatePercentage() {
		for (int i = 0; i < sizeCategories.size(); i++) {
			SizeCategory sizeCat = (SizeCategory) sizeCategories.get(i);
			sizeCat.setFileNumPercent((int) (sizeCat.getFileNum() * 100L / filesInTree));
			
			sizeCat.setSizePercent(treeFileSize);
		}
	}
	
	/**
	 * @return List of SizeCategory objects.
	 */
	public ArrayList getResults()
	{
	    return sizeCategories;
	}
	
	public long getFileNumCategoryMax() {
        long fileNumMax = 0L;
        
	    for (int i = 0; i < sizeCategories.size(); i++) {
            SizeCategory sizeCat = (SizeCategory) sizeCategories.get(i);
            if (sizeCat.getFileNum() > fileNumMax) {
                fileNumMax = sizeCat.getFileNum();
            }
        }
	    
	    return fileNumMax;
	}

    public long getSizeSumCategoryMax() {
        long sizeSumMax = 0L;
        
        for (int i = 0; i < sizeCategories.size(); i++) {
            SizeCategory sizeCat = (SizeCategory) sizeCategories.get(i);
            if (sizeCat.getSizeSum() > sizeSumMax) {
                sizeSumMax = sizeCat.getSizeSum();
            }
        }
        
        return sizeSumMax;
    }
	
	private void showResults() 
	{
		for (int i = 0; i < sizeCategories.size(); i++) {
			SizeCategory sizeCat = (SizeCategory) sizeCategories.get(i);
			System.out.println(formatSizeForDisplay(sizeCat.getMinSize()) + " - " + formatSizeForDisplay(sizeCat.getMaxSize()) + " : " + sizeCat.getFileNum() + " (" + sizeCat.getFileNumPercent() + "% / " + sizeCat.getSizePercent() + "%)");
		}
	}
	
	private String formatSizeForDisplay(long sizeVal) 
	{
		StringBuffer formattedSize = new StringBuffer();
		
		long formatVal = sizeVal;
		
		if (formatVal >= GB) 
		{
			formattedSize.append(formatVal / GB);
			formattedSize.append(".");
			formatVal = formatVal % GB;
			formattedSize.append(formatVal / MB);
			formattedSize.append(" GB");
		} 
		else 
		{
			if (formatVal >= MB) 
			{
				formattedSize.append(formatVal / MB);
				formattedSize.append(".");
				formatVal = formatVal % MB;
				formattedSize.append(formatVal / KB);
				formattedSize.append(" MB");
			} 
			else 
			{
				if (formatVal >= KB) 
				{
					formattedSize.append(formatVal / KB);
					formattedSize.append(".");
					formatVal = formatVal % KB;
					formattedSize.append(formatVal);
					formattedSize.append(" KB");
				} 
				else
				{
					formattedSize.append(formatVal);
					formattedSize.append(" Byte");
				}
			}
		}
		
		return formattedSize.toString();
	}
}
