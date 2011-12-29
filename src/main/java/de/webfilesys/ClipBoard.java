package de.webfilesys;

import java.util.*;

public class ClipBoard extends Hashtable
{
    private final int CONTENT_TYPE_DIR =  1;
    private final int CONTENT_TYPE_FILE = 2;

    private final int OPERATION_COPY = 1;
    private final int OPERATION_MOVE = 2;

    private int operation=0;

    public ClipBoard()
    {
        super();
    }

    public void reset()
    {
        this.clear();
        operation=0;
    }

    public void addFile(String path)
    {
        put(path,new Integer(CONTENT_TYPE_FILE));
    }

    public void addDir(String path)
    {
        put(path,new Integer(CONTENT_TYPE_DIR));
    }

    public int getContentType(String ident)
    {
        Integer contentType=(Integer) get(ident);
        if (contentType==null)
        {
            return(0);
        }

        return(contentType.intValue());
    }

    public Enumeration getAllFiles()
    {
        return(getAllOfType(CONTENT_TYPE_FILE));
    }

    public Enumeration getAllDirs()
    {
        return(getAllOfType(CONTENT_TYPE_DIR));
    }

    public Enumeration getAllOfType(int typeToRemove)
    {
        Vector allFiles=null;

        Enumeration allKeys=keys();

        if (allKeys!=null)
        {
            while (allKeys.hasMoreElements())
            {
                String path=(String) allKeys.nextElement();
                int contentType=((Integer) get(path)).intValue();
                if (contentType==typeToRemove)
                {
                    if (allFiles==null)
                    {
                        allFiles=new Vector();
                    }
                    allFiles.add(path);
                }   
            }
        }

        if (allFiles==null)
        {
            return(null);
        }        

        return(allFiles.elements());
    }

    public void setOperation(int newOperation)
    {
        operation=newOperation;
    }

    public int getOperation()
    {
        return(operation);
    }

    public void setMoveOperation()
    {
        operation=OPERATION_MOVE;
    }

    public boolean isMoveOperation()
    {
        return(operation==OPERATION_MOVE);
    }
    
    public void setCopyOperation()
    {
        operation=OPERATION_COPY;
    }

    public boolean isCopyOperation()
    {
        return(operation==OPERATION_COPY);
    }
}