package de.webfilesys.graphics;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import de.webfilesys.WebFileSys;
import de.webfilesys.util.CommonUtils;

public class VideoThumbnailCreator extends Thread
{
    public static final String THUMBNAIL_SUBDIR = "_thumbnails";

    String videoFilePath;
    
    public VideoThumbnailCreator(String videoPath) {
    	videoFilePath = videoPath;
    }

    /*
    public VideoThumbnailThread(String actPath, int scope)
    {
        basePath = actPath;
     
        this.scope = scope;
    }
    */

    public void run() {
        if (Logger.getLogger(getClass()).isDebugEnabled()) {
            Logger.getLogger(getClass()).debug("starting video thumbnail creator thread for video file " + videoFilePath);
        }
        
        WebFileSys.getInstance().setThumbThreadRunning(true);

        Thread.currentThread().setPriority(1);

        String ffmpegExePath = WebFileSys.getInstance().getFfmpegExePath();
        
        if (!CommonUtils.isEmpty(ffmpegExePath)) {
        	String progNameAndParams = ffmpegExePath + " -i " + videoFilePath + " -ss 00:00:01.00 -vframes 1 " + getFfmpegOutputFileSpec(videoFilePath);

            if (Logger.getLogger(getClass()).isDebugEnabled()) {
                Logger.getLogger(getClass()).debug("ffmpeg call with params: " + progNameAndParams);
            }
        	
			try {
				Process grabProcess = Runtime.getRuntime().exec(progNameAndParams);

				grabProcess.waitFor();
			} catch (IOException ioex) {
				Logger.getLogger(getClass()).error("failed to grab frame for thumbnail from video " + videoFilePath, ioex);
			} catch (InterruptedException iex) {
				Logger.getLogger(getClass()).error("failed to grab frame for thumbnail from video " + videoFilePath, iex);
			}
        }
        
        WebFileSys.getInstance().setThumbThreadRunning(false);
    }

    public static String getFfmpegOutputFileSpec(String videoPath) {
        int sepIdx = videoPath.lastIndexOf(File.separator);

        if (sepIdx < 0) {
            Logger.getLogger(VideoThumbnailCreator.class).error("incorrect video file path: " + videoPath);
            return(null); 
        }

        String basePath = videoPath.substring(0, sepIdx + 1);

        String thumbPath = basePath + THUMBNAIL_SUBDIR + File.separator;

        String imgFileName = videoPath.substring(sepIdx + 1);

        return(thumbPath + imgFileName + "%03d.jpg");
    }
    
    public static String getThumbnailPath(String videoPath) {
        int sepIdx = videoPath.lastIndexOf(File.separator);

        if (sepIdx < 0) {
            Logger.getLogger(VideoThumbnailCreator.class).error("incorrect video file path: " + videoPath);
            return(null); 
        }

        String basePath = videoPath.substring(0, sepIdx + 1);

        String thumbPath = basePath + THUMBNAIL_SUBDIR + File.separator;

        String imgFileName = videoPath.substring(sepIdx + 1) + ".jpg";

        return(thumbPath + imgFileName);
    }
    
}

