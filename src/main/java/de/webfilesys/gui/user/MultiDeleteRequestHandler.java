package de.webfilesys.gui.user;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.MetaInfManager;
import de.webfilesys.graphics.ThumbnailThread;
import de.webfilesys.gui.xsl.XslFileListHandler;
import de.webfilesys.gui.xsl.mobile.MobileFolderFileListHandler;

/**
 * @author Frank Hoehnel
 */
public class MultiDeleteRequestHandler extends MultiFileRequestHandler {
	public MultiDeleteRequestHandler(HttpServletRequest req, HttpServletResponse resp, HttpSession session,
			PrintWriter output, String uid) {
		super(req, resp, session, output, uid);
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
						Logger.getLogger(getClass()).debug("cannot remove thumbnail file " + thumbnailPath);
					}
				}
			}
		}

		if (errorMsg.length() > 0) {
			setParameter("errorMsg", errorMsg.toString());
		}
		
		String mobile = (String) session.getAttribute("mobile");

		if (mobile != null) {
			(new MobileFolderFileListHandler(req, resp, session, output, uid)).handleRequest();
		} else {
		    (new XslFileListHandler(req, resp, session, output, uid)).handleRequest();
		}
	}

}
