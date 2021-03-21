package de.webfilesys.gui.xsl;

import java.io.File;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

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
import de.webfilesys.gui.user.SwitchFileAgeColoringHandler;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslFileListHandler extends XslFileListHandlerBase
{
	private static final long MILLISECONDS_HOUR = 60l * 60l * 1000;
	private static final long MILLISECONDS_DAY = MILLISECONDS_HOUR * 24l;
	private static final long MILLISECONDS_WEEK = MILLISECONDS_DAY * 7l;
	private static final long MILLISECONDS_MONTH = MILLISECONDS_DAY * 30l;
	private static final long MILLISECONDS_YEAR = MILLISECONDS_DAY * 365l;
	
	public XslFileListHandler(
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
		String actPath = getParameter("actpath");

		if ((actPath == null) || (actPath.length() == 0))
		{
			actPath = getCwd();
		}

		String docRoot = userMgr.getDocumentRoot(uid);

		if (!accessAllowed(actPath))
		{
			actPath = docRoot;
		}

		String mask = getParameter("mask");
		
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

		session.setAttribute(Constants.SESSION_KEY_CWD, actPath);

		IconManager iconMgr = null;

		if (WebFileSys.getInstance().isShowAssignedIcons())
		{
			iconMgr = IconManager.getInstance();
		}

		Element fileListElement = doc.createElement("fileList");
			
		doc.appendChild(fileListElement);
			
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

		if (WebFileSys.getInstance().isMaintananceMode())
		{
			if (!isAdminUser(false))
			{
				XmlUtil.setChildText(fileListElement, "maintananceMode", "true", false);
			}
		}

		File dirFile = new File(actPath);
		
		if ((!dirFile.exists()) || (!dirFile.isDirectory()) || (!dirFile.canRead()))
		{
		    Logger.getLogger(getClass()).warn("folder is not a readable directory: " + actPath);
			XmlUtil.setChildText(fileListElement, "dirNotFound", "true", false);
			processResponse("fileList.xsl");
			return; 
		}
 
		XmlUtil.setChildText(fileListElement, "dirModified", Long.toString(dirFile.lastModified()), false);
		
		String normalizedPath=null;

		String pathWithMask;
		String path_no_slash;

		if (actPath.endsWith(File.separator))
		{
			pathWithMask=actPath + mask;
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
			pathWithMask=actPath + File.separator + mask;
		}

		XmlUtil.setChildText(fileListElement, "headLine", getHeadlinePath(pathWithMask), false);

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

		ArrayList<FileContainer> selectedFiles = selectionStatus.getSelectedFiles();

		int fileNum = selectionStatus.getNumberOfFiles();
		
		XmlUtil.setChildText(fileListElement, "fileNumber", Integer.toString(fileNum), false);

		XmlUtil.setChildText(fileListElement, "currentPath", normalizedPath, false);

		XmlUtil.setChildText(fileListElement, "menuPath", insertDoubleBackslash(normalizedPath), false);

		XmlUtil.setChildText(fileListElement, "relativePath", insertDoubleBackslash(getHeadlinePath(normalizedPath)), false);

		XmlUtil.setChildText(fileListElement, "filter", mask, false);

		XmlUtil.setChildText(fileListElement, "sortBy", Integer.toString(sortBy), false);

		DecimalFormat numFormat = new DecimalFormat("#,###,###,###,###");
		
		long fileSizeSum = selectionStatus.getFileSizeSum();
		
		addFormattedSizeSum(fileSizeSum, fileListElement);		

		XmlUtil.setChildText(fileListElement, "sizeSumBytes", Long.toString(fileSizeSum), false);
		
        boolean linkFound = false;
		
		if (selectedFiles != null)
		{
			long now = System.currentTimeMillis();
			
			Boolean fileAgeColoringActive = (Boolean) session.getAttribute(SwitchFileAgeColoringHandler.SESSION_KEY_FILE_AGE_COLORING);
			
			if (fileAgeColoringActive != null) {
				XmlUtil.setChildText(fileListElement, "fileAgeColoring", "true", false);
			}
			
			SimpleDateFormat dateFormat = LanguageManager.getInstance().getDateFormat(language);

			for (FileContainer fileCont : selectedFiles) {
			
                Element fileElement = doc.createElement("file");
                
                fileListElement.appendChild(fileElement);
				
				String fileName = fileCont.getName();

                fileElement.setAttribute("name", fileName);
                fileElement.setAttribute("nameForScript", escapeForJavascript(fileName));

				File tempFile = fileCont.getRealFile();

				if (fileCont.isLink())
				{
                    fileElement.setAttribute("link" , "true");
                    XmlUtil.setChildText(fileElement, "realPath", fileCont.getRealFile().getAbsolutePath(), false);
                    XmlUtil.setChildText(fileElement, "linkPath", getHeadlinePath(fileCont.getRealFile().getAbsolutePath()), false);

                    linkFound = true;
				}

				String docImage = null;

				if (WebFileSys.getInstance().isShowAssignedIcons())
				{
					int extIdx = fileName.lastIndexOf('.');

					if ((extIdx > 0) && (extIdx < (fileName.length() - 1)))
					{
						docImage = iconMgr.getFileIconNoDefault(fileName);
					}
				}

				if (docImage != null) {
	                fileElement.setAttribute("icon", docImage);
				} else {
					String docImageIconFont = iconMgr.getFileIconFont(fileName); 
					if (docImageIconFont != null) {
		                fileElement.setAttribute("iconFont", docImageIconFont);
					}
				}

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
                
                if (browserManufacturer == BROWSER_MSIE) {
                	// prevent layout distortion by too long unbreakable filename
                	// word-wrap:break-word does not work in MSIE
                	
                    int nameLength = displayName.length();
                    if (nameLength > 40)
                    {
                    	StringTokenizer filenameParser = new StringTokenizer(displayName, " ");
                    	
                    	boolean tokenTooLong = false;
                    	while ((!tokenTooLong) && filenameParser.hasMoreTokens())
                    	{
                    		String token = filenameParser.nextToken();
                    		if (token.length() > 40)
                    		{
                    			tokenTooLong = true;
                    		}
                    	}
                    	
                    	if (tokenTooLong)
                    	{
                           	displayName = displayName.substring(0,35) + " " + displayName.substring(35);
                        	
                        	fileElement.setAttribute("displayName", displayName);
                    	}
                    }
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

				long lastModified = tempFile.lastModified();
				
				fileElement.setAttribute("lastModified", dateFormat.format(new Date(lastModified)));

				if (fileAgeColoringActive != null) {
					if (now - lastModified < MILLISECONDS_HOUR) {
						XmlUtil.setChildText(fileElement, "age", "hour");
					} else if (now - lastModified < MILLISECONDS_DAY) {
						XmlUtil.setChildText(fileElement, "age", "day");
					} else if (now - lastModified < MILLISECONDS_WEEK) {
						XmlUtil.setChildText(fileElement, "age", "week");
					} else if (now - lastModified < MILLISECONDS_MONTH) {
						XmlUtil.setChildText(fileElement, "age", "month");
					} else if (now - lastModified < MILLISECONDS_YEAR) {
						XmlUtil.setChildText(fileElement, "age", "year");
					} 
				}
				
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
				XmlUtil.setChildText(fileListElement, "linksExist", "true");
            }
		}

		int pollInterval = WebFileSys.getInstance().getPollFilesysChangesInterval();
		if (pollInterval > 0) {
			XmlUtil.setChildText(fileListElement, "pollInterval", Integer.toString(pollInterval));
		}

		if (WebFileSys.getInstance().getFfmpegExePath() != null) {
            XmlUtil.setChildText(fileListElement, "videoEnabled", "true");
		}
		
		addCurrentTrail(fileListElement, actPath, docRoot, mask);		
		
		processResponse("fileList.xsl");

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
