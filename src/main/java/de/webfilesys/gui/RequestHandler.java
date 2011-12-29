package de.webfilesys.gui;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

/**
 * @author Frank Hoehnel
 */
public abstract class RequestHandler
{
	public static final int BROWSER_MSIE     = 1;
    public static final int BROWSER_NON_MSIE = 2;
	public static final int BROWSER_MOZILLA  = 3;
	public static final int BROWSER_OPERA    = 4;
    public static final int BROWSER_SAFARI   = 5;
    public static final int BROWSER_GOOGLE   = 6;
	
	public static final int LIC_REMINDER_INTERVAL = 10;

	protected HttpServletRequest req = null;

	protected HttpServletResponse resp = null;
	
	protected PrintWriter output = null;
	
	protected HttpSession session = null;
	
	protected String browserType = null;
	
	protected int browserManufacturer = BROWSER_MSIE;;
	
	protected int browserVersion = 0;
	
	protected static int requestCounter = 0;
	
    public RequestHandler(HttpServletRequest req, HttpServletResponse resp,
    		              HttpSession session,
    		              PrintWriter output)
    {
        this.req = req;
        
        this.resp = resp;

        this.session = session;
    	
    	this.output = output;
    	
    	getBrowserType(req);
    }
    
	public void handleRequest()
	{
		process();
	}

	protected abstract void process();

	protected void setParameter(String parmName,String parmValue)
	{
		req.setAttribute(parmName, parmValue);
	}
	
	public String getParameter(String parmName)
	{
		String parmValue = req.getParameter(parmName);
		
		if (parmValue != null)
		{
			return(parmValue);
		}
		
		Object o = req.getAttribute(parmName);
		
		if ((o != null) && (o instanceof String))
		{
			return((String) o);
		}
		
		return(null);
	}

	public String insertDoubleBackslash(String source)
	{
		StringBuffer dest=new StringBuffer();

		for (int i=0;i<source.length();i++)
		{
			if (source.charAt(i)=='\\')
				dest.append("\\\\");
			else
				dest.append(source.charAt(i));
		}    
		return(dest.toString());
	}

	protected void headLine(String text)
	{
		output.println("<div class=\"headline\">");
        output.println(text);
		output.println("</div>");
	}
    
	protected void javascriptAlert(String alertText)
	{
		output.println("<script type=\"text/javascript\">");
		output.println("alert('" + alertText + "');");
		output.println("</script>");
		output.flush();
	}
	
	private void getBrowserType(HttpServletRequest req)
	{
		browserType = req.getHeader("User-Agent");
		
		if (browserType == null)
		{
			Logger.getLogger(getClass()).debug("user agent of browser undefined");
			
			browserType = "";
			browserManufacturer = BROWSER_NON_MSIE;
			
			return;
		}
		
		if (browserType.indexOf("Opera") >= 0)
		{
			browserManufacturer = BROWSER_OPERA;
		}
		else
		{
			if (browserType.indexOf("MSIE") >= 0)
			{
				if (browserType.indexOf("MSIE 6.") >= 0)
				{
					browserVersion = 6;
				}
				else if (browserType.indexOf("MSIE 7.") >= 0)
				{
					browserVersion = 7;
				}
                else if (browserType.indexOf("MSIE 8.") >= 0)
                {
                    browserVersion = 8;
                }
			}
			else
			{
                if (browserType.indexOf("Chrome") >= 0)
                {
                    browserManufacturer = BROWSER_GOOGLE;
                    browserVersion = 6;
                }
                else if (browserType.indexOf("Safari") >= 0)
                {
                    browserManufacturer = BROWSER_SAFARI;
                    
                    if (browserType.indexOf("Mobile") >= 0)
                    {
                        browserVersion = 0; 
                    }
                    else
                    {
                        browserVersion = 6; 
                    }
                }
                else if (browserType.indexOf("Gecko") >= 0)
                {
                    browserManufacturer = BROWSER_MOZILLA;
                    browserVersion = 6;
                }
                else
                {
                    browserManufacturer = BROWSER_NON_MSIE;
                }
			}
		}
	}
	
	protected String getCwd()
	{
		return((String) session.getAttribute("cwd"));
	}
	
	/**
	 * Safari can handle XSLT but not via Javascript.
	 * 
	 * @return true if browser supports XSLT for XML+XSL files and via Javascript
	 */
	protected boolean isBrowserXslEnabled()
	{
		return(((browserManufacturer == BROWSER_MSIE) ||
                (browserManufacturer == BROWSER_GOOGLE) ||
             // (browserManufacturer == BROWSER_SAFARI) ||
		        (browserManufacturer == BROWSER_MOZILLA)) &&
		       (browserVersion >= 6));
	}
	
	protected int getIntParam(String paramName, int defaultValue)
	{
		int value = defaultValue;
		
		String paramValue = getParameter(paramName);		

		if (paramValue != null)
		{
			try
			{
				value = Integer.parseInt(paramValue);
			}
			catch (NumberFormatException nfex)
			{
			}
		}
		
		return(value);
	}
	
	protected int getScreenWidth()
	{
        int screenWidth = 0;
        
        String screenWidthParam = (String) req.getParameter("screenWidth");
        
        if (screenWidthParam != null) 
        {
            try
            {
                screenWidth = Integer.parseInt(screenWidthParam);
            }
            catch (NumberFormatException numEx)
            {
                Logger.getLogger(getClass()).warn(numEx);
            }
        }
        
        if (screenWidth == 0)
        {
            Integer screenWidthFromSession = (Integer) session.getAttribute("screenWidth");
            if (screenWidthFromSession != null) 
            {
                screenWidth = screenWidthFromSession.intValue();
            }
        }
        
        if (screenWidth == 0)
        {
            screenWidth = 1024;
        }
        
        return screenWidth;
	}

    protected int getScreenHeight()
    {
        int screenHeight = 0;
        
        String screenHeightParam = (String) req.getParameter("screenHeight");
        
        if (screenHeightParam != null) 
        {
            try
            {
                screenHeight = Integer.parseInt(screenHeightParam);
            }
            catch (NumberFormatException numEx)
            {
                Logger.getLogger(getClass()).warn(numEx);
            }
        }
        
        if (screenHeight == 0)
        {
            Integer screenHeightFromSession = (Integer) session.getAttribute("screenHeight");
            if (screenHeightFromSession != null) 
            {
                screenHeight = screenHeightFromSession.intValue();
            }
        }
        
        if (screenHeight == 0)
        {
            screenHeight = 1024;
        }
        
        return screenHeight;
    }
	
}
