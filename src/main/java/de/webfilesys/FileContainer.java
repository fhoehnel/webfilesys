package de.webfilesys;

import java.io.File;

/**
 * @author Frank Hoehnel
 */
public class FileContainer
{
	private String name = null;
	
	private File realFile = null;
	
	private long size = -1L;
	
	private long lastModified = -1L;
	
	private boolean linkFlag = false;
	
	public FileContainer(String path, String fileName)
	{
		this.name = fileName;
		
		realFile = new File(path, fileName);
		
		linkFlag = false;
	}
	
	public FileContainer(String fileName, File file)
	{
		this.name = fileName;
		
		realFile = file;
		
		linkFlag = false;
	}
	
	public FileContainer(FileLink fileLink)
	{
		this.name = fileLink.getName();
		
		realFile = new File(fileLink.getDestPath());
		
		linkFlag = true;
	}
	
	public String getName()
	{
		return(name);
	}
	
	public File getRealFile()
	{
		return(realFile);
	}

    public boolean isLink()
    {
    	return(linkFlag);
    }
    
    public long getSize()
    {
    	return(size);
    }
    
    public void setSize(long newVal)
    {
    	size = newVal;
    }
    
    public long getLastModified()
    {
    	return(lastModified);
    }
    
    public void setLastModified(long newVal)
    {
    	lastModified = newVal;
    }
}
