package de.webfilesys.gui.anonymous;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.webfilesys.gui.RequestHandler;

/**
 * @author Frank Hoehnel
 */
public class BlankPageRequestHandler extends RequestHandler
{
	public BlankPageRequestHandler(
			HttpServletRequest req, 
			HttpServletResponse resp,
            PrintWriter output)
	{
		super(req, resp, null, output);
	}

	protected void process()
	{
		output.println("<html>");
		output.println("<head>");
		output.println("</head>");
		output.println("<body>");
		output.println("</body></html>");
		output.flush();
	}
}
