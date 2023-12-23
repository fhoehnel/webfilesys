package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Element;

import de.webfilesys.graphics.VideoDeshaker;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class MultiVideoDeshakeHandler extends MultiVideoHandlerBase {
	
	private static Logger LOG = LogManager.getLogger(MultiVideoDeshakeHandler.class);

	public MultiVideoDeshakeHandler(HttpServletRequest req, HttpServletResponse resp, HttpSession session,
			PrintWriter output, String uid) {
		super(req, resp, session, output, uid);
	}

	protected void process() {
		if (!checkWriteAccess()) {
			return;
		}
		
		String currentPath = getCwd();

		ArrayList<String> deshakeQueue = new ArrayList<String>();
		
		List<String> selectedFiles = getSelectedFiles();
		
		for (String videoFileName : selectedFiles) {
			deshakeQueue.add(CommonUtils.joinFilesysPath(currentPath, videoFileName));
		}

		VideoDeshaker videoDeshaker = new VideoDeshaker(deshakeQueue);

		videoDeshaker.start();

    	String targetPath = currentPath + File.separator + VideoDeshaker.DESHAKE_TARGET_DIR;
		
		Element resultElement = doc.createElement("result");

		XmlUtil.setChildText(resultElement, "success", Boolean.toString(true));

		XmlUtil.setChildText(resultElement, "targetFolder", VideoDeshaker.DESHAKE_TARGET_DIR);

		XmlUtil.setChildText(resultElement, "targetPath", targetPath);
		
		doc.appendChild(resultElement);

		processResponse();
	}

}
