package de.webfilesys.graphics;

import java.io.DataInputStream;
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

    public void run() {
        if (Logger.getLogger(getClass()).isDebugEnabled()) {
            Logger.getLogger(getClass()).debug("starting video thumbnail creator thread for video file " + videoFilePath);
        }
        
        WebFileSys.getInstance().setThumbThreadRunning(true);

        Thread.currentThread().setPriority(1);

        String ffmpegExePath = WebFileSys.getInstance().getFfmpegExePath();
        
        if (!CommonUtils.isEmpty(ffmpegExePath)) {
            
            String videoThumbPath = getThumbPath(videoFilePath);
            File thumbDirFile = new File(videoThumbPath);
            if (!thumbDirFile.exists()) {
                if (!thumbDirFile.mkdir()) {
                    Logger.getLogger(getClass()).error("failed to create video thumbnail directory " + videoThumbPath);
                }
            }
            
            File videoFile = new File(videoFilePath);
            long fileSize = videoFile.length();
            
            String frameGrabTime = "00:00:01.00";
            if (fileSize > 50 * 1000000) {
                frameGrabTime = "00:00:05.00";
            } else if (fileSize > 10 * 1000000) {
                frameGrabTime = "00:00:03.00";
            } else if (fileSize > 5 * 1000000) {
                frameGrabTime = "00:00:02.00";
            }
            
        	String progNameAndParams = ffmpegExePath + " -i " + videoFilePath + " -ss " + frameGrabTime + " -filter:v scale=160:-1" + " -vframes 1 " + getFfmpegOutputFileSpec(videoFilePath);

            if (Logger.getLogger(getClass()).isDebugEnabled()) {
                Logger.getLogger(getClass()).debug("ffmpeg call with params: " + progNameAndParams);
            }
        	
			try {
				Process grabProcess = Runtime.getRuntime().exec(progNameAndParams);
				
		        DataInputStream grabProcessOut = new DataInputStream(grabProcess.getErrorStream());
		        
		        String outLine = null;
		        
		        while ((outLine = grabProcessOut.readLine()) != null) {
		        	if (Logger.getLogger(getClass()).isDebugEnabled()) {
		                Logger.getLogger(getClass()).debug("ffmpeg output: " + outLine);
		        	}
		        }
				
				int grabResult = grabProcess.waitFor();
				
				if (grabResult == 0) {
					File resultFile = new File(getFfmpegResultFilePath(videoFilePath));
					
					if (resultFile.exists()) {
					    
					    File thumbnailFile = new File(getThumbnailPath(videoFilePath));
					    
					    if (!resultFile.renameTo(thumbnailFile)) {
			                Logger.getLogger(getClass()).error("failed to rename result file for video frame grabbing from video " + videoFilePath);
					    }
					} else {
	                    Logger.getLogger(getClass()).error("result file from ffmpeg video frame grabbing not found: " + getFfmpegResultFilePath(videoFilePath));
					}
				} else {
					Logger.getLogger(getClass()).warn("ffmpeg returned error " + grabResult);
				}
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

        String basePath = videoPath.substring(0, sepIdx + 1);

        String thumbPath = basePath + THUMBNAIL_SUBDIR + File.separator;

        String imgFileName = videoPath.substring(sepIdx + 1);

        return(thumbPath + "%01d"+ imgFileName + ".jpg");
    }

    public static String getFfmpegResultFilePath(String videoPath) {
        int sepIdx = videoPath.lastIndexOf(File.separator);

        String basePath = videoPath.substring(0, sepIdx + 1);

        String thumbPath = basePath + THUMBNAIL_SUBDIR + File.separator;

        String imgFileName = videoPath.substring(sepIdx + 1);

        return(thumbPath + "1"+ imgFileName + ".jpg");
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
    
    private static String getThumbPath(String videoPath) {
        int sepIdx = videoPath.lastIndexOf(File.separator);

        if (sepIdx < 0) {
            Logger.getLogger(VideoThumbnailCreator.class).error("incorrect video file path: " + videoPath);
            return(null); 
        }

        String basePath = videoPath.substring(0, sepIdx + 1);

        return basePath + THUMBNAIL_SUBDIR;
    }
    
}

