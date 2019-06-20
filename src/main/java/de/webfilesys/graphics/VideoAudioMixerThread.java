package de.webfilesys.graphics;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.log4j.Logger;

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
        if (Logger.getLogger(getClass()).isDebugEnabled()) {
            Logger.getLogger(getClass()).debug("starting video/audio mixer thread for video file " + videoFilePath);
        }
        
        Thread.currentThread().setPriority(1);
        
    	String[] partsOfPath = CommonUtils.splitPath(videoFilePath);
    	
    	String sourcePath = partsOfPath[0];
    	String sourceFileName = partsOfPath[1];

        String targetPath = sourcePath + File.separator + "_audio";
    	
        File targetDirFile = new File(targetPath);
        if (!targetDirFile.exists()) {
            if (!targetDirFile.mkdir()) {
                Logger.getLogger(getClass()).error("failed to create target folder for video conversion: " + targetPath);
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

        	StringBuilder progNameAndParams = new StringBuilder(ffmpegExePath);
            progNameAndParams.append(" -y");

        	progNameAndParams.append(" -i ");
        	progNameAndParams.append(videoFilePath);
        	
        	progNameAndParams.append(" -i ");
        	progNameAndParams.append(audioFilePath);
        	
        	progNameAndParams.append(" -map 0:v");
        	progNameAndParams.append(" -map " + 1 + ":a");
        	progNameAndParams.append(" -strict -1");
        	progNameAndParams.append(" -c copy");
        	
         	progNameAndParams.append(" -shortest ");
        	
        	progNameAndParams.append(targetFilePath);

            if (Logger.getLogger(getClass()).isDebugEnabled()) {
                Logger.getLogger(getClass()).debug("ffmpeg call with params: " + progNameAndParams);
            }
        	
			try {
				Process convertProcess = Runtime.getRuntime().exec(progNameAndParams.toString());
				
		        DataInputStream convertProcessOut = new DataInputStream(convertProcess.getErrorStream());
		        
		        String outLine = null;
		        
		        while ((outLine = convertProcessOut.readLine()) != null) {
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
        
        if (audioFiles.size() > 1) {
        	// TODO: delete combined audio file
        }
    }
   
    private String concatenateAudioFiles(ArrayList<String> audioFiles, String targetPath, String ffmpegExePath) {
    	
        String combinedAudioFilePath = targetPath + File.separator + "combinedAudio.mp3";
    	
    	// C:/Programs/ffmpeg/bin/ffmpeg.exe -i C:\temp\mp3-examples\SoundHelixSong-4b.mp3 -i C:\temp\mp3-examples\Vivaldi_Sonata_eminor.mp3 -i C:\temp\mp3-examples\Tchaikovsky_Nocturne.mp3 -filter_complex "[0:a:0][1:a:0][2:a:0]concat=n=3:v=0:a=1[outa]" -map "[outa]" C:\temp\mp3-examples\combined.mp3

        StringBuilder progNameAndParams = new StringBuilder(ffmpegExePath);
    	
        progNameAndParams.append(" -y");
        
    	for (String audioFile: audioFiles) {
        	progNameAndParams.append(" -i ");
        	progNameAndParams.append(audioFile);
    	}

    	progNameAndParams.append(" -filter_complex ");
    	
    	progNameAndParams.append("\"");
        int i = 0;
    	for (String audioFile: audioFiles) {
    		progNameAndParams.append("[" + i + ":a:0]");
    		i++;
    	}
    	progNameAndParams.append("concat=n=");
    	progNameAndParams.append(audioFiles.size());
    	progNameAndParams.append(":v=0:a=1[outa]");
    	progNameAndParams.append("\"");
    	
    	progNameAndParams.append(" -map \"[outa]\" ");
    	
    	progNameAndParams.append(combinedAudioFilePath);
    	
        if (Logger.getLogger(getClass()).isDebugEnabled()) {
            Logger.getLogger(getClass()).debug("ffmpeg call with params: " + progNameAndParams);
        }
    	
		try {
			Process convertProcess = Runtime.getRuntime().exec(progNameAndParams.toString());
			
	        DataInputStream convertProcessOut = new DataInputStream(convertProcess.getErrorStream());
	        
	        String outLine = null;
	        
	        while ((outLine = convertProcessOut.readLine()) != null) {
	        	if (Logger.getLogger(getClass()).isDebugEnabled()) {
	                Logger.getLogger(getClass()).debug("ffmpeg output: " + outLine);
	        	}
	        }
			
			int convertResult = convertProcess.waitFor();
			
			if (convertResult == 0) {
				File resultFile = new File(combinedAudioFilePath);
				
				if (!resultFile.exists()) {
                    Logger.getLogger(getClass()).error("result file from ffmpeg audio concatenation not found: " + combinedAudioFilePath);
				} else {
					return combinedAudioFilePath;
				}
			} else {
				Logger.getLogger(getClass()).warn("ffmpeg returned error " + convertResult);
			}
		} catch (IOException ioex) {
			Logger.getLogger(getClass()).error("failed to concatenate audio files", ioex);
		} catch (InterruptedException iex) {
			Logger.getLogger(getClass()).error("failed to concatenate audio files", iex);
		}
		
		return null;
    }
}

