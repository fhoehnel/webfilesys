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

public class VideoSilentAudioGeneratorThread extends Thread {

	public static final String TARGET_SUBDIR = "_silentAudio";
	
	private ArrayList<String> processQueue = null;

    public VideoSilentAudioGeneratorThread(String videoFilePath) {
    	processQueue = new ArrayList<String>();
    	processQueue.add(videoFilePath);
    }
    
    public VideoSilentAudioGeneratorThread(ArrayList<String> videoFilePathList) {
    	processQueue = videoFilePathList;
    }

    public void run() {
        if (LogManager.getLogger(getClass()).isDebugEnabled()) {
            LogManager.getLogger(getClass()).debug("starting silent audio thread for " + processQueue.size() + " video files");
        }
        
        Thread.currentThread().setPriority(1);
 
        String ffmpegExePath = WebFileSys.getInstance().getFfmpegExePath();
        
        if (!CommonUtils.isEmpty(ffmpegExePath)) {
        	for (String videoFilePath : processQueue) {
				String[] partsOfPath = CommonUtils.splitPath(videoFilePath);
				String sourcePath = partsOfPath[0];
				String sourceFileName = partsOfPath[1];
				String targetPath = sourcePath + File.separator + TARGET_SUBDIR;

				File targetDirFile = new File(targetPath);
				if (!targetDirFile.exists()) {
					if (!targetDirFile.mkdir()) {
						LogManager.getLogger(getClass())
								.error("failed to create target folder for silent audio: " + targetPath);
					} else {
						SubdirExistCache.getInstance().setExistsSubdir(targetPath, Integer.valueOf(1));
					}
				}

	            String targetFilePath = CommonUtils.getNonConflictingTargetFilePath(targetPath + File.separator + sourceFileName);

				// ffmpeg -f lavfi -i anullsrc=channel_layout=stereo:sample_rate=44100 -i
				// input.mp4 -c:v copy -c:a aac -shortest output.mp4

				ArrayList<String> progNameAndParams = new ArrayList<String>();
				progNameAndParams.add(ffmpegExePath);

				progNameAndParams.add("-f");
				progNameAndParams.add("lavfi");

				progNameAndParams.add("-i");
				progNameAndParams.add("anullsrc=channel_layout=stereo:sample_rate=44100");

				progNameAndParams.add("-i");
				progNameAndParams.add(videoFilePath);

				progNameAndParams.add("-c:v");
				progNameAndParams.add("copy");

				progNameAndParams.add("-c:a");
				progNameAndParams.add("aac");

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
							LogManager.getLogger(getClass())
									.error("result file from ffmpeg add silent audio not found: " + targetFilePath);
						}
					} else {
						LogManager.getLogger(getClass()).warn("ffmpeg returned error " + convertResult);
					}
				} catch (IOException ioex) {
					LogManager.getLogger(getClass()).error("failed to add silent audio to video " + videoFilePath, ioex);
				} catch (InterruptedException iex) {
					LogManager.getLogger(getClass()).error("failed to add silent audio to video " + videoFilePath, iex);
				}
        	}
        }
    }

    
}

