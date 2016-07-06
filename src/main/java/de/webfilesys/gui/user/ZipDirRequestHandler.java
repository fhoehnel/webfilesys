package de.webfilesys.gui.user;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.UTF8URLEncoder;

/**
 * @author Frank Hoehnel
 */
public class ZipDirRequestHandler extends UserRequestHandler {
    private static final Logger LOG = Logger.getLogger(ZipDirRequestHandler.class);
	
	public ZipDirRequestHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid) {
        super(req, resp, session, output, uid);
	}

	protected void process() {
		if (!checkWriteAccess()) {
			return;
		}

        String currentPath = getParameter("actPath");

        if (isMobile()) {
            currentPath = getAbsolutePath(currentPath);
        }
		
		if (!checkAccess(currentPath)) {
			return;
		}

		output.println("<html>");
		output.println("<head>");

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/common.css\">");
		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/skins/" + userMgr.getCSS(uid) + ".css\">");

		output.println("</head>");
		output.println("<body>");

		headLine(getResource("label.zippingdir","compressing directory"));

		output.flush();

		ZipOutputStream zipOut = null;

		try {
			File zipDestFile = File.createTempFile("webfilesys", null);

			String dirName = currentPath.substring(currentPath.lastIndexOf(File.separatorChar) + 1);

			String finalZipFilePath = null;

			boolean destFileExists = true;
			
			for (int i = 0; destFileExists; i++) {
				if (currentPath.endsWith(File.separator)) {
					finalZipFilePath = currentPath + dirName + "_" + i + ".zip";
				} else {
					finalZipFilePath = currentPath + File.separator + dirName + "_" + i + ".zip";
				}
				
				File destFile = new File(finalZipFilePath);
				if (!destFile.exists()) {
					destFileExists = false;
				}
			}

	        String relativePath = getHeadlinePath(finalZipFilePath);

	        output.println("<form accept-charset=\"utf-8\" name=\"form1\">");
	        
	        output.println("<table class=\"dataForm\" width=\"100%\">");
	        
	        output.println("<tr>");
	        output.println("<td colspan=\"2\" class=\"formParm1\">");
	        output.println(getResource("label.directory","folder") + ":");
	        output.println("</td>");
	        output.println("</tr>");

	        output.println("<tr>");
	        output.println("<td colspan=\"2\" class=\"formParm2\">");
	        output.println(CommonUtils.shortName(getHeadlinePath(currentPath), 50));
	        output.println("</td>");
	        output.println("</tr>");
	        
	        output.println("<tr>");
	        output.println("<td colspan=\"2\" class=\"formParm1\">");
			output.println(getResource("label.ziparchive","ZIP archive") + ":");
			output.println("</td>");
			output.println("</tr>");
			
	        output.println("<tr>");
			output.println("<td colspan=\"2\" class=\"formParm2\">");
			output.println(CommonUtils.shortName(relativePath, 50));
	        output.println("</td>");
	        output.println("</tr>");

	        output.println("<tr>");
			output.println("<td colspan=\"2\" class=\"formParm1\">");
			output.println(getResource("label.currentfile","current file") + ":");
	        output.println("</td>");
	        output.println("</tr>");

	        output.println("<tr>");
	        output.println("<td colspan=\"2\" class=\"formParm2\">");
	        output.println("<div id=\"currentDir\" />");
	        output.println("</td>");
	        output.println("</tr>");

	        output.println("<tr>");
	        output.println("<td colspan=\"2\" class=\"formParm1\">");
	        output.println(getResource("label.zipCount","files zipped") + ":");
	        output.println("</td>");
	        output.println("</tr>");
	        
	        output.println("<tr>");
	        output.println("<td colspan=\"2\" class=\"formParm2\">");
	        output.println("<div id=\"compressCount\" />");
	        output.println("</td>");
	        output.println("</tr>");
	        
	        output.println("</table>");

	        output.println("</form>");

			output.flush();
			
			zipOut = new ZipOutputStream(new FileOutputStream(zipDestFile));

			treeFileSize = 0l;

			int zipFileCount = zipTree(currentPath, "", zipOut, 0);
			
			if (zipOut != null) {
				try {
					zipOut.close();
					zipOut = null;
				} catch (Exception ioex) {
				}
			}
			
	        output.println("<script language=\"javascript\">");
	        output.println("document.getElementById('compressCount').innerHTML=\"" + zipFileCount + "\";");
	        output.println("</script>");
			
	        output.println("<br>");
	        output.println("<form>");

	        output.println("<table class=\"dataForm\" width=\"100%\">");

			if (zipFileCount < 1) {
				output.println("<tr><td colspan=\"2\" class=\"formParm2\">");
				output.println(getResource("label.zipnofile", "This directory tree contains no file to be zipped!"));
				output.println("</td></tr>");
			} else {
				long compressedSize = zipDestFile.length();

				if (treeFileSize > 0l) {
					long compressionRatio = (compressedSize * 100) / treeFileSize;
					
					output.println("<tr><td class=\"formParm1\">");
					output.println(getResource("label.zipratio","compression ratio (% of original size)") + ":");
					output.println("</td>");

					output.println("<td class=\"formParm2\">");
					output.println(compressionRatio + "%"); 
					output.println("</td></tr>");
				}

				File moveDestFile = new File(finalZipFilePath);

				if (!zipDestFile.renameTo(moveDestFile)) {
					if (!copyFile(zipDestFile.getAbsolutePath(), finalZipFilePath)) {
						LOG.warn("cannot copy temporary zip file " + zipDestFile.getAbsolutePath() + " to " + finalZipFilePath);
					} else {
						if (!zipDestFile.delete()) {
							LOG.warn("cannot delete temporary zip file " + zipDestFile.getAbsolutePath());
						}
					}
				}
			}

			output.println("<tr><td colspan=\"2\" class=\"formButton\" style=\"text-align:center;\">");
			
	        String mobile = (String) session.getAttribute("mobile");
	        
	        if (mobile != null) {
	            output.println("<input type=\"button\" value=\"" + getResource("button.return","Return") + "\" onclick=\"window.location.href='/webfilesys/servlet?command=mobile&cmd=folderFileList&absPath=" + UTF8URLEncoder.encode(currentPath) + "'\">");
	        } else {
	            output.println("<input type=\"button\" value=\"" + getResource("button.return","Return") + "\" onclick=\"window.location.href='/webfilesys/servlet?command=exp&expand=" + UTF8URLEncoder.encode(currentPath) +"&fastPath=true'\">");
	        }
			
			output.println("</td></tr>");

			output.println("</table>");

			output.println("</form>");

			output.println("</body></html>");
			output.flush();
			
		} catch (IOException ioex) {
			LOG.error(ioex);
			
			output.println("<script language=\"javascript\">");
			output.println("alert('Failed to zip folder tree!');");

			if (isMobile()) {
	            output.println("location.href='/webfilesys/servlet?command=mobile&cmd=folderFileList&absPath=" + UTF8URLEncoder.encode(currentPath) + "';");
			} else {
	            output.println("location.href='/webfilesys/servlet?command=exp&expand=" + UTF8URLEncoder.encode(currentPath) + "';");
			}

			output.println("</script>");

			output.println("</body>");
			output.println("</html>");
			output.flush();
			return;
		} finally {
			if (zipOut != null) {
				try {
					zipOut.close();
				} catch (Exception ioex) {
					LOG.warn(ioex);
				}
			}
		}
	}
}
