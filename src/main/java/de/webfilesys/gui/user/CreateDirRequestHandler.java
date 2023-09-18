package de.webfilesys.gui.user;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


import de.webfilesys.Constants;
import de.webfilesys.DirTreeStatus;
import de.webfilesys.SubdirExistCache;
import de.webfilesys.gui.xsl.XslUnixDirTreeHandler;
import de.webfilesys.gui.xsl.XslWinDirTreeHandler;
import de.webfilesys.gui.xsl.mobile.MobileFolderFileListHandler;

/**
 * @author Frank Hoehnel
 */
public class CreateDirRequestHandler extends UserRequestHandler {
	boolean clientIsLocal = false;

	public CreateDirRequestHandler(HttpServletRequest req, HttpServletResponse resp, HttpSession session,
			PrintWriter output, String uid, boolean clientIsLocal) {
		super(req, resp, session, output, uid);

		this.clientIsLocal = clientIsLocal;
	}

	protected void process() {
		if (!checkWriteAccess()) {
			return;
		}

		String newDir = getParameter("NewDirName");

		if (newDir == null) {
			LogManager.getLogger(getClass()).error("required parameter newDirName missing");

			return;
		}

		String actPath = getParameter("actpath");

		String mobile = (String) session.getAttribute("mobile");

		if (mobile != null) {
			if (actPath.charAt(0) == '\\') {
				actPath = actPath.substring(1);
			}
		}

		String parentPath = actPath;

		String newPath = null;

		if (actPath.endsWith(File.separator)) {
			newPath = actPath + newDir;
		} else {
			newPath = actPath + File.separator + newDir;
		}

		if (!checkAccess(newPath)) {
			return;
		}

		String errorMsg = null;
		
		File newFile = new File(newPath);

		if (!newFile.mkdir()) {
			errorMsg = getResource("alert.createDirError", "Directory could not be created.") + "<br/>"
					 + getResource("label.path", "path") + ": " + insertDoubleBackslash(actPath) + "<br/>"
					 + getResource("label.directory", "folder") + ": " + newDir;
			
			setParameter("errorMsg", errorMsg);
		}

		if (mobile != null) {
			session.setAttribute(Constants.SESSION_KEY_CWD, actPath);

			(new MobileFolderFileListHandler(req, resp, session, output, uid)).handleRequest();
			return;
		}

		if (errorMsg == null) {
			SubdirExistCache.getInstance().setExistsSubdir(actPath + File.separator + newDir, new Integer(0));

			SubdirExistCache.getInstance().setExistsSubdir(actPath, new Integer(1));
			
			DirTreeStatus dirTreeStatus = (DirTreeStatus) session.getAttribute(Constants.SESSION_KEY_DIR_TREE_STATUS);

			if (dirTreeStatus == null) {
				dirTreeStatus = new DirTreeStatus();
				session.setAttribute(Constants.SESSION_KEY_DIR_TREE_STATUS, dirTreeStatus);
			}

			if (!dirTreeStatus.dirExpanded(parentPath)) {
				dirTreeStatus.expandDir(parentPath);
			}

			dirTreeStatus.expandDir(newPath);

			setParameter("actPath", newPath);
			setParameter("fastPath", "true");
			setParameter("expand", newPath);
			
		} else {
			setParameter("actPath", actPath);
			setParameter("fastPath", "true");
		}

		if (File.separatorChar == '/') {
			(new XslUnixDirTreeHandler(req, resp, session, output, uid, clientIsLocal)).handleRequest();
		} else {
			(new XslWinDirTreeHandler(req, resp, session, output, uid, clientIsLocal)).handleRequest();
		}
	}
}
