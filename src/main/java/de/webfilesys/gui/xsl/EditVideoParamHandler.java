package de.webfilesys.gui.xsl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.WebFileSys;
import de.webfilesys.graphics.ScaledImage;
import de.webfilesys.graphics.VideoInfo;
import de.webfilesys.graphics.VideoInfoExtractor;
import de.webfilesys.graphics.VideoThumbnailCreator;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.UTF8URLEncoder;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class EditVideoParamHandler extends XslRequestHandlerBase {
    private static final Logger LOG = LogManager.getLogger(EditVideoParamHandler.class);
	
	public EditVideoParamHandler(
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

		String videoFileName = getParameter("videoFile");
		
		String videoFilePath = currentPath + File.separator + videoFileName;
		if (currentPath.endsWith(File.separator)) {
			videoFilePath = currentPath + videoFileName;
		}
		
		Element editParamsElem = doc.createElement("editParams");
			
		doc.appendChild(editParamsElem);

		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/editVideoParams.xsl\"");

		doc.insertBefore(xslRef, editParamsElem);
		
		XmlUtil.setChildText(editParamsElem, "videoFileName", videoFileName, false);
		XmlUtil.setChildText(editParamsElem, "shortVideoFileName", CommonUtils.shortName(videoFileName, 40), false);
		XmlUtil.setChildText(editParamsElem, "videoFilePath", videoFilePath, false);
		
        String ffprobeExePath = WebFileSys.getInstance().getFfprobeExePath();
        
        if (!CommonUtils.isEmpty(ffprobeExePath)) {
	        VideoInfoExtractor videoInfoExtractor = new VideoInfoExtractor();
            VideoInfo videoInfo = videoInfoExtractor.getVideoInfo(videoFilePath);

            if (videoInfo.getFfprobeResult() == 0) {
				Element videoInfoElem = doc.createElement("videoInfo");
				editParamsElem.appendChild(videoInfoElem);
				
				XmlUtil.setChildText(videoInfoElem, "xpix", Integer.toString(videoInfo.getWidth()));
    	        XmlUtil.setChildText(videoInfoElem, "ypix", Integer.toString(videoInfo.getHeight()));
                XmlUtil.setChildText(videoInfoElem, "codec", videoInfo.getCodec());
                if (videoInfo.getDuration().length() > 0) {
                    XmlUtil.setChildText(videoInfoElem, "duration", videoInfo.getDuration());
                    XmlUtil.setChildText(videoInfoElem, "durationSeconds", Integer.toString(videoInfo.getDurationSeconds()));
                }
                XmlUtil.setChildText(videoInfoElem, "fps", Integer.toString(videoInfo.getFrameRate()));
                
                try {
                    int xResolution = videoInfo.getWidth();
                    int yResolution = videoInfo.getHeight();
                    
                    int maxDimension = xResolution;
                    if (yResolution > xResolution) {
                    	maxDimension = yResolution;
                    }
                    
    				Element targetResolutionElem = doc.createElement("targetResolution");
    				editParamsElem.appendChild(targetResolutionElem);
    				
    				addTargetResolutionOption(targetResolutionElem, maxDimension, 1920);
    				addTargetResolutionOption(targetResolutionElem, maxDimension, 1280);
    				addTargetResolutionOption(targetResolutionElem, maxDimension, 1024);
    				addTargetResolutionOption(targetResolutionElem, maxDimension, 800);
    				addTargetResolutionOption(targetResolutionElem, maxDimension, 640);
    				addTargetResolutionOption(targetResolutionElem, maxDimension, 380);
    				addTargetResolutionOption(targetResolutionElem, maxDimension, 260);

    				Element targetHeightElem = doc.createElement("targetHeight");
    				editParamsElem.appendChild(targetHeightElem);
    				
    				addTargetResolutionOption(targetHeightElem, maxDimension, 1080);
    				addTargetResolutionOption(targetHeightElem, maxDimension, 720);
    				addTargetResolutionOption(targetHeightElem, maxDimension, 600);
    				addTargetResolutionOption(targetHeightElem, maxDimension, 480);
                } catch (Exception ex) {
                	LogManager.getLogger(getClass()).warn("invalid video resolution: " + videoInfo.getWidth() + " x " + videoInfo.getHeight());
                }
			}
			
			XmlUtil.setChildText(editParamsElem, "videoFileName", videoFileName, false);
			
			XmlUtil.setChildText(editParamsElem, "thumbnailSource", "/webfilesys/servlet?command=videoThumb&videoFile=" + UTF8URLEncoder.encode(videoFileName), false);                    
			
			try {
				String videoThumbnailPath = VideoThumbnailCreator.getThumbnailPath(videoFilePath);
				
				ScaledImage scaledImage = new ScaledImage(videoThumbnailPath, 100, 100);

    			XmlUtil.setChildText(editParamsElem, "thumbnailWidth", Integer.toString(scaledImage.getRealWidth()), false);                    
    			XmlUtil.setChildText(editParamsElem, "thumbnailHeight", Integer.toString(scaledImage.getRealHeight()), false);                    
				
			} catch (IOException ioEx) {
				LogManager.getLogger(getClass()).error(ioEx);
			}
        }        
		
		processResponse("editVideoParams.xsl");
    }
	
	private void addTargetResolutionOption(Element targetResolutionElem, int maxDimension, int resolution) {
		if (maxDimension >= resolution) {
			Element resolutionOptionElem = doc.createElement("option");
			XmlUtil.setElementText(resolutionOptionElem, Integer.toString(resolution));
			targetResolutionElem.appendChild(resolutionOptionElem);
		}
	}
}