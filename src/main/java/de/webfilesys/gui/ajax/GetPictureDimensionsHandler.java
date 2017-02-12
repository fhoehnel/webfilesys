package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import de.webfilesys.FileLink;
import de.webfilesys.MetaInfManager;
import de.webfilesys.graphics.ScaledImage;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class GetPictureDimensionsHandler extends XmlRequestHandlerBase {
	
    private static final Logger LOG = Logger.getLogger(GetPictureDimensionsHandler.class);
	
	public GetPictureDimensionsHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid) {
        super(req, resp, session, output, uid);
	}
	
	protected void process() {
        String fileName = getParameter("fileName");

        if (CommonUtils.isEmpty(fileName)) {
        	LOG.warn("parameter fileName missing");
        	return;
        }
        
        String path = getCwd();
        
        File picFile = new File(path, fileName);

        String isLink = getParameter("link");
        if (!CommonUtils.isEmpty(isLink)) {
        	FileLink fileLink = MetaInfManager.getInstance().getLink(path, fileName);
        	if (fileLink != null) {
        		if (!accessAllowed(fileLink.getDestPath())) {
        			LOG.error("user " + uid + " tried to access a linked file outside his docuemnt root: " + fileLink.getDestPath());
        			return;
        		}
        		picFile = new File(fileLink.getDestPath());
        	} else {
    			LOG.error("link does not exist: " + path + " " + fileName);
    			return;
        	}
        } else {
            picFile = new File(path, fileName);
        }
        
        if ((!picFile.exists()) || (!picFile.isFile()) || (!picFile.canRead())) {
        	LOG.warn("not a readable file: " + path + " " + fileName);
        	return;
        }

        Element resultElement = doc.createElement("result");
        
		try {
			ScaledImage scaledImage = new ScaledImage(picFile.getAbsolutePath(), 100, 100);

	        XmlUtil.setChildText(resultElement, "xpix", Integer.toString(scaledImage.getRealWidth()));
	        XmlUtil.setChildText(resultElement, "ypix", Integer.toString(scaledImage.getRealHeight()));
	        XmlUtil.setChildText(resultElement, "imageType", Integer.toString(scaledImage.getImageType()));
		} catch (IOException ioex) {
			Logger.getLogger(getClass()).error("failed to create scaled image " + picFile.getAbsolutePath(), ioex);
		}
        	
        doc.appendChild(resultElement);
		
		processResponse();
	}
}
