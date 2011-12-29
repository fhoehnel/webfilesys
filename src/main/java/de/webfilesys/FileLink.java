package de.webfilesys;

import java.util.*;

public class FileLink
{
	private String name = null;
	
	private String destPath = null;
	
	private String creator = null;

	private Date creationDate = null;

	public FileLink(String name, String destPath, String user)
	{
		this.name = name;
		this.destPath = destPath;
		this.creator = user;
		this.creationDate = new Date();
	}

	public FileLink(String name, String destPath, String user, Date creationDate)
	{
		this.name = name;
		this.destPath = destPath;
		this.creator = user;
		this.creationDate = creationDate;
	}

	public String getName()
	{
		return(name);
	}

	public String getDestPath()
	{
		return(destPath);
	}

	public String getCreator()
	{
		return(creator);
	}

	public Date getCreationDate()
	{
		return(creationDate);
	}

	public long getCreationTime()
	{
		return(creationDate.getTime());
	}
}