package de.webfilesys.gui.user;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


import de.webfilesys.Category;
import de.webfilesys.Comment;
import de.webfilesys.GeoTag;
import de.webfilesys.MetaInfManager;
import de.webfilesys.WebFileSys;
import de.webfilesys.graphics.AutoThumbnailCreator;
import de.webfilesys.graphics.ThumbnailThread;
import de.webfilesys.gui.xsl.XslFileListHandler;
import de.webfilesys.gui.xsl.mobile.MobileFolderFileListHandler;
import de.webfilesys.util.CommonUtils;

/**
 * @author Frank Hoehnel
 */
public class RenameFileRequestHandler extends UserRequestHandler {
	protected HttpServletRequest req = null;

	protected HttpServletResponse resp = null;

	public RenameFileRequestHandler(HttpServletRequest req, HttpServletResponse resp, HttpSession session,
			PrintWriter output, String uid) {
		super(req, resp, session, output, uid);

		this.req = req;

		this.resp = resp;
	}

	protected void process() {
		if (!checkWriteAccess()) {
			return;
		}

		String newFileName = getParameter("newFileName");

		String oldFileName = getParameter("fileName");

		String path = getCwd();

		String oldFilePath = null;

		String newFilePath = null;

		if (path.endsWith(File.separator)) {
			oldFilePath = path + oldFileName;

			newFilePath = path + newFileName;
		} else {
			oldFilePath = path + File.separator + oldFileName;

			newFilePath = path + File.separator + newFileName;
		}

		if (!checkAccess(oldFilePath)) {
			return;
		}

		String mobile = getParameter("mobile");

		File source = new File(oldFilePath);

		File dest = new File(newFilePath);

		if ((newFileName.indexOf("..") >= 0) || (!source.renameTo(dest))) {
			String errorMsg = oldFileName + "<br/>" + getResource("error.renameFailed", "could not be renamed to")
					+ "<br/>" + newFileName;

			if (errorMsg.length() > 0) {
				setParameter("errorMsg", errorMsg);
			}

			if (mobile != null) {
				(new MobileFolderFileListHandler(req, resp, session, output, uid)).handleRequest();
			} else {
				(new XslFileListHandler(req, resp, session, output, uid)).handleRequest();
			}

			return;
		}

		MetaInfManager metaInfMgr = MetaInfManager.getInstance();

		String description = metaInfMgr.getDescription(oldFilePath);

		if ((description != null) && (description.trim().length() > 0)) {
			metaInfMgr.setDescription(newFilePath, description);
		}

		ArrayList<Category> assignedCategories = metaInfMgr.getListOfCategories(oldFilePath);

		if (assignedCategories != null) {
			for (int i = 0; i < assignedCategories.size(); i++) {
				Category cat = (Category) assignedCategories.get(i);

				metaInfMgr.addCategory(newFilePath, cat);
			}
		}

		GeoTag geoTag = metaInfMgr.getGeoTag(oldFilePath);
		if (geoTag != null) {
			metaInfMgr.setGeoTag(newFilePath, geoTag);
		}

		ArrayList<Comment> comments = metaInfMgr.getListOfComments(oldFilePath);
		if ((comments != null) && (comments.size() > 0)) {
			for (Comment comment : comments) {
				metaInfMgr.addComment(newFilePath, comment);
			}
		}

		if (WebFileSys.getInstance().isReverseFileLinkingEnabled()) {
			metaInfMgr.updateLinksAfterMove(oldFilePath, newFilePath, uid);
		}

		metaInfMgr.removeMetaInf(oldFilePath);

		String thumbnailPath = ThumbnailThread.getThumbnailPath(oldFilePath);

		File thumbnailFile = new File(thumbnailPath);

		if (thumbnailFile.exists()) {
			if (!thumbnailFile.delete()) {
				LogManager.getLogger(getClass()).debug("cannot remove thumbnail file " + thumbnailPath);
			}
		}

		setParameter("actpath", getCwd());

		setParameter("mask", "*");

		if (mobile == null) {
			(new XslFileListHandler(req, resp, session, output, uid)).handleRequest();
		} else {
			(new MobileFolderFileListHandler(req, resp, session, output, uid)).handleRequest();
		}

		if (WebFileSys.getInstance().isAutoCreateThumbs()) {
			String ext = CommonUtils.getFileExtension(newFilePath);

			if (ext.equals(".jpg") || ext.equals(".jpeg") || (ext.equals("png"))) {
				AutoThumbnailCreator.getInstance().queuePath(newFilePath, AutoThumbnailCreator.SCOPE_FILE);
			}
		}
	}
}
