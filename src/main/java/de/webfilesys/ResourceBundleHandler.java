package de.webfilesys;

import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.webfilesys.gui.user.UserRequestHandler;
import de.webfilesys.util.CommonUtils;

/**
 * Delivers the resource bundle properties for the current user's language in JSON format.
 * @author fho
 */
public class ResourceBundleHandler extends UserRequestHandler{

	public ResourceBundleHandler(HttpServletRequest req, HttpServletResponse resp, HttpSession session, 
			PrintWriter output, String uid) 
	{
		super(req, resp, session, output, uid);
	}

	public ResourceBundleHandler(HttpServletRequest req, HttpServletResponse resp, PrintWriter output) 
	{
		super(req, resp, null, output, null);
	}
	
	protected void process()
	{
        String bundleLang = language;
		String langParam = getParameter("lang");
		if (!CommonUtils.isEmpty(langParam)) {
			bundleLang = langParam;
		}
		
		resp.setContentType("text/javascript");

        // overwrite the no chache headers already set in WebFileSysServlet
		// resp.setHeader("Cache-Control", null);
		resp.setHeader("Cache-Control", "public, max-age=36000, s-maxage=36000");
		resp.setDateHeader("expires", System.currentTimeMillis() + (10 * 60 * 60 * 1000)); // now + 10 hours

		output.println("var resourceBundle = {");
		
		Properties languageResources = LanguageManager.getInstance().getLanguageResources(bundleLang);

		Enumeration keys = languageResources.propertyNames();

		while (keys.hasMoreElements()) 
		{
			String key = (String) keys.nextElement();
			
			String value = languageResources.getProperty(key);
			
			output.print("\"" + key + "\"");
			output.print(": ");
			output.print("\"" + CommonUtils.escapeJSON(value) + "\"");
			if (keys.hasMoreElements())
			{
				output.println(',');
			}
			else
			{
				output.println();
			}
		}
		
		output.println("};");
		
		output.flush();
	}
	
}
