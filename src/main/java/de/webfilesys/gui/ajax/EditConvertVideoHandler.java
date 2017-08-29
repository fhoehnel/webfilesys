package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;

import de.webfilesys.graphics.VideoConverterThread;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class EditConvertVideoHandler extends XmlRequestHandlerBase {
	public EditConvertVideoHandler(HttpServletRequest req, HttpServletResponse resp, HttpSession session,
			PrintWriter output, String uid) {
		super(req, resp, session, output, uid);
	}

	protected void process() {
		if (!checkWriteAccess()) {
			return;
		}

		String videoFileName = getParameter("videoFileName");

		String newSize = getParameter("newSize");

		String newFps = getParameter("newFps");

		String newCodec = getParameter("newCodec");

		String videoFilePath = getCwd();
		if (videoFilePath.endsWith(File.separator)) {
			videoFilePath = videoFilePath + videoFileName;
		} else {
			videoFilePath = videoFilePath + File.separator + videoFileName;
		}
		
		VideoConverterThread videoConverter = new VideoConverterThread(videoFilePath);
		
		videoConverter.setNewSize(newSize);
		videoConverter.setNewCodec(newCodec);
		videoConverter.setNewFps(newFps);
		
		videoConverter.start();

    	String[] partsOfPath = CommonUtils.splitPath(videoFilePath);
    	
    	String targetPath = partsOfPath[0] + File.separator + newSize;
		
		Element resultElement = doc.createElement("result");

		XmlUtil.setChildText(resultElement, "success", Boolean.toString(true));

		XmlUtil.setChildText(resultElement, "targetFolder", newSize);

		XmlUtil.setChildText(resultElement, "targetPath", targetPath);
		
		doc.appendChild(resultElement);

		processResponse();
	}
}
