package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import de.webfilesys.Constants;
import de.webfilesys.FileContainerComparator;
import de.webfilesys.FileLinkSelector;
import de.webfilesys.FileSelectionStatus;
import de.webfilesys.MetaInfManager;
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

        String lastDirStatusTimeParam = getParameter("lastDirStatusTime");
        String lastSizeSumParam = getParameter("lastSizeSum");
        
        long lastDirStatusTime;
        long lastSizeSum;
        
        try {
        	lastDirStatusTime = Long.parseLong(lastDirStatusTimeParam);
        	lastSizeSum = Long.parseLong(lastSizeSumParam);
        } catch (NumberFormatException numEx) {
            Logger.getLogger(getClass()).error("invalid parameter: lastDirStatusTime: " + lastDirStatusTimeParam + " lastSizeSum: " + lastSizeSumParam);
            return;
        }
        
        File dirFile = new File(currentPath);
        
        long lastModified = dirFile.lastModified();
        
        String fileMask = getParameter("mask");
        
        if (CommonUtils.isEmpty(fileMask)) {
        	fileMask = "*";
        }
        
        String[] filterMasks = new String[] {fileMask};
        
		FileLinkSelector fileSelector = new FileLinkSelector(currentPath, FileContainerComparator.SORT_BY_FILENAME, true);

		FileSelectionStatus selectionStatus = fileSelector.selectFiles(filterMasks, Constants.MAX_FILE_NUM, 0);

		long currentSizeSum = selectionStatus.getFileSizeSum();
        
        Element resultElement = doc.createElement("result");

        boolean modified = false;
        if (fileMask.equals("*")) {
            modified = (lastModified > lastDirStatusTime) || (currentSizeSum != lastSizeSum);
        } else {
            modified = (currentSizeSum != lastSizeSum);
        }
        
        XmlUtil.setElementText(resultElement, Boolean.toString(modified));
        
        doc.appendChild(resultElement);
		
		processResponse();
	}
}
