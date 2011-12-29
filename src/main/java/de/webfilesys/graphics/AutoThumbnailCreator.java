package de.webfilesys.graphics;

import java.util.Vector;

import org.apache.log4j.Logger;

import de.webfilesys.WebFileSys;

public class AutoThumbnailCreator extends ThumbnailCreatorBase implements Runnable
{
    private Vector queue = null;
    
    boolean shutdownFlag = false;
    
    private Thread thumbnailThread = null;
    
    private static AutoThumbnailCreator thumbCreator = null;
    
    private AutoThumbnailCreator()
    {
		if (WebFileSys.getInstance().getJavaVersion().startsWith("1.1") || WebFileSys.getInstance().getJavaVersion().startsWith("1.2"))
		{
			imgFileMasks=new String[2];
			imgFileMasks[0]="*.jpg";
			imgFileMasks[1]="*.jpeg";
		}
    	
    	queue = new Vector();
    	
    	shutdownFlag = false;

    }
    
    public static boolean instanceCreated()
    {
    	return(thumbCreator != null);
    }
    
    public static AutoThumbnailCreator getInstance()
    {
    	if (thumbCreator == null)
    	{
    		thumbCreator = new AutoThumbnailCreator();
            Thread thumbThread = new Thread(thumbCreator);
            thumbThread.start();
            thumbCreator.setThumbnailThread(thumbThread);
    	}
    	
    	return(thumbCreator);
    }
    
    public void setThumbnailThread(Thread thumbThread)
    {
        thumbnailThread = thumbThread;
    }
    
    public void interrupt() 
    {
        if ((thumbnailThread != null) && thumbnailThread.isAlive() && (!thumbnailThread.isInterrupted()))
        {
             thumbnailThread.interrupt();    
        }
    }
    
    public void run()
    {
        Logger.getLogger(getClass()).info("AutoThumbnailCreator started");

        Thread.currentThread().setPriority(1);

    	while (!shutdownFlag)
    	{
    		while (queue.size() > 0)
    		{
    			QueueElem elem = (QueueElem) queue.elementAt(0);
    			
    			updateThumbnails(elem);
    			
    			synchronized (queue)
    			{
    				queue.removeElementAt(0);
    			}
    			
    			Logger.getLogger(getClass()).debug("size of AutoThumbnailCreator queue: " + queue.size());
    		}

            try
            {
            	synchronized (this)
            	{
					wait();
            	}
            }
            catch (InterruptedException intEx)
            {
            	shutdownFlag = true;
            }
    	}

		Logger.getLogger(getClass()).info("AutoThumbnailCreator shutting down");
    }
    
    public synchronized void queuePath(String path, int scope)
    {
    	synchronized (queue)
    	{
			queue.add(new QueueElem(path, scope));
    	}
    	
		notify();
    }
    
    private void updateThumbnails(QueueElem queueElem)
    {
        if (Logger.getLogger(getClass()).isDebugEnabled())
        {
            Logger.getLogger(getClass()).debug("updating thumbnail(s) of " + queueElem.getPath());
        }
		
		if (queueElem.getScope() == SCOPE_TREE)
		{
			updateThumbnails(queueElem.getPath(),true);
		}
		else
		{
			if (queueElem.getScope() == SCOPE_DIR)
			{
				updateThumbnails(queueElem.getPath(),false);
			}
			else
			{
				if (queueElem.getScope() == SCOPE_FILE)
				{
					createThumbnail(queueElem.getPath());
				}
			}
		}
    }

    public class QueueElem
    {
    	private String path = null;
    	
    	private int scope = 0;
    	
    	public QueueElem(String path, int scope)
    	{
    		this.path = path;
    		this.scope = scope;
    	}
    	
    	public String getPath()
    	{
    		return(path);
    	}
    	
    	public int getScope()
    	{
    		return(scope);
    	}
    }
}
