/*  
 * WebFileSys
 * Copyright (C) 2011 Frank Hoehnel

 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
package de.webfilesys.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.CategoryManager;
import de.webfilesys.Constants;
import de.webfilesys.ResourceBundleHandler;
import de.webfilesys.SubdirExistCache;
import de.webfilesys.TestSubDirThread;
import de.webfilesys.WebFileSys;
import de.webfilesys.graphics.ThumbnailGarbageCollector;
import de.webfilesys.gui.admin.AdminAddUserRequestHandler;
import de.webfilesys.gui.admin.AdminChangeUserRequestHandler;
import de.webfilesys.gui.admin.AdminEditUserRequestHandler;
import de.webfilesys.gui.admin.AdminMenuRequestHandler;
import de.webfilesys.gui.admin.AdminRegisterUserRequestHandler;
import de.webfilesys.gui.admin.AdminSelectUnixFolderHandler;
import de.webfilesys.gui.admin.AdminSelectWinFolderHandler;
import de.webfilesys.gui.admin.AdminSendEmailRequestHandler;
import de.webfilesys.gui.admin.BroadcastRequestHandler;
import de.webfilesys.gui.admin.DeleteUserRequestHandler;
import de.webfilesys.gui.admin.LoginLogoutHistoryHandler;
import de.webfilesys.gui.admin.SessionListHandler;
import de.webfilesys.gui.admin.UserListRequestHandler;
import de.webfilesys.gui.admin.ViewLogRequestHandler;
import de.webfilesys.gui.ajax.AjaxCheckFileChangeHandler;
import de.webfilesys.gui.ajax.AjaxCheckFileExistHandler;
import de.webfilesys.gui.ajax.AjaxCheckForGeoDataHandler;
import de.webfilesys.gui.ajax.AjaxCheckGrepAllowedHandler;
import de.webfilesys.gui.ajax.AjaxDeleteDirPromptHandler;
import de.webfilesys.gui.ajax.AjaxDeleteFilePromptHandler;
import de.webfilesys.gui.ajax.AjaxGrepParamsHandler;
import de.webfilesys.gui.ajax.AjaxSendEmailHandler;
import de.webfilesys.gui.ajax.AutoImageRotateHandler;
import de.webfilesys.gui.ajax.DiscardSearchResultHandler;
import de.webfilesys.gui.ajax.RefreshDriveListHandler;
import de.webfilesys.gui.ajax.XmlAjaxSubDirHandler;
import de.webfilesys.gui.ajax.XmlAssociatedProgramHandler;
import de.webfilesys.gui.ajax.XmlCancelSearchHandler;
import de.webfilesys.gui.ajax.XmlCheckLosslessHandler;
import de.webfilesys.gui.ajax.XmlClearThumbsHandler;
import de.webfilesys.gui.ajax.XmlCoBrowsingClientImageHandler;
import de.webfilesys.gui.ajax.XmlCoBrowsingExitHandler;
import de.webfilesys.gui.ajax.XmlCoBrowsingMasterImageHandler;
import de.webfilesys.gui.ajax.XmlCollapseDirHandler;
import de.webfilesys.gui.ajax.XmlCopyDirHandler;
import de.webfilesys.gui.ajax.XmlCreateBookmarkHandler;
import de.webfilesys.gui.ajax.XmlCreateThumbsHandler;
import de.webfilesys.gui.ajax.XmlCutCopyHandler;
import de.webfilesys.gui.ajax.XmlDeleteDirHandler;
import de.webfilesys.gui.ajax.XmlDirStatsHandler;
import de.webfilesys.gui.ajax.XmlLocalEditorHandler;
import de.webfilesys.gui.ajax.XmlMoveDirHandler;
import de.webfilesys.gui.ajax.XmlMultiCutCopyHandler;
import de.webfilesys.gui.ajax.XmlMultiImageCutCopyHandler;
import de.webfilesys.gui.ajax.XmlRemoveDirHandler;
import de.webfilesys.gui.ajax.XmlRunUnixCmdHandler;
import de.webfilesys.gui.ajax.XmlSelectCompFolderHandler;
import de.webfilesys.gui.ajax.XmlSelectDiffFileHandler;
import de.webfilesys.gui.ajax.XmlSelectSyncFolderHandler;
import de.webfilesys.gui.ajax.XmlSetScreenSizeHandler;
import de.webfilesys.gui.ajax.XmlSlideShowImageHandler;
import de.webfilesys.gui.ajax.XmlSwitchWatchFolderHandler;
import de.webfilesys.gui.ajax.XmlTouchFileHandler;
import de.webfilesys.gui.ajax.XmlTransformImageHandler;
import de.webfilesys.gui.ajax.XmlUploadStatusHandler;
import de.webfilesys.gui.ajax.XmlWinCmdLineHandler;
import de.webfilesys.gui.ajax.XslSwitchReadonlyHandler;
import de.webfilesys.gui.ajax.XslWatchFolderHandler;
import de.webfilesys.gui.ajax.calendar.XmlChangeAppointmentHandler;
import de.webfilesys.gui.ajax.calendar.XmlCheckAlarmHandler;
import de.webfilesys.gui.ajax.calendar.XmlCreateAppointmentHandler;
import de.webfilesys.gui.ajax.calendar.XmlDelayAppointmentHandler;
import de.webfilesys.gui.ajax.calendar.XmlDeleteAppointmentHandler;
import de.webfilesys.gui.ajax.calendar.XmlMoveAppointmentHandler;
import de.webfilesys.gui.ajax.calendar.XmlPasteAppointmentHandler;
import de.webfilesys.gui.anonymous.BlankPageRequestHandler;
import de.webfilesys.gui.anonymous.VersionInfoRequestHandler;
import de.webfilesys.gui.anonymous.VisitorFileRequestHandler;
import de.webfilesys.gui.google.GoogleEarthDirPlacemarkHandler;
import de.webfilesys.gui.google.GoogleEarthFolderPlacemarkHandler;
import de.webfilesys.gui.google.GoogleEarthSinglePlacemarkHandler;
import de.webfilesys.gui.user.AddCommentRequestHandler;
import de.webfilesys.gui.user.CancelPublishRequestHandler;
import de.webfilesys.gui.user.ClipboardPasteRequestHandler;
import de.webfilesys.gui.user.CloneFileRequestHandler;
import de.webfilesys.gui.user.CompareImageRequestHandler;
import de.webfilesys.gui.user.CopyLinkRequestHandler;
import de.webfilesys.gui.user.CreateDirRequestHandler;
import de.webfilesys.gui.user.CreateFileRequestHandler;
import de.webfilesys.gui.user.DecryptFileRequestHandler;
import de.webfilesys.gui.user.DelImageFromThumbHandler;
import de.webfilesys.gui.user.DeleteCommentsRequestHandler;
import de.webfilesys.gui.user.DeleteFileRequestHandler;
import de.webfilesys.gui.user.DeleteImageLinkHandler;
import de.webfilesys.gui.user.DeleteImageRequestHandler;
import de.webfilesys.gui.user.DeleteLinkRequestHandler;
import de.webfilesys.gui.user.DiffCompareHandler;
import de.webfilesys.gui.user.DiffFromTreeHandler;
import de.webfilesys.gui.user.DiffRequestHandler;
import de.webfilesys.gui.user.DiskQuotaRequestHandler;
import de.webfilesys.gui.user.DownloadFolderZipHandler;
import de.webfilesys.gui.user.EditMP3RequestHandler;
import de.webfilesys.gui.user.EditorSaveRequestHandler;
import de.webfilesys.gui.user.EncryptFileRequestHandler;
import de.webfilesys.gui.user.ExecProgramRequestHandler;
import de.webfilesys.gui.user.ExifThumbRequestHandler;
import de.webfilesys.gui.user.FtpBackupHandler;
import de.webfilesys.gui.user.GUnzipRequestHandler;
import de.webfilesys.gui.user.GetFileRequestHandler;
import de.webfilesys.gui.user.GetThumbRequestHandler;
import de.webfilesys.gui.user.GrepRequestHandler;
import de.webfilesys.gui.user.HexViewHandler;
import de.webfilesys.gui.user.ImageTransformationHandler;
import de.webfilesys.gui.user.LicenseReminderRequestHandler;
import de.webfilesys.gui.user.MainFrameSetHandler;
import de.webfilesys.gui.user.Mp3V2ThumbnailHandler;
import de.webfilesys.gui.user.MultiDeleteRequestHandler;
import de.webfilesys.gui.user.MultiDownloadRequestHandler;
import de.webfilesys.gui.user.MultiFileDownloadPromptHandler;
import de.webfilesys.gui.user.MultiImageDeleteHandler;
import de.webfilesys.gui.user.MultiImageDownloadPromptHandler;
import de.webfilesys.gui.user.MultiMoveCopyRequestHandler;
import de.webfilesys.gui.user.MultiZipRequestHandler;
import de.webfilesys.gui.user.OpenStreetMapFilesPOIHandler;
import de.webfilesys.gui.user.OpenStreetMapPOIHandler;
import de.webfilesys.gui.user.PasteAsLinkRequestHandler;
import de.webfilesys.gui.user.PictureStoryRequestHandler;
import de.webfilesys.gui.user.PublishListRequestHandler;
import de.webfilesys.gui.user.PublishMailRequestHandler;
import de.webfilesys.gui.user.PublishRequestHandler;
import de.webfilesys.gui.user.RateVotingHandler;
import de.webfilesys.gui.user.RemoteEditorRequestHandler;
import de.webfilesys.gui.user.RenameFileRequestHandler;
import de.webfilesys.gui.user.RenameImageRequestHandler;
import de.webfilesys.gui.user.RenameLinkRequestHandler;
import de.webfilesys.gui.user.RenameToExifDateHandler;
import de.webfilesys.gui.user.ResetStatisticsRequestHandler;
import de.webfilesys.gui.user.ResizeImageRequestHandler;
import de.webfilesys.gui.user.ResizeParmsRequestHandler;
import de.webfilesys.gui.user.ReturnToPrevDirHandler;
import de.webfilesys.gui.user.RotatedExifThumbHandler;
import de.webfilesys.gui.user.SearchRequestHandler;
import de.webfilesys.gui.user.SelfChangeUserRequestHandler;
import de.webfilesys.gui.user.SelfEditUserRequestHandler;
import de.webfilesys.gui.user.SynchronizeRequestHandler;
import de.webfilesys.gui.user.TailRequestHandler;
import de.webfilesys.gui.user.TransformImageRequestHandler;
import de.webfilesys.gui.user.URLFileRequestHandler;
import de.webfilesys.gui.user.UntarRequestHandler;
import de.webfilesys.gui.user.UserSettingsRequestHandler;
import de.webfilesys.gui.user.ZipContentFileRequestHandler;
import de.webfilesys.gui.user.ZipDirRequestHandler;
import de.webfilesys.gui.user.ZipFileRequestHandler;
import de.webfilesys.gui.user.unix.CompressLZCRequestHandler;
import de.webfilesys.gui.user.unix.KillProcessRequestHandler;
import de.webfilesys.gui.user.unix.MultiTarArchiveHandler;
import de.webfilesys.gui.user.unix.ProcessListRequestHandler;
import de.webfilesys.gui.user.unix.UnixOwnerRequestHandler;
import de.webfilesys.gui.user.unix.XslUnixFileSysStatHandler;
import de.webfilesys.gui.user.windows.DriveInfoRequestHandler;
import de.webfilesys.gui.xsl.XslAddBookmarkPromptHandler;
import de.webfilesys.gui.xsl.XslAlbumImageHandler;
import de.webfilesys.gui.xsl.XslAssignCategoryHandler;
import de.webfilesys.gui.xsl.XslCategoryHandler;
import de.webfilesys.gui.xsl.XslCloneFilePromptHandler;
import de.webfilesys.gui.xsl.XslCoBrowsingClientHandler;
import de.webfilesys.gui.xsl.XslCoBrowsingMasterHandler;
import de.webfilesys.gui.xsl.XslCompFolderParmsHandler;
import de.webfilesys.gui.xsl.XslCompareFolderHandler;
import de.webfilesys.gui.xsl.XslCreateFilePromptHandler;
import de.webfilesys.gui.xsl.XslCreateFolderPromptHandler;
import de.webfilesys.gui.xsl.XslCryptoKeyPromptHandler;
import de.webfilesys.gui.xsl.XslDownloadPromptHandler;
import de.webfilesys.gui.xsl.XslEditMetaInfHandler;
import de.webfilesys.gui.xsl.XslEmailFilePromptHandler;
import de.webfilesys.gui.xsl.XslExifDataHandler;
import de.webfilesys.gui.xsl.XslFastPathHandler;
import de.webfilesys.gui.xsl.XslFileAgeStatsHandler;
import de.webfilesys.gui.xsl.XslFileListHandler;
import de.webfilesys.gui.xsl.XslFileListStatsHandler;
import de.webfilesys.gui.xsl.XslFileSizeStatsHandler;
import de.webfilesys.gui.xsl.XslFileSysBookmarkHandler;
import de.webfilesys.gui.xsl.XslFileTypeStatsHandler;
import de.webfilesys.gui.xsl.XslFindFileHandler;
import de.webfilesys.gui.xsl.XslFolderDiffTreeHandler;
import de.webfilesys.gui.xsl.XslFolderWatchListHandler;
import de.webfilesys.gui.xsl.XslGoogleMapHandler;
import de.webfilesys.gui.xsl.XslListCommentsHandler;
import de.webfilesys.gui.xsl.XslLogonHandler;
import de.webfilesys.gui.xsl.XslMenuBarHandler;
import de.webfilesys.gui.xsl.XslMultiUploadHandler;
import de.webfilesys.gui.xsl.XslOpenStreetMapFilesHandler;
import de.webfilesys.gui.xsl.XslOpenStreetMapHandler;
import de.webfilesys.gui.xsl.XslPictureAlbumHandler;
import de.webfilesys.gui.xsl.XslPictureStoryHandler;
import de.webfilesys.gui.xsl.XslPublishFileHandler;
import de.webfilesys.gui.xsl.XslRenameDirHandler;
import de.webfilesys.gui.xsl.XslRenameFilePromptHandler;
import de.webfilesys.gui.xsl.XslRenameFolderPromptHandler;
import de.webfilesys.gui.xsl.XslRenameImagePromptHandler;
import de.webfilesys.gui.xsl.XslSearchParmsHandler;
import de.webfilesys.gui.xsl.XslSelfRegistrationHandler;
import de.webfilesys.gui.xsl.XslShowImageHandler;
import de.webfilesys.gui.xsl.XslSlideShowHandler;
import de.webfilesys.gui.xsl.XslSlideShowInFrameHandler;
import de.webfilesys.gui.xsl.XslSlideshowParmsHandler;
import de.webfilesys.gui.xsl.XslSyncCompareHandler;
import de.webfilesys.gui.xsl.XslThumbnailHandler;
import de.webfilesys.gui.xsl.XslTreeStatSunburstHandler;
import de.webfilesys.gui.xsl.XslTreeStatsHandler;
import de.webfilesys.gui.xsl.XslUnixCmdLineHandler;
import de.webfilesys.gui.xsl.XslUnixDirTreeHandler;
import de.webfilesys.gui.xsl.XslUploadParmsHandler;
import de.webfilesys.gui.xsl.XslWinDirTreeHandler;
import de.webfilesys.gui.xsl.XslZipContentHandler;
import de.webfilesys.gui.xsl.calendar.XslCalendarHandler;
import de.webfilesys.gui.xsl.calendar.XslCalendarMonthHandler;
import de.webfilesys.gui.xsl.mobile.MobileFolderFileListHandler;
import de.webfilesys.gui.xsl.mobile.MobileShowImageHandler;
import de.webfilesys.mail.Email;
import de.webfilesys.user.UserManager;
import de.webfilesys.util.UTF8URLDecoder;

/**
 * The main servlet class.
 * Command dispatcher that delegates the work to request handlers.
 * 
 * @author Frank Hoehnel
 */
public class WebFileSysServlet extends HttpServlet
{
	// we are open source now!
	// private static char lic[] = {'l','i','c','e','n','s','e','.','t','x','t'};

	private Properties configProperties = null;
	
	static boolean initialized = false;
	
	private static int REQUEST_PATH_LENGTH = "/webfilesys/servlet".length();
	
    public void init(ServletConfig config)
    throws ServletException
    {
    	if (initialized)
    	{
    		return;
    	}
    	
        ServletContext context = config.getServletContext();
    	
        String realLogDirPath = context.getRealPath("/WEB-INF/log");
        
        // this system property is used in log4j.xml to specify an absolute path for the log files
        System.setProperty("webfilesys.log.path", realLogDirPath);
        
    	String configFileName = config.getInitParameter("config");

		if ((configFileName == null) || (configFileName.trim().length() == 0))
		{
			Logger.getLogger(getClass()).fatal("config file not specified in web.xml");
			throw new ServletException ("config file not specified in web.xml");
		}

		String configPath = context.getRealPath(configFileName);
		
		if ((configPath == null) || (configPath.length() == 0))
		{
			Logger.getLogger(getClass()).fatal("cannot determine real path of config file " + configFileName);
			throw new ServletException ("cannot determine real path of config file " + configFileName);
		}

		File configFile = new File(configPath);
		
		if (!configFile.exists())
		{
			throw new ServletException ("config file does not exist: " + configPath);
		}
		
		if ((!configFile.isFile()) || (!configFile.canRead()))
		{
			Logger.getLogger(getClass()).fatal(configPath + " is not a readable file");
			throw new ServletException (configPath + " is not a readable file");
		}
		
		configProperties = new Properties();
		
		try
		{
			FileInputStream propFile = new FileInputStream(configFile);
			
			configProperties.load(propFile);
			
			Logger.getLogger(getClass()).info("properties loaded from " + configFile);
		}
		catch (IOException ioEx)
		{
			Logger.getLogger(getClass()).fatal("error reading config file: " + ioEx);
			throw new ServletException ("error reading config file: " + ioEx);
		}

		String webAppRootDir = context.getRealPath("/");
		
		if ((!webAppRootDir.endsWith(File.separator)) && (!webAppRootDir.endsWith("/")))
		{
		    webAppRootDir = webAppRootDir + File.separator;
		}
        
        WebFileSys webFileSys = WebFileSys.createInstance(configProperties, webAppRootDir);
        
        webFileSys.initialize(configProperties);
        
        /*
        String licFileName = context.getRealPath("/WEB-INF") + "/" + new String(lic);

		File licFile = new File(licFileName);

		boolean licensed = licFile.exists();
		
		Logger.getLogger(getClass()).info("licensed: " + licensed);
		
        webFileSys.setLicensed(licensed);
        */
        
        // we are open source now!
        webFileSys.setLicensed(true);
        
		initialized = true;
    }

    public void destroy ()
    {
        super.destroy ();
    }

    public void doGet (HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, java.io.IOException
    {
		PrintWriter output = null;

		String command = null;
		
		String requestPath = req.getRequestURI();

		if (requestPath.length() > REQUEST_PATH_LENGTH)
		{
            command = "getFile";
			
			if (File.separatorChar == '\\')
			{
				if (requestPath.length() > REQUEST_PATH_LENGTH + 1)
				{
					req.setAttribute("filePath", UTF8URLDecoder.decode(requestPath.substring(REQUEST_PATH_LENGTH + 1)));
				}
				else
				{
					Logger.getLogger(getClass()).warn("invalid request path: " + requestPath);
				}
			}
			else
			{
				req.setAttribute("filePath", UTF8URLDecoder.decode(requestPath.substring(REQUEST_PATH_LENGTH)));
			}
		}
		else
		{
			command = req.getParameter("command");
		}

		if ((command == null) || 
		    ((!command.equals("exifThumb")) && (!command.equals("getFile")) &&
		     (!command.equals("getThumb")) && (!command.equals("multiDownload")) &&
		     (!command.equals("getZipContentFile")) && (!command.equals("visitorFile")) &&
		     (!command.equals("mp3Thumb")) && (!command.equals("downloadFolder"))))
		{
            // resp.setCharacterEncoding("ISO-8859-1");
            resp.setCharacterEncoding("UTF-8");

            output = new PrintWriter(new OutputStreamWriter(resp.getOutputStream(), "UTF-8"));
		}
		
        String clientIP = req.getRemoteAddr();
        
        StringBuffer logEntry = new StringBuffer();
        
        logEntry.append(clientIP);
        logEntry.append(' ');
        logEntry.append(req.getMethod());
        logEntry.append(' ');
        logEntry.append(req.getRequestURI());
        
        String queryString = req.getQueryString();
        if (queryString != null)
        {
            logEntry.append('?');
            logEntry.append(queryString);
        }
        
        logEntry.append(" (");
        logEntry.append(req.getProtocol());
        logEntry.append(')');

        Logger.getLogger(getClass()).info(logEntry.toString());
        
        String localIP = WebFileSys.getInstance().getLocalIPAddress();
		
        boolean requestIsLocal = false;
        
        if (!WebFileSys.getInstance().isSimulateRemote())
        {
            requestIsLocal = clientIP.equals(localIP) || clientIP.equals(WebFileSys.getInstance().getLoopbackAddress());
        }
		
        // prevent caching
        // will be overwritten in GetFileRequestHandler and VisitorFileRequestHandler with Parameter cache=true
		resp.setDateHeader("expires", 0l); 
		
		// inserted 2008/10/08
		// content type will be overwritten in some request handlers
        // last call to setContentType() wins
		resp.setContentType("text/html");
		
		String userid = null;
		
		HttpSession session = req.getSession(false);
    	
		if (session != null)
		{
			userid = (String) session.getAttribute("userid");
			
			if (userid != null)
			{
				if (handleCommand(command, userid, req, resp, session, output, requestIsLocal))
				{
					return;
				}

				if (anonymousCommand(command, req, resp, output, requestIsLocal))
				{
					return;
				}
			}
			else
			{
				if (anonymousCommand(command, req, resp, output, requestIsLocal))
				{
					return;
				}

				if ((command != null) && command.equals("loginForm"))
				{
					(new XslLogonHandler(req, resp, session, output, false)).handleRequest(); 

					return;
				}

				if (output == null) 
				{
		            output = new PrintWriter(new OutputStreamWriter(resp.getOutputStream(), "UTF-8"));
				}
				redirectToLogin(output);
			}
	    }
		else
		{
		    session = req.getSession(true);
		    
		    session.setAttribute("protocol", req.getScheme());
		    
		    session.setAttribute("clientAddress", getClientAddress(req));
		    
		    String userAgent = req.getHeader("User-Agent");
		    
		    if (userAgent != null)
		    {
		    	session.setAttribute("userAgent", userAgent);
		    }
		    
			if (anonymousCommand(command, req, resp, output, requestIsLocal))
			{
				return;
			}
		    
			if ((command != null) && command.equals("loginForm"))
			{
				(new XslLogonHandler(req, resp, session, output, false)).handleRequest(); 

				return;
			}

			redirectToLogin(output);
		}
		
		if (output != null)
		{
			output.flush();
		}

		return;
    }

    public void doPost ( HttpServletRequest req, HttpServletResponse resp )
    throws ServletException, java.io.IOException
    {
    	doGet(req, resp);
    }

    private boolean anonymousCommand(String command, 
    		HttpServletRequest req, HttpServletResponse resp,
    		PrintWriter output,
    		boolean requestIsLocal)
    {
    	if (command == null)
    	{
    		return(false);
    	}
    	
    	if (command.equals("login"))
    	{
    		verifyLogin(req, resp, output, requestIsLocal);
    		
    		return(true);
    	}
    		
    	if (command.equals("silentLogin"))
    	{
    		silentLogin(req, resp, output, requestIsLocal);
    		
    		return(true);
    	}
    		
    	if (command.equals("registerSelf"))
    	{
			(new XslSelfRegistrationHandler(req, resp, req.getSession(true), output)).handleRequest(); 
    		
    		return(true);
    	}
    		
    	if (command.equals("visitorFile"))
    	{
		    (new VisitorFileRequestHandler(req, resp, null, output)).handleRequest(); 
		    
    		return(true);
    	}
    		
    	if (command.equals("blank"))
    	{
		    (new BlankPageRequestHandler(req, resp, output)).handleRequest(); 
    		
    		return(true);
    	}
    	
    	if (command.equals("versionInfo"))
    	{
		    (new VersionInfoRequestHandler(output)).handleRequest(); 
    		
    		return(true);
    	}
    	
    	return(false);
    }

    private boolean handleCommand(String command, String userid,
    		HttpServletRequest req, HttpServletResponse resp,
    		HttpSession session,
    		PrintWriter output,
    		boolean requestIsLocal)
    {
    	if (command == null)
    	{
		    (new MainFrameSetHandler(req, resp, session, output, userid, requestIsLocal)).handleRequest(); 

    		return(true);
    	}
    	
    	if (command.equals("winDirTree") || command.equals("exp") || command.equals("col"))
    	{
    		if (File.separatorChar == '/')
    		{
    			(new XslUnixDirTreeHandler(req, resp, session, output, userid, requestIsLocal)).handleRequest();
    		}
    		else
    		{
    			(new XslWinDirTreeHandler(req, resp, session, output, userid, requestIsLocal)).handleRequest();
    		}
    		
    		return(true);
    	}
    	
    	if (command.equals("ajaxExp"))
    	{
			(new XmlAjaxSubDirHandler(req, resp, session, output, userid)).handleRequest();

			return(true);
    	}

    	if (command.equals("ajaxCollapse"))
    	{
			(new XmlCollapseDirHandler(req, resp, session, output, userid)).handleRequest();

			return(true);
    	}
    	
    	if (command.equals("listFiles"))
    	{
    	    String mobile = (String) session.getAttribute("mobile");
    	    
    	    if (mobile != null) 
    	    {
                (new MobileFolderFileListHandler(req, resp, session, output, userid)).handleRequest(); 
                return true;
    	    }
    	    
    		boolean initial = false;
    		
			int viewMode = Constants.VIEW_MODE_LIST;
        	
        	String viewModeParm = req.getParameter("viewMode");
        	
        	if (viewModeParm != null)
        	{
				try
				{
					viewMode = Integer.parseInt(viewModeParm);
				}
				catch (NumberFormatException nfex)
				{
				}
        	}
            else
            {        	
			    Integer sessionViewMode = (Integer) session.getAttribute("viewMode");
			    
			    if (sessionViewMode != null)
			    {
			    	viewMode = sessionViewMode.intValue();
			    }
            }
        	
        	if (viewMode == Constants.VIEW_MODE_THUMBS)
        	{
    			if (req.getParameter("keepListStatus") == null)
    			{
    				req.setAttribute("initial", "true");
    			}
        		
    		    (new XslThumbnailHandler(req, resp, session, output, userid, requestIsLocal)).handleRequest(); 
					
				return(true);
        	}

			if (viewMode == Constants.VIEW_MODE_STORY)
			{
			    (new XslPictureStoryHandler(req, resp, session, output, userid)).handleRequest(); 
				
				return(true);
			}

            if (viewMode == Constants.VIEW_MODE_STATS)
            {
                (new XslFileListStatsHandler(req, resp, session, output, userid)).handleRequest();

                return(true);
            }
            
			if (req.getParameter("keepListStatus") == null)
			{
        		initial = true;
			}
            
			(new XslFileListHandler(req, resp, session, output, userid, initial)).handleRequest();
			
			return(true);
    	}
    	
        if (command.equals("thumbnail"))
        {
		    (new XslThumbnailHandler(req, resp, session, output, userid, requestIsLocal)).handleRequest(); 

            return(true);
        }
    	
        if (command.equals("storyInFrame"))
        {
		    (new XslPictureStoryHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }
        
        if (command.equals("pictureStory"))
        {
		    (new PictureStoryRequestHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }
        
        if (command.equals("exifThumb"))
        {
            if (req.getParameter("rotate") != null) 
            {
                (new RotatedExifThumbHandler(req, resp, session, output, userid)).handleRequest(); 
            }
            else
            {
                (new ExifThumbRequestHandler(req, resp, session, output, userid)).handleRequest(); 
            }
                
            return(true);
        }
        
        if (command.equals("getThumb"))
        {
  		    (new GetThumbRequestHandler(req, resp, session, output, userid)).handleRequest(); 
		    
		    return(true);
        }
        
        if (command.equals("getFile"))
        {
		    (new GetFileRequestHandler(req, resp, session, output, userid)).handleRequest(); 
		    
		    return(true);
        }
        
        if (command.equals("album"))
        {
		    (new XslPictureAlbumHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }
        
        if (command.equals("albumImg"))
        {
		    (new XslAlbumImageHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }
        
        if (command.equals("getResourceBundle"))
        {
		    (new ResourceBundleHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }

        if (command.equals("ajaxRPC"))
        {
            String method = req.getParameter("method");
            
            if (method.equals("deleteFilePrompt"))
            {
                (new AjaxDeleteFilePromptHandler(req, resp, session, output, userid)).handleRequest(); 
            } 
            else if (method.equals("deleteDirPrompt"))
            {
                (new AjaxDeleteDirPromptHandler(req, resp, session, output, userid)).handleRequest(); 
            } 
            else if (method.equals("checkForGeoData"))
            {
                (new AjaxCheckForGeoDataHandler(req, resp, session, output, userid)).handleRequest(); 
            }
            else if (method.equals("existFile"))
            {
                (new AjaxCheckFileExistHandler(req, resp, session, output, userid)).handleRequest(); 
            }
            else if (method.equals("grepAllowed"))
            {
                (new AjaxCheckGrepAllowedHandler(req, resp, session, output, userid)).handleRequest(); 
            }
            else if (method.equals("grepParams"))
            {
                (new AjaxGrepParamsHandler(req, resp, session, output, userid)).handleRequest(); 
            }
            else if (method.equals("refreshDriveList"))
            {
                (new RefreshDriveListHandler(req, resp, session, output, userid)).handleRequest(); 
            }
            
            return(true);
        }
        
        if (command.equals("fmdelete"))
        {
            (new DeleteFileRequestHandler(req, resp, session, output, userid, requestIsLocal, true)).handleRequest();

            return(true);
        }
        
        if (command.equals("renameFile"))
        {
            (new RenameFileRequestHandler(req, resp, session, output, userid)).handleRequest();

            return(true);
        }

        if (command.equals("renameFilePrompt"))
        {
            (new XslRenameFilePromptHandler(req, resp, session, output, userid)).handleRequest();

            return(true);
        }

        if (command.equals("cloneFilePrompt"))
        {
            (new XslCloneFilePromptHandler(req, resp, session, output, userid)).handleRequest();

            return(true);
        }

        if (command.equals("cloneFile"))
        {
            (new CloneFileRequestHandler(req, resp, session, output, userid)).handleRequest();

            return(true);
        }

    	if (command.equals("menuBar"))
    	{
		    // (new MenuBarRequestHandler(req, resp, session, output, userid, requestIsLocal)).handleRequest(); 

		    (new XslMenuBarHandler(req, resp, session, output, userid)).handleRequest(); 

		    return(true);
    	}
    	
    	if (command.equals("showImg"))
    	{
		    (new XslShowImageHandler(req, resp, session, output, userid)).handleRequest(); 

		    return(true);
    	}
    	
        if (command.equals("slideShowInFrame"))
        {
            (new XslSlideShowInFrameHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }

        if (command.equals("slideShowParms"))
        {
            (new XslSlideshowParmsHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }

        if (command.equals("slideShow"))
        {
		    (new XslSlideShowHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }

        if (command.equals("slideShowImage"))
        {
		    (new XmlSlideShowImageHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }

        if (command.equals("zipFile"))
        {
		    (new ZipFileRequestHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }

        if (command.equals("mkdirPrompt"))
        {
		    (new XslCreateFolderPromptHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }

        if (command.equals("mkdir"))
        {
		    (new CreateDirRequestHandler(req, resp, session, output, userid, requestIsLocal)).handleRequest(); 

            return(true);
        }

        if (command.equals("copyDir"))
        {
		    (new XmlCopyDirHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }

        if (command.equals("moveDir"))
        {
            (new XmlMoveDirHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }

        if (command.equals("deleteDir"))
        {
		    (new XmlDeleteDirHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }

        if (command.equals("removeDir"))
        {
            (new XmlRemoveDirHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }

        if (command.equals("renDirPrompt"))
        {
		    (new XslRenameFolderPromptHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }
        
        if (command.equals("renameDir"))
    	{
		    (new XslRenameDirHandler(req, resp, session, output, userid, requestIsLocal)).handleRequest(); 

		    return(true);
    	}

        if (command.equals("mkfilePrompt"))
        {
		    (new XslCreateFilePromptHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }

        if (command.equals("mkfile"))
        {
		    (new CreateFileRequestHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }
        
        if (command.equals("fastpath"))
        {
		    (new XslFastPathHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }
        
        if (command.equals("returnToPrevDir"))
        {
            (new ReturnToPrevDirHandler(req, resp, session, output, userid, requestIsLocal)).handleRequest(); 

            return(true);
        }
        
        if (command.equals("bookmarks"))
        {
		    (new XslFileSysBookmarkHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }
        
        if (command.equals("addBookmark"))
        {
		    (new XslAddBookmarkPromptHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }

        if (command.equals("createBookmark"))
        {
		    (new XmlCreateBookmarkHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }
        
        if (command.equals("delImageFromThumb"))
        {
		    (new DelImageFromThumbHandler(req, resp, session, output, userid, requestIsLocal)).handleRequest(); 

            return(true);
        }

        if (command.equals("delImage"))
        {
		    (new DeleteImageRequestHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }

        if (command.equals("renameImage"))
        {
		    (new RenameImageRequestHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }
        
        if (command.equals("renameImagePrompt"))
        {
		    (new XslRenameImagePromptHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }

        if (command.equals("resizeParms"))
        {
		    (new ResizeParmsRequestHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }
        
        if (command.equals("resizeImages"))
        {
		    (new ResizeImageRequestHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }
        
        if (command.equals("transformImage"))
        {
		    (new ImageTransformationHandler(req, resp, session, output, userid, requestIsLocal)).handleRequest(); 

            return(true);
        }

        if (command.equals("autoImgRotate"))
        {
		    (new AutoImageRotateHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }

        if (command.equals("xformImage"))
        {
		    (new XmlTransformImageHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }

        if (command.equals("checkLossless"))
        {
		    (new XmlCheckLosslessHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }

        if (command.equals("exifData"))
        {
            (new XslExifDataHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }
        
        if (command.equals("gunzip"))
        {
		    (new GUnzipRequestHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }

        if (command.equals("uploadParms"))
        {
		    (new XslUploadParmsHandler(req, resp, session, output, userid)).handleRequest(); 
        	
            return(true);
        }

        if (command.equals("multiUpload"))
        {
            (new XslMultiUploadHandler(req, resp, session, output, userid)).handleRequest(); 
            
            return(true);
        }
        
        if (command.equals("cutCopy"))
        {
			(new XmlCutCopyHandler(req, resp, session, output, userid)).handleRequest(); 
            
            return(true);
        }
        
        if (command.equals("editFile"))
        {
        	if (requestIsLocal)
        	{
    			(new XmlLocalEditorHandler(req, resp, session, output, userid)).handleRequest(); 
        	}
        	else
        	{
    		    (new RemoteEditorRequestHandler(req, resp, session, output, userid)).handleRequest(); 
        	}
        	
            return(true);
        }

        if (command.equals("emailFilePrompt"))
        {
            (new XslEmailFilePromptHandler(req, resp, session, output, userid)).handleRequest(); 
            
            return(true);
        }

        if (command.equals("emailFile"))
        {
            (new AjaxSendEmailHandler(req, resp, session, output, userid)).handleRequest(); 
            
            return(true);
        }

        if (command.equals("rate"))
        {
			(new RateVotingHandler(req, resp, session, output, userid)).handleRequest(); 
            
            return(true);
        }
        
        if (command.equals("saveEditor"))
        {
            (new EditorSaveRequestHandler(req, resp, session, output, userid)).handleRequest();
            return (true);
        }
        
        if (command.equals("compareImg"))
        {
		    (new CompareImageRequestHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }
        
        if (command.equals("multiFileCopyMove"))
        {
        	(new XmlMultiCutCopyHandler(req, resp, session, output, userid)).handleRequest(); 
		    
            return(true);
        }

        if (command.equals("multiImageCopyMove"))
        {
		    (new XmlMultiImageCutCopyHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }
                
        if (command.equals("multiImageDelete"))
        {
		    (new MultiImageDeleteHandler(req, resp, session, output, userid, requestIsLocal)).handleRequest(); 

            return(true);
        }
                
        if (command.equals("multiImageExifRename"))
        {
		    (new RenameToExifDateHandler(req, resp, session, output, userid, requestIsLocal)).handleRequest(); 

            return(true);
        }
                
        if (command.equals("multiFileOp"))
        {
            String cmd = req.getParameter("cmd");

            if (cmd.equals("copy") || cmd.equals("move"))
            {
                (new MultiMoveCopyRequestHandler(req, resp, session, output, userid)).handleRequest();

                return(true);
            }

            if (cmd.equals("delete"))
            {
                (new MultiDeleteRequestHandler(req, resp, session, output, userid)).handleRequest();

                return(true);
            }

            if (cmd.equals("zip"))
            {
                (new MultiZipRequestHandler(req, resp, session, output, userid)).handleRequest();

                return(true);
            }

            if (cmd.equals("tar") || cmd.equals("compress"))
            {
                (new MultiTarArchiveHandler(req, resp, session, output, userid)).handleRequest();

                return(true);
            }
        }

        if (command.equals("multiTransform"))
        {
			(new TransformImageRequestHandler(req, resp, session, output, userid)).handleRequest(); 
            
            return(true);
        }

        if (command.equals("diff"))
        {
            (new DiffRequestHandler(req, resp, session, output, userid)).handleRequest();

            return(true);
        }

        if (command.equals("getZipContentFile"))
        {
            (new ZipContentFileRequestHandler(req, resp, session, output, userid)).handleRequest();

            return(true);
        }
        
        if (command.equals("downloadFolder"))
        {
            (new DownloadFolderZipHandler(req, resp, session, output, userid)).handleRequest(); 
            
            return(true);
        }
        
        if (command.equals("mp3Thumb"))
        {
            (new Mp3V2ThumbnailHandler(req, resp, session, output, userid)).handleRequest(); 
                
            return(true);
        }
        
        if (command.equals("tail"))
        {
            (new TailRequestHandler(req, resp, session, output, userid)).handleRequest(); 
            
            return(true);
        }
        
        if (command.equals("grep"))
        {
            (new GrepRequestHandler(req, resp, session, output, userid)).handleRequest(); 
            
            return(true);
        }

        if (command.equals("checkFileChange"))
        {
            (new AjaxCheckFileChangeHandler(req, resp, session, output, userid)).handleRequest(); 
            
            return(true);
        }
        
        if (command.startsWith("coBrowsing"))
        {
            if (command.equals("coBrowsingMaster"))
            {
                (new XslCoBrowsingMasterHandler(req, resp, session, output, userid)).handleRequest(); 

                return(true);
            }

            if (command.equals("coBrowsingClient"))
            {
                (new XslCoBrowsingClientHandler(req, resp, session, output, userid)).handleRequest(); 

                return(true);
            }

            if (command.equals("coBrowsingMasterImage"))
            {
                (new XmlCoBrowsingMasterImageHandler(req, resp, session, output, userid)).handleRequest(); 

                return(true);
            }
            
            if (command.equals("coBrowsingClientImage"))
            {
                (new XmlCoBrowsingClientImageHandler(req, resp, session, output, userid)).handleRequest(); 

                return(true);
            }
            
            if (command.equals("coBrowsingExit"))
            {
                (new XmlCoBrowsingExitHandler(req, resp, session, output, userid)).handleRequest(); 

                return(true);
            }
        }
        
        if (command.equals("uploadStatus"))
        {
			(new XmlUploadStatusHandler(req, resp, session, output, userid)).handleRequest();
			
            return(true);
        }
        
        if (command.equals("createThumbs"))
        {
			(new XmlCreateThumbsHandler(req, resp, session, output, userid)).handleRequest();
			
            return(true);
        }
        
        if (command.equals("clearThumbs"))
        {
			(new XmlClearThumbsHandler(req, resp, session, output, userid)).handleRequest();
			
            return(true);
        }
        
        if (command.equals("pasteFiles"))
        {
            (new ClipboardPasteRequestHandler(req, resp, session, output, userid)).handleRequest();

            return(true);
        }
        
        if (command.equals("pasteLinks"))
        {
            (new PasteAsLinkRequestHandler(req, resp, session, output, userid, requestIsLocal)).handleRequest();

            return(true);
        }
        
        if (command.equals("copyLinks"))
        {
            (new CopyLinkRequestHandler(req, resp, session, output, userid, requestIsLocal)).handleRequest();

            return(true);
        }
        
		if (command.equals("fileStats"))
		{
            (new XslFileListStatsHandler(req, resp, session, output, userid)).handleRequest();

            return(true);
		}
        
		if (command.equals("viewZip"))
		{
            (new XslZipContentHandler(req, resp, session, output, userid)).handleRequest();

            return(true);
		}

		if (command.equals("zipDir"))
		{
            (new ZipDirRequestHandler(req, resp, session, output, userid)).handleRequest();

            return(true);
		}

		if (command.equals("switchReadWrite"))
		{
            (new XslSwitchReadonlyHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
		}

		if (command.equals("editMetaInf"))
        {
		    (new XslEditMetaInfHandler(req, resp, session, output, userid)).handleRequest(); 
        	
            return(true);
        }

        if (command.equals("ajaxFolderStats"))
        {
            (new XmlDirStatsHandler(req, resp, session, output, userid)).handleRequest(); 
            
            return(true);
        }

        if (command.equals("watchFolder"))
        {
            (new XslWatchFolderHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }

        if (command.equals("watchList"))
        {
            (new XslFolderWatchListHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }
        
        if (command.equals("switchFolderWatch"))
        {
            (new XmlSwitchWatchFolderHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }
        
        if (command.equals("fileStatistics")) {

            String cmd = req.getParameter("cmd");
            
            if (cmd != null)
            {
                if (cmd.equals("treeStats"))
                {
                    // (new XslTreeStatisticsHandler(req, resp, session, output, userid)).handleRequest(); 

                    (new XslTreeStatsHandler(req, resp, session, output, userid)).handleRequest(); 
                    
                    return(true);
                }

                if (cmd.equals("sizeStats"))
                {
                    (new XslFileSizeStatsHandler(req, resp, session, output, userid)).handleRequest(); 
                    
                    return(true);
                }

                if (cmd.equals("typeStats"))
                {
                    (new XslFileTypeStatsHandler(req, resp, session, output, userid)).handleRequest(); 
                    
                    return(true);
                }

                if (cmd.equals("ageStats"))
                {
                    (new XslFileAgeStatsHandler(req, resp, session, output, userid)).handleRequest(); 
                    
                    return(true);
                }
            }
            return true;
        }
        
        if (command.equals("search"))
        {
		    (new XslSearchParmsHandler(req, resp, session, output, userid)).handleRequest(); 
        	
            return(true);
        }
		
		if (command.equals("fmfindfile"))
		{
			(new SearchRequestHandler(req, resp, session, output, userid)).handleRequest(); 
            
            return(true);
		}
			
        if (command.equals("findFileTree"))
        {
            (new XslFindFileHandler(req, resp, session, output, userid)).handleRequest(); 
            
            return(true);
        }
            
        if (command.equals("selectSyncFolder"))
        {
			(new XmlSelectSyncFolderHandler(req, resp, session, output, userid)).handleRequest();
			
            return(true);
        }
        
        if (command.equals("syncCompare"))
		{
		    (new XslSyncCompareHandler(req, resp, session, output, userid)).handleRequest(); 

		    return(true);
		}
		
        if (command.equals("synchronize"))
        {
            (new SynchronizeRequestHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }

        if (command.equals("diffSelect"))
        {
            (new XmlSelectDiffFileHandler(req, resp, session, output, userid)).handleRequest();
            
            return(true);
        }

        if (command.equals("startDiff"))
        {
            (new DiffCompareHandler(req, resp, session, output, userid)).handleRequest();
            
            return(true);
        }

        if (command.equals("diffFromTree"))
        {
            (new DiffFromTreeHandler(req, resp, session, output, userid)).handleRequest();
            
            return(true);
        }

        if (command.equals("selectCompFolder"))
        {
            (new XmlSelectCompFolderHandler(req, resp, session, output, userid)).handleRequest();
            
            return(true);
        }

        if (command.equals("compFolderParms"))
        {
            (new XslCompFolderParmsHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }
        
        if (command.equals("compareFolders"))
        {
            (new XslCompareFolderHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }
        
        if (command.equals("folderDiffTree"))
        {
            (new XslFolderDiffTreeHandler(req, resp, session, output, userid)).handleRequest(); 
            return(true);
        }

        if (command.equals("folderTreeStats"))
        {
            (new XslTreeStatSunburstHandler(req, resp, session, output, userid)).handleRequest(); 
            return(true);
        }
        
        if (command.equals("hexView"))
        {
            (new HexViewHandler(req, resp, session, output, userid)).handleRequest();

            return(true);
        }
        
        if (command.equals("cryptoKeyPrompt"))
        {
            (new XslCryptoKeyPromptHandler(req, resp, session, output, userid)).handleRequest();

            return(true);
        }

        if (command.equals("encrypt"))
        {
            (new EncryptFileRequestHandler(req, resp, session, output, userid)).handleRequest();

            return(true);
        }
        
        if (command.equals("decrypt"))
        {
            (new DecryptFileRequestHandler(req, resp, session, output, userid)).handleRequest();

            return(true);
        }
        
        if (command.equals("touch")) {
            (new XmlTouchFileHandler(req, resp, session, output, userid)).handleRequest();
            
            return(true);
        }

        if (command.equals("refresh"))
    	{
            String path = req.getParameter("path");

            SubdirExistCache.getInstance().cleanupExistSubdir(path);
            
            (new TestSubDirThread(path)).start();

            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException iex)
            {
            }

            req.setAttribute("expand", path);
            
            if (File.separatorChar == '/')
    		{
    			(new XslUnixDirTreeHandler(req, resp, session, output, userid, requestIsLocal)).handleRequest();
    		}
    		else
    		{
    			(new XslWinDirTreeHandler(req, resp, session, output, userid, requestIsLocal)).handleRequest();
    		}
    		
    		return(true);
    	}
		
        if (command.equals("setScreenSize"))
        {
            (new XmlSetScreenSizeHandler(req, resp, session, output, userid)).handleRequest(); 
            
            return(true);
        }

        if (command.equals("assignCategory"))
        {
		    (new XslAssignCategoryHandler(req, resp, session, output, userid)).handleRequest(); 
        	
            return(true);
        }
        
        if (command.equals("category"))
        {
		    (new XslCategoryHandler(req, resp, session, output, userid)).handleRequest(); 
        	
            return(true);
        }
        
        if (command.equals("editMP3"))
        {
			(new EditMP3RequestHandler(req, resp, session, output, userid)).handleRequest(); 
            
            return(true);
        }
        
        if (command.equals("unixRights"))
        {
            (new UnixOwnerRequestHandler(req, resp, session, output, userid, false)).handleRequest();

            return(true);
        }
		
        if (command.equals("setUnixRights"))
        {
            (new UnixOwnerRequestHandler(req, resp, session, output, userid, true)).handleRequest();

            return(true);
        }

        if (command.equals("unixCompress"))
        {
            (new CompressLZCRequestHandler(req, resp, session, output, userid)).handleRequest();

            return(true);
        }

        if (command.equals("untar"))
        {
            (new UntarRequestHandler(req, resp, session, output, userid)).handleRequest();

            return(true);
        }
        
        if (command.equals("openUrlFile"))
        {
            (new URLFileRequestHandler(req, resp, session, output, userid)).handleRequest();

            return(true);
        }
        
        if (command.equals("runAssociatedProgram"))
        {
            (new XmlAssociatedProgramHandler(req, resp, session, output, userid)).handleRequest();
            return(true);
        }
        
        if (command.equals("multiFileDownloadPrompt"))
        {
            (new MultiFileDownloadPromptHandler(req, resp, session, output, userid)).handleRequest();

            return(true);
        }

        if (command.equals("multiImageDownloadPrompt"))
        {
            (new MultiImageDownloadPromptHandler(req, resp, session, output, userid)).handleRequest();

            return(true);
        }

        if (command.equals("multiDownload"))
        {
            (new MultiDownloadRequestHandler(req, resp, session, output, userid)).handleRequest();

            return(true);
        }

        if (command.equals("downloadPrompt"))
		{
		    (new XslDownloadPromptHandler(req, resp, session, output, userid)).handleRequest(); 

		    return(true);
		}

		if (command.equals("googleMap"))
        {
		    (new XslGoogleMapHandler(req, resp, session, output, userid)).handleRequest(); 
        	
            return(true);
        }

		if (command.equals("osMap"))
        {
		    (new XslOpenStreetMapHandler(req, resp, session, output, userid)).handleRequest(); 
        	
            return(true);
        }

		if (command.equals("osmPOIList"))
        {
		    (new OpenStreetMapPOIHandler(req, resp, session, output, userid)).handleRequest(); 
        	
            return(true);
        }

		if (command.equals("osMapFiles"))
        {
		    (new XslOpenStreetMapFilesHandler(req, resp, session, output, userid)).handleRequest(); 
        	
            return(true);
        }

		if (command.equals("osmFilesPOIList"))
        {
		    (new OpenStreetMapFilesPOIHandler(req, resp, session, output, userid)).handleRequest(); 
        	
            return(true);
        }

		if (command.equals("publishFile"))
		{
		    (new XslPublishFileHandler(req, resp, session, output, userid)).handleRequest(); 

		    return(true);
		}
        
        if (command.equals("publishForm"))
		{
		    (new PublishMailRequestHandler(req, resp, session, output, userid)).handleRequest(); 

		    return(true);
		}
        
        if (command.equals("publishParms") || command.equals("publishFolder"))
        {
		    (new PublishRequestHandler(req, resp, session, output, userid)).handleRequest(); 

		    return(true);
        }

        if (command.equals("publish"))
		{
		    (new PublishMailRequestHandler(req, resp, session, output, userid)).handleRequest(); 

		    return(true);
		}
        
        if (command.equals("publishList"))
		{
		    (new PublishListRequestHandler(req, resp, session, output, userid)).handleRequest(); 

		    return(true);
		}
        
        if (command.equals("cancelPublish"))
		{
		    (new CancelPublishRequestHandler(req, resp, session, output, userid)).handleRequest(); 

		    return(true);
		}
        
        if (command.equals("listComments"))
		{
		    (new XslListCommentsHandler(req, resp, session, output, userid)).handleRequest(); 

		    return(true);
		}

        if (command.equals("addComment"))
		{
		    (new AddCommentRequestHandler(req, resp, session, output, userid)).handleRequest(); 

		    return(true);
		}

        if (command.equals("delComments"))
		{
		    (new DeleteCommentsRequestHandler(req, resp, session, output, userid)).handleRequest(); 

		    return(true);
		}

        if (command.equals("deleteLink"))
		{
		    (new DeleteLinkRequestHandler(req, resp, session, output, userid)).handleRequest(); 

		    return(true);
		}

        if (command.equals("delImageLink"))
		{
		    (new DeleteImageLinkHandler(req, resp, session, output, userid, requestIsLocal)).handleRequest(); 

		    return(true);
		}

        if (command.equals("renameLink"))
		{
		    (new RenameLinkRequestHandler(req, resp, session, output, userid, requestIsLocal)).handleRequest(); 

		    return(true);
		}

        if (command.equals("ftpBackup"))
		{
		    (new FtpBackupHandler(req, resp, session, output, userid)).handleRequest(); 

		    return(true);
		}

        if (command.equals("diskQuota"))
		{
		    (new DiskQuotaRequestHandler(req, resp, session, output, userid)).handleRequest(); 

		    return(true);
		}

        if (command.equals("resetStatistics"))
        {
			(new ResetStatisticsRequestHandler(req, resp, session, output, userid)).handleRequest();
			
            return(true);
        }

        if (command.equals("selfEditUser"))
        {
			(new SelfEditUserRequestHandler(req, resp, session, output, userid, null)).handleRequest();
			
            return(true);
        }
        
        if (command.equals("selfChangeUser"))
        {
			(new SelfChangeUserRequestHandler(req, resp, session, output, userid)).handleRequest();
			
            return(true);
        }
        
        if (command.equals("editPw") || command.equals("changePw"))
        {
			(new UserSettingsRequestHandler(req, resp, session, output, userid)).handleRequest();
			
            return(true);
        }
        
        if (command.equals("execProgram"))
        {
			(new ExecProgramRequestHandler(req, resp, session, output, userid)).handleRequest();
			
            return(true);
        }

        if (command.equals("driveInfo"))
        {
			(new DriveInfoRequestHandler(req, resp, session, output, userid)).handleRequest();
			
            return(true);
        }

        if (command.equals("winCmdLine"))
        {
			(new XmlWinCmdLineHandler(req, resp, session, output, userid, requestIsLocal)).handleRequest();
			
            return(true);
        }

        if (command.equals("cancelSearch"))
		{
			(new XmlCancelSearchHandler(req, resp, session, output, userid)).handleRequest(); 
            
            return(true);
		}
        
        if (command.equals("discardSearchResults"))
		{
			(new DiscardSearchResultHandler(req, resp, session, output, userid)).handleRequest(); 
            
            return(true);
		}
        
        if (command.equals("processList"))
		{
			(new ProcessListRequestHandler(req, resp, session, output, userid)).handleRequest(); 
            
            return(true);
		}
        
        if (command.equals("killProcess"))
		{
			(new KillProcessRequestHandler(req, resp, session, output, userid)).handleRequest(); 
            
            return(true);
		}
        
        if (command.equals("fileSysUsage"))
		{
            (new XslUnixFileSysStatHandler(req, resp, session, output, userid)).handleRequest(); 
            
            return(true);
		}
        
        if (command.equals("unixCmdLine"))
        {
            (new XslUnixCmdLineHandler(req, resp, session, output, userid)).handleRequest(); 
            
            return(true);
        }
        
        if (command.equals("runUnixCmd"))
        {
            (new XmlRunUnixCmdHandler(req, resp, session, output, userid)).handleRequest(); 
            
            return(true);
        }

        if (command.equals("googleEarthPlacemark"))
        {
            (new GoogleEarthSinglePlacemarkHandler(req, resp, session, output, userid)).handleRequest();
            return(true);
        }
        
        if (command.equals("googleEarthDirPlacemarks"))
        {
            (new GoogleEarthDirPlacemarkHandler(req, resp, session, output, userid)).handleRequest();
            return(true);
        }
        
        if (command.equals("googleEarthFolderPlacemark")) {
            (new GoogleEarthFolderPlacemarkHandler(req, resp, session, output, userid)).handleRequest();
            return(true);
        }
        
        if (command.equals("mobile"))
        {
            String cmd = req.getParameter("cmd");

            if (cmd.equals("folderFileList")) 
            {
                (new MobileFolderFileListHandler(req, resp, session, output, userid)).handleRequest(); 
                return(true);
            }
            
            if (cmd.equals("editFile"))
            {
                (new RemoteEditorRequestHandler(req, resp, session, output, userid)).handleRequest(); 
                return(true);
            }
            
            if (cmd.equals("showImg")) 
            {
                (new MobileShowImageHandler(req, resp, session, output, userid)).handleRequest(); 
                return(true);
            }
        }
        
        if (command.equals("mobileMultiFile"))
        {
            String selCmd = req.getParameter("cmd");

            if (selCmd.equals("delete"))
            {
                (new MultiDeleteRequestHandler(req, resp, session, output, userid)).handleRequest();

                return(true);
            }

            if (selCmd.equals("zip"))
            {
                (new MultiZipRequestHandler(req, resp, session, output, userid)).handleRequest();

                return(true);
            }
        }

        if (command.equals("calendar"))
        {
            String selCmd = req.getParameter("cmd");

            if (selCmd != null) 
            {
                if (selCmd.equals("month"))
                {
                    (new XslCalendarMonthHandler(req, resp, session, output, userid)).handleRequest();
                }
                else if (selCmd.equals("checkAlarm"))
                {
                    (new XmlCheckAlarmHandler(req, resp, session, output, userid)).handleRequest();
                }
                else if (selCmd.equals("newAppointment"))
                {
                    (new XmlCreateAppointmentHandler(req, resp, session, output, userid)).handleRequest();
                }
                else if (selCmd.equals("changeAppointment"))
                {
                    (new XmlChangeAppointmentHandler(req, resp, session, output, userid)).handleRequest();
                }
                else if (selCmd.equals("delAppointment"))
                {
                    (new XmlDeleteAppointmentHandler(req, resp, session, output, userid)).handleRequest();
                }
                else if (selCmd.equals("moveAppointment"))
                {
                    (new XmlMoveAppointmentHandler(req, resp, session, output, userid)).handleRequest();
                }
                else if (selCmd.equals("pasteAppointment"))
                {
                    (new XmlPasteAppointmentHandler(req, resp, session, output, userid)).handleRequest();
                }
                else if (selCmd.equals("delay"))
                {
                    (new XmlDelayAppointmentHandler(req, resp, session, output, userid)).handleRequest();
                }
            } 
            else
            {
                (new XslCalendarHandler(req, resp, session, output, userid)).handleRequest();
            }

            return true;        	
        }
        
        if (command.equals("admin"))
        {
        	String cmd = req.getParameter("cmd");
        	
        	if (cmd == null)
        	{
        		cmd = "menu";
        	}
        	
        	if (cmd.equals("menu"))
        	{
    			(new AdminMenuRequestHandler(req, resp, session, output, userid)).handleRequest(); 
                
                return(true);
        	}

        	if (cmd.equals("userList"))
        	{
    			(new UserListRequestHandler(req, resp, session, output, userid)).handleRequest(); 
                
                return(true);
        	}

        	if (cmd.equals("editUser"))
        	{
    			(new AdminEditUserRequestHandler(req, resp, session, output, userid, null)).handleRequest(); 
                
                return(true);
        	}

        	if (cmd.equals("changeUser"))
        	{
    			(new AdminChangeUserRequestHandler(req, resp, session, output, userid)).handleRequest(); 
                
                return(true);
        	}

        	if (cmd.equals("registerUser"))
        	{
    			(new AdminRegisterUserRequestHandler(req, resp, session, output, userid, null)).handleRequest(); 
                
                return(true);
        	}

        	if (cmd.equals("addUser"))
        	{
    			(new AdminAddUserRequestHandler(req, resp, session, output, userid)).handleRequest(); 
                
                return(true);
        	}

        	if (cmd.equals("deleteUser"))
        	{
    			(new DeleteUserRequestHandler(req, resp, session, output, userid)).handleRequest(); 
                
                return(true);
        	}

        	if (cmd.equals("viewLog"))
        	{
    			(new ViewLogRequestHandler(req, resp, session, output, userid)).handleRequest(); 
                
                return(true);
        	}

            if (cmd.equals("selectDocRoot") || cmd.equals("selectDocRootExp") || cmd.equals("selectDocRootCol"))
            {
                if (File.separatorChar == '/')
                {
                    (new AdminSelectUnixFolderHandler(req, resp, session, output, userid, requestIsLocal)).handleRequest();
                }
                else
                {
                    (new AdminSelectWinFolderHandler(req, resp, session, output, userid, requestIsLocal)).handleRequest();
                }
                
                return(true);
            }

        	if (cmd.equals("broadcast"))
        	{
    			(new BroadcastRequestHandler(req, resp, session, output, userid, null)).handleRequest(); 
                
                return(true);
        	}

        	if (cmd.equals("sendEmail"))
        	{
    			(new AdminSendEmailRequestHandler(req, resp, session, output, userid)).handleRequest(); 
                
                return(true);
        	}
        	
        	if (cmd.equals("sessionList"))
        	{
    			(new SessionListHandler(req, resp, session, output, userid)).handleRequest(); 
                
                return(true);
        	}
        	
        	if (cmd.equals("loginHistory"))
        	{
    			(new LoginLogoutHistoryHandler(req, resp, session, output, userid)).handleRequest(); 
                
                return(true);
        	}
        	
        	if (cmd.equals("switchMode"))
        	{
        		WebFileSys.getInstance().setMaintananceMode(!WebFileSys.getInstance().isMaintananceMode());
                
    			(new AdminMenuRequestHandler(req, resp, session, output, userid)).handleRequest(); 
        		
                return(true);
        	}
        }
        
        if (command.equals("start"))
        {
        	// for executeOnSlientLogin
        	
        	String viewModeParm = req.getParameter("viewMode");
        	
        	if (viewModeParm != null)
        	{
    			int viewMode = Constants.VIEW_MODE_LIST;
        		
        		try
        		{
        			viewMode = Integer.parseInt(viewModeParm);
        			
        			session.setAttribute("viewMode" , new Integer(viewMode));
        		}
        		catch (NumberFormatException numEx)
        		{
        		}
        	}

		    (new MainFrameSetHandler(req, resp, session, output, userid, requestIsLocal)).handleRequest(); 

		    return(true);
        }
        
        if (command.equals("licenseReminder"))
        {
			(new LicenseReminderRequestHandler(req, resp, session, output, userid)).handleRequest();
			
            return(true);
        }

        if (command.equals("logout"))
		{
			logout(req, resp, session, userid);
            
            return(true);
		}

        return(false);
    }
    
    protected void logout(HttpServletRequest req, HttpServletResponse resp, HttpSession session, String userid)
    {
        UserManager userMgr = WebFileSys.getInstance().getUserMgr();

		if (WebFileSys.getInstance().isAutoCreateThumbs())
		{
			String docRoot = userMgr.getDocumentRoot(userid);
        	
			if ((!docRoot.equals("/")) && (!docRoot.equals("*:")))
			{
				ThumbnailGarbageCollector thumbnailCleaner = new ThumbnailGarbageCollector(docRoot);
				thumbnailCleaner.start();
			}
		}

		CategoryManager.getInstance().disposeCategoryList(userid);

		session.removeAttribute("userid");

    	session.invalidate();

        String logoutPage = "/webfilesys/servlet";

        if (WebFileSys.getInstance().getLogoutURL() != null)
        {
            logoutPage = WebFileSys.getInstance().getLogoutURL();
        }

        Logger.getLogger(getClass()).info(req.getRemoteAddr() + ": logout user " + userid);
        
        try
        {
            resp.sendRedirect(logoutPage);
        }
        catch (IOException ioex)
        {
        	Logger.getLogger(getClass()).warn(ioex);
        }
    }
    
    public void verifyLogin(HttpServletRequest req, HttpServletResponse resp,
    		PrintWriter output, boolean requestIsLocal)
    {
        String userid = req.getParameter("userid");
        String password = req.getParameter("password");

        String clientIP = req.getRemoteAddr();

        UserManager userMgr = WebFileSys.getInstance().getUserMgr();
        
        String logEntry = null;

		HttpSession session = null;
        
        if ((userid != null) && (password != null))
        {
            if (userMgr.checkPassword(userid, password))
            {
        		session = req.getSession(true);

        		session.setAttribute("userid", userid);
        		
        		session.setAttribute("loginEvent", "true");
        		
                String browserType = req.getHeader("User-Agent");
                
        		String role = userMgr.getRole(userid);
        		
        		if ((role != null) && role.equals("album"))
        		{
        			(new XslPictureAlbumHandler(req, resp, session, output, userid)).handleRequest();
        			
        		}
        		else
        		{
        		    if (isMobileClient(browserType)) 
        		    {
        		        req.setAttribute("initial", "true");
                        (new MobileFolderFileListHandler(req, resp, session, output, userid)).handleRequest(); 
        		    }
        		    else
        		    {
                        (new MainFrameSetHandler(req, resp, session, output, userid, requestIsLocal)).handleRequest(); 
        		    }
        		}

                WebFileSys.getInstance().getUserMgr().setLastLoginTime(userid, new Date());

                logEntry = clientIP + ": login user " + userid;

    			if (browserType != null)
    			{
    				logEntry = logEntry + " [" + browserType + "]";
    			}
    			
                Logger.getLogger(getClass()).info(logEntry);

                if ((WebFileSys.getInstance().getMailHost() != null) && WebFileSys.getInstance().isMailNotifyLogin())
                {
                    (new Email(WebFileSys.getInstance().getUserMgr().getAdminUserEmails(),
                        "login successful",
                        WebFileSys.getInstance().getLogDateFormat().format(new Date()) + " " + logEntry))
                        .send();
                }

                return;
            }

            if (userMgr.checkReadonlyPassword(userid, password))
            {
        		session = req.getSession(true);

        		session.setAttribute("userid", userid);
        		
        		session.setAttribute("loginEvent", "true");

        		session.setAttribute("readonly", "true");

        		String role = userMgr.getRole(userid);
        		
        		if ((role != null) && role.equals("album"))
        		{
        			(new XslPictureAlbumHandler(req, resp, session, output, userid)).handleRequest();
        			
        		}
        		else
        		{
    		        (new MainFrameSetHandler(req, resp, session, output, userid, requestIsLocal)).handleRequest(); 
        		}
    		        
                logEntry = clientIP + ": login user " + userid + " (read-only)";
                
        		String browserType = req.getHeader("User-Agent");
                
    			if (browserType != null)
    			{
    				logEntry = logEntry + " [" + browserType + "]";
    			}

                Logger.getLogger(getClass()).info(logEntry);

                if ((WebFileSys.getInstance().getMailHost() != null) && WebFileSys.getInstance().isMailNotifyLogin())
                {
                    (new Email(WebFileSys.getInstance().getUserMgr().getAdminUserEmails(),
                        "login successful",
                        WebFileSys.getInstance().getLogDateFormat().format(new Date()) + " " + logEntry))
                        .send();
                }

                return;
            }
        }

        logEntry = clientIP + ": login failed for user " + userid;
        Logger.getLogger(getClass()).warn(logEntry);

        if ((WebFileSys.getInstance().getMailHost() != null) && WebFileSys.getInstance().isMailNotifyLogin())
        {
            (new Email(WebFileSys.getInstance().getUserMgr().getAdminUserEmails(),
                "login failed",
                WebFileSys.getInstance().getLogDateFormat().format(new Date()) + " " + logEntry))
                .send();
        }

        if (WebFileSys.getInstance().getLoginErrorPage() != null)
        {
        	try
        	{
                resp.sendRedirect(WebFileSys.getInstance().getLoginErrorPage());
        	}
        	catch (IOException ioex)
        	{
        		Logger.getLogger(getClass()).warn(ioex);
        	}
        	
            return;
        }

        // logon(true);
        
	    (new XslLogonHandler(req, resp, session, output, true)).handleRequest(); 
    }
    
    public void silentLogin(HttpServletRequest req, HttpServletResponse resp,
    		PrintWriter output, boolean requestIsLocal)
    {
    	String requestParms = req.getQueryString();
    	
    	StringTokenizer parmParser = new StringTokenizer(requestParms, "?&=");
    	
    	if (parmParser.hasMoreTokens())
    	{
    		parmParser.nextToken(); // skip command param name
    	}
    	
    	if (parmParser.hasMoreTokens())
    	{
    		parmParser.nextToken(); // skip the command value
    	}

    	String userid = null;
        String password = null;
    	
    	if (parmParser.hasMoreTokens())
    	{
    		userid = parmParser.nextToken();
    	}

    	if (parmParser.hasMoreTokens())
    	{
    		password = parmParser.nextToken();
    	}

    	StringBuffer executeOnLoginCmd = new StringBuffer();

    	StringBuffer redirectURL = new StringBuffer();

    	if (parmParser.hasMoreTokens())
    	{
    		// execute on login command exists
    		parmParser.nextToken(); // skip the cmd value

            executeOnLoginCmd.append("command");
    		
            int i = 0;
            
    		while (parmParser.hasMoreTokens())
        	{
        		if (i % 2 == 0)
        		{
        			executeOnLoginCmd.append('=');
        		}
        		else
        		{
        			executeOnLoginCmd.append('&');
        		}

        		executeOnLoginCmd.append(parmParser.nextToken());
        		
                i++;        		
        	}

    	    // System.out.println("execute on Login cmd: " + executeOnLoginCmd);
    	    
    	    String originalURL = req.getRequestURI();
    	    
    	    // System.out.println("original URL: " + originalURL);
    	    
    	    redirectURL = new StringBuffer();
    	    
    	    redirectURL.append(originalURL);  
    	    
    	    redirectURL.append('?');
    	    
            redirectURL.append(executeOnLoginCmd.toString());
            
    	    // System.out.println("redirect URL: " + redirectURL);
    	}
    	
        UserManager userMgr = WebFileSys.getInstance().getUserMgr();

        String clientIP = req.getRemoteAddr();

        String logEntry = null;

		HttpSession session = null;
        
        if ((userid != null) && (password != null))
        {
            if (userMgr.checkPassword(userid, password))
            {
        		session = req.getSession(true);

        		session.setAttribute("userid", userid);
        		
        		session.setAttribute("loginEvent", "true");
        		
        		session.removeAttribute("cwd");

        		session.removeAttribute("startIdx");
        		
                WebFileSys.getInstance().getUserMgr().setLastLoginTime(userid, new Date());

                logEntry = clientIP + ": silent login user " + userid;

        		String browserType = req.getHeader("User-Agent");
                
    			if (browserType != null)
    			{
    				logEntry = logEntry + " [" + browserType + "]";
    			}
    			
                Logger.getLogger(getClass()).info(logEntry);

                if ((WebFileSys.getInstance().getMailHost() != null) && WebFileSys.getInstance().isMailNotifyLogin())
                {
                    (new Email(WebFileSys.getInstance().getUserMgr().getAdminUserEmails(),
                        "login successful",
                        WebFileSys.getInstance().getLogDateFormat().format(new Date()) + " " + logEntry))
                        .send();
                }

                if (redirectURL.length() > 0)
                {
                	try
                	{
                	    resp.sendRedirect(redirectURL.toString());
                	    
                	    return;
                	}
                	catch (IOException ioex)
                	{
                		Logger.getLogger(getClass()).error(ioex);
                	}
                }
                
        		String role = userMgr.getRole(userid);
        		
        		if ((role != null) && role.equals("album"))
        		{
        			(new XslPictureAlbumHandler(req, resp, session, output, userid)).handleRequest();
        			
        		}
        		else
        		{
    		        (new MainFrameSetHandler(req, resp, session, output, userid, requestIsLocal)).handleRequest(); 
        		}
    		    
                return;
            }

            if (userMgr.checkReadonlyPassword(userid, password))
            {
        		session = req.getSession(true);

        		session.setAttribute("userid", userid);
        		
        		session.setAttribute("loginEvent", "true");

        		session.setAttribute("readonly", "true");

        		session.removeAttribute("cwd");

        		session.removeAttribute("startIdx");

                logEntry = clientIP + ": silent login user " + userid + " (read-only)";
                
        		String browserType = req.getHeader("User-Agent");
                
    			if (browserType != null)
    			{
    				logEntry = logEntry + " [" + browserType + "]";
    			}

                Logger.getLogger(getClass()).info(logEntry);

                if ((WebFileSys.getInstance().getMailHost() != null) && WebFileSys.getInstance().isMailNotifyLogin())
                {
                    (new Email(WebFileSys.getInstance().getUserMgr().getAdminUserEmails(),
                        "silent login successful",
                        WebFileSys.getInstance().getLogDateFormat().format(new Date()) + " " + logEntry))
                        .send();
                }

                if (redirectURL.length() > 0)
                {
                	try
                	{
                	    resp.sendRedirect(redirectURL.toString());
                	    
                	    return;
                	}
                	catch (IOException ioex)
                	{
                		Logger.getLogger(getClass()).error(ioex);
                	}
                }
                
        		String role = userMgr.getRole(userid);
        		
        		if ((role != null) && role.equals("album"))
        		{
        			(new XslPictureAlbumHandler(req, resp, session, output, userid)).handleRequest();
        		}
        		else
        		{
        		    (new MainFrameSetHandler(req, resp, session, output, userid, requestIsLocal)).handleRequest(); 
        		}
    		    
                return;
            }
        }

        logEntry = clientIP + ": silent login failed for user " + userid;
        Logger.getLogger(getClass()).warn(logEntry);

        if ((WebFileSys.getInstance().getMailHost() != null) && WebFileSys.getInstance().isMailNotifyLogin())
        {
            (new Email(WebFileSys.getInstance().getUserMgr().getAdminUserEmails(),
                "silent login failed",
                WebFileSys.getInstance().getLogDateFormat().format(new Date()) + " " + logEntry))
                .send();
        }

        if (WebFileSys.getInstance().getLoginErrorPage() != null)
        {
        	try
        	{
                resp.sendRedirect(WebFileSys.getInstance().getLoginErrorPage());
        	}
        	catch (IOException ioex)
        	{
        		Logger.getLogger(getClass()).warn(ioex);
        	}
        	
            return;
        }

        // logon(true);
        
	    (new XslLogonHandler(req, resp, session, output, true)).handleRequest(); 
    }
    
    private void redirectToLogin(PrintWriter output)
    {
		output.println("<html>");
		output.println("<head>");
		output.println("<meta http-equiv=\"expires\" content=\"0\">");

		output.println("<script language=\"javascript\">");

		output.println("  top.location.href='/webfilesys/servlet?command=loginForm';"); 

		output.println("</script>");

		output.println("</head>"); 
		output.println("</html>");
		
		output.flush();
    }
    
    private String getClientAddress(HttpServletRequest req)
    {
		String hostIP = req.getRemoteHost();
		
		if ((hostIP == null) || (hostIP.trim().length() == 0))
		{
			hostIP = req.getRemoteAddr();
		}
		
		return(hostIP);
    }
    
    private boolean isMobileClient(String browserType)
    {
        if (browserType == null)
        {
            return false;
        }
        
        if (browserType.indexOf("Android") >= 0)
        {
            return true;
        }
        
        return false;
    }
}


