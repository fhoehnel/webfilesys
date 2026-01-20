package de.webfilesys.graphics;

import de.webfilesys.SubdirExistCache;
import de.webfilesys.WebFileSys;
import de.webfilesys.WebFileSysConfig;
import de.webfilesys.util.CommonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class VideoSilentAudioGenerator {

    public static final String TARGET_SUBDIR = "_silentAudio";

    private static final Logger LOG = LogManager.getLogger(VideoSilentAudioGenerator.class);

    public static void addSilentAudioToVideo(String videoFilePath) {
        LOG.debug("adding silent audio to video {}", videoFilePath);

        String ffmpegExePath = WebFileSysConfig.getInstance().getFfmpegExePath();
        
        if (!CommonUtils.isEmpty(ffmpegExePath)) {
            String[] partsOfPath = CommonUtils.splitPath(videoFilePath);
            String sourcePath = partsOfPath[0];
            String sourceFileName = partsOfPath[1];
            String targetPath = sourcePath + File.separator + TARGET_SUBDIR;

            File targetDirFile = new File(targetPath);
            if (!targetDirFile.exists()) {
                if (!targetDirFile.mkdir()) {
                    LOG.error("failed to create target folder for silent audio: {}", targetPath);
                } else {
                    SubdirExistCache.getInstance().setExistsSubdir(targetPath, 1);
                }
            }

            String targetFilePath = CommonUtils.getNonConflictingTargetFilePath(targetPath + File.separator + sourceFileName);

            // ffmpeg -f lavfi -i anullsrc=channel_layout=stereo:sample_rate=44100 -i
            // input.mp4 -c:v copy -c:a aac -shortest output.mp4

            ArrayList<String> progNameAndParams = new ArrayList<>();
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

            if (LOG.isDebugEnabled()) {
                StringBuilder buff = new StringBuilder();
                for (String cmdToken : progNameAndParams) {
                    buff.append(cmdToken);
                    buff.append(' ');
                }
                LOG.debug("ffmpeg call with params: {}", buff.toString());
            }

            try {
                Process convertProcess = Runtime.getRuntime().exec(progNameAndParams.toArray(new String[0]));

                DataInputStream grabProcessOut = new DataInputStream(convertProcess.getErrorStream());

                String outLine = null;

                while ((outLine = grabProcessOut.readLine()) != null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("ffmpeg output: {}", outLine);
                    }
                }

                int convertResult = convertProcess.waitFor();

                if (convertResult == 0) {
                    File resultFile = new File(targetFilePath);
                    if (!resultFile.exists()) {
                        LOG.error("result file from ffmpeg add silent audio not found: {}", targetFilePath);
                    }
                } else {
                    LOG.warn("ffmpeg returned error {}", convertResult);
                }
            } catch (IOException | InterruptedException ex) {
                LOG.error("failed to add silent audio to video {}", videoFilePath, ex);
            }
        }
    }
}

