package de.webfilesys;
 
import java.util.*;

public class FileSysBookmark
{ 
    private String id;
    private String name;
    private String path;
    private Date creationTime;
    private Date updateTime;

    public FileSysBookmark(String id)
    {
        this.id=id;

        init();
    }

    public FileSysBookmark()
    {
        this.id="-1";
    
        init();
    }

    protected void init()
    {
        name="";
        creationTime=new Date();
        updateTime=new Date();
    }

    public void setId(String newId)
    {
        id=newId;
    }

    public String getId()
    {
        return(id);
    }

    public void setName(String newVal)
    {
        name = newVal;
    }

    public String getName()
    {
        return(name);
    }
    
    public void setPath(String newVal)
    {
    	path = newVal;
    }
    
    public String getPath()
    {
    	return(path);
    }

    public void setCreationTime(Date newCreationTime)
    {
        creationTime=newCreationTime;
    }

    public Date getCreationTime()
    {
        return(creationTime);
    }

    public void setUpdateTime(Date newUpdateTime)
    {
        updateTime=newUpdateTime;
    }

    public Date getUpdateTime()
    {
        return(updateTime);
    }

    public boolean searchMatch(String searchArg)
    {
        if ((name != null) && (name.toLowerCase().indexOf(searchArg)>=0))
        {
            return(true);
        }

        return(false);
    }
}

