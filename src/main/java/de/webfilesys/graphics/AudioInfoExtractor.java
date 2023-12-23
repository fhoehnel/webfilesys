package de.webfilesys.graphics;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


import de.webfilesys.WebFileSys;
import de.webfilesys.util.CommonUtils;

public class AudioInfoExtractor {

    private static final Logger LOG = LogManager.getLogger(AudioInfoExtractor.class);

    private ArrayList<String> cmdOutput = new ArrayList<>();
    
    public AudioInfo getAudioInfo(String audioFilePath) throws IllegalArgumentException {

        AudioInfo audioInfo = new AudioInfo();
    	
        audioInfo.setFfprobeResult(readProbeOutput(audioFilePath));

        if (cmdOutput.isEmpty()) {
        	audioInfo.setFfprobeEmptyOutput(true);
        } else {
        	String durationLabel = "Duration:";
            String duration = null;
            for (String outLine : cmdOutput) {
            	int durationLabelIdx = outLine.indexOf(durationLabel);
                if (durationLabelIdx >= 0) {
                    // Duration: 00:00:19.20, start: 0.025057, bitrate: 128 kb/s
                	int durationIdx = durationLabelIdx + durationLabel.length() + 1;
                	duration = outLine.substring(durationIdx, durationIdx + 8);
                	LOG.debug("extracted duration: " + duration);
                    audioInfo.setDuration(duration);
                    String[] partsOfDuration = duration.split(":");
                    if (partsOfDuration.length == 3) {
                        try {
                            int durationSeconds = (Integer.parseInt(partsOfDuration[0]) * 3600) + (Integer.parseInt(partsOfDuration[1]) * 60) + Integer.parseInt(partsOfDuration[2]);
                            audioInfo.setDurationSeconds(durationSeconds);
                        } catch (Exception ex) {
                        	LOG.warn("invalid audio duration: " + duration);
                        }
                    }
                } 
            }
        }
        
        return audioInfo;
    }
    
    private int readProbeOutput(String audioFilePath) throws IllegalArgumentException {

        File audioFile = new File(audioFilePath);

        if ((!audioFile.exists()) || (!audioFile.isFile()) || (!audioFile.canRead())) {
            LOG.warn("not a readable file: " + audioFilePath);
            throw new IllegalArgumentException("audio file is not a readable file: " + audioFilePath);
        }        

        String ffprobeExePath = WebFileSys.getInstance().getFfprobeExePath();
        
        if (CommonUtils.isEmpty(ffprobeExePath)) {
            throw new IllegalArgumentException("ffprobe executable path not configured");
        }
        
        try {
            // String progNameAndParams = ffprobeExePath +  " -i " + audioFile.getAbsolutePath();
            
            ArrayList<String> progNameAndParams = new ArrayList<String>();
            progNameAndParams.add(ffprobeExePath);
            progNameAndParams.add("-i");
            progNameAndParams.add(audioFile.getAbsolutePath());
            
            if (LOG.isDebugEnabled()) {
            	StringBuilder buff = new StringBuilder();
                for (String cmdToken : progNameAndParams) {
                	buff.append(cmdToken);
                	buff.append(' ');
                }
                LOG.debug("ffprobe call with params: " + buff.toString());
            }
            
            ProcessBuilder pb = new ProcessBuilder(progNameAndParams).redirectErrorStream(true);
            Process ffprobeProcess = pb.start();
            BufferedReader ffprobeOut = new BufferedReader(new InputStreamReader(ffprobeProcess.getInputStream()));
        	
            String outLine = null;
            
            while ((outLine = ffprobeOut.readLine()) != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("ffprobe output: " + outLine);
                }
                cmdOutput.add(outLine);
            }

            int ffprobeResult = ffprobeProcess.waitFor();

            if (ffprobeResult != 0) {
                LOG.warn("ffprobe returned error " + ffprobeResult);
            }
            
            return ffprobeResult;
        } catch (IOException ioex) {
            LOG.error("failed to get audio info for file " + audioFile, ioex);
        } catch (InterruptedException iex) {
            LOG.error("failed to get audio info for file " + audioFile, iex);
        }
        
        return -1;
    }
    
}
