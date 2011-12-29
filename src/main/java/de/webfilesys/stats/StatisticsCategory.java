package de.webfilesys.stats;

public abstract class StatisticsCategory {
	private long fileNum = 0;
	private int fileNumPercent = 0;
	private long sizeSum = 0;
	private int sizePercent = 0;
	
	public StatisticsCategory() {
		fileNum = 0;
		sizeSum = 0;
	}
	
	public long getFileNum() {
		return fileNum;
	}
	
	public void addFile(long fileSize) {
		fileNum++;
		sizeSum += fileSize;
	}
	
	public void setFileNumPercent(int newVal) {
		fileNumPercent = newVal;
	}
	
	public int getFileNumPercent() {
		return fileNumPercent;
	}
	
	public long getSizeSum() {
	    return sizeSum;
	}
	
	public void setSizePercent(long treeFileSize) {
		sizePercent = (int) ((sizeSum * 100L) / treeFileSize);
	}
	
	public int getSizePercent() {
		return sizePercent;
	}
}
