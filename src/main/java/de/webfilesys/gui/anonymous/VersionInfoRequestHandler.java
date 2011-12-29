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
		output.println("<HTML>");
		output.println("<HEAD>");
		output.println("<TITLE> WebFileSys Version Info </TITLE>");

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/css/fmweb.css\">");

		output.println("</HEAD>");
		output.println("<BODY>");
		output.println("<table border=\"0\" width=\"100%\" cellpadding=\"14\">");
		output.println("<tr><td class=\"story\" align=\"center\"> WebFileSys </td></tr>");
		output.println("<tr><td class=\"value\" align=\"center\">" + WebFileSys.VERSION + "</td></tr>");
		output.println("<tr><td align=\"center\"><a class=\"fn\" href=\"http://www.webfilesys.de\" target=\"_blank\">www.webfilesys.de</a></td></tr>");
		output.println("<tr><td align=\"center\"><form><INPUT TYPE=\"button\" VALUE=\"Close\" onClick=\"self.close()\"></form></td></tr>");
		output.println("</table>");
		output.println("</body></html>");
		output.flush();
    }
}
