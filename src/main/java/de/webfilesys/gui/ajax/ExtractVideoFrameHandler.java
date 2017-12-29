package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import de.webfilesys.graphics.VideoFrameExtractor;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class ExtractVideoFrameHandler extends XmlRequestHandlerBase {
    
	public ExtractVideoFrameHandler(HttpServletRequest req, HttpServletResponse resp, HttpSession session,
			PrintWriter output, String uid) {
		super(req, resp, session, output, uid);
	}

	protected void process() {
		if (!checkWriteAccess()) {
			return;
		}

		String videoFileName = getParameter("videoFileName");

		String frameSize = getParameter("frameSize");

        String startHourParam = getParameter("startHour");
        String startMinParam = getParameter("startMin");
        String startSecParam = getParameter("startSec");

        String videoWidthParam = getParameter("videoWidth");
        String videoHeightParam = getParameter("videoHeight");

        int videoWidth = 0;
        int videoHeight = 0;
        try {
        	videoWidth = Integer.parseInt(videoWidthParam);
        	videoHeight = Integer.parseInt(videoHeightParam);
        } catch (NumberFormatException numEx) {
        }
        
		String videoFilePath = getCwd();
		if (videoFilePath.endsWith(File.separator)) {
			videoFilePath = videoFilePath + videoFileName;
		} else {
			videoFilePath = videoFilePath + File.separator + videoFileName;
		}
		
		String extractTime = "00:00:00.00";

        DecimalFormat numFormat = new DecimalFormat("00");
        
        if ((!CommonUtils.isEmpty(startHourParam)) && 
            (!CommonUtils.isEmpty(startMinParam)) &&    
            (!CommonUtils.isEmpty(startSecParam))) {
            try {
                int startHour = Integer.parseInt(startHourParam);
                int startMin = Integer.parseInt(startMinParam);
                int startSec = Integer.parseInt(startSecParam);

                extractTime = numFormat.format(startHour) + ":" + numFormat.format(startMin) + ":" + numFormat.format(startSec) + ".00";
            } catch (Exception ex) {
                Logger.getLogger(getClass()).warn("invalid video extract time value", ex);
                return;
            }
        }
        
		VideoFrameExtractor frameExtractor = new VideoFrameExtractor(videoFilePath, extractTime, videoWidth, videoHeight, frameSize);

		frameExtractor.start();

    	String[] partsOfPath = CommonUtils.splitPath(videoFilePath);
    	
    	String targetFolder = VideoFrameExtractor.FRAME_TARGET_DIR;
    	
    	String targetPath = partsOfPath[0] + File.separator + targetFolder;
		
		Element resultElement = doc.createElement("result");

		XmlUtil.setChildText(resultElement, "success", Boolean.toString(true));

		XmlUtil.setChildText(resultElement, "targetFolder", targetFolder);

		XmlUtil.setChildText(resultElement, "targetPath", targetPath);
		
		doc.appendChild(resultElement);

		processResponse();
	}
}
