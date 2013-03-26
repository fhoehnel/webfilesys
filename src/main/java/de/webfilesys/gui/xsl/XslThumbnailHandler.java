package de.webfilesys.gui.xsl;

import java.io.File;
import java.io.IOException;
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
import de.webfilesys.GeoTag;
import de.webfilesys.LanguageManager;
import de.webfilesys.MetaInfManager;
import de.webfilesys.PictureRating;
import de.webfilesys.WebFileSys;
import de.webfilesys.graphics.CameraExifData;
import de.webfilesys.graphics.ScaledImage;
import de.webfilesys.graphics.ThumbnailThread;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.UTF8URLEncoder;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslThumbnailHandler extends XslFileListHandlerBase
{
	private static int COLUMNS_NO_ZOOM = 4;
	private static int COLUMNS_ZOOM = 2;

	private boolean clientIsLocal = false;
    
	public XslThumbnailHandler(
			HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid,
            boolean clientIsLocal)
	{
        super(req, resp, session, output, uid);
		
		this.clientIsLocal = clientIsLocal;
	}
	  
	protected void process()
	{
		int i;
		File tempFile;

		session.setAttribute("viewMode", new Integer(Constants.VIEW_MODE_THUMBS));

		MetaInfManager metaInfMgr=MetaInfManager.getInstance();

		String actPath = getParameter("actPath");
		if ((actPath == null) || (actPath.length() == 0))
		{
			actPath = getParameter("actpath");
			if ((actPath == null) || (actPath.length() == 0))
			{
				actPath = (String) session.getAttribute("cwd");
			}
		}

		session.setAttribute("cwd", actPath);
		
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
		
		if ((mask != null) && (mask.length()>0))
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
			
			for (i = 0; i < fileFilter.length; i++)
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

		String zoomString = getParameter("zoom");

		if (zoomString != null)
		{
			session.setAttribute("thumbnailZoom", new Boolean(zoomString.equalsIgnoreCase("yes")));
		}

		boolean zoom = false;
		
		Boolean sessionZoom = (Boolean) session.getAttribute("thumbnailZoom");
		
		if (sessionZoom != null)
		{
			zoom = sessionZoom.booleanValue();
		}
		
		int columns = COLUMNS_NO_ZOOM;

		int thumbnailSize = Constants.THUMBNAIL_SIZE;

		if (zoom)
		{
			columns = COLUMNS_ZOOM;
			thumbnailSize=200;
		}

        boolean pageSizeChanged = false;
        
        int pageSize = WebFileSys.getInstance().getThumbnailsPerPage();

        String pageSizeParm = getParameter("pageSize");
        if ((pageSizeParm != null) && (pageSizeParm.trim().length() > 0))
        {
            try
            {
                pageSize = Integer.parseInt(pageSizeParm);

                Integer sessionThumbPageSize = (Integer) session.getAttribute("thumbPageSize");
                
                if ((sessionThumbPageSize != null) && (sessionThumbPageSize.intValue() != pageSize))
                {
                    pageSizeChanged = true;
                }
                
                if ((sessionThumbPageSize == null) || (sessionThumbPageSize.intValue() != pageSize))
                {
                    session.setAttribute("thumbPageSize", new Integer(pageSize));

                    if (!readonly)
                    {
                        userMgr.setPageSize(uid, pageSize);
                    }
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
            else
            {
                pageSize = userMgr.getPageSize(uid);
                session.setAttribute("thumbPageSize", new Integer(pageSize));
            }
        }
		
        boolean sortByChanged = false;
        
        int sortBy = FileComparator.SORT_BY_FILENAME;

        String temp = getParameter("sortBy");
        if ((temp != null) && (temp.length() > 0))
        {
            try
            {
                sortBy=Integer.parseInt(temp);

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
            Integer sessionSortField = (Integer) session.getAttribute("sortField");
            
            if (sessionSortField != null)
            {
                sortBy = sessionSortField.intValue();
                if (sortBy > 7)
                {
                    sortBy = FileComparator.SORT_BY_FILENAME;
                }
            }
        }
        
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

		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/thumbnails.xsl\"");

		Element fileListElement = doc.createElement("fileList");
			
		doc.appendChild(fileListElement);
			
		doc.insertBefore(xslRef, fileListElement);

		String errorMsg = getParameter("errorMsg");
		
		if (errorMsg != null)
		{
		    XmlUtil.setChildText(fileListElement, "errorMsg", errorMsg, false);
		}
        
		ClipBoard clipBoard = (ClipBoard) session.getAttribute("clipBoard");
		
		if (readonly)
		{
			XmlUtil.setChildText(fileListElement, "readonly", "true", false);
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
				XmlUtil.setChildText(fileListElement, "maintananceMode", "true", false);
			}
		}

		XmlUtil.setChildText(fileListElement, "css", userMgr.getCSS(uid), false);
		
	    XmlUtil.setChildText(fileListElement, "language", language, false);
		
		if (this.isBrowserXslEnabled())
		{
			XmlUtil.setChildText(fileListElement, "browserXslEnabled", "true", false);
		}
		
		File dirFile = new File(actPath);
		
		if ((!dirFile.exists()) || (!dirFile.isDirectory()) || (!dirFile.canRead()))
		{
			XmlUtil.setChildText(fileListElement, "dirNotFound", "true", false);
			processResponse("xsl/folderTree.xsl");
			return; 
		}

		if (File.separatorChar == '/')
		{
			if (WebFileSys.getInstance().getJpegtranPath() != null)
			{
				XmlUtil.setChildText(fileListElement, "jpegtran", "true");
			}
		}
		else
		{
			XmlUtil.setChildText(fileListElement, "jpegtran", "true");
		}
        
		XmlUtil.setChildText(fileListElement, "headLine", getHeadlinePath(actPath), false);

		String description = metaInfMgr.getDescription(actPath,".");

		if ((description!=null) && (description.trim().length()>0))
		{
			XmlUtil.setChildText(fileListElement, "description", description, true);
		}

		FileLinkSelector fileSelector = new FileLinkSelector(actPath, sortBy, true);

		FileSelectionStatus selectionStatus = fileSelector.selectFiles(fileFilter, rating, pageSize, startIdx);

		Vector selectedFiles = selectionStatus.getSelectedFiles();

		int fileNum = 0;

		if (selectedFiles != null)
		{
			fileNum=selectionStatus.getNumberOfFiles();
		}
		
		XmlUtil.setChildText(fileListElement, "fileNumber", Integer.toString(fileNum), false);

		XmlUtil.setChildText(fileListElement, "currentPath", actPath, false);

		XmlUtil.setChildText(fileListElement, "menuPath", insertDoubleBackslash(actPath), false);

		XmlUtil.setChildText(fileListElement, "pathForScript", insertDoubleBackslash(pathWithSlash), false);

		XmlUtil.setChildText(fileListElement, "filter", mask, false);

		XmlUtil.setChildText(fileListElement, "sortBy", Integer.toString(sortBy), false);

		XmlUtil.setChildText(fileListElement, "rating", Integer.toString(rating), false);

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
                
				int pageStep = numPages / 9;
                
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
                        // the current page is accidentally a index step page
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
							    // we stepped over the current page, time to show it now!

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
				
				if (!currentPrinted) {
				    // the current page is located after the last page step index, last chance to show it!
                    Element pageElement = doc.createElement("page");
                    pagingElement.appendChild(pageElement);
                    pageElement.setAttribute("num", Integer.toString(currentPage + 1));
				}
			}
		}

		boolean linkFound = false;
		
		if (selectedFiles != null)
		{
			SimpleDateFormat dateFormat=LanguageManager.getInstance().getDateFormat(language);

			// DecimalFormat numFormat=new DecimalFormat("0,000,000,000,000");
			DecimalFormat numFormat=new DecimalFormat("#,###,###,###,###");

            Element fileGroupElement = null;

			for (i=0;i<selectedFiles.size();i++)
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
				
				tempFile = fileCont.getRealFile();

				if (fileCont.isLink())
				{
					fileElement.setAttribute("link" , "true");
					XmlUtil.setChildText(fileElement, "realPath", fileCont.getRealFile().getAbsolutePath(), false);
					XmlUtil.setChildText(fileElement, "realPathForScript", insertDoubleBackslash(fileCont.getRealFile().getAbsolutePath()), false);

					if (!this.accessAllowed(fileCont.getRealFile().getAbsolutePath()))
					{	
						fileElement.setAttribute("outsideDocRoot", "true");
					}
					
					linkFound = true;
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


				String displayName = actFilename;
            
				if (zoom)
				{
					displayName = CommonUtils.shortName(actFilename,40);
				}
				else
				{
					displayName = CommonUtils.shortName(actFilename,20);
				}

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
				
				if (!readonly)
				{
					int ownerRating = metaInfMgr.getOwnerRating(tempFile.getAbsolutePath());

					if (ownerRating > (-1))
					{
						XmlUtil.setChildText(fileElement, "ownerRating", Integer.toString(ownerRating));
					}
				}
				
				String fullFileName = tempFile.getAbsolutePath();

				boolean imgFound=true;

				ScaledImage scaledImage=null;

				try
				{
					// scaledImage = new ScaledImage(fullFileName,screenWidth-100,screenHeight-135);
					scaledImage = new ScaledImage(fullFileName,screenWidth-40,screenHeight-155);
				}
				catch (IOException io1)
				{
					Logger.getLogger(getClass()).error("failed to create scaled image " + fullFileName, io1);
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
					
					// int fullScreenWidth = scaledImage.getScaledWidth() + 20;
					int fullScreenWidth = scaledImage.getScaledWidth() + 10;
					
					if (fullScreenWidth < 600)
					{
						fullScreenWidth = 600;
					}
					
					XmlUtil.setChildText(fileElement, "fullScreenWidth", Integer.toString(fullScreenWidth));
					XmlUtil.setChildText(fileElement, "fullScreenHeight", Integer.toString(scaledImage.getScaledHeight()));
					
					String srcFileName = fullFileName;
					
					boolean useThumb = false;
					boolean useExif = false;
                    boolean orientationMissmatch = false;

					CameraExifData exifData=null;

					if (!zoom)
					{
						String thumbFileName = ThumbnailThread.getThumbnailPath(fullFileName);

						File thumbnailFile = new File(thumbFileName);
						if (thumbnailFile.exists())
						{
                            srcFileName="/webfilesys/servlet?command=getThumb&imgFile=" + UTF8URLEncoder.encode(fullFileName);
                            useThumb = true;
                            
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

							if (clientIsLocal)
							{
								sizeBorder=640;
							}

							if ((scaledImage.getImageType()==ScaledImage.IMG_TYPE_JPEG) && 
								((scaledImage.getRealWidth() > sizeBorder) ||
								 (scaledImage.getRealHeight() > sizeBorder)))
							{
								exifData = new CameraExifData(fullFileName);

								if (exifData.getThumbnailLength() > 0)
								{
                                    useExif = true;
                                    
                                    orientationMissmatch = false;

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

		if (!readonly)
		{
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
			
			if (WebFileSys.getInstance().isAutoCreateThumbs())
			{
				XmlUtil.setChildText(fileListElement, "autoCreateThumbs", "true");
			}
			
			if (WebFileSys.getInstance().getMailHost() !=null)
			{
                XmlUtil.setChildText(fileListElement, "mailEnabled", "true");
			}

			if (linkFound)
	        {
				XmlUtil.setChildText(fileListElement, "linksExist", "true");
	        }
		}

		GeoTag geoTag = metaInfMgr.getGeoTag(actPath, ".");
		
		if (geoTag != null)
		{
			XmlUtil.setChildText(fileListElement, "geoTag", "true", false);

			String googleMapsAPIKey = null;
			if (req.getScheme().equalsIgnoreCase("https"))
			{
				googleMapsAPIKey = WebFileSys.getInstance().getGoogleMapsAPIKeyHTTPS();
			}
			else
			{
				googleMapsAPIKey = WebFileSys.getInstance().getGoogleMapsAPIKeyHTTP();
			}
			if (googleMapsAPIKey != null) {
				XmlUtil.setChildText(fileListElement, "googleMaps", "true", false);
			}
		}
		
		addCurrentTrail(fileListElement, actPath, userMgr.getDocumentRoot(uid), mask);		
		
		this.processResponse("thumbnails.xsl", true);

		FastPathManager.getInstance().queuePath(uid,actPath);
	}
}
