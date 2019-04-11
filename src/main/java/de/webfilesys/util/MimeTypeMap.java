package de.webfilesys.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import de.webfilesys.WebFileSys;

public class MimeTypeMap 
{
    private HashMap<String, String> mimeTable;

    static final public String MIME_FILE = "mimetypes.conf";

    private static MimeTypeMap mimeMap = null;

    private MimeTypeMap()
    {
        mimeTable = new HashMap<String, String>();

        try
        {
        	String mimeFilePath = WebFileSys.getInstance().getConfigBaseDir() + "/" + MIME_FILE;
        	
            BufferedReader mimeReader = new BufferedReader(new FileReader(mimeFilePath));

            String line  = null;

            while ((line = mimeReader.readLine())!=null)
            {
                if (line.trim().length() > 0)
                {
                    StringTokenizer tokener = new StringTokenizer(line);

                    String type = tokener.nextToken();

                    while (tokener.hasMoreTokens())
                    {
                        mimeTable.put(tokener.nextToken().toUpperCase(),type);
                    }
                }
            }

            mimeReader.close();

        }
        catch (IOException ioex)
        {
            Logger.getLogger(getClass()).error("Failed to read mimetype configuration", ioex);
        }
    }

    public static synchronized MimeTypeMap getInstance()
    {
        if (mimeMap == null)
        {
            mimeMap = new MimeTypeMap();
        }

        return(mimeMap);
    }

    public String getMimeType(String fileName)
    {
        int dotIndex = fileName.lastIndexOf(".");

        if ((dotIndex < 0) || (dotIndex==fileName.length()-1))
        {
            return "text/plain";
        }

        String extension = fileName.substring(dotIndex+1).toUpperCase();

        String type = mimeTable.get(extension);

        if (type == null)
        {
            return "text/plain";
        }
        
        return type;
    }
}


