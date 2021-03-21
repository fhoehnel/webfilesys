package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import de.webfilesys.Constants;
import de.webfilesys.LanguageManager;
import de.webfilesys.MetaInfManager;
import de.webfilesys.PictureRating;
import de.webfilesys.graphics.CameraExifData;
import de.webfilesys.graphics.ImageTransform;
import de.webfilesys.graphics.ScaledImage;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.UTF8URLEncoder;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XformImageHandler extends XmlRequestHandlerBase {
    private static final Logger LOG = Logger.getLogger(XformImageHandler.class);
	
	public XformImageHandler(HttpServletRequest req, HttpServletResponse resp, HttpSession session, PrintWriter output,
			String uid) {
		super(req, resp, session, output, uid);
	}

	protected void process() {
		if (!checkWriteAccess()) {
			return;
		}

		String imageName = req.getParameter("imgName");

		if (CommonUtils.isEmpty(imageName)) {
			LOG.warn("missing parameter imgName");
			return;
		}

		String domId = getParameter("domId");
		if (CommonUtils.isEmpty(domId)) {
			LOG.warn("missing parameter domId");
			return;
		}

		String currentPath = getCwd();
		
		String origImagePath;
		if (currentPath.endsWith(File.separator)) {
			origImagePath = currentPath + imageName;
		} else {
			origImagePath = currentPath + File.separator + imageName;
		}

		String action = req.getParameter("action");

		String degrees = req.getParameter("degrees");

		if (degrees == null) {
			degrees = "90";
		}

		ImageTransform imgTrans = new ImageTransform(origImagePath, action, degrees);

		String resultImageName = imgTrans.execute(false);

		File resultImgFile = new File(currentPath, resultImageName);

		SimpleDateFormat dateFormat = LanguageManager.getInstance().getDateFormat(language);

		DecimalFormat numFormat = new DecimalFormat("#,###,###,###,###");

		Element fileElement = doc.createElement("file");

		doc.appendChild(fileElement);

		fileElement.setAttribute("id", domId);

		fileElement.setAttribute("name", resultImageName);

		fileElement.setAttribute("nameForScript", escapeForJavascript(resultImageName));
		
		MetaInfManager metaInfMgr = MetaInfManager.getInstance();

		String description = metaInfMgr.getDescription(currentPath, resultImageName);

		if ((description != null) && (description.trim().length() > 0)) {
			XmlUtil.setChildText(fileElement, "description", description, true);
		}

		String displayName = CommonUtils.shortName(resultImageName, 22);

		XmlUtil.setChildText(fileElement, "displayName", displayName);

		fileElement.setAttribute("lastModified", dateFormat.format(new Date(resultImgFile.lastModified())));

		long kBytes = 0L;

		long fileSize = resultImgFile.length();

		if (fileSize > 0L) {
			kBytes = fileSize / 1024L;

			if (kBytes == 0L) {
				kBytes = 1;
			}
		}

		fileElement.setAttribute("size", numFormat.format(kBytes));

		String realPath = resultImgFile.getParent();

		String realFileName = resultImgFile.getName();

		int commentCount = metaInfMgr.countComments(realPath, realFileName);

		XmlUtil.setChildText(fileElement, "comments", Integer.toString(commentCount));

		PictureRating pictureRating = metaInfMgr.getPictureRating(realPath, realFileName);

		if (pictureRating != null) {
			if (pictureRating.getNumberOfVotes() > 0) {
				XmlUtil.setChildText(fileElement, "visitorRating",
						Integer.toString(pictureRating.getAverageVisitorRating()));
			}
		}

		if (!readonly) {
			int ownerRating = metaInfMgr.getOwnerRating(resultImgFile.getAbsolutePath());

			if (ownerRating > (-1)) {
				XmlUtil.setChildText(fileElement, "ownerRating", Integer.toString(ownerRating));
			}
		}

		String resultImagePath = resultImgFile.getAbsolutePath();
		
		try {
			ScaledImage scaledImage = new ScaledImage(resultImagePath, 100, 100);

			XmlUtil.setChildText(fileElement, "imgType", Integer.toString(scaledImage.getImageType()));
			XmlUtil.setChildText(fileElement, "xpix", Integer.toString(scaledImage.getRealWidth()));
			XmlUtil.setChildText(fileElement, "ypix", Integer.toString(scaledImage.getRealHeight()));

			int thumbWidth = 0;
			int thumbHeight = 0;

	        CameraExifData exifData = new CameraExifData(resultImagePath);
	        
	        if ((exifData.getThumbWidth() > 0) && (exifData.getThumbHeight() > 0)) {
	            thumbWidth = exifData.getThumbWidth();
	            thumbHeight = exifData.getThumbHeight();
	        } else {
	   			if (scaledImage.getRealHeight() > scaledImage.getRealWidth()) {
	   				thumbHeight = Constants.THUMBNAIL_SIZE;
	   				thumbWidth = scaledImage.getRealWidth() * Constants.THUMBNAIL_SIZE / scaledImage.getRealHeight();
	   			} else {
	   				thumbWidth = Constants.THUMBNAIL_SIZE;
	   				thumbHeight = scaledImage.getRealHeight() * Constants.THUMBNAIL_SIZE / scaledImage.getRealWidth();
	   			}
	        }

	        if ((exifData.getOrientation() == 6) || (exifData.getOrientation() == 8)) {
	        	int savedThumbWidth = thumbWidth;
	            thumbWidth = thumbHeight;
	        	thumbHeight = savedThumbWidth;
	        }

	        XmlUtil.setChildText(fileElement, "thumbWidth", Integer.toString(thumbWidth));
			XmlUtil.setChildText(fileElement, "thumbHeight", Integer.toString(thumbHeight));
			
		} catch (IOException ioex) {
			LOG.error("failed to get image data", ioex);
		}
		
		String imgSrcPath = "/webfilesys/servlet?command=picThumb&imgFile=" + UTF8URLEncoder.encode(resultImageName);

		XmlUtil.setChildText(fileElement, "imgSrcPath", imgSrcPath);

		XmlUtil.setChildText(fileElement, "imgPath", resultImagePath);

		XmlUtil.setChildText(fileElement, "encodedPath", UTF8URLEncoder.encode(resultImagePath), false);
		
		XmlUtil.setChildText(fileElement, "pathForScript", escapeForJavascript(resultImagePath), false);

		XmlUtil.setChildText(fileElement, "encodedName", UTF8URLEncoder.encode(resultImageName), false);
		
		XmlUtil.setChildText(fileElement, "nameForId", resultImageName.replace(' ',  '_'));

		addMsgResource("label.comments", getResource("label.comments", "Comments"));

		processResponse();
	}

}
