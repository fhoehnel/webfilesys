package de.webfilesys;

import java.io.*;
import java.util.*;
import java.text.*;

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
            System.out.println("WinDiskUsage.getFreeSpace: invalid path " + path);
            return(0L);
        }

        String driveString=path.substring(0,2) + "\\";

        Runtime rt=Runtime.getRuntime();

        Process osProcess=null;

        String osCommand=null;
        
        int opSysType = WebFileSys.getInstance().getOpSysType();
        
        if ((opSysType == WebFileSys.OS_OS2) ||
            WebFileSys.getInstance().is32bitWindows())
        {
            osCommand = "cmd /c dir " + driveString;
        }
        else  // Win95, Win98, ME
        {
            osCommand = "command.com /c dir " + driveString;
        }

        try
        {
            osProcess=rt.exec(osCommand);
        }
        catch (Exception e)
        {
            System.out.println("WinDiskUsage.getFreeSpace: " + e);
            return(0L);
        }

        long bytesFree=0L;

        DataInputStream stdout=new DataInputStream(osProcess.getInputStream());

        String line=null;

        String lastLine=null;

        try
        {
            while ((line=stdout.readLine())!=null)
            {
                if (line.trim().length() > 0)
                {
                    lastLine=line;
                }
            }

            stdout.close();

            if (lastLine!=null)
            {
                DecimalFormat numFormat=new DecimalFormat("#,####");

                StringTokenizer lineParser=new StringTokenizer(lastLine," ");

                while (lineParser.hasMoreTokens())
                {
                    String temp=lineParser.nextToken();

                    try
                    {
                        bytesFree=numFormat.parse(temp).longValue();
                    }
                    catch (ParseException pex)
                    {
                    }
                }
            }
        }
        catch (IOException ioex)
        {
            System.out.println("WinDiskUsage.getFreeSpace: " + ioex);
            return(0L);
        }

        return(bytesFree);
    }

    public static void main(String args[])
    {
        System.out.println("free space on drive c: " + (new WinDiskUsage("c:")).getFreeSpace());
        System.out.println("free space on drive d: " + (new WinDiskUsage("d:")).getFreeSpace());
    }
}

