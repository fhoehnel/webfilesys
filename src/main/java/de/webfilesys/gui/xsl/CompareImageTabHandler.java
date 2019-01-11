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

import de.webfilesys.Constants;
import de.webfilesys.graphics.ScaledImage;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.UTF8URLEncoder;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class CompareImageTabHandler extends XslRequestHandlerBase {
	private static final Logger LOG = Logger.getLogger(CompareImageTabHandler.class);
    
	private static final String XSL_STYLESHEET_NAME = "compareImgTab.xsl";
	
	private static final int THUMBNAIL_SIZE = 160;
	
	public CompareImageTabHandler(
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
		
		if (selectedFiles.size() < 2) {
		    LOG.error("this image comparision requires at least two selected pictures");
		    return;
		}

		Collections.sort(selectedFiles);

		String imgBasePath = currentPath;

		if (!currentPath.endsWith(File.separator)) {
			imgBasePath = currentPath + File.separator;
		}
		
		Element compareImageElem = doc.createElement("compareImage");
			
		doc.appendChild(compareImageElem);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/" + XSL_STYLESHEET_NAME + "\"");

		doc.insertBefore(xslRef, compareImageElem);

		XmlUtil.setChildText(compareImageElem, "css", userMgr.getCSS(uid), false);

	    XmlUtil.setChildText(compareImageElem, "language", language, false);

	    XmlUtil.setChildText(compareImageElem, "path", imgBasePath, false);

	    XmlUtil.setChildText(compareImageElem, "pathForScript", escapeForJavascript(imgBasePath), false);

	    XmlUtil.setChildText(compareImageElem, "relativePath", this.getHeadlinePath(imgBasePath), false);
	    
	    Element fileListElem = doc.createElement("fileList");
	    compareImageElem.appendChild(fileListElem);
	    
	    for (String selectedFile : selectedFiles) {
			
			Element fileElement = doc.createElement("file");

			fileListElem.appendChild(fileElement);

			fileElement.setAttribute("name", selectedFile);

			fileElement.setAttribute("nameForScript", escapeForJavascript(selectedFile));

			fileElement.setAttribute("nameForId", selectedFile.replace(' ',  '_'));
			
			String imgPath = imgBasePath + selectedFile;

			String displayName = CommonUtils.shortName(selectedFile, 22);

			XmlUtil.setChildText(fileElement, "displayName", displayName);
			
			String imgSrcPath = "/webfilesys/servlet?command=picThumb&imgFile=" + UTF8URLEncoder.encode(selectedFile);

			XmlUtil.setChildText(fileElement, "imgPath", imgSrcPath);
			
			try {
				ScaledImage scaledImage = new ScaledImage(imgPath, THUMBNAIL_SIZE, THUMBNAIL_SIZE);
				XmlUtil.setChildText(fileElement, "thumbWidth", Integer.toString(scaledImage.getScaledWidth()));
				XmlUtil.setChildText(fileElement, "thumbHeight", Integer.toString(scaledImage.getScaledHeight()));
			} catch (IOException io1) {
				Logger.getLogger(getClass()).error("failed to determine image size for image comparision", io1);
				return;
			}
		}
        
        processResponse(XSL_STYLESHEET_NAME, false);
    }

	private ArrayList<String> getSelectedFiles() {
		ArrayList<String> selectedFiles = new ArrayList<String>();

		int prefixLength = Constants.CHECKBOX_LIST_PREFIX.length();
		
		Enumeration allKeys = req.getParameterNames();

		while (allKeys.hasMoreElements()) {
			String parmKey = (String) allKeys.nextElement();

            if (parmKey.startsWith(Constants.CHECKBOX_LIST_PREFIX)) {
				String fileName = parmKey.substring(prefixLength);
				selectedFiles.add(fileName); 
            } 
		}
		return selectedFiles;
	}
	
}