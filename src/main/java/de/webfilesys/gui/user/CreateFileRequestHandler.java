package de.webfilesys.gui.user;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.gui.xsl.XslUnixDirTreeHandler;
import de.webfilesys.gui.xsl.XslWinDirTreeHandler;
import de.webfilesys.gui.xsl.mobile.MobileFolderFileListHandler;
import de.webfilesys.util.CommonUtils;

/**
 * @author Frank Hoehnel
 */
public class CreateFileRequestHandler extends UserRequestHandler {
	boolean clientIsLocal = false;

	public CreateFileRequestHandler(HttpServletRequest req, HttpServletResponse resp, HttpSession session,
			PrintWriter output, String uid, boolean clientIsLocal) {
		super(req, resp, session, output, uid);

		this.clientIsLocal = clientIsLocal;
	}

	protected void process() {
		if (!checkWriteAccess()) {
			return;
		}

		String newFileName = getParameter("NewFileName");

		if (CommonUtils.isEmpty(newFileName)) {
			Logger.getLogger(getClass()).error("required parameter newFileName missing");
			return;
		}

		String actPath = getParameter("actpath");

		if (!checkAccess(actPath)) {
			return;
		}

		String newFilePath = null;

		if (actPath.endsWith(File.separator)) {
			newFilePath = actPath + newFileName;
		} else {
			newFilePath = actPath + File.separator + newFileName;
		}

		String errorMsg = null;

		File fileToCreate = new File(newFilePath);

		if (fileToCreate.exists()) {
			errorMsg = getResource("alert.mkfileDuplicate", "A file with this name already exists") + ":<br/>" + newFileName;
		} else {
			try {
				fileToCreate.createNewFile();
			} catch (Exception ioex) {
				errorMsg = getResource("alert.mkfileFail", "The file could not be created") + ":<br/>" + newFileName;
			}
		}

		setParameter("actPath", actPath);
		setParameter("fastPath", "true");

		if (errorMsg != null) {
			setParameter("errorMsg", errorMsg);
		}

		String mobile = (String) session.getAttribute("mobile");

		if (mobile != null) {
			(new MobileFolderFileListHandler(req, resp, session, output, uid)).handleRequest();
		} else {
			if (File.separatorChar == '/') {
				(new XslUnixDirTreeHandler(req, resp, session, output, uid, clientIsLocal)).handleRequest();
			} else {
				(new XslWinDirTreeHandler(req, resp, session, output, uid, clientIsLocal)).handleRequest();
			}
		}
	}

}
