package de.webfilesys;

import java.io.File;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


public class WinDiskUsage
{
    String path=null;

    public WinDiskUsage(String path)
    {
        this.path=path;
    }    

    public long getFreeSpace()
    {
        boolean validDrive=false;

        if (path.length() >= 2)
        {
            if ((path.charAt(0) >= 'a') && (path.charAt(0) <='z')) 
            {     
                validDrive=true;
            }
            else
            {
                if ((path.charAt(0) >= 'A') && (path.charAt(0) <='Z')) 
                {
                    validDrive=true;
                }
            }
        }

        if (!validDrive)
        {
            LogManager.getLogger(getClass()).error("failed to determine disk free space - invalid path: " + path);
            return(0L);
        }

        String driveString = path.substring(0,2) + "\\";

        File driveFile = new File(driveString);
        
        if (!driveFile.exists()) {
            LogManager.getLogger(getClass()).error("failed to determine disk free space - file does not exist: " + path);
            return(0L);
        }
        
        return driveFile.getFreeSpace();
    }

    public long getTotalSpace()
    {
        boolean validDrive=false;

        if (path.length() >= 2)
        {
            if ((path.charAt(0) >= 'a') && (path.charAt(0) <='z')) 
            {     
                validDrive=true;
            }
            else
            {
                if ((path.charAt(0) >= 'A') && (path.charAt(0) <='Z')) 
                {
                    validDrive=true;
                }
            }
        }

        if (!validDrive)
        {
            LogManager.getLogger(getClass()).error("failed to determine total disk space - invalid path: " + path);
            return(0L);
        }

        String driveString = path.substring(0,2) + "\\";

        File driveFile = new File(driveString);
        
        if (!driveFile.exists()) {
            LogManager.getLogger(getClass()).error("failed to determine total disk space - file does not exist: " + path);
            return(0L);
        }
        
        return driveFile.getTotalSpace();
    }
    
}

