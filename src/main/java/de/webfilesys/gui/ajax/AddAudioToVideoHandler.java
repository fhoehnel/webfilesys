package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Element;

import de.webfilesys.ClipBoard;
import de.webfilesys.WebFileSys;
import de.webfilesys.graphics.VideoAudioMixerThread;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class AddAudioToVideoHandler extends XmlRequestHandlerBase {
    
	public AddAudioToVideoHandler(HttpServletRequest req, HttpServletResponse resp, HttpSession session,
			PrintWriter output, String uid) {
		super(req, resp, session, output, uid);
	}

	protected void process() {
		if (!checkWriteAccess()) {
			return;
		}

        String ffmpegExePath = WebFileSys.getInstance().getFfmpegExePath();
        
        if (CommonUtils.isEmpty(ffmpegExePath)) {
        	LogManager.getLogger(getClass()).warn("ffmpeg not configured");
        	return;
        }		
		
		String videoFilePath = getParameter("videoFilePath");
		
		Element resultElement = doc.createElement("result");
		
		ClipBoard clipBoard = (ClipBoard) session.getAttribute("clipBoard");
		
		if ((clipBoard == null) || clipBoard.isEmpty() || (!clipBoard.isCopyOperation())) {
		    String errorMsg = getResource("errorMissingAudioInClipboard", "missing audio file in clipboard");
			XmlUtil.setChildText(resultElement, "error", errorMsg);
		} else {
			ArrayList<String> audioFiles = new ArrayList<String>();
			for (String clipFile : clipBoard.getAllFiles()) {
				if (isAudioFile(clipFile)) {
					audioFiles.add(clipFile);
				}
			}
			
			if (audioFiles.size() > 1) {
			    Collections.sort(audioFiles);
			}

			if (audioFiles.size() > 0) {
				VideoAudioMixerThread videoAudioMixer = new VideoAudioMixerThread(videoFilePath);
				videoAudioMixer.setAudioFiles(audioFiles);
				videoAudioMixer.start();
				XmlUtil.setChildText(resultElement, "success", Boolean.toString(true));
				
		    	String[] partsOfPath = CommonUtils.splitPath(videoFilePath);
		    	
		    	String targetFolder = "_audio";
		    	
		    	String targetPath = partsOfPath[0] + File.separator + targetFolder;
				
				XmlUtil.setChildText(resultElement, "success", Boolean.toString(true));

				XmlUtil.setChildText(resultElement, "targetFolder", targetFolder);

				XmlUtil.setChildText(resultElement, "targetPath", targetPath);
			} else {
			    String errorMsg = getResource("errorMissingAudioInClipboard", "missing audio file in clipboard");
				XmlUtil.setChildText(resultElement, "error", errorMsg);
			}
			
			clipBoard.clear();
		}
		
		doc.appendChild(resultElement);

		processResponse();
	}
	
	private boolean isAudioFile(String filePath) {
		String fileExt = CommonUtils.getFileExtension(filePath);
		return fileExt.equals(".mp3");
	}
	
}
