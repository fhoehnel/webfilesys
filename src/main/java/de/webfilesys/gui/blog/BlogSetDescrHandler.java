package de.webfilesys.gui.blog;

import java.io.File;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.GeoTag;
import de.webfilesys.InvitationManager;
import de.webfilesys.MetaInfManager;
import de.webfilesys.WebFileSys;
import de.webfilesys.gui.user.UserRequestHandler;
import de.webfilesys.util.CommonUtils;

/**
 * @author Frank Hoehnel
 */
public class BlogSetDescrHandler extends UserRequestHandler
{
	public static final String PLACEHOLDER_PIC_PATH = "favicon.gif";
	
	protected HttpServletRequest req = null;

	protected HttpServletResponse resp = null;
	
	public BlogSetDescrHandler(
			HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid)
	{
        super(req, resp, session, output, uid);
        this.req = req;
        this.resp = resp;
	}

	protected void process()
	{
		if (!checkWriteAccess())
		{
			return;
		}

		String currentPath = userMgr.getDocumentRoot(uid).replace('/',  File.separatorChar);

		String firstUploadFileName = req.getParameter("firstUploadFileName");
		
		if (CommonUtils.isEmpty(firstUploadFileName)) {
			firstUploadFileName = createDummyPicFileName();
			
			String placeholderPicDestPath;
			if (currentPath.endsWith(File.separator)) {
				placeholderPicDestPath = currentPath + firstUploadFileName;
			} else {
				placeholderPicDestPath = currentPath + File.separator + firstUploadFileName;
			}
			
			String placeholderPicSourcePath = WebFileSys.getInstance().getWebAppRootDir() + "images" + File.separator + PLACEHOLDER_PIC_PATH;			
			
	        Logger.getLogger(getClass()).debug("copying dummy pic file from " + placeholderPicSourcePath);

	        copy_file(placeholderPicSourcePath, placeholderPicDestPath, false);
		}
		
		String blogText = req.getParameter("blogText");
		
		if (Logger.getLogger(getClass()).isDebugEnabled()) {
	        Logger.getLogger(getClass()).debug("firstUploadFileName: " + firstUploadFileName + " blogText: " + blogText);
		}
		
		MetaInfManager metaInfMgr = MetaInfManager.getInstance();

		if (!CommonUtils.isEmpty(blogText)) {
			blogText = CommonUtils.filterForbiddenChars(blogText);
			metaInfMgr.setDescription(currentPath, firstUploadFileName, blogText);
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

				metaInfMgr.setGeoTag(currentPath, firstUploadFileName, geoTag);
			}
		}
		
		String accessCode = InvitationManager.getInstance().getInvitationCode(uid, currentPath);
		
		if (accessCode != null) {
			InvitationManager.getInstance().notifySubscribers(accessCode);
		} else {
	        Logger.getLogger(getClass()).warn("could not determine invitation code for subscription notification, uid=" + uid + " docRoot=" + currentPath);
		}
		
		session.removeAttribute(BlogListHandler.SESSION_KEY_BEFORE_DAY);
		session.removeAttribute(BlogListHandler.SESSION_KEY_AFTER_DAY);
		
		(new BlogListHandler(req, resp, session, output, uid)).handleRequest(); 
	}
	
    private String createDummyPicFileName() {
		String dateYear = req.getParameter("dateYear");
		String dateMonth = req.getParameter("dateMonth");
		String dateDay = req.getParameter("dateDay");
		
		Date now = new Date();

		return dateYear + "-" + dateMonth + "-" + dateDay + "-" + now.getTime() + "0.gif";
    }
}
