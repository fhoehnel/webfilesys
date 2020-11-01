package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;

import de.webfilesys.graphics.VideoFadeAudioThread;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class VideoFadeAudioHandler extends XmlRequestHandlerBase {
    
	public VideoFadeAudioHandler(HttpServletRequest req, HttpServletResponse resp, HttpSession session,
			PrintWriter output, String uid) {
		super(req, resp, session, output, uid);
	}

	protected void process() {
		if (!checkWriteAccess()) {
			return;
		}

		String videoFileName = getParameter("videoFileName");

		String videoFilePath = getCwd();
		if (videoFilePath.endsWith(File.separator)) {
			videoFilePath = videoFilePath + videoFileName;
		} else {
			videoFilePath = videoFilePath + File.separator + videoFileName;
		}

		String fadeInDuration = getParameter("fadeInDuration");

		int fadeInSeconds = -1;
		
		if (!CommonUtils.isEmpty(fadeInDuration)) {
			try {
				fadeInSeconds = Integer.parseInt(fadeInDuration);
			} catch (Exception ex) {
			}
		}
		
		String fadeOutDuration = getParameter("fadeOutDuration");
		
		int fadeOutSeconds = -1;
		
		if (!CommonUtils.isEmpty(fadeOutDuration)) {
			try {
				fadeOutSeconds = Integer.parseInt(fadeOutDuration);
			} catch (Exception ex) {
			}
		}
		
		VideoFadeAudioThread videoFadeAudioThread = new VideoFadeAudioThread(videoFilePath);
		
		videoFadeAudioThread.setFadeInDuration(fadeInSeconds);
		videoFadeAudioThread.setFadeOutDuration(fadeOutSeconds);
		
		videoFadeAudioThread.start();

    	String[] partsOfPath = CommonUtils.splitPath(videoFilePath);
    	
    	String targetFolder = "_fadeAudio";
    	
    	String targetPath = partsOfPath[0] + File.separator + targetFolder;
		
		Element resultElement = doc.createElement("result");

		XmlUtil.setChildText(resultElement, "success", Boolean.toString(true));

		XmlUtil.setChildText(resultElement, "targetFolder", targetFolder);

		XmlUtil.setChildText(resultElement, "targetPath", targetPath);
		
		doc.appendChild(resultElement);

		processResponse();
	}
}
