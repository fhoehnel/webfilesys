package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.PrintWriter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import de.webfilesys.gui.user.UserRequestHandler;

import de.webfilesys.WinDriveManager;

/**
 * Check for addedd/removed drives.
 * 
 * @author Frank Hoehnel
 */
public class RefreshDriveListHandler extends UserRequestHandler {

    public RefreshDriveListHandler(HttpServletRequest req, HttpServletResponse resp, HttpSession session,
			PrintWriter output, String uid) {
		super(req, resp, session, output, uid);
	}

	protected void process() {
		if ((File.separatorChar == '\\') && userMgr.getDocumentRoot(uid).equals("*:")) {
			WinDriveManager.getInstance().queryDrives();
		}
	}
}
