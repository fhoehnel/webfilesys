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

import de.webfilesys.ClipBoard;
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
	private static final int MOBILE_FILE_PAGE_SIZE = 8;
	
	private static final int MAX_FILENAME_DISPLAY_LENGTH = 28;
	
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
                    session.setAttribute("cwd", absPath);
                }
            }
            
            String cwd = (String) session.getAttribute("cwd");
            
            if (cwd != null) 
            {
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

		session.setAttribute("cwd", currentPath);

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
                    
                    foldersElem.appendChild(subDirElem);
                
                    subDirElem.setAttribute("name", this.insertDoubleBackslash(subdirPath));

                    String displayName = subdirPath;
                    
                    if (driveLabel.trim().length() > 0) {
                        displayName = displayName + " [" + CommonUtils.shortName(driveLabel, 15) + "]"; 
                    }
                    
                    subDirElem.setAttribute("displayName", displayName);
                
                    subDirElem.setAttribute("path", UTF8URLEncoder.encode(relPathWithSlash + subdirPath));
                }
            }
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
                        shortDirName = CommonUtils.shortName(subDirName, 18);
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

        addMsgResource("label.mobileWindowTitle", getResource("label.mobileWindowTitle","WebFileSys mobile version"));
        addMsgResource("label.about", getResource("label.about","About WebFileSys"));
        addMsgResource("label.logout", getResource("label.logout","Logout"));
        addMsgResource("label.bookmarksMobile", getResource("label.bookmarksMobile","bookmarks"));
        addMsgResource("classicView", getResource("classicView", "classic/desktop view"));
        addMsgResource("label.selectFunction", getResource("label.selectFunction", "- select function -"));
        addMsgResource("folderIsEmpty", getResource("folderIsEmpty","no files or subfolders in this directory"));
        addMsgResource("alert.nofileselected", getResource("alert.nofileselected","Select at least 1 file"));
		addMsgResource("sort.name", getResource("sort.name","sort by name"));
		addMsgResource("sort.extension", getResource("sort.extension","sort by extension"));
		addMsgResource("sort.size", getResource("sort.size","sort by size"));
		addMsgResource("sort.date", getResource("sort.date","sort by change date"));

        addMsgResource("label.modelist", getResource("label.modelist","file list"));
        addMsgResource("label.modethumb", getResource("label.modethumb","thumbnails"));
		addMsgResource("label.modestory", getResource("label.modestory","picture story"));
		addMsgResource("label.modeSlideshow", getResource("label.modeSlideshow","slideshow"));

		addMsgResource("label.mask", getResource("label.mask","Mask"));
		addMsgResource("label.listPageSize", getResource("label.listPageSize","files per page"));
		addMsgResource("label.page", getResource("label.page","page"));

		addMsgResource("checkbox.selectall", getResource("checkbox.selectall","Select all"));
		addMsgResource("label.selectedFiles", getResource("label.selectedFiles","- selected files -"));

		addMsgResource("label.comments", getResource("label.comments","Comments"));

		// addMsgResource("label.selectFunction", getResource("label.selectFunction","- select function -"));

		if (!readonly)
		{
            addMsgResource("checkbox.confirmdel", getResource("checkbox.confirmdel","Confirm Delete"));
	        addMsgResource("button.delete", getResource("button.delete","Delete"));
			addMsgResource("label.copyToClip", getResource("label.copyToClip","Copy to clipboard"));
			addMsgResource("label.cutToClip", getResource("label.cutToClip","Move to clipboard"));
			addMsgResource("button.zip", getResource("button.zip","Create ZIP archive"));
			addMsgResource("confirm.deleteFiles", getResource("confirm.deleteFiles", "Delete selected files?"));
		}
		addMsgResource("button.downloadAsZip", getResource("button.downloadAsZip","Download as Zip"));
		
        if (!readonly)
        {
            addMsgResource("button.bookmark", getResource("button.bookmark", "Bookmark"));
            addMsgResource("title.bookmarkButton", getResource("title.bookmarkButton", "Create bookmark for the current folder"));
        }

        addMsgResource("button.upload", getResource("button.upload","Upload"));
        addMsgResource("button.paste", getResource("button.paste","Paste"));
        addMsgResource("button.pasteLink", getResource("button.pasteLink","Paste as Link"));
		
		if (readonly)
		{
			XmlUtil.setChildText(folderFileListElement, "readonly", "true", false);
		}
		
        if (WebFileSys.getInstance().getMailHost() !=null)
        {
            XmlUtil.setChildText(folderFileListElement, "mailEnabled", "true");
        }
            
		if (WebFileSys.getInstance().isMaintananceMode())
		{
			if (!isAdminUser(false))
			{
				addMsgResource("alert.maintanance", getResource("alert.maintanance","The server has been switched to maintanance mode. Please logout!"));
			}
		}

		XmlUtil.setChildText(folderFileListElement, "userid", uid, false);
		
	    XmlUtil.setChildText(folderFileListElement, "language", language, false);
		
		if ((!dirFile.exists()) || (!dirFile.isDirectory()) || (!dirFile.canRead()))
		{
		    Logger.getLogger(getClass()).error("directory not found or not readable: " + dirFile);
			addMsgResource("alert.dirNotFound", getResource("alert.dirNotFound","The folder is not a readable directory"));
			this.processResponse("mobile/folderFileList.xsl");
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

		int pageSize = MOBILE_FILE_PAGE_SIZE;
		
		temp = getParameter("pageSize");
		if ((temp != null) && (temp.trim().length() > 0))
		{
			try
			{
				pageSize = Integer.parseInt(temp);

				Integer listPageSize = (Integer) session.getAttribute("listPageSize");
				
				if ((listPageSize == null) || (listPageSize.intValue() != pageSize))
				{
					session.setAttribute("listPageSize", new Integer(pageSize));
				}
			}
			catch (NumberFormatException nfex)
			{
			}
		}
		else
		{
			Integer listPageSize = (Integer) session.getAttribute("listPageSize");
			
			if ((listPageSize == null) || (listPageSize.intValue() == 0))
			{
				pageSize = WebFileSys.getInstance().getThumbnailsPerPage();
			}
			else
			{
                pageSize = listPageSize.intValue();				
			}
		}

        Vector selectedFiles = null;

        FileSelectionStatus selectionStatus = null;

        if (pathNoSlash.length() > 0) {
            FileLinkSelector fileSelector = new FileLinkSelector(currentPath, sortBy, true);

            selectionStatus = fileSelector.selectFiles(fileFilter, -1, pageSize, startIdx);

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

		if (fileNum > 0)
		{
			Element pagingElement = doc.createElement("paging");
        
			folderFileListElement.appendChild(pagingElement);
        
			XmlUtil.setChildText(pagingElement, "pageSize", Integer.toString(pageSize));

			XmlUtil.setChildText(pagingElement, "firstOnPage" , Integer.toString(selectionStatus.getBeginIndex()+1), false);
			XmlUtil.setChildText(pagingElement, "lastOnPage" , Integer.toString(selectionStatus.getEndIndex()+1), false);

			if (selectionStatus.getBeginIndex() > 0)
			{
				XmlUtil.setChildText(pagingElement, "prevStartIdx" , Integer.toString(selectionStatus.getBeginIndex() - pageSize), false);
			}
			
			if (!selectionStatus.getIsLastPage())
			{
				XmlUtil.setChildText(pagingElement, "nextStartIdx" , Integer.toString(selectionStatus.getBeginIndex() + pageSize), false);
				XmlUtil.setChildText(pagingElement, "lastStartIdx" , Integer.toString(selectionStatus.getLastPageStartIdx()), false);
			}

			XmlUtil.setChildText(pagingElement, "currentPage", Integer.toString(selectionStatus.getCurrentPage() + 1), false);

			if (fileNum > pageSize)
			{
				int numPages = fileNum / pageSize;
                
				int pageStep = numPages / 5;
                
				if (pageStep == 0)
				{
					pageStep = 1;
				}
            
				int currentPage = selectionStatus.getCurrentPage();
            
				boolean currentPrinted = false;
            
				Vector pageStartIndices = selectionStatus.getPageStartIndices();
            
				for (int pageCounter = 0; pageCounter < pageStartIndices.size(); pageCounter += pageStep)
				{
					if (pageCounter == currentPage)
					{
						Element pageElement = doc.createElement("page");
						pagingElement.appendChild(pageElement);
						pageElement.setAttribute("num", Integer.toString(pageCounter + 1));

						currentPrinted = true;
					}
					else
					{
						if (!currentPrinted)
						{
							if (pageCounter > currentPage)
							{
								Element pageElement = doc.createElement("page");
								pagingElement.appendChild(pageElement);
								pageElement.setAttribute("num", Integer.toString(currentPage + 1));

								currentPrinted = true;
							}
						}
						
						Integer pageStartIdx = (Integer) pageStartIndices.elementAt(pageCounter);
						
						Element pageElement = doc.createElement("page");
						pagingElement.appendChild(pageElement);
						pageElement.setAttribute("num", Integer.toString(pageCounter + 1));
						pageElement.setAttribute("startIdx", Integer.toString(pageStartIdx.intValue()));
					}
				}
			}
		}

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
			
            int i = 0;

			for (i = 0; i < selectedFiles.size(); i++)
			{
				Element fileElement = doc.createElement("file");
                
				fileListElement.appendChild(fileElement);

				FileContainer fileCont = (FileContainer) selectedFiles.elementAt(i);
				
				String actFilename = fileCont.getName();

				fileElement.setAttribute("name", actFilename);

				fileElement.setAttribute("id", Integer.toString(i));
				
                if (WebFileSys.getInstance().isShowAssignedIcons())
                {
                    String docImage = "doc.gif";

                    int extIdx = actFilename.lastIndexOf('.');

                    if ((extIdx > 0) && (extIdx < (actFilename.length() - 1)))
                    {
                        docImage = iconMgr.getAssignedIcon(actFilename.substring(extIdx + 1));
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
						description = metaInfMgr.getDescription(pathNoSlash,actFilename);
					}
				}

				if ((description!=null) && (description.trim().length()>0))
				{
					XmlUtil.setChildText(fileElement, "description", description, true);
				}

				String displayName = CommonUtils.shortName(actFilename, MAX_FILENAME_DISPLAY_LENGTH);

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

		FastPathManager.getInstance().queuePath(uid, currentPath);
	}
}
