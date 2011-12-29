package de.webfilesys.graphics;

import org.apache.log4j.Logger;

import de.webfilesys.WebFileSys;

public class ThumbnailThread extends ThumbnailCreatorBase implements Runnable
{
    String basePath = null;

    private int scope;

    public ThumbnailThread(String actPath)
    {
        basePath = actPath;

        scope = SCOPE_TREE;
    }

    public ThumbnailThread(String actPath, int scope)
    {
        basePath = actPath;
     
        this.scope = scope;
    }

    public void run()
    {
        if (Logger.getLogger(getClass()).isDebugEnabled())
        {
            Logger.getLogger(getClass()).debug("starting thumbnail creator thread");
        }
        
        WebFileSys.getInstance().setThumbThreadRunning(true);

        Thread.currentThread().setPriority(1);

        if (scope==SCOPE_TREE)
        {
            updateThumbnails(basePath,true);
        }
        else
        {
            if (scope == SCOPE_DIR)
            {
                updateThumbnails(basePath,false);
            }
            else
            {
                if (scope == SCOPE_FILE)
                {
                    createThumbnail(basePath);
                }
            }
        }

        WebFileSys.getInstance().setThumbThreadRunning(false);
    }

}

