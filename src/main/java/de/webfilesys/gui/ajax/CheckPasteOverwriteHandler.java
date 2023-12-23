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

import de.webfilesys.ClipBoard;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class CheckPasteOverwriteHandler extends XmlRequestHandlerBase {
	
    private static final Logger LOG = LogManager.getLogger(CheckPasteOverwriteHandler.class);
	
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
		
		boolean isFolderCopy = false;
		boolean destFolderFileConflict = false;
		boolean targetEqualsSource = false;
		boolean targetIsSubOfSource = false;
		
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
		} else {
			ArrayList<String> clipDirs = clipBoard.getAllDirs();
			if (clipDirs != null) {
				for (String sourcePath : clipDirs) {
					String sourceDir = sourcePath.substring(sourcePath.lastIndexOf(File.separatorChar) + 1);
					
					File destDirFile = new File(path, sourceDir);

					if (sourcePath.equals(path)) {
						targetEqualsSource = true;
					} else {
						if (path.startsWith(sourcePath) && (path.length() > sourcePath.length() + 1) && (path.charAt(sourcePath.length()) == File.separatorChar)) {
							targetIsSubOfSource = true;
						} else {
							if (destDirFile.exists()) {
								conflictingFiles.add(sourceDir);
								
								if (destDirFile.isFile()) {
									destFolderFileConflict = true;
								}
							}
						}
					}
					
					isFolderCopy = true;
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

		if (isFolderCopy) {
			XmlUtil.setChildText(resultElement, "folder", "true", false);
		}
		
		if (destFolderFileConflict) {
			XmlUtil.setChildText(resultElement, "destFolderFileConflict", "true", false);
		}
	
		if (targetEqualsSource) {
			XmlUtil.setChildText(resultElement, "targetEqualsSource", "true", false);
		}

		if (targetIsSubOfSource) {
			XmlUtil.setChildText(resultElement, "targetIsSubOfSource", "true", false);
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
