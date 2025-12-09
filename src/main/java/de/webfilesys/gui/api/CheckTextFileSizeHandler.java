package de.webfilesys.gui.api;

import de.webfilesys.gui.user.UserRequestHandler;
import de.webfilesys.util.CommonUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Frank Hoehnel
 */
public class CheckTextFileSizeHandler extends UserRequestHandler {

    private static final long MAX_FILE_SIZE = 4 * 1024 * 1024;

	protected HttpServletRequest req;
	protected HttpServletResponse resp;

	public CheckTextFileSizeHandler(
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
        String filePath = req.getParameter("filePath");
        if (filePath == null) {
            String fileName = getParameter("fileName");
            String path = getCwd();
            filePath = CommonUtils.getFullPath(path, fileName);
        }

		if (!accessAllowed(filePath)) {
            try {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            } catch (IOException ex) {
            }
            return;
		}
		
		File textFile = new File(filePath);
		if (!textFile.exists() || !textFile.isFile() || !textFile.canRead()) {
            try {
                resp.sendError(HttpServletResponse.SC_CONFLICT, "file does not exist or is not readable");
            } catch (IOException ex) {
            }
            return;
		}

        if (textFile.length() > MAX_FILE_SIZE) {
            try {
                resp.sendError(HttpServletResponse.SC_CONFLICT, "file is too large for remote editing");
            } catch (IOException ex) {
            }
        }
    }
}
