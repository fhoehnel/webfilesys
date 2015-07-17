package de.webfilesys.gui.anonymous;

import java.io.PrintWriter;

import de.webfilesys.WebFileSys;

/**
 * @author Frank Hoehnel
 */
public class VersionInfoRequestHandler
{
	private PrintWriter output = null;
	
	public VersionInfoRequestHandler(PrintWriter output)
	{
		this.output = output;
	}

    public void handleRequest()
    {
		output.println("<html>");
		output.println("<head>");
		output.println("<title> WebFileSys Version Info </title>");

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/common.css\">");
		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/skins/fmweb.css\">");

		output.println("</head>");
		output.println("<body style=\"background-color:#e0e0e0\">");
		output.println("<table border=\"0\" width=\"100%\" cellpadding=\"14\">");
		output.println("<tr><td class=\"story\" style=\"text-align:center\"> WebFileSys </td></tr>");
		output.println("<tr><td class=\"value\" style=\"text-align:center\">" + WebFileSys.VERSION + "</td></tr>");
		output.println("<tr><td style=\"text-align:center\"><a class=\"fn\" href=\"http://www.webfilesys.de\" target=\"_blank\">www.webfilesys.de</a></td></tr>");
		output.println("<tr><td style=\"text-align:center\"><form><input type=\"button\" value=\"Close\" onClick=\"self.close()\" /></form></td></tr>");
		output.println("</table>");
		output.println("</body></html>");
		output.flush();
    }
}
