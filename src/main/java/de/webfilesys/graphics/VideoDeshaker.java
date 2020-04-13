package de.webfilesys.graphics;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import de.webfilesys.WebFileSys;
import de.webfilesys.util.CommonUtils;

public class VideoDeshaker extends Thread {
	public static final String DESHAKE_TARGET_DIR = "_stabilized"; 
	
	private ArrayList<String> processQueue = null;
	
    public VideoDeshaker(String videoFilePath) {
    	processQueue = new ArrayList<String>();
    	processQueue.add(videoFilePath);
    }
    
    public VideoDeshaker(ArrayList<String> videoFilePathList) {
    	processQueue = videoFilePathList;
    }

    public void run() {
        if (Logger.getLogger(getClass()).isDebugEnabled()) {
            Logger.getLogger(getClass()).debug("starting video deshaker thread for " + processQueue.size() + " video files");
        }
        
        String ffmpegExePath = WebFileSys.getInstance().getFfmpegExePath();
        
        if (!CommonUtils.isEmpty(ffmpegExePath)) {
        	
        	for (String videoFilePath : processQueue) {
        		String targetVideoPath = getTargetPath(videoFilePath);
                File targetVideoDir = new File(targetVideoPath);
                if (!targetVideoDir.exists()) {
                    if (!targetVideoDir.mkdir()) {
                        Logger.getLogger(getClass()).error("failed to create target directory for deshaked video " + targetVideoPath);
                        return;
                    }
                }

                File transformFile = new File(getTransformFilePath(targetVideoPath));
                if (transformFile.exists()) {
                    Logger.getLogger(getClass()).error("transform file for video deshaking still exists: " + transformFile + " - an other deshaking process seems to be running");
                    return;
                }

                if (Logger.getLogger(getClass()).isDebugEnabled()) {
                    Logger.getLogger(getClass()).debug("starting video deshaking for file " + videoFilePath);
                }
                
                if (prepareStabilizer(videoFilePath, targetVideoPath)) {
                	runStabilizer(videoFilePath, targetVideoPath);
                	
                	removeTransformFile(targetVideoPath);
                }
        	}
        }
    }

    private boolean prepareStabilizer(String videoFilePath, String targetVideoPath) {
        
    	// ffmpeg -i original.mp4 -vf vidstabdetect=stepsize=5:shakiness=7:accuracy=15:result=transform.trf -f null -
        
        int stepsize = 5;
        int shakiness = 7;
        int accuracy = 15;
        
        String transformFile = getEscapedTransformFilePath(targetVideoPath);
        
        ArrayList<String> progNameAndParams = new ArrayList<String>();
        progNameAndParams.add(WebFileSys.getInstance().getFfmpegExePath());
        progNameAndParams.add("-i");
        progNameAndParams.add(videoFilePath);
        progNameAndParams.add("-vf");
        progNameAndParams.add("vidstabdetect=stepsize=" + stepsize + ":shakiness=" + shakiness + ":accuracy=" + accuracy + ":result='" + transformFile + "'");
        
        progNameAndParams.add("-f");
        progNameAndParams.add("null");
        progNameAndParams.add("-");
        
        if (Logger.getLogger(getClass()).isDebugEnabled()) {
        	StringBuilder buff = new StringBuilder();
            for (String cmdToken : progNameAndParams) {
            	buff.append(cmdToken);
            	buff.append(' ');
            }
            Logger.getLogger(getClass()).debug("ffmpeg call with params: " + buff.toString());
        }
    	
		try {
			Process deshakeProcess = Runtime.getRuntime().exec(progNameAndParams.toArray(new String[0]));
			
	        DataInputStream processOut = new DataInputStream(deshakeProcess.getErrorStream());
	        
	        String outLine = null;
	        
	        while ((outLine = processOut.readLine()) != null) {
	        	if (Logger.getLogger(getClass()).isDebugEnabled()) {
	                Logger.getLogger(getClass()).debug("ffmpeg output: " + outLine);
	        	}
	        }
			
			int deshakeResult = deshakeProcess.waitFor();
			
			if (deshakeResult == 0) {
				File resultFile = new File(getTransformFilePath(targetVideoPath));
				
				if (resultFile.exists()) {
					return true;
				} 
                Logger.getLogger(getClass()).error("transform file for ffmpeg video deshaking not found: " + resultFile);
			} else {
				Logger.getLogger(getClass()).warn("ffmpeg deshaking returned error " + deshakeResult);
			}
		} catch (IOException ioex) {
			Logger.getLogger(getClass()).error("failed to deshake video " + videoFilePath, ioex);
		} catch (InterruptedException iex) {
			Logger.getLogger(getClass()).error("failed to deshake video " + videoFilePath, iex);
		}
		return false;
    }

    private boolean runStabilizer(String videoFilePath, String targetVideoPath) {
        
    	// ffmpeg -i original.mp4 -vf vidstabtransform=input="transform.trf":zoom=2:smoothing=12,unsharp=5:5:0.8:3:3:0.4 -vcodec libx264 -preset slow -tune film -crf 18 -acodec copy stabilisiert.mp4
        
        String transformFile = getEscapedTransformFilePath(targetVideoPath);
        
        int smoothing = 12;
        
        ArrayList<String> progNameAndParams = new ArrayList<String>();
        progNameAndParams.add(WebFileSys.getInstance().getFfmpegExePath());
        progNameAndParams.add("-i");
        progNameAndParams.add(videoFilePath);
        progNameAndParams.add("-vf");
        progNameAndParams.add("vidstabtransform=input='" + transformFile + "':zoom=2:smoothing=" + smoothing + ",unsharp=5:5:0.8:3:3:0.4");
        progNameAndParams.add("-vcodec");
        progNameAndParams.add("libx264");
        progNameAndParams.add("-preset");

        
        progNameAndParams.add("slow");
        progNameAndParams.add("-tune");
        progNameAndParams.add("film");
        progNameAndParams.add("-crf");
        progNameAndParams.add("18");
        progNameAndParams.add("-acodec");
        progNameAndParams.add("copy");
        
    	String[] partsOfPath = CommonUtils.splitPath(videoFilePath);
    	String sourceFileName = partsOfPath[1];
    	
        String targetFilePath = CommonUtils.getNonConflictingTargetFilePath(targetVideoPath + File.separator + sourceFileName);
        
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
			Process deshakeProcess = Runtime.getRuntime().exec(progNameAndParams.toArray(new String[0]));
			
	        DataInputStream processOut = new DataInputStream(deshakeProcess.getErrorStream());
	        
	        String outLine = null;
	        
	        while ((outLine = processOut.readLine()) != null) {
	        	if (Logger.getLogger(getClass()).isDebugEnabled()) {
	                Logger.getLogger(getClass()).debug("ffmpeg output: " + outLine);
	        	}
	        }
			
			int deshakeResult = deshakeProcess.waitFor();
			
			if (deshakeResult == 0) {
				File resultFile = new File(targetFilePath);
				
				if (resultFile.exists()) {
					return true;
				} else {
                    Logger.getLogger(getClass()).error("ffmpeg deshake result file not found: " + targetFilePath);
				}
			} else {
				Logger.getLogger(getClass()).warn("ffmpeg deshaking returned error " + deshakeResult);
			}
		} catch (IOException ioex) {
			Logger.getLogger(getClass()).error("failed to deshake video " + videoFilePath, ioex);
		} catch (InterruptedException iex) {
			Logger.getLogger(getClass()).error("failed to deshake video " + videoFilePath, iex);
		}
		return false;
    }
    
    private void removeTransformFile(String targetVideoPath) {
        File transformFile = new File(getTransformFilePath(targetVideoPath));
        
        if (!transformFile.delete()) {
        	Logger.getLogger(getClass()).error("failed to delete ffmpeg deshake transform file " + transformFile);
        }
    }
    
    private String getTransformFilePath(String targetVideoPath) {
        return targetVideoPath + File.separator + "transform.trf";
    }
    
    private String getEscapedTransformFilePath(String targetVideoPath) {
        String transformFile = getTransformFilePath(targetVideoPath);
        transformFile = transformFile.replace('\\',  '/');
        if (transformFile.charAt(1) == ':') {
        	transformFile = transformFile.substring(0, 1) + "\\:" + transformFile.substring(2);
        }
        return transformFile;
    }
    
    private static String getTargetPath(String videoPath) {
        int sepIdx = videoPath.lastIndexOf(File.separator);

        if (sepIdx < 0) {
            Logger.getLogger(VideoThumbnailCreator.class).error("incorrect video file path: " + videoPath);
            return(null); 
        }

        String basePath = videoPath.substring(0, sepIdx + 1);

        return basePath + DESHAKE_TARGET_DIR;
    }
    
    
}

