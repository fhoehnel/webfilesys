package de.webfilesys.gui.xsl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.Constants;
import de.webfilesys.FileComparator;
import de.webfilesys.FileContainer;
import de.webfilesys.FileLinkSelector;
import de.webfilesys.FileSelectionStatus;
import de.webfilesys.MetaInfManager;
import de.webfilesys.WebFileSys;
import de.webfilesys.graphics.ScaledImage;
import de.webfilesys.util.UTF8URLEncoder;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslQuizHandler extends XslRequestHandlerBase {
    
    public static final String ANSWER_FILE_MASKS[]={"a*.gif","a*.jpg","a*.jpeg","a*.png","q*.bmp"};

    public static final String SOLUTION_FILE_MASKS[]={"s*.gif","s*.jpg","s*.jpeg","s*.png","s*.bmp"};
    
    public XslQuizHandler(HttpServletRequest req, HttpServletResponse resp, HttpSession session, PrintWriter output,
            String uid) {
        super(req, resp, session, output, uid);
    }

    protected void process() {

        session.setAttribute("viewMode", new Integer(Constants.VIEW_MODE_QUIZ));

        MetaInfManager metaInfMgr = MetaInfManager.getInstance();

        String currentPath = null;

        if (currentPath == null) {
            currentPath = getParameter("actPath");
            if ((currentPath == null) || (currentPath.length() == 0)) {
                currentPath = getParameter("actpath");
                if ((currentPath == null) || (currentPath.length() == 0)) {
                    currentPath = getCwd();

                    if (currentPath == null) {
                        currentPath = WebFileSys.getInstance().getUserMgr().getDocumentRoot(uid);
                    }
                }
            }
        }

        session.setAttribute(Constants.SESSION_KEY_CWD, currentPath);

        String path_no_slash = null;
        String pathWithSlash = null;

        if (currentPath.endsWith(File.separator)) {
            path_no_slash = currentPath.substring(0, currentPath.length() - 1);
            pathWithSlash = currentPath;
        } else {
            path_no_slash = currentPath;
            pathWithSlash = currentPath + File.separator;
        }

        boolean dirHasMetaInf = metaInfMgr.dirHasMetaInf(currentPath);

        String stylesheetName = "quiz.xsl";

        ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet",
                "type=\"text/xsl\" href=\"/webfilesys/xsl/" + stylesheetName + "\"");

        Element quizElement = doc.createElement("quiz");

        doc.appendChild(quizElement);

        doc.insertBefore(xslRef, quizElement);

        File dirFile = new File(currentPath);

        if ((!dirFile.exists()) || (!dirFile.isDirectory()) || (!dirFile.canRead())) {
            XmlUtil.setChildText(quizElement, "dirNotFound", "true", false);
            addMsgResource("alert.dirNotFound", getResource("alert.dirNotFound", "directory does not exist"));
            this.processResponse("xsl/quiz.xsl");
            return;
        }

        String question = metaInfMgr.getDescription(currentPath, ".");

        if ((question != null) && (question.trim().length() > 0)) {
            XmlUtil.setChildText(quizElement, "question", question, true);
        }

        Element currentPathElem = doc.createElement("currentPath");

        quizElement.appendChild(currentPathElem);

        XmlUtil.setElementText(currentPathElem, currentPath, false);
        
        FileLinkSelector fileSelector = new FileLinkSelector(currentPath, FileComparator.SORT_BY_FILENAME, true);

        // solution picture
        
        int thumbnailSize = 600;
        
        FileSelectionStatus selectionStatus = fileSelector.selectFiles(SOLUTION_FILE_MASKS, 4, 0);

        ArrayList<FileContainer> selectedFiles = selectionStatus.getSelectedFiles();

        if ((selectedFiles != null) && (selectedFiles.size() > 0)) {
            FileContainer fileCont = (FileContainer) selectedFiles.get(0);

            Element solutionElem = doc.createElement("solution");
            
            String solutionText = metaInfMgr.getDescription(fileCont.getRealFile().getAbsolutePath());
            
            XmlUtil.setChildText(solutionElem, "solutionText", solutionText, true);
            
            quizElement.appendChild(solutionElem);

            ScaledImage scaledImage = null;

            try {
                scaledImage = new ScaledImage(fileCont.getRealFile().getAbsolutePath(), thumbnailSize, thumbnailSize);

                XmlUtil.setChildText(solutionElem, "imgType", Integer.toString(scaledImage.getImageType()));
                XmlUtil.setChildText(solutionElem, "xpix", Integer.toString(scaledImage.getRealWidth()));
                XmlUtil.setChildText(solutionElem, "ypix", Integer.toString(scaledImage.getRealHeight()));

                int thumbWidth = 0;
                int thumbHeight = 0;

                if ((scaledImage.getRealWidth() <= thumbnailSize) && (scaledImage.getRealHeight() <= thumbnailSize)) {
                    thumbHeight = scaledImage.getRealHeight();
                    thumbWidth = scaledImage.getRealWidth();
                } else {
                    if (scaledImage.getRealHeight() > scaledImage.getRealWidth()) {
                        thumbHeight = thumbnailSize;
                        thumbWidth = scaledImage.getRealWidth() * thumbnailSize / scaledImage.getRealHeight();
                    } else {
                        thumbWidth = thumbnailSize;
                        thumbHeight = scaledImage.getRealHeight() * thumbnailSize / scaledImage.getRealWidth();
                    }
                }

                XmlUtil.setChildText(solutionElem, "thumbnailWidth", Integer.toString(thumbWidth));
                XmlUtil.setChildText(solutionElem, "thumbnailHeight", Integer.toString(thumbHeight));

                String srcFileName = "/webfilesys/servlet?command=getFile&filePath=" + UTF8URLEncoder.encode(fileCont.getRealFile().getAbsolutePath()) + "&cached=true";

                XmlUtil.setChildText(solutionElem, "imgPath", srcFileName);
            } catch (IOException io1) {
                Logger.getLogger(getClass()).error("failed to get scaled image dimensions", io1);
            }
        }
        
        // answer pictures
        
        thumbnailSize = 400;
        
        selectionStatus = fileSelector.selectFiles(ANSWER_FILE_MASKS, 4, 0);
        
        selectedFiles = selectionStatus.getSelectedFiles();

        int fileNum = 0;

        if (selectedFiles != null) {
            fileNum = selectionStatus.getNumberOfFiles();
        }

        XmlUtil.setChildText(quizElement, "fileNumber", Integer.toString(fileNum), false);

        XmlUtil.setChildText(quizElement, "pathForScript", insertDoubleBackslash(pathWithSlash), false);

        if (selectedFiles != null) {
            // boolean metaInfFileIncluded=false;

            for (int i = 0; i < selectedFiles.size(); i++) {
                Element answerElement = doc.createElement("answer");

                quizElement.appendChild(answerElement);

                char answerId = (char) ('A' + i);
                
                answerElement.setAttribute("answerId", Character.toString(answerId));
                
                FileContainer fileCont = (FileContainer) selectedFiles.get(i);

                String actFilename = fileCont.getName();

                answerElement.setAttribute("name", actFilename);

                answerElement.setAttribute("id", Integer.toString(i));

                File tempFile = fileCont.getRealFile();

                if (fileCont.isLink()) {
                    answerElement.setAttribute("link", "true");
                    XmlUtil.setChildText(answerElement, "realPath", fileCont.getRealFile().getAbsolutePath(), false);
                    XmlUtil.setChildText(answerElement, "realPathForScript", insertDoubleBackslash(fileCont.getRealFile()
                            .getAbsolutePath()), false);

                    if (!this.accessAllowed(fileCont.getRealFile().getAbsolutePath())) {
                        answerElement.setAttribute("outsideDocRoot", "true");
                    }
                }

                String description = null;

                if (fileCont.isLink()) {
                    description = metaInfMgr.getDescription(fileCont.getRealFile().getAbsolutePath());
                } else {
                    if (dirHasMetaInf) {
                        description = metaInfMgr.getDescription(path_no_slash, actFilename);
                    }
                }

                if ((description != null) && (description.trim().length() > 0)) {
                    XmlUtil.setChildText(answerElement, "description", description, true);
                }

                String fullFileName = tempFile.getAbsolutePath();

                boolean imgFound = true;

                ScaledImage scaledImage = null;

                try {
                    scaledImage = new ScaledImage(fullFileName, thumbnailSize, thumbnailSize);
                } catch (IOException io1) {
                    Logger.getLogger(getClass()).error("failed to get scaled image dimensions", io1);
                    imgFound = false;
                }

                if (imgFound) {
                    XmlUtil.setChildText(answerElement, "imgType", Integer.toString(scaledImage.getImageType()));
                    XmlUtil.setChildText(answerElement, "xpix", Integer.toString(scaledImage.getRealWidth()));
                    XmlUtil.setChildText(answerElement, "ypix", Integer.toString(scaledImage.getRealHeight()));

                    int thumbWidth = 0;
                    int thumbHeight = 0;

                    if ((scaledImage.getRealWidth() <= thumbnailSize) && (scaledImage.getRealHeight() <= thumbnailSize)) {
                        thumbHeight = scaledImage.getRealHeight();
                        thumbWidth = scaledImage.getRealWidth();
                    } else {
                        if (scaledImage.getRealHeight() > scaledImage.getRealWidth()) {
                            thumbHeight = thumbnailSize;
                            thumbWidth = scaledImage.getRealWidth() * thumbnailSize / scaledImage.getRealHeight();
                        } else {
                            thumbWidth = thumbnailSize;
                            thumbHeight = scaledImage.getRealHeight() * thumbnailSize / scaledImage.getRealWidth();
                        }
                    }

                    XmlUtil.setChildText(answerElement, "thumbnailWidth", Integer.toString(thumbWidth));
                    XmlUtil.setChildText(answerElement, "thumbnailHeight", Integer.toString(thumbHeight));

                    String srcFileName = "/webfilesys/servlet?command=getFile&filePath="
                            + UTF8URLEncoder.encode(fullFileName) + "&cached=true";

                    XmlUtil.setChildText(answerElement, "imgPath", srcFileName);
                }
            }
        }

        processResponse(stylesheetName, false);
    }
}
