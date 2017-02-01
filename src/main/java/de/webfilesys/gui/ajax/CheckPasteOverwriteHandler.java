package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import de.webfilesys.ClipBoard;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class CheckPasteOverwriteHandler extends XmlRequestHandlerBase {
	
    private static final Logger LOG = Logger.getLogger(CheckPasteOverwriteHandler.class);
	
	public CheckPasteOverwriteHandler(HttpServletRequest req, HttpServletResponse resp, HttpSession session,
			PrintWriter output, String uid) {
		super(req, resp, session, output, uid);
	}

	protected void process() {
		if (!checkWriteAccess()) {
			return;
		}
		
		String path = getParameter("path");
		
		if (CommonUtils.isEmpty(path)) {
			path = getCwd();
		} else {
			if (!checkAccess(path)) {
				return;
			}
		}

		ClipBoard clipBoard = (ClipBoard) session.getAttribute(ClipBoard.SESSION_KEY);
		
		if (clipBoard == null) {
			// should never happen
			LOG.warn("paste request for empty clipboard for path " + path);
            return;
		}
		
		ArrayList<String> conflictingFiles = new ArrayList<String>();
		
		ArrayList<String> clipFiles = clipBoard.getAllFiles();

		if (clipFiles != null) {
			for (String clipFilePath : clipFiles) {
				String clipFile = clipFilePath.substring(clipFilePath.lastIndexOf(File.separatorChar) + 1);
				File existingFile = new File(path, clipFile);
				if (existingFile.exists()) {
					conflictingFiles.add(clipFile);
				}
			}
		}
		
		Element resultElement = doc.createElement("result");
		doc.appendChild(resultElement);
		
		if (!CommonUtils.isEmpty(getParameter("path"))) {
			Element pathElem = doc.createElement("path");
			XmlUtil.setElementText(pathElem, path);
			resultElement.appendChild(pathElem);
		}
		
		int counter = 0;
		
		for (String conflictingFile : conflictingFiles) {
			Element conflictElem = doc.createElement("conflict");
			if (counter < 12) {
				XmlUtil.setElementText(conflictElem, conflictingFile);
				resultElement.appendChild(conflictElem);
				counter++;
			} else {
				XmlUtil.setElementText(conflictElem, getResource("morePasteConflicts", "... more"));
				resultElement.appendChild(conflictElem);
                break;
			}
		}

		processResponse();
	}
}
