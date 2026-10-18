package de.webfilesys.gui.api;

import de.webfilesys.Constants;
import de.webfilesys.gui.user.UserRequestHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.text.DecimalFormat;

/**
 * @author Frank Hoehnel
 */
public class UploadStatusHandler extends UserRequestHandler {
	public UploadStatusHandler(
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
		
		DecimalFormat numFormat = new DecimalFormat("##,###,###,###");
		
		long fileSize = 0;
		
		Long uploadSize = (Long) session.getAttribute(Constants.UPLOAD_SIZE);
		
		if (uploadSize != null) {
			fileSize = uploadSize;
		}
		
		long bytesUploaded = 0;
		
		Long uploadCounter = (Long) session.getAttribute(Constants.UPLOAD_COUNTER);

		if (uploadCounter != null) {
			bytesUploaded = uploadCounter.longValue();
		}

		long percentUploaded = 0;

        if (fileSize > 0) {
			percentUploaded = (bytesUploaded * 100) / fileSize;
        }

        Boolean uploadSuccess = (Boolean) session.getAttribute(Constants.UPLOAD_SUCCESS);

        resp.setContentType("application/json");

        output.println("{");
        output.println("\"fileSize\": \"" + numFormat.format(fileSize) + "\",");
        output.println("\"bytesUploaded\": \"" + numFormat.format(bytesUploaded) + "\",");
        output.println("\"percent\": \"" + percentUploaded + "\",");
        output.println("\"success\": " + ((uploadSuccess != null) && uploadSuccess));
        output.println("}");
        output.flush();
	}
}
