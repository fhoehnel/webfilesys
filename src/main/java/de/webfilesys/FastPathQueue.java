package de.webfilesys;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Vector;

import org.apache.log4j.Logger;

public class FastPathQueue
{
    public static final String FAST_PATH_DIR = "fastpath";

    private String fastPathFileName=null;

    static final int MAX_QUEUE_SIZE = 25;

    private Vector pathQueue=null;
    
    FastPathQueue(String userid)
    {
    	fastPathFileName = WebFileSys.getInstance().getConfigBaseDir() + "/" + FAST_PATH_DIR + "/" + userid + ".dat";
        
        if (!loadFromFile())
        {
            pathQueue = new Vector(MAX_QUEUE_SIZE);
        }
    }

    private boolean loadFromFile()
    {
        ObjectInputStream fastPathFile;

        try
        {
            fastPathFile=new ObjectInputStream(new FileInputStream(fastPathFileName));
            pathQueue=(Vector) fastPathFile.readObject();
            fastPathFile.close();
        }
        catch (ClassNotFoundException cnfe)
        {
        	Logger.getLogger(getClass()).warn(cnfe);
            return(false);
        }
        catch (IOException ioe)
        {
        	Logger.getLogger(getClass()).warn(ioe);
            return(false);
        }

        return(true);
    }

    public void saveToFile()
    {
        File fastPathDir = new File(WebFileSys.getInstance().getConfigBaseDir() + "/" + FAST_PATH_DIR);

        if (!fastPathDir.exists())
        {
            if (!fastPathDir.mkdirs())
            {
            	Logger.getLogger(getClass()).warn("cannot create fastpath directory " + fastPathDir);
            }

            return;
        }

        ObjectOutputStream fastPathFile = null;

        try
        {
            fastPathFile = new ObjectOutputStream(new FileOutputStream(fastPathFileName));
            fastPathFile.writeObject(pathQueue);
            fastPathFile.flush();
        }
        catch (IOException ioEx)
        {
        	Logger.getLogger(getClass()).warn(ioEx);
        }
        finally
        {
            if (fastPathFile != null)
            {
                try
                {
                    fastPathFile.close();
                }
                catch (Exception ex)
                {
                }
            }
        }
    }

    public synchronized void queuePath(String pathName)
    {
        // remove trailing separator char

        if (File.separatorChar=='/')
        {
            if ((pathName.length() > 1) && pathName.endsWith("/"))
            {
                pathName=pathName.substring(0,pathName.length()-1);
            }
        }
        else
        {
            if ((pathName.length() > 3) && pathName.endsWith("\\"))
            {
                pathName=pathName.substring(0,pathName.length()-1);
            }
        }

        pathQueue.insertElementAt(pathName,0);

        for (int i=1;i<pathQueue.size();i++)
        {
            String actPath=(String) pathQueue.elementAt(i);

            if (actPath.equals(pathName))
            {
                pathQueue.removeElementAt(i);
            }
        }

        if (pathQueue.size() > MAX_QUEUE_SIZE)
        {
            pathQueue.removeElementAt(pathQueue.size()-1);
        }
    }

    public Vector getPathVector()
    {
        return(pathQueue);
    }

}