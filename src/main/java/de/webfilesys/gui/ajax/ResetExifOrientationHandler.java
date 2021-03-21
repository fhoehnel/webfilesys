package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;

import de.webfilesys.graphics.ExifUtil;
import de.webfilesys.util.XmlUtil;

/**
 * Sets the Exif orientation value for a JPEG file to 1.
 * @author Frank Hoehnel
 */
public class ResetExifOrientationHandler extends XmlRequestHandlerBase {
	public ResetExifOrientationHandler(
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
		
		String imgPath = getParameter("imgPath");

		if (!checkAccess(imgPath)) {
			return;
		}
        
        boolean success = true;
        
        File imgFile = new File(imgPath);
        
        if (!imgFile.exists() || (!imgFile.isFile() || (!imgFile.canWrite()))) {
            success = false;
        } else {
        	success = ExifUtil.resetExifOrientation(imgFile);
        }
        
        Element resultElement = doc.createElement("result");
        
		XmlUtil.setChildText(resultElement, "success", Boolean.toString(success));
		
		doc.appendChild(resultElement);
		
		this.processResponse();
	}
}
