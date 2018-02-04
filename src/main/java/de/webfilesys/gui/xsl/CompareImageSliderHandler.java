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
import de.webfilesys.gui.user.MultiImageRequestHandler;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.UTF8URLEncoder;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class CompareImageSliderHandler extends XslRequestHandlerBase {
	private static final Logger LOG = Logger.getLogger(CompareImageSliderHandler.class);
    
	public CompareImageSliderHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid) {
        super(req, resp, session, output, uid);
	}
	
	protected void process() {
		
		String currentPath = getCwd();

		ArrayList<String> selectedFiles = getSelectedFiles();
		
		if (selectedFiles.size() != 2) {
		    LOG.error("this image comparision requires two selected pictures");
		    return;
		}

		Collections.sort(selectedFiles);

		String imgBasePath = currentPath;

		if (!currentPath.endsWith(File.separator)) {
			imgBasePath = currentPath + File.separator;
		}
		
		String img1Path = imgBasePath + selectedFiles.get(0);
		String img2Path = imgBasePath + selectedFiles.get(1);
		
		int displayWidth = 770;

		String screenWidthParam = getParameter("screenWidth");
		if (!CommonUtils.isEmpty(screenWidthParam)) {
		    try {
		        session.setAttribute("screenWidth", new Integer(Integer.parseInt(screenWidthParam)));
		    } catch (Exception numEx) {
		    }
		}

        String screenHeightParam = getParameter("screenHeight");
        if (!CommonUtils.isEmpty(screenHeightParam)) {
            try {
                session.setAttribute("screenHeight", new Integer(Integer.parseInt(screenHeightParam)));
            } catch (Exception numEx) {
            }
        }
		
		Integer screenWidth = (Integer) session.getAttribute("screenWidth");
		
		if (screenWidth != null) {
			displayWidth = screenWidth.intValue() - 28;
		}
		
		int displayHeight = 520;

		Integer screenHeight = (Integer) session.getAttribute("screenHeight");
		
		if (screenHeight != null) {
			displayHeight = screenHeight.intValue() - 120;
		}
		
		ScaledImage scaledImage1 = null;
		ScaledImage scaledImage2 = null;

		try {
			scaledImage1 = new ScaledImage(img1Path, displayWidth, displayHeight);
			scaledImage2 = new ScaledImage(img2Path, displayWidth, displayHeight);
		} catch (IOException io1) {
			Logger.getLogger(getClass()).error("failed to determine image size for image comparision", io1);
			return;
		}
		
		Element compareImageElem = doc.createElement("compareImage");
			
		doc.appendChild(compareImageElem);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/compareImage.xsl\"");

		doc.insertBefore(xslRef, compareImageElem);

		XmlUtil.setChildText(compareImageElem, "css", userMgr.getCSS(uid), false);

	    XmlUtil.setChildText(compareImageElem, "language", language, false);
		
        Element img1Elem = doc.createElement("image1");

        compareImageElem.appendChild(img1Elem);
        
		String img1Src = "/webfilesys/servlet?command=getFile&filePath=" + UTF8URLEncoder.encode(img1Path);
        
        XmlUtil.setChildText(img1Elem, "path", img1Src);
        XmlUtil.setChildText(img1Elem, "name", selectedFiles.get(0));
        XmlUtil.setChildText(img1Elem, "displayWidth", Integer.toString(scaledImage1.getScaledWidth()));
        XmlUtil.setChildText(img1Elem, "displayHeight", Integer.toString(scaledImage1.getScaledHeight()));
        
        Element img2Elem = doc.createElement("image2");

        compareImageElem.appendChild(img2Elem);
        
		String img2Src = "/webfilesys/servlet?command=getFile&filePath=" + UTF8URLEncoder.encode(img2Path);

		XmlUtil.setChildText(img2Elem, "path", img2Src);
        XmlUtil.setChildText(img2Elem, "name", selectedFiles.get(1));
        XmlUtil.setChildText(img2Elem, "displayWidth", Integer.toString(scaledImage2.getScaledWidth()));
        XmlUtil.setChildText(img2Elem, "displayHeight", Integer.toString(scaledImage2.getScaledHeight()));

		int maxWidth; 
		int maxHeight; 
		
		if (scaledImage1.getRealHeight() > scaledImage2.getRealWidth()) {
			maxWidth = scaledImage1.getScaledWidth();
			maxHeight = scaledImage1.getScaledHeight();
		} else {
			maxWidth = scaledImage2.getScaledWidth();
			maxHeight = scaledImage1.getScaledHeight();
		}
		
	    XmlUtil.setChildText(compareImageElem, "maxWidth", Integer.toString(maxWidth), false);
	    XmlUtil.setChildText(compareImageElem, "maxHeight", Integer.toString(maxHeight), false);
        
        processResponse("compareImage.xsl", false);
    }

	private ArrayList<String> getSelectedFiles() {
		ArrayList<String> selectedFiles = new ArrayList<String>();

		int prefixLength = MultiImageRequestHandler.LIST_PREFIX.length();
		
		Enumeration allKeys = req.getParameterNames();

		while (allKeys.hasMoreElements()) {
			String parmKey = (String) allKeys.nextElement();

            if (parmKey.startsWith(MultiImageRequestHandler.LIST_PREFIX)) {
				String fileName = parmKey.substring(prefixLength);
				selectedFiles.add(fileName); 
            } 
		}
		return selectedFiles;
	}
	
}