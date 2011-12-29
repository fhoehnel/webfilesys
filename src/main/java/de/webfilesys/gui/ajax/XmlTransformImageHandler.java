package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import de.webfilesys.Constants;
import de.webfilesys.DirTreeStatus;
import de.webfilesys.LanguageManager;
import de.webfilesys.MetaInfManager;
import de.webfilesys.PictureRating;
import de.webfilesys.graphics.CameraExifData;
import de.webfilesys.graphics.ImageTransformation;
import de.webfilesys.graphics.ScaledImage;
import de.webfilesys.graphics.ThumbnailThread;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.UTF8URLEncoder;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XmlTransformImageHandler extends XmlRequestHandlerBase
{
	DirTreeStatus dirTreeStatus = null;
	
	public XmlTransformImageHandler(
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
		if (!checkWriteAccess())
		{
			return;
		}

		String origImagePath = req.getParameter("imgName");

		if ((origImagePath == null) || (origImagePath.trim().length()==0))
		{
			return;
		}

		if (!this.checkAccess(origImagePath))
		{
			return;
		}

		String currentPath = this.getCwd();
		
		String action = req.getParameter("action");

		String degrees = req.getParameter("degrees");

		if (degrees == null)
		{
			degrees="90";
		}

		ImageTransformation imgTrans = new ImageTransformation(origImagePath, action, degrees);

		String resultImageName = imgTrans.execute(false);
		
		File tempFile = new File(currentPath, resultImageName);
		
		String domId = req.getParameter("domId");

		boolean zoom = false;
		
		Boolean sessionZoom = (Boolean) session.getAttribute("thumbnailZoom");
		
		if (sessionZoom != null)
		{
			zoom = sessionZoom.booleanValue();
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

		SimpleDateFormat dateFormat = LanguageManager.getInstance().getDateFormat(language);

		DecimalFormat numFormat = new DecimalFormat("#,###,###,###,###");

		int thumbnailSize = Constants.THUMBNAIL_SIZE;

		Element fileElement = doc.createElement("file");
		
		doc.appendChild(fileElement);
		
		fileElement.setAttribute("name", resultImageName);

		fileElement.setAttribute("id", domId);
		
		MetaInfManager metaInfMgr = MetaInfManager.getInstance();

		String description = metaInfMgr.getDescription(currentPath, resultImageName);

		if ((description != null) && (description.trim().length() > 0))
		{
			XmlUtil.setChildText(fileElement, "description", description, true);
		}

		String displayName = resultImageName;
    
		if (zoom)
		{
			displayName = CommonUtils.shortName(resultImageName, 40);
		}
		else
		{
			displayName = CommonUtils.shortName(resultImageName, 20);
		}

		XmlUtil.setChildText(fileElement, "displayName", displayName);

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

		boolean imgFound = true;

		ScaledImage scaledImage = null;

		try
		{
			scaledImage = new ScaledImage(fullFileName, screenWidth-100, screenHeight-135);
		}
		catch (IOException io1)
		{
			Logger.getLogger(getClass()).error(io1);
			imgFound = false;                 
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
			
			int fullScreenWidth = scaledImage.getScaledWidth() + 20;
			
			if (fullScreenWidth < 600)
			{
				fullScreenWidth = 600;
			}
			
			XmlUtil.setChildText(fileElement, "fullScreenWidth", Integer.toString(fullScreenWidth));
			XmlUtil.setChildText(fileElement, "fullScreenHeight", Integer.toString(scaledImage.getScaledHeight()));
			
			String srcFileName = fullFileName;
			
			boolean useThumb = false;
			boolean useExif = false;

			CameraExifData exifData=null;

			if (!zoom)
			{
				String thumbFileName = ThumbnailThread.getThumbnailPath(fullFileName);

				File thumbnailFile = new File(thumbFileName);
				if (thumbnailFile.exists())
				{
					srcFileName = "/webfilesys/servlet?command=getThumb&imgFile=" + UTF8URLEncoder.encode(fullFileName);
					useThumb=true;
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
							if (scaledImage.getRealWidth() >= scaledImage.getRealHeight())
							{
								useExif = true;
							}
							else
							{
								if (exifData.getThumbOrientation() == CameraExifData.ORIENTATION_PORTRAIT)
								{
									useExif = true;
								}
							}

							if (useExif)
							{
								int exifThumbWidth = exifData.getThumbWidth();
								int exifThumbHeight = exifData.getThumbHeight();
								
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
								
								srcFileName = "/webfilesys/servlet?command=exifThumb&imgFile=" + URLEncoder.encode(fullFileName);
								useThumb=true;
							}
						}
					}
				}
			}

			XmlUtil.setChildText(fileElement, "thumbnailWidth", Integer.toString(thumbWidth));
			XmlUtil.setChildText(fileElement, "thumbnailHeight", Integer.toString(thumbHeight));

			if (!useThumb)
			{
				srcFileName = "/webfilesys/servlet?command=getFile&filePath=" + URLEncoder.encode(srcFileName);
			}
			
			XmlUtil.setChildText(fileElement, "imgPath", srcFileName);
			
			XmlUtil.setChildText(fileElement, "encodedPath", URLEncoder.encode(tempFile.getAbsolutePath()), false);

			XmlUtil.setChildText(fileElement, "encodedName", URLEncoder.encode(resultImageName), false);
		}
		
		addMsgResource("label.comments", getResource("label.comments", "Comments"));
		
		processResponse();
	}
	
}
