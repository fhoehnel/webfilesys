package de.webfilesys.gui.xsl;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.graphics.VideoInfo;
import de.webfilesys.graphics.VideoInfoExtractor;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.SessionKey;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class AnyVideoConcatParamHandler extends XslRequestHandlerBase {
	
	private static Logger LOG = LogManager.getLogger(AnyVideoConcatParamHandler.class);
	
	private static final int ERROR_CODE_NO_AUDIO = 5;
	
	public AnyVideoConcatParamHandler(HttpServletRequest req, HttpServletResponse resp, HttpSession session,
			PrintWriter output, String uid) {
		super(req, resp, session, output, uid);
	}

	protected void process() {
		if (!checkWriteAccess()) {
			return;
		}
		
		String currentPath = getCwd();

		List<String> selectedFiles = getSelectedFiles();
		
		session.setAttribute(SessionKey.SELECTED_FILES, selectedFiles);

		ArrayList<String> videosWithoutAudio = new ArrayList<>();
		
		int maxVideoWidth = 0;
		int maxVideoHeight = 0;
		
        for (String selectedFile : selectedFiles) {
            String filePath = CommonUtils.joinFilesysPath(currentPath, selectedFile);
            
            VideoInfoExtractor videoInfoExtractor = new VideoInfoExtractor();
            
            VideoInfo videoInfo = videoInfoExtractor.getVideoInfo(filePath);
            
            if (videoInfo.getAudioCodec() == null) {
            	videosWithoutAudio.add(selectedFile);
            } else {
                int videoWidth = videoInfo.getWidth();
                if (videoWidth > maxVideoWidth) {
                	maxVideoWidth = videoWidth;
                }

                int videoHeight = videoInfo.getHeight();
                if (videoHeight > maxVideoHeight) {
                   	maxVideoHeight = videoHeight;
                }
                
                if (LOG.isDebugEnabled()) {
                    LOG.debug("video file to concatenate: " + filePath + " width: " + videoWidth + " height: " + videoHeight);
                }
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("max video width: " + maxVideoWidth + " max video height: " + maxVideoHeight);
        }

		Element paramsElem = doc.createElement("concatParams");
		
		doc.appendChild(paramsElem);

		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/concatVideoParams.xsl\"");

		doc.insertBefore(xslRef, paramsElem);
		
		if (videosWithoutAudio.size() > 0) {
			Element missingAudioListElem = doc.createElement("missingAudio");
			paramsElem.appendChild(missingAudioListElem);
			for (String videoWithoutAudio : videosWithoutAudio) {
				Element fileElem = doc.createElement("file");
				missingAudioListElem.appendChild(fileElem);
				XmlUtil.setElementText(fileElem, videoWithoutAudio, false);
			}
		} else {
			XmlUtil.setChildText(paramsElem, "maxVideoWidth", Integer.toString(maxVideoWidth));
			XmlUtil.setChildText(paramsElem, "maxVideoHeight", Integer.toString(maxVideoHeight));
			
	        int maxDimension = maxVideoWidth;
	        if (maxVideoHeight > maxVideoWidth) {
	        	maxDimension = maxVideoHeight;
	        }
			
			Element targetResolutionElem = doc.createElement("targetWidth");
			paramsElem.appendChild(targetResolutionElem);
			
			addTargetResolutionOption(targetResolutionElem, maxDimension, 3840); // UHD / 4K
			addTargetResolutionOption(targetResolutionElem, maxDimension, 1920); // Full HD
			addTargetResolutionOption(targetResolutionElem, maxDimension, 1280); // HD ready
			addTargetResolutionOption(targetResolutionElem, maxDimension, 1024); // SD
			addTargetResolutionOption(targetResolutionElem, maxDimension, 854);
			addTargetResolutionOption(targetResolutionElem, maxDimension, 640);  // VGA
			addTargetResolutionOption(targetResolutionElem, maxDimension, 426);
			addTargetResolutionOption(targetResolutionElem, maxDimension, 380);
			addTargetResolutionOption(targetResolutionElem, maxDimension, 260);

			Element targetHeightElem = doc.createElement("targetHeight");
			paramsElem.appendChild(targetHeightElem);
			
			addTargetResolutionOption(targetHeightElem, maxDimension, 2160); // UHD / 4K
			addTargetResolutionOption(targetHeightElem, maxDimension, 1080); // Full HD
			addTargetResolutionOption(targetHeightElem, maxDimension, 720);  // HD ready
			addTargetResolutionOption(targetHeightElem, maxDimension, 576);  // SD
			addTargetResolutionOption(targetHeightElem, maxDimension, 480);  // VGA
			addTargetResolutionOption(targetHeightElem, maxDimension, 360);
			addTargetResolutionOption(targetHeightElem, maxDimension, 240);
			addTargetResolutionOption(targetHeightElem, maxDimension, 200);
		}
		
		processResponse("concatVideoParams.xsl");
	}
	
	private void addTargetResolutionOption(Element targetResolutionElem, int maxDimension, int resolution) {
		if (maxDimension >= resolution) {
			Element resolutionOptionElem = doc.createElement("option");
			XmlUtil.setElementText(resolutionOptionElem, Integer.toString(resolution));
			targetResolutionElem.appendChild(resolutionOptionElem);
		}
	}

}
