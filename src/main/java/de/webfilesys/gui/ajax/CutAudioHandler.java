package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.PrintWriter;
import java.text.DecimalFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Element;

import de.webfilesys.graphics.AudioCutterThread;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class CutAudioHandler extends XmlRequestHandlerBase {
    
	public CutAudioHandler(HttpServletRequest req, HttpServletResponse resp, HttpSession session,
			PrintWriter output, String uid) {
		super(req, resp, session, output, uid);
	}

	protected void process() {
		if (!checkWriteAccess()) {
			return;
		}

		String audioFileName = getParameter("audioFileName");

        String startHourParam = getParameter("startHour");
        String startMinParam = getParameter("startMin");
        String startSecParam = getParameter("startSec");

        String endHourParam = getParameter("endHour");
        String endMinParam = getParameter("endMin");
        String endSecParam = getParameter("endSec");
		
		String audioFilePath = CommonUtils.getFullPath(getCwd(), audioFileName);
		
		AudioCutterThread audioCutter = new AudioCutterThread(audioFilePath);
		
        DecimalFormat numFormat = new DecimalFormat("00");
        
        if ((!CommonUtils.isEmpty(startHourParam)) && 
            (!CommonUtils.isEmpty(startMinParam)) &&    
            (!CommonUtils.isEmpty(startSecParam))) {
            try {
                int startHour = Integer.parseInt(startHourParam);
                int startMin = Integer.parseInt(startMinParam);
                int startSec = Integer.parseInt(startSecParam);

                audioCutter.setStartTime(numFormat.format(startHour) + ":" + numFormat.format(startMin) + ":" + numFormat.format(startSec));
            } catch (Exception ex) {
                LogManager.getLogger(getClass()).warn("invalid audio time range value", ex);
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

                audioCutter.setEndTime(numFormat.format(endHour) + ":" + numFormat.format(endMin) + ":" + numFormat.format(endSec));
            } catch (Exception ex) {
                LogManager.getLogger(getClass()).warn("invalid audio time range value", ex);
                return;
            }
        }
        
		audioCutter.start();

    	String[] partsOfPath = CommonUtils.splitPath(audioFilePath);
    	
    	String targetFolder = "_audioCut";
    	
    	String targetPath = partsOfPath[0] + File.separator + targetFolder;
		
		Element resultElement = doc.createElement("result");

		XmlUtil.setChildText(resultElement, "success", Boolean.toString(true));

		XmlUtil.setChildText(resultElement, "targetFolder", targetFolder);

		XmlUtil.setChildText(resultElement, "targetPath", targetPath);
		
		doc.appendChild(resultElement);

		processResponse();
	}
}
