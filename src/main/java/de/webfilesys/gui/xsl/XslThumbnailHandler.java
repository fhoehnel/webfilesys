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

import de.webfilesys.ClipBoard;
import de.webfilesys.Constants;
import de.webfilesys.FastPathManager;
import de.webfilesys.FileComparator;
import de.webfilesys.FileContainer;
import de.webfilesys.FileLinkSelector;
import de.webfilesys.FileSelectionStatus;
import de.webfilesys.GeoTag;
import de.webfilesys.LanguageManager;
import de.webfilesys.MetaInfManager;
import de.webfilesys.PictureRating;
import de.webfilesys.WebFileSys;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.UTF8URLEncoder;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslThumbnailHandler extends XslFileListHandlerBase {
	public XslThumbnailHandler(HttpServletRequest req, HttpServletResponse resp, HttpSession session,
			PrintWriter output, String uid, boolean clientIsLocal) {
		super(req, resp, session, output, uid);
	}

	protected void process() {
		session.setAttribute("viewMode", new Integer(Constants.VIEW_MODE_THUMBS));

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

		String fileFilter[] = Constants.imgFileMasks;

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

		int sortBy = FileComparator.SORT_BY_FILENAME;

		String temp = getParameter("sortBy");
		if ((temp != null) && (temp.length() > 0)) {
			try {
				sortBy = Integer.parseInt(temp);
				session.setAttribute("sortField", new Integer(sortBy));
			} catch (NumberFormatException nfe) {
			}
		} else {
			Integer sessionSortField = (Integer) session.getAttribute("sortField");

			if (sessionSortField != null) {
				sortBy = sessionSortField.intValue();
				if (sortBy > 7) {
					sortBy = FileComparator.SORT_BY_FILENAME;
				}
			}
		}

		int rating = (-1);

		temp = getParameter("rating");

		if (temp != null) {
			try {
				rating = Integer.parseInt(temp);

				session.setAttribute("rating", new Integer(rating));
			} catch (NumberFormatException nfe) {
			}
		} else {
			Integer sessionRating = (Integer) session.getAttribute("rating");

			if (sessionRating != null) {
				rating = sessionRating.intValue();
			}
		}

		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/thumbnailOnePage.xsl\"");

		Element fileListElement = doc.createElement("fileList");

		doc.appendChild(fileListElement);

		doc.insertBefore(xslRef, fileListElement);

		String errorMsg = getParameter("errorMsg");

		if (errorMsg != null) {
			XmlUtil.setChildText(fileListElement, "errorMsg", errorMsg, false);
		}

		ClipBoard clipBoard = (ClipBoard) session.getAttribute("clipBoard");

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
			processResponse("xsl/folderTree.xsl");
			return;
		}

		XmlUtil.setChildText(fileListElement, "dirModified", Long.toString(dirFile.lastModified()), false);
		
		XmlUtil.setChildText(fileListElement, "headLine", getHeadlinePath(currentPath), false);

		String description = metaInfMgr.getDescription(currentPath, ".");

		if ((description != null) && (description.trim().length() > 0)) {
			XmlUtil.setChildText(fileListElement, "description", description, true);
		}

		FileLinkSelector fileSelector = new FileLinkSelector(currentPath, sortBy, true);

		FileSelectionStatus selectionStatus = fileSelector.selectFiles(fileFilter, rating, 4096, 0);

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

		XmlUtil.setChildText(fileListElement, "sortBy", Integer.toString(sortBy), false);

		XmlUtil.setChildText(fileListElement, "rating", Integer.toString(rating), false);

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

				File pictureFile = fileCont.getRealFile();

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

				fileElement.setAttribute("lastModified", dateFormat.format(new Date(pictureFile.lastModified())));

				long kBytes = 0L;

				long fileSize = pictureFile.length();

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
				
				String realPath = pictureFile.getParent();

				String realFileName = pictureFile.getName();

				int commentCount = metaInfMgr.countComments(realPath, realFileName);

				XmlUtil.setChildText(fileElement, "comments", Integer.toString(commentCount));

				PictureRating pictureRating = metaInfMgr.getPictureRating(realPath, realFileName);

				if (pictureRating != null) {
					if (pictureRating.getNumberOfVotes() > 0) {
						XmlUtil.setChildText(fileElement, "visitorRating",
								Integer.toString(pictureRating.getAverageVisitorRating()));
						XmlUtil.setChildText(fileElement, "numberOfVotes",
								Integer.toString(pictureRating.getNumberOfVotes()));
					}
				}

				if (!readonly) {
					int ownerRating = metaInfMgr.getOwnerRating(pictureFile.getAbsolutePath());

					if (ownerRating > (-1)) {
						XmlUtil.setChildText(fileElement, "ownerRating", Integer.toString(ownerRating));
					}
				}

				String imgSrcPath = "/webfilesys/servlet?command=picThumb&imgFile=" + UTF8URLEncoder.encode(fileCont.getName());

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
			if ((clipBoard == null) || clipBoard.isEmpty()) {
				XmlUtil.setChildText(fileListElement, "clipBoardEmpty", "true");
			} else {
				if (clipBoard.isCopyOperation()) {
					XmlUtil.setChildText(fileListElement, "copyOperation", "true");
				}
			}

			if (WebFileSys.getInstance().isAutoCreateThumbs()) {
				XmlUtil.setChildText(fileListElement, "autoCreateThumbs", "true");
			}

			if (WebFileSys.getInstance().getMailHost() != null) {
				XmlUtil.setChildText(fileListElement, "mailEnabled", "true");
			}

			if (linkFound) {
				XmlUtil.setChildText(fileListElement, "linksExist", "true");
			}
		}

		GeoTag geoTag = metaInfMgr.getGeoTag(currentPath, ".");

		if (geoTag != null) {
			XmlUtil.setChildText(fileListElement, "geoTag", "true", false);

			// the reason for this is historic: previous google maps api version
			// required an API key
			XmlUtil.setChildText(fileListElement, "googleMaps", "true", false);
		}

		int pollInterval = WebFileSys.getInstance().getPollFilesysChangesInterval();
		if (pollInterval > 0) {
			XmlUtil.setChildText(fileListElement, "pollInterval", Integer.toString(pollInterval));
		}
		
        if (WebFileSys.getInstance().getFfmpegExePath() != null) {
            XmlUtil.setChildText(fileListElement, "videoEnabled", "true");
        }
		
        String scrollTo = getParameter("scrollTo");
        if (scrollTo != null) {
            XmlUtil.setChildText(fileListElement, "scrollTo", scrollTo);
        }
        
		addCurrentTrail(fileListElement, currentPath, userMgr.getDocumentRoot(uid), mask);

		processResponse("thumbnailOnePage.xsl", true);

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
