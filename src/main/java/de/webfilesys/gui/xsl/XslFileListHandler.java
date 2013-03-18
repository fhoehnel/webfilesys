package de.webfilesys.gui.xsl;

import java.io.File;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.ClipBoard;
import de.webfilesys.Constants;
import de.webfilesys.FastPathManager;
import de.webfilesys.FileComparator;
import de.webfilesys.FileContainer;
import de.webfilesys.FileLinkSelector;
import de.webfilesys.FileSelectionStatus;
import de.webfilesys.IconManager;
import de.webfilesys.LanguageManager;
import de.webfilesys.MP3ExtractorThread;
import de.webfilesys.MetaInfManager;
import de.webfilesys.WebFileSys;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslFileListHandler extends XslFileListHandlerBase
{
	protected boolean initial = false;
	
	public XslFileListHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid,
            boolean initial)
	{
        super(req, resp, session, output, uid);
        
        this.initial = initial;
	}
	  
	protected void process()
	{
		String path_mask;
		String path_no_slash;
		int i;
		File tempFile;
		int fileNum;

		String actPath = getParameter("actpath");

		if ((actPath == null) || (actPath.length() == 0))
		{
			actPath = (String) session.getAttribute("cwd");
		}

		String docRoot = userMgr.getDocumentRoot(uid);

		if (!accessAllowed(actPath))
		{
			actPath = docRoot;
		}

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
		
		if ((mask!=null) && (mask.length()>0))
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
		
		if ((mask == null) || (mask.length() == 0))
		{
			mask = "*";
		}

		int sortBy = FileComparator.SORT_BY_FILENAME;

		String temp=getParameter("sortBy");
		if ((temp!=null) && (temp.length()>0))
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
				if (sortBy > 5)
				{
				    sortBy = FileComparator.SORT_BY_FILENAME;
				}
			}
			else
			{
				sortBy = FileComparator.SORT_BY_FILENAME;
			}
		}

		session.setAttribute("cwd", actPath);

		IconManager iconMgr = null;

		if (WebFileSys.getInstance().isShowAssignedIcons())
		{
			iconMgr = IconManager.getInstance();
		}

		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/fileList.xsl\"");

		Element fileListElement = doc.createElement("fileList");
			
		doc.appendChild(fileListElement);
			
		doc.insertBefore(xslRef, fileListElement);

		addMsgResource("alert.nofileselected", getResource("alert.nofileselected","Select at least 1 file"));
		addMsgResource("sort.name.ignorecase", getResource("sort.name.ignorecase","sort by name (ignore case)"));
		addMsgResource("sort.name.respectcase", getResource("sort.name.respectcase","sort by name (respect case)"));
		addMsgResource("sort.extension", getResource("sort.extension","sort by extension"));
		addMsgResource("sort.size", getResource("sort.size","sort by size"));
		addMsgResource("sort.date", getResource("sort.date","sort by change date"));

		addMsgResource("label.modelist", getResource("label.modelist","list files"));
		addMsgResource("label.modethumb", getResource("label.modethumb","thumbnails"));
		addMsgResource("label.modestory", getResource("label.modestory","picture story"));
		addMsgResource("label.modeSlideshow", getResource("label.modeSlideshow","slideshow"));
		addMsgResource("label.fileStats", getResource("label.fileStats","statistics"));

		addMsgResource("label.mask", getResource("label.mask","Mask"));
		addMsgResource("label.refresh", getResource("label.refresh","Refresh"));
		addMsgResource("label.files", getResource("label.files","files"));

		addMsgResource("label.filename", getResource("label.filename","file name"));
		addMsgResource("label.lastModified", getResource("label.lastModified","last modified"));
		addMsgResource("label.fileSize", getResource("label.fileSize","size (bytes)"));

		addMsgResource("checkbox.confirmdel", getResource("checkbox.confirmdel","Confirm Delete"));
		addMsgResource("label.selectedFiles", getResource("label.selectedFiles","selected files"));

		addMsgResource("label.selectFunction", getResource("label.selectFunction","- select function -"));

		if (!readonly)
		{
			addMsgResource("button.delete", getResource("button.delete","Delete"));
			addMsgResource("label.copyToClip", getResource("label.copyToClip","Copy to clipboard"));
			addMsgResource("label.cutToClip", getResource("label.cutToClip","Move to clipboard"));
			addMsgResource("button.zip", getResource("button.zip","Create ZIP archive"));
            if (File.separatorChar == '/')
			{
				addMsgResource("button.tar", getResource("button.tar","Create tar archive"));
			}
			addMsgResource("button.bookmark", getResource("button.bookmark", "Bookmark"));
			addMsgResource("title.bookmarkButton", getResource("title.bookmarkButton", "Create bookmark for the current folder"));
		}
		addMsgResource("button.downloadAsZip", getResource("button.downloadAsZip","Download as Zip"));
        addMsgResource("action.diff", getResource("action.diff","Compare (diff)"));
        addMsgResource("selectTwoFilesForDiff", getResource("selectTwoFilesForDiff","Select two files to compare!"));

		addMsgResource("button.upload", getResource("button.upload","Upload"));
		addMsgResource("button.paste", getResource("button.paste","Paste"));
		addMsgResource("button.pasteLink", getResource("button.pasteLink","Paste as Link"));

		String errorMsg = getParameter("errorMsg");
		
		if (errorMsg != null)
		{
		    XmlUtil.setChildText(fileListElement, "errorMsg", errorMsg, false);
		}
		
        if (readonly)
        {
        	XmlUtil.setChildText(fileListElement, "readonly", "true", false);
        }
        
		if (this.isBrowserXslEnabled())
		{
			Element xslEnabledElement = doc.createElement("browserXslEnabled");
			
			XmlUtil.setElementText(xslEnabledElement, "true", false);
			
			fileListElement.appendChild(xslEnabledElement);
		}

		if (!WebFileSys.getInstance().isLicensed())
		{
			requestCounter++;

			if (requestCounter % LIC_REMINDER_INTERVAL == 0)
			{
				XmlUtil.setChildText(fileListElement, "unlicensed", "true", false);
			}
		}

		if (WebFileSys.getInstance().isMaintananceMode())
		{
			if (!isAdminUser(false))
			{
				addMsgResource("alert.maintanance", getResource("alert.maintanance","The server has been switched to maintanance mode. Please logout!"));
			}
		}

		XmlUtil.setChildText(fileListElement, "css", userMgr.getCSS(uid), false);
		
		File dirFile = new File(actPath);
		
		if ((!dirFile.exists()) || (!dirFile.isDirectory()) || (!dirFile.canRead()))
		{
		    Logger.getLogger(getClass()).warn("folder is not a readable directory: " + actPath);
			addMsgResource("alert.dirNotFound", getResource("alert.dirNotFound","The folder is not a readable directory"));
			this.processResponse("xsl/folderTree.xsl");
			return; 
		}
 
		String normalizedPath=null;

		if (actPath.endsWith(File.separator))
		{
			path_mask=actPath + mask;
			if ((File.separatorChar=='\\') && (actPath.length()==3))
			{
				normalizedPath=actPath;
			}
			else
			{
				if (actPath.length()==1)   // the root
				{
					normalizedPath=actPath;
				}
				else
				{
					normalizedPath=actPath.substring(0,actPath.length()-1);
				}
			}
			path_no_slash=actPath.substring(0,actPath.length()-1);
		}
		else
		{
			path_no_slash=actPath;
			normalizedPath=actPath;
			path_mask=actPath + File.separator + mask;
		}

		XmlUtil.setChildText(fileListElement, "headLine", getHeadlinePath(path_mask), false);

		MetaInfManager metaInfMgr=MetaInfManager.getInstance();

		boolean dirHasMetaInf = metaInfMgr.dirHasMetaInf(path_no_slash);

		String description=null;

		if (dirHasMetaInf)
		{
			description = metaInfMgr.getDescription(path_no_slash,".");

			if ((description!=null) && (description.trim().length()>0))
			{
				XmlUtil.setChildText(fileListElement, "description", description, true);
			}
		}
		
		String fileMasks[] = new String[1];
		fileMasks[0]=mask;

		FileLinkSelector fileSelector = new FileLinkSelector(actPath,sortBy,true);

		FileSelectionStatus selectionStatus = fileSelector.selectFiles(fileMasks, Constants.MAX_FILE_NUM, 0);

		Vector selectedFiles = selectionStatus.getSelectedFiles();

		fileNum = selectionStatus.getNumberOfFiles();

		XmlUtil.setChildText(fileListElement, "fileNumber", Integer.toString(fileNum), false);

		XmlUtil.setChildText(fileListElement, "currentPath", normalizedPath, false);

		XmlUtil.setChildText(fileListElement, "menuPath", insertDoubleBackslash(normalizedPath), false);

		XmlUtil.setChildText(fileListElement, "filter", mask, false);

		XmlUtil.setChildText(fileListElement, "sortBy", Integer.toString(sortBy), false);

        boolean linkFound = false;
		
		if (selectedFiles != null)
		{
			SimpleDateFormat dateFormat=LanguageManager.getInstance().getDateFormat(language);

			// DecimalFormat numFormat=new DecimalFormat("0,000,000,000,000");
			DecimalFormat numFormat=new DecimalFormat("#,###,###,###,###");

			for (i = 0; i < selectedFiles.size(); i++)
			{
                Element fileElement = doc.createElement("file");
                
                fileListElement.appendChild(fileElement);

				FileContainer fileCont = (FileContainer) selectedFiles.elementAt(i);
				
				String fileName = fileCont.getName().replace('\'', '`');

                fileElement.setAttribute("name", fileName);

				tempFile = fileCont.getRealFile();

				if (fileCont.isLink())
				{
                    fileElement.setAttribute("link" , "true");
                    XmlUtil.setChildText(fileElement, "realPath", fileCont.getRealFile().getAbsolutePath(), false);
                    XmlUtil.setChildText(fileElement, "linkPath", getHeadlinePath(fileCont.getRealFile().getAbsolutePath()), false);

                    linkFound = true;
				}

				String docImage="doc.gif";

				if (WebFileSys.getInstance().isShowAssignedIcons())
				{
					int extIdx = fileName.lastIndexOf('.');

					if ((extIdx > 0) && (extIdx < (fileName.length() - 1)))
					{
						docImage=iconMgr.getAssignedIcon(fileName.substring(extIdx + 1));
					}
				}

                fileElement.setAttribute("icon", docImage);

                description = null;

				if (fileCont.isLink())
				{
					description = metaInfMgr.getDescription(fileCont.getRealFile().getAbsolutePath());
				}
				else
				{
					if (dirHasMetaInf)
					{
						description = metaInfMgr.getDescription(path_no_slash,fileName);
					}
				}

                String displayName = fileName;
                
                int nameLength = displayName.length();
                
                if (nameLength > 40)
                {
                	displayName = displayName.substring(0,35) + " " + displayName.substring(35);
                	
                	fileElement.setAttribute("displayName", displayName);
                }

				if (fileCont.isLink())
				{
					if (this.accessAllowed(fileCont.getRealFile().getAbsolutePath()))
					{
						fileElement.setAttribute("linkMenuPath", insertDoubleBackslash(fileCont.getRealFile().getAbsolutePath()));
					}
					else
					{	
						fileElement.setAttribute("outsideDocRoot", "true");
					}
				}

				fileElement.setAttribute("lastModified", dateFormat.format(new Date(tempFile.lastModified())));

				fileElement.setAttribute("size", numFormat.format(tempFile.length()));

				if (fileCont.isLink() || (dirHasMetaInf))
				{
					if (WebFileSys.getInstance().isShowDescriptionsInline())
					{
						if ((description!=null) && (description.trim().length()>0))
						{
							String shortDesc=description;

							if (description.length() > 90)
							{
								shortDesc=description.substring(0,90) + "...";
							}

                            XmlUtil.setChildText(fileElement, "description", shortDesc, true);
						}
					}
				}
			}
		}

		if (!readonly)
		{
			ClipBoard clipBoard = (ClipBoard) session.getAttribute("clipBoard");
			
			if ((clipBoard == null) || clipBoard.isEmpty())
            {
				XmlUtil.setChildText(fileListElement, "clipBoardEmpty", "true");
            }
            else
			{
				if (clipBoard.isCopyOperation())
				{
					XmlUtil.setChildText(fileListElement, "copyOperation", "true");
				}
			}
			
            if (linkFound)
            {
                addMsgResource("button.copyLinks", getResource("button.copyLinks", "Copy linked Files"));
                addMsgResource("tooltip.copyLinks", getResource("tooltip.copyLinks", "replace file links by a copy of the original files"));
                addMsgResource("confirm.copyLinks", getResource("confirm.copyLinks", "Replace all file links by a copy of the original files?"));
            }
		}

		addCurrentTrail(fileListElement, actPath, docRoot, mask);		
		
		processResponse("fileList.xsl", true);

		FastPathManager.getInstance().queuePath(uid,actPath);

		session.setAttribute("viewMode", new Integer(Constants.VIEW_MODE_LIST));

		if (!readonly)
		{
			if (WebFileSys.getInstance().isAutoExtractMP3())
			{
				(new MP3ExtractorThread(actPath)).start();
			}
		}
	}
	
}
