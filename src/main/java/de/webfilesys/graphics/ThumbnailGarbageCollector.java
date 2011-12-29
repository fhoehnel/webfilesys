package de.webfilesys.graphics;
import java.io.File;
import java.util.Date;

import org.apache.log4j.Logger;


public class ThumbnailGarbageCollector extends Thread
{
    public final static String imgFileMasks[]={"*"};

    String path;

    int removedCount=0;

    public ThumbnailGarbageCollector(String path)
    {
        this.path=path;
    }
 
    public synchronized void run()
    {
        setPriority(1);
        
        removeThumbnailGarbage();
    }

    protected void removeThumbnailGarbage()
    {
        Logger.getLogger(getClass()).debug("thumbnail garbage collector started for dir " + path);
        long startTime=(new Date()).getTime();

        removedCount=0;

        exploreTree(path);

        long endTime=(new Date()).getTime();

		Logger.getLogger(getClass()).debug("thumbnail garbage collection ended for dir " + path + " (" + (endTime-startTime) + " ms) - " + removedCount + " thumbnails removed.");
    }

    protected void exploreTree(String path)
    {
        String pathWithSlash=path;

        if (!path.endsWith(File.separator))
        {
            pathWithSlash=path + File.separator;
        }

        File dirFile=new File(path);
        
        String fileList[]=dirFile.list();

        if (fileList==null)
        {
            return;
        }

        for (int i=0;i<fileList.length;i++)
        {
            String subdirName=fileList[i];

            String subdirPath=pathWithSlash + subdirName;

            File tempFile=new File(subdirPath);

            if (tempFile.isDirectory() && tempFile.canRead())
            {
                if (subdirName.equals(ThumbnailThread.THUMBNAIL_SUBDIR))
                {
                    removeExpiredThumbnails(subdirPath);
                }                
                else
                {
                    exploreTree(subdirPath);
                }
            }
        }
    }

    protected void removeExpiredThumbnails(String thumbnailPath)
    {
        File dirFile=new File(thumbnailPath);
        
        String fileList[]=dirFile.list();

        if (fileList==null)
        {
            return;
        }

        for (int i=0;i<fileList.length;i++)
        {
            String fileName=fileList[i];

            File tempFile=new File(thumbnailPath,fileName);
            
            if (tempFile.isFile() && tempFile.canWrite())
            {
                String imgPath=thumbnailPath.substring(0,thumbnailPath.lastIndexOf(File.separatorChar)+1); 

                String imgFileName=imgPath + fileName;
                
                File imgFile=new File(imgFileName);

                if ((!imgFile.exists()) || (!imgFile.isFile()))
                {
                    if (!tempFile.delete())
                    {
						Logger.getLogger(getClass()).warn("cannot remove thumbnail garbage file " + tempFile);
                    }
                    else
                    {
                        removedCount++;

                        if (Logger.getLogger(getClass()).isDebugEnabled())
                        {
    						Logger.getLogger(getClass()).debug("removed thumbnail garbage file: " + tempFile);
                        }
                    }
                }
            }
        }

        fileList=dirFile.list();

        if ((fileList!=null) && (fileList.length==0))
        {
            if (dirFile.delete())
            {
                if (Logger.getLogger(getClass()).isDebugEnabled())
                {
    				Logger.getLogger(getClass()).debug("removed empty thumbnail dir " + thumbnailPath);
                }
            }
            else
            {
				Logger.getLogger(getClass()).warn("cannot delete empty thumbnail dir " + thumbnailPath);
            }
        }
    }

}
