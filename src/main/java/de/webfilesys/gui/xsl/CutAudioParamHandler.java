package de.webfilesys.gui.xsl;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.WebFileSys;
import de.webfilesys.graphics.AudioInfo;
import de.webfilesys.graphics.AudioInfoExtractor;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class CutAudioParamHandler extends XslRequestHandlerBase {
    private static final Logger LOG = LogManager.getLogger(CutAudioParamHandler.class);
	
	public CutAudioParamHandler(
			HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid) {
        super(req, resp, session, output, uid);
	}
	  
	protected void process() {
		if (!checkWriteAccess()) {
			return;
		}

		String currentPath = getCwd();

		String audioFilePath = getParameter("filePath");
		
		File audioFile = new File(audioFilePath);
		
		String audioFileName = audioFile.getName();
		
		Element cutParamsElem = doc.createElement("cutParams");
			
		doc.appendChild(cutParamsElem);

		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/cutAudioParams.xsl\"");

		doc.insertBefore(xslRef, cutParamsElem);
		
		XmlUtil.setChildText(cutParamsElem, "audioFileName", audioFileName, false);
		XmlUtil.setChildText(cutParamsElem, "shortAudioFileName", CommonUtils.shortName(audioFileName, 40), false);
		XmlUtil.setChildText(cutParamsElem, "audioFilePath", audioFilePath, false);
		
        String ffprobeExePath = WebFileSys.getInstance().getFfprobeExePath();
        
        if (!CommonUtils.isEmpty(ffprobeExePath)) {
	        AudioInfoExtractor audioInfoExtractor = new AudioInfoExtractor();
            AudioInfo audioInfo = audioInfoExtractor.getAudioInfo(audioFilePath);

            if (audioInfo.getFfprobeResult() == 0) {
				Element audioInfoElem = doc.createElement("audioInfo");
				cutParamsElem.appendChild(audioInfoElem);
				
                if (audioInfo.getDuration().length() > 0) {
                    XmlUtil.setChildText(audioInfoElem, "duration", audioInfo.getDuration());
                    XmlUtil.setChildText(audioInfoElem, "durationSeconds", Integer.toString(audioInfo.getDurationSeconds()));
                }
			}
        }  
		
		processResponse("cutAudioParams.xsl");
    }
	
}