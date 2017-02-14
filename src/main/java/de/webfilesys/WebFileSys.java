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
package de.webfilesys;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import javax.mail.Session;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;

import de.webfilesys.calendar.AppointmentManager;
import de.webfilesys.decoration.DecorationManager;
import de.webfilesys.user.UserManager;
import de.webfilesys.user.XmlUserManager;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.watch.FolderWatchManager;

/**
 * Container for configuration settings.
 */
public class WebFileSys
{
	private static WebFileSys instance = null;

	public static final String VERSION = "Version 2.16.0-beta1 (14 Feb 2017)";
 
    public static final String THUMB_DIR = "thumbnails";

    public static final String DEFAULT_MAIL_SENDER_ADDRESS = "WebFileSys@nowhere.com";

    public static final String DEFAULT_MAIL_SENDER_NAME = "WebFileSys";

    public static final String LOG_CONFIG_FILE = "LogConfig.xml";

    public static final int OS_OS2 = 1;
    public static final int OS_WIN = 2;
    public static final int OS_AIX = 3;
    public static final int OS_LINUX = 4;
    public static final int OS_SOLARIS = 5;
    public static final int OS_UNKNOWN = 9;

    private static final String LOOPBACK_ADDRESS = "127.0.0.1";

    // default upload limit: 128 MBytes
    private static final long DEFAULT_UPLOAD_LIMIT = (128l * 1024l * 1024l);

    /** folder watch interval in minutes */
    private static final int DEFAULT_FOLDER_WATCH_INTERVAL = 24 * 60;
    
    private static long DEFAULT_DISK_QUOTA = (1024l * 1024l);
    
	public static final int DEFAULT_TEXT_FILE_MAX_LINE_LENGTH = 2048;
    
	/** maximum number of appointment e-mails that can be sent in one hour */
	public static final int DEFAULT_MAX_APP_MAILS_PER_HOUR = 200;
	
	/** default expiration period in days for non-repeated appointments */
	private static final int DEFAULT_CAL_EXPIRATION_PERIOD = 365;
	
    private String webAppRootDir = null;
    
    private String configBaseDir = null;
    
    private String opSysName;
    private int opSysType;
    private String localHostName;
    private String localIPAddress = null;

    private int slideShowDelay;

    private String primaryLanguage = null;

    private UserManager userMgr = null;
    
    private String systemEditor;
    private String javaVersion;

    private int thumbnailsPerPage = 12;

    private boolean oldLinuxPsStyle;

    private boolean autoCreateThumbs = false;

    private boolean autoExtractMP3 = false;

    private boolean thumbThreadRunning = false;

    private boolean allowProcessKill = true;
    
    private boolean syncIgnoreOffsetDST = false;

    private boolean openRegistration = false;

    private boolean showAssignedIcons = false;

    private boolean showDescriptionsInline = false;

    private boolean downloadStatistics = false;

    private boolean debugMail = false;
    
    private Session mailSession = null;
    
    private boolean mailNotifyLogin = false;

    private boolean mailNotifyRegister = false;

    private boolean mailNotifyWelcome = false;
    
    private boolean enableFolderWatch = false;
    
    /** folder watch interval in minutes */
    private int folderWatchInterval = DEFAULT_FOLDER_WATCH_INTERVAL;

    private String mailHost = null;
    
    private boolean smtpAuth = false;

    private boolean smtpSecure = false;
    
    private String smtpUser = null;

    private String smtpPassword = null;

    private ArrayList<String> adminEmailList = null;

    private String mailSenderAddress = null;

    private String mailSenderName = null;

	// emergency brake to prevent sending uncontrolled numbers of mails if something unexpected happens
    private int maxAppointmentMailsPerHour = DEFAULT_MAX_APP_MAILS_PER_HOUR;
    
    private String clientUrl = null;
    
    /** the fully qualified server DNS name, if different from localhost DNS */
    private String serverDNS = null;

    private String userDocRoot = null;

    private String logoutURL = null;

    private String loginErrorPage = null;

    private boolean maintananceMode = false;
    
	private String userMgrClass = null;

	/** simulate remote client connections from the local host */
    private boolean simulateRemote = false;
    
    private boolean mailNotifyQuotaAdmin = false;
    private boolean mailNotifyQuotaUser = false;

    private boolean enableDiskQuota = false;
    
    private boolean enableCalendar = false;
    
    private int calendarExpirationPeriod = DEFAULT_CAL_EXPIRATION_PERIOD;

    private int diskQuotaCheckHour = 3;
    
    private long uploadLimit = DEFAULT_UPLOAD_LIMIT;
    
    private int textFileMaxLineLength = DEFAULT_TEXT_FILE_MAX_LINE_LENGTH;
    
    /** 
     * allow chmod/chown for users of role webspace
     * on special request from chat3you
     */
    private boolean chmodAllowed = false;
    
    /**
     * Are backward links from the linked file to the linking file enabeld?
     * Linking backward from the link target file to the link is required to
     * allow automatic update of the link when the target file is moved so that the
     * link points to the new target path after the move operation.
     * Reverse linking has some disadvantages too: 
     * A metainf file is created in the folder containing the link taget file (if it not already exists).
     * And there is some additional processing required for move operations (performance!).
     */
    private boolean reverseFileLinkingEnabled = false;

    private DiskQuotaInspector quotaInspector = null;

    private boolean licensed = false;

    private SimpleDateFormat logDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        
	private DocumentBuilderFactory docFactory = null;
	
	public static WebFileSys getInstance()
	{
	    return(instance);
    }
	
	public static WebFileSys createInstance(Properties configProps, String configBaseDir)
	{
		if (instance != null)
		{
			return(instance);
		}
		
		instance = new WebFileSys(configProps, configBaseDir);
		
		return(instance);
    }
	
	private WebFileSys(Properties config, String webAppRootDir)
	{
        Logger.getLogger(getClass()).info("starting WebFileSys " + VERSION);      
	    
		this.webAppRootDir = webAppRootDir;
		
		if (webAppRootDir.endsWith("\\") || webAppRootDir.endsWith("/"))
		{
			this.configBaseDir = webAppRootDir + "WEB-INF";
		}
		else
		{
			this.configBaseDir = webAppRootDir + "/WEB-INF";
		}
		
        javaVersion = System.getProperty("java.version");

        Logger.getLogger(getClass()).info("java version : " + javaVersion);
		
        opSysName = System.getProperty("os.name");
        
        Logger.getLogger(getClass()).info("operating system : " + opSysName);

        if (opSysName.startsWith("OS/2"))
        {
            opSysType = OS_OS2;
        }
        else if (opSysName.startsWith("Win"))
        {
            opSysType = OS_WIN;
        }
        else if (opSysName.startsWith("AIX"))
        {
            opSysType = OS_AIX;
        }
        else if (opSysName.startsWith("Linux"))
        {
            opSysType = OS_LINUX;
        }
        else if ((opSysName.startsWith("Solaris")) || (opSysName.startsWith("SunOS")))
        {
            opSysType = OS_SOLARIS;
        }
        else
        {
            opSysType = OS_UNKNOWN;
        }

		docFactory = DocumentBuilderFactory.newInstance();
        
        String thumbNumString = config.getProperty("PageThumbnailNumber", "12");
        try
        {
            thumbnailsPerPage = Integer.parseInt(thumbNumString);
        }
        catch (NumberFormatException nfe)
        {
        	Logger.getLogger(getClass()).error("invalid config parameter PageThumbnailNumber: " + nfe);
        	thumbnailsPerPage = 12;
        }

        oldLinuxPsStyle = false;
        String linuxPsStyle = config.getProperty("LinuxPsStyle", "new").toLowerCase();
        if (linuxPsStyle.equals("old"))
        {
            oldLinuxPsStyle = true;
        }

        openRegistration = false;
        String temp = config.getProperty("RegistrationType", "closed");
        if (temp.equalsIgnoreCase("open"))
        {
            openRegistration = true;
            Logger.getLogger(getClass()).info("registration: open");
        }
        else
        {
            Logger.getLogger(getClass()).info("registration: closed");
        }

        showDescriptionsInline = false;
        temp = config.getProperty("ShowDescriptionsInline", "true");
        if (temp.equalsIgnoreCase("true"))
        {
            showDescriptionsInline = true;
        }

        showAssignedIcons = false;
        temp = config.getProperty("ShowAssignedIcons", "true");
        if (temp.equalsIgnoreCase("true"))
        {
            showAssignedIcons = true;
        }

        userMgrClass = config.getProperty("UserManagerClass");


        userDocRoot = config.getProperty("UserDocumentRoot");

        if (openRegistration)
        {
            if (userDocRoot != null)
            {
                File docRootFile = new File(userDocRoot);

                if ((!docRootFile.exists())
                    || (!docRootFile.isDirectory())
                    || (!docRootFile.canWrite()))
                {
                	Logger.getLogger(getClass()).error("UserDocumentRoot is not a writable directory: "+ userDocRoot);
                    userDocRoot = null;
                }
                else
                {
                    if ((File.separatorChar == '\\')
                        && (userDocRoot.length() > 2))
                    {
                        try
                        {
                            String canonicalRoot =
                                docRootFile.getCanonicalPath().substring(2);
                            String absoluteRoot =
                                docRootFile.getAbsolutePath().substring(2);

                            if (!canonicalRoot.equals(absoluteRoot))
                            {
                            	Logger.getLogger(getClass()).error("UserDocumentRoot is not a writable directory (check uppercase/lowercase!): " + userDocRoot);
                                userDocRoot = null;
                            }
                        }
                        catch (IOException ioex)
                        {
                        }

                    }
                }

                if (userDocRoot != null)
                {
                    Logger.getLogger(getClass()).info("User Document Root: " + userDocRoot);
                }
            }

            if (userDocRoot == null)
            {
            	userDocRoot = configBaseDir + File.separator + "userhome";

                Logger.getLogger(getClass()).info(
                    "using default UserDocumentRoot for open registration: "
                        + userDocRoot);
            }
        }

        temp = config.getProperty("UploadLimit");
        
        if (temp!=null)
        {
        	try
        	{
        		uploadLimit = Long.parseLong(temp);
        	}
        	catch (NumberFormatException nfex)
        	{
                Logger.getLogger(getClass()).warn("invalid upload limit ignored: " + temp);        		
        	}
        }
        
        temp = config.getProperty("TextFileMaxLineLength");
        
        if (temp != null)
        {
        	try
        	{
        		textFileMaxLineLength = Integer.parseInt(temp);
        	}
        	catch (NumberFormatException nfex)
        	{
                Logger.getLogger(getClass()).warn("invalid upload limit ignored: " + temp);        		
        	}
        }
        
        downloadStatistics = false;
        temp = config.getProperty("EnableDownloadStatistics", "false");
        if (temp.equalsIgnoreCase("true"))
        {
            downloadStatistics = true;
        }

        autoCreateThumbs = false;
        temp = config.getProperty("AutoCreateThumbnails", "false");
        if (temp.equalsIgnoreCase("true") || temp.equalsIgnoreCase("yes"))
        {
            autoCreateThumbs = true;
        }

        autoExtractMP3 = false;
        temp = config.getProperty("AutoExtractMP3Tags", "false");
        if (temp.equalsIgnoreCase("true") || temp.equalsIgnoreCase("yes"))
        {
            autoExtractMP3 = true;
        }

        if (openRegistration)
        {
            temp = config.getProperty("DiskQuotaDefaultMB", "1");

            try
            {
                int diskQuotaMB = Integer.parseInt(temp);

                DEFAULT_DISK_QUOTA = ((long) diskQuotaMB) * 1024l * 1024l;
            }
            catch (NumberFormatException nfex)
            {
            	Logger.getLogger(getClass()).error(
                    "invalid default disk quota value: "
                        + temp
                        + " - using default value " + DEFAULT_DISK_QUOTA);
            }
        }

        logoutURL = config.getProperty("LogoutPageURL");

        loginErrorPage = config.getProperty("LoginErrorURL");

        mailHost = config.getProperty("SmtpMailHost");

        if ((mailHost != null) && (mailHost.trim().length() > 0))
        {
        	Logger.getLogger(getClass()).info("SMTP mail host: " + mailHost);
        	
        	temp = config.getProperty("SmtpAuth");
        	
        	smtpAuth = (temp != null) && temp.equalsIgnoreCase("true");
        	
        	if (smtpAuth) 
        	{
        		smtpUser = config.getProperty("SmtpUser");
        		if (CommonUtils.isEmpty(smtpUser))
        		{
        			Logger.getLogger(getClass()).error("SmtpUser property is required if SmtpAuth=true");
        		}

        		smtpPassword = config.getProperty("SmtpPassword");
        		
        		if (CommonUtils.isEmpty(smtpPassword))
        		{
        			Logger.getLogger(getClass()).error("smtpPassword property is required if SmtpAuth=true");
        		}
        	}

        	temp = config.getProperty("SmtpSecure");
        	
        	smtpSecure = (temp != null) && temp.equalsIgnoreCase("true");
        	
            mailSenderAddress =
                config.getProperty(
                    "MailSenderAddress",
                    DEFAULT_MAIL_SENDER_ADDRESS);

            mailSenderName =
                config.getProperty("MailSenderName", DEFAULT_MAIL_SENDER_NAME);

            mailNotifyLogin = false;
            temp = config.getProperty("MailNotification.login", "false");
            if (temp.equalsIgnoreCase("true"))
            {
                mailNotifyLogin = true;
            }

            mailNotifyRegister = false;
            temp = config.getProperty("MailNotification.registration", "false");
            if (temp.equalsIgnoreCase("true"))
            {
                mailNotifyRegister = true;
            }

            mailNotifyWelcome = false;
            temp = config.getProperty("MailNotification.welcome", "false");
            if (temp.equalsIgnoreCase("true"))
            {
                mailNotifyWelcome = true;
            }

            clientUrl = config.getProperty("ClientURL");

        }

        serverDNS = config.getProperty("serverDNS");

        temp = config.getProperty("EnableDiskQuota", "false");

        if (temp.equalsIgnoreCase("true"))
        {
            enableDiskQuota = true;

            Logger.getLogger(getClass()).info("disk quota enabled");

            diskQuotaCheckHour = 3;

            temp = config.getProperty("DiskQuotaCheckHour", "3");

            try
            {
                diskQuotaCheckHour = Integer.parseInt(temp);
            }
            catch (NumberFormatException nfex)
            {
            	Logger.getLogger(getClass()).error("invalid DiskQuotaCheckHour: " + temp);
            }

            mailNotifyQuotaAdmin = false;

            temp = config.getProperty("DiskQuotaNotifyAdmin", "false");

            if (temp.equalsIgnoreCase("true"))
            {
                mailNotifyQuotaAdmin = true;
            }

            mailNotifyQuotaUser = false;

            temp = config.getProperty("DiskQuotaNotifyUser", "false");

            if (temp.equalsIgnoreCase("true"))
            {
                mailNotifyQuotaUser = true;
            }
        }
        else
        {
        	Logger.getLogger(getClass()).info("disk quota disabled");
        }

        systemEditor = config.getProperty("SystemEditor");

        allowProcessKill = true;

        temp = config.getProperty("AllowProcessKill", "true");
        if (temp.equalsIgnoreCase("no") || temp.equalsIgnoreCase("false"))
        {
            allowProcessKill = false;
        }

		temp = config.getProperty("DebugMail", "false");
		if (temp.equalsIgnoreCase("true"))
		{
			debugMail = true;
		}

		temp = config.getProperty("EnableCalendar", "false");
		if (temp.equalsIgnoreCase("true"))
		{
			enableCalendar = true;

			temp = config.getProperty("MaxAppointmentMailsPerHour", null);
	        if (!CommonUtils.isEmpty(temp))
	        {
	            try
	            {
	            	maxAppointmentMailsPerHour = Integer.parseInt(temp);
	            }
	            catch (NumberFormatException numEx)
	            {
	            	Logger.getLogger(getClass()).error("invalid property value for MaxAppointmentMailsPerHour: " + temp);
	            	maxAppointmentMailsPerHour = DEFAULT_MAX_APP_MAILS_PER_HOUR;
	            }
	        }
        	Logger.getLogger(getClass()).debug("maximum allowed appointment e-mails per hour: " + maxAppointmentMailsPerHour);

		    temp = config.getProperty("AppointmentExpirationDays", null);
		    if (!CommonUtils.isEmpty(temp))
		    {
	            try
	            {
	                calendarExpirationPeriod = Integer.parseInt(temp);
	            }
	            catch (NumberFormatException numEx)
	            {
	            	Logger.getLogger(getClass()).error("invalid property value for AppointmentExpirationDays: " + temp);
	            	calendarExpirationPeriod = DEFAULT_CAL_EXPIRATION_PERIOD;
	            }
		    }
		}
		
        temp = config.getProperty("SyncIgnoreOffsetDST", "false");
        if (temp.equalsIgnoreCase("true"))
        {
            syncIgnoreOffsetDST = true;
        }
		
        String slideShowString = config.getProperty("SlideshowDelay", "5");
        try
        {
            slideShowDelay = Integer.parseInt(slideShowString);
        }
        catch (NumberFormatException nfe)
        {
            slideShowDelay = 10;
        }

        temp = config.getProperty("EnableFolderWatch", "false");
        if (temp.equalsIgnoreCase("true"))
        {
            enableFolderWatch = true;
            
            String watchIntervalString = config.getProperty("FolderWatchInterval", "1440");
            try
            {
                folderWatchInterval = Integer.parseInt(watchIntervalString);
            }
            catch (NumberFormatException nfe)
            {
                folderWatchInterval = 1440;
            }
        }
        
		temp = config.getProperty("SimulateRemote");
		
		if ((temp != null) && temp.equalsIgnoreCase("true"))
		{
			// for testing remote access and role webspace
			
		    simulateRemote = true;
		}
		
		chmodAllowed = false;
		temp = config.getProperty("ChmodWebspace", "false");
		if (temp.equalsIgnoreCase("true") || temp.equalsIgnoreCase("yes"))
		{
			chmodAllowed = true;
		}

        reverseFileLinkingEnabled = false;
        temp = config.getProperty("ReverseFileLinkingEnabled", "false");
        if (temp.equalsIgnoreCase("true") || temp.equalsIgnoreCase("yes"))
        {
            reverseFileLinkingEnabled = true;
        }
		
        String local_full = null;

        try
        {
            InetAddress localHost = InetAddress.getLocalHost();
            local_full = localHost.toString();
            localIPAddress = localHost.getHostAddress();
            Logger.getLogger(getClass()).info("local ip address : " + local_full);
            localHostName = localHost.getHostName();
        }
        catch (Exception e)
        {
        	Logger.getLogger(getClass()).error(e);
            try
            {
                localHostName = InetAddress.getLocalHost().toString();
            }
            catch (Exception o)
            {
            	Logger.getLogger(getClass()).error(o);
                localHostName = "cannot query host name";
            }
        }

        primaryLanguage =
            config.getProperty(
                "primaryLanguage",
                LanguageManager.DEFAULT_LANGUAGE);

        Logger.getLogger(getClass()).info("primary language: " + primaryLanguage);

        if (File.separatorChar == '/')
        {
        	SubdirExistCache.getInstance().setExistsSubdir("/", new Integer(1));
        }

        SubdirExistCache.getInstance().initialReadSubdirs(opSysType);
    }

    public void initialize(Properties config)
    {
        if ((userMgrClass == null) || (userMgrClass.trim().length() == 0))
        {
            userMgr = new XmlUserManager();
        }
        else
        {
            try
            {
                userMgr =
                    (UserManager) Class
                        .forName(this.userMgrClass)
                        .newInstance();

                Logger.getLogger(getClass()).info("User Manager class: " + this.userMgrClass);
            }
            catch (ClassNotFoundException cnfex)
            {
            	Logger.getLogger(getClass()).error(
                    "the user manager class "
                        + userMgrClass
                        + " cannot be found: "
                        + cnfex);
            }
            catch (InstantiationException instEx)
            {
            	Logger.getLogger(getClass()).error(
                    "the user manager cannot be instantiated: " + instEx);
            }
            catch (IllegalAccessException iaEx)
            {
            	Logger.getLogger(getClass()).error(
                    "the user manager cannot be instantiated: " + iaEx);
            }
            catch (ClassCastException cex)
            {
            	Logger.getLogger(getClass()).error(
                    "the class "
                        + userMgrClass
                        + " does not implement the UserManager interface: "
                        + cex);
            }
        }

        if ((mailHost != null) && (mailHost.trim().length() > 0))
        {
            adminEmailList = userMgr.getAdminUserEmails();

            if (adminEmailList.size() == 0)
            {
            	Logger.getLogger(getClass()).warn("no admin e-mail address available for event notification");
            }
        }
        
        LanguageManager.getInstance(primaryLanguage).listAvailableLanguages();

        if (File.separatorChar == '\\')
        {
            WinDriveManager.getInstance();
        }
        
        readDateFormats(config);
        
        if (mailHost != null)
        {
            InvitationManager.getInstance();
        }

        ViewHandlerManager.getInstance();
        
        if (enableDiskQuota)
        {
            quotaInspector = new DiskQuotaInspector();

            quotaInspector.start();
        }
        
        if (enableFolderWatch) 
        {
            FolderWatchManager.getInstance();
        }
        
        DecorationManager.getInstance();
        
        if ((mailHost != null) && (mailHost.trim().length() > 0))
        {
            initMailSession();        
        }

        if (enableCalendar) 
        {
            AppointmentManager.getInstance();
        }
    }
    
    private void initMailSession() {
    	Properties mailProps = new Properties();
        
    	mailProps.put("mail.transport.protocol", "smtp");
    	mailProps.put("mail.smtp.host", getMailHost());        
            
    	mailProps.put("mail.smtp.starttls.enable", isSmtpSecure());
        
    	mailProps.put("mail.smtp.auth", isSmtpAuth());      
    	
        if (getSmtpUser() != null) 
        {
    		mailProps.put("mail.smtp.user", getSmtpUser());        
        }
    	
        mailSession = Session.getInstance(mailProps, null);

        if (isDebugMail()) 
        {
            mailSession.setDebug(true);
        }
    }
    
    public Session getMailSession() {
    	return mailSession;
    }
    
    protected void readDateFormats(Properties config)
    {
        Enumeration propertyNames = config.propertyNames();

        while (propertyNames.hasMoreElements())
        {
            String propertyName = (String) propertyNames.nextElement();

            if (propertyName.startsWith("date.format."))
            {
                try
                {
                    String lang =
                        propertyName.substring(
                            propertyName.lastIndexOf('.') + 1);

                    String dateFormatString = config.getProperty(propertyName);

                    if (dateFormatString.trim().length() == 0)
                    {
                        dateFormatString = "yyyy/MM/dd HH:mm";
                    }

                    LanguageManager.getInstance().addDateFormat(
                        lang,
                        dateFormatString);
                }
                catch (IndexOutOfBoundsException iex)
                {
                	Logger.getLogger(getClass()).warn("invalid date format: " + iex);
                }
            }
        }
    }

    public int getDiskQuotaCheckHour()
    {
    	return(diskQuotaCheckHour);
    }
    
    public String getMailHost()
    {
    	return(mailHost);
    }
    
    public boolean isSmtpAuth() 
    {
    	return smtpAuth;
    }
    
    public String getSmtpUser()
    {
    	return smtpUser;
    }
    
    public String getSmtpPassword()
    {
    	return smtpPassword;
    }

    public boolean isSmtpSecure() 
    {
    	return smtpSecure;
    }
    
    public boolean isMailNotifyQuotaAdmin()
    {
    	return(mailNotifyQuotaAdmin);
    }

    public boolean isMailNotifyQuotaUser()
    {
    	return(mailNotifyQuotaUser);
    }

    public boolean isMailNotifyLogin()
    {
    	return(mailNotifyLogin);
    }
    
    public ArrayList<String> getAdminEmailList()
    {
    	return(adminEmailList);
    }
    
    public SimpleDateFormat getLogDateFormat()
    {
    	return(logDateFormat);
    }
    
    public int getThumbnailsPerPage()
    {
    	return(thumbnailsPerPage);
    }
    
    public String getConfigBaseDir()
    {
    	return(configBaseDir);
    }
    
    public UserManager getUserMgr()
    {
    	return(this.userMgr);
    }
    
    public boolean isDebugMail()
    {
    	return(debugMail);
    }

    public boolean isEnableCalendar()
    {
    	return(enableCalendar);
    }
    
    public boolean isFolderWatch()
    {
        return enableFolderWatch;
    }
    
    public String getMailSenderAddress()
    {
    	return(mailSenderAddress);
    }
    
    public String getMailSenderName()
    {
    	return(mailSenderName);
    }
    
    public int getMaxAppointmentMailsPerHour()
    {
    	return maxAppointmentMailsPerHour;
    }
    
    public String getClientUrl()
    {
    	return(this.clientUrl);
    }
    
    public int getOpSysType()
    {
    	return(opSysType);
    }
    
    public String getOpSysName()
    {
    	return(opSysName);
    }
    
    public boolean is32bitWindows() 
    {
        return((opSysName != null)) &&  
               (opSysName.equals("Windows NT") ||
                opSysName.equals("Windows XP") ||
                opSysName.equals("Windows 2003") ||
                opSysName.equals("Windows Vista") ||
                opSysName.equals("Windows 7") ||
                opSysName.startsWith("Windows 8") ||
                opSysName.startsWith("Windows 10") ||
                opSysName.equals("Windows 2000") ||
                opSysName.startsWith("Windows Server"));
    }
    
    public String getJavaVersion()
    {
    	return(javaVersion);
    }
    
    public DocumentBuilderFactory getDocFactory()
    {
    	return(docFactory);
    }
    
    public String getPrimaryLanguage()
    {
    	return(primaryLanguage);
    }
    
    public String getLocalHostName()
    {
    	return(localHostName);
    }
    
    public boolean isOpenRegistration()
    {
    	return(openRegistration);
    }
    
    public String getWebAppRootDir()
    {
    	return(webAppRootDir);
    }
    
    public String getLoginErrorPage()
    {
    	return(loginErrorPage);
    }
    
    public String getLocalIPAddress()
    {
    	return(localIPAddress);
    }
    
    public void setMaintananceMode(boolean newVal)
    {
    	maintananceMode = newVal;
    }
    
    public boolean isMaintananceMode()
    {
    	return(maintananceMode);
    }
    
    public boolean isAutoCreateThumbs()
    {
    	return(autoCreateThumbs);
    }

    public boolean isChmodAllowed()
    {
    	return(chmodAllowed);
    }
    
    /**
     * Are backward links from the linked file to the linking file enabeld?
     * @return
     */
    public boolean isReverseFileLinkingEnabled()
    {
        return reverseFileLinkingEnabled;
    }
    
    public void setThumbThreadRunning(boolean newVal)
    {
    	thumbThreadRunning = newVal;
    }
    
    public boolean isThumbThreadRunning()
    {
    	return(thumbThreadRunning);
    }
    
    public boolean isShowAssignedIcons()
    {
    	return(showAssignedIcons);
    }

    public void setLicensed(boolean newVal)
    {
    	licensed = newVal;
    }
    
    public boolean isLicensed()
    {
    	return(licensed);
    }
    
    public boolean isShowDescriptionsInline()
    {
    	return(showDescriptionsInline);
    }
    
    public boolean isAutoExtractMP3()
    {
    	return(autoExtractMP3);
    }
    
    public boolean isSimulateRemote()
    {
        return simulateRemote;
    }
    
    public boolean isDownloadStatistics()
    {
    	return(downloadStatistics);
    }
    
    public int getSlideShowDelay()
    {
    	return(slideShowDelay);
    }
    
    public long getUploadLimit()
    {
    	return(uploadLimit);
    }
    
    public int getTextFileMaxLineLength() {
    	return textFileMaxLineLength;
    }
    
    public int getFolderWatchInterval() 
    {
        return folderWatchInterval;
    }
    
    public String getSystemEditor()
    {
    	return(systemEditor);
    }
    
    public String getLoopbackAddress()
    {
    	return(LOOPBACK_ADDRESS);
    }
    
    public String getServerDNS()
    {
        return(serverDNS);
    }

    public String getLogoutURL()
    {
    	return(this.logoutURL);
    }
    
    public String getUserDocRoot()
    {
    	return(userDocRoot);
    }
    
    public long getDefaultDiskQuota()
    {
    	return(DEFAULT_DISK_QUOTA);
    }
    
    public boolean isMailNotifyRegister()
    {
    	return(mailNotifyRegister);
    }

    public boolean isMailNotifyWelcome()
    {
    	return(mailNotifyWelcome);
    }
    
    public boolean isAllowProcessKill()
    {
    	return(allowProcessKill);
    }
    
    public boolean isSyncIgnoreOffsetDST()
    {
        return(syncIgnoreOffsetDST);
    }
    
    public boolean isOldLinuxPsStyle()
    {
    	return(oldLinuxPsStyle);
    }

    public DiskQuotaInspector getDiskQuotaInspector()
    {
    	return(quotaInspector);
    }
    
    public int getCalendarExpirationPeriod()
    {
    	return calendarExpirationPeriod;
    }
}

