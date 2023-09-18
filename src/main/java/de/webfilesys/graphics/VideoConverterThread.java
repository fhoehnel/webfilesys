package de.webfilesys.graphics;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


import de.webfilesys.SubdirExistCache;
import de.webfilesys.WebFileSys;
import de.webfilesys.util.CommonUtils;

public class VideoConverterThread extends Thread {
    private String videoFilePath;
    
    private String newSize;
    
    private String newHeight;
    
    private String newCodec;
    
    private String newContainerFormat;
    
    private String newFps;
    
    private String startTime;
    private String endTime;    
    
    private boolean reencode;
    
    private static HashMap<String, String> videoFileExtensions;
    
    static {
    	videoFileExtensions = new HashMap<String, String>(5);
    	videoFileExtensions.put("h264", "mp4");
    	videoFileExtensions.put("mpeg2video", "mpeg");
    	videoFileExtensions.put("mp4", "mp4");
    	videoFileExtensions.put("mkv", "mkv");
    }
    
    public VideoConverterThread(String videoPath) {
    	videoFilePath = videoPath;
    }

    public void setNewSize(String newVal) {
    	newSize = newVal;
    }
    
    public void setNewHeight(String newVal) {
    	newHeight = newVal;
    }
    
    public void setNewCodec(String newVal) {
    	newCodec = newVal;
    }
    
    public void setNewContainerFormat(String newVal) {
    	newContainerFormat = newVal;
    }
    
    public void setNewFps(String newVal) {
    	newFps = newVal;
    }
    
    public void setStartTime(String newVal) {
        startTime = newVal;
    }
    
    public void setEndTime(String newVal) {
        endTime = newVal;
    }
    
    public void setReencode(boolean newVal) {
    	reencode = newVal;
    }
    
    public boolean getReencode() {
    	return reencode;
    }
    
    public void run() {
        if (LogManager.getLogger(getClass()).isDebugEnabled()) {
            LogManager.getLogger(getClass()).debug("starting video conversion thread for video file " + videoFilePath);
        }
        
        Thread.currentThread().setPriority(1);

        String ffmpegExePath = WebFileSys.getInstance().getFfmpegExePath();
        
        if (!CommonUtils.isEmpty(ffmpegExePath)) {

	        VideoInfoExtractor videoInfoExtractor = new VideoInfoExtractor();
            VideoInfo sourceVideoInfo = videoInfoExtractor.getVideoInfo(videoFilePath);

            if (sourceVideoInfo.getFfprobeResult() == 0) {
            	String[] partsOfPath = CommonUtils.splitPath(videoFilePath);
            	
            	String sourcePath = partsOfPath[0];
            	String sourceFileName = partsOfPath[1];

                String targetPath = null;
                if (CommonUtils.isEmpty(newSize)) {
                    targetPath = sourcePath + File.separator + "_converted";
                } else {
                    targetPath = sourcePath + File.separator + newSize;
                }
            	
                File targetDirFile = new File(targetPath);
                if (!targetDirFile.exists()) {
                    if (!targetDirFile.mkdir()) {
                        LogManager.getLogger(getClass()).error("failed to create target folder for video conversion: " + targetPath);
                    }
                }
                
                String targetFilePath = targetPath + File.separator + sourceFileName;
                
                if (!CommonUtils.isEmpty(newContainerFormat)) {
                	String newExt = videoFileExtensions.get(newContainerFormat);
                	if (newExt != null) {
                		int extIdx = targetFilePath.lastIndexOf(".");
                		if (extIdx > 0) {
                    	    targetFilePath = targetFilePath.substring(0, extIdx) + "." + newExt;
                		}
                	}
                }
                
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
                
                ArrayList<String> progNameAndParams = new ArrayList<String>();
                progNameAndParams.add(ffmpegExePath);
                progNameAndParams.add("-i");
                progNameAndParams.add(videoFilePath);
                
                if ((!CommonUtils.isEmpty(startTime)) && (!CommonUtils.isEmpty(endTime))) {
                    if (CommonUtils.isEmpty(newSize) && 
                    	(CommonUtils.isEmpty(newCodec) || newCodec.equals(sourceVideoInfo.getCodec())) && 
                    	((!("h264".equals(newCodec) || CommonUtils.isEmpty(newCodec) && "h264".equals(sourceVideoInfo.getCodec()))) || !reencode) && 
                    	(CommonUtils.isEmpty(newFps) || newFps.equals(sourceVideoInfo.getFrameRate()))) {
                        // timeRangeParam = " -c copy -ss " + startTime + " -to " + endTime;
                        progNameAndParams.add("-c");
                        progNameAndParams.add("copy");
                    }
                    progNameAndParams.add("-ss");
                    progNameAndParams.add(startTime);
                    progNameAndParams.add("-to");
                    progNameAndParams.add(endTime);
                }
                
                if (!CommonUtils.isEmpty(newSize)) {
                    progNameAndParams.add("-vf");
                    if (!CommonUtils.isEmpty(newHeight)) {
                        progNameAndParams.add("scale=" + newSize + ":" + newHeight + ":force_original_aspect_ratio=decrease,pad=" + newSize + ":" + newHeight + ":(ow-iw)/2:(oh-ih)/2");
                    } else {
                        if (sourceVideoInfo.getWidth() > sourceVideoInfo.getHeight()) {
                            progNameAndParams.add("scale=" + newSize + ":-1");
                        } else {
                            progNameAndParams.add("scale=-1:" + newSize);
                        }
                    }
                }
                
                if (!CommonUtils.isEmpty(newCodec)) {
                	if (!newCodec.equals(sourceVideoInfo.getCodec())) {
                        progNameAndParams.add("-vcodec");
                        progNameAndParams.add(newCodec);

                    	String newExt = videoFileExtensions.get(newCodec);
                    	if (newExt != null) {
                    		int extIdx = targetFilePath.lastIndexOf(".");
                    		if (extIdx > 0) {
                        	    targetFilePath = targetFilePath.substring(0, extIdx) + "." + newExt;
                    		}
                    	}
                	}
                }
                
                if (!CommonUtils.isEmpty(newFps)) {
                    if (!newFps.equals(sourceVideoInfo.getFrameRate())) {
                        progNameAndParams.add("-r");
                        progNameAndParams.add(newFps);
                    }
                }
                
                if ("h264".equals(newCodec) ||
                	CommonUtils.isEmpty(newCodec) && "h264".equals(sourceVideoInfo.getCodec()))	{
                	if (reencode) {
                        // required to run on Samsung TV
                        progNameAndParams.add("-profile:v");
                        progNameAndParams.add("high");
                        progNameAndParams.add("-level:v");
                        progNameAndParams.add("4.0");
                        progNameAndParams.add("-pix_fmt");
                        progNameAndParams.add("yuv420p");
                	}
                }
                
                String addParams = WebFileSys.getInstance().getFfmpegAddParams();
                if (addParams != null) {
                	String[] params = addParams.split(" ");
                	for (String param : params) {
                        progNameAndParams.add(param);
                	}
                }
                
                progNameAndParams.add(targetFilePath);
                
                if (LogManager.getLogger(getClass()).isDebugEnabled()) {
                	StringBuilder buff = new StringBuilder();
                    for (String cmdToken : progNameAndParams) {
                    	buff.append(cmdToken);
                    	buff.append(' ');
                    }
                    LogManager.getLogger(getClass()).debug("ffmpeg call with params: " + buff.toString());
                }
                
            	// String progNameAndParams = ffmpegExePath + " -i " + videoFilePath + timeRangeParam + scaleFilter + codecFilter + frameRateFilter + addParams + " "  + targetFilePath;
                
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
    					SubdirExistCache.getInstance().setExistsSubdir(sourcePath, new Integer(1));
    				} else {
    					LogManager.getLogger(getClass()).warn("ffmpeg returned error " + convertResult);
    				}
    			} catch (IOException ioex) {
    				LogManager.getLogger(getClass()).error("failed to convert video " + videoFilePath, ioex);
    			} catch (InterruptedException iex) {
    				LogManager.getLogger(getClass()).error("failed to convert video " + videoFilePath, iex);
    			}
            }
        }
    }
    
}

