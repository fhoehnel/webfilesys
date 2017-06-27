package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import de.webfilesys.Constants;
import de.webfilesys.FileContainerComparator;
import de.webfilesys.FileLinkSelector;
import de.webfilesys.FileSelectionStatus;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class PollForDirChangeHandler extends XmlRequestHandlerBase
{
	public PollForDirChangeHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid) {
        super(req, resp, session, output, uid);
	}
	
	protected void process() {
        String currentPath = getCwd();

        // String lastDirStatusTimeParam = getParameter("lastDirStatusTime");
        String lastSizeSumParam = getParameter("lastSizeSum");
        
        // long lastDirStatusTime;
        long lastSizeSum;
        
        try {
        	// lastDirStatusTime = Long.parseLong(lastDirStatusTimeParam);
        	lastSizeSum = Long.parseLong(lastSizeSumParam);
        } catch (NumberFormatException numEx) {
            // Logger.getLogger(getClass()).error("invalid parameter: lastDirStatusTime: " + lastDirStatusTimeParam + " lastSizeSum: " + lastSizeSumParam);
            Logger.getLogger(getClass()).error("invalid parameter: lastSizeSum: " + lastSizeSumParam);
            return;
        }
        
        // File dirFile = new File(currentPath);
        // long lastModified = dirFile.lastModified();
        
        String fileMask = getParameter("mask");
        
        if (CommonUtils.isEmpty(fileMask)) {
        	fileMask = "*";
        }
        
        String[] filterMasks = new String[] {fileMask};
        
        boolean isThumbnailView = (getParameter("thumbnails") != null);
        
        if (isThumbnailView) {
        	if (fileMask.equals("*")) {
        		filterMasks = Constants.imgFileMasks;
        	}
        }
        
		FileLinkSelector fileSelector = new FileLinkSelector(currentPath, FileContainerComparator.SORT_BY_FILENAME, true);

		FileSelectionStatus selectionStatus = fileSelector.selectFiles(filterMasks, Constants.MAX_FILE_NUM, 0);

		long currentSizeSum = selectionStatus.getFileSizeSum();
        
        Element resultElement = doc.createElement("result");

//      Adding or deleting subdirectories causes (sometimes?) a change of the last modification time of the directory, but this
//      should not trigger a change event in the file list / thumbnail view. So we rely only on the file size sum to detect a change.
//
//        boolean modified = false;
//        if (fileMask.equals("*") && (!isThumbnailView)) {
//            modified = (lastModified > lastDirStatusTime) || (currentSizeSum != lastSizeSum);
//        } else {
//            modified = (currentSizeSum != lastSizeSum);
//        }

        boolean modified = (currentSizeSum != lastSizeSum);
        
        XmlUtil.setElementText(resultElement, Boolean.toString(modified));
        
        doc.appendChild(resultElement);
		
		processResponse();
	}
}
