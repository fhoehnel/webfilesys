package de.webfilesys.gui.xsl;

import java.io.File;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.Constants;
import de.webfilesys.FastPathManager;
import de.webfilesys.FileContainer;
import de.webfilesys.FileLinkSelector;
import de.webfilesys.FileSelectionStatus;
import de.webfilesys.LanguageManager;
import de.webfilesys.MetaInfManager;
import de.webfilesys.WebFileSys;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.UTF8URLEncoder;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslVideoListHandler extends XslFileListHandlerBase {
	public XslVideoListHandler(HttpServletRequest req, HttpServletResponse resp, HttpSession session,
			PrintWriter output, String uid, boolean clientIsLocal) {
		super(req, resp, session, output, uid);
	}

	protected void process() {
		session.setAttribute("viewMode", new Integer(Constants.VIEW_MODE_VIDEO));

		MetaInfManager metaInfMgr = MetaInfManager.getInstance();

		String currentPath = getParameter("actPath");
		if ((currentPath == null) || (currentPath.length() == 0)) {
			currentPath = getParameter("actpath");
			if ((currentPath == null) || (currentPath.length() == 0)) {
				currentPath = getCwd();
			}
		}

		session.setAttribute(Constants.SESSION_KEY_CWD, currentPath);

		String mask = getParameter("mask");

		if ((mask != null) && (mask.length() > 0)) {
			session.setAttribute("mask", mask);
		} else {
			if (mask == null) {
				mask = (String) session.getAttribute("mask");
			} else {
				session.removeAttribute("mask");
			}
		}

		if ((mask == null) || (mask.length() == 0)) {
			mask = "*";
		}

		String fileFilter[] = Constants.VIDEO_FILE_MASKS;

		if (!mask.equals("*")) {
			String maskFilter[] = new String[fileFilter.length];

			String lowerCaseMask = mask.toLowerCase();

			for (int i = 0; i < fileFilter.length; i++) {
				if (lowerCaseMask.endsWith(fileFilter[i].substring(1).toLowerCase())) {
					maskFilter[i] = mask;
				} else {
					if (mask.endsWith(".*")) {
						maskFilter[i] = mask.substring(0, mask.length() - 2) + fileFilter[i].substring(1);
					} else {
						maskFilter[i] = mask + fileFilter[i].substring(1);
					}
				}
			}

			fileFilter = maskFilter;
		}

		String pathWithoutSlash = null;
		String pathWithSlash = null;

		if (currentPath.endsWith(File.separator)) {
			pathWithoutSlash = currentPath.substring(0, currentPath.length() - 1);
			pathWithSlash = currentPath;
		} else {
			pathWithoutSlash = currentPath;
			pathWithSlash = currentPath + File.separator;
		}

		boolean dirHasMetaInf = metaInfMgr.dirHasMetaInf(currentPath);

		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/videoList.xsl\"");

		Element fileListElement = doc.createElement("fileList");

		doc.appendChild(fileListElement);

		doc.insertBefore(xslRef, fileListElement);

		String errorMsg = getParameter("errorMsg");

		if (errorMsg != null) {
			XmlUtil.setChildText(fileListElement, "errorMsg", errorMsg, false);
		}

		if (readonly) {
			XmlUtil.setChildText(fileListElement, "readonly", "true", false);
		}

		if (WebFileSys.getInstance().isMaintananceMode()) {
			if (!isAdminUser(false)) {
				XmlUtil.setChildText(fileListElement, "maintananceMode", "true", false);
			}
		}

		XmlUtil.setChildText(fileListElement, "css", userMgr.getCSS(uid), false);

		XmlUtil.setChildText(fileListElement, "language", language, false);

		if (isBrowserXslEnabled()) {
			XmlUtil.setChildText(fileListElement, "browserXslEnabled", "true", false);
		}

		File dirFile = new File(currentPath);

		if ((!dirFile.exists()) || (!dirFile.isDirectory()) || (!dirFile.canRead())) {
			Logger.getLogger(getClass()).warn("folder is not a readable directory: " + currentPath);
			XmlUtil.setChildText(fileListElement, "dirNotFound", "true", false);
			processResponse("videoList.xsl");
			return;
		}

		XmlUtil.setChildText(fileListElement, "headLine", getHeadlinePath(currentPath), false);

		String description = metaInfMgr.getDescription(currentPath, ".");

		if ((description != null) && (description.trim().length() > 0)) {
			XmlUtil.setChildText(fileListElement, "description", description, true);
		}

		FileLinkSelector fileSelector = new FileLinkSelector(currentPath, 1, true);

		FileSelectionStatus selectionStatus = fileSelector.selectFiles(fileFilter, -1, 4096, 0);

		filterLinksOutsideDocRoot(selectionStatus);

		ArrayList<FileContainer> selectedFiles = selectionStatus.getSelectedFiles();

		int fileNum = 0;

		if (selectedFiles != null) {
			fileNum = selectionStatus.getNumberOfFiles();
		}

		XmlUtil.setChildText(fileListElement, "sizeSumBytes", Long.toString(selectionStatus.getFileSizeSum()), false);
		
		XmlUtil.setChildText(fileListElement, "fileNumber", Integer.toString(fileNum), false);

		XmlUtil.setChildText(fileListElement, "currentPath", currentPath, false);

		XmlUtil.setChildText(fileListElement, "menuPath", escapeForJavascript(currentPath), false);

		XmlUtil.setChildText(fileListElement, "pathForScript", escapeForJavascript(pathWithSlash), false);

		XmlUtil.setChildText(fileListElement, "relativePath", escapeForJavascript(getHeadlinePath(currentPath)), false);
		
		XmlUtil.setChildText(fileListElement, "filter", mask, false);

		boolean linkFound = false;

		if (selectedFiles != null) {
			SimpleDateFormat dateFormat = LanguageManager.getInstance().getDateFormat(language);

			// DecimalFormat numFormat=new DecimalFormat("0,000,000,000,000");
			DecimalFormat numFormat = new DecimalFormat("#,###,###,###,###");

			long sizeSum = 0l;
			
			for (int i = 0; i < selectedFiles.size(); i++) {

				Element fileElement = doc.createElement("file");

				fileListElement.appendChild(fileElement);

				FileContainer fileCont = (FileContainer) selectedFiles.get(i);

				String picFilename = fileCont.getName();

				fileElement.setAttribute("name", picFilename);
				fileElement.setAttribute("nameForScript", escapeForJavascript(picFilename));
				fileElement.setAttribute("nameForId", picFilename.replace(' ',  '_'));

				fileElement.setAttribute("id", Integer.toString(i));

				File videoFile = fileCont.getRealFile();

				if (fileCont.isLink()) {
					fileElement.setAttribute("link", "true");
					XmlUtil.setChildText(fileElement, "realPath", fileCont.getRealFile().getAbsolutePath(), false);
					XmlUtil.setChildText(fileElement, "realPathForScript", escapeForJavascript(fileCont.getRealFile().getAbsolutePath()), false);

					linkFound = true;
				}

				description = null;

				if (fileCont.isLink()) {
					description = metaInfMgr.getDescription(fileCont.getRealFile().getAbsolutePath());
				} else {
					if (dirHasMetaInf) {
						description = metaInfMgr.getDescription(pathWithoutSlash, picFilename);
					}
				}

				if (!CommonUtils.isEmpty(description)) {
					XmlUtil.setChildText(fileElement, "description", description, true);
				}

				String displayName = CommonUtils.shortName(picFilename, 22);

				XmlUtil.setChildText(fileElement, "displayName", displayName);

				if (fileCont.isLink()) {
					fileElement.setAttribute("linkMenuPath", escapeForJavascript(fileCont.getRealFile().getAbsolutePath()));
				}

				fileElement.setAttribute("lastModified", dateFormat.format(new Date(videoFile.lastModified())));

				long kBytes = 0L;

				long fileSize = videoFile.length();

				if (fileSize > 0L) {
					kBytes = fileSize / 1024L;
					if (kBytes == 0L) {
						kBytes = 1;
					}
				}

				fileElement.setAttribute("size", numFormat.format(kBytes));

				if (!fileCont.isLink()) {
				    sizeSum += fileSize;
				}				
				
				String realPath = videoFile.getParent();

				String realFileName = videoFile.getName();

				String imgSrcPath = "/webfilesys/servlet?command=videoThumb&videoFile=" + UTF8URLEncoder.encode(fileCont.getName());

				if (fileCont.isLink()) {
					imgSrcPath += "&link=true";
				}

				XmlUtil.setChildText(fileElement, "imgPath", imgSrcPath);
			}
			
			if (selectedFiles.size() > 0) {
				addFormattedSizeSum(sizeSum, fileListElement);		
			}
		}

		if (!readonly) {
			if (linkFound) {
				XmlUtil.setChildText(fileListElement, "linksExist", "true");
			}
		}
		
		addCurrentTrail(fileListElement, currentPath, userMgr.getDocumentRoot(uid), mask);

		processResponse("videoList.xsl");

		FastPathManager.getInstance().queuePath(uid, currentPath);
	}

	private void filterLinksOutsideDocRoot(FileSelectionStatus selectionStatus) {
		ArrayList<FileContainer> filteredOutList = new ArrayList<FileContainer>();

		ArrayList<FileContainer> selectedFiles = selectionStatus.getSelectedFiles();

		if (selectedFiles != null) {
			for (int i = 0; i < selectedFiles.size(); i++) {
				FileContainer fileCont = (FileContainer) selectedFiles.get(i);
				if (fileCont.isLink()) {
					if (!accessAllowed(fileCont.getRealFile().getAbsolutePath())) {
						filteredOutList.add(fileCont);
					}
				}
			}

			if (filteredOutList.size() > 0) {
				selectionStatus.setNumberOfFiles(selectionStatus.getNumberOfFiles() - filteredOutList.size());
				selectionStatus.getSelectedFiles().removeAll(filteredOutList);
			}
		}
	}

}
