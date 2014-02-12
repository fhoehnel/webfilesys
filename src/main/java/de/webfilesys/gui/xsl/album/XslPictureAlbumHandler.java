package de.webfilesys.gui.xsl.album;

import java.io.File;
import java.io.IOException;
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

import de.webfilesys.Constants;
import de.webfilesys.FastPathManager;
import de.webfilesys.FileComparator;
import de.webfilesys.FileContainer;
import de.webfilesys.FileLinkSelector;
import de.webfilesys.FileSelectionStatus;
import de.webfilesys.GeoTag;
import de.webfilesys.LanguageManager;
import de.webfilesys.MetaInfManager;
import de.webfilesys.PictureRating;
import de.webfilesys.WebFileSys;
import de.webfilesys.graphics.CameraExifData;
import de.webfilesys.graphics.ScaledImage;
import de.webfilesys.graphics.ThumbnailThread;
import de.webfilesys.gui.xsl.XslRequestHandlerBase;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.UTF8URLEncoder;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslPictureAlbumHandler extends XslRequestHandlerBase
{
	private static int COLUMNS_NO_ZOOM = 4;

	public XslPictureAlbumHandler(
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
		MetaInfManager metaInfMgr=MetaInfManager.getInstance();

        String actPath = null;

		String docRoot = userMgr.getDocumentRoot(uid);
		
		String docRootOS = docRoot;
		
		if (File.separatorChar == '\\')
		{
			docRootOS = docRoot.replace('/', File.separatorChar);
		}

        String relativePath = getParameter("relPath");
        
        if ((relativePath != null) && (relativePath.trim().length() > 0))
        {
        	if (relativePath.equals(File.separator))
        	{
        		actPath = docRoot;
        		
				if (File.separatorChar == '\\')
				{
					actPath = actPath.replace('/', File.separatorChar);
				}
        	}
        	else
        	{
        		if (relativePath.startsWith(File.separator))
        		{
					actPath = docRootOS + relativePath;
        		}
        		else
        		{
					actPath = docRootOS + File.separator + relativePath;
        		}
        	}
        }
        else
        {
			actPath = getParameter("actPath");

			if ((actPath == null) || (actPath.length() == 0))
			{
				actPath = getCwd();
				
                if (actPath == null)
                {
                	actPath = userMgr.getDocumentRoot(uid);
                }
				
				if (File.separatorChar == '\\')
				{
					actPath = actPath.replace('/', File.separatorChar);
				}
			}
			
			relativePath = actPath.substring(docRoot.length());
        }

		session.setAttribute("cwd", actPath);

		String showDeatailsParm = req.getParameter("showDetails");
		
		boolean showDetails = false;
		
		if (showDeatailsParm != null) 
		{
		    showDetails = Boolean.valueOf(showDeatailsParm);
		    session.setAttribute("showDetails", new Boolean(showDetails));
		}
		else
		{
		    Boolean showDetailsFromSession = (Boolean) session.getAttribute("showDetails");
		    if (showDetailsFromSession != null)
		    {
		        showDetails = showDetailsFromSession.booleanValue();
		    }
		}
		
		boolean pageSizeChanged = false;
		
        int pageSize = WebFileSys.getInstance().getThumbnailsPerPage();
        
        String pageSizeParm = getParameter("pageSize");
        if ((pageSizeParm != null) && (pageSizeParm.trim().length() > 0))
        {
            try
            {
                pageSize = Integer.parseInt(pageSizeParm);

                Integer sessionPageSize = (Integer) session.getAttribute("listPageSize");
                
                if ((sessionPageSize != null) && (sessionPageSize.intValue() != pageSize))
                {
                    pageSizeChanged = true;
                }

                session.setAttribute("listPageSize", new Integer(pageSize));
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
                // pageSize = WebFileSys.getInstance().getThumbnailsPerPage();
                pageSize = userMgr.getPageSize(uid);
                session.setAttribute("listPageSize", new Integer(pageSize));
            }
            else
            {
                pageSize = listPageSize.intValue();             
            }
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
		
		String fileFilter[] = Constants.imgFileMasks;
		
		if (!mask.equals("*"))
		{
			String maskFilter[] = new String[fileFilter.length];
			
			String lowerCaseMask = mask.toLowerCase();
			
			for (int i = 0; i < fileFilter.length; i++)
			{
				if (lowerCaseMask.endsWith(fileFilter[i].toLowerCase()))
				{
					maskFilter[i] = mask;
				}
                else
                {				
					maskFilter[i] = mask + fileFilter[i].substring(1);
                }
			}
			
			fileFilter = maskFilter;
		}
		
        String path_no_slash = null;
        String pathWithSlash = null;

		if (actPath.endsWith(File.separator))
		{
			path_no_slash=actPath.substring(0,actPath.length()-1);
			pathWithSlash = actPath;
		}
		else
		{
			path_no_slash = actPath;
			pathWithSlash = actPath + File.separator;
		}

		boolean dirHasMetaInf=metaInfMgr.dirHasMetaInf(actPath);

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

		if (screenHeightParm!=null)
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
		
        boolean sortByChanged = false;
        
        int sortBy = FileComparator.SORT_BY_FILENAME;

        String temp = getParameter("sortBy");
        if ((temp != null) && (temp.length() > 0))
        {
            try
            {
                sortBy = Integer.parseInt(temp);

                Integer oldSortBy = (Integer) session.getAttribute("sortField");
                
                if ((oldSortBy == null) || (oldSortBy != sortBy)) 
                {
                    sortByChanged = true;
                }
                
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
		
		int columns = COLUMNS_NO_ZOOM;
		int thumbnailSize = Constants.THUMBNAIL_SIZE;

		int startIdx = (-1);

		String initial = getParameter("initial");
        
		if ((initial != null) && (initial.equalsIgnoreCase("true")))
		{
			session.removeAttribute("startIdx");
			
			session.removeAttribute("rating");
		}

        if (maskChanged || pageSizeChanged || sortByChanged)
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
					
					// System.out.println("start idx from session: " + startIdx);
				}
			}
		
			if (startIdx < 0)
			{
				startIdx = 0;
			}
        }

        int rating = (-1); 

		temp = getParameter("rating");

        if (temp != null)
        {
        	try
        	{
        		rating = Integer.parseInt(temp);  
        		
        		session.setAttribute("rating", new Integer(rating));      
        	}
        	catch (NumberFormatException nfe)
        	{
        	}
        }
        else
        {
        	Integer sessionRating = (Integer) session.getAttribute("rating");
        	
        	if (sessionRating != null)
        	{
			    rating = sessionRating.intValue();  
        	}
        }

		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/album/pictureAlbum.xsl\"");

		Element albumElement = doc.createElement("pictureAlbum");
			
		doc.appendChild(albumElement);
			
		doc.insertBefore(xslRef, albumElement);
		
	    XmlUtil.setChildText(albumElement, "language", language, false);
		
		// path section
		Element currentPathElem = doc.createElement("currentPath");
		
		albumElement.appendChild(currentPathElem);
		
		currentPathElem.setAttribute("path", relativePath);
		
		Element partOfPathElem = doc.createElement("pathElem");
			
		currentPathElem.appendChild(partOfPathElem);
			
		partOfPathElem.setAttribute("name", uid);
			
		partOfPathElem.setAttribute("path", File.separator);
		
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
			
			partOfPathElem = doc.createElement("pathElem");
			
			currentPathElem.appendChild(partOfPathElem);
			
			partOfPathElem.setAttribute("name", partOfPath);
			
			partOfPathElem.setAttribute("path", UTF8URLEncoder.encode(partialPath.toString()));
		}
		// end path section

        // subdir section
		Element foldersElem = doc.createElement("folders");
		
		albumElement.appendChild(foldersElem);

        String relPathWithSlash = relativePath;
        
        if ((relativePath.length() > 0) && (!relativePath.endsWith(File.separator)))
        {
        	relPathWithSlash = relativePath + File.separator;
        }
        
		// System.out.println("rel path with slash: " + relPathWithSlash);
        
		File dirFile = new File(actPath);
		
		String fileList[] = dirFile.list();

		if (fileList != null)
		{
			ArrayList subFolders = new ArrayList();
			
			for (int i = 0; i < fileList.length; i++)
			{
				String subDirName = fileList[i];
				
				if (!subDirName.equals(ThumbnailThread.THUMBNAIL_SUBDIR))
				{
					File tempFile = new File(actPath + File.separator + subDirName);

					if (tempFile.isDirectory())
					{
                        subFolders.add(subDirName);
					}
				}
			}

            if (subFolders.size() > 1)
            {
            	Collections.sort(subFolders);
            }

            Iterator iter = subFolders.iterator();
            
            while (iter.hasNext())
            {
				String subDirName = (String) iter.next();
				
				String shortDirName = subDirName;
				
				if (subDirName.length() > 16)
				{
					shortDirName = CommonUtils.shortName(subDirName, 16);
				}
				
				Element subDirElem = doc.createElement("folder");
			
				foldersElem.appendChild(subDirElem);
			
				subDirElem.setAttribute("name", subDirName);

				subDirElem.setAttribute("displayName", shortDirName);
			
				subDirElem.setAttribute("path", UTF8URLEncoder.encode(relPathWithSlash + subDirName));
			}
		}
        // end subdir section

		if (readonly)
		{
			XmlUtil.setChildText(albumElement, "readonly", "true", false);
		}

		if (!WebFileSys.getInstance().isLicensed())
		{
			requestCounter++;

			if (requestCounter % LIC_REMINDER_INTERVAL == 0)
			{
				XmlUtil.setChildText(albumElement, "unlicensed", "true", false);
			}
		}

		XmlUtil.setChildText(albumElement, "css", userMgr.getCSS(uid), false);

		XmlUtil.setChildText(albumElement, "userid", uid, false);
		
		dirFile = new File(actPath);
		
		if ((!dirFile.exists()) || (!dirFile.isDirectory()) || (!dirFile.canRead()))
		{
			Logger.getLogger(getClass()).error("folder is not a readable directory: " + actPath);
			return; 
		}

		XmlUtil.setChildText(albumElement, "headLine", getHeadlinePath(actPath), false);

		String description = metaInfMgr.getDescription(actPath,".");
		
		if ((description!=null) && (description.trim().length()>0))
		{
			XmlUtil.setChildText(albumElement, "description", description, true);
		}

		FileLinkSelector fileSelector = new FileLinkSelector(actPath, sortBy, true);

		FileSelectionStatus selectionStatus = fileSelector.selectFiles(fileFilter, rating, pageSize, startIdx);

		Vector selectedFiles = selectionStatus.getSelectedFiles();

		int fileNum = 0;

		if (selectedFiles != null)
		{
			fileNum=selectionStatus.getNumberOfFiles();
		}
		
		XmlUtil.setChildText(albumElement, "fileNumber", Integer.toString(fileNum), false);

		// XmlUtil.setChildText(albumElement, "currentPath", actPath, false);

        String menuPath = actPath;
        
        if (File.separatorChar == '\\')
        {
        	menuPath = insertDoubleBackslash(actPath.replace('/', '\\'));
        }

		XmlUtil.setChildText(albumElement, "menuPath", menuPath, false);

		XmlUtil.setChildText(albumElement, "pathForScript", insertDoubleBackslash(pathWithSlash), false);

		XmlUtil.setChildText(albumElement, "filter", mask, false);

		XmlUtil.setChildText(albumElement, "sortBy", Integer.toString(sortBy), false);

		XmlUtil.setChildText(albumElement, "rating", Integer.toString(rating), false);

        if (showDetails) 
        {
            XmlUtil.setChildText(albumElement, "showDetails", "true", false);
        }
        
		if (fileNum > 0)
		{
			Element pagingElement = doc.createElement("paging");
        
			albumElement.appendChild(pagingElement);
        
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
                
				int pageStep = numPages / 8;
                
				if (pageStep == 0)
				{
					pageStep = 1;
				}
            
				int currentPage = selectionStatus.getCurrentPage();
            
				boolean currentPrinted = false;
            
				Vector pageStartIndices = selectionStatus.getPageStartIndices();
            
				for (int pageCounter = 0; pageCounter < pageStartIndices.size();)
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
					
					if ((pageCounter < pageStartIndices.size() - 1) &&
					    (pageCounter + pageStep >= pageStartIndices.size())) {
						// show the last page even if it is not a multiple of the page step
						pageCounter = pageStartIndices.size() - 1;
					} else {
						pageCounter += pageStep;
					}
				}
			}
		}

		Element fileListElement = doc.createElement("fileList");
			
		albumElement.appendChild(fileListElement);

		if (selectedFiles != null)
		{
			SimpleDateFormat dateFormat=LanguageManager.getInstance().getDateFormat(language);

			// DecimalFormat numFormat=new DecimalFormat("0,000,000,000,000");
			DecimalFormat numFormat=new DecimalFormat("#,###,###,###,###");

            Element fileGroupElement = null;

            int i = 0;

			for (i = 0; i < selectedFiles.size(); i++)
			{
				if (i % columns == 0)
				{
					fileGroupElement = doc.createElement("fileGroup");
					fileListElement.appendChild(fileGroupElement);
				}
				
				Element fileElement = doc.createElement("file");
                
				fileGroupElement.appendChild(fileElement);

				FileContainer fileCont = (FileContainer) selectedFiles.elementAt(i);
				
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


				String displayName = CommonUtils.shortName(actFilename,20);

				XmlUtil.setChildText(fileElement, "displayName", displayName);

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

				String realPath = tempFile.getParent();
                
				String realFileName = tempFile.getName();

				int commentCount = metaInfMgr.countComments(realPath, realFileName);

				XmlUtil.setChildText(fileElement, "comments", Integer.toString(commentCount));
				
		        PictureRating pictureRating = metaInfMgr.getPictureRating(realPath, realFileName);
		        
		        if (pictureRating != null) 
		        {
		            if (pictureRating.getNumberOfVotes() > 0) 
		            {
	                    XmlUtil.setChildText(fileElement, "visitorRating", Integer.toString(pictureRating.getAverageVisitorRating()));
                        XmlUtil.setChildText(fileElement, "numberOfVotes", Integer.toString(pictureRating.getNumberOfVotes()));
		            }
		        }
				
				String fullFileName = tempFile.getAbsolutePath();
				
				if (File.pathSeparatorChar == '\\')
				{
					fullFileName = fullFileName.replace('\\', '/');
				}

				boolean imgFound=true;

				ScaledImage scaledImage=null;

				try
				{
					scaledImage = new ScaledImage(fullFileName, screenWidth-80, screenHeight-135);
				}
				catch (IOException io1)
				{
					Logger.getLogger(getClass()).error(io1);
					imgFound=false;                 
				}
				
				if (imgFound)
				{
					XmlUtil.setChildText(fileElement, "imgType", Integer.toString(scaledImage.getImageType()));
					XmlUtil.setChildText(fileElement, "xpix", Integer.toString(scaledImage.getRealWidth()));
					XmlUtil.setChildText(fileElement, "ypix", Integer.toString(scaledImage.getRealHeight()));
             
					int thumbWidth = 0;
					int thumbHeight = 0;

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
					
					String srcFileName = fullFileName;
					
					boolean useThumb = false;
					boolean useExif = false;

					CameraExifData exifData=null;

                    String thumbFileName = ThumbnailThread.getThumbnailPath(fullFileName);

                    File thumbnailFile = new File(thumbFileName);
                    if (thumbnailFile.exists())
                    {
                        srcFileName="/webfilesys/servlet?command=getThumb&imgFile=" + UTF8URLEncoder.encode(fullFileName);
                        useThumb=true;

                        try
                        {
                            ScaledImage thumbImage = new ScaledImage(thumbFileName, 100, 100);
                            thumbWidth = thumbImage.getRealWidth();
                            thumbHeight = thumbImage.getRealHeight();
                        }
                        catch (IOException ioex)
                        {
                            Logger.getLogger(getClass()).error(ioex);
                        }
                    }
                    else
                    {
						int sizeBorder=500;

						if ((scaledImage.getImageType()==ScaledImage.IMG_TYPE_JPEG) && 
							((scaledImage.getRealWidth() > sizeBorder) ||
							 (scaledImage.getRealHeight() > sizeBorder)))
						{
							exifData = new CameraExifData(fullFileName);

							if (exifData.getThumbnailLength() > 0)
							{
                                useExif = true;
                                
                                boolean orientationMissmatch = false;

                                if (scaledImage.getRealWidth() < scaledImage.getRealHeight())
								{
                                    // portrait orientation
                                    
									if (exifData.getThumbOrientation() != CameraExifData.ORIENTATION_PORTRAIT)
									{
                                        orientationMissmatch = true;
									}
								}
                                else if (scaledImage.getRealWidth() > scaledImage.getRealHeight())
                                {
                                    // landscape orientation
                                    
                                    if (exifData.getThumbOrientation() != CameraExifData.ORIENTATION_LANDSCAPE)
                                    {
                                        orientationMissmatch = true;
                                    }
                                }

                                if (orientationMissmatch) {
                                    
                                    if (exifData.getOrientation() == 1) {
                                        // orientation value of exif data suggests that no rotation is required
                                        // but orientation of exif thumbnail does not match orientation of
                                        // the JPEG picture
                                        // some camera models that have no orientation sensor 
                                        // set the orientation value to 1
                                        useExif = false;
                                    }
                                }
                                
                                if (useExif) {
                                    int exifThumbWidth;
                                    int exifThumbHeight;

                                    if (orientationMissmatch)
                                    {
                                        exifThumbWidth = exifData.getThumbHeight();
                                        exifThumbHeight = exifData.getThumbWidth();
                                        srcFileName = "/webfilesys/servlet?command=exifThumb&imgFile=" + UTF8URLEncoder.encode(fullFileName) + "&rotate=true";
                                    }
                                    else
                                    {
                                        exifThumbWidth = exifData.getThumbWidth();
                                        exifThumbHeight = exifData.getThumbHeight();
                                        srcFileName = "/webfilesys/servlet?command=exifThumb&imgFile=" + UTF8URLEncoder.encode(fullFileName);
                                    }
                                        
                                    if (exifThumbHeight > exifThumbWidth)
                                    {
                                        thumbHeight = thumbnailSize;
                                        thumbWidth = exifThumbWidth * thumbnailSize / exifThumbHeight;
                                    }
                                    else
                                    {
                                        thumbWidth = thumbnailSize;
                                        thumbHeight = exifThumbHeight * thumbnailSize / exifThumbWidth;
                                    }
                                    
                                    useThumb = true;
                                }
							}
						}
                    }

					XmlUtil.setChildText(fileElement, "thumbnailWidth", Integer.toString(thumbWidth));
					XmlUtil.setChildText(fileElement, "thumbnailHeight", Integer.toString(thumbHeight));

					if (!useThumb)
					{
						srcFileName = "/webfilesys/servlet?command=getFile&filePath=" + UTF8URLEncoder.encode(srcFileName);
					}
					
					XmlUtil.setChildText(fileElement, "imgPath", srcFileName);
				}
			}
			
			if (fileGroupElement != null) 
			{
				// fill the rest of the table row
				
				while (i % columns > 0)
				{
					Element dummyElement = doc.createElement("dummy");
				
					fileGroupElement.appendChild(dummyElement);
					
					i++;
				}
			}
		}

		GeoTag geoTag = metaInfMgr.getGeoTag(actPath, ".");
		
		if (geoTag != null)
		{
			XmlUtil.setChildText(albumElement, "geoTag", "true", false);
	        
	        // the reason for this is historic: previous google maps api version required an API key
			XmlUtil.setChildText(albumElement, "googleMaps", "true", false);
		}

		this.processResponse("album/pictureAlbum.xsl", true);

		FastPathManager.getInstance().queuePath(uid, actPath);
	}
}
