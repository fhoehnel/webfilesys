package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.MessageFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
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

		String newContainerFormat = getParameter("newContainer");
		
        String startHourParam = getParameter("startHour");
        String startMinParam = getParameter("startMin");
        String startSecParam = getParameter("startSec");

        String endHourParam = getParameter("endHour");
        String endMinParam = getParameter("endMin");
        String endSecParam = getParameter("endSec");
		
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
		videoConverter.setNewContainerFormat(newContainerFormat);

        DecimalFormat numFormat = new DecimalFormat("00");
        
        if ((!CommonUtils.isEmpty(startHourParam)) && 
            (!CommonUtils.isEmpty(startMinParam)) &&    
            (!CommonUtils.isEmpty(startSecParam))) {
            try {
                int startHour = Integer.parseInt(startHourParam);
                int startMin = Integer.parseInt(startMinParam);
                int startSec = Integer.parseInt(startSecParam);

                videoConverter.setStartTime(numFormat.format(startHour) + ":" + numFormat.format(startMin) + ":" + numFormat.format(startSec));
            } catch (Exception ex) {
                Logger.getLogger(getClass()).warn("invalid video time range value", ex);
                return;
            }
        }

        if ((!CommonUtils.isEmpty(endHourParam)) && 
            (!CommonUtils.isEmpty(endMinParam)) &&    
            (!CommonUtils.isEmpty(endSecParam))) {
            try {
                int endHour = Integer.parseInt(endHourParam);
                int endMin = Integer.parseInt(endMinParam);
                int endSec = Integer.parseInt(endSecParam);

                videoConverter.setEndTime(numFormat.format(endHour) + ":" + numFormat.format(endMin) + ":" + numFormat.format(endSec));
            } catch (Exception ex) {
                Logger.getLogger(getClass()).warn("invalid video time range value", ex);
                return;
            }
        }
        
		videoConverter.start();

    	String[] partsOfPath = CommonUtils.splitPath(videoFilePath);
    	
    	String targetFolder = CommonUtils.isEmpty(newSize) ? "_converted" : newSize;
    	
    	String targetPath = partsOfPath[0] + File.separator + targetFolder;
		
		Element resultElement = doc.createElement("result");

		XmlUtil.setChildText(resultElement, "success", Boolean.toString(true));

		XmlUtil.setChildText(resultElement, "targetFolder", targetFolder);

		XmlUtil.setChildText(resultElement, "targetPath", targetPath);
		
		doc.appendChild(resultElement);

		processResponse();
	}
}
