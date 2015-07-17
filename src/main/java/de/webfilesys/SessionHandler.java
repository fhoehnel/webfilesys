package de.webfilesys;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.log4j.Logger;

import de.webfilesys.calendar.AppointmentManager;
import de.webfilesys.decoration.DecorationManager;
import de.webfilesys.graphics.AutoThumbnailCreator;
import de.webfilesys.user.UserManager;
import de.webfilesys.user.UserManagerBase;
import de.webfilesys.watch.FolderWatchManager;

public class SessionHandler
implements HttpSessionListener, ServletContextListener
{
	private static int activeSessions = 0;

	private static Hashtable sessionList = new Hashtable();

	/**
	 * @see javax.servlet.http.HttpSessionListener#sessionCreated(HttpSessionEvent)
	 */
	public void sessionCreated(HttpSessionEvent sessionEvent)
	{
		HttpSession session = sessionEvent.getSession();

		String sessionId = session.getId();

		sessionList.put(sessionId, session);

		activeSessions++;
		
        if (activeSessions >= 0)  // this value can be negative because of sessions that survived tomcat restart
        {
  		    // Logger.getLogger(getClass()).debug("active sessions: " + activeSessions);
        }
	}

	/**
	 * @see javax.servlet.http.HttpSessionListener#sessionDestroyed(HttpSessionEvent)
	 */
	public void sessionDestroyed(HttpSessionEvent sessionEvent)
	{
		HttpSession session = sessionEvent.getSession();

		String sessionId = null;

		try
		{
			sessionId = session.getId();

			sessionList.remove(sessionId);

			String userid = (String) session.getAttribute("userid");

			if (userid == null)
			{
		        Logger.getLogger(getClass()).info("session expired/destroyed with id " + sessionId);
			}
			else
			{
		        Logger.getLogger(getClass()).info("session expired/destroyed for user: " + userid + " sessionId: " + sessionId);
			}
		}
		catch (IllegalStateException iex)
		{
			Logger.getLogger(getClass()).info("session expired/destroyed with id " + sessionId);

			// In tomcat version 4 the session has already been invalidated when sessionDestroyed()
			// is called. So we get an IllegalStateException when we try to read the userid attribute.
			// In tomcat version 5 sessionDestroyed() is called before the session is being invalidated.
			
			Logger.getLogger(getClass()).debug(iex);
		}

		activeSessions--;

		Logger.getLogger(getClass()).debug("active sessions: " + activeSessions);
	}

	public static Enumeration getSessions()
	{
		return (sessionList.elements());
	}
	
    public void contextInitialized (ServletContextEvent servletContextEvent)
    {
    	// ServletContext servletContext = servletContextEvent.getServletContext ();
    }

    public void contextDestroyed (ServletContextEvent servletContextEvent)
    {
        // ServletContext servletContext = servletContextEvent.getServletContext ();

    	Logger.getLogger(getClass()).info("saving and cleaning up on context shutdown");
    	
    	UserManager userMgr = WebFileSys.getInstance().getUserMgr();    	
    	
    	((UserManagerBase) userMgr).interrupt();

        MetaInfManager.getInstance().interrupt();
        
        CategoryManager.getInstance().interrupt();

        DecorationManager.getInstance().interrupt();

		if (WebFileSys.getInstance().getDiskQuotaInspector() != null)
		{
			WebFileSys.getInstance().getDiskQuotaInspector().interrupt();
		}

		if (WebFileSys.getInstance().isEnableCalendar())
		{
			AppointmentManager.getInstance().interrupt();
		}

		if (AutoThumbnailCreator.instanceCreated())
        {
			AutoThumbnailCreator.getInstance().interrupt();
        }
        
        if (File.separatorChar == '\\')
        {
            WinDriveManager.getInstance().interrupt();
        }
        
        FastPathManager.getInstance().interrupt();
        
        if (WebFileSys.getInstance().isFolderWatch()) {
            FolderWatchManager.getInstance().interrupt();
        }

        do
		{
			try
			{
				Thread.currentThread().sleep(3000);
			}
			catch (InterruptedException iex)
			{
				System.out.println(iex);
			}
		}
		while (!userMgr.isReadyForShutdown());

        Logger.getLogger(getClass()).info("WebFileSys ready for shutdown");
    }
	
}
