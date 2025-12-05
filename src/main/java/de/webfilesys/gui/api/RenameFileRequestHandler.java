package de.webfilesys.gui.api;

import de.webfilesys.*;
import de.webfilesys.graphics.AutoThumbnailCreator;
import de.webfilesys.graphics.ThumbnailThread;
import de.webfilesys.gui.user.UserRequestHandler;
import de.webfilesys.util.CommonUtils;
import org.apache.logging.log4j.LogManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

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
        String oldFilePath = CommonUtils.getFullPath(path, oldFileName);
        String newFilePath = CommonUtils.getFullPath(path, newFileName);

        if (!checkAccess(oldFilePath)) {
            try {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            } catch (IOException ex) {
            }
        }

		File source = new File(oldFilePath);
		File dest = new File(newFilePath);

		if (newFileName.contains("..") || !source.renameTo(dest)) {
            try {
                resp.sendError(HttpServletResponse.SC_CONFLICT,
                        getResource("error.renameFailed", "could not be renamed to"));
            } catch (IOException ex) {
            }
			return;
		}

		MetaInfManager metaInfMgr = MetaInfManager.getInstance();

        metaInfMgr.moveMetaInf(path, oldFileName, newFileName);

		if (WebFileSysConfig.getInstance().isReverseFileLinkingEnabled()) {
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

		if (WebFileSysConfig.getInstance().isAutoCreateThumbs()) {
			String ext = CommonUtils.getFileExtension(newFilePath);
			if (ext.equals(".jpg") || ext.equals(".jpeg") || (ext.equals("png"))) {
				AutoThumbnailCreator.getInstance().queuePath(newFilePath, AutoThumbnailCreator.SCOPE_FILE);
			}
		}
	}
}
