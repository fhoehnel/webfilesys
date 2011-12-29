package de.webfilesys;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.log4j.Logger;

public class WinDriveManager extends Thread
{
	private static WinDriveManager instance = null;  
	
    private HashMap<Integer,String> driveLabels = null;
    
	private WinDriveManager() {
		driveLabels = new HashMap<Integer,String>(1);
		this.start();
	}
	
	public static WinDriveManager getInstance()
	{
		if (instance == null)
		{
			instance = new WinDriveManager();
		}
		
		return instance;
	}
	
    public synchronized void run()
    {
    	if (Logger.getLogger(getClass()).isDebugEnabled())
    	{
        	Logger.getLogger(getClass()).debug("DriveQueryThread started");
    	}
    	
        setPriority(1);

        boolean stop = false;

        while (!stop)
        {
            queryDrives();

            try
            {
                this.wait(60000);  // 60 sec
            }
            catch(InterruptedException e)
            {
                stop = true;
                Logger.getLogger(getClass()).debug("DriveQueryThread ready for shutdown");
            }
        }
    }

    public synchronized void queryDrives()
    {
        HashMap<Integer, String> newDriveLabels = new HashMap<Integer, String>(30);

        File fileSysRoots[]=File.listRoots();

        for (int i=0;i<fileSysRoots.length;i++)
        {
            String fileSysRootName=fileSysRoots[i].getAbsolutePath();

            String label=null;

            if (fileSysRootName.charAt(0)=='A')
            {
                label="Floppy";
            }
            else
            {
                if (fileSysRootName.charAt(0)!='B')
                {
                    label=queryDriveLabel(fileSysRootName);
                }
            }

            if (label==null)
            {
                label="";
            }

            char driveLetter=fileSysRootName.charAt(0);

            int driveNum=(driveLetter - 'A') + 1;

            newDriveLabels.put(new Integer(driveNum),label);
        }

        driveLabels = newDriveLabels;
    } 

    private String queryDriveLabel(String driveString)
    {
        Runtime rt=Runtime.getRuntime();

        Process labelProcess=null;

        String osCommand=null;
        
        if ((WebFileSys.getInstance().getOpSysType() == WebFileSys.OS_OS2) ||
            WebFileSys.getInstance().is32bitWindows())
        {
            osCommand="cmd /c dir " + driveString;
        }
        else  // Win95, Win98, ME
        {
            osCommand="command.com /c dir " + driveString;
        }

        try
        {
            labelProcess=rt.exec(osCommand);
        }
        catch (Exception e)
        {
        	Logger.getLogger(WinDriveManager.class).error("cannot query drive label", e);
            return(null);
        }

        DataInputStream stdout=new DataInputStream(labelProcess.getInputStream());

        try
        {
            String line=null;

            do
            {
                line=stdout.readLine();
            }
            while ((line!=null) && (line.trim().length()==0));

            stdout.close();
            
            if (line==null)
            {
                return(null);
            }
         
            int lastSpaceIdx=line.lastIndexOf(':');

            if (lastSpaceIdx<0)
            {
                return(line);
            }

            return(line.substring(lastSpaceIdx+1));
        }
        catch (IOException ioex)
        {
        	Logger.getLogger(WinDriveManager.class).error("cannot query drive label", ioex);
            return(null);
        }
    }

    public String getDriveLabel(int drive)
    {
        return ((String) driveLabels.get(new Integer(drive)));
    }
}

