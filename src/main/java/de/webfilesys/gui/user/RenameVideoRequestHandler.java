package de.webfilesys.gui.user;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import de.webfilesys.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


import de.webfilesys.graphics.VideoThumbnailCreator;
import de.webfilesys.gui.xsl.XslVideoListHandler;

/**
 * @author Frank Hoehnel
 */
public class RenameVideoRequestHandler extends UserRequestHandler {
	protected HttpServletRequest req = null;

	protected HttpServletResponse resp = null;

	private boolean requestIsLocal;

	public RenameVideoRequestHandler(HttpServletRequest req, HttpServletResponse resp, HttpSession session,
			PrintWriter output, String uid, boolean requestIsLocal) {
		super(req, resp, session, output, uid);

		this.req = req;
		this.resp = resp;
		this.requestIsLocal = requestIsLocal;
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

		File source = new File(oldFilePath);

		File dest = new File(newFilePath);

		if ((newFileName.indexOf("..") >= 0) || (!source.renameTo(dest))) {
			output.println("<html>");
			output.println("<head>");
			output.println("<script language=\"javascript\">");

			String errorMsg = insertDoubleBackslash(oldFilePath) + "\\n"
					+ getResource("error.renameFailed", "could not be renamed to") + "\\n"
					+ insertDoubleBackslash(newFilePath);

			output.println("alert('" + errorMsg + "');");

			output.println("window.location.href='/webfilesys/servlet?command=listVideos';");

			output.println("</script>");
			output.println("</head>");
			output.println("</html>");
			output.flush();
			return;
		}

		MetaInfManager metaInfMgr = MetaInfManager.getInstance();

        metaInfMgr.moveMetaInf(path, oldFileName, newFileName);

        if (WebFileSysConfig.getInstance().isReverseFileLinkingEnabled()) {
            metaInfMgr.updateLinksAfterMove(newFilePath, uid);
        }

		String thumbnailPath = VideoThumbnailCreator.getThumbnailPath(oldFilePath);

		File thumbnailFile = new File(thumbnailPath);

		if (thumbnailFile.exists()) {
			if (!thumbnailFile.delete()) {
				LogManager.getLogger(getClass()).debug("failed to remove video thumbnail file " + thumbnailPath);
			}
		}

		(new XslVideoListHandler(req, resp, session, output, uid)).handleRequest();
	}
}
