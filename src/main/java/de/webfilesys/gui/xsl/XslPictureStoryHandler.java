package de.webfilesys.gui.xsl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.Constants;
import de.webfilesys.FastPathManager;
import de.webfilesys.FileComparator;
import de.webfilesys.FileContainer;
import de.webfilesys.FileLinkSelector;
import de.webfilesys.FileSelectionStatus;
import de.webfilesys.MetaInfManager;
import de.webfilesys.WebFileSys;
import de.webfilesys.graphics.ScaledImage;
import de.webfilesys.util.UTF8URLEncoder;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslPictureStoryHandler extends XslRequestHandlerBase
{
	public XslPictureStoryHandler(
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
		session.setAttribute("viewMode", new Integer(Constants.VIEW_MODE_STORY));

		MetaInfManager metaInfMgr = MetaInfManager.getInstance();

		String mode = "story";
		String modeParam = getParameter("mode");
		if ((modeParam != null) && (modeParam.length() > 0)) {
			mode = modeParam;
		}

		String act_path = null;
		
		if (mode.equals("pictureBook")) {
	        String relativePath = getParameter("relPath");
	        
	        if ((relativePath != null) && (relativePath.trim().length() > 0))
	        {
				String docRoot = userMgr.getDocumentRoot(uid);
				
				String docRootOS = docRoot;
				
				if (File.separatorChar == '\\')
				{
					docRootOS = docRoot.replace('/', File.separatorChar);
				}

	        	if (relativePath.equals(File.separator))
	        	{
	        		act_path = docRoot;
	        		
					if (File.separatorChar == '\\')
					{
						act_path = act_path.replace('/', File.separatorChar);
					}
	        	}
	        	else
	        	{
	        		if (relativePath.startsWith(File.separator))
	        		{
						act_path = docRootOS + relativePath;
	        		}
	        		else
	        		{
						act_path = docRootOS + File.separator + relativePath;
	        		}
	        	}
	        }
		}
	        
	    if (act_path == null)
	    {
			act_path = getParameter("actPath");
			if ((act_path == null) || (act_path.length() == 0))
			{
				act_path = getParameter("actpath");
				if ((act_path == null) || (act_path.length() == 0))
				{
					act_path = getCwd();
					
					if (act_path == null) {
						act_path = WebFileSys.getInstance().getUserMgr().getDocumentRoot(uid);
					}
				}
			}
		}
		
		session.setAttribute(Constants.SESSION_KEY_CWD, act_path);

		String fileFilter[] = Constants.imgFileMasks;
		
        String path_no_slash = null;
        String pathWithSlash = null;

		if (act_path.endsWith(File.separator))
		{
			path_no_slash=act_path.substring(0,act_path.length()-1);
			pathWithSlash = act_path;
		}
		else
		{
			path_no_slash = act_path;
			pathWithSlash = act_path + File.separator;
		}

		boolean dirHasMetaInf = metaInfMgr.dirHasMetaInf(act_path);
		
		String screenWidthParm = getParameter("screenWidth");
		String screenHeightParm = getParameter("screenHeight");

		if (screenWidthParm!=null)
		{
			try
			{
				int newScreenWidth = Integer.parseInt(screenWidthParm);

				session.setAttribute("screenWidth", new Integer(newScreenWidth));
			}
			catch (NumberFormatException nfex)
			{
			}
		}

		if (screenHeightParm != null)
		{
			try
			{
				int newScreenHeight = Integer.parseInt(screenHeightParm);

				session.setAttribute("screenHeight", new Integer(newScreenHeight));
			}
			catch (NumberFormatException nfex)
			{
			}
		}

		int screenWidth = Constants.DEFAULT_SCREEN_WIDTH;
		int screenHeight = Constants.DEFAULT_SCREEN_HEIGHT;

		Integer widthScreen = (Integer) session.getAttribute("screenWidth");
		
		if (widthScreen != null)
		{
			screenWidth = widthScreen.intValue();
		}

		Integer heightScreen = (Integer) session.getAttribute("screenHeight");
		
		if (heightScreen != null)
		{
			screenHeight = heightScreen.intValue();
		}
		
		int thumbnailSize = 400;

		int startIdx = (-1);

		String initial = getParameter("initial");
        
		if ((initial != null) && (initial.equalsIgnoreCase("true")))
		{
			session.removeAttribute("startIdx");
		}

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

		String stylesheetName = "story.xsl";
		if (mode.equals("pictureBook")) {
			stylesheetName = "album/pictureBook.xsl";
		}
		
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/" + stylesheetName + "\"");

		Element fileListElement = doc.createElement("fileList");
			
		doc.appendChild(fileListElement);
			
		doc.insertBefore(xslRef, fileListElement);

		if (WebFileSys.getInstance().isMaintananceMode())
		{
			if (!isAdminUser(false))
			{
				XmlUtil.setChildText(fileListElement, "maintananceMode", "true", false);
			}
		}

		XmlUtil.setChildText(fileListElement, "css", userMgr.getCSS(uid), false);
		
	    XmlUtil.setChildText(fileListElement, "language", language, false);
		
		File dirFile = new File(act_path);
		
		if ((!dirFile.exists()) || (!dirFile.isDirectory()) || (!dirFile.canRead()))
		{
			XmlUtil.setChildText(fileListElement, "dirNotFound", "true", false);
			processResponse("xsl/folderTree.xsl");
			return; 
		}

		String description = metaInfMgr.getDescription(act_path,".");

		if ((description!=null) && (description.trim().length()>0))
		{
			XmlUtil.setChildText(fileListElement, "description", description, true);

			XmlUtil.setChildText(fileListElement, "headLine", description, true);
		}
        else
        {
			XmlUtil.setChildText(fileListElement, "headLine", getHeadlinePath(act_path), false);
        }

        Element currentPathElem = doc.createElement("currentPath");
		
		fileListElement.appendChild(currentPathElem);
		
		XmlUtil.setElementText(currentPathElem, act_path, false);

		String role = userMgr.getRole(uid);
        
        if ((role != null) && role.equals("album"))
        {
			addAlbumPath(act_path, fileListElement, currentPathElem);
			
			XmlUtil.setChildText(fileListElement, "role", role);
        }

        if (readonly)
        {
			XmlUtil.setChildText(fileListElement, "readonly", "true");
        }

		int pageSize = WebFileSys.getInstance().getThumbnailsPerPage();

		String temp = getParameter("pageSize");
		
		if ((temp!=null) && (temp.trim().length() > 0))
		{
			try
			{
				pageSize = Integer.parseInt(temp);

				Integer sessionThumbPageSize = (Integer) session.getAttribute("thumbPageSize");
				
				if ((sessionThumbPageSize == null) || (sessionThumbPageSize.intValue() != pageSize))
				{
					session.setAttribute("thumbPageSize", new Integer(pageSize));

					if (!readonly)
					{
						userMgr.setPageSize(uid,pageSize);
					}
					
					startIdx = 0;

					session.setAttribute("startIdx", new Integer(0));
				}
			}
			catch (NumberFormatException nfex)
			{
			}
		}
		else
		{
			Integer sessionThumbPageSize = (Integer) session.getAttribute("thumbPageSize");
			
			if ((sessionThumbPageSize != null) && (sessionThumbPageSize.intValue() != 0))
			{
				pageSize = sessionThumbPageSize.intValue();
			}
		}

		FileLinkSelector fileSelector = new FileLinkSelector(act_path, FileComparator.SORT_BY_FILENAME, true);

		FileSelectionStatus selectionStatus = fileSelector.selectFiles(fileFilter, pageSize, startIdx);

		ArrayList<FileContainer> selectedFiles = selectionStatus.getSelectedFiles();

		int fileNum = 0;

		if (selectedFiles != null)
		{
			fileNum=selectionStatus.getNumberOfFiles();
		}
		
		XmlUtil.setChildText(fileListElement, "fileNumber", Integer.toString(fileNum), false);

		XmlUtil.setChildText(fileListElement, "pathForScript", insertDoubleBackslash(pathWithSlash), false);


		if (fileNum > 0)
		{
			Element pagingElement = doc.createElement("paging");
        
			fileListElement.appendChild(pagingElement);
        
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
                
				int pageStep = (int) Math.round(Math.ceil(((float) numPages) / 9));
                
				if (pageStep == 0)
				{
					pageStep = 1;
				}
            
				int currentPage = selectionStatus.getCurrentPage();
            
				boolean currentPrinted = false;
            
				ArrayList<Integer> pageStartIndices = selectionStatus.getPageStartIndices();
            
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
						
						Integer pageStartIdx = (Integer) pageStartIndices.get(pageCounter);
						
						Element pageElement = doc.createElement("page");
						pagingElement.appendChild(pageElement);
						pageElement.setAttribute("num", Integer.toString(pageCounter + 1));
						pageElement.setAttribute("startIdx", Integer.toString(pageStartIdx.intValue()));
					}
				}
			}
		}

		if (selectedFiles != null)
		{
			// boolean metaInfFileIncluded=false;

			for (int i = 0; i < selectedFiles.size(); i++)
			{
				Element fileElement = doc.createElement("file");
                
				fileListElement.appendChild(fileElement);

				FileContainer fileCont = (FileContainer) selectedFiles.get(i);
				
				String actFilename = fileCont.getName();

				fileElement.setAttribute("name", actFilename);

				fileElement.setAttribute("id", Integer.toString(i));
				
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

				description = null;

				if (fileCont.isLink())
				{
					description = metaInfMgr.getDescription(fileCont.getRealFile().getAbsolutePath());
				}
				else
				{
					if (dirHasMetaInf)
					{
						description = metaInfMgr.getDescription(path_no_slash,actFilename);
					}
				}

				if ((description!=null) && (description.trim().length()>0))
				{
					XmlUtil.setChildText(fileElement, "description", description, true);
				}

				String realPath = tempFile.getParent();
                
				String realFileName = tempFile.getName();

				int commentCount = metaInfMgr.countComments(realPath, realFileName);

				XmlUtil.setChildText(fileElement, "comments", Integer.toString(commentCount));
				
				String fullFileName = tempFile.getAbsolutePath();

				boolean imgFound=true;

				ScaledImage scaledImage=null;

				try
				{
					scaledImage = new ScaledImage(fullFileName, screenWidth-100, screenHeight-135);
				}
				catch (IOException io1)
				{
	            	Logger.getLogger(getClass()).error("failed to get scaled image dimensions", io1);
					imgFound=false;                 
				}
				
				if (imgFound)
				{
					XmlUtil.setChildText(fileElement, "imgType", Integer.toString(scaledImage.getImageType()));
					XmlUtil.setChildText(fileElement, "xpix", Integer.toString(scaledImage.getRealWidth()));
					XmlUtil.setChildText(fileElement, "ypix", Integer.toString(scaledImage.getRealHeight()));
             
					int thumbWidth = 0;
					int thumbHeight = 0;

                    if ((scaledImage.getRealWidth() <= thumbnailSize) &&
                        (scaledImage.getRealHeight() <= thumbnailSize))
                    {
						thumbHeight = scaledImage.getRealHeight();
						thumbWidth = scaledImage.getRealWidth();
                    }
                    else
                    {
						if (scaledImage.getRealHeight() > scaledImage.getRealWidth())
						{
							thumbHeight = thumbnailSize;
							thumbWidth = scaledImage.getRealWidth() * thumbnailSize / scaledImage.getRealHeight();
						}
						else
						{
							thumbWidth = thumbnailSize;
							thumbHeight = scaledImage.getRealHeight() * thumbnailSize / scaledImage.getRealWidth();
						}
                    }
					
					XmlUtil.setChildText(fileElement, "thumbnailWidth", Integer.toString(thumbWidth));
					XmlUtil.setChildText(fileElement, "thumbnailHeight", Integer.toString(thumbHeight));

					int fullScreenWidth = scaledImage.getScaledWidth() + 20;
					
					if (fullScreenWidth < 600)
					{
						fullScreenWidth = 600;
					}
					
					XmlUtil.setChildText(fileElement, "fullScreenWidth", Integer.toString(fullScreenWidth));
					XmlUtil.setChildText(fileElement, "fullScreenHeight", Integer.toString(scaledImage.getScaledHeight()));
					
					String srcFileName = "/webfilesys/servlet?command=getFile&filePath=" + UTF8URLEncoder.encode(fullFileName) + "&cached=true";
					
					XmlUtil.setChildText(fileElement, "imgPath", srcFileName);
				}
			}
		}

        if (WebFileSys.getInstance().getFfmpegExePath() != null) {
            XmlUtil.setChildText(fileListElement, "videoEnabled", "true");
        }
		
		processResponse(stylesheetName, false);

		FastPathManager.getInstance().queuePath(uid,act_path);
	}
}
