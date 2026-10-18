package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.PrintWriter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import de.webfilesys.graphics.VideoSilentAudioGenerator;
import org.w3c.dom.Element;

import de.webfilesys.graphics.VideoSilentAudioGeneratorThread;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class VideoAddSilentAudioHandler extends XmlRequestHandlerBase {
    
	public VideoAddSilentAudioHandler(HttpServletRequest req, HttpServletResponse resp, HttpSession session,
			PrintWriter output, String uid) {
		super(req, resp, session, output, uid);
	}

	protected void process() {
		if (!checkWriteAccess()) {
			return;
		}

		String videoFileName = getParameter("videoFileName");

		String videoFilePath = CommonUtils.joinFilesysPath(getCwd(), videoFileName);
		
		VideoSilentAudioGeneratorThread addAudioThread = new VideoSilentAudioGeneratorThread(videoFilePath);
		
		addAudioThread.start();

    	String[] partsOfPath = CommonUtils.splitPath(videoFilePath);
    	
    	String targetPath = partsOfPath[0] + File.separator + VideoSilentAudioGenerator.TARGET_SUBDIR;
    	
		
		Element resultElement = doc.createElement("result");

		XmlUtil.setChildText(resultElement, "success", Boolean.toString(true));

		XmlUtil.setChildText(resultElement, "targetFolder", VideoSilentAudioGenerator.TARGET_SUBDIR);

		XmlUtil.setChildText(resultElement, "targetPath", targetPath);
		
		doc.appendChild(resultElement);

		processResponse();
	}
}
