package de.webfilesys.gui.xsl.album;

import java.io.File;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

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
import de.webfilesys.graphics.ThumbnailThread;
import de.webfilesys.gui.xsl.XslRequestHandlerBase;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.UTF8URLEncoder;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslPictureAlbumHandler extends XslRequestHandlerBase {

    public XslPictureAlbumHandler(HttpServletRequest req, HttpServletResponse resp, HttpSession session,
            PrintWriter output, String uid) {
        super(req, resp, session, output, uid);
    }

    protected void process() {
        MetaInfManager metaInfMgr = MetaInfManager.getInstance();

        String actPath = null;

        String docRoot = userMgr.getDocumentRoot(uid);

        String docRootOS = docRoot;

        if (File.separatorChar == '\\') {
            docRootOS = docRoot.replace('/', File.separatorChar);
        }

        String relativePath = getParameter("relPath");

        if ((relativePath != null) && (relativePath.trim().length() > 0)) {
            if (relativePath.equals(File.separator)) {
                actPath = docRoot;

                if (File.separatorChar == '\\') {
                    actPath = actPath.replace('/', File.separatorChar);
                }
            } else {
                if (relativePath.startsWith(File.separator)) {
                    actPath = docRootOS + relativePath;
                } else {
                    actPath = docRootOS + File.separator + relativePath;
                }
            }
        } else {
            actPath = getParameter("actPath");

            if ((actPath == null) || (actPath.length() == 0)) {
                actPath = getCwd();

                if (actPath == null) {
                    actPath = userMgr.getDocumentRoot(uid);
                }

                if (File.separatorChar == '\\') {
                    actPath = actPath.replace('/', File.separatorChar);
                }
            }

            relativePath = actPath.substring(docRoot.length());
        }

        session.setAttribute(Constants.SESSION_KEY_CWD, actPath);

        String showDeatailsParm = req.getParameter("showDetails");

        boolean showDetails = false;

        if (showDeatailsParm != null) {
            showDetails = Boolean.valueOf(showDeatailsParm);
            session.setAttribute("showDetails", new Boolean(showDetails));
        } else {
            Boolean showDetailsFromSession = (Boolean) session.getAttribute("showDetails");
            if (showDetailsFromSession != null) {
                showDetails = showDetailsFromSession.booleanValue();
            }
        }

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
                if (lowerCaseMask.endsWith(fileFilter[i].toLowerCase())) {
                    maskFilter[i] = mask;
                } else {
                    maskFilter[i] = mask + fileFilter[i].substring(1);
                }
            }

            fileFilter = maskFilter;
        }

        String pathNoSlash = null;
        String pathWithSlash = null;

        if (actPath.endsWith(File.separator)) {
            pathNoSlash = actPath.substring(0, actPath.length() - 1);
            pathWithSlash = actPath;
        } else {
            pathNoSlash = actPath;
            pathWithSlash = actPath + File.separator;
        }

        boolean dirHasMetaInf = metaInfMgr.dirHasMetaInf(actPath);

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
            } else {
                sortBy = FileComparator.SORT_BY_FILENAME;
            }
        }

        String initial = getParameter("initial");

        if ((initial != null) && (initial.equalsIgnoreCase("true"))) {
            session.removeAttribute("rating");
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

        ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet",
                "type=\"text/xsl\" href=\"/webfilesys/xsl/album/pictureAlbum.xsl\"");

        Element albumElement = doc.createElement("pictureAlbum");

        doc.appendChild(albumElement);

        doc.insertBefore(xslRef, albumElement);

        // path section
        Element currentPathElem = doc.createElement("currentPath");

        albumElement.appendChild(currentPathElem);

        currentPathElem.setAttribute("path", relativePath);

        Element partOfPathElem = doc.createElement("pathElem");

        currentPathElem.appendChild(partOfPathElem);

        partOfPathElem.setAttribute("name", "home");

        partOfPathElem.setAttribute("path", File.separator);

        StringTokenizer pathParser = new StringTokenizer(relativePath, File.separator);

        StringBuffer partialPath = new StringBuffer();

        while (pathParser.hasMoreTokens()) {
            String partOfPath = pathParser.nextToken();

            partialPath.append(partOfPath);

            if (pathParser.hasMoreTokens()) {
                partialPath.append(File.separatorChar);
            }

            partOfPathElem = doc.createElement("pathElem");

            currentPathElem.appendChild(partOfPathElem);

            partOfPathElem.setAttribute("name", partOfPath);

            partOfPathElem.setAttribute("path", UTF8URLEncoder.encode(partialPath.toString()));
        }
        // end path section

        // subdir section
        Element foldersElem = doc.createElement("folders");

        albumElement.appendChild(foldersElem);

        String relPathWithSlash = relativePath;

        if ((relativePath.length() > 0) && (!relativePath.endsWith(File.separator))) {
            relPathWithSlash = relativePath + File.separator;
        }

        File dirFile = new File(actPath);

        if ((!dirFile.exists()) || (!dirFile.isDirectory()) || (!dirFile.canRead())) {
            LogManager.getLogger(getClass()).error("folder is not a readable directory: " + actPath);
            return;
        }
        
        File[] fileList = dirFile.listFiles();
        
        if (fileList != null) {
            ArrayList<String> subFolders = new ArrayList<String>();

            for (File file : fileList) {
                if (file.isDirectory() && file.canRead()) {
                    if (!file.getName().equals(ThumbnailThread.THUMBNAIL_SUBDIR)) {
                        subFolders.add(file.getName());
                    }
                }
            }
            
            if (subFolders.size() > 1) {
                Collections.sort(subFolders);
            }
            
            for (String subFolder : subFolders) {
                String shortDirName = subFolder;

                if (shortDirName.length() > 16) {
                    shortDirName = CommonUtils.shortName(shortDirName, 16);
                }

                Element subDirElem = doc.createElement("folder");

                foldersElem.appendChild(subDirElem);

                subDirElem.setAttribute("name", subFolder);

                subDirElem.setAttribute("displayName", shortDirName);

                subDirElem.setAttribute("path", UTF8URLEncoder.encode(relPathWithSlash + subFolder));
                
            }
        }
        
        // end subdir section

        if (readonly) {
            XmlUtil.setChildText(albumElement, "readonly", "true", false);
        }

        XmlUtil.setChildText(albumElement, "userid", uid, false);

        XmlUtil.setChildText(albumElement, "headLine", getHeadlinePath(actPath), false);

        String description = metaInfMgr.getDescription(actPath, ".");

        if ((description != null) && (description.trim().length() > 0)) {
            XmlUtil.setChildText(albumElement, "description", description, true);
        }

        FileLinkSelector fileSelector = new FileLinkSelector(actPath, sortBy, true);

        FileSelectionStatus selectionStatus = fileSelector.selectFiles(fileFilter, rating, 4096, 0);

        ArrayList<FileContainer> selectedFiles = selectionStatus.getSelectedFiles();

        int fileNum = 0;

        if (selectedFiles != null) {
            fileNum = selectionStatus.getNumberOfFiles();
        }

        XmlUtil.setChildText(albumElement, "fileNumber", Integer.toString(fileNum), false);

        // XmlUtil.setChildText(albumElement, "currentPath", actPath, false);

        String menuPath = actPath;

        if (File.separatorChar == '\\') {
            menuPath = insertDoubleBackslash(actPath.replace('/', '\\'));
        }

        XmlUtil.setChildText(albumElement, "menuPath", menuPath, false);

        XmlUtil.setChildText(albumElement, "pathForScript", insertDoubleBackslash(pathWithSlash), false);

        XmlUtil.setChildText(albumElement, "filter", mask, false);

        XmlUtil.setChildText(albumElement, "sortBy", Integer.toString(sortBy), false);

        XmlUtil.setChildText(albumElement, "rating", Integer.toString(rating), false);

        if (showDetails) {
            XmlUtil.setChildText(albumElement, "showDetails", "true", false);
        }

        Element fileListElement = doc.createElement("fileList");

        albumElement.appendChild(fileListElement);

        if (selectedFiles != null) {
            SimpleDateFormat dateFormat = LanguageManager.getInstance().getDateFormat(language);

            // DecimalFormat numFormat=new DecimalFormat("0,000,000,000,000");
            DecimalFormat numFormat = new DecimalFormat("#,###,###,###,###");

            for (int i = 0; i < selectedFiles.size(); i++) {

                Element fileElement = doc.createElement("file");

                fileListElement.appendChild(fileElement);

                FileContainer fileCont = (FileContainer) selectedFiles.get(i);

                String actFilename = fileCont.getName();

                fileElement.setAttribute("name", actFilename);

                fileElement.setAttribute("id", Integer.toString(i));

                File tempFile = fileCont.getRealFile();

                if (fileCont.isLink()) {
                    fileElement.setAttribute("link", "true");
                    XmlUtil.setChildText(fileElement, "realPath", fileCont.getRealFile().getAbsolutePath(), false);
                    XmlUtil.setChildText(fileElement, "realPathForScript", insertDoubleBackslash(fileCont.getRealFile()
                            .getAbsolutePath()), false);

                    if (!this.accessAllowed(fileCont.getRealFile().getAbsolutePath())) {
                        fileElement.setAttribute("outsideDocRoot", "true");
                    }
                }

                description = null;

                if (fileCont.isLink()) {
                    description = metaInfMgr.getDescription(fileCont.getRealFile().getAbsolutePath());
                } else {
                    if (dirHasMetaInf) {
                        description = metaInfMgr.getDescription(pathNoSlash, actFilename);
                    }
                }

                if ((description != null) && (description.trim().length() > 0)) {
                    XmlUtil.setChildText(fileElement, "description", description, true);
                }

                String displayName = CommonUtils.shortName(actFilename, 20);

                XmlUtil.setChildText(fileElement, "displayName", displayName);

                if (fileCont.isLink()) {
                    if (this.accessAllowed(fileCont.getRealFile().getAbsolutePath())) {
                        fileElement.setAttribute("linkMenuPath", insertDoubleBackslash(fileCont.getRealFile()
                                .getAbsolutePath()));
                    } else {
                        fileElement.setAttribute("outsideDocRoot", "true");
                    }
                }

                fileElement.setAttribute("lastModified", dateFormat.format(new Date(tempFile.lastModified())));

                long kBytes = 0L;

                long fileSize = tempFile.length();

                if (fileSize > 0L) {
                    kBytes = fileSize / 1024L;

                    if (kBytes == 0L) {
                        kBytes = 1;
                    }
                }

                fileElement.setAttribute("size", numFormat.format(kBytes));

                String realPath = tempFile.getParent();

                String realFileName = tempFile.getName();

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

                String imgSrcPath = "/webfilesys/servlet?command=picThumb&imgFile=" + UTF8URLEncoder.encode(fileCont.getName());

                if (fileCont.isLink()) {
                    imgSrcPath += "&link=true";
                }

                XmlUtil.setChildText(fileElement, "imgPath", imgSrcPath);
            }
        }

        GeoTag geoTag = metaInfMgr.getGeoTag(actPath, ".");

        if (geoTag != null) {
            XmlUtil.setChildText(albumElement, "geoTag", "true", false);

            // the reason for this is historic: previous google maps api version
            // required an API key
            XmlUtil.setChildText(albumElement, "googleMaps", "true", false);
        }

        this.processResponse("album/pictureAlbum.xsl");

        FastPathManager.getInstance().queuePath(uid, actPath);
    }
}
