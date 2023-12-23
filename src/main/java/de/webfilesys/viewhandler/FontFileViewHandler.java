package de.webfilesys.viewhandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


import de.webfilesys.ViewHandlerConfig;
import de.webfilesys.util.UTF8URLEncoder;

/**
 * View the content of a font file (WOFF, TTF, EOT).
 * 
 * @author Frank Hoehnel
 */
public class FontFileViewHandler implements ViewHandler {
	
	private static final int START_IDX = 0x0000;

	private static final int END_IDX = 0xffff;

	public void process(String filePath, ViewHandlerConfig viewHandlerConfig, HttpServletRequest req,
			HttpServletResponse resp) {
		
		try {
			resp.setContentType("text/html");
			
			PrintWriter output = resp.getWriter();

			output.println("<html>");
			output.println("<head>");
			output.println("<title>WebFileSys font file viewer</title>");
			output.println("<style>");
			
			output.println("@font-face {");
			output.println("font-family: 'TheFont';");
			output.println("src: url('/webfilesys/servlet?command=getFile&filePath=" + UTF8URLEncoder.encode(filePath) + "&disposition=inline');");
			output.println("font-weight: normal;");
			output.println("font-style: normal;");
			output.println("}");

			output.println("span:before {");
			output.println("    font-family: TheFont;");
			output.println("    font-size: 20px;");
			output.println("}");

			output.println("table {");
			output.println("    border-collapse: collapse;");
			output.println("    margin-bottom: 10px;");
			output.println("}");

			output.println("td {");
			output.println("    width: 40px;");
			output.println("    text-align: center;");
			output.println("    padding-top: 4px;");
			output.println("    padding-bottom: 4px;");
			output.println("    border: 1px solid navy;");
			output.println("    background-color: ivory;");
			output.println("}");
			
			for (int i = START_IDX; i < END_IDX; i++) {
				output.println(".char-" + i + ":before {");
				output.println("    content: '\\" + String.format("%04x", i) + "';");
				output.println("}");
			}

			output.println("</style>");
			output.println("</head>");
			output.println("<body>");

			int i = START_IDX;
			for (; i < END_IDX; i++) {
				if (i % 16 == 0) {
					if (i != START_IDX) {
						output.println("</tr>");
					}
					if (i % (16 * 16) == 0) {
						if (i != START_IDX) {
							output.println("</table>");
						}
						output.println("<table>");
					}
				    output.println("<tr>");
				    output.print("<td>");
				    output.print(String.format("%04x", i));
				    output.println("</td>");
				}
			    output.print("<td title=\"" + String.format("%04x", i) + "\">");
			    output.print("<span class=\"char-" + i + "\"></span>");
			    output.println("</td>");
			}
			
			if (i % 16 > 0) {
				for (; i % 16 > 0; i++) {
				    output.println("<td></td>");
				}
			}
			
			output.println("</tr>");
			output.println("</table>");

			output.println("</body>");
			output.println("</html>");

			output.flush();
		} catch (IOException ex) {
			LogManager.getLogger(getClass()).error("Font file viewer exception: " + ex);
		}
	}

	/**
	 * Create the HTML response for viewing the given file contained in a ZIP
	 * archive..
	 * 
	 * @param zipFilePath
	 *            path of the ZIP entry
	 * @param zipIn
	 *            the InputStream for the file extracted from a ZIP archive
	 * @param req
	 *            the servlet request
	 * @param resp
	 *            the servlet response
	 */
	public void processZipContent(String zipFilePath, InputStream zipIn, ViewHandlerConfig viewHandlerConfig,
			HttpServletRequest req, HttpServletResponse resp) {
		// not supported
	}

	/**
	 * Does this ViewHandler support reading the file from an input stream of a
	 * ZIP archive?
	 * 
	 * @return true if reading from ZIP archive is supported, otherwise false
	 */
	public boolean supportsZipContent() {
		return false;
	}

}
