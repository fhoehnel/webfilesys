package de.webfilesys.graphics;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.log4j.Logger;

import de.webfilesys.WebFileSys;

public class SlideshowToVideoThread extends Thread {
	
	private static final String TARGET_VIDEO_FILENAME = "slideshow.mp4";
	
    String pictureListFilePath;
    
    String targetPath;
    
    int videoResolutionWidth;
    int videoResolutionHeight;
    
    public SlideshowToVideoThread(String pictureListFilePath, String targetPath, int videoResolutionWidth, int videoResolutionHeight) {
    	this.pictureListFilePath = pictureListFilePath;
    	this.targetPath = targetPath;
    	this.videoResolutionWidth = videoResolutionWidth;
    	this.videoResolutionHeight = videoResolutionHeight;
    }

    public void run() {
        if (Logger.getLogger(getClass()).isDebugEnabled()) {
            Logger.getLogger(getClass()).debug("starting picture slideshow video creation thread for pictures in " + pictureListFilePath);
        }
        
        Thread.currentThread().setPriority(1);
    	
        File targetDirFile = new File(targetPath);
        if (!targetDirFile.exists()) {
            if (!targetDirFile.mkdir()) {
                Logger.getLogger(getClass()).error("failed to create target folder for slideshow video: " + targetPath);
                return;
            }
        }
        
        String targetFilePath = targetPath + File.separator + TARGET_VIDEO_FILENAME;
		
        String ffmpegExePath = WebFileSys.getInstance().getFfmpegExePath();
		
        ArrayList<String> progNameAndParams = new ArrayList<String>();
        progNameAndParams.add(ffmpegExePath);
        
        progNameAndParams.add("-f");
        progNameAndParams.add("concat");
        progNameAndParams.add("-safe");
        progNameAndParams.add("0");
        progNameAndParams.add("-i");
        progNameAndParams.add(pictureListFilePath);
        progNameAndParams.add("-vf");
        progNameAndParams.add("zoompan=d=6:s=" + videoResolutionWidth + "x" + videoResolutionHeight + ":fps=1,framerate=25:interp_start=0:interp_end=255:scene=100");
        progNameAndParams.add("-c:v");
        progNameAndParams.add("h264");
        progNameAndParams.add(targetFilePath);
        
        if (Logger.getLogger(getClass()).isDebugEnabled()) {
        	StringBuilder buff = new StringBuilder();
            for (String cmdToken : progNameAndParams) {
            	buff.append(cmdToken);
            	buff.append(' ');
            }
            Logger.getLogger(getClass()).debug("ffmpeg call with params: " + buff.toString());
        }
        
		try {
			Process convertProcess = Runtime.getRuntime().exec(progNameAndParams.toArray(new String[0]));
			
	        DataInputStream convertProcessOut = new DataInputStream(convertProcess.getErrorStream());
	        
	        String outLine = null;
	        
	        while ((outLine = convertProcessOut.readLine()) != null) {
	        	if (Logger.getLogger(getClass()).isDebugEnabled()) {
	                Logger.getLogger(getClass()).debug("ffmpeg output: " + outLine);
	        	}
	        }
			
			int convertResult = convertProcess.waitFor();
			
			if (convertResult == 0) {
				File resultFile = new File(targetFilePath);
				
				if (!resultFile.exists()) {
                    Logger.getLogger(getClass()).error("result file from ffmpeg video creation not found: " + targetFilePath);
				}
			} else {
				Logger.getLogger(getClass()).warn("ffmpeg returned error " + convertResult);
			}
		} catch (IOException ioex) {
			Logger.getLogger(getClass()).error("failed to create slideshow video", ioex);
		} catch (InterruptedException iex) {
			Logger.getLogger(getClass()).error("failed to create slideshow video", iex);
		}
		
    	File fileListFile = new File(pictureListFilePath);
    	if (fileListFile.exists()) {
    		fileListFile.delete();
    	}
    }
   
}

