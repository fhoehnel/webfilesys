package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Element;

import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class CheckUploadConflictHandler extends XmlRequestHandlerBase {
	
    private static final Logger LOG = LogManager.getLogger(CheckUploadConflictHandler.class);
	
	public CheckUploadConflictHandler(HttpServletRequest req, HttpServletResponse resp, HttpSession session,
			PrintWriter output, String uid) {
		super(req, resp, session, output, uid);
	}

	protected void process() {
		if (!checkWriteAccess()) {
			return;
		}
		
		String currentPath = getCwd();

		String[] filesToUpload = req.getParameterValues("file");
		
		ArrayList<String> conflictingFiles = new ArrayList<String>();
		
		for (String fileName : filesToUpload) {
		    File existingFile = new File(currentPath, fileName);
			if (existingFile.exists()) {
				conflictingFiles.add(fileName);
			}
		}
		
		Element resultElement = doc.createElement("result");
		doc.appendChild(resultElement);
		
		Element pathElem = doc.createElement("path");
		XmlUtil.setElementText(pathElem, currentPath);
		resultElement.appendChild(pathElem);
		
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
