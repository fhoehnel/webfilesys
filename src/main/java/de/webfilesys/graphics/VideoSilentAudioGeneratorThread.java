package de.webfilesys.graphics;

import java.util.ArrayList;

import de.webfilesys.WebFileSysConfig;
import org.apache.logging.log4j.LogManager;
import de.webfilesys.WebFileSys;
import de.webfilesys.util.CommonUtils;

public class VideoSilentAudioGeneratorThread extends Thread {

	private ArrayList<String> processQueue;

    public VideoSilentAudioGeneratorThread(String videoFilePath) {
    	processQueue = new ArrayList<String>();
    	processQueue.add(videoFilePath);
    }
    
    public VideoSilentAudioGeneratorThread(ArrayList<String> videoFilePathList) {
    	processQueue = videoFilePathList;
    }

    public void run() {
        String ffmpegExePath = WebFileSysConfig.getInstance().getFfmpegExePath();
        
        if (!CommonUtils.isEmpty(ffmpegExePath)) {
            if (LogManager.getLogger(getClass()).isDebugEnabled()) {
                LogManager.getLogger(getClass()).debug("starting silent audio thread for {} video files", processQueue.size());
            }

            Thread.currentThread().setPriority(1);

            for (String videoFilePath : processQueue) {
                VideoSilentAudioGenerator.addSilentAudioToVideo(videoFilePath);
        	}
        }
    }

}

