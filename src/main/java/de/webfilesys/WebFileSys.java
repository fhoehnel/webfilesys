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
import java.util.Enumeration;
import java.util.Properties;

import jakarta.mail.Session;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.logging.log4j.LogManager;

import de.webfilesys.calendar.AppointmentManager;
import de.webfilesys.user.UserManager;
import de.webfilesys.user.XmlUserManager;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.watch.FolderWatchManager;
import org.apache.logging.log4j.Logger;

public class WebFileSys {
	private static WebFileSys instance = null;

    private static final Logger LOG = LogManager.getLogger(WebFileSys.class);

    public static final String VERSION = "Version 2.32.1-beta7 (12 Apr 2026)";
 
    public static final int OS_OS2 = 1;
    public static final int OS_WIN = 2;
    public static final int OS_AIX = 3;
    public static final int OS_LINUX = 4;
    public static final int OS_SOLARIS = 5;
    public static final int OS_UNKNOWN = 9;

    public static final String LOOPBACK_ADDRESS = "127.0.0.1";
    
    public static final String IPV6_LOOPBACK_ADDRESS = "0:0:0:0:0:0:0:1";

    private Properties configProps;

    WebFileSysConfig configuration;

    private String webAppRootDir = null;
    
    private String configBaseDir = null;
    
    private String opSysName;
    private int opSysType;
    private String localHostName;
    private String localIPAddress = null;

    private UserManager userMgr = null;
    
    private boolean thumbThreadRunning = false;

    private Session mailSession = null;
    
    private String userDocRoot = null;

    private boolean maintananceMode = false;
    
	private String userMgrClass = null;

    private DiskQuotaInspector quotaInspector = null;

    private SimpleDateFormat logDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        
	private DocumentBuilderFactory docFactory;
	
	public static WebFileSys getInstance()
	{
	    return(instance);
    }
	
	public static WebFileSys createInstance(Properties configProps, String configBaseDir) {
		if (instance != null) {
			return instance;
		}
		instance = new WebFileSys(configProps, configBaseDir);
		return(instance);
    }
	
	private WebFileSys(Properties configProps, String webAppRootDir) {
        LOG.info("starting WebFileSys version " + VERSION);

        this.configProps = configProps;

        configuration = WebFileSysConfig.createInstance(configProps);

		this.webAppRootDir = webAppRootDir;
		
		if (webAppRootDir.endsWith("\\") || webAppRootDir.endsWith("/")) {
			configBaseDir = webAppRootDir + "WEB-INF";
		} else {
			configBaseDir = webAppRootDir + "/WEB-INF";
		}

        LOG.info("java version : {}", System.getProperty("java.version"));
		
        opSysName = System.getProperty("os.name");
        
        LOG.info("operating system : " + opSysName);

        if (opSysName.startsWith("OS/2")) {
            opSysType = OS_OS2;
        } else if (opSysName.startsWith("Win")) {
            opSysType = OS_WIN;
        } else if (opSysName.startsWith("AIX")) {
            opSysType = OS_AIX;
        } else if (opSysName.startsWith("Linux")) {
            opSysType = OS_LINUX;
        } else if ((opSysName.startsWith("Solaris")) || (opSysName.startsWith("SunOS"))) {
            opSysType = OS_SOLARIS;
        } else {
            opSysType = OS_UNKNOWN;
        }

		docFactory = DocumentBuilderFactory.newInstance();
        
        userMgrClass = configProps.getProperty("UserManagerClass");

        userDocRoot = configProps.getProperty("UserDocumentRoot");

        if (configuration.isOpenRegistration()) {
            if (userDocRoot != null) {
                File docRootFile = new File(userDocRoot);

                if ((!docRootFile.exists())
                    || (!docRootFile.isDirectory())
                    || (!docRootFile.canWrite())) {
                    LOG.error("UserDocumentRoot is not a writable directory: {}", userDocRoot);
                    userDocRoot = null;
                } else {
                    if ((File.separatorChar == '\\')
                        && (userDocRoot.length() > 2)) {
                        try {
                            String canonicalRoot =
                                docRootFile.getCanonicalPath().substring(2);
                            String absoluteRoot =
                                docRootFile.getAbsolutePath().substring(2);

                            if (!canonicalRoot.equals(absoluteRoot)) {
                                LOG.error("UserDocumentRoot is not a writable directory (check uppercase/lowercase!): {}", userDocRoot);
                                userDocRoot = null;
                            }
                        } catch (IOException ioex) {
                            LOG.error(ioex);
                        }
                    }
                }

                if (userDocRoot != null) {
                    LOG.info("User Document Root: {}", userDocRoot);
                }
            }

            if (userDocRoot == null) {
            	userDocRoot = configBaseDir + File.separator + "userhome";
                LOG.info("using default UserDocumentRoot for open registration: {}", userDocRoot);
            }
        }

        String mailHost = configuration.getMailHost();

        if ((mailHost != null) && (!mailHost.trim().isEmpty())) {
        	LOG.info("SMTP mail host: " + mailHost);

        	if (configuration.isSmtpAuth()) {
        		if (CommonUtils.isEmpty(configuration.getSmtpUser())) {
        			LOG.error("SmtpUser property is required if SmtpAuth=true");
        		}
        		if (CommonUtils.isEmpty(configuration.getSmtpPassword())) {
        			LOG.error("SmtpPassword property is required if SmtpAuth=true");
        		}
        	}
        } else {
        	LOG.warn("SmtpMailHost not configured - WebFileSys will not send e-mails. It is strongly recommended to configure a mail server.");
        	if (configuration.isOpenRegistration()) {
            	LOG.warn("SmtpMailHost not configured - self registered users must be activated by an administrator");
        	}
        }

        try {
            InetAddress localHost = InetAddress.getLocalHost();
            localIPAddress = localHost.getHostAddress();
            LOG.info("local ip address : {}", localHost);
            localHostName = localHost.getHostName();
        } catch (Exception e) {
        	LOG.error(e);
            try {
                localHostName = InetAddress.getLocalHost().toString();
            } catch (Exception o) {
            	LOG.error(o);
                localHostName = "cannot query host name";
            }
        }

        if (File.separatorChar == '/') {
        	SubdirExistCache.getInstance().setExistsSubdir("/", 1);
        }
        SubdirExistCache.getInstance().initialReadSubdirs(opSysType);
    }

    public void initialize() {
        if ((userMgrClass == null) || (userMgrClass.trim().isEmpty())) {
            userMgr = new XmlUserManager();
        } else {
            try {
                userMgr = (UserManager) Class.forName(this.userMgrClass).newInstance();
                LOG.info("User Manager class: " + this.userMgrClass);
            } catch (ClassNotFoundException cnfex) {
                LOG.error("the user manager class {} cannot be found: {}", userMgrClass, cnfex);
            }
            catch (InstantiationException instEx) {
            	LOG.error("the user manager cannot be instantiated: " + instEx);
            } catch (IllegalAccessException iaEx) {
            	LOG.error("the user manager cannot be instantiated: " + iaEx);
            } catch (ClassCastException cex) {
            	LOG.error("the class " + userMgrClass + " does not implement the UserManager interface: " + cex);
            }
        }

        LanguageManager.getInstance(configuration.getPrimaryLanguage()).listAvailableLanguages();

        if (File.separatorChar == '\\') {
            WinDriveManager.getInstance();
        }
        
        readDateFormats(configProps);

        String mailHost = configuration.getMailHost();

        if ((mailHost != null) && (!mailHost.trim().isEmpty())) {
            initMailSession();
            InvitationManager.getInstance();
        }

        ViewHandlerManager.getInstance();
        
        if (configuration.isEnableDiskQuota()) {
            quotaInspector = new DiskQuotaInspector();
            quotaInspector.start();
        }
        
        if (configuration.isFolderWatch()) {
            FolderWatchManager.getInstance();
        }

        if (configuration.isEnableCalendar()) {
            AppointmentManager.getInstance();
        }
    }
    
    private void initMailSession() {
    	Properties mailProps = new Properties();
        
    	mailProps.put("mail.transport.protocol", "smtp");
    	mailProps.put("mail.smtp.host", configuration.getMailHost());

        if (configuration.getMailPort() != null) {
            mailProps.put("mail.smtp.port", configuration.getMailPort());
        }
            
    	mailProps.put("mail.smtp.starttls.enable", configuration.isSmtpSecure());
        
    	mailProps.put("mail.smtp.auth", configuration.isSmtpAuth());
    	
        if (configuration.getSmtpUser() != null) {
    		mailProps.put("mail.smtp.user", configuration.getSmtpUser());
        }
    	
        mailSession = Session.getInstance(mailProps, null);

        if (configuration.isDebugMail()) {
            mailSession.setDebug(true);
        }
    }
    
    public Session getMailSession() {
    	return mailSession;
    }
    
    protected void readDateFormats(Properties config) {
        Enumeration propertyNames = config.propertyNames();

        while (propertyNames.hasMoreElements()) {
            String propertyName = (String) propertyNames.nextElement();
            if (propertyName.startsWith("date.format.")) {
                try {
                    String lang = propertyName.substring(propertyName.lastIndexOf('.') + 1);

                    String dateFormatString = config.getProperty(propertyName);
                    if (dateFormatString.trim().isEmpty()) {
                        dateFormatString = "yyyy/MM/dd HH:mm";
                    }

                    LanguageManager.getInstance().addDateFormat(lang, dateFormatString);
                } catch (IndexOutOfBoundsException iex) {
                	LOG.warn("invalid date format: " + iex);
                }
            }
        }
    }

    public SimpleDateFormat getLogDateFormat() {
    	return(logDateFormat);
    }
    
    public String getConfigBaseDir() {
    	return(configBaseDir);
    }
    
    public UserManager getUserMgr() {
    	return(this.userMgr);
    }
    
    public int getOpSysType() {
    	return(opSysType);
    }
    
    public String getOpSysName() {
    	return(opSysName);
    }
    
    public DocumentBuilderFactory getDocFactory() {
    	return(docFactory);
    }
    
    public String getLocalHostName() {
    	return(localHostName);
    }
    
    public String getWebAppRootDir() {
    	return(webAppRootDir);
    }
    
    public String getLocalIPAddress() {
    	return(localIPAddress);
    }
    
    public void setMaintananceMode(boolean newVal) {
    	maintananceMode = newVal;
    }
    
    public boolean isMaintananceMode() {
    	return(maintananceMode);
    }
    
    public void setThumbThreadRunning(boolean newVal) {
    	thumbThreadRunning = newVal;
    }
    
    public boolean isThumbThreadRunning() {
    	return(thumbThreadRunning);
    }
    
    public String getUserDocRoot() {
    	return(userDocRoot);
    }
    
    public DiskQuotaInspector getDiskQuotaInspector() {
    	return(quotaInspector);
    }
    
}

