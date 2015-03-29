package de.webfilesys.gui.blog;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import de.webfilesys.MetaInfManager;
import de.webfilesys.graphics.ImageTransform;
import de.webfilesys.gui.ajax.XmlRequestHandlerBase;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class BlogRotateImgHandler extends XmlRequestHandlerBase {
	
	private static final String DIRECTION_LEFT = "left";
	private static final String DIRECTION_RIGHT = "right";
	
	public BlogRotateImgHandler(
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
		
		String currentPath = userMgr.getDocumentRoot(uid).replace('/',  File.separatorChar);

        String imgName = getParameter("imgName");
        if (CommonUtils.isEmpty(imgName)) {
			Logger.getLogger(getClass()).error("missing parameter imgName");
			return;
        }

        File imgFile = new File(currentPath, imgName);
        
        if ((!imgFile.exists()) || (!imgFile.isFile()) || (!imgFile.canWrite())) {
			Logger.getLogger(getClass()).error("img file is not a writable file: " + imgFile.getAbsolutePath());
        }
        
        String direction = getParameter("direction");
        if (CommonUtils.isEmpty(direction)) {
			Logger.getLogger(getClass()).error("missing parameter direction");
			return;
        }
		
        if ((!direction.equals(DIRECTION_LEFT)) && (!direction.equals(DIRECTION_RIGHT))) {
			Logger.getLogger(getClass()).error("invalid parameter value for direction: " + direction);
			return;
        }
        
        String degrees = "90";
        if (direction.equals(DIRECTION_LEFT)) {
        	degrees = "270";
        }
        
		ImageTransform imgTrans = new ImageTransform(imgFile.getAbsolutePath(), "rotate", degrees);

		String resultImageName = imgTrans.execute(false);

		MetaInfManager.getInstance().moveMetaInf(currentPath, imgName, resultImageName);
		
		boolean success = true;
		
		if (imgFile.exists()) {
			success = imgFile.delete();
		}
		
		Element resultElement = doc.createElement("result");
        
        XmlUtil.setChildText(resultElement, "success", Boolean.toString(success));

        // XmlUtil.setChildText(resultElement, "resultFileName", resultImageName);
        
        doc.appendChild(resultElement);
		
		processResponse();
	}
}
