package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import de.webfilesys.graphics.VideoSilentAudioGeneratorThread;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class MultiVideoAddSilentAudioHandler extends MultiVideoHandlerBase {
	
	private static Logger LOG = Logger.getLogger(MultiVideoAddSilentAudioHandler.class);

	public MultiVideoAddSilentAudioHandler(HttpServletRequest req, HttpServletResponse resp, HttpSession session,
			PrintWriter output, String uid) {
		super(req, resp, session, output, uid);
	}

	protected void process() {
		if (!checkWriteAccess()) {
			return;
		}
		
		String currentPath = getCwd();

		ArrayList<String> workerQueue = new ArrayList<String>();
		
		List<String> selectedFiles = getSelectedFiles();
		
		for (String videoFileName : selectedFiles) {
			workerQueue.add(CommonUtils.joinFilesysPath(currentPath, videoFileName));
		}

		VideoSilentAudioGeneratorThread silentAudioGenerator = new VideoSilentAudioGeneratorThread(workerQueue);

		silentAudioGenerator.start();

    	String targetPath = currentPath + File.separator + VideoSilentAudioGeneratorThread.TARGET_SUBDIR;
		
		Element resultElement = doc.createElement("result");

		XmlUtil.setChildText(resultElement, "success", Boolean.toString(true));

		XmlUtil.setChildText(resultElement, "targetFolder", VideoSilentAudioGeneratorThread.TARGET_SUBDIR);

		XmlUtil.setChildText(resultElement, "targetPath", targetPath);
		
		doc.appendChild(resultElement);

		processResponse();
	}

}
