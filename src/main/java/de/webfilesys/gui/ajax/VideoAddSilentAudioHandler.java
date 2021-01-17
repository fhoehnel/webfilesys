package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;

import de.webfilesys.graphics.VideoAddSilentAudioThread;
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
		
		VideoAddSilentAudioThread addAudioThread = new VideoAddSilentAudioThread(videoFilePath);
		
		addAudioThread.start();

    	String[] partsOfPath = CommonUtils.splitPath(videoFilePath);
    	
    	String targetFolder = "_silentAudio";
    	
    	String targetPath = partsOfPath[0] + File.separator + targetFolder;
		
		Element resultElement = doc.createElement("result");

		XmlUtil.setChildText(resultElement, "success", Boolean.toString(true));

		XmlUtil.setChildText(resultElement, "targetFolder", targetFolder);

		XmlUtil.setChildText(resultElement, "targetPath", targetPath);
		
		doc.appendChild(resultElement);

		processResponse();
	}
}
