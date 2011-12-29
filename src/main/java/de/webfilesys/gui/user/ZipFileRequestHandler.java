package de.webfilesys.gui.user;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
public class ZipFileRequestHandler extends UserRequestHandler
{
	public ZipFileRequestHandler(
			HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid)
	{
        super(req, resp, session, output, uid);
	}

	protected void process()
	{
		if (!checkWriteAccess())
		{
			return;
		}

		String filePath = getParameter("filePath");

		if (filePath == null)
		{
			// we come from UploadServlet
			
			filePath = (String) req.getAttribute("filePath");
		}
		
		if (!checkAccess(filePath))
		{
			return;
		}

		String zipFileName = "";
		ZipOutputStream zipOut;

		output.println("<HTML>");
		output.println("<HEAD>");

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/css/" + userMgr.getCSS(uid) + ".css\">");

		output.println("</HEAD><BODY>");

        String file_ext = CommonUtils.getFileExtension(filePath);

	    if (isZipFile(file_ext))
		{
			headLine(getResource("label.unziphead","Unzip file"));

			ZipFile zipFile=null;
			ZipEntry zipEntry=null;

			try
			{
				zipFile = new ZipFile(filePath);
			}
			catch (IOException ioex)
			{
				Logger.getLogger(getClass()).error("ZIP file format error: " + ioex);
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

			boolean unzip_okay=true;

			boolean dirCreated=false;

			int unzipNum = 0;

			byte buffer[] = new byte[4096];

			while (entries.hasMoreElements())
			{
				zipEntry=(ZipEntry) entries.nextElement();

				boolean showStatus = false;
				
                if (unzipNum < 100)
                {
                    showStatus = true;
                }
                else if (unzipNum < 1000)
                {
                    if (unzipNum % 10 == 0)
                    {
                        showStatus = true;
                    }
                } 
                else if (unzipNum < 5000)
                {
                    if (unzipNum % 50 == 0)
                    {
                        showStatus = true;
                    }
                }
                else
                {
                    if (unzipNum % 100 == 0)
                    {
                        showStatus = true;
                    }
                }
                    
                if (showStatus)
                {
                    output.println("<script language=\"javascript\">");
                    output.println("document.getElementById('currentFile').innerHTML='" + CommonUtils.shortName(zipEntry.getName(),45) + " (" + zipEntry.getSize() + " bytes)';");
                    output.println("document.getElementById('extractCount').innerHTML='" + unzipNum +  "';");
                    output.println("</script>");
                    output.flush();
                }

                String zipEntryPath = zipEntry.getName();

                if ((zipEntryPath.indexOf("..") >= 0) || (zipEntryPath.charAt(0) == '/'))
                {
                	Logger.getLogger(getClass()).warn("the ZIP archive " + filePath + " contains illegal entry " + zipEntryPath);
                }
                else
                {
					File zipOutFile = createUnzipFile(zipEntryPath);

					if (!(zipOutFile.isDirectory()))
					{
						FileOutputStream destination = null;

						try
						{
							InputStream zipInFile = zipFile.getInputStream(zipEntry);

							destination = new FileOutputStream(zipOutFile);

							boolean done = false;
							while (!done)
							{
								int bytesRead = zipInFile.read(buffer);

								if (bytesRead == -1)
								{
									done=true;
								}
								else
								{
									destination.write(buffer, 0, bytesRead);
								}
							}

							unzipNum++;
						}
						catch (IOException ioex4)
						{
							Logger.getLogger(getClass()).error("unzip error in file " + zipOutFile + ": " + ioex4);

							output.println("<font class=\"error\">");
							output.println(getResource("label.unzipError", "Error in unzip of file") + ": " + zipEntryPath);
							output.println("</font><br><br>");
							unzip_okay=false; 
						}
						catch (NullPointerException nullEx)
						{
							Logger.getLogger(getClass()).error("unzip error in file " + zipOutFile + ": " + nullEx);

							output.println("<font class=\"error\">");
							output.println(getResource("label.unzipError", "Error in unzip of file") + ": " + zipEntryPath);
							output.println("</font><br><br>");
							unzip_okay=false; 
						}
						finally
						{
                            if (destination != null)
                            {
                                try
                                {
									destination.close();
									
									if (!unzip_okay)
									{
										if (zipOutFile.delete())
										{
											Logger.getLogger(getClass()).debug("deleted incomplete unzipped file " + zipOutFile);
										}
									}
                                }
                                catch (Exception ex)
                                {
                                	Logger.getLogger(getClass()).warn(ex);
                                }
                            }

						}
					}
				
					if (zipEntry.getName().indexOf('/') >= 0)
					{
						dirCreated=true;
					}
                }
			}

			try
			{
				zipFile.close();
			}
			catch (IOException ioex2)
			{
				System.out.println(ioex2);
			}

			if (dirCreated)
			{
		        TestSubDirThread subDirThread = new TestSubDirThread(getCwd());

		        subDirThread.start();
			}

			String fn_only=filePath.substring(filePath.lastIndexOf(File.separator)+1);

			output.println("<script language=\"javascript\">");
			output.println("document.getElementById('extractCount').innerHTML='" + unzipNum +  "';");
			output.println("</script>");
			output.flush();
            
			String returnUrl = null;

            
			if (!unzip_okay)
			{
                output.println("<tr>");
                output.println("<td colspan=\"2\" class=\"formButton\">");
				returnUrl="/webfilesys/servlet?command=listFiles";
				output.println("<input type=\"button\" value=\"" + getResource("button.return","Return") + "\" onclick=\"window.location.href='" + returnUrl + "'\">");
                output.println("</td>");
                output.println("</tr>");
            }
			else
			{
				String deleteZipFile = getParameter("delZipFile");
				
				if (deleteZipFile == null)
				{
					// maybe we come from the UploadServlet
					
					deleteZipFile = (String) req.getAttribute("delZipFile");
				}
				
				if (deleteZipFile != null)
				{
					// do not ask what to do with the ZIP file
                    output.println("</table>");
					output.println("<script language=\"javascript\">");
					output.println("window.location.href='/webfilesys/servlet?command=fmdelete&fileName=" + UTF8URLEncoder.encode(fn_only) + "&deleteRO=no';");
					output.println("</script>");
				}
				else
				{
                    output.println("<tr>");
                    output.println("<td class=\"formButton\">");
                    
                    String mobile = (String) session.getAttribute("mobile");
                    
                    if (mobile != null)
                    {
                        returnUrl = "/webfilesys/servlet?command=mobile&cmd=folderFileList&keepListStatus=true";
                    }
                    else
                    {
                        returnUrl = "/webfilesys/servlet?command=listFiles&keepListStatus=true";
                    }
					output.print("<input type=\"button\" value=\"" + getResource("button.keepzip","keep ZIP file") + "\" onclick=\"");
					
					if ((mobile == null) && (dirCreated))
                    {
                        output.print("window.parent.DirectoryPath.location.href='/webfilesys/servlet?command=refresh&path=" + URLEncoder.encode(getCwd()) + "';");
					}
					output.println("window.location.href='" + returnUrl + "'\">");

                    output.println("</td>");
                    
                    output.println("<td class=\"formButton\" style=\"text-align:right\">");

					returnUrl = "/webfilesys/servlet?command=fmdelete&fileName=" + UTF8URLEncoder.encode(fn_only) + "&deleteRO=no";
					output.print("<input type=\"button\" value=\"" + getResource("button.delzip","delete ZIP file") + "\" onclick=\"");
					
					if ((mobile == null) && (dirCreated))
					{
						output.print("window.parent.DirectoryPath.location.href='/webfilesys/servlet?command=refresh&path=" + UTF8URLEncoder.encode(getCwd()) + "';");
					}
					
					output.println("window.location.href='" + returnUrl + "'\">");
                    
                    output.println("</td>");
                    output.println("</tr>");
				}

				if (WebFileSys.getInstance().isAutoCreateThumbs())
				{
					if (dirCreated)
					{
						AutoThumbnailCreator.getInstance().queuePath(getCwd(), AutoThumbnailCreator.SCOPE_TREE);
					}
					else
					{
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

			if (filePath.lastIndexOf('.')<0)
			{
				zipFileName = filePath + ".zip";
			}
			else
			{
				zipFileName = filePath.substring(0,filePath.lastIndexOf('.')) + ".zip";
			}

			try
			{
				zipOut = new ZipOutputStream(new FileOutputStream(zipFileName));
			}
			catch (FileNotFoundException fnfEx)
			{
				Logger.getLogger(getClass()).error("cannot create ZIP file " + zipFileName + " : " + fnfEx);
				return;
			}

			output.println("<br>");
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

			String fn_only=filePath.substring(filePath.lastIndexOf(File.separator)+1,filePath.length());

			try
			{
				zipOut.putNextEntry(new ZipEntry(fn_only));
			}
			catch (IOException ioEx)
			{
				Logger.getLogger(getClass()).error("Cannot write ZIP entry " + fn_only + " : " + ioEx);
				return;
			}

			try
			{
				FileInputStream fin = new FileInputStream(filePath);

				int count;
				
				byte [] buff = new byte[4096];

				while (( count = fin.read(buff)) >= 0 )
				{
					zipOut.write(buff, 0, count);
				}

				fin.close();
			}
			catch (Exception zipEx)
			{
				Logger.getLogger(getClass()).error("Cannot write ZIP entry " + fn_only + " : " + zipEx);
				return;
			}

			try
			{
				zipOut.close();
			}
			catch (IOException ioEx)
			{
				Logger.getLogger(getClass()).error("Cannot close ZIP file " + zipFileName + " : " + ioEx);
				return;
			}

			File sourceFile = new File(filePath);
			File zipFile = new File(zipFileName);

			long sourceLength=sourceFile.length();

			if (sourceLength > 0L)  // prevent division by zero
			{
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

            output.print("<input type=\"button\" onclick=\"window.location.href='/webfilesys/servlet?command=fmdelete&fileName=" + UTF8URLEncoder.encode(fn_only) + "&deleteRO=no'\""); 
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
	
	private boolean isZipFile(String ext) 
	{
        return (ext.equals(".zip") ||
                ext.equals(".gzip") || 
                ext.equals(".jar") || 
                ext.equals(".ear") || 
                ext.equals(".war"));
	    
	}
	
	protected File createUnzipFile(String zip_fn)
	{
		File zipOutFile = new File(getCwd(),zip_fn);

		if (zip_fn.indexOf('/')>=0)
		{
			String dir_name=zip_fn.substring(0,zip_fn.lastIndexOf('/'));

			File dir=new File(getCwd(),dir_name);

			if (dir.exists())
				return(zipOutFile);

			if (!dir.mkdirs())
				System.out.println("Cannot create output directory " + dir);
			return(zipOutFile);
		}

		return(zipOutFile);
	}
}
