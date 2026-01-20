package de.webfilesys.gui.api;

import de.webfilesys.FileSysBookmark;
import de.webfilesys.FileSysBookmarkManager;
import de.webfilesys.gui.user.UserRequestHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;

/**
 * @author Frank Hoehnel
 */
public class CreateBookmarkHandler extends UserRequestHandler {
	public CreateBookmarkHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid) {

        super(req, resp, session, output, uid);
	}
	
	protected void process() {
		if (!checkWriteAccess()) {
			return;
		}

        String path = getCwd();

        String bookmarkName = getParameter("bookmarkName");
        FileSysBookmarkManager bookmarkMgr = FileSysBookmarkManager.getInstance();
        FileSysBookmark newBookmark = new FileSysBookmark();
        newBookmark.setPath(path);
        newBookmark.setName(bookmarkName);
        bookmarkMgr.createBookmark(uid, newBookmark);
	}
}
