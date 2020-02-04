package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;

import de.webfilesys.graphics.VideoDeshaker;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class DeshakeVideoHandler extends XmlRequestHandlerBase {
    
	public DeshakeVideoHandler(HttpServletRequest req, HttpServletResponse resp, HttpSession session,
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
        
		VideoDeshaker videoDeshaker = new VideoDeshaker(videoFilePath);

		videoDeshaker.start();

    	String[] partsOfPath = CommonUtils.splitPath(videoFilePath);
    	
    	String targetFolder = VideoDeshaker.DESHAKE_TARGET_DIR;
    	
    	String targetPath = partsOfPath[0] + File.separator + targetFolder;
		
		Element resultElement = doc.createElement("result");

		XmlUtil.setChildText(resultElement, "success", Boolean.toString(true));

		XmlUtil.setChildText(resultElement, "targetFolder", targetFolder);

		XmlUtil.setChildText(resultElement, "targetPath", targetPath);
		
		doc.appendChild(resultElement);

		processResponse();
	}
}
