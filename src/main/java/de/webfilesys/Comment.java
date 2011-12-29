package de.webfilesys;

import java.util.*;

public class Comment
{
    private String user=null;

    private Date creationDate=null;

    private String message=null;

    public Comment(String user,Date creationDate, String message)
    {
        this.user=user;
        this.creationDate=creationDate;
        this.message=message;
    }

    public String getUser()
    {
        return(user);
    }

    public Date getCreationDate()
    {
        return(creationDate);
    }

    public long getCreationTime()
    {
        return(creationDate.getTime());
    }

    public String getMessage()
    {
        return(message);
    }
}