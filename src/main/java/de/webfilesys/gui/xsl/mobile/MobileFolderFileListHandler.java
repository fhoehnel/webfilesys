package de.webfilesys.gui.xsl.mobile;

import java.io.File;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import com.sun.org.apache.xml.internal.security.utils.XMLUtils;

import de.webfilesys.ClipBoard;
import de.webfilesys.Constants;
import de.webfilesys.FastPathManager;
import de.webfilesys.FileComparator;
import de.webfilesys.FileContainer;
import de.webfilesys.FileLinkSelector;
import de.webfilesys.FileSelectionStatus;
import de.webfilesys.IconManager;
import de.webfilesys.LanguageManager;
import de.webfilesys.MetaInfManager;
import de.webfilesys.WebFileSys;
import de.webfilesys.WinDriveManager;
import de.webfilesys.graphics.ThumbnailThread;
import de.webfilesys.gui.xsl.XslRequestHandlerBase;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.StringComparator;
import de.webfilesys.util.UTF8URLEncoder;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class MobileFolderFileListHandler extends XslRequestHandlerBase
{
	private static final int MOBILE_FILE_PAGE_SIZE = 2048;
	
	private static final int MAX_FILENAME_DISPLAY_LENGTH = 26;
	
	private static final String[] FILTER_ALL_FILES = new String[] {"*"};

	public MobileFolderFileListHandler(
			HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid)
	{
        super(req, resp, session, output, uid);
	}
	  
	protected void process()
	{
		// session.setViewMode(Constants.VIEW_MODE_THUMBS);

	    if (getParameter("initial") != null)
	    {
            session.setAttribute("mobile", "true");
	    }
	    
		MetaInfManager metaInfMgr=MetaInfManager.getInstance();

		String docRoot = userMgr.getDocumentRoot(uid);
		
		String docRootOS = docRoot;
		
		if (File.separatorChar == '\\')
		{
			docRootOS = docRoot.replace('/', File.separatorChar);
		}

        String currentPath = null;

        String relativePath = getParameter("relPath");
        
        if (relativePath == null) 
        {
            // absPath parameter is used from the bookmark list
            String absPath = getParameter("absPath");
            
            if (absPath != null) 
            {
                if (checkAccess(absPath))
                {
                    session.setAttribute(Constants.SESSION_KEY_CWD, absPath);
                }
            }
            
            String cwd = getCwd();
            
            if (cwd != null) 
            {
            	File cwdFile = new File(cwd);
            	if (!cwdFile.exists()) {
            		// folder has been deleted?
            		cwdFile = cwdFile.getParentFile();
            		if (cwdFile.exists() && cwdFile.isDirectory() && cwdFile.canRead()) {
            			if (checkAccess(cwdFile.getAbsolutePath())) {
            				cwd = cwdFile.getAbsolutePath();
            			}
            		}
            	}
            	
                if (docRootOS.charAt(0) == '*') 
                {
                    relativePath = cwd;
                }
                else
                {
                    relativePath = cwd.substring(docRootOS.length());
                }
            }
            else
            {
                if (File.separatorChar == '\\')
                {
                    // relativePath = "C:\\";
                    relativePath = "\\";
                }
                else
                {
                    relativePath = "/";
                }
            }
        }
        else if (File.separatorChar == '\\') 
        {
            relativePath = relativePath.replace('/', File.separatorChar);
        }
        
        if ((File.separatorChar == '\\') && (docRoot.charAt(0) == '*')) 
        {
            if ((relativePath.charAt(0) == '\\') && (relativePath.length() > 1))
            {
                currentPath = relativePath.substring(1);
            }
            else
            {
                currentPath = relativePath;
            }
        }
        else
        {
            if (docRootOS.endsWith(File.separator) || (relativePath.charAt(0) == File.separatorChar))
            {
                currentPath = docRootOS + relativePath;
            }
            else
            {
                currentPath = docRootOS + File.separator + relativePath;
            }
	    }

		session.setAttribute(Constants.SESSION_KEY_CWD, currentPath);

        boolean maskChanged = false;

		String mask = getParameter("mask");
			
        if (mask != null)
        {
			String oldMask = (String) session.getAttribute("mask");

			if ((oldMask != null) && (!oldMask.equals(mask)))
			{
				maskChanged = true;
			}
        }
		
		if ((mask != null) && (mask.length() > 0))
		{
			session.setAttribute("mask", mask);
		}
		else
		{
			if (mask == null)
			{
				mask = (String) session.getAttribute("mask");
			}
			else
			{
				session.removeAttribute("mask");
			}
		}
		
        String fileFilter[] = FILTER_ALL_FILES;
        
		if ((mask != null) && (mask.length()> 0))
		{
		    fileFilter = new String[1];
		    fileFilter[0] = mask;
		}
		else
		{
		    mask = "*";
		}
		
        String pathNoSlash = null;
        String pathWithSlash = null;

		if (currentPath.endsWith(File.separator))
		{
			pathNoSlash = currentPath.substring(0, currentPath.length()-1);
			pathWithSlash = currentPath;
		}
		else
		{
			pathNoSlash = currentPath;
			pathWithSlash = currentPath + File.separator;
		}

        boolean dirHasMetaInf = false;

        if (pathNoSlash.length() > 0) 
        {
            dirHasMetaInf = metaInfMgr.dirHasMetaInf(currentPath);
        }
		
		int startIdx = (-1);

		String initial = getParameter("initial");
        
		if ((initial != null) && (initial.equalsIgnoreCase("true")))
		{
			session.removeAttribute("startIdx");
		}

        if (maskChanged)
        {
        	startIdx = 0;
        	
			session.setAttribute("startIdx", new Integer(startIdx));
        }
        else
        {
			String startIdxParm = getParameter("startIdx");

			if ((startIdxParm != null) && (startIdxParm.trim().length() > 0))
			{
				try
				{
					startIdx = Integer.parseInt(startIdxParm);

					session.setAttribute("startIdx",new Integer(startIdx));
				}
				catch (NumberFormatException nfex)
				{
				}
			}
		
			if (startIdx < 0)
			{
				Integer startIdxFromSession = (Integer) session.getAttribute("startIdx");
			
				if (startIdxFromSession != null)
				{
					startIdx = startIdxFromSession.intValue();
				}
			}
		
			if (startIdx < 0)
			{
				startIdx = 0;
			}
        }

		int sortBy = FileComparator.SORT_BY_FILENAME;

		String temp = getParameter("sortBy");
		if ((temp != null) && (temp.length() > 0))
		{
			try
			{
				sortBy=Integer.parseInt(temp);

				session.setAttribute("sortField", new Integer(sortBy));
			}
			catch (NumberFormatException nfe)
			{
			}
		}
		else
		{
			Integer sortField = (Integer) session.getAttribute("sortField");
			
			if (sortField != null)
			{
				sortBy = sortField.intValue();
			}
			else
			{
				sortBy = FileComparator.SORT_BY_FILENAME;
			}
		}

		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/mobile/folderFileList.xsl\"");

		Element folderFileListElement = doc.createElement("folderFileList");
			
		doc.appendChild(folderFileListElement);
			
		doc.insertBefore(xslRef, folderFileListElement);
		
		if (File.separatorChar == '\\') 
		{
		    XmlUtil.setChildText(folderFileListElement, "serverOS", "win");
		}
		else
		{
            XmlUtil.setChildText(folderFileListElement, "serverOS", "ix");
		}
		
		// path section
		Element currentPathElem = doc.createElement("currentPath");
		
		folderFileListElement.appendChild(currentPathElem);
		
		currentPathElem.setAttribute("path", relativePath);
		
		currentPathElem.setAttribute("pathForScript", insertDoubleBackslash(relativePath));
		
        if (((File.separatorChar == '\\') && (docRoot.charAt(0) != '*')) ||
            ((File.separatorChar == '/') && (docRoot.length() > 1)))
        {
            // userid as first path element
            
            Element partOfPathElem = doc.createElement("pathElem");
            
            currentPathElem.appendChild(partOfPathElem);
                
            partOfPathElem.setAttribute("name", uid);
                
            partOfPathElem.setAttribute("path", "/");
        }
        
        if (((File.separatorChar == '\\') && (docRoot.charAt(0) == '*')) ||
            ((File.separatorChar == '/') && (docRoot.length() == 1)))
        {
            // host name as first path element
            
            Element partOfPathElem = doc.createElement("pathElem");
            
            currentPathElem.appendChild(partOfPathElem);
                
            partOfPathElem.setAttribute("name", WebFileSys.getInstance().getLocalHostName());
                
            partOfPathElem.setAttribute("path", "/");
        }        
		
		StringTokenizer pathParser = new StringTokenizer(relativePath, File.separator);
		
		StringBuffer partialPath = new StringBuffer();
		
		while (pathParser.hasMoreTokens())
		{
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

        // subdir section
		Element foldersElem = doc.createElement("folders");
		
		folderFileListElement.appendChild(foldersElem);

        String relPathWithSlash = relativePath;
        
        if ((relativePath.length() > 0) && (!relativePath.endsWith(File.separator)))
        {
        	relPathWithSlash = relativePath + File.separator;
        }
        
        File dirFile = new File(currentPath);
        
        String folderName = dirFile.getName();
        if (CommonUtils.isEmpty(folderName)) {
        	folderName = dirFile.getAbsolutePath();
        }
        
		currentPathElem.setAttribute("folderName", insertDoubleBackslash(folderName));
        
        if ((File.separatorChar == '\\') && 
            (docRoot.charAt(0) == '*') &&
            relativePath.equals(File.separator))
        {
            // show Windows drive letters

            for (int i = 1; i <= 26; i++)
            {
                String driveLabel = WinDriveManager.getInstance().getDriveLabel(i);

                if (driveLabel != null)
                {
                    char driveChar = 'A';
                    
                    driveChar += (i - 1);

                    String subdirPath = driveChar + ":" + File.separator;
                    
                    Element subDirElem = doc.createElement("folder");
                    
                    subDirElem.setAttribute("drive", "true");
                    
                    foldersElem.appendChild(subDirElem);
                
                    subDirElem.setAttribute("name", this.insertDoubleBackslash(subdirPath));

                    String displayName = subdirPath;
                    
                    if (driveLabel.trim().length() > 0) {
                        displayName = displayName + " [" + CommonUtils.shortName(driveLabel, MAX_FILENAME_DISPLAY_LENGTH - 7) + "]"; 
                    }
                    
                    subDirElem.setAttribute("displayName", displayName);
                
                    subDirElem.setAttribute("path", UTF8URLEncoder.encode(relPathWithSlash + subdirPath));
                }
            }
            
            XmlUtil.setChildText(folderFileListElement, "cwdNotSelected", "true");
        }
        else
        {
            String fileList[] = dirFile.list();

            if (fileList != null)
            {
                ArrayList subFolders = new ArrayList();
                
                for (int i = 0; i < fileList.length; i++)
                {
                    String subDirName = fileList[i];
                    
                    if (!subDirName.equals(ThumbnailThread.THUMBNAIL_SUBDIR))
                    {
                        File tempFile = new File(currentPath, subDirName);

                        if (tempFile.isDirectory())
                        {
                            subFolders.add(subDirName);
                        }
                    }
                }

                if (subFolders.size() > 1)
                {
                    Collections.sort(subFolders, new StringComparator(StringComparator.SORT_IGNORE_CASE));
                }

                Iterator iter = subFolders.iterator();
                
                while (iter.hasNext())
                {
                    String subDirName = (String) iter.next();
                    
                    String shortDirName = subDirName;
                    
                    if (subDirName.length() > 18)
                    {
                        shortDirName = CommonUtils.shortName(subDirName, MAX_FILENAME_DISPLAY_LENGTH);
                    }
                    
                    Element subDirElem = doc.createElement("folder");
                
                    foldersElem.appendChild(subDirElem);
                
                    subDirElem.setAttribute("name", subDirName);

                    subDirElem.setAttribute("displayName", shortDirName);
                
                    subDirElem.setAttribute("path", UTF8URLEncoder.encode(relPathWithSlash + subDirName));
                }
            }
        }
        // end subdir section
		
		if (readonly)
		{
			XmlUtil.setChildText(folderFileListElement, "readonly", "true", false);
		}
		
        if (WebFileSys.getInstance().getMailHost() !=null)
        {
            XmlUtil.setChildText(folderFileListElement, "mailEnabled", "true");
        }
            
		XmlUtil.setChildText(folderFileListElement, "userid", uid, false);
		
	    XmlUtil.setChildText(folderFileListElement, "language", language, false);
		
		if ((!dirFile.exists()) || (!dirFile.isDirectory()) || (!dirFile.canRead()))
		{
		    Logger.getLogger(getClass()).error("directory not found or not readable: " + dirFile);
			processResponse("mobile/folderFileList.xsl");
			return; 
		}

        if ((File.separatorChar != '\\') ||
            (docRoot.charAt(0) != '*') ||
            (!relativePath.equals(File.separator)))
        {
            String description = metaInfMgr.getDescription(currentPath,".");

            if ((description != null) && (description.trim().length() > 0))
            {
                XmlUtil.setChildText(folderFileListElement, "description", description, true);
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
		
		XmlUtil.setChildText(folderFileListElement, "fileNumber", Integer.toString(fileNum), false);

        String menuPath = currentPath;
        
        if (File.separatorChar == '\\')
        {
        	menuPath = insertDoubleBackslash(currentPath.replace('/', '\\'));
        }

		XmlUtil.setChildText(folderFileListElement, "menuPath", menuPath, false);

		XmlUtil.setChildText(folderFileListElement, "pathForScript", insertDoubleBackslash(pathWithSlash), false);

		XmlUtil.setChildText(folderFileListElement, "filter", mask, false);

		XmlUtil.setChildText(folderFileListElement, "sortBy", Integer.toString(sortBy), false);

		Element fileListElement = doc.createElement("fileList");
			
		folderFileListElement.appendChild(fileListElement);

		if (selectedFiles != null)
		{
			SimpleDateFormat dateFormat=LanguageManager.getInstance().getDateFormat(language);

			// DecimalFormat numFormat=new DecimalFormat("0,000,000,000,000");
			DecimalFormat numFormat = new DecimalFormat("#,###,###,###,###");

	        IconManager iconMgr = null;

	        if (WebFileSys.getInstance().isShowAssignedIcons())
	        {
	            iconMgr = IconManager.getInstance();
	        }
			
			for (int i = 0; i < selectedFiles.size(); i++)
			{
				Element fileElement = doc.createElement("file");
                
				fileListElement.appendChild(fileElement);

				FileContainer fileCont = (FileContainer) selectedFiles.get(i);
				
				String fileName = fileCont.getName();

				fileElement.setAttribute("name", fileName);
                fileElement.setAttribute("nameForScript", escapeForJavascript(fileName));

				fileElement.setAttribute("id", Integer.toString(i));
				
                if (WebFileSys.getInstance().isShowAssignedIcons())
                {
                    String docImage = "doc.gif";

                    int extIdx = fileName.lastIndexOf('.');

                    if ((extIdx > 0) && (extIdx < (fileName.length() - 1)))
                    {
                        docImage = iconMgr.getAssignedIcon(fileName.substring(extIdx + 1));
                    }

                    fileElement.setAttribute("icon", docImage);
                }
				
				File tempFile = fileCont.getRealFile();

				if (fileCont.isLink())
				{
					fileElement.setAttribute("link" , "true");
					XmlUtil.setChildText(fileElement, "realPath", fileCont.getRealFile().getAbsolutePath(), false);
					XmlUtil.setChildText(fileElement, "realPathForScript", insertDoubleBackslash(fileCont.getRealFile().getAbsolutePath()), false);

					if (!this.accessAllowed(fileCont.getRealFile().getAbsolutePath()))
					{	
						fileElement.setAttribute("outsideDocRoot", "true");
					}
				}

				String description = null;

				if (fileCont.isLink())
				{
					description = metaInfMgr.getDescription(fileCont.getRealFile().getAbsolutePath());
				}
				else
				{
					if (dirHasMetaInf)
					{
						description = metaInfMgr.getDescription(pathNoSlash,fileName);
					}
				}

				if ((description!=null) && (description.trim().length()>0))
				{
					XmlUtil.setChildText(fileElement, "description", description, true);
				}

				String displayName = CommonUtils.shortName(fileName, MAX_FILENAME_DISPLAY_LENGTH);

				XmlUtil.setChildText(fileElement, "displayName", displayName);

				if (fileCont.isLink())
				{
					if (accessAllowed(fileCont.getRealFile().getAbsolutePath()))
					{
						fileElement.setAttribute("linkMenuPath", insertDoubleBackslash(fileCont.getRealFile().getAbsolutePath()));
					}
					else
					{	
						fileElement.setAttribute("outsideDocRoot", "true");
					}
				}

				fileElement.setAttribute("lastModified", dateFormat.format(new Date(tempFile.lastModified())));

				long kBytes = 0L; 

				long fileSize = tempFile.length();
            
				if (fileSize > 0L)
				{
					kBytes = fileSize / 1024L;
            	
					if (kBytes == 0L)
					{
						kBytes = 1; 
					}
				}

				fileElement.setAttribute("size", numFormat.format(kBytes));

				String fullFileName = tempFile.getAbsolutePath();
				
				if (File.pathSeparatorChar == '\\')
				{
					fullFileName = fullFileName.replace('\\', '/');
				}
			}
		}

        if (!readonly)
        {
            ClipBoard clipBoard = (ClipBoard) session.getAttribute("clipBoard");
            
            if ((clipBoard == null) || clipBoard.isEmpty())
            {
                XmlUtil.setChildText(folderFileListElement, "clipBoardEmpty", "true");
            }
            else
            {
                if (clipBoard.isCopyOperation())
                {
                    XmlUtil.setChildText(folderFileListElement, "copyOperation", "true");
                }
            }
        }
		
	    processResponse("mobile/folderFileList.xsl", false);

	    if (!"\\".equals(currentPath)) {
			FastPathManager.getInstance().queuePath(uid, currentPath);
	    }
	}
}
