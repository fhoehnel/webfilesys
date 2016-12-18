package de.webfilesys.gui.blog;

import java.io.File;
import java.io.PrintWriter;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.Comment;
import de.webfilesys.GeoTag;
import de.webfilesys.MetaInfManager;
import de.webfilesys.graphics.BlogThumbnailHandler;
import de.webfilesys.gui.user.UserRequestHandler;
import de.webfilesys.util.CommonUtils;

/**
 * @author Frank Hoehnel
 */
public class BlogChangeEntryHandler extends UserRequestHandler {
	protected HttpServletRequest req = null;

	protected HttpServletResponse resp = null;
	
	public BlogChangeEntryHandler(
			HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid) {
        super(req, resp, session, output, uid);
        this.req = req;
        this.resp = resp;
	}

	protected void process() {
		if (!checkWriteAccess()) {
			return;
		}

		String currentPath = getCwd();

		if ((currentPath == null) || (currentPath.trim().length() == 0)) {
			currentPath = userMgr.getDocumentRoot(uid).replace('/',  File.separatorChar);
		}

		if (!checkAccess(currentPath)) {
			return;
		}

		String fileName = req.getParameter("fileName");
		
		if (CommonUtils.isEmpty(fileName)) {
	        Logger.getLogger(getClass()).error("missing parameter fileName");
            return;
		}

        File oldFile = new File(currentPath, fileName);
        if ((!oldFile.exists()) || (!oldFile.isFile()) || (!oldFile.canWrite())) {
	        Logger.getLogger(getClass()).error("blog entry file not found: " + fileName);
	        return;
        }

        String oldFilePath = oldFile.getAbsolutePath();

        MetaInfManager metaInfMgr = MetaInfManager.getInstance();
		
        String newFileName = fileName;
        
		String fileNamePrefixFromDate = getFileNamePrefixFromDate();
		
		int savedStatus = (-1);
		
		if (!fileNamePrefixFromDate.equals(fileName.substring(0, 10))) {
	        Logger.getLogger(getClass()).debug("date has changed");

	        savedStatus = metaInfMgr.getStatus(oldFilePath);	        
	        
	        // newFileName = fileNamePrefixFromDate + fileName.substring(10);
	        newFileName = fileNamePrefixFromDate + "-" + System.currentTimeMillis() + CommonUtils.getFileExtension(fileName);
	        
	        File newFile = new File(currentPath, newFileName);
	        
	        if (!oldFile.renameTo(newFile)) {
		        Logger.getLogger(getClass()).error("failed to rename blog file " + fileName + " to " + newFile.getName());
		        return;
	        } else {
	        	BlogThumbnailHandler.getInstance().renameThumbnail(oldFilePath, newFileName);
	        }

	        metaInfMgr.removeDescription(oldFilePath);
	        
			metaInfMgr.removeGeoTag(currentPath, fileName);
			
			Vector<Comment> comments = metaInfMgr.getListOfComments(oldFilePath);
			if ((comments != null) && (comments.size() > 0)) {
				for (Comment comment : comments) {
					metaInfMgr.addComment(currentPath, newFileName, comment);
				}
			}
			
			metaInfMgr.removeComments(oldFilePath);
		}
		
		String blogText = req.getParameter("blogText");
		
		if (!CommonUtils.isEmpty(blogText)) {
			blogText = CommonUtils.filterForbiddenChars(blogText);
			metaInfMgr.setDescription(currentPath, newFileName, blogText);
		} else {
			metaInfMgr.setDescription(currentPath, newFileName, "");
		}

		if (savedStatus == MetaInfManager.STATUS_BLOG_EDIT) {
			metaInfMgr.setStatus(currentPath, newFileName, MetaInfManager.STATUS_BLOG_EDIT);
		}
		
		String geoDataSwitcher = req.getParameter("geoDataSwitcher");
		
		if (geoDataSwitcher != null) {
			boolean geoDataExist = false;

			float latitude = 0f;
			
			String latitudeParm = req.getParameter("latitude");
			
			if ((latitudeParm != null) && (latitudeParm.trim().length() > 0)) {
				try
				{
					latitude = Float.parseFloat(latitudeParm);
					
					if ((latitude >= -90.0f) && (latitude <= 90.0f)) {
						geoDataExist = true;
					}
				} catch (NumberFormatException nfex) {
				}
			}

			float longitude = 0f;
			
			String longitudeParm = req.getParameter("longitude");
			
			if ((longitudeParm != null) && (longitudeParm.trim().length() > 0)) {
				try {
					longitude = Float.parseFloat(longitudeParm);

					if ((longitude >= -180.0f) && (longitude <= 180.0f)) {
						geoDataExist = true;
					}
				} catch (NumberFormatException nfex) {
				}
			}

			int zoomFactor = 10;
			
			String zoomFactorParm = req.getParameter("zoomFactor");
			
			if ((zoomFactorParm != null) && (zoomFactorParm.trim().length() > 0)) {
				try {
					zoomFactor = Integer.parseInt(zoomFactorParm);
				} catch (NumberFormatException nfex) {
				}
			}
			
			if (geoDataExist) {
				GeoTag geoTag = new GeoTag(latitude, longitude, zoomFactor);
				
				String infoText = req.getParameter("infoText");
				
				if (infoText != null) {
					geoTag.setInfotext(infoText);
				}

				metaInfMgr.setGeoTag(currentPath, newFileName, geoTag);
			}
		} else {
			if (newFileName.equals(fileName)) {
				if (metaInfMgr.getGeoTag(currentPath, newFileName) != null) {
					metaInfMgr.removeGeoTag(currentPath, newFileName);
				}
			}
		}

		(new BlogListHandler(req, resp, session, output, uid)).handleRequest(); 
	}
	
    private String getFileNamePrefixFromDate() {
		String dateYear = req.getParameter("dateYear");
		String dateMonth = req.getParameter("dateMonth");
		String dateDay = req.getParameter("dateDay");
		
		return dateYear + "-" + dateMonth + "-" + dateDay;
    }
    
}
