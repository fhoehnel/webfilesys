package de.webfilesys.gui.api;

import de.webfilesys.gui.user.UserRequestHandler;
import de.webfilesys.util.CommonUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Frank Hoehnel
 */
public class CloneFileRequestHandler extends UserRequestHandler {
	protected HttpServletRequest req = null;
	protected HttpServletResponse resp = null;

	public CloneFileRequestHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid) {
        super(req, resp, session, output, uid);
        this.req = req;
        this.resp = resp;
	}

	protected void process() {
		if (!checkWriteAccess()) {
            try {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            } catch (IOException ex) {
            }
			return;
		}

		String newFileName = getParameter("newFileName");
		String sourceFileName = getParameter("sourceFileName");

		String path = getCwd();
		String oldFilePath = CommonUtils.joinFilesysPath(path, sourceFileName);
		String newFilePath = CommonUtils.joinFilesysPath(path, newFileName);

		if (!checkAccess(oldFilePath)) {
            try {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            } catch (IOException ex) {
            }
		}
		
		File destFile = new File(newFilePath);
		if (destFile.exists()) {
            try {
                resp.sendError(HttpServletResponse.SC_CONFLICT,
                               getResource("alert.cloneTargetExists", "Failed to clone file, target file already exists"));
            } catch (IOException ex) {
            }
		} else {
		    if (!copyFile(oldFilePath, newFilePath)) {
                try {
                    resp.sendError(HttpServletResponse.SC_CONFLICT,
                            getResource("alert.cloneFailed", "Failed to clone file."));
                } catch (IOException ex) {
                }
		    }
		}
	}
}
