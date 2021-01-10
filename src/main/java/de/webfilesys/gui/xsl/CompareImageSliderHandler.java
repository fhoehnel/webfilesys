package de.webfilesys.gui.xsl;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.Constants;
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

		List<String> selectedFiles = getSelectedFiles();
		
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
		
		Element compareImageElem = doc.createElement("compareImage");
			
		doc.appendChild(compareImageElem);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/compareImage.xsl\"");

		doc.insertBefore(xslRef, compareImageElem);

        Element img1Elem = doc.createElement("image1");

        compareImageElem.appendChild(img1Elem);
        
		String img1Src = "/webfilesys/servlet?command=getFile&filePath=" + UTF8URLEncoder.encode(img1Path);
        
        XmlUtil.setChildText(img1Elem, "path", img1Src);
        XmlUtil.setChildText(img1Elem, "name", selectedFiles.get(0));
        
        Element img2Elem = doc.createElement("image2");

        compareImageElem.appendChild(img2Elem);
        
		String img2Src = "/webfilesys/servlet?command=getFile&filePath=" + UTF8URLEncoder.encode(img2Path);

		XmlUtil.setChildText(img2Elem, "path", img2Src);
        XmlUtil.setChildText(img2Elem, "name", selectedFiles.get(1));
        
        processResponse("compareImage.xsl");
    }

}