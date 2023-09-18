package de.webfilesys.gui.admin;

import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/**
 * @author Frank Hoehnel
 */
public abstract class LogRequestHandlerBase extends AdminRequestHandler
{
    /** name of the WebFileSys logger defined in log4j.xml */
    public static final String WEBFILESYS_LOGGER_NAME = "de.webfilesys";
    
    /** name of the DailyRollingFileAppender defined in log4j.xml for the de.webfilesys logger */
    public static final String APPENDER_NAME = "WebFileSysLogAppender";
	
    public LogRequestHandlerBase (HttpServletRequest req, HttpServletResponse resp,
        HttpSession session, PrintWriter output, String uid)
    {
         super(req, resp, session, output, uid);
    }
    
	protected String getSystemLogFilePath()
	{
		Logger logger = LogManager.getLogger(WEBFILESYS_LOGGER_NAME);
		Map<String, Appender> appenderMap = ((org.apache.logging.log4j.core.Logger) logger).getAppenders();
		
		Appender appender = appenderMap.get(APPENDER_NAME);
        if (appender instanceof FileAppender) 
        {
        	System.out.println("appender filename: " + ((FileAppender) appender).getFileName());
            return ((FileAppender) appender).getFileName();
        }
    	System.out.println("appender not found, map size: " + appenderMap.size());
		return null;
	}
}
