package de.webfilesys.gui.admin;

import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;

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
		Enumeration allAppenders = Logger.getLogger(WEBFILESYS_LOGGER_NAME).getAllAppenders();

		while (allAppenders.hasMoreElements())
		{
			Appender appender = (Appender) allAppenders.nextElement();

			if (appender.getName().equals(APPENDER_NAME)) {
	            if (appender instanceof org.apache.log4j.FileAppender)
	            {
	                return ((FileAppender) appender).getFile();
	            }
			}
		}
		
		return null;
	}
}
