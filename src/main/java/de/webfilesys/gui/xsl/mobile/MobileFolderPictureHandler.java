package de.webfilesys.gui.xsl.mobile;

import java.io.File;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.ClipBoard;
import de.webfilesys.Constants;
import de.webfilesys.FileComparator;
import de.webfilesys.FileContainer;
import de.webfilesys.FileLinkSelector;
import de.webfilesys.FileSelectionStatus;
import de.webfilesys.LanguageManager;
import de.webfilesys.MetaInfManager;
import de.webfilesys.PictureRating;
import de.webfilesys.WebFileSys;
import de.webfilesys.gui.xsl.XslRequestHandlerBase;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.UTF8URLEncoder;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class MobileFolderPictureHandler extends XslRequestHandlerBase {
	private static final int MOBILE_FILE_PAGE_SIZE = 2048;
	
	public MobileFolderPictureHandler(
			HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid) {
        super(req, resp, session, output, uid);
	}
	  
	protected void process() {
		// session.setViewMode(Constants.VIEW_MODE_THUMBS);

	    if (getParameter("initial") != null) {
            session.setAttribute("mobile", "true");
	    }
	    
		MetaInfManager metaInfMgr = MetaInfManager.getInstance();

        String currentPath = getCwd();
        
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
        
		int sortBy = FileComparator.SORT_BY_FILENAME;

		String temp = getParameter("sortBy");
		if ((temp != null) && (temp.length() > 0)) {
			try {
				sortBy = Integer.parseInt(temp);
				session.setAttribute("sortField", new Integer(sortBy));
			} catch (NumberFormatException nfe) {
			}
		} else {
			Integer sortField = (Integer) session.getAttribute("sortField");
			if (sortField != null) {
				sortBy = sortField.intValue();
			}
		}
		
        String pathNoSlash = null;
        String pathWithSlash = null;

		if (currentPath.endsWith(File.separator)) {
			pathNoSlash = currentPath.substring(0, currentPath.length()-1);
			pathWithSlash = currentPath;
		} else {
			pathNoSlash = currentPath;
			pathWithSlash = currentPath + File.separator;
		}

        boolean dirHasMetaInf = false;

        if (pathNoSlash.length() > 0) {
            dirHasMetaInf = metaInfMgr.dirHasMetaInf(currentPath);
        }
		
		int startIdx = 0;

		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/mobile/folderPictures.xsl\"");

		Element fileListElem = doc.createElement("fileList");
			
		doc.appendChild(fileListElem);
			
		doc.insertBefore(xslRef, fileListElem);
		
		if (File.separatorChar == '\\') 
		{
		    XmlUtil.setChildText(fileListElem, "serverOS", "win");
		}
		else
		{
            XmlUtil.setChildText(fileListElem, "serverOS", "ix");
		}
		
		String relativePath = getHeadlinePath(currentPath);
		
		// path section
		Element currentPathElem = doc.createElement("currentPath");
		
		fileListElem.appendChild(currentPathElem);
		
		currentPathElem.setAttribute("path", relativePath);
		
		currentPathElem.setAttribute("pathForScript", insertDoubleBackslash(relativePath));
		
		XmlUtil.setChildText(fileListElem, "filter", mask, false);

		String docRoot = userMgr.getDocumentRoot(uid);
        
        if (((File.separatorChar == '\\') && (docRoot.charAt(0) != '*')) ||
            ((File.separatorChar == '/') && (docRoot.length() > 1))) {
            // userid as first path element
            
            Element partOfPathElem = doc.createElement("pathElem");
            
            currentPathElem.appendChild(partOfPathElem);
                
            partOfPathElem.setAttribute("name", uid);
                
            partOfPathElem.setAttribute("path", "/");
        }
        
        if (((File.separatorChar == '\\') && (docRoot.charAt(0) == '*')) ||
            ((File.separatorChar == '/') && (docRoot.length() == 1))) {
            // host name as first path element
            
            Element partOfPathElem = doc.createElement("pathElem");
            
            currentPathElem.appendChild(partOfPathElem);
                
            partOfPathElem.setAttribute("name", WebFileSys.getInstance().getLocalHostName());
                
            partOfPathElem.setAttribute("path", "/");
        }        
		
		StringTokenizer pathParser = new StringTokenizer(relativePath, File.separator);
		
		StringBuffer partialPath = new StringBuffer();
		
		while (pathParser.hasMoreTokens()) {
			String partOfPath = pathParser.nextToken();
			
			partialPath.append(partOfPath);
			
			if (pathParser.hasMoreTokens())
			{
				partialPath.append(File.separatorChar);		
			}
			
			Element partOfPathElem = doc.createElement("pathElem");
			
			currentPathElem.appendChild(partOfPathElem);
			
			partOfPathElem.setAttribute("name", partOfPath);
			
			partOfPathElem.setAttribute("path", UTF8URLEncoder.encode(partialPath.toString()));
		}
		// end path section
		
		if (readonly) {
			XmlUtil.setChildText(fileListElem, "readonly", "true", false);
		}
		
        if (WebFileSys.getInstance().getMailHost() !=null) {
            XmlUtil.setChildText(fileListElem, "mailEnabled", "true");
        }
            
		XmlUtil.setChildText(fileListElem, "userid", uid, false);
		
        File dirFile = new File(currentPath);
	    
		if ((!dirFile.exists()) || (!dirFile.isDirectory()) || (!dirFile.canRead())) {
		    LogManager.getLogger(getClass()).error("directory not found or not readable: " + dirFile);
			processResponse("mobile/folderPictures.xsl");
			return; 
		}

        if ((File.separatorChar != '\\') ||
            (docRoot.charAt(0) != '*') ||
            (!relativePath.equals(File.separator))) {
            String description = metaInfMgr.getDescription(currentPath,".");

            if ((description != null) && (description.trim().length() > 0)) {
                XmlUtil.setChildText(fileListElem, "description", description, true);
            }
        }

        ArrayList<FileContainer> selectedFiles = null;

        FileSelectionStatus selectionStatus = null;

        if (pathNoSlash.length() > 0) {
            FileLinkSelector fileSelector = new FileLinkSelector(currentPath, sortBy, true);
            selectionStatus = fileSelector.selectFiles(fileFilter, -1, MOBILE_FILE_PAGE_SIZE, startIdx);
            selectedFiles = selectionStatus.getSelectedFiles();
        }

		int fileNum = 0;

		if (selectedFiles != null)
		{
			fileNum = selectionStatus.getNumberOfFiles();
		}
		
		XmlUtil.setChildText(fileListElem, "fileNumber", Integer.toString(fileNum), false);

        String menuPath = currentPath;
        
        if (File.separatorChar == '\\')
        {
        	menuPath = insertDoubleBackslash(currentPath.replace('/', '\\'));
        }

		XmlUtil.setChildText(fileListElem, "menuPath", menuPath, false);

		XmlUtil.setChildText(fileListElem, "pathForScript", insertDoubleBackslash(pathWithSlash), false);

		if (selectedFiles != null) {
			SimpleDateFormat dateFormat = LanguageManager.getInstance().getDateFormat(language);

			// DecimalFormat numFormat=new DecimalFormat("0,000,000,000,000");
			DecimalFormat numFormat = new DecimalFormat("#,###,###,###,###");

			long sizeSum = 0l;
			
			for (int i = 0; i < selectedFiles.size(); i++) {

				Element fileElement = doc.createElement("file");

				fileListElem.appendChild(fileElement);

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
				}

				String description = null;

				if (fileCont.isLink()) {
					description = metaInfMgr.getDescription(fileCont.getRealFile().getAbsolutePath());
				} else {
					if (dirHasMetaInf) {
						description = metaInfMgr.getDescription(pathNoSlash, picFilename);
					}
				}

				if (!CommonUtils.isEmpty(description)) {
					XmlUtil.setChildText(fileElement, "description", description, true);
				}

				String displayName = CommonUtils.shortName(picFilename, 20);

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
		}

        if (!readonly)
        {
            ClipBoard clipBoard = (ClipBoard) session.getAttribute("clipBoard");
            
            if ((clipBoard == null) || clipBoard.isEmpty())
            {
                XmlUtil.setChildText(fileListElem, "clipBoardEmpty", "true");
            }
            else
            {
                if (clipBoard.isCopyOperation())
                {
                    XmlUtil.setChildText(fileListElem, "copyOperation", "true");
                }
            }
        }
		
	    processResponse("mobile/folderPictures.xsl");
	}
}
