package de.webfilesys.calendar;

import java.util.*;

public class Note
{
    private String id;
    private String subject;
    private String content;
	private String category;
    private Date creationTime;
    private Date updateTime;

    public Note(String id)
    {
        this.id=id;

        init();
    }

    public Note()
    {
        this.id="-1";
    
        init();
    }

    protected void init()
    {
        subject="";
        content="";
		category="";
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

    public void setSubject(String newSubject)
    {
        subject=newSubject;
    }

    public String getSubject()
    {
        return(subject);
    }

    public void setContent(String newContent)
    {
        content=newContent;
    }

    public String getContent()
    {
        return(content);
    }

	public void setCategory(String newCategory)
	{
		category=newCategory;
	}

	public String getCategory()
	{
		return(category);
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
        if ((subject!=null) && (subject.toLowerCase().indexOf(searchArg)>=0))
        {
            return(true);
        }

        if ((content!=null) && (content.toLowerCase().indexOf(searchArg)>=0))
        {
            return(true);
        }

        return(false);
    }
}

