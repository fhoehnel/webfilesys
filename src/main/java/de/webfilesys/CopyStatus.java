package de.webfilesys;

public class CopyStatus {
    private long treeFileSize = 0l;
    private long treeFileNum = 0l;
    private long filesCopied = 0l;
    private long bytesCopied = 0l;
    private boolean error = false;
    
    
    public void setTreeFileSize(long newVal) {
    	treeFileSize = newVal;
    }
    
    public long getTreeFileSize() {
    	return treeFileSize;
    }

    public void setTreeFileNum(long newVal) {
    	treeFileNum = newVal;
    }
    
    public long getTreeFileNum() {
    	return treeFileNum;
    }
    
    public void setFilesCopied(long newVal) {
    	filesCopied = newVal;
    }
    
    public long getFilesCopied() {
    	return filesCopied;
    }
    
    public void setBytesCopied(long newVal) {
    	bytesCopied = newVal;
    }
    
    public long getBytesCopied() {
    	return bytesCopied;
    }
    
    public void setError(boolean newVal) {
    	error = newVal;
    }
    
    public boolean getError() {
    	return error;
    }
}
