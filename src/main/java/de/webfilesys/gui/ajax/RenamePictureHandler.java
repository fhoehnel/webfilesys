package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import de.webfilesys.Category;
import de.webfilesys.Comment;
import de.webfilesys.GeoTag;
import de.webfilesys.MetaInfManager;
import de.webfilesys.WebFileSys;
import de.webfilesys.graphics.AutoThumbnailCreator;
import de.webfilesys.graphics.ThumbnailThread;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class RenamePictureHandler extends XmlRequestHandlerBase {
	boolean fileNameKnown = false;

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
			Logger.getLogger(getClass()).error("required parameter newFileName missing");
			return;
		}

		String oldFileName = getParameter("imageFile");

		if (CommonUtils.isEmpty(oldFileName)) {
			Logger.getLogger(getClass()).error("required parameter oldFileName missing");
			return;
		}
		
		String imagePath = getCwd();
        String newImagePath = imagePath;
		
		if (imagePath.endsWith(File.separator)) {
			imagePath = imagePath + oldFileName;
			newImagePath = newImagePath + newFileName;
		} else {
			imagePath = imagePath + File.separator + oldFileName;
			newImagePath = newImagePath + File.separator + newFileName;
		}
		
		File source = new File(imagePath);

		File dest = new File(newImagePath);

		boolean success = false;
		
		if ((newFileName.indexOf("..") < 0) && (source.renameTo(dest))) {
			
			MetaInfManager metaInfMgr = MetaInfManager.getInstance();

			String description = metaInfMgr.getDescription(imagePath);

			if ((description != null) && (description.trim().length() > 0)) {
				metaInfMgr.setDescription(newImagePath, description);
			}

			ArrayList<Category> assignedCategories = metaInfMgr.getListOfCategories(imagePath);

			if (assignedCategories != null) {
				for (int i = 0; i < assignedCategories.size(); i++) {
					Category cat = (Category) assignedCategories.get(i);

					metaInfMgr.addCategory(newImagePath, cat);
				}
			}

			GeoTag geoTag = metaInfMgr.getGeoTag(imagePath);
			if (geoTag != null) {
				metaInfMgr.setGeoTag(newImagePath, geoTag);
			}

			ArrayList<Comment> comments = metaInfMgr.getListOfComments(imagePath);
			if ((comments != null) && (comments.size() > 0)) {
				for (Comment comment : comments) {
					metaInfMgr.addComment(newImagePath, comment);
				}
			}

			if (WebFileSys.getInstance().isReverseFileLinkingEnabled()) {
				metaInfMgr.updateLinksAfterMove(imagePath, newImagePath, uid);
			}

			metaInfMgr.removeMetaInf(imagePath);

			String thumbnailPath = ThumbnailThread.getThumbnailPath(imagePath);

			File thumbnailFile = new File(thumbnailPath);

			if (thumbnailFile.exists()) {
				if (!thumbnailFile.delete()) {
					Logger.getLogger(getClass()).debug("cannot remove thumbnail file " + thumbnailPath);
				}
			}

			if (WebFileSys.getInstance().isAutoCreateThumbs()) {
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
