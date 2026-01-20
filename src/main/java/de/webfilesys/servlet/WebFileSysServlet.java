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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.webfilesys.*;
import de.webfilesys.gui.ajax.*;
import de.webfilesys.gui.api.*;
import de.webfilesys.gui.xsl.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;


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
import de.webfilesys.gui.user.ActivateUserRequestHandler;
import de.webfilesys.gui.user.AddCommentRequestHandler;
import de.webfilesys.gui.user.CancelPublishRequestHandler;
import de.webfilesys.gui.user.ClipboardPasteRequestHandler;
import de.webfilesys.gui.user.CloneFolderRequestHandler;
import de.webfilesys.gui.user.CopyLinkRequestHandler;
import de.webfilesys.gui.user.CreateDirRequestHandler;
import de.webfilesys.gui.user.CreateFileRequestHandler;
import de.webfilesys.gui.user.DecryptFileRequestHandler;
import de.webfilesys.gui.user.DeleteCommentsRequestHandler;
import de.webfilesys.gui.user.DeleteFileRequestHandler;
import de.webfilesys.gui.user.DeleteImageLinkHandler;
import de.webfilesys.gui.user.DeleteLinkRequestHandler;
import de.webfilesys.gui.user.DiffCompareHandler;
import de.webfilesys.gui.user.DiffFromTreeHandler;
import de.webfilesys.gui.user.DiffRequestHandler;
import de.webfilesys.gui.user.DiskQuotaRequestHandler;
import de.webfilesys.gui.user.DownloadFolderZipHandler;
import de.webfilesys.gui.user.EditMP3RequestHandler;
import de.webfilesys.gui.user.EncryptFileRequestHandler;
import de.webfilesys.gui.user.ExecProgramRequestHandler;
import de.webfilesys.gui.user.ExifThumbRequestHandler;
import de.webfilesys.gui.user.FtpBackupHandler;
import de.webfilesys.gui.user.GPXTrackHandler;
import de.webfilesys.gui.user.GPXWayPointHandler;
import de.webfilesys.gui.user.GUnzipRequestHandler;
import de.webfilesys.gui.user.GetFileRequestHandler;
import de.webfilesys.gui.user.GetThumbRequestHandler;
import de.webfilesys.gui.user.GrepRequestHandler;
import de.webfilesys.gui.user.HexViewHandler;
import de.webfilesys.gui.user.ImageTransformationHandler;
import de.webfilesys.gui.user.MainFrameSetHandler;
import de.webfilesys.gui.user.Mp3V2ThumbnailHandler;
import de.webfilesys.gui.user.MultiDeleteRequestHandler;
import de.webfilesys.gui.user.MultiFileDownloadHandler;
import de.webfilesys.gui.user.MultiImageDeleteHandler;
import de.webfilesys.gui.user.MultiImageDownloadHandler;
import de.webfilesys.gui.user.MultiMoveCopyRequestHandler;
import de.webfilesys.gui.user.MultiVideoDeleteHandler;
import de.webfilesys.gui.user.MultiZipRequestHandler;
import de.webfilesys.gui.user.OpenStreetMapFilesPOIHandler;
import de.webfilesys.gui.user.OpenStreetMapPOIHandler;
import de.webfilesys.gui.user.PasteAsLinkRequestHandler;
import de.webfilesys.gui.user.PublishMailRequestHandler;
import de.webfilesys.gui.user.PublishRequestHandler;
import de.webfilesys.gui.user.RateVotingHandler;
import de.webfilesys.gui.user.RenameLinkRequestHandler;
import de.webfilesys.gui.user.RenameToExifDateHandler;
import de.webfilesys.gui.user.RenameVideoRequestHandler;
import de.webfilesys.gui.user.ResetStatisticsRequestHandler;
import de.webfilesys.gui.user.ResizeImageRequestHandler;
import de.webfilesys.gui.user.ReturnToPrevDirHandler;
import de.webfilesys.gui.user.RotatedExifThumbHandler;
import de.webfilesys.gui.user.SearchGPSRequestHandler;
import de.webfilesys.gui.user.SearchRequestHandler;
import de.webfilesys.gui.user.SelfChangeUserRequestHandler;
import de.webfilesys.gui.user.SwitchFileAgeColoringHandler;
import de.webfilesys.gui.user.SynchronizeRequestHandler;
import de.webfilesys.gui.user.TailRequestHandler;
import de.webfilesys.gui.user.ThumbnailRequestHandler;
import de.webfilesys.gui.user.TransformImageRequestHandler;
import de.webfilesys.gui.user.URLFileRequestHandler;
import de.webfilesys.gui.user.UntarRequestHandler;
import de.webfilesys.gui.user.UserSettingsRequestHandler;
import de.webfilesys.gui.user.VideoFramePreviewHandler;
import de.webfilesys.gui.user.VideoThumbHandler;
import de.webfilesys.gui.user.ZipContentFileRequestHandler;
import de.webfilesys.gui.user.ZipDirRequestHandler;
import de.webfilesys.gui.user.ZipFileRequestHandler;
import de.webfilesys.gui.user.unix.CompressLZCRequestHandler;
import de.webfilesys.gui.user.unix.KillProcessRequestHandler;
import de.webfilesys.gui.user.unix.MultiTarArchiveHandler;
import de.webfilesys.gui.user.unix.ProcessListRequestHandler;
import de.webfilesys.gui.user.unix.UnixOwnerRequestHandler;
import de.webfilesys.gui.user.unix.XslUnixFileSysStatHandler;
import de.webfilesys.gui.user.windows.XslDriveInfoRequestHandler;
import de.webfilesys.gui.xsl.album.AddAlbumCommentHandler;
import de.webfilesys.gui.xsl.album.XslAlbumPictureHandler;
import de.webfilesys.gui.xsl.album.XslAlbumSlideShowHandler;
import de.webfilesys.gui.xsl.album.XslPictureAlbumHandler;
import de.webfilesys.gui.xsl.calendar.XslCalendarHandler;
import de.webfilesys.gui.xsl.calendar.XslCalendarMonthHandler;
import de.webfilesys.gui.xsl.mobile.MobileFolderFileListHandler;
import de.webfilesys.gui.xsl.mobile.MobileFolderPictureHandler;
import de.webfilesys.gui.xsl.mobile.MobileShowImageHandler;
import de.webfilesys.mail.SmtpEmail;
import de.webfilesys.user.UserManager;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.UTF8URLDecoder;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.builder.api.*;

/**
 * The main servlet class.
 * Command dispatcher that delegates the work to request handlers.
 * 
 * @author Frank Hoehnel
 */
public class WebFileSysServlet extends ServletBase {
    private static final Logger LOG = LogManager.getLogger(WebFileSysServlet.class);

	private static final int MIN_SCREEN_WIDTH_FOR_DESKTOP_VERSION = 1024;

    static boolean initialized = false;
	
	private static final int REQUEST_PATH_LENGTH = "/webfilesys/servlet".length();
	
    public void init(ServletConfig config)
    throws ServletException {
    	if (initialized) {
    		return;
    	}

        ServletContext context = config.getServletContext();
    	
        String realLogDirPath = context.getRealPath("/WEB-INF/log");
        updateLoggerPath(realLogDirPath + File.separator + "system.log");

    	String configFileName = config.getInitParameter("config");

		if ((configFileName == null) || (configFileName.trim().isEmpty())) {
			LOG.fatal("config file not specified in web.xml");
			throw new ServletException ("config file not specified in web.xml");
		}

		String configPath = context.getRealPath(configFileName);
		
		if ((configPath == null) || (configPath.isEmpty())) {
			LOG.fatal("cannot determine real path of config file " + configFileName);
			throw new ServletException ("cannot determine real path of config file " + configFileName);
		}

		File configFile = new File(configPath);
		
		if (!configFile.exists()) {
			throw new ServletException ("config file does not exist: " + configPath);
		}
		
		if ((!configFile.isFile()) || (!configFile.canRead())) {
			LOG.fatal(configPath + " is not a readable file");
			throw new ServletException (configPath + " is not a readable file");
		}

        Properties configProperties = new Properties();
		
		FileInputStream propFile = null;
		
		try {
			propFile = new FileInputStream(configFile);
            configProperties.load(propFile);
            LOG.info("config properties loaded from {}", configFile);
		} catch (IOException ioEx) {
			LOG.fatal("error reading config file " + configFile, ioEx);
			throw new ServletException ("error reading config file " + configFile + ": " + ioEx.getMessage());
		} finally {
			if (propFile != null) {
				try {
					propFile.close();
				} catch (IOException ex) {
				}
			}
		}

		String webAppRootDir = context.getRealPath("/");
        if ((!webAppRootDir.endsWith(File.separator)) && (!webAppRootDir.endsWith("/"))) {
		    webAppRootDir = webAppRootDir + File.separator;
		}

        WebFileSys webFileSys = WebFileSys.createInstance(configProperties, webAppRootDir);
        webFileSys.initialize();
        initialized = true;
    }

    public void destroy ()
    {
        super.destroy ();
    }

    private void updateLoggerPath(String logFilePath) {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration configuration = context.getConfiguration();

        Appender appender = configuration.getAppender("WebFileSysLogAppender");
        Layout<? extends Serializable> oldLayout = appender.getLayout();
        appender.stop();
        configuration.removeLogger("de.webfilesys");

        LoggerConfig loggerConfig = new LoggerConfig("de.webfilesys", Level.DEBUG, false);

        appender = FileAppender.createAppender(logFilePath, "false", "false", "WebFileSysLogAppender",
                "true", "true", "true",
                  "8192", oldLayout, null, "false", "", configuration);
        appender.start();
        loggerConfig.addAppender(appender, null, null);
        configuration.addLogger("de.webfilesys", loggerConfig);

        context.updateLoggers();
    }

    public void doGet (HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, java.io.IOException
    {
		PrintWriter output = null;

		String command = null;
		
		String requestPath = req.getRequestURI();

		if (requestPath.length() > REQUEST_PATH_LENGTH) {
            command = "getFile";
			
			if (File.separatorChar == '\\') {
				if (requestPath.length() > REQUEST_PATH_LENGTH + 1) {
					req.setAttribute("filePath", UTF8URLDecoder.decode(requestPath.substring(REQUEST_PATH_LENGTH + 1)));
				} else {
					LOG.warn("invalid request path: " + requestPath);
				}
			} else {
				req.setAttribute("filePath", UTF8URLDecoder.decode(requestPath.substring(REQUEST_PATH_LENGTH)));
			}
		} else {
			command = req.getParameter("command");
		}

		if ((command == null) || 
		    ((!command.equals("exifThumb")) && (!command.equals("getFile")) && (!command.equals("picThumb")) &&
		     (!command.equals("getThumb")) && (!command.equals("multiDownload")) &&
		     (!command.equals("getZipContentFile")) && (!command.equals("visitorFile")) &&
		     (!command.equals("videoThumb")) && 
		     (!command.equals("mp3Thumb")) && (!command.equals("downloadFolder")))) {

            resp.setCharacterEncoding("UTF-8");
            output = new PrintWriter(new OutputStreamWriter(resp.getOutputStream(), StandardCharsets.UTF_8));
		}
		
        String clientIP = req.getRemoteAddr();

        logAccess(req, clientIP);

        String localIP = WebFileSys.getInstance().getLocalIPAddress();
		
        boolean requestIsLocal = false;
        
        if (!WebFileSysConfig.getInstance().isSimulateRemote()) {
            requestIsLocal = clientIP.equals(localIP) || clientIP.equals(WebFileSys.LOOPBACK_ADDRESS) || clientIP.equals(WebFileSys.IPV6_LOOPBACK_ADDRESS);
        }
		
        // prevent caching
        // will be overwritten in GetFileRequestHandler and VisitorFileRequestHandler with Parameter cache=true
        // and in ResourceBundleHandler
		resp.setDateHeader("expires", 0L);
		resp.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate, no-store");
		
		// inserted 2008/10/08
		// content type will be overwritten in some request handlers
        // last call to setContentType() wins
		resp.setContentType("text/html");
		
		HttpSession session = req.getSession(false);

        boolean userInSession = false;

		if (session != null) {
			String userid = (String) session.getAttribute("userid");
			if (userid != null) {
                userInSession = true;
				if (handleCommand(command, userid, req, resp, session, output, requestIsLocal)) {
					return;
				}
				if (anonymousCommand(command, req, resp, output, requestIsLocal)) {
					return;
				}
			}
	    } else {
            session = req.getSession(true);
            setSessionInfo(req, session);
        }

        if (!userInSession) {
			if (anonymousCommand(command, req, resp, output, requestIsLocal)) {
				return;
			}
			if ("loginForm".equals(command)) {
				(new XslLogonHandler(req, resp, session, output, false)).handleRequest(); 
				return;
			}
            if ("ajaxExp".equals(command) ||
                "pollForFolderTreeChange".equals(command) ||
                "pollForDirChange".equals(command)) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
			if (output == null) {
	            output = new PrintWriter(new OutputStreamWriter(resp.getOutputStream(), StandardCharsets.UTF_8));
			}
			redirectToLogin(output);
		}
		
		if (output != null) {
			output.flush();
		}
    }

    public void doPost (HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, java.io.IOException {
    	doGet(req, resp);
    }

    private void logAccess(HttpServletRequest req, String clientIP) {
        StringBuilder logEntry = new StringBuilder();

        logEntry.append(clientIP);
        logEntry.append(' ');
        logEntry.append(req.getMethod());
        logEntry.append(' ');

        logEntry.append(req.getRequestURI());

        String queryString = req.getQueryString();
        if (queryString != null)
        {
            if (!queryString.contains("silentLogin"))
            {
                logEntry.append('?');
                logEntry.append(queryString);
            }
        }

        logEntry.append(" (");
        logEntry.append(req.getProtocol());
        logEntry.append(')');

        LOG.info(logEntry.toString());
    }

    private boolean anonymousCommand(String command, 
    		HttpServletRequest req, HttpServletResponse resp,
    		PrintWriter output,
    		boolean requestIsLocal) {
    	if (command == null) {
    		return(false);
    	}
    	
        if (command.equals("getResourceBundle")) {
		    (new ResourceBundleHandler(req, resp, output)).handleRequest(); 
            return(true);
        }
        
        if (command.equals("visitorFile")) {
		    (new VisitorFileRequestHandler(req, resp, null, output)).handleRequest(); 
    		return(true);
    	}
        
        if (command.equals("login")) {
    		verifyLogin(req, resp, output, requestIsLocal);
    		return(true);
    	}
        
        if (command.equals("silentLogin")) {
    		silentLogin(req, resp, output, requestIsLocal);
    		return(true);
    	}
        
        if (command.equals("registerSelf")) {
            (new SelfRegistrationHandler(req, resp, null, output, null)).handleRequest();
    		return(true);
    	}
        
        if (command.equals("activateUser")) {
            (new ActivateUserRequestHandler(req, resp)).handleRequest();
            return true;
        }
        
        if (command.equals("blank")) {
		    (new BlankPageRequestHandler(req, resp, output)).handleRequest(); 
    		return(true);
    	}
        
        if (command.equals("versionInfo")) {
		    (new VersionInfoRequestHandler(output)).handleRequest(); 
    		return(true);
    	}

        if (command.equals("languages")) {
            (new GetAvailableLanguagesHandler(req, resp, null, output, null)).handleRequest();
            return true;
        }

        if (command.equals("existUser")) {
            (new CheckUserExistHandler(req, resp, null, output, null)).handleRequest();
            return true;
        }
        if (command.equals("checkOpenRegistration")) {
            (new CheckOpenRegistrationHandler(req, resp, null, output, null)).handleRequest();
            return true;
        }

        if (command.equals("skins")) {
            (new GetLayoutSkinsHandler(req, resp, null, output, null)).handleRequest();
            return true;
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
    	
    	if (command.equals("listFiles")) {
    	    String mobile = (String) session.getAttribute("mobile");
    	    if (mobile != null) {
                (new MobileFolderFileListHandler(req, resp, session, output, userid)).handleRequest(); 
                return true;
    	    }
    	    
			int viewMode = Constants.VIEW_MODE_LIST;
        	
        	String viewModeParm = req.getParameter("viewMode");
        	
        	if (viewModeParm != null) {
				try {
					viewMode = Integer.parseInt(viewModeParm);
				} catch (NumberFormatException nfex) {
				}
        	} else {
			    Integer sessionViewMode = (Integer) session.getAttribute("viewMode");
			    if (sessionViewMode != null) {
			    	viewMode = sessionViewMode.intValue();
			    }
            }
        	
        	if (viewMode == Constants.VIEW_MODE_THUMBS) {
    			if (req.getParameter("keepListStatus") == null) {
    				req.setAttribute("initial", "true");
    			}
    		    (new XslThumbnailHandler(req, resp, session, output, userid, requestIsLocal)).handleRequest();
				return(true);
        	} 
        	
        	if (viewMode == Constants.VIEW_MODE_VIDEO) {
			    (new XslVideoListHandler(req, resp, session, output, userid)).handleRequest();
				return(true);
			} 
        	
        	if (viewMode == Constants.VIEW_MODE_STORY) {
				req.setAttribute("initial", "true");
			    (new XslPictureStoryHandler(req, resp, session, output, userid)).handleRequest(); 
				return(true);
			} 
        	
        	if (viewMode == Constants.VIEW_MODE_STATS) {
                (new XslFileListStatsHandler(req, resp, session, output, userid)).handleRequest();
                return(true);
            }
            
			(new XslFileListHandler(req, resp, session, output, userid)).handleRequest();
			
			return(true);
    	}
    	
        if (command.equals("thumbnail")) {
		    (new XslThumbnailHandler(req, resp, session, output, userid, requestIsLocal)).handleRequest(); 
            return(true);
        }
    	
        if (command.equals("listVideos")) {
		    (new XslVideoListHandler(req, resp, session, output, userid)).handleRequest();
            return(true);
        }
    	
        if (command.equals("storyInFrame"))
        {
		    (new XslPictureStoryHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }
        
        if (command.equals("pictureStory"))
        {
		    (new XslPictureStoryOwnWindowHandler(req, resp, session, output, userid)).handleRequest(); 

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
 
        if (command.equals("picThumb"))
        {
  		    (new ThumbnailRequestHandler(req, resp, session, output, userid)).handleRequest(); 
		    
		    return(true);
        }
        
        if (command.equals("videoThumb"))
        {
  		    (new VideoThumbHandler(req, resp, session, output, userid)).handleRequest(); 
		    
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
            else if (method.equals("existFolder"))
            {
                (new AjaxCheckFolderExistHandler(req, resp, session, output, userid)).handleRequest(); 
            }
            
            return(true);
        }
        
    	if (command.equals("testSubdirExist"))
    	{
			(new TestSubdirExistHandler(req, resp, session, output, userid)).handleRequest();

			return(true);
    	}
        
    	if (command.equals("pollForDirChange")) {
			(new PollForDirChangeHandler(req, resp, session, output, userid)).handleRequest();
			return(true);
    	}

    	if (command.equals("pollForFolderTreeChange")) {
			(new PollForFolderTreeChangeHandler(req, resp, session, output, userid)).handleRequest();
			return(true);
    	}
    	
        if (command.equals("fmdelete"))
        {
            (new DeleteFileRequestHandler(req, resp, session, output, userid, requestIsLocal, true)).handleRequest();

            return(true);
        }
        
        if (command.equals("renameFile")) {
            (new RenameFileRequestHandler(req, resp, session, output, userid)).handleRequest();
            return(true);
        }

        if (command.equals("cloneFile"))
        {
            (new CloneFileRequestHandler(req, resp, session, output, userid)).handleRequest();

            return(true);
        }

        if (command.equals("cloneFolder"))
        {
            (new CloneFolderRequestHandler(req, resp, session, output, userid)).handleRequest();

            return(true);
        }

    	if (command.equals("menuBar")) {
		    (new XslMenuBarHandler(req, resp, session, output, userid)).handleRequest();
		    return(true);
    	}
    	
    	if (command.equals("showImg"))
    	{
		    (new XslShowImageHandler(req, resp, session, output, userid)).handleRequest(); 

		    return(true);
    	}

    	if (command.equals("getPicDimensions"))
    	{
		    (new GetPictureDimensionsHandler(req, resp, session, output, userid)).handleRequest(); 

		    return(true);
    	}

    	if (command.equals("bookPicture"))
    	{
		    (new XslAlbumPictureHandler(req, resp, session, output, userid)).handleRequest(); 

		    return(true);
    	}
    	
        if (command.equals("slideShowInFrame"))
        {
            (new XslSlideShowInFrameHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }

        if (command.equals("albumSlideShow"))
        {
            (new XslAlbumSlideShowHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }
        
        if (command.equals("addAlbumComment"))
		{
		    (new AddAlbumCommentHandler(req, resp, session, output, userid)).handleRequest(); 

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
		    (new CreateFileRequestHandler(req, resp, session, output, userid, requestIsLocal)).handleRequest(); 

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
        
        if (command.equals("createBookmark")) {
		    (new CreateBookmarkHandler(req, resp, session, output, userid)).handleRequest();
            return(true);
        }
        
        if (command.equals("delFile"))
        {
            (new DeleteFileHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }
        
        if (command.equals("renamePicture"))
        {
		    (new RenamePictureHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }
        
        if (command.equals("renameImagePrompt"))
        {
		    (new XslRenameImagePromptHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }

        if (command.equals("resizeParms"))
        {
		    (new XslResizeParmsHandler(req, resp, session, output, userid)).handleRequest(); 

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
        if (command.equals("rotateImagePrompt"))
        {
		    (new XmlRotateImagePromptHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }
        if (command.equals("autoImgRotate"))
        {
		    (new AutoImageRotateHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }

        if (command.equals("xformImage"))
        {
		    (new XformImageHandler(req, resp, session, output, userid)).handleRequest(); 

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
        
        if (command.equals("editFile")) {
   			(new XmlLocalEditorHandler(req, resp, session, output, userid)).handleRequest();
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

        if (command.equals("rate")) {
			(new RateVotingHandler(req, resp, session, output, userid)).handleRequest(); 
            return(true);
        }

        if (command.equals("checkTextFileSize")) {
            (new CheckTextFileSizeHandler(req, resp, session, output, userid)).handleRequest();
            return(true);
        }

        if (command.equals("saveRemoteEditor")) {
            (new XmlSaveRemoteEditorHandler(req, resp, session, output, userid)).handleRequest();
            return (true);
        }
        
        if (command.equals("compareImg"))
        {
		    (new CompareImageTabHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }

        if (command.equals("compareImgSlider"))
        {
		    (new CompareImageSliderHandler(req, resp, session, output, userid)).handleRequest(); 

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

        if (command.equals("multiVideoDelete"))
        {
		    (new MultiVideoDeleteHandler(req, resp, session, output, userid, requestIsLocal)).handleRequest(); 

            return(true);
        }
        
        if (command.equals("multiVideoConcat"))
        {
		    (new MultiVideoConcatHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }

        if (command.equals("multiVideoDeshake"))
        {
		    (new MultiVideoDeshakeHandler(req, resp, session, output, userid)).handleRequest(); 

            return(true);
        }
        
        if (command.equals("multiVideoAddSilentAudio"))
        {
		    (new MultiVideoAddSilentAudioHandler(req, resp, session, output, userid)).handleRequest(); 

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
        
        if (command.equals("switchFileAgeColoring"))
        {
            (new SwitchFileAgeColoringHandler(req, resp, session, output, userid)).handleRequest(); 
            
            return(true);
        }

        if (command.equals("getFileDesc"))
        {
            (new GetFileDescriptionHandler(req, resp, session, output, userid)).handleRequest(); 
            
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
        
        if (command.equals("checkUploadConflict"))
        {
			(new CheckUploadConflictHandler(req, resp, session, output, userid)).handleRequest();
			
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

        if (command.equals("checkPasteOverwrite"))
        {
            (new CheckPasteOverwriteHandler(req, resp, session, output, userid)).handleRequest();

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
                    (new XmlFileSizeStatsHandler(req, resp, session, output, userid)).handleRequest(); 
                    
                    return(true);
                }

                if (cmd.equals("typeStats"))
                {
                    (new XmlFileTypeStatsHandler(req, resp, session, output, userid)).handleRequest(); 
                    
                    return(true);
                }

                if (cmd.equals("ageStats"))
                {
                    (new XmlFileAgeStatsHandler(req, resp, session, output, userid)).handleRequest(); 
                    
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
           
		if (command.equals("searchGPS"))
		{
			(new SearchGPSRequestHandler(req, resp, session, output, userid)).handleRequest(); 
            
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

        if (command.equals("resetExifOrientation")) {
            (new ResetExifOrientationHandler(req, resp, session, output, userid)).handleRequest();
            return(true);
        }

        if (command.equals("searchGPSParms")) {
		    (new XslSearchGPSParmsHandler(req, resp, session, output, userid)).handleRequest();
            return(true);
        }
        
        if (command.equals("refresh")) {
            String path = req.getParameter("path");

            SubdirExistCache.getInstance().cleanupExistSubdir(path);
	        SubdirExistTester.getInstance().queuePath(path, 1, true);
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException iex) {
            }

            req.setAttribute("expand", path);
            
            if (File.separatorChar == '/') {
    			(new XslUnixDirTreeHandler(req, resp, session, output, userid, requestIsLocal)).handleRequest();
    		} else {
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

		if (command.equals("cutAudioParams"))
		{
			(new CutAudioParamHandler(req, resp, session, output, userid)).handleRequest();
			return(true);
		}

		if (command.equals("cutAudio"))
		{
			(new CutAudioHandler(req, resp, session, output, userid)).handleRequest();
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
        
        if (command.equals("multiDownload"))
        {
        	(new MultiFileDownloadHandler(req, resp, session, output, userid)).handleRequest();

            return(true);
        }

        if (command.equals("multiImgDownload"))
        {
        	(new MultiImageDownloadHandler(req, resp, session, output, userid)).handleRequest();

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

		if (command.equals("googleMapMulti"))
        {
		    (new XslGoogleMapMultiHandler(req, resp, session, output, userid)).handleRequest(); 
        	
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
		    (new XslPublishListHandler(req, resp, session, output, userid)).handleRequest(); 

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
			(new XslUserSettingsHandler(req, resp, session, output, userid, null)).handleRequest();
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
			(new XslDriveInfoRequestHandler(req, resp, session, output, userid)).handleRequest();
			
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

        if (command.equals("viewGPX")) {
            (new GPXViewHandler(req, resp, session, output, userid)).handleRequest();
            return(true);
        }

        if (command.equals("multiGPX")) {
            (new MultiGPXTrackHandler(req, resp, session, output, userid)).handleRequest();
            return(true);
        }

        if (command.equals("gpxTrack")) {
            (new GPXTrackHandler(req, resp, session, output, userid)).handleRequest();
            return(true);
        }
        
        if (command.equals("gpxWayPoints")) {
            (new GPXWayPointHandler(req, resp, session, output, userid)).handleRequest();
            return(true);
        }

        if (command.equals("playVideoLocal")) {
            (new VideoLocalPlayerHandler(req, resp, session, output, userid, requestIsLocal)).handleRequest();
            return(true);
        }
        
        if (WebFileSysConfig.getInstance().getFfmpegExePath() != null) {
        	
            if (command.equals("video")) {
            	String cmd = req.getParameter("cmd");

            	if (cmd.equals("getVideoDimensions")) {
        		    (new GetVideoDimensionsHandler(req, resp, session, output, userid)).handleRequest(); 
        		    return(true);
            	}
            	
                if (cmd.equals("editVideoParams")) {
        		    (new EditVideoParamHandler(req, resp, session, output, userid)).handleRequest(); 
                    return(true);
                }

                if (cmd.equals("slideshowVideoParams")) {
        		    (new SlideshowToVideoParamHandler(req, resp, session, output, userid)).handleRequest(); 
                    return(true);
                }

                if (cmd.equals("editConvertVideo")) {
        		    (new EditConvertVideoHandler(req, resp, session, output, userid)).handleRequest(); 
                    return(true);
                }
                
                if (cmd.equals("extractVideoFrameParams")) {
        		    (new ExtractVideoFrameParamHandler(req, resp, session, output, userid)).handleRequest(); 
                    return(true);
                }
                
                if (cmd.equals("extractVideoFrame")) {
        		    (new ExtractVideoFrameHandler(req, resp, session, output, userid)).handleRequest(); 
                    return(true);
                }
                
                if (cmd.equals("previewFrame")) {
        		    (new VideoFramePreviewHandler(req, resp, session, output, userid)).handleRequest(); 
                    return(true);
                }

                if (cmd.equals("renameVideo")) {
                    (new RenameVideoRequestHandler(req, resp, session, output, userid, requestIsLocal)).handleRequest();
                    return(true);
                }

                if (cmd.equals("addAudioToVideo")) {
                    (new AddAudioToVideoHandler(req, resp, session, output, userid)).handleRequest();
                    return(true);
                }

                if (cmd.equals("multiImgToVideo")) {
        		    (new SlideshowToVideoHandler(req, resp, session, output, userid)).handleRequest(); 
                    return(true);
                }

                if (cmd.equals("deshakeVideo")) {
        		    (new DeshakeVideoHandler(req, resp, session, output, userid)).handleRequest(); 
                    return(true);
                }

                if (cmd.equals("textOnVideoParams")) {
        		    (new TextOnVideoParamHandler(req, resp, session, output, userid)).handleRequest(); 
                    return(true);
                }

                if (cmd.equals("textOnVideo")) {
        		    (new TextOnVideoHandler(req, resp, session, output, userid)).handleRequest(); 
                    return(true);
                }

                if (cmd.equals("fadeAudioParams")) {
        		    (new VideoFadeAudioParamHandler(req, resp, session, output, userid)).handleRequest(); 
                    return(true);
                }

                if (cmd.equals("fadeAudio")) {
        		    (new VideoFadeAudioHandler(req, resp, session, output, userid)).handleRequest(); 
                    return(true);
                }

                if (cmd.equals("addSilentAudio")) {
        		    (new VideoAddSilentAudioHandler(req, resp, session, output, userid)).handleRequest(); 
                    return(true);
                }

                if (cmd.equals("join"))
                {
        		    (new AnyVideoConcatParamHandler(req, resp, session, output, userid)).handleRequest(); 
                    return(true);
                }

                if (cmd.equals("multiVideoJoin"))
                {
        		    (new AnyVideoConcatHandler(req, resp, session, output, userid)).handleRequest(); 
                    return(true);
                }
                
            	if (cmd.equals("videoDurationSum")) {
        		    (new VideoDurationSumHandler(req, resp, session, output, userid)).handleRequest(); 
        		    return(true);
            	}
            }
        }

        if (command.equals("mobile"))
        {
            String cmd = req.getParameter("cmd");

            if (cmd.equals("folderFileList")) 
            {
                (new MobileFolderFileListHandler(req, resp, session, output, userid)).handleRequest(); 
                return(true);
            }

            if (cmd.equals("folderPictures")) 
            {
                (new MobileFolderPictureHandler(req, resp, session, output, userid)).handleRequest(); 
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
        
        if (command.equals("extractDescriptions"))
		{
			(new XslThumbnailExtractDescriptionHandler(req, resp, session, output, userid)).handleRequest();
            
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

		if (WebFileSysConfig.getInstance().isAutoCreateThumbs())
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

        if (WebFileSysConfig.getInstance().getLogoutURL() != null)
        {
            logoutPage = WebFileSysConfig.getInstance().getLogoutURL();
        }

        LOG.info(req.getRemoteAddr() + ": logout user " + userid);
        
        try
        {
            resp.sendRedirect(logoutPage);
        }
        catch (IOException ioex)
        {
        	LOG.warn(ioex);
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
                destroyExistingSession(req);

        		session = req.getSession(true);
        		
        		setSessionInfo(req, session);

        		session.setAttribute("userid", userid);
        		
        		session.setAttribute(Constants.SESSION_KEY_LOGIN_EVENT, "true");
        		
                String browserType = req.getHeader("User-Agent");
                
        		String role = userMgr.getRole(userid);
        		
        		if ((role != null) && role.equals("album"))
        		{
        			(new XslPictureAlbumHandler(req, resp, session, output, userid)).handleRequest();
        			
        		}
        		else
        		{
        			int screenWidth = 100000;
        			String screenWithParam = req.getParameter("screenWidth");
        			if (!CommonUtils.isEmpty(screenWithParam)) {
        				try {
        					screenWidth = Integer.parseInt(screenWithParam);
        				} catch (Exception ex) {
        				}
        			}
        			
        		    if ((screenWidth < MIN_SCREEN_WIDTH_FOR_DESKTOP_VERSION) && isMobileClient(browserType)) 
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
    			
                LOG.info(logEntry);

                if ((WebFileSysConfig.getInstance().getMailHost() != null) && WebFileSysConfig.getInstance().isMailNotifyLogin())
                {
                	ArrayList<String> adminUserEmailList = userMgr.getAdminUserEmails();
                    
                    (new SmtpEmail(adminUserEmailList,
                               "login successful",
                               WebFileSys.getInstance().getLogDateFormat().format(new Date()) + " " + logEntry)).send();
                }

                return;
            }

            if (userMgr.checkReadonlyPassword(userid, password))
            {
                destroyExistingSession(req);
            	
        		session = req.getSession(true);

        		setSessionInfo(req, session);

        		session.setAttribute("userid", userid);
        		
        		session.setAttribute(Constants.SESSION_KEY_LOGIN_EVENT, "true");

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

                LOG.info(logEntry);

                if ((WebFileSysConfig.getInstance().getMailHost() != null) && WebFileSysConfig.getInstance().isMailNotifyLogin())
                {
                	ArrayList<String> adminUserEmailList = userMgr.getAdminUserEmails();
                    
                    (new SmtpEmail(adminUserEmailList,
                                   "login successful",
                                   WebFileSys.getInstance().getLogDateFormat().format(new Date()) + " " + logEntry)).send();
                }

                return;
            }
        }

        logEntry = clientIP + ": login failed for user " + userid;
        LOG.warn(logEntry);

        if ((WebFileSysConfig.getInstance().getMailHost() != null) && WebFileSysConfig.getInstance().isMailNotifyLogin())
        {
        	ArrayList<String> adminUserEmailList = userMgr.getAdminUserEmails();
            
            (new SmtpEmail(adminUserEmailList,
                           "login failed",
                           WebFileSys.getInstance().getLogDateFormat().format(new Date()) + " " + logEntry)).send();
        }

        if (WebFileSysConfig.getInstance().getLoginErrorPage() != null)
        {
        	try
        	{
                resp.sendRedirect(WebFileSysConfig.getInstance().getLoginErrorPage());
        	}
        	catch (IOException ioex)
        	{
        		LOG.warn(ioex);
        	}
        	
            return;
        }

	    (new XslLogonHandler(req, resp, session, output, true)).handleRequest();
    }
    
    public void silentLogin(HttpServletRequest req, HttpServletResponse resp,
    		                PrintWriter output, boolean requestIsLocal) {

    	String requestParms = req.getQueryString();
    	StringTokenizer paramParser = new StringTokenizer(requestParms, "?&=");
    	if (paramParser.hasMoreTokens()) {
    		paramParser.nextToken(); // skip command param name
    	}
        if (paramParser.hasMoreTokens()) {
    		paramParser.nextToken(); // skip the command value (silentLogin)
    	}

    	String userid = null;
    	if (paramParser.hasMoreTokens()) {
    		userid = paramParser.nextToken();
    	}

        String password = null;
    	if (paramParser.hasMoreTokens()) {
    		password = paramParser.nextToken();
    	}

        String redirectURL = extractRedirectAfterSilentLoginUrl(req, paramParser);

        UserManager userMgr = WebFileSys.getInstance().getUserMgr();
        String clientIP = req.getRemoteAddr();
        HttpSession session = null;

        if ((userid != null) && (password != null)) {
            boolean authSuccess = false;
            if (userMgr.checkPassword(userid, password)) {
                session = handleSilentLoginSuccess(userMgr, req, resp, userid, clientIP,false);
                authSuccess = true;
            } else if (userMgr.checkReadonlyPassword(userid, password)) {
                session = handleSilentLoginSuccess(userMgr, req, resp, userid, clientIP,true);
                authSuccess = true;
            }

            if (authSuccess) {
                if (!redirectURL.isEmpty()) {
                    try {
                        resp.sendRedirect(redirectURL);
                        return;
                    } catch (IOException ioex) {
                        LOG.error(ioex);
                    }
                }

                String role = userMgr.getRole(userid);
                if ("album".equals(role)) {
                    (new XslPictureAlbumHandler(req, resp, session, output, userid)).handleRequest();
                } else {
                    (new MainFrameSetHandler(req, resp, session, output, userid, requestIsLocal)).handleRequest();
                }
                return;
            }
        }

        String logEntry = clientIP + ": silent login failed for user " + userid;
        LOG.warn(logEntry);

        notifyAdminUsersAboutLogin(userMgr, "silent login failed", logEntry);

        if (WebFileSysConfig.getInstance().getLoginErrorPage() != null) {
        	try {
                resp.sendRedirect(WebFileSysConfig.getInstance().getLoginErrorPage());
        	} catch (IOException ioex) {
        		LOG.warn(ioex);
        	}
            return;
        }

        // silent login failed - goto login page
	    (new XslLogonHandler(req, resp, session, output, true)).handleRequest();
    }

    private String extractRedirectAfterSilentLoginUrl(HttpServletRequest req, StringTokenizer parmParser) {
        StringBuilder redirectURL = new StringBuilder();
        if (parmParser.hasMoreTokens()) {
            // execute on login command exists
            parmParser.nextToken(); // skip the cmd value

            StringBuilder executeOnLoginCmd = new StringBuilder();

            executeOnLoginCmd.append("command");

            int i = 0;
            while (parmParser.hasMoreTokens()) {
                if (i % 2 == 0) {
                    executeOnLoginCmd.append('=');
                } else {
                    executeOnLoginCmd.append('&');
                }
                executeOnLoginCmd.append(parmParser.nextToken());
                i++;
            }

            String originalURL = req.getRequestURI();
            redirectURL.append(originalURL);
            redirectURL.append('?');
            redirectURL.append(executeOnLoginCmd);
        }
        return redirectURL.toString();
    }

    private HttpSession handleSilentLoginSuccess(UserManager userMgr, HttpServletRequest req, HttpServletResponse resp,
                                          String userid, String clientIP, boolean readonly) {
        destroyExistingSession(req);

        HttpSession session = req.getSession(true);
        setSessionInfo(req, session);
        session.setAttribute("userid", userid);
        session.setAttribute(Constants.SESSION_KEY_LOGIN_EVENT, "true");
        session.removeAttribute(Constants.SESSION_KEY_CWD);
        session.removeAttribute("startIdx");
        if (readonly) {
            session.setAttribute("readonly", "true");
        }

        logSilentLogin(userMgr, req, userid, clientIP, readonly);

        userMgr.setLastLoginTime(userid, new Date());
        return session;
    }

    private void destroyExistingSession(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null) {
            LOG.debug("destroying existing session");
            session.invalidate();
        }
    }

    private void logSilentLogin(UserManager userMgr, HttpServletRequest req, String userid, String clientIP, boolean readonly) {
        StringBuilder logEntry = new StringBuilder();
        logEntry.append(clientIP);
        logEntry.append(": silent login user ");
        logEntry.append(userid);
        if (readonly) {
            logEntry.append(" (read-only)");
        }

        String browserType = req.getHeader("User-Agent");
        if (browserType != null) {
            logEntry.append(" [");
            logEntry.append(browserType);
            logEntry.append("]");
        }

        LOG.info(logEntry.toString());

        String subject = "silent login successful";
        if (readonly) {
            subject = subject + " (read-only)";
        }
        notifyAdminUsersAboutLogin(userMgr, subject, logEntry.toString());
    }

    private void notifyAdminUsersAboutLogin(UserManager userMgr, String subject, String message) {
        if (WebFileSysConfig.getInstance().getMailHost() != null && WebFileSysConfig.getInstance().isMailNotifyLogin()) {
            ArrayList<String> adminUserEmailList = userMgr.getAdminUserEmails();
            (new SmtpEmail(adminUserEmailList, subject,
                    WebFileSys.getInstance().getLogDateFormat().format(new Date()) + " " + message)).send();
        }

    }

    private void redirectToLogin(PrintWriter output) {
		output.println("<html>");
		output.println("<head>");
		output.println("<meta http-equiv=\"expires\" content=\"0\">");
		output.println("<script type=\"text/javascript\">");
		output.println("top.location.href='/webfilesys/servlet?command=loginForm';");
		output.println("</script>");
		output.println("</head>");
		output.println("</html>");
		output.flush();
    }
    
    private boolean isMobileClient(String browserType) {
        if (browserType == null) {
            return false;
        }
        return (browserType.contains("Android"));
    }
}


