package de.webfilesys.sync;

public class SyncFileInfo
{
    private String path = null;

    private boolean canRead = false;
    
    private boolean canWrite = false;
    
    private long size = 0L;
    
    private long modificationTime = 0L;
    
    public void setPath(String newVal)
    {
        path = newVal;
    }
    
    public String getPath()
    {
        return path;
    }

    public void setCanRead(boolean newVal)
    {
        canRead = newVal;
    }
    
    public boolean getCanRead()
    {
        return canRead;
    }
    
    public void setCanWrite(boolean newVal)
    {
        canWrite = newVal;
    }
    
    public boolean getCanWrite()
    {
        return canWrite;
    }
    
    public void setSize(long newVal)
    {
        size = newVal;
    }

    public long getSize()
    {
        return size;
    }
    
    public void setModificationTime(long newVal)
    {
        modificationTime = newVal;
    }
    
    public long getModificationTime()
    {
        return modificationTime;
    }
    
    public String toString()
    {
        StringBuffer buff = new StringBuffer();
        
        buff.append("FileSyncInfo [");
        buff.append("path=");
        buff.append(path);        
        buff.append(", canRead=");
        buff.append(canRead);
        buff.append(", canWrite=");
        buff.append(canWrite);
        buff.append(", size=");
        buff.append(size);
        buff.append(", modified=");
        buff.append(modificationTime);
        buff.append("]");
        
        return buff.toString();
    }
    
}
