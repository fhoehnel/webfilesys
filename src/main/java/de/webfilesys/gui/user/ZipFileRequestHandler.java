package de.webfilesys.gui.user;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.TestSubDirThread;
import de.webfilesys.WebFileSys;
import de.webfilesys.graphics.AutoThumbnailCreator;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.UTF8URLEncoder;

/**
 * Zip a file or unzip a zip archive.
 * @author Frank Hoehnel
 */
public class ZipFileRequestHandler extends UserRequestHandler {
    private static final Logger LOG = Logger.getLogger(ZipFileRequestHandler.class);

    public ZipFileRequestHandler(
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

		String filePath = getParameter("filePath");

		if (filePath == null) {
			// we come from UploadServlet
			
			filePath = (String) req.getAttribute("filePath");
		}
		
		if (!checkAccess(filePath)) {
			return;
		}

		output.println("<html>");
		output.println("<head>");

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/common.css\">");
		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/skins/" + userMgr.getCSS(uid) + ".css\">");

		output.println("</head><body>");

        String file_ext = CommonUtils.getFileExtension(filePath);

	    if (isZipFile(file_ext)) {
			headLine(getResource("label.unziphead","Unzip file"));

			ZipFile zipFile = null;
			ZipEntry zipEntry = null;

			try {
				zipFile = new ZipFile(filePath);
			} catch (IOException ioex) {
				LOG.error("ZIP file format error: " + ioex);
				javascriptAlert(getResource("alert.zipformat","ZIP file format error") + "\\n" + ioex);

				output.println("<script language=\"javascript\">");
				output.println("window.location.href='/webfilesys/servlet?command=listFiles';");
				output.println("</script>");
				output.println("</body></html>");
				output.flush();
				return;
			}

			output.println("<br/>");

			output.println("<form accept-charset=\"utf-8\" name=\"form1\">");

			output.println("<table class=\"dataForm\" width=\"100%\">");
			
			output.println("<tr>");
			output.println("<td colspan=\"2\" class=\"formParm1\">");
			output.println(getResource("label.extractfrom","extracting from") + ":");
			output.println("</td>");
			output.println("</tr>");
			output.println("<tr>");
			output.println("<td colspan=\"2\" class=\"formParm2\">");
			output.println(this.getHeadlinePath(filePath));
			output.println("</td>");
			output.println("</tr>");
			
			output.println("<tr>");
			output.println("<td colspan=\"2\" class=\"formParm1\">");
			output.println(getResource("label.currentzip","current file") + ":");
			output.println("</td>");
			output.println("</tr>");
			
			output.println("<tr>");
			output.println("<td colspan=\"2\" class=\"formParm2\">");
			output.println("<div id=\"currentFile\" />");
			output.println("</td>");
			output.println("</tr>");

			output.println("<tr>");
			output.println("<td colspan=\"2\" class=\"formParm1\">");
			output.println(getResource("label.unzipresult","files extracted") + ":");
			output.println("</td>");
			output.println("</tr>");
			
			output.println("<tr>");
			output.println("<td colspan=\"2\" class=\"formParm2\">");
			output.println("<div id=\"extractCount\" />");
			output.println("</td>");
			output.println("</tr>");
			
			Enumeration entries = zipFile.entries();
			
			boolean unzipOk=true;

			boolean dirCreated=false;

			int unzipNum = 0;

			byte buffer[] = new byte[4096];

			while (entries.hasMoreElements()) {
				zipEntry = (ZipEntry) entries.nextElement();

				boolean showStatus = false;
				
                if (unzipNum < 100) {
                    showStatus = true;
                } else if (unzipNum < 1000) {
                    if (unzipNum % 10 == 0) {
                        showStatus = true;
                    }
                } else if (unzipNum < 5000) {
                    if (unzipNum % 50 == 0) {
                        showStatus = true;
                    }
                } else {
                    if (unzipNum % 100 == 0) {
                        showStatus = true;
                    }
                }
                    
                if (showStatus) {
                    output.println("<script language=\"javascript\">");
                    output.println("document.getElementById('currentFile').innerHTML='" + escapeForJavascript(CommonUtils.shortName(zipEntry.getName(),45)) + " (" + zipEntry.getSize() + " bytes)';");
                    output.println("document.getElementById('extractCount').innerHTML='" + unzipNum +  "';");
                    output.println("</script>");
                    output.flush();
                }

                String zipEntryPath = zipEntry.getName();

                if ((zipEntryPath.indexOf("..") >= 0) || (zipEntryPath.charAt(0) == '/')) {
                	LOG.warn("the ZIP archive " + filePath + " contains illegal entry " + zipEntryPath);
                } else {
					File zipOutFile = createUnzipFile(zipEntryPath);

					if (!(zipOutFile.isDirectory())) {
						BufferedOutputStream fout = null;

						try {
							InputStream zipInFile = zipFile.getInputStream(zipEntry);

							fout = new BufferedOutputStream(new FileOutputStream(zipOutFile));
							
							int bytesRead;
							while ((bytesRead = zipInFile.read(buffer)) >= 0) {
								fout.write(buffer, 0, bytesRead);
							}

							zipInFile.close();
							
							unzipNum++;
						} catch (Exception ioex4) {
							LOG.error("unzip error in file " + zipOutFile.getAbsolutePath() + ": " + ioex4);

							output.println("<font class=\"error\">");
							output.println(getResource("label.unzipError", "failed to unzip file") + ": " + zipEntryPath);
							output.println("</font><br><br>");
							unzipOk = false; 
						} finally {
                            if (fout != null) {
                                try {
									fout.close();
									
									if (!unzipOk) {
										if (zipOutFile.delete()) {
											LOG.debug("deleted incomplete unzipped file " + zipOutFile);
										}
									}
                                } catch (Exception ex) {
                                	LOG.warn(ex);
                                }
                            }
						}
					}
				
					if (zipEntry.getName().indexOf('/') >= 0) {
						dirCreated=true;
					}
                }
			}

			if (zipFile != null) {
			    try {
				    zipFile.close();
			    } catch (Exception ex) {
			    }
			}

			if (dirCreated) {
		        TestSubDirThread subDirThread = new TestSubDirThread(getCwd());
		        subDirThread.start();
			}

			String zipFileName = filePath.substring(filePath.lastIndexOf(File.separator)+1);

			output.println("<script language=\"javascript\">");
			output.println("document.getElementById('extractCount').innerHTML='" + unzipNum +  "';");
			output.println("</script>");
			output.flush();
            
			String returnUrl = null;
            
			if (!unzipOk) {
                output.println("<tr>");
                output.println("<td colspan=\"2\" class=\"formButton\">");
				returnUrl="/webfilesys/servlet?command=listFiles";
				output.println("<input type=\"button\" value=\"" + getResource("button.return","Return") + "\" onclick=\"window.location.href='" + returnUrl + "'\">");
                output.println("</td>");
                output.println("</tr>");
            } else {
				String deleteZipFile = getParameter("delZipFile");
				
				if (deleteZipFile == null) {
					// maybe we come from the UploadServlet
					
					deleteZipFile = (String) req.getAttribute("delZipFile");
				}
				
				if (deleteZipFile != null) {
					// do not ask what to do with the ZIP file
                    output.println("</table>");
					output.println("<script language=\"javascript\">");
					output.println("window.location.href='/webfilesys/servlet?command=fmdelete&fileName=" + UTF8URLEncoder.encode(zipFileName) + "&deleteRO=no';");
					output.println("</script>");
				} else {
                    output.println("<tr>");
                    output.println("<td class=\"formButton\">");
                    
                    String mobile = (String) session.getAttribute("mobile");
                    
                    if (mobile != null) {
                        returnUrl = "/webfilesys/servlet?command=mobile&cmd=folderFileList&keepListStatus=true";
                    } else {
                        returnUrl = "/webfilesys/servlet?command=listFiles&keepListStatus=true";
                    }
					output.print("<input type=\"button\" value=\"" + getResource("button.keepzip","keep ZIP file") + "\" onclick=\"");
					
					if ((mobile == null) && (dirCreated)) {
                        output.print("window.parent.DirectoryPath.location.href='/webfilesys/servlet?command=refresh&path=" + URLEncoder.encode(getCwd()) + "';");
					}
					output.println("window.location.href='" + returnUrl + "'\">");

                    output.println("</td>");
                    
                    output.println("<td class=\"formButton\" style=\"text-align:right\">");

					returnUrl = "/webfilesys/servlet?command=fmdelete&fileName=" + UTF8URLEncoder.encode(zipFileName) + "&deleteRO=no";
					output.print("<input type=\"button\" value=\"" + getResource("button.delzip","delete ZIP file") + "\" onclick=\"");
					
					if ((mobile == null) && (dirCreated)) {
						output.print("window.parent.DirectoryPath.location.href='/webfilesys/servlet?command=refresh&path=" + UTF8URLEncoder.encode(getCwd()) + "';");
					}
					
					output.println("window.location.href='" + returnUrl + "'\">");
                    
                    output.println("</td>");
                    output.println("</tr>");
				}

				if (WebFileSys.getInstance().isAutoCreateThumbs()) {
					if (dirCreated) {
						AutoThumbnailCreator.getInstance().queuePath(getCwd(), AutoThumbnailCreator.SCOPE_TREE);
					} else {
						AutoThumbnailCreator.getInstance().queuePath(getCwd(), AutoThumbnailCreator.SCOPE_DIR);
					}
				}
			}

            output.println("</table>");

			output.println("</form>");
		}
		else  // ZIP
		{
			headLine(getResource("label.ziphead","create ZIP archive"));

			String zipFilePath = "";

			if (filePath.lastIndexOf('.') < 0) {
				zipFilePath = filePath + ".zip";
			} else {
				zipFilePath = filePath.substring(0, filePath.lastIndexOf('.')) + ".zip";
			}

			output.println("<br/>");
            output.println("<form>");

            output.println("<table class=\"dataForm\" width=\"100%\">");
			output.println("<tr>");
            output.println("<td colspan=\"2\" class=\"formParm1\">");
			output.println(getResource("label.currentzip","Compressing file") + ":");
            output.println("</td>");
            output.println("</tr>");
			output.println("<tr>");
            output.println("<td colspan=\"2\" class=\"formParm2\">");
			output.println(this.getHeadlinePath(filePath));
			output.println("</td>");
            output.println("</tr>");

			output.flush();

			String zipFileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1, filePath.length());
			
			ZipOutputStream zipOut = null;
			BufferedInputStream fin = null;

			try {
				zipOut = new ZipOutputStream(new FileOutputStream(zipFilePath));

				zipOut.putNextEntry(new ZipEntry(zipFileName));

				fin = new BufferedInputStream(new FileInputStream(filePath));

				int count;
				
				byte [] buff = new byte[4096];

				while ((count = fin.read(buff)) >= 0) {
					zipOut.write(buff, 0, count);
				}
				
				zipOut.closeEntry();
			} catch (Exception ex) {
				LOG.error("failed to create ZIP file " + zipFilePath, ex);
				return;
			} finally {
				if (zipOut != null) {
					try {
						zipOut.close();
					} catch (Exception ioEx) {
					}
				}
				if (fin != null) {
					try {
						fin.close();
					} catch (Exception ioEx) {
					}
				}
			}

			File sourceFile = new File(filePath);
			File zipFile = new File(zipFilePath);

			long sourceLength = sourceFile.length();

			if (sourceLength > 0L) {  // prevent division by zero
                output.println("<tr>");
                output.println("<td colspan=\"2\" class=\"formParm1\">");
				output.println(getResource("label.zipratio","compression ratio (% of original size)") + ":");
                output.println("</td>");
                output.println("</tr>");
                output.println("<tr>");
				output.println("<td colspan=\"2\" class=\"formParm2\">");
				output.println((zipFile.length() * 100L) / sourceFile.length());
				output.println(" %");
                output.println("</td>");
                output.println("</tr>");
			}

            output.println("<tr>");
            output.println("<td class=\"formButton\">");
            
            output.print("<input type=\"button\" onclick=\"window.location.href='/webfilesys/servlet?command=listFiles&keepListStatus=true'\""); 
            output.println(" value=\"" + getResource("button.keepsource","Keep Source File") + "\" />");

            output.println("</td>");

            output.println("<td class=\"formButton\" style=\"text-align:right;\">");

            output.print("<input type=\"button\" onclick=\"window.location.href='/webfilesys/servlet?command=fmdelete&fileName=" + UTF8URLEncoder.encode(zipFileName) + "&deleteRO=no'\""); 
            output.println(" value=\"" + getResource("button.delsource","Delete Source File") + "\" />");
            
            output.println("</td>");
            
            output.println("</tr>");

            output.println("</table>");
            
			output.println("</form>");
		}

		output.print("</body>");
		output.println("</html>");
		output.flush();
	}
	
	private boolean isZipFile(String ext) {
        return ext.equals(".zip") ||
               ext.equals(".gzip") || 
               ext.equals(".jar") || 
               ext.equals(".ear") || 
               ext.equals(".war");
	}
	
	protected File createUnzipFile(String zip_fn) {
		File zipOutFile = new File(getCwd(), zip_fn);

		if (zip_fn.indexOf('/') >= 0) {
			String dir_name = zip_fn.substring(0, zip_fn.lastIndexOf('/'));

			File dir = new File(getCwd(), dir_name);

			if (dir.exists()) {
				return(zipOutFile);
			}

			if (!dir.mkdirs()) {
				LOG.error("Cannot create output directory " + dir);
			}
			return(zipOutFile);
		}

		return(zipOutFile);
	}
}
