package de.webfilesys.graphics;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


import de.webfilesys.WebFileSys;
import de.webfilesys.util.CommonUtils;

public class VideoAudioMixerThread extends Thread {
    private String videoFilePath;
    
    ArrayList<String> audioFiles;
    
    public VideoAudioMixerThread(String videoPath) {
    	videoFilePath = videoPath;
    }

    public void setAudioFiles(ArrayList<String> newList) {
    	audioFiles = newList;
    }
    
    public void run() {
        if (LogManager.getLogger(getClass()).isDebugEnabled()) {
            LogManager.getLogger(getClass()).debug("starting video/audio mixer thread for video file " + videoFilePath);
        }
        
        Thread.currentThread().setPriority(1);
        
    	String[] partsOfPath = CommonUtils.splitPath(videoFilePath);
    	
    	String sourcePath = partsOfPath[0];
    	String sourceFileName = partsOfPath[1];

        String targetPath = sourcePath + File.separator + "_audio";
    	
        File targetDirFile = new File(targetPath);
        if (!targetDirFile.exists()) {
            if (!targetDirFile.mkdir()) {
                LogManager.getLogger(getClass()).error("failed to create target folder for video conversion: " + targetPath);
                return;
            }
        }
        
        String ffmpegExePath = WebFileSys.getInstance().getFfmpegExePath();
        
        String audioFilePath = null;
        if (audioFiles.size() == 1) {
            audioFilePath = audioFiles.get(0);
        } else {
        	audioFilePath = concatenateAudioFiles(audioFiles, targetPath, ffmpegExePath);
        	if (audioFilePath == null) {
        		return;
        	}
        }
        
        String targetFilePath = targetPath + File.separator + sourceFileName;
        
        if (!CommonUtils.isEmpty(ffmpegExePath)) {
            
            ArrayList<String> progNameAndParams = new ArrayList<String>();
            progNameAndParams.add(ffmpegExePath);
            progNameAndParams.add("-y");
            progNameAndParams.add("-i");
            progNameAndParams.add(videoFilePath);
            progNameAndParams.add("-i");
            progNameAndParams.add(audioFilePath);
            progNameAndParams.add("-map");
            progNameAndParams.add("0:v");
            progNameAndParams.add("-map");
            progNameAndParams.add("1:a");
            progNameAndParams.add("-strict");
            progNameAndParams.add("-1");
            progNameAndParams.add("-c");
            progNameAndParams.add("copy");
            progNameAndParams.add("-shortest");
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
				
		        DataInputStream convertProcessOut = new DataInputStream(convertProcess.getErrorStream());
		        
		        String outLine = null;
		        
		        while ((outLine = convertProcessOut.readLine()) != null) {
		        	if (LogManager.getLogger(getClass()).isDebugEnabled()) {
		                LogManager.getLogger(getClass()).debug("ffmpeg output: " + outLine);
		        	}
		        }
				
				int convertResult = convertProcess.waitFor();
				
				if (convertResult == 0) {
					File resultFile = new File(targetFilePath);
					
					if (!resultFile.exists()) {
	                    LogManager.getLogger(getClass()).error("result file from ffmpeg video conversion not found: " + targetFilePath);
					}
				} else {
					LogManager.getLogger(getClass()).warn("ffmpeg returned error " + convertResult);
				}
			} catch (IOException ioex) {
				LogManager.getLogger(getClass()).error("failed to convert video " + videoFilePath, ioex);
			} catch (InterruptedException iex) {
				LogManager.getLogger(getClass()).error("failed to convert video " + videoFilePath, iex);
			}
        }
        
        if (audioFiles.size() > 1) {
        	File combinedAudioFile = new File(audioFilePath);
        	if (!combinedAudioFile.delete()) {
        		LogManager.getLogger(getClass()).warn("failed to delete temporary combined audio file " + audioFilePath);
        	}
        }
    }
   
    private String concatenateAudioFiles(ArrayList<String> audioFiles, String targetPath, String ffmpegExePath) {
    	
        String combinedAudioFilePath = targetPath + File.separator + "combinedAudio.mp3";
        
        ArrayList<String> progNameAndParams = new ArrayList<String>();
        progNameAndParams.add(ffmpegExePath);
        progNameAndParams.add("-y");
        
    	for (String audioFile: audioFiles) {
        	progNameAndParams.add("-i");
        	progNameAndParams.add(audioFile);
    	}
        progNameAndParams.add("-filter_complex");
        
        StringBuilder buff = new StringBuilder();
        if (File.separatorChar == '\\') {
            buff.append("\"");
        }
        int i = 0;
    	for (String audioFile: audioFiles) {
    		buff.append("[" + i + ":a:0]");
    		i++;
    	}
    	buff.append("concat=n=");
    	buff.append(audioFiles.size());
    	buff.append(":v=0:a=1[outa]");
        if (File.separatorChar == '\\') {
            buff.append("\"");
        }
    	progNameAndParams.add(buff.toString());       
        
        progNameAndParams.add("-map");
        
        if (File.separatorChar == '\\') {
            progNameAndParams.add("\"[outa]\"");
        } else {
            progNameAndParams.add("[outa]");
        }
        
        progNameAndParams.add(combinedAudioFilePath);
        
        if (LogManager.getLogger(getClass()).isDebugEnabled()) {
        	buff = new StringBuilder();
            for (String cmdToken : progNameAndParams) {
            	buff.append(cmdToken);
            	buff.append(' ');
            }
            LogManager.getLogger(getClass()).debug("ffmpeg call with params: " + buff.toString());
        }
        
		try {
			Process convertProcess = Runtime.getRuntime().exec(progNameAndParams.toArray(new String[0]));
			
	        DataInputStream convertProcessOut = new DataInputStream(convertProcess.getErrorStream());
	        
	        String outLine = null;
	        
	        while ((outLine = convertProcessOut.readLine()) != null) {
	        	if (LogManager.getLogger(getClass()).isDebugEnabled()) {
	                LogManager.getLogger(getClass()).debug("ffmpeg output: " + outLine);
	        	}
	        }
			
			int convertResult = convertProcess.waitFor();
			
			if (convertResult == 0) {
				File resultFile = new File(combinedAudioFilePath);
				
				if (!resultFile.exists()) {
                    LogManager.getLogger(getClass()).error("result file from ffmpeg audio concatenation not found: " + combinedAudioFilePath);
				} else {
					return combinedAudioFilePath;
				}
			} else {
				LogManager.getLogger(getClass()).warn("ffmpeg returned error " + convertResult);
			}
		} catch (IOException ioex) {
			LogManager.getLogger(getClass()).error("failed to concatenate audio files", ioex);
		} catch (InterruptedException iex) {
			LogManager.getLogger(getClass()).error("failed to concatenate audio files", iex);
		}
		
		return null;
    }
}

