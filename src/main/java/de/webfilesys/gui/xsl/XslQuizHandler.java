package de.webfilesys.gui.xsl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.Category;
import de.webfilesys.Constants;
import de.webfilesys.FileComparator;
import de.webfilesys.FileContainer;
import de.webfilesys.FileLinkSelector;
import de.webfilesys.FileSelectionStatus;
import de.webfilesys.MetaInfManager;
import de.webfilesys.WebFileSys;
import de.webfilesys.graphics.ScaledImage;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.UTF8URLEncoder;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslQuizHandler extends XslRequestHandlerBase {
    
    public static final String ANSWER_FILE_MASKS[]={"a*.gif","a*.jpg","a*.jpeg","a*.png","q*.bmp"};

    public static final String SOLUTION_FILE_MASKS[]={"s*.gif","s*.jpg","s*.jpeg","s*.png","s*.bmp"};
    
    private static final String CORRECT_ANSWER_SUFFIX = "-correct";
    
    private static final int SOLUTION_THUMBNAIL_WIDTH = 800;
    private static final int SOLUTION_THUMBNAIL_HEIGHT = 480;
    
    private static final int ANSWER_THUMBNAIL_WIDTH = 500;
    private static final int ANSWER_THUMBNAIL_HEIGHT = 320;
    
    public XslQuizHandler(HttpServletRequest req, HttpServletResponse resp, HttpSession session, PrintWriter output,
            String uid) {
        super(req, resp, session, output, uid);
    }

    protected void process() {

    	String quizDirPath = getParameter("quizDirPath");
    	
    	if (CommonUtils.isEmpty(quizDirPath)) {
    		quizDirPath = getCwd();
    	} else {
            File quizDirFile = new File(quizDirPath);
            if (quizDirFile.exists() && quizDirFile.isDirectory() && quizDirFile.canRead()) {
        		session.setAttribute(Constants.SESSION_KEY_CWD, quizDirPath);
            }
    	}
    	
    	String afterDir = getParameter("afterDir");
    	String beforeDir = getParameter("beforeDir");
    	
    	QuestionInfo questionInfo = getCurrentQuestionPath(quizDirPath, beforeDir, afterDir);
    	String currentQuestionPath = questionInfo.getCurrentQuestionPath();
    	
        MetaInfManager metaInfMgr = MetaInfManager.getInstance();
        
        String path_no_slash = null;
        String pathWithSlash = null;

        if (currentQuestionPath.endsWith(File.separator)) {
            path_no_slash = currentQuestionPath.substring(0, currentQuestionPath.length() - 1);
            pathWithSlash = currentQuestionPath;
        } else {
            path_no_slash = currentQuestionPath;
            pathWithSlash = currentQuestionPath + File.separator;
        }

        String stylesheetName = "quiz.xsl";

        ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet",
                "type=\"text/xsl\" href=\"/webfilesys/xsl/" + stylesheetName + "\"");

        Element quizElement = doc.createElement("quiz");

        doc.appendChild(quizElement);

        doc.insertBefore(xslRef, quizElement);

        String quizTitle = metaInfMgr.getDescription(quizDirPath, ".");
        
        if (!CommonUtils.isEmpty(quizTitle)) {
        	XmlUtil.setChildText(quizElement, "quizTitle", quizTitle, true);
        }
        
        XmlUtil.setChildText(quizElement, "questionCount", Integer.toString(questionInfo.getQuestionCount()));
        XmlUtil.setChildText(quizElement, "currentQuestionNum", Integer.toString(questionInfo.getCurrentQuestionNum() + 1));
        
        if (questionInfo.getCurrentQuestionNum() == 0) {
            XmlUtil.setChildText(quizElement, "firstQuestion", "true");
        }

        if (questionInfo.getCurrentQuestionNum() == questionInfo.getQuestionCount() - 1) {
            XmlUtil.setChildText(quizElement, "lastQuestion", "true");
        }
        
        File dirFile = new File(currentQuestionPath);

        if ((!dirFile.exists()) || (!dirFile.isDirectory()) || (!dirFile.canRead())) {
            XmlUtil.setChildText(quizElement, "dirNotFound", "true", false);
            addMsgResource("alert.dirNotFound", getResource("alert.dirNotFound", "directory does not exist"));
            this.processResponse("xsl/quiz.xsl");
            return;
        }

        XmlUtil.setChildText(quizElement, "currentQuestionDir", dirFile.getName());
        
        String question = metaInfMgr.getDescription(currentQuestionPath, ".");

        if ((question != null) && (question.trim().length() > 0)) {
            XmlUtil.setChildText(quizElement, "question", question, true);
        }

        Element currentPathElem = doc.createElement("currentPath");

        quizElement.appendChild(currentPathElem);

        XmlUtil.setElementText(currentPathElem, currentQuestionPath, false);
        
        FileLinkSelector fileSelector = new FileLinkSelector(currentQuestionPath, FileComparator.SORT_BY_FILENAME, true);

        // solution picture
        
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
                scaledImage = new ScaledImage(fileCont.getRealFile().getAbsolutePath(), SOLUTION_THUMBNAIL_WIDTH, SOLUTION_THUMBNAIL_HEIGHT);

                XmlUtil.setChildText(solutionElem, "imgType", Integer.toString(scaledImage.getImageType()));
                XmlUtil.setChildText(solutionElem, "xpix", Integer.toString(scaledImage.getRealWidth()));
                XmlUtil.setChildText(solutionElem, "ypix", Integer.toString(scaledImage.getRealHeight()));

                int thumbWidth = 0;
                int thumbHeight = 0;

                if ((scaledImage.getRealWidth() <= SOLUTION_THUMBNAIL_WIDTH) && (scaledImage.getRealHeight() <= SOLUTION_THUMBNAIL_HEIGHT)) {
                    thumbHeight = scaledImage.getRealHeight();
                    thumbWidth = scaledImage.getRealWidth();
                } else {
                    if ((scaledImage.getRealHeight() * 100 / SOLUTION_THUMBNAIL_HEIGHT) > (scaledImage.getRealWidth() * 100 / SOLUTION_THUMBNAIL_WIDTH)) {
                        thumbHeight = SOLUTION_THUMBNAIL_HEIGHT;
                        thumbWidth = scaledImage.getRealWidth() * SOLUTION_THUMBNAIL_HEIGHT / scaledImage.getRealHeight();
                    } else {
                        thumbWidth = SOLUTION_THUMBNAIL_WIDTH;
                        thumbHeight = scaledImage.getRealHeight() * SOLUTION_THUMBNAIL_WIDTH / scaledImage.getRealWidth();
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

                String answerText = metaInfMgr.getDescription(path_no_slash, actFilename);

                if ((answerText != null) && (answerText.trim().length() > 0)) {
                    XmlUtil.setChildText(answerElement, "answerText", answerText, true);
                }

                if (isCorrectAnswer(fileCont.getRealFile())) {
                    XmlUtil.setChildText(answerElement, "correct", Boolean.toString(true));
                }
                
                String fullFileName = tempFile.getAbsolutePath();

                boolean imgFound = true;

                ScaledImage scaledImage = null;

                try {
                    scaledImage = new ScaledImage(fullFileName, ANSWER_THUMBNAIL_WIDTH, ANSWER_THUMBNAIL_HEIGHT);
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

                    if ((scaledImage.getRealWidth() <= ANSWER_THUMBNAIL_WIDTH) && (scaledImage.getRealHeight() <= ANSWER_THUMBNAIL_HEIGHT)) {
                        thumbHeight = scaledImage.getRealHeight();
                        thumbWidth = scaledImage.getRealWidth();
                    } else {
                        if ((scaledImage.getRealHeight() * 100 / ANSWER_THUMBNAIL_HEIGHT) > (scaledImage.getRealWidth() * 100 / ANSWER_THUMBNAIL_WIDTH)) {
                            thumbHeight = ANSWER_THUMBNAIL_HEIGHT;
                            thumbWidth = scaledImage.getRealWidth() * ANSWER_THUMBNAIL_HEIGHT / scaledImage.getRealHeight();
                        } else {
                            thumbWidth = ANSWER_THUMBNAIL_WIDTH;
                            thumbHeight = scaledImage.getRealHeight() * ANSWER_THUMBNAIL_WIDTH / scaledImage.getRealWidth();
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
    
    private QuestionInfo getCurrentQuestionPath(String quizDirPath, String beforeDir, String afterDir) {
    	QuestionInfo questionInfo = new QuestionInfo();
    	
    	File quizDirFile = new File(quizDirPath);
    	
    	ArrayList<String> subdirs = new ArrayList<String>();
    	
    	File[] fileList = quizDirFile.listFiles();
    	for (File file : fileList) {
    		if (file.isDirectory() && file.canRead()) {
    			subdirs.add(file.getName());
    		}
    	}
    	
    	questionInfo.setQuestionCount(subdirs.size());
    	
    	if (subdirs.size() > 1) {
    		Collections.sort(subdirs, new FileComparator(quizDirPath, FileComparator.SORT_BY_FILENAME));
    	}
    	
    	String currentQuestionSubdir = null;
    	
    	int currentQuestionNum = 0;
    	
    	if (!CommonUtils.isEmpty(beforeDir)) {
    		String lastBefore = subdirs.get(0);
    		for (String subdir : subdirs) {
    			if (subdir.toLowerCase().compareTo(beforeDir.toLowerCase()) >= 0) {
    				break;
    			} else {
    				lastBefore = subdir;
    				currentQuestionNum++;
    			}
    		}
    		currentQuestionSubdir = lastBefore;
    		currentQuestionNum--;
    	} else if (!CommonUtils.isEmpty(afterDir)) {
    		String firstAfter = subdirs.get(subdirs.size() - 1);
    		for (String subdir : subdirs) {
    			if (subdir.toLowerCase().compareTo(afterDir.toLowerCase()) > 0) {
    				firstAfter = subdir;
    				break;
    			}
				currentQuestionNum++;
    		}
    		currentQuestionSubdir = firstAfter;
    	} else {
    		currentQuestionSubdir = subdirs.get(0);
    	}
    	
    	questionInfo.setCurrentQuestionPath(CommonUtils.joinFilesysPath(quizDirPath, currentQuestionSubdir));
    	questionInfo.setCurrentQuestionNum(currentQuestionNum);
    	
    	return questionInfo;
    }

    private boolean isCorrectAnswer(File answerFile) {
    	return answerFile.getName().contains(CORRECT_ANSWER_SUFFIX);
    }
    
    private class QuestionInfo {
    	String currentQuestionPath;
    	int questionCount;
    	int currentQuestionNum;
    	
    	public void setCurrentQuestionPath(String newVal) {
    		currentQuestionPath = newVal;    		
    	}
    	
    	public String getCurrentQuestionPath() {
    		return currentQuestionPath;
    	}
    	
    	public void setQuestionCount(int newVal) {
    		questionCount = newVal;
    	}
    	
    	public int getQuestionCount() {
    		return questionCount;
    	}

    	public void setCurrentQuestionNum(int newVal) {
    		currentQuestionNum = newVal;
    	}
    	
    	public int getCurrentQuestionNum() {
    		return currentQuestionNum;
    	}
    }
}
