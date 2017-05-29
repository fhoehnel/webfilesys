package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;

import de.webfilesys.DirTreeStatus;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class PollForFolderTreeChangeHandler extends XmlRequestHandlerBase
{
	public PollForFolderTreeChangeHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid) {
        super(req, resp, session, output, uid);
	}
	
	protected void process() {
        // String currentPath = getCwd();
        
        boolean modified = false;
        
		DirTreeStatus dirTreeStatus = (DirTreeStatus) session.getAttribute("dirTreeStatus");
		
		if (dirTreeStatus != null) {
			ArrayList<String> expandedFolders = dirTreeStatus.getExpandedFolders();
			
			for (String path : expandedFolders) {
				File folderFile = new File(path);
				if (folderFile.exists() && folderFile.isDirectory()) {
					long lastKnownNameLengthSum = dirTreeStatus.getSubdirNameLenghtSum(path);
					if (lastKnownNameLengthSum >= 0) {
						long currentNameLengthSum = getSubdirNameLengthSum(folderFile);
						if (lastKnownNameLengthSum != currentNameLengthSum) {
							modified = true;
							break;
						}
					}
				}
			}
		}
        
        Element resultElement = doc.createElement("result");

        XmlUtil.setElementText(resultElement, Boolean.toString(modified));
        
        doc.appendChild(resultElement);
		
		processResponse();
	}
	
	private long getSubdirNameLengthSum(File folderFile) {
		long subdirNameLengthSum = 0;
		
		File[] files = folderFile.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					subdirNameLengthSum += file.getName().length();
				}
			}
		}
		
		return subdirNameLengthSum;
	}
}
