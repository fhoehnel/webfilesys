package de.webfilesys.gui.blog;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import de.webfilesys.MetaInfManager;
import de.webfilesys.gui.ajax.XmlRequestHandlerBase;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class BlogSetTitlePicHandler extends XmlRequestHandlerBase {
	
	public BlogSetTitlePicHandler(
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
        
        if ((!imgFile.exists()) || (!imgFile.isFile())) {
			Logger.getLogger(getClass()).error("img file is not a readable file: " + imgFile.getAbsolutePath());
			return;
        }
        
		MetaInfManager.getInstance().setTitlePic(currentPath, imgName);
		
		boolean success = true;
		
		Element resultElement = doc.createElement("result");
        
        XmlUtil.setChildText(resultElement, "success", Boolean.toString(success));

        doc.appendChild(resultElement);
		
		processResponse();
	}
}
