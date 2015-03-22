package de.webfilesys.gui.blog;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import de.webfilesys.FileComparator;
import de.webfilesys.MetaInfManager;
import de.webfilesys.gui.ajax.XmlRequestHandlerBase;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * Move a blog entry up or down within a day.
 */
public class BlogMoveEntryHandler extends XmlRequestHandlerBase {
	public BlogMoveEntryHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid) {
        super(req, resp, session, output, uid);
	}
	
	protected void process() {
		if (!checkWriteAccess()) {
			return;
		}
		
		String fileToMove = getParameter("fileName");

		if (CommonUtils.isEmpty(fileToMove)) {
			Logger.getLogger(getClass()).error("missing parameter fileName");
			return;
		}
		
		String direction = getParameter("direction");
		if (CommonUtils.isEmpty(direction)) {
			Logger.getLogger(getClass()).error("missing parameter direction");
			return;
		}

		if ((!direction.equals("up")) && (!direction.equals("down"))) {
			Logger.getLogger(getClass()).error("invalid parameter direction");
			return;
		}
		
		String currentPath = userMgr.getDocumentRoot(uid).replace('/',  File.separatorChar);

		boolean moveSuccess = false;
		
		ArrayList<File> filesOfDay = getAllFilesOfDaySorted(currentPath, fileToMove);
		
		if (direction.equals("up")) {
			File prevFile = null;
			
			Iterator<File> iter = filesOfDay.iterator();
			
            boolean found = false;
            
			while ((!found) && iter.hasNext()) {
			     File fileOfDay = iter.next();
			     if (fileOfDay.getName().equals(fileToMove)) {
			    	 found = true;
			     } else {
			    	 prevFile = fileOfDay;
			     }
			}
			
			if (found && (prevFile != null)) {
				moveSuccess = swapFileNamesAndRandomize(currentPath, prevFile.getName(), fileToMove);				
			}
		} else if (direction.equals("down")) {
			File nextFile = null;
			
			Iterator<File> iter = filesOfDay.iterator();
			
            boolean found = false;
            
			while ((!found) && iter.hasNext()) {
			     File fileOfDay = iter.next();
			     if (fileOfDay.getName().equals(fileToMove)) {
			    	 found = true;
			    	 if (iter.hasNext()) {
			    		 nextFile = iter.next();
			    	 }
			     }
			}
			
			if (found && (nextFile != null)) {
				moveSuccess = swapFileNamesAndRandomize(currentPath, nextFile.getName(), fileToMove);				
			}
		}
		
		Element resultElement = doc.createElement("result");

		XmlUtil.setChildText(resultElement, "success", Boolean.toString(moveSuccess));
		
		doc.appendChild(resultElement);
		
		processResponse();
	}
	
	private boolean swapFileNamesAndRandomize(String currentPath, String fileName1, String fileName2) {
		
		boolean moveSuccess = false;
		
		int firstDotIdx = fileName1.indexOf('.');
        String fileName1Base = fileName1.substring(0, firstDotIdx);

        int lastDotIdx = fileName1.lastIndexOf('.');
        String fileName1Ext = fileName1.substring(lastDotIdx + 1);

        // file 1 -> temp file name
        String tempFileName = System.currentTimeMillis() + "." + fileName1Ext;
		if (renameInclMetaInf(currentPath, fileName1, tempFileName)) {

            // file 2 -> file 1 name + rand
			String file2NewName = fileName1Base + "." + System.currentTimeMillis() + "." + fileName1Ext;
			if (renameInclMetaInf(currentPath, fileName2, file2NewName)) {
                
				// temp filename -> file 2
				firstDotIdx = fileName2.indexOf('.');
                String fileName2Base = fileName2.substring(0, firstDotIdx);

                lastDotIdx = fileName2.lastIndexOf('.');
                String fileName2Ext = fileName2.substring(lastDotIdx + 1);
				
				String file1NewName = fileName2Base + "." + System.currentTimeMillis() + "." + fileName2Ext;
				
    			if (renameInclMetaInf(currentPath, tempFileName, file1NewName)) {
    				moveSuccess = true;
    			}
			}
		}
		
		return moveSuccess;
	}
	
    private boolean renameInclMetaInf(String currentPath, String fileToMove, String newFileName) {
    	File sourceFile = new File(currentPath, fileToMove);
    	File destFile = new File(currentPath, newFileName);
    	
    	if (!sourceFile.renameTo(destFile)) {
			Logger.getLogger(getClass()).error("failed to rename file " + fileToMove + " to : " + newFileName);
    		return false;
    	}

    	MetaInfManager.getInstance().moveMetaInf(currentPath, fileToMove, newFileName);
    	
    	return true;
    }
	
	private ArrayList<File> getAllFilesOfDaySorted(String currentPath, String fileToMove) {
		
		ArrayList<File> entriesOfDay = new ArrayList<File>();

		String currentFileDate = fileToMove.substring(0, 10);
		
		File blogDir = new File(currentPath);
		
		File[] filesInDir = blogDir.listFiles();
		
		for (int i = 0; i < filesInDir.length; i++) {
			if (filesInDir[i].isFile() && filesInDir[i].canRead()) {
	            
	            if (isPictureFile(filesInDir[i])) {
					
					String fileName = filesInDir[i].getName();
					if (fileName.length() >= 10) {
						String blogDate = fileName.substring(0, 10);
						
                        if (blogDate.equals(currentFileDate)) {
    						entriesOfDay.add(filesInDir[i]);
                        }
					}
				}
			}
		}
		
        if (entriesOfDay.size() > 1) {
			Collections.sort(entriesOfDay, new FileComparator());
        }
		
		return entriesOfDay;
	}
	
	private boolean isPictureFile(File file) {
		String fileNameExt = CommonUtils.getFileExtension(file.getName());
		
		return fileNameExt.equals(".jpg") || fileNameExt.equals(".jpeg") || fileNameExt.equals(".png") || fileNameExt.equals(".gif");
	}
	
}
