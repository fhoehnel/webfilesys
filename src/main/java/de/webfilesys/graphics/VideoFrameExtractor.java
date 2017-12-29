package de.webfilesys.graphics;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import org.apache.log4j.Logger;

import de.webfilesys.WebFileSys;
import de.webfilesys.util.CommonUtils;

public class VideoFrameExtractor extends Thread
{
	public static final String FRAME_TARGET_DIR = "_frames"; 
	
    String videoFilePath;
    String frameGrabTime;
    String frameSize;
    int width;
    int height;
    
    public VideoFrameExtractor(String videoPath, String frameExtractTime, int videoWidth, int videoHeight, String grabFrameSize) {
    	videoFilePath = videoPath;
    	frameGrabTime = frameExtractTime;
    	frameSize = grabFrameSize;
    	width = videoWidth;
    	height = videoHeight;
    }

    public void run() {
        if (Logger.getLogger(getClass()).isDebugEnabled()) {
            Logger.getLogger(getClass()).debug("starting video frame extractor thread for video file " + videoFilePath);
        }
        
        String ffmpegExePath = WebFileSys.getInstance().getFfmpegExePath();
        
        if (!CommonUtils.isEmpty(ffmpegExePath)) {
            
            String videoFramePath = getFramePath(videoFilePath);
            File frameDirFile = new File(videoFramePath);
            if (!frameDirFile.exists()) {
                if (!frameDirFile.mkdir()) {
                    Logger.getLogger(getClass()).error("failed to create video frame directory " + videoFramePath);
                }
            }
            
            String scaleFilter = null;
            
            if (width >= height) {
            	scaleFilter = "scale=\"" + frameSize + ":-1\"";
            } else {
            	scaleFilter = "scale=\"-1:" + frameSize + "\"";
            }
            
        	String progNameAndParams = ffmpegExePath + " -i " + videoFilePath + " -ss " + frameGrabTime + " -filter:v " + scaleFilter + " -vframes 1 " + getFfmpegOutputFileSpec(videoFilePath);

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
					    
					    File frameFile = new File(getFrameTargetPath(videoFilePath));
					    
					    if (!resultFile.renameTo(frameFile)) {
			                Logger.getLogger(getClass()).error("failed to rename result file for video frame grabbing from video " + videoFilePath);
					    }
					} else {
	                    Logger.getLogger(getClass()).error("result file from ffmpeg video frame grabbing not found: " + getFfmpegResultFilePath(videoFilePath));
					}
				} else {
					Logger.getLogger(getClass()).warn("ffmpeg returned error " + grabResult);
				}
			} catch (IOException ioex) {
				Logger.getLogger(getClass()).error("failed to grab frame from video " + videoFilePath, ioex);
			} catch (InterruptedException iex) {
				Logger.getLogger(getClass()).error("failed to grab frame from video " + videoFilePath, iex);
			}
        }
    }

    public static String getFfmpegOutputFileSpec(String videoPath) {
        int sepIdx = videoPath.lastIndexOf(File.separator);

        String basePath = videoPath.substring(0, sepIdx + 1);

        String thumbPath = basePath + FRAME_TARGET_DIR + File.separator;

        String imgFileName = videoPath.substring(sepIdx + 1);

        return(thumbPath + "%01d"+ imgFileName + ".jpg");
    }

    public static String getFfmpegResultFilePath(String videoPath) {
        int sepIdx = videoPath.lastIndexOf(File.separator);

        String basePath = videoPath.substring(0, sepIdx + 1);

        String thumbPath = basePath + FRAME_TARGET_DIR + File.separator;

        String imgFileName = videoPath.substring(sepIdx + 1);

        return(thumbPath + "1"+ imgFileName + ".jpg");
    }
    
    private static String getFramePath(String videoPath) {
        int sepIdx = videoPath.lastIndexOf(File.separator);

        if (sepIdx < 0) {
            Logger.getLogger(VideoThumbnailCreator.class).error("incorrect video file path: " + videoPath);
            return(null); 
        }

        String basePath = videoPath.substring(0, sepIdx + 1);

        return basePath + FRAME_TARGET_DIR;
    }
    
    private static String getFrameTargetPath(String videoPath) {
        int sepIdx = videoPath.lastIndexOf(File.separator);

        if (sepIdx < 0) {
            Logger.getLogger(VideoThumbnailCreator.class).error("incorrect video file path: " + videoPath);
            return(null); 
        }

        String basePath = videoPath.substring(0, sepIdx + 1);

        String thumbPath = basePath + FRAME_TARGET_DIR + File.separator;

        String imgFileName = videoPath.substring(sepIdx + 1) + ".jpg";

        return(thumbPath + imgFileName);
    }
    
}

