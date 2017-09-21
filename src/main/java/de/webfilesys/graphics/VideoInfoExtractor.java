package de.webfilesys.graphics;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import de.webfilesys.WebFileSys;
import de.webfilesys.util.CommonUtils;

public class VideoInfoExtractor {

    private static final Logger LOG = Logger.getLogger(VideoInfoExtractor.class);

    public VideoInfo getVideoInfo(String videoFilePath) 
    throws IllegalArgumentException {

        File videoFile = new File(videoFilePath);

        if ((!videoFile.exists()) || (!videoFile.isFile()) || (!videoFile.canRead())) {
            LOG.warn("not a readable file: " + videoFilePath);
            throw new IllegalArgumentException("video file is not a readable file: " + videoFilePath);
        }        

        String ffprobeExePath = WebFileSys.getInstance().getFfprobeExePath();
        
        if (CommonUtils.isEmpty(ffprobeExePath)) {
            throw new IllegalArgumentException("ffprobe executable path not configured");
        }
        
        VideoInfo videoInfo = new VideoInfo();
        
        try {

            String progNameAndParams = ffprobeExePath +  " -v error -of flat=s=_ -select_streams v:0 -show_entries stream=height,width,codec_name,duration,avg_frame_rate -sexagesimal " + videoFile.getAbsolutePath();

            if (LOG.isDebugEnabled()) {
                LOG.debug("ffprobe call with params: " + progNameAndParams);
            }
            
            String videoWidth = null;
            String videoHeight = null;
            String codec = null;
            String duration = null;
            String averageFrameRate = null;
            
            Process ffprobeProcess = Runtime.getRuntime().exec(progNameAndParams);
            
            DataInputStream ffprobeOut = new DataInputStream(ffprobeProcess.getInputStream());
            
            String outLine = null;
            
            while ((outLine = ffprobeOut.readLine()) != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("ffprobe output: " + outLine);
                }

                if ((videoWidth == null) && outLine.contains("_width")) {
                    String[] tokens = outLine.split("=");
                    videoWidth = tokens[1];
                    try {
                        videoInfo.setWidth(Integer.parseInt(videoWidth));
                    } catch (Exception ex) {
                        LOG.warn("invalid video width: " + videoWidth);
                    }
                } else if ((videoHeight == null) && outLine.contains("_height")) {
                    String[] tokens = outLine.split("=");
                    videoHeight = tokens[1];
                    try {
                        videoInfo.setHeight(Integer.parseInt(videoHeight));
                    } catch (Exception ex) {
                        LOG.warn("invalid video height: " + videoHeight);
                    }
                } else if ((codec == null) && outLine.contains("_codec")) {
                    String[] tokens = outLine.split("=");
                    codec = tokens[1].substring(1, tokens[1].length() - 1);
                    videoInfo.setCodec(codec);
                } else if ((duration == null) && outLine.contains("_duration")) {
                    // streams_stream_0_duration="0:04:36.400000"
                    String[] tokens = outLine.split("=");
                    duration = tokens[1].substring(1, 8);
                    videoInfo.setDuration(duration);
                } else if ((averageFrameRate == null) && outLine.contains("_avg_frame_rate")) {
                    String[] tokens = outLine.split("=");
                    averageFrameRate = tokens[1].substring(1, tokens[1].length() - 1);
                    tokens =  averageFrameRate.split("/");
                    if (tokens.length == 2) {
                        try {
                            int frameRatePart1 = Integer.parseInt(tokens[0]);
                            int frameRatePart2 = Integer.parseInt(tokens[1]);
                            videoInfo.setFrameRate(frameRatePart1 / frameRatePart2);
                        } catch (Exception ex) {
                            Logger.getLogger(getClass()).warn("invalid frame rate for " + videoFile + ": " + averageFrameRate);
                        }
                    }
                }                    
            }
            
            int ffprobeResult = ffprobeProcess.waitFor();
            
            if (ffprobeResult != 0) {
                LOG.warn("ffprobe returned error " + ffprobeResult);
            }
        } catch (IOException ioex) {
            LOG.error("failed to get video dimensions for video " + videoFile, ioex);
        } catch (InterruptedException iex) {
            LOG.error("failed to get video dimensions for video " + videoFile, iex);
        }
        
        return videoInfo;
    }
}
