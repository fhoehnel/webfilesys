package de.webfilesys.gui.xsl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.graphics.ScaledImage;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class SlideshowToVideoParamHandler extends XslRequestHandlerBase {
	
	public static final String SESSION_KEY_SELECTED_SLIDESHOW_VIDEO_FILES = "slideshowVideoSelectedFiles";
	
    private static final Logger LOG = Logger.getLogger(SlideshowToVideoParamHandler.class);

	public static final String LIST_PREFIX = "list-";
	
	private static final int LIST_PREFIX_LENGTH = LIST_PREFIX.length();
	
	private static final int MAX_VIDEO_RESOLUTION = 1920;
    
	public SlideshowToVideoParamHandler(
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

		ArrayList<String> selectedFiles = new ArrayList<String>();
		
        Enumeration allKeys = req.getParameterNames();
		
		while (allKeys.hasMoreElements()) {
			String paramKey = (String) allKeys.nextElement();

            if (paramKey.startsWith(LIST_PREFIX)) {
				selectedFiles.add(paramKey.substring(LIST_PREFIX_LENGTH)); 
            }
		}
		
		if (selectedFiles.size() > 1) {
			Collections.sort(selectedFiles);
		}
		
		boolean picDimensionMissmatch = false;
        int commonImgWidth = 0;
        int commonImgHeight = 0;
		
		try {
	        for (int i = 0; !picDimensionMissmatch && (i < selectedFiles.size()); i++) {
	            String filePath = null;

	            if (currentPath.endsWith(File.separator)) {
	                filePath = currentPath + selectedFiles.get(i);
	            } else {
	                filePath = currentPath + File.separator + selectedFiles.get(i);
	            }
	            
	            ScaledImage scaledImg = new ScaledImage(filePath, MAX_VIDEO_RESOLUTION, MAX_VIDEO_RESOLUTION);
	            
	            if (commonImgWidth == 0) {
	            	commonImgWidth = scaledImg.getRealWidth();
	            	commonImgHeight = scaledImg.getRealHeight();
	            } else {
	            	if (scaledImg.getRealWidth() != commonImgWidth) {
	            		picDimensionMissmatch = true;
	            	}
	            	if (scaledImg.getRealHeight() != commonImgHeight) {
	            		picDimensionMissmatch = true;
	            	}
	            }
	        }
		} catch (IOException ioex) {
		    LOG.error("failed to get picture dimensions of pictures selected for slideshow video", ioex);
		}
		
		Element videoParamsElem = doc.createElement("videoParams");
			
		doc.appendChild(videoParamsElem);

		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/slideshowVideoParams.xsl\"");

		doc.insertBefore(xslRef, videoParamsElem);
		
		XmlUtil.setChildText(videoParamsElem, "selectedPictureCount", Integer.toString(selectedFiles.size()), false);
		
		XmlUtil.setChildText(videoParamsElem, "pictureWidth", Integer.toString(commonImgWidth), false);
		XmlUtil.setChildText(videoParamsElem, "pictureHeight", Integer.toString(commonImgHeight), false);
		
		if (picDimensionMissmatch) {
			XmlUtil.setChildText(videoParamsElem, "picDimensionMissmatch", "true", false);
		} else {
			req.getSession().setAttribute(SESSION_KEY_SELECTED_SLIDESHOW_VIDEO_FILES, selectedFiles);
			
			double aspectRatio = (double) commonImgWidth / (double) commonImgHeight;
			
			Element targetResolutionElem = doc.createElement("targetResolution");
			videoParamsElem.appendChild(targetResolutionElem);
			
			addTargetResolutionOption(targetResolutionElem, commonImgWidth, commonImgHeight, aspectRatio, 1920);
			addTargetResolutionOption(targetResolutionElem, commonImgWidth, commonImgHeight, aspectRatio, 1024);
			addTargetResolutionOption(targetResolutionElem, commonImgWidth, commonImgHeight, aspectRatio, 800);
			addTargetResolutionOption(targetResolutionElem, commonImgWidth, commonImgHeight, aspectRatio, 640);
			addTargetResolutionOption(targetResolutionElem, commonImgWidth, commonImgHeight, aspectRatio, 380);
			addTargetResolutionOption(targetResolutionElem, commonImgWidth, commonImgHeight, aspectRatio, 260);
		}
		
		processResponse("slideshowVideoParams.xsl");
    }
	
	private void addTargetResolutionOption(Element targetResolutionElem, int maxWidth, int maxHeight, double aspectRatio, int resolution) {
		
		int targetWidth;
		int targetHeight;
		
		if (aspectRatio >= 1) {
			if (resolution > maxWidth) {
				return;
			}
			targetWidth = resolution;
			targetHeight = (int) Math.round(resolution / aspectRatio);
		} else {
			if (resolution > maxHeight) {
				return;
			}
			targetHeight = resolution;
			targetWidth = (int) Math.round(resolution * aspectRatio);
		}
		
		
		Element resolutionOptionElem = doc.createElement("option");
		XmlUtil.setChildText(resolutionOptionElem, "width", Integer.toString(targetWidth));
		XmlUtil.setChildText(resolutionOptionElem, "height", Integer.toString(targetHeight));
		targetResolutionElem.appendChild(resolutionOptionElem);
	}
}