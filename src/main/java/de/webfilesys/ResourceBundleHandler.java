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
	
	protected void process()
	{
		resp.setContentType("text/javascript");

		resp.setDateHeader("expires", System.currentTimeMillis() + (10 * 60 * 60 * 1000)); // now + 10 hours

		output.println("var resourceBundle = {");
		
		Properties languageResources = LanguageManager.getInstance().getLanguageResources(language);

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
