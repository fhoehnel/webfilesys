package de.webfilesys.graphics;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


import de.webfilesys.SubdirExistCache;
import de.webfilesys.WebFileSys;
import de.webfilesys.util.CommonUtils;

public class VideoConcatAnyThread extends Thread {
	
    private static HashMap<String, String> videoFileExtensions;

    static {
    	videoFileExtensions = new HashMap<String, String>(5);
    	videoFileExtensions.put("h264", "mp4");
    	videoFileExtensions.put("mpeg2video", "mpeg");
    	videoFileExtensions.put("mp4", "mp4");
    	videoFileExtensions.put("mkv", "mkv");
    }
	
    private List<String> selectedFiles = null;
    private String cwd = null;
    private int maxVideoWidth;
    private int maxVideoHeight;
    private String newContainer;
    private String newFrameRate;
    private String targetPath;

    public VideoConcatAnyThread(String cwd, List<String> selectedFiles, int maxVideoWidth, int maxVideoHeight, String targetPath) {
    	this.cwd = cwd;
    	this.selectedFiles = selectedFiles;
    	this.maxVideoWidth = maxVideoWidth;
    	this.maxVideoHeight = maxVideoHeight;
    	this.targetPath = targetPath;
    }

    public void setNewContainer(String newVal) {
    	newContainer = newVal;
    }
    
    public void setNewFrameRate(String newVal) {
    	newFrameRate = newVal;
    }
    
    public void run() {
        if (LogManager.getLogger(getClass()).isDebugEnabled()) {
            LogManager.getLogger(getClass()).debug("starting video concatenation thread");
        }
        
        Thread.currentThread().setPriority(1);

        File targetDirFile = new File(targetPath);
        if (!targetDirFile.exists()) {
            if (!targetDirFile.mkdir()) {
                LogManager.getLogger(getClass()).error("failed to create target folder for video joining: " + targetPath);
                return;
            } else {
				SubdirExistCache.getInstance().setExistsSubdir(targetDirFile.getAbsolutePath(), Integer.valueOf(1));
            }
        }
        
        String firstFileName = selectedFiles.get(0);
        
        String fileNameOnly = firstFileName.substring(0,  firstFileName.lastIndexOf('.'));
    	String ext = videoFileExtensions.get(newContainer);
        String targetFileName = fileNameOnly + "_joined." + ext;
        String targetFilePath = targetPath + File.separator + targetFileName;
        
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
        
        String ffmpegExePath = WebFileSys.getInstance().getFfmpegExePath();
		
        ArrayList<String> progNameAndParams = new ArrayList<String>();
        progNameAndParams.add(ffmpegExePath);
        
        for (String inputFile : selectedFiles) {
        	String inputPath = CommonUtils.joinFilesysPath(cwd, inputFile);
            progNameAndParams.add("-i");
            progNameAndParams.add(inputPath);
        }

        // -filter_complex [0:v:0]scale=1920:1080[v0],[1:v:0]scale=1920:1080[v1],[v0][0:a:0][v1][1:a:0]concat=n=2:v=1:a=1[outv][outa] -map [outv] -map [outa] -r 25 -profile:v high -level:v 4.0 -pix_fmt yuv420p -strict -2 /tmp/videos/_joined/1-Schnee_joined.mp4 
        
        progNameAndParams.add("-filter_complex");
        // progNameAndParams.add("\"");
        
        // scale=1920:1080:force_original_aspect_ratio=decrease,pad=1920:1080:(ow-iw)/2:(oh-ih)/2
        
        StringBuilder buff = new StringBuilder();
        for (int i = 0; i < selectedFiles.size(); i++) {
            buff.append("[");
            buff.append(Integer.toString(i));
            buff.append(":v:0]");
            buff.append("scale=");
            buff.append(Integer.toString(maxVideoWidth));
            buff.append(":");
            buff.append(Integer.toString(maxVideoHeight));
            buff.append(":");
            buff.append("force_original_aspect_ratio=decrease,pad=");
            buff.append(Integer.toString(maxVideoWidth));
            buff.append(":");
            buff.append(Integer.toString(maxVideoHeight));
            buff.append(":(ow-iw)/2:(oh-ih)/2");
            buff.append("[v");
            buff.append(Integer.toString(i));
            buff.append("],");
        }

        for (int i = 0; i < selectedFiles.size(); i++) {
            buff.append("[v");
            buff.append(Integer.toString(i));
            buff.append("]");
            buff.append("[");
            buff.append(Integer.toString(i));
            buff.append(":a:0");
            buff.append("]");
        }
        
        buff.append("concat=n=");
        buff.append(Integer.toString(selectedFiles.size()));
        buff.append(":v=1:a=1[outv][outa]");
        
        progNameAndParams.add(buff.toString());
        
        progNameAndParams.add("-map");
        progNameAndParams.add("[outv]");
        progNameAndParams.add("-map");
        progNameAndParams.add("[outa]");
        
        // -r 25 -profile:v high -level:v 4.0 -pix_fmt yuv420p -strict -2

        progNameAndParams.add("-r");
        progNameAndParams.add(newFrameRate);
        progNameAndParams.add("-profile:v");
        progNameAndParams.add("high");
        progNameAndParams.add("-level:v");
        progNameAndParams.add("4.0");
        progNameAndParams.add("-pix_fmt");
        progNameAndParams.add("yuv420p");
        progNameAndParams.add("-strict");
        progNameAndParams.add("-2");
        
        progNameAndParams.add(targetFilePath);
        
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
                    LogManager.getLogger(getClass()).error("result file from ffmpeg video conversion not found: " + targetFilePath);
				}
			} else {
				LogManager.getLogger(getClass()).warn("ffmpeg returned error " + convertResult);
			}
			
		} catch (IOException ioex) {
			LogManager.getLogger(getClass()).error("failed to concatente videos", ioex);
		} catch (InterruptedException iex) {
			LogManager.getLogger(getClass()).error("failed to concatente videos", iex);
		}
    }
    
}

