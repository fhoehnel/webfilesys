package de.webfilesys.gui.user;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


import de.webfilesys.MetaInfManager;
import de.webfilesys.graphics.ThumbnailThread;
import de.webfilesys.gui.xsl.XslThumbnailHandler;

/**
 * @author Frank Hoehnel
 */
public class MultiImageDeleteHandler extends MultiImageRequestHandler {
	boolean clientIsLocal = false;

	public MultiImageDeleteHandler(HttpServletRequest req, HttpServletResponse resp, HttpSession session,
			PrintWriter output, String uid, boolean clientIsLocal) {
		super(req, resp, session, output, uid);

		this.clientIsLocal = clientIsLocal;
	}

	protected void process() {
		if (!checkWriteAccess()) {
			return;
		}

		StringBuffer errorMsg = new StringBuffer();

		MetaInfManager metaInfMgr = MetaInfManager.getInstance();

		for (String selectedFile : selectedFiles) {
			String filePath = null;

			if (actPath.endsWith(File.separator)) {
				filePath = actPath + selectedFile;
			} else {
				filePath = actPath + File.separator + selectedFile;
			}

			File delFile = new File(filePath);

			if ((!delFile.canWrite()) || (!delFile.delete())) {
				if (errorMsg.length() > 0) {
					errorMsg.append("<br/>");
				}
				errorMsg.append(getResource("alert.delete.failed", "cannot delete file ") + "<br/>" + selectedFile);
			} else {
				metaInfMgr.removeMetaInf(actPath, selectedFile);

				String thumbnailPath = ThumbnailThread.getThumbnailPath(filePath);

				File thumbnailFile = new File(thumbnailPath);

				if (thumbnailFile.exists()) {
					if (!thumbnailFile.delete()) {
						LogManager.getLogger(getClass()).debug("cannot remove thumbnail file " + thumbnailPath);
					}
				}
			}
		}

		if (errorMsg.length() > 0) {
			setParameter("errorMsg", errorMsg.toString());
		}

		(new XslThumbnailHandler(req, resp, session, output, uid, clientIsLocal)).handleRequest();
	}
}
