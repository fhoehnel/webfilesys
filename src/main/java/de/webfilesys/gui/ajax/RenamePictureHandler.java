package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.PrintWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import de.webfilesys.*;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Element;

import de.webfilesys.graphics.AutoThumbnailCreator;
import de.webfilesys.graphics.ThumbnailThread;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class RenamePictureHandler extends XmlRequestHandlerBase {

	public RenamePictureHandler(HttpServletRequest req, HttpServletResponse resp, HttpSession session, PrintWriter output,
			String uid) {
		super(req, resp, session, output, uid);
	}

	protected void process() {
		if (!checkWriteAccess()) {
			return;
		}

		String newFileName = getParameter("newFileName");

		if (CommonUtils.isEmpty(newFileName)) {
			LogManager.getLogger(getClass()).error("required parameter newFileName missing");
			return;
		}

		String oldFileName = getParameter("imageFile");

		if (CommonUtils.isEmpty(oldFileName)) {
			LogManager.getLogger(getClass()).error("required parameter oldFileName missing");
			return;
		}
		
		String path = getCwd();
        String imagePath = CommonUtils.joinFilesysPath(path, oldFileName);
        String newImagePath = CommonUtils.joinFilesysPath(path, newFileName);

        File source = new File(imagePath);
		File dest = new File(newImagePath);

		boolean success = false;
		
		if ((!newFileName.contains("..")) && (source.renameTo(dest))) {
			
			MetaInfManager metaInfMgr = MetaInfManager.getInstance();

            metaInfMgr.moveMetaInf(path, oldFileName, newFileName);

            if (WebFileSysConfig.getInstance().isReverseFileLinkingEnabled()) {
                metaInfMgr.updateLinksAfterMove(newImagePath, uid);
            }

            // metaInfMgr.removeMetaInf(imagePath);

			String thumbnailPath = ThumbnailThread.getThumbnailPath(imagePath);

			File thumbnailFile = new File(thumbnailPath);

			if (thumbnailFile.exists()) {
				if (!thumbnailFile.delete()) {
					LogManager.getLogger(getClass()).debug("cannot remove thumbnail file " + thumbnailPath);
				}
			}

			if (WebFileSysConfig.getInstance().isAutoCreateThumbs()) {
				String ext = CommonUtils.getFileExtension(newImagePath);

				if (ext.equals(".jpg") || ext.equals(".jpeg") || (ext.equals("png"))) {
					AutoThumbnailCreator.getInstance().queuePath(newImagePath, AutoThumbnailCreator.SCOPE_FILE);
				}
			}
			
			success = true;
		}
		
        Element resultElement = doc.createElement("result");
        
        XmlUtil.setChildText(resultElement, "success", Boolean.toString(success));

        XmlUtil.setChildText(resultElement, "filePath", dest.getAbsolutePath());
        
        doc.appendChild(resultElement);
		
		processResponse();
	}
}
