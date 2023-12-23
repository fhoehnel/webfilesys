package de.webfilesys.gui.ajax;

import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Element;

import de.webfilesys.graphics.VideoConcatAnyThread;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.SessionKey;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class AnyVideoConcatHandler extends MultiVideoHandlerBase {
	
	private static Logger LOG = LogManager.getLogger(AnyVideoConcatHandler.class);
	
	private static final String TARGET_FOLDER = "_joined";
	
	public AnyVideoConcatHandler(HttpServletRequest req, HttpServletResponse resp, HttpSession session,
			PrintWriter output, String uid) {
		super(req, resp, session, output, uid);
	}

	protected void process() {
		if (!checkWriteAccess()) {
			return;
		}
		
		String currentPath = getCwd();

		List<String> selectedFiles = (List<String>) session.getAttribute(SessionKey.SELECTED_FILES);

		if (selectedFiles == null) {
			LOG.error("selected video files not found in session");
			return;
		}
		
		session.removeAttribute(SessionKey.SELECTED_FILES);
		
		int errorCode = 0;
		
		String newWidthParam = getParameter("newWidth");
		String newHeightParam = getParameter("newHeight");
		
		int newWidth;
		int newHeight;
		try {
			newWidth = Integer.parseInt(newWidthParam);
			newHeight = Integer.parseInt(newHeightParam);
		} catch (Exception ex) {
			LOG.error("invalid value for new video dimension", ex);
			return;
		}

		String newContainer = getParameter("newContainer");
		String newFps = getParameter("newFps");
		
        String targetPath = CommonUtils.joinFilesysPath(currentPath, TARGET_FOLDER);

        if (errorCode == 0) {
            VideoConcatAnyThread concatThread = new VideoConcatAnyThread(currentPath, selectedFiles, newWidth, newHeight, targetPath);
            concatThread.setNewContainer(newContainer);
            concatThread.setNewFrameRate(newFps);
            concatThread.start();
        }
		
		Element resultElement = doc.createElement("result");

		if (errorCode != 0) {
			XmlUtil.setChildText(resultElement, "errorCode", Integer.toString(errorCode));
		} else {
			XmlUtil.setChildText(resultElement, "success", Boolean.toString(true));
			XmlUtil.setChildText(resultElement, "targetFolder", TARGET_FOLDER);
			XmlUtil.setChildText(resultElement, "targetPath", targetPath);
		}
		
		doc.appendChild(resultElement);

		processResponse();
	}

}
