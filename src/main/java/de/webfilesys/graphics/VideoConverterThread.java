package de.webfilesys.graphics;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.log4j.Logger;

import de.webfilesys.WebFileSys;
import de.webfilesys.util.CommonUtils;

public class VideoConverterThread extends Thread {
    private String videoFilePath;
    
    private String newSize;
    
    private String newCodec;
    
    private String newFps;
    
    private String startTime;
    private String endTime;     
    
    private int oldVideoWidth;
    
    private int oldVideoHeight;
    
    private String oldCodec;
    
    private String oldFrameRate = "";
    
    private static HashMap<String, String> videoFileExtensions;
    
    static {
    	videoFileExtensions = new HashMap<String, String>(5);
    	videoFileExtensions.put("h264", "mp4");
    	videoFileExtensions.put("mpeg2video", "mpeg");
    }
    
    public VideoConverterThread(String videoPath) {
    	videoFilePath = videoPath;
    }

    public void setNewSize(String newVal) {
    	newSize = newVal;
    }
    
    public void setNewCodec(String newVal) {
    	newCodec = newVal;
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
    
    public void run() {
        if (Logger.getLogger(getClass()).isDebugEnabled()) {
            Logger.getLogger(getClass()).debug("starting video conversion thread for video file " + videoFilePath);
        }
        
        // WebFileSys.getInstance().setThumbThreadRunning(true);

        Thread.currentThread().setPriority(1);

        String ffmpegExePath = WebFileSys.getInstance().getFfmpegExePath();
        
        if (!CommonUtils.isEmpty(ffmpegExePath)) {

            getSourceVideoInfo(videoFilePath);
            
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
                    Logger.getLogger(getClass()).error("failed to create target folder for video conversion: " + targetPath);
                }
            }
            
            String targetFilePath = targetPath + File.separator + sourceFileName;
            
            String scaleFilter = "";
            
            if (!CommonUtils.isEmpty(newSize)) {
                if (oldVideoWidth > oldVideoHeight) {
                    scaleFilter = " -vf scale=" + newSize + ":-1";
                } else {
                    scaleFilter = " -vf scale=-1:" + newSize;
                }
            }
            
            String codecFilter = "";
            
            if (!CommonUtils.isEmpty(newCodec)) {
            	if (!newCodec.equals(oldCodec)) {
                	codecFilter = " -vcodec " + newCodec;

                	String newExt = videoFileExtensions.get(newCodec);
                	if (newExt != null) {
                		int extIdx = targetFilePath.lastIndexOf(".");
                		if (extIdx > 0) {
                    	    targetFilePath = targetFilePath.substring(0, extIdx) + "." + newExt;
                		}
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
            
            String frameRateFilter = "";
            
            if (!CommonUtils.isEmpty(newFps)) {
                if (!newFps.equals(oldFrameRate)) {
                    frameRateFilter = " -r " + newFps;
                }
            }
            
            String timeRangeParam = "";
            
            if ((!CommonUtils.isEmpty(startTime)) && (!CommonUtils.isEmpty(endTime))) {
                if (CommonUtils.isEmpty(scaleFilter) && (CommonUtils.isEmpty(codecFilter)) && (CommonUtils.isEmpty(frameRateFilter))) {
                    timeRangeParam = " -c copy -ss " + startTime + " -to " + endTime;
                } else {
                    timeRangeParam = " -ss " + startTime + " -to " + endTime;
                }
            }
            
        	String progNameAndParams = ffmpegExePath + " -i " + videoFilePath + timeRangeParam + scaleFilter + codecFilter + frameRateFilter + " "  + targetFilePath;

            if (Logger.getLogger(getClass()).isDebugEnabled()) {
                Logger.getLogger(getClass()).debug("ffmpeg call with params: " + progNameAndParams);
            }
        	
			try {
				Process convertProcess = Runtime.getRuntime().exec(progNameAndParams);
				
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
	                    Logger.getLogger(getClass()).error("result file from ffmpeg video conversion not found: " + targetFilePath);
					}
				} else {
					Logger.getLogger(getClass()).warn("ffmpeg returned error " + convertResult);
				}
			} catch (IOException ioex) {
				Logger.getLogger(getClass()).error("failed to convert video " + videoFilePath, ioex);
			} catch (InterruptedException iex) {
				Logger.getLogger(getClass()).error("failed to convert video " + videoFilePath, iex);
			}
        }
        
        // WebFileSys.getInstance().setThumbThreadRunning(false);
    }

    private void getSourceVideoInfo(String videoFilePath) {
        String ffprobeExePath = WebFileSys.getInstance().getFfprobeExePath();
        
        if (!CommonUtils.isEmpty(ffprobeExePath)) {
    		try {

            	String progNameAndParams = ffprobeExePath +  " -v error -of flat=s=_ -select_streams v:0 -show_entries stream=height,width,codec_name,duration,avg_frame_rate -sexagesimal " + videoFilePath;

                if (Logger.getLogger(getClass()).isDebugEnabled()) {
                    Logger.getLogger(getClass()).debug("ffprobe call with params: " + progNameAndParams);
                }
            	
                String videoWidth = "";
                String videoHeight = "";
                String codec = "";
                String duration = "";

                Process ffprobeProcess = Runtime.getRuntime().exec(progNameAndParams);
    			
    	        DataInputStream ffprobeOut = new DataInputStream(ffprobeProcess.getInputStream());
    	        
    	        String outLine = null;
    	        
    	        while ((outLine = ffprobeOut.readLine()) != null) {
                    if (Logger.getLogger(getClass()).isDebugEnabled()) {
                        Logger.getLogger(getClass()).debug("ffprobe output: " + outLine);
                    }

                    if ((videoWidth.length() == 0) && outLine.contains("_width")) {
    	                String[] tokens = outLine.split("=");
    	                videoWidth = tokens[1];
    	                try {
    	                	oldVideoWidth = Integer.parseInt(videoWidth);
    	                } catch (Exception ex) {
    	                }
    	            } else if ((videoHeight.length() == 0) && outLine.contains("_height")) {
                        String[] tokens = outLine.split("=");
                        videoHeight = tokens[1];
    	                try {
    	                	oldVideoHeight = Integer.parseInt(videoHeight);
    	                } catch (Exception ex) {
    	                }
                    } else if ((codec.length() == 0) && outLine.contains("_codec")) {
                        String[] tokens = outLine.split("=");
                        oldCodec = tokens[1].substring(1, tokens[1].length() - 1);
                    } else if ((duration.length() == 0) && outLine.contains("_duration")) {
                        // streams_stream_0_duration="0:04:36.400000"
                        String[] tokens = outLine.split("=");
                        duration = tokens[1].substring(1, 8);
                    } else if ((oldFrameRate.length() == 0) && outLine.contains("_avg_frame_rate")) {
                        String[] tokens = outLine.split("=");
                        String averageFrameRate = tokens[1].substring(1, tokens[1].length() - 1);
                        tokens =  averageFrameRate.split("/");
                        if (tokens.length == 2) {
                            try {
                                int frameRatePart1 = Integer.parseInt(tokens[0]);
                                int frameRatePart2 = Integer.parseInt(tokens[1]);
                                int fps = frameRatePart1 / frameRatePart2;
                                oldFrameRate = Integer.toString(fps);
                            } catch (Exception ex) {
                                Logger.getLogger(getClass()).warn("invalid frame rate for " + videoFilePath + ": " + averageFrameRate);
                            }
                        }
                    }                    
    	        }
    			
    			int ffprobeResult = ffprobeProcess.waitFor();
    			
    			if (ffprobeResult != 0) {
    				Logger.getLogger(getClass()).warn("ffprobe returned error " + ffprobeResult);
    			}
    		} catch (IOException ioex) {
    			Logger.getLogger(getClass()).error("failed to get video dimensions for video " + videoFilePath, ioex);
    		} catch (InterruptedException iex) {
    			Logger.getLogger(getClass()).error("failed to get video dimensions for video " + videoFilePath, iex);
    		}
        }        
    }
    
}

