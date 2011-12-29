package de.webfilesys;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

public class ViewHandlerConfig 
{
	private String handlerClass = null;
	
	private Vector filePatternList = null;
	
	private Hashtable parameterMap = null;
	
	public ViewHandlerConfig()
	{
		filePatternList = new Vector();
		
		parameterMap = new Hashtable(3);
	}
	
	public void setHandlerClass(String newClass)
	{
		handlerClass = newClass;
	}
	
	public String getHandlerClass()
	{
		return(handlerClass);
	}
	
	public void addFilePattern(String newVal)
	{
		filePatternList.add(newVal);
	}
	
	public Iterator getFilePatterns()
	{
		return(filePatternList.iterator());
	}
	
	public void addParameter(String paramName, String paramValue)
	{
		parameterMap.put(paramName, paramValue);
	}
	
	public String getParameter(String paramName)
	{
		return((String) parameterMap.get(paramName));
	}
}
