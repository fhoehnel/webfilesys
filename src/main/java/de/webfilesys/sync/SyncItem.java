package de.webfilesys.sync;

import java.util.Date;

public class SyncItem
{
    public static final int DIFF_TYPE_MISSING_TARGET_FILE = 1;
    public static final int DIFF_TYPE_MISSING_SOURCE_FILE = 2;
    public static final int DIFF_TYPE_MISSING_TARGET_DIR  = 3;
    public static final int DIFF_TYPE_MISSING_SOURCE_DIR  = 4;
    public static final int DIFF_TYPE_SIZE                = 5;
    public static final int DIFF_TYPE_MODIFICATION_TIME   = 6;
    public static final int DIFF_TYPE_SIZE_TIME           = 7;
    public static final int DIFF_TYPE_ACCESS_RIGHTS       = 8;

    private SyncFileInfo source = null;

    private SyncFileInfo target = null;

    private String fileName = null;
    
    private int diffType = (-1);
    
    private int id = 0;
    
    public SyncItem(int id)
    {
        this.id = id;
        
        source = new SyncFileInfo();
        target = new SyncFileInfo();
    }
    
    public int getId()
    {
        return id;
    }
    
    public SyncFileInfo getSource()
    {
        return source;
    }
    
    public SyncFileInfo getTarget()
    {
        return target;
    }
    
    public void setFileName(String newVal)
    {
        fileName = newVal;
    }
    
    public String getFileName()
    {
        return fileName;
    }

    public void setDiffType(int newVal)
    {
        diffType = newVal;
    }
    
    public int getDiffType()
    {
        return diffType;
    }
    
    public String toString()
    {
        StringBuffer buff = new StringBuffer();
        
        buff.append("SyncItem [id=");
        buff.append(id);
        buff.append(", type=");
        buff.append(diffType);

        buff.append(", fileName=");
        buff.append(fileName);

        buff.append(", source=(");
        buff.append(getSource());
        buff.append("), target=(");
        buff.append(getTarget());

        buff.append(")]");
        
        return buff.toString();
    }
    
    public String getDisplayString()
    {
        StringBuffer buff = new StringBuffer();

        if (diffType == DIFF_TYPE_MISSING_TARGET_FILE)
        {
            buff.append("target file missing: " + source.getPath());
        }
        else if (diffType == DIFF_TYPE_MISSING_SOURCE_FILE)
        {
            buff.append("source file missing: " + target.getPath());
        }
        else if (diffType == DIFF_TYPE_MISSING_TARGET_DIR)
        {
            buff.append("target folder missing: " + source.getPath());
        }
        else if (diffType == DIFF_TYPE_MISSING_SOURCE_DIR)
        {
            buff.append("source folder missing: " + target.getPath());
        }
        else if (diffType == DIFF_TYPE_SIZE)
        {
            buff.append("different size: " + source.getPath() + " source: " + getSource().getSize() + " target: " + getTarget().getSize());
        }
        else if (diffType == DIFF_TYPE_MODIFICATION_TIME)
        {
            buff.append("different modification time: " + source.getPath() 
                        + " source: " + new Date(getSource().getModificationTime()) 
                        + " target: " + new Date(getTarget().getModificationTime()));
        }
        else if (diffType == DIFF_TYPE_SIZE_TIME)
        {
            buff.append("different size and modification time: " + source.getPath() 
                        + " source: " + getSource().getSize() + " target: " + getTarget().getSize() 
                        + " source: " + new Date(getSource().getModificationTime()) + " target: " + new Date(getTarget().getModificationTime()));
        }
        else if (diffType == DIFF_TYPE_ACCESS_RIGHTS)
        {
            buff.append("different access rights: " + source.getPath() 
                        + " source: " + source.getCanRead() + "," + source.getCanWrite() 
                        + " target: " + target.getCanRead() + "," + target.getCanWrite());
        }
        
        return buff.toString();
    }
}
