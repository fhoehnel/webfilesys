package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;

import de.webfilesys.graphics.TextOnVideoThread;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class TextOnVideoHandler extends XmlRequestHandlerBase {
    
	public TextOnVideoHandler(HttpServletRequest req, HttpServletResponse resp, HttpSession session,
			PrintWriter output, String uid) {
		super(req, resp, session, output, uid);
	}

	protected void process() {
		if (!checkWriteAccess()) {
			return;
		}

		String videoFileName = getParameter("videoFileName");

		String text = getParameter("videoText");

		String textSize = getParameter("videoTextSize");
		if (CommonUtils.isEmpty(textSize)) {
			textSize = "80";
		}

		String textColor = getParameter("videoTextColorName");
		if (CommonUtils.isEmpty(textColor)) {
			textColor = getParameter("videoTextColor");
		}
		if (CommonUtils.isEmpty(textColor)) {
			textColor = "white";
		}
        
		int textPosition = TextOnVideoThread.TEXT_POSITION_CENTER;
		String textPositionParam = getParameter("videoTextPosition");
		if (textPositionParam.equals("top")) {
			textPosition = TextOnVideoThread.TEXT_POSITION_TOP;
		} else if (textPositionParam.equals("bottom")) {
			textPosition = TextOnVideoThread.TEXT_POSITION_BOTTOM;
		}
		
		String videoFilePath = getCwd();
		if (videoFilePath.endsWith(File.separator)) {
			videoFilePath = videoFilePath + videoFileName;
		} else {
			videoFilePath = videoFilePath + File.separator + videoFileName;
		}
		
		TextOnVideoThread textOnVideoThread = new TextOnVideoThread(videoFilePath);
		
		textOnVideoThread.setText(text);
		textOnVideoThread.setTextSize(textSize);
		textOnVideoThread.setTextColor(textColor);
		textOnVideoThread.setTextPosition(textPosition);
		
		textOnVideoThread.start();

    	String[] partsOfPath = CommonUtils.splitPath(videoFilePath);
    	
    	String targetFolder = "_text";
    	
    	String targetPath = partsOfPath[0] + File.separator + targetFolder;
		
		Element resultElement = doc.createElement("result");

		XmlUtil.setChildText(resultElement, "success", Boolean.toString(true));

		XmlUtil.setChildText(resultElement, "targetFolder", targetFolder);

		XmlUtil.setChildText(resultElement, "targetPath", targetPath);
		
		doc.appendChild(resultElement);

		processResponse();
	}
}
