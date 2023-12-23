package de.webfilesys.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


import de.webfilesys.WebFileSys;

public class FileEncodingMap 
{
    private static final String MAPPING_FILE = "fileEncoding.conf";

    private Properties encodingMap = null;

    private static FileEncodingMap instance = null;

    private FileEncodingMap()
    {
        encodingMap = new Properties();
    	FileInputStream fin = null;

        try
        {
        	String propFilePath = WebFileSys.getInstance().getConfigBaseDir() + "/" + MAPPING_FILE;
        	
        	fin = new FileInputStream(propFilePath);

            if (LogManager.getLogger(getClass()).isDebugEnabled()) {
                LogManager.getLogger(getClass()).debug("reading file encoding map from " + propFilePath);
            }
            
        	encodingMap.load(fin);
        }
        catch (IOException ioex)
        {
            LogManager.getLogger(getClass()).error("Failed to read file encoding configuration", ioex);
        }
		finally
		{
			if (fin != null) 
			{
				try 
				{
					fin.close();
				}
				catch (IOException ex)
				{
				}
			}
		}
    }

    public synchronized static FileEncodingMap getInstance()
    {
        if (instance == null)
        {
            instance = new FileEncodingMap();
        }

        return(instance);
    }

    public String getFileEncoding(String fileName)
    {
        Enumeration keys = encodingMap.keys();
        
        while (keys.hasMoreElements()) 
        {
            String key = (String) keys.nextElement();
            
            if (PatternComparator.patternMatch(fileName, key)) 
            {
                return (String) encodingMap.get(key); 
            }
        }
        
        return null;
    }
}


