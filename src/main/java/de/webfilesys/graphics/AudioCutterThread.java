package de.webfilesys.graphics;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.webfilesys.SubdirExistCache;
import de.webfilesys.WebFileSys;
import de.webfilesys.util.CommonUtils;

public class AudioCutterThread extends Thread {
	
    private static final Logger LOG = LogManager.getLogger(AudioCutterThread.class);
	
    private String audioFilePath;
    
    private String startTime;
    private String endTime;    
    
    public AudioCutterThread(String audioPath) {
    	audioFilePath = audioPath;
    }

    public void setStartTime(String newVal) {
        startTime = newVal;
    }
    
    public void setEndTime(String newVal) {
        endTime = newVal;
    }
    
    public void run() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("starting audio cutting thread for audio file " + audioFilePath);
        }
        
        Thread.currentThread().setPriority(1);

        String ffmpegExePath = WebFileSys.getInstance().getFfmpegExePath();
        
        if (!CommonUtils.isEmpty(ffmpegExePath)) {

	        AudioInfoExtractor audioInfoExtractor = new AudioInfoExtractor();
            AudioInfo sourceAudioInfo = audioInfoExtractor.getAudioInfo(audioFilePath);

            if (sourceAudioInfo.getFfprobeResult() == 0) {
            	String[] partsOfPath = CommonUtils.splitPath(audioFilePath);
            	
            	String sourcePath = partsOfPath[0];
            	String sourceFileName = partsOfPath[1];

                String targetPath = null;
                targetPath = sourcePath + File.separator + "_audioCut";
            	
                File targetDirFile = new File(targetPath);
                if (!targetDirFile.exists()) {
                    if (!targetDirFile.mkdir()) {
                        LOG.error("failed to create target folder for audio cutting: " + targetPath);
                    }
                }
                
                String targetFilePath = CommonUtils.getNonConflictingTargetFilePath(targetPath + File.separator + sourceFileName);
                
                ArrayList<String> progNameAndParams = new ArrayList<String>();
                progNameAndParams.add(ffmpegExePath);
                progNameAndParams.add("-i");
                progNameAndParams.add(audioFilePath);
                progNameAndParams.add("-vn");
                progNameAndParams.add("-acodec");
                progNameAndParams.add("copy");
                progNameAndParams.add("-ss");
                progNameAndParams.add(startTime);
                progNameAndParams.add("-t");
                progNameAndParams.add(endTime);
                progNameAndParams.add(targetFilePath);
                
                if (LOG.isDebugEnabled()) {
                	StringBuilder buff = new StringBuilder();
                    for (String cmdToken : progNameAndParams) {
                    	buff.append(cmdToken);
                    	buff.append(' ');
                    }
                    LOG.debug("ffmpeg call with params: " + buff.toString());
                }
                
    			try {
    	            ProcessBuilder pb = new ProcessBuilder(progNameAndParams).redirectErrorStream(true);
    	            Process ffmpegProcess = pb.start();
    	            BufferedReader ffmpegOut = new BufferedReader(new InputStreamReader(ffmpegProcess.getInputStream()));
    		        
    		        String outLine = null;
    		        while ((outLine = ffmpegOut.readLine()) != null) {
    		        	if (LOG.isDebugEnabled()) {
    		                LOG.debug("ffmpeg output: " + outLine);
    		        	}
    		        }
    				
    				int convertResult = ffmpegProcess.waitFor();
    				
    				if (convertResult == 0) {
    					File resultFile = new File(targetFilePath);
    					if (!resultFile.exists()) {
    	                    LOG.error("result file from ffmpeg audio conversion not found: " + targetFilePath);
    					}
    					SubdirExistCache.getInstance().setExistsSubdir(sourcePath, new Integer(1));
    				} else {
    					LOG.warn("ffmpeg returned error " + convertResult);
    				}
    			} catch (IOException ioex) {
    				LOG.error("failed to cut audio " + audioFilePath, ioex);
    			} catch (InterruptedException iex) {
    				LOG.error("failed to cut audio " + audioFilePath, iex);
    			}
            }
        }
    }
    
}

