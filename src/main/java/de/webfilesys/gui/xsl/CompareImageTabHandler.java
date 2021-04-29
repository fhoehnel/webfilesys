package de.webfilesys.gui.xsl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

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

		List<String> selectedFiles = getSelectedFiles();
		
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
		}
        
        processResponse(XSL_STYLESHEET_NAME);
    }

}