package de.webfilesys;

import java.util.*;

public class FileSelectionStatus
{
    private Vector selectedFiles;
    private int beginIndex;
    private int endIndex;
    private int lastPageStartIdx;
    private int numberOfFiles;
    private boolean isLastPage;

    private String firstFileName;
    private String lastFileName;
    
	/** The start indices of all pages. */
	private Vector pageStartIndices;
	
	private long fileSizeSum = 0;
	
	private int currentPage = 0;

    public FileSelectionStatus()
    {
        selectedFiles=null;
        beginIndex=0;
        endIndex=0;
        lastPageStartIdx = 0;
        numberOfFiles=0;
        isLastPage=false;
        firstFileName=null;
        lastFileName=null;
        pageStartIndices = new Vector();
    }

    public void setSelectedFiles(Vector selectedFiles)
    {
        this.selectedFiles=selectedFiles;
    }

    public Vector getSelectedFiles()
    {
        return(selectedFiles);
    }

    public void setBeginIndex(int beginIndex)
    { 
        this.beginIndex=beginIndex;
    }
    
    public int getBeginIndex()
    {
        return(beginIndex);
    }

    public void setEndIndex(int endIndex)
    { 
        this.endIndex=endIndex;
    }
    
    public int getEndIndex()
    {
        return(endIndex);
    }
    
    public void setLastPageStartIdx(int newVal)
    {
    	this.lastPageStartIdx = newVal;
    }
    
    public int getLastPageStartIdx()
    {
    	return(lastPageStartIdx);
    }

    public void setNumberOfFiles(int numberOfFiles)
    {
        this.numberOfFiles=numberOfFiles;
    }

    public int getNumberOfFiles()
    {
        return(numberOfFiles);
    }

    public void setFileSizeSum(long newVal) 
    {
    	fileSizeSum = newVal;
    }
    
    public long getFileSizeSum() 
    {
    	return fileSizeSum;
    }
    
    public void setIsLastPage(boolean isLastPage)
    { 
        this.isLastPage=isLastPage;
    }
    
    public boolean getIsLastPage()
    {
        return(isLastPage);
    }

    public void setLastFileName(String lastFileName)
    { 
        this.lastFileName=lastFileName;
    }
    
    public String getLastFileName()
    {
        return(lastFileName);
    }

    public void setFirstFileName(String firstFileName)
    { 
        this.firstFileName=firstFileName;
    }
    
    public String getFirstFileName()
    {
        return(firstFileName);
    }
    
    public void setCurrentPage(int newVal)
    {
    	this.currentPage = newVal;
    }
    
    public int getCurrentPage()
    {
    	return(currentPage);
    }
    
	/**
	 * Add a page start index.
	 *
	 * @param startIndex the start index of a page
	 */
	public void addStartIndex(int startIndex) 
	{
		pageStartIndices.addElement(new Integer(startIndex));
	}

    public Vector getPageStartIndices()
    {
    	return(pageStartIndices);
    }
}

