package de.webfilesys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class ViewHandlerConfig 
{
	private String handlerClass = null;
	
	private ArrayList<String> filePatternList = null;
	
	private HashMap<String, String> parameterMap = null;
	
	public ViewHandlerConfig()
	{
		filePatternList = new ArrayList<String>();
		
		parameterMap = new HashMap<String, String>(3);
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
	
	public Iterator<String> getFilePatterns()
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
