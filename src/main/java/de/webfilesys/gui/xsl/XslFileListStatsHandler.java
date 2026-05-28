package de.webfilesys.gui.xsl;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.webfilesys.*;
import de.webfilesys.util.CommonUtils;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.util.XmlUtil;

/**
 * Statistics (view/download count, voting count, comment count) for the files of the current directory
 * (Tab Statistics).
 * 
 * @author Frank Hoehnel
 */
public class XslFileListStatsHandler extends XslFileListHandlerBase
{
	public XslFileListStatsHandler(
			HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid)
	{
        super(req, resp, session, output, uid);
	}
	  
	protected void process() {

        String currentPath = getParameter("actpath");
        if (currentPath == null || currentPath.isEmpty()) {
            currentPath = getCwd();
        } else {
            if (!accessAllowed(currentPath)) {
                return;
            }
            session.setAttribute(Constants.SESSION_KEY_CWD, currentPath);
        }

		session.setAttribute("viewMode", Constants.VIEW_MODE_STATS);

		int sortBy = FileComparator.SORT_BY_FILENAME;

        String temp = getParameter("sortBy");
        if (temp != null && !temp.isEmpty()) {
            try {
                sortBy = Integer.parseInt(temp);
                session.setAttribute("sortField", sortBy);
            } catch (NumberFormatException nfe) {
            }
        } else {
            Integer sortField = (Integer) session.getAttribute("sortField");
            if (sortField != null) {
                sortBy = sortField;
            }
        }
		
		Element fileListElem = doc.createElement("fileList");
			
		doc.appendChild(fileListElem);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/fileListStats.xsl\"");

		doc.insertBefore(xslRef, fileListElem);

		XmlUtil.setChildText(fileListElem, "currentPath", currentPath, false);
    	XmlUtil.setChildText(fileListElem, "pathForScript", insertDoubleBackslash(currentPath));
		XmlUtil.setChildText(fileListElem, "sortBy", Integer.toString(sortBy), false);
		
        String description = MetaInfManager.getInstance().getDescription(currentPath, ".");
        if (description != null && !description.isEmpty()) {
    		XmlUtil.setChildText(fileListElem, "description", description, true);
        }

		Date resetDate = MetaInfManager.getInstance().getStatisticsResetDate(currentPath);
		if (resetDate != null) {
			SimpleDateFormat dateFormat = LanguageManager.getInstance().getDateFormat(language);
			XmlUtil.setChildText(fileListElem, "lastResetDate", dateFormat.format(resetDate), false);
		}
		
		String[] fileMasks = new String[1];
		fileMasks[0] = "*";

		FileLinkSelector fileSelector = new FileLinkSelector(currentPath, sortBy, true);

		FileSelectionStatus selectionStatus = fileSelector.selectFiles(fileMasks, Constants.MAX_FILE_NUM, 0);

		ArrayList<FileContainer> selectedFiles = selectionStatus.getSelectedFiles();

		if (selectedFiles != null) {
            for (FileContainer fileCont : selectedFiles) {
                if (!fileCont.isLink()) {
                    Element fileElem = doc.createElement("file");
                    fileListElem.appendChild(fileElem);

                    String filename = fileCont.getName();

                    if (WebFileSysConfig.getInstance().isShowAssignedIcons()) {
                        String docImage = null;

                        int extIdx = filename.lastIndexOf('.');

                        if ((extIdx > 0) && (extIdx < (filename.length() - 1))) {
                            docImage = IconManager.getInstance().getAssignedIcon(filename.substring(extIdx + 1));
                        }

                        if (docImage == null) {
                            docImage = "doc.gif";
                        }

                        fileElem.setAttribute("icon", docImage);
                    }

                    fileElem.setAttribute("name", filename);

                    if (filename.length() > 50) {
                        fileElem.setAttribute("displayName", CommonUtils.shortName(filename, 50));
                    }

                    String filePath = fileCont.getRealFile().getAbsolutePath();

                    int viewCount = MetaInfManager.getInstance().getNumberOfDownloads(filePath);
                    XmlUtil.setChildText(fileElem, "viewCount", Integer.toString(viewCount));

                    int voteCount = MetaInfManager.getInstance().getVisitorRatingCount(filePath);
                    XmlUtil.setChildText(fileElem, "voteCount", Integer.toString(voteCount));

                    int voteStarSum = MetaInfManager.getInstance().getVisitorRatingStarSum(filePath);
                    XmlUtil.setChildText(fileElem, "voteStarSum", Integer.toString(voteStarSum));

                    int commentCount = MetaInfManager.getInstance().countComments(filePath);
                    XmlUtil.setChildText(fileElem, "commentCount", Integer.toString(commentCount));

                    if (commentCount > 0) {
                        XmlUtil.setChildText(fileElem, "pathForScript", insertDoubleBackslash(filePath));
                    }
                }
            }
		}		
		
        if (WebFileSysConfig.getInstance().getFfmpegExePath() != null) {
            XmlUtil.setChildText(fileListElem, "videoEnabled", "true");
        }
        if (WebFileSysConfig.getInstance().isDownloadStatistics()) {
            XmlUtil.setChildText(fileListElem, "statistics", "true", false);
        }

        addCurrentTrail(fileListElem, currentPath, userMgr.getDocumentRoot(uid), "*");

        processResponse("fileListStats.xsl");
    }
	
}