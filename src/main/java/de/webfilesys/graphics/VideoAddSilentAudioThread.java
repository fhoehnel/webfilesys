package de.webfilesys.graphics;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import de.webfilesys.SubdirExistCache;
import de.webfilesys.WebFileSys;
import de.webfilesys.util.CommonUtils;

public class VideoAddSilentAudioThread extends Thread {
	public static final String TARGET_SUBDIR = "_silentAudio";
	
    private String videoFilePath;

    public VideoAddSilentAudioThread(String videoPath) {
    	videoFilePath = videoPath;
    }

    public void run() {
        if (Logger.getLogger(getClass()).isDebugEnabled()) {
            Logger.getLogger(getClass()).debug("starting thread for adding silent audio to video file " + videoFilePath);
        }
        
        Thread.currentThread().setPriority(1);

        String ffmpegExePath = WebFileSys.getInstance().getFfmpegExePath();
        
        if (!CommonUtils.isEmpty(ffmpegExePath)) {

        	String[] partsOfPath = CommonUtils.splitPath(videoFilePath);
        	
        	String sourcePath = partsOfPath[0];
        	String sourceFileName = partsOfPath[1];

            String targetPath = sourcePath + File.separator + TARGET_SUBDIR;
        	
            File targetDirFile = new File(targetPath);
            if (!targetDirFile.exists()) {
                if (!targetDirFile.mkdir()) {
                    Logger.getLogger(getClass()).error("failed to create target folder for video audio fade: " + targetPath);
                } else {
					SubdirExistCache.getInstance().setExistsSubdir(targetPath, Integer.valueOf(1));
                }
            }
            
            String targetFilePath = targetPath + File.separator + sourceFileName;
            
            boolean targetFileNameOk = true;
            do {
                File existingTargetFile = new File(targetFilePath);
                if (existingTargetFile.exists()) {
                    targetFileNameOk = false;
                    int dotIdx = targetFilePath.lastIndexOf(".");
                    targetFilePath = targetFilePath.substring(0, dotIdx) + "-1" + targetFilePath.substring(dotIdx);
                } else {
                    targetFileNameOk = true;
                }
            } while (!targetFileNameOk);
            
            // ffmpeg -f lavfi -i anullsrc=channel_layout=stereo:sample_rate=44100 -i input.mp4 -c:v copy -c:a aac -shortest output.mp4
            
            ArrayList<String> progNameAndParams = new ArrayList<String>();
            progNameAndParams.add(ffmpegExePath);

            progNameAndParams.add("-f");
            progNameAndParams.add("lavfi");

            progNameAndParams.add("-i");
            progNameAndParams.add("anullsrc=channel_layout=stereo:sample_rate=44100");
            
            progNameAndParams.add("-i");
            progNameAndParams.add(videoFilePath);
            
            progNameAndParams.add("-c:v");
            progNameAndParams.add("copy");
            
            progNameAndParams.add("-c:a");
            progNameAndParams.add("aac");
            
            progNameAndParams.add("-shortest");
            
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
				
		        DataInputStream grabProcessOut = new DataInputStream(convertProcess.getErrorStream());
		        
		        String outLine = null;
		        
		        while ((outLine = grabProcessOut.readLine()) != null) {
		        	if (Logger.getLogger(getClass()).isDebugEnabled()) {
		                Logger.getLogger(getClass()).debug("ffmpeg output: " + outLine);
		        	}
		        }
				
				int convertResult = convertProcess.waitFor();
				
				if (convertResult == 0) {
					File resultFile = new File(targetFilePath);
					if (!resultFile.exists()) {
	                    Logger.getLogger(getClass()).error("result file from ffmpeg add silent audio not found: " + targetFilePath);
					}
				} else {
					Logger.getLogger(getClass()).warn("ffmpeg returned error " + convertResult);
				}
			} catch (IOException ioex) {
				Logger.getLogger(getClass()).error("failed to add silent audio to video " + videoFilePath, ioex);
			} catch (InterruptedException iex) {
				Logger.getLogger(getClass()).error("failed to add silent audio to video " + videoFilePath, iex);
			}
        }
    }
    
}

