package de.webfilesys;

import de.webfilesys.util.CommonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;

public class WebFileSysConfig {

    private static WebFileSysConfig instance = null;

    /** default upload limit: 128 MBytes */
    private static final long DEFAULT_UPLOAD_LIMIT = (128L * 1024L * 1024L);

    /** folder watch interval in minutes */
    private static final int DEFAULT_FOLDER_WATCH_INTERVAL = 24 * 60;

    public static final int DEFAULT_TEXT_FILE_MAX_LINE_LENGTH = 2048;

    /** maximum number of appointment e-mails that can be sent in one hour */
    public static final int DEFAULT_MAX_APP_MAILS_PER_HOUR = 200;

    /** default expiration period in days for non-repeated appointments */
    private static final int DEFAULT_CAL_EXPIRATION_PERIOD = 365;

    private static final long DEFAULT_DISK_QUOTA_DEFAULT = 1024L * 1024L;

    private static final int DEFAULT_THUMBNAILS_PER_PAGE = 12;

    private static final int DEFAULT_SLIDESHOW_DELAY = 5;

    private static final Logger LOG = LogManager.getLogger(WebFileSysConfig.class);

    private Properties configProps;

    private boolean openRegistration = false;

    /** the fully qualified server DNS name, if different from localhost DNS */
    private String serverDNS = null;

    public static WebFileSysConfig getInstance()
    {
        return(instance);
    }

    public static WebFileSysConfig createInstance(Properties configProps) {
        if (instance != null) {
            return instance;
        }
        instance = new WebFileSysConfig(configProps);
        return(instance);
    }

    private WebFileSysConfig(Properties configProps) {
        this.configProps = configProps;
        initialize();
    }

    private void initialize() {
        openRegistration = false;
        String value = configProps.getProperty("RegistrationType", "closed");
        if ("open".equalsIgnoreCase(value)) {
            openRegistration = true;
            LOG.info("registration: open");
        } else {
            LOG.info("registration: closed");
        }
    }

    private boolean getBooleanValue(String propertyName, boolean defaultValue) {
        String value = configProps.getProperty(propertyName);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value);
    }

    private int getIntValue(String propertyName, int defaultValue) {
        String value = configProps.getProperty(propertyName);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException nfex) {
            LOG.warn("invalid value for config property {} ignored - using default value: {}", propertyName, defaultValue);
        }
        return defaultValue;
    }

    private long getLongValue(String propertyName, long defaultValue) {
        String value = configProps.getProperty(propertyName);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException nfex) {
            LOG.warn("invalid value for config property {} ignored - using default value: {}", propertyName, defaultValue);
        }
        return defaultValue;
    }

    public long getUploadLimit() {
        return getLongValue("UploadLimit", DEFAULT_UPLOAD_LIMIT);
    }

    public boolean isOpenRegistration() {
        return(openRegistration);
    }

    public String getFfmpegExePath() {
        return configProps.getProperty("ffmpegExePath");
    }

    public String getFfprobeExePath() {
        return configProps.getProperty("ffprobeExePath");
    }

    public String getVideoPlayerExePath() {
        return configProps.getProperty("VideoPlayerExePath");
    }

    public String getFfmpegAddParams() {
        return configProps.getProperty("ffmpegAddParams");
    }

    public String getVideoPlayerAddParams() {
        return configProps.getProperty("videoPlayerAddParams");
    }

    public String getSystemEditor() {
        return configProps.getProperty("SystemEditor");
    }

    public boolean isFolderWatch() {
        return getBooleanValue("EnableFolderWatch", false);
    }

    /** folder watch interval in minutes */
    public int getFolderWatchInterval() {
        return getIntValue("FolderWatchInterval", DEFAULT_FOLDER_WATCH_INTERVAL);
    }

    public String getServerDNS() {
        return configProps.getProperty("serverDNS");
    }

    public String getLogoutURL() {
        return configProps.getProperty("LogoutPageURL");
    }

    public String getLoginErrorPage() {
        return configProps.getProperty("LoginErrorURL");
    }

    public boolean isDownloadStatistics() {
        return getBooleanValue("EnableDownloadStatistics", false);
    }

    public boolean isMailNotifyRegister() {
        return getBooleanValue("MailNotification.registration", false);
    }

    public int getPollFilesysChangesInterval() {
        return getIntValue("PollFilesysChangesInterval", 0) * 1000;
    }

    public int getTextFileMaxLineLength() {
        return getIntValue("TextFileMaxLineLength", DEFAULT_TEXT_FILE_MAX_LINE_LENGTH);
    }

    public boolean isShowDescriptionsInline() {
        return getBooleanValue("ShowDescriptionsInline", true);
    }

    public boolean isShowAssignedIcons() {
        return getBooleanValue("ShowAssignedIcons", true);
    }

    public boolean isAutoExtractMP3() {
        return getBooleanValue("AutoExtractMP3Tags", false);
    }

    public String getMailHost() {
        return configProps.getProperty("SmtpMailHost");
    }

    public String getMailPort() {
        return configProps.getProperty("SmtpMailPort");
    }

    public boolean isSmtpSecure() {
        return getBooleanValue("SmtpSecure", false);
    }

    public boolean isSmtpAuth() {
        return getBooleanValue("SmtpAuth", false);
    }

    public boolean isDebugMail() {
        return getBooleanValue("DebugMail", false);
    }

    public String getSmtpUser() {
        return configProps.getProperty("SmtpUser");
    }

    public String getSmtpPassword() {
        return configProps.getProperty("SmtpPassword");
    }

    public String getMailSenderAddress() {
        return configProps.getProperty("MailSenderAddress");
    }

    public String getMailSenderName() {
        return configProps.getProperty("MailSenderName");
    }

    public boolean isEnableCalendar() {
        return getBooleanValue("EnableCalendar", false);
    }

    /** emergency brake to prevent sending uncontrolled numbers of mails if something unexpected happens */
    public int getMaxAppointmentMailsPerHour() {
        return getIntValue("MaxAppointmentMailsPerHour", DEFAULT_MAX_APP_MAILS_PER_HOUR);
    }

    public int getCalendarExpirationPeriod() {
        return getIntValue("AppointmentExpirationDays", DEFAULT_CAL_EXPIRATION_PERIOD);
    }

    public long getDefaultDiskQuota() {
        return getLongValue("DiskQuotaDefaultMB", DEFAULT_DISK_QUOTA_DEFAULT) * 1024L * 1024L;
    }

    public int getThumbnailsPerPage() {
        return getIntValue("PageThumbnailNumber", DEFAULT_THUMBNAILS_PER_PAGE);
    }

    public boolean isAutoCreateThumbs() {
        return getBooleanValue("AutoCreateThumbnails", false);
    }

    /** simulate remote client connections from the local host */
    public boolean isSimulateRemote() {
        return getBooleanValue("SimulateRemote", false);
    }

    /**
     * Are backward links from the linked file to the linking file enabeld?
     * Linking backward from the link target file to the link is required to
     * allow automatic update of the link when the target file is moved so that the
     * link points to the new target path after the move operation.
     * Reverse linking has some disadvantages too:
     * A metainf file is created in the folder containing the link target file (if it not already exists).
     * And there is some additional processing required for move operations (performance!).
     */
    public boolean isReverseFileLinkingEnabled() {
        return getBooleanValue("ReverseFileLinkingEnabled", false);
    }

    public String getPrimaryLanguage() {
        return configProps.getProperty("primaryLanguage", LanguageManager.DEFAULT_LANGUAGE);
    }

    public String getClientUrl() {
        return configProps.getProperty("ClientURL");
    }

    public boolean isMailNotifyLogin() {
        return getBooleanValue("MailNotification.login", false);
    }

    public boolean isMailNotifyQuotaAdmin() {
        return getBooleanValue("DiskQuotaNotifyAdmin", false);
    }

    public boolean isMailNotifyQuotaUser() {
        return getBooleanValue("DiskQuotaNotifyUser", false);
    }

    /** slideshow delay in seconds */
    public int getSlideShowDelay() {
        return getIntValue("SlideshowDelay", DEFAULT_SLIDESHOW_DELAY);
    }

    public boolean isEnableDiskQuota() {
        return getBooleanValue("EnableDiskQuota", false);
    }

    public int getDiskQuotaCheckHour() {
        return getIntValue("DiskQuotaCheckHour", 3);
    }

    public boolean isSyncIgnoreOffsetDST() {
        return getBooleanValue("SyncIgnoreOffsetDST", false);
    }

    public boolean isOldLinuxPsStyle() {
        String linuxPsStyle = configProps.getProperty("LinuxPsStyle", "new").toLowerCase();
        return "old".equals(linuxPsStyle);
    }

    public boolean isAllowProcessKill() {
        return getBooleanValue("AllowProcessKill", false);
    }

    /** Unix only: enable OS shell command execution via WebFileSys web interface */
    public boolean isOSShellCommandExcution() {
        return getBooleanValue("OSShellCommandExceution", false);
    }

    /** allow chmod/chown for users of role webspace */
    public boolean isChmodAllowed() {
        return getBooleanValue("ChmodWebspace", false);
    }

    public String getGoogleMapsAPIKeyHTTP() {
        String key = configProps.getProperty("GoogleMapsAPIKeyHTTP");
        if (CommonUtils.isEmpty(key)) {
            LOG.warn("no google maps API key configured for HTTP (missing config property GoogleMapsAPIKeyHTTP)");
        }
        return key;
    }

    public String getGoogleMapsAPIKeyHTTPS() {
        String key = configProps.getProperty("GoogleMapsAPIKeyHTTPS");
        if (CommonUtils.isEmpty(key)) {
            LOG.warn("no google maps API key configured for HTTPS (missing config property GoogleMapsAPIKeyHTTPS)");
        }
        return key;
    }

}
