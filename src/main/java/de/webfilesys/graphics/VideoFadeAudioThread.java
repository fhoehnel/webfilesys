package de.webfilesys.graphics;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


import de.webfilesys.SubdirExistCache;
import de.webfilesys.WebFileSys;
import de.webfilesys.util.CommonUtils;

public class VideoFadeAudioThread extends Thread {
	public static final String TARGET_SUBDIR = "_fadeAudio";
	
    private String videoFilePath;

    private int fadeInDuration;
    
    private int fadeOutDuration;
    
    public VideoFadeAudioThread(String videoPath) {
    	videoFilePath = videoPath;
    }

    public void setFadeInDuration(int newVal) {
    	fadeInDuration = newVal;
    }

    public void setFadeOutDuration(int newVal) {
    	fadeOutDuration = newVal;
    }
    
    public void run() {
        if (LogManager.getLogger(getClass()).isDebugEnabled()) {
            LogManager.getLogger(getClass()).debug("starting fade audio thread for video file " + videoFilePath);
        }
        
        Thread.currentThread().setPriority(1);

        int fadeOutStart = -1;
        
        if (fadeOutDuration > 0) {
            VideoInfoExtractor videoInfoExtractor = new VideoInfoExtractor();
            VideoInfo videoInfo = videoInfoExtractor.getVideoInfo(videoFilePath);
            if (videoInfo.getFfprobeResult() == 0) {
                if (videoInfo.getDuration().length() > 0) {
                    int durationSeconds = videoInfo.getDurationSeconds();
                    if (fadeOutDuration < durationSeconds) {
                        fadeOutStart = durationSeconds - fadeOutDuration;
                    } else {
                    	throw new IllegalArgumentException("fade out duration must be less than video duration");
                    }
                }
    		}
            if (fadeOutStart < 0) {
                LogManager.getLogger(getClass()).error("failed to determine fade out start point");
                return;
            }
        }
        
        String ffmpegExePath = WebFileSys.getInstance().getFfmpegExePath();
        
        if (!CommonUtils.isEmpty(ffmpegExePath)) {

        	String[] partsOfPath = CommonUtils.splitPath(videoFilePath);
        	
        	String sourcePath = partsOfPath[0];
        	String sourceFileName = partsOfPath[1];

            String targetPath = sourcePath + File.separator + TARGET_SUBDIR;
        	
            File targetDirFile = new File(targetPath);
            if (!targetDirFile.exists()) {
                if (!targetDirFile.mkdir()) {
                    LogManager.getLogger(getClass()).error("failed to create target folder for video audio fade: " + targetPath);
                }
            }
            
            String targetFilePath = CommonUtils.getNonConflictingTargetFilePath(targetPath + File.separator + sourceFileName);
            
            // ffmpeg -i testvideo.mp4 -filter:a "afade=in:st=0:d=1, afade=out:st=30:d=6" -c:v libx264 -c:a aac testvideo-fade.mp4
            
            StringBuffer fadeParams = new StringBuffer();
            if (fadeInDuration > 0) {
            	fadeParams.append("afade=in:st=0:d=");
            	fadeParams.append(fadeInDuration);
            	fadeParams.append(", ");
            }
            if (fadeOutStart > 0) {
            	fadeParams.append("afade=out:st=");
            	fadeParams.append(fadeOutStart);
            	fadeParams.append(":d=");
            	fadeParams.append(fadeOutDuration);
            }
            
            ArrayList<String> progNameAndParams = new ArrayList<String>();
            progNameAndParams.add(ffmpegExePath);
            progNameAndParams.add("-i");
            progNameAndParams.add(videoFilePath);
            
            progNameAndParams.add("-filter:a");
            
            progNameAndParams.add(fadeParams.toString());
            
            progNameAndParams.add("-c:v");
            progNameAndParams.add("copy");
            
            progNameAndParams.add("-c:a");
            progNameAndParams.add("aac");
            
            progNameAndParams.add(targetFilePath);
            
            if (LogManager.getLogger(getClass()).isDebugEnabled()) {
            	StringBuilder buff = new StringBuilder();
                for (String cmdToken : progNameAndParams) {
                	buff.append(cmdToken);
                	buff.append(' ');
                }
                LogManager.getLogger(getClass()).debug("ffmpeg call with params: " + buff.toString());
            }
            
			try {
				Process convertProcess = Runtime.getRuntime().exec(progNameAndParams.toArray(new String[0]));
				
		        DataInputStream grabProcessOut = new DataInputStream(convertProcess.getErrorStream());
		        
		        String outLine = null;
		        
		        while ((outLine = grabProcessOut.readLine()) != null) {
		        	if (LogManager.getLogger(getClass()).isDebugEnabled()) {
		                LogManager.getLogger(getClass()).debug("ffmpeg output: " + outLine);
		        	}
		        }
				
				int convertResult = convertProcess.waitFor();
				
				if (convertResult == 0) {
					File resultFile = new File(targetFilePath);
					if (!resultFile.exists()) {
	                    LogManager.getLogger(getClass()).error("result file from ffmpeg fade audio not found: " + targetFilePath);
					}
					SubdirExistCache.getInstance().setExistsSubdir(sourcePath, new Integer(1));
				} else {
					LogManager.getLogger(getClass()).warn("ffmpeg returned error " + convertResult);
				}
			} catch (IOException ioex) {
				LogManager.getLogger(getClass()).error("failed to fade audio in video " + videoFilePath, ioex);
			} catch (InterruptedException iex) {
				LogManager.getLogger(getClass()).error("failed to fade audio in video " + videoFilePath, iex);
			}
        }
    }
    
}

