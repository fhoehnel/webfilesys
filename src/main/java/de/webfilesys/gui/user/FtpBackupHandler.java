package de.webfilesys.gui.user;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketException;
import java.text.DecimalFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

import de.webfilesys.util.CommonUtils;

/**
 * @author Frank Hoehnel
 */
public class FtpBackupHandler extends UserRequestHandler
{
	private long bytesTransferred = 0;
	
	private int filesTransferred = 0;

	DecimalFormat numFormat = null;
	
	public FtpBackupHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid)
	{
        super(req, resp, session, output, uid);

		numFormat = new DecimalFormat("#,###,###,###,###");
	}

	protected void process()
	{
		String actPath=getParameter("actPath");
		
		if ((actPath == null) || (actPath.trim().length() == 0))
		{
			actPath = getCwd();
		}

		if (!this.checkAccess(actPath))
		{
			return;
		}

        String ftpServerName = getParameter("ftpServerName");

		if (ftpServerName == null)
		{
			ftpParamForm(null);

			return;
		}
		
		StringBuffer alertText = new StringBuffer();
		
		if (ftpServerName.trim().length() == 0)
		{
			alertText.append(getResource("alert.missingFtpServer","FTP Server is a required field") + "\\n");
		}

		String userid = getParameter("userid");

		if (userid.trim().length() == 0)
		{
			alertText.append(getResource("alert.missingFtpUserid","Userid is a required field") + "\\n");
		}

		String password = getParameter("password");

		if (password.trim().length() == 0)
		{
			alertText.append(getResource("alert.missingFtpPassword","Password is a required field") + "\\n");
		}

		String remoteDir = getParameter("remoteDir");

        if (alertText.length() > 0)
        {
			ftpParamForm(alertText.toString());  
			
			return;      	
        }
        
		output.println("<HTML>");
		output.println("<HEAD>");

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/css/" + userMgr.getCSS(uid) + ".css\">");
		output.println("</HEAD>");

		output.println("<BODY>");

		headLine(getResource("label.ftpBackupHead","Backup to FTP Server"));

		output.println("<br>");

		output.println("<form accept-charset=\"utf-8\" name=\"form1\">");

		output.println("<table class=\"dataForm\" width=\"100%\">");

		output.println("<tr><td class=\"formParm1\">");
		output.println(getResource("label.ftpServerName","Name or IP of FTP server") + ":");
		output.println("</td>");
		output.println("<td class=\"formParm2\">");
		output.println(ftpServerName);
		output.println("</td></tr>");

		output.println("<tr><td colspan=\"2\" class=\"formParm1\">");
		output.println(getResource("label.ftpLocalDir","Local Folder to backup") + ":");
		output.println("</td></tr>");

		output.println("<tr><td colspan=\"2\" class=\"formParm2\">");
		output.println(CommonUtils.shortName(getHeadlinePath(actPath),70));
		output.println("</td></tr>");
		
		output.println("<tr><td colspan=\"2\" class=\"formParm1\">");
		output.println(getResource("label.currentcopy","current file") + ":");
		output.println("</td></tr>");

		output.println("<tr><td colspan=\"2\" class=\"formParm2\">");
        output.println("<span id=\"currentFile\"/>");
		output.println("</td></tr>");

		output.println("<tr><td colspan=\"2\" class=\"formParm1\">");
		output.println(getResource("label.xferStatus","transferred") + ":");
		output.println("</td></tr>");

		output.println("<tr><td colspan=\"2\" class=\"formParm2\">");
		output.println("<span id=\"xferStatus\"/>");
		output.println("</td></tr>");

		output.println("</table>");
		
		output.println("</form>");
        
        String subDir = null;
        
        int lastSepIdx = actPath.lastIndexOf(File.separator);
        
        if ((lastSepIdx >= 0 ) && (lastSepIdx < (actPath.length() - 1)))
        {
        	subDir = actPath.substring(lastSepIdx + 1);
        }
        
        boolean recursive = (getParameter("recursive") != null);
        
        FTPClient ftpClient = new FTPClient();
        
        try
        {
			ftpClient.connect(ftpServerName);
			
			Logger.getLogger(getClass()).debug("FTP connect to " + ftpServerName + " response: " + ftpClient.getReplyString());

			int reply = ftpClient.getReplyCode();

			if (FTPReply.isPositiveCompletion(reply)) 
			{
				if (ftpClient.login(userid, password))
				{
                    ftpClient.setFileType(FTP.IMAGE_FILE_TYPE);

					if ((remoteDir.trim().length() > 0) && (!ftpClient.changeWorkingDirectory(remoteDir)))
					{
						javascriptAlert("FTP remote directory " + remoteDir + " does not exist!");
					}
                    else
                    {
                    	boolean remoteChdirOk = true;
                    	
						if (!ftpClient.changeWorkingDirectory(subDir))
						{
							if (!ftpClient.makeDirectory(subDir))
							{
								Logger.getLogger(getClass()).warn("FTP cannot create remote directory " + subDir);
								remoteChdirOk = false;
							}
							else
							{
								Logger.getLogger(getClass()).debug("FTP created new remote directory " + subDir);
									
								if (!ftpClient.changeWorkingDirectory(subDir))
								{
									Logger.getLogger(getClass()).warn("FTP cannot chdir to remote directory " + subDir);
									remoteChdirOk = false;
								}
							}
						}

                        if (remoteChdirOk)
                        {                    	
							if (!backupDir(ftpClient, actPath, recursive, output))
							{
								javascriptAlert("FTP backup failed");
							}
							else
							{
		                        output.println("<script language=\"javascript\">");
		                        output.println("document.getElementById('currentFile').innerHTML='';");
		                        output.println("</script>");
							}
                        }
                        else
                        {
                        	javascriptAlert("FTP cannot create remote subdirectory " + subDir);
                        }
                    }
				}
				else
				{
					Logger.getLogger(getClass()).info("FTP connect to " + ftpServerName + " login failed");
					javascriptAlert("Login to FTP server " + ftpServerName + " failed");
				}
			}
			else
			{
				Logger.getLogger(getClass()).warn("FTP connect to " + ftpServerName + " response: " + reply);
				javascriptAlert("Login to FTP server " + ftpServerName + " failed");
			}

            ftpClient.logout();

			ftpClient.disconnect();

			// output.println("<br>" + filesTransferred + " files (" + bytesTransferred / 1024 + " KB) transferred");
        }
		catch (SocketException sockEx)
		{
			Logger.getLogger(getClass()).warn(sockEx);
			javascriptAlert("FTP transfer failed: " + sockEx);
		}
		catch (IOException ioEx)
		{
			Logger.getLogger(getClass()).warn(ioEx);
			javascriptAlert("FTP transfer failed: " + ioEx);
		}

		output.println("<center><FORM><INPUT type=\"Button\" VALUE=\"" + getResource("button.closewin","Close Window") + "\" onClick=\"self.close()\"></FORM></center>");
    
        output.flush();
	}

    protected void ftpParamForm(String alertText)
    {
        String actPath = getParameter("actPath");
        
        if ((actPath == null) || (actPath.trim().length() == 0))
        {
        	actPath = getCwd();
        }

		output.println("<html><head>");

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/css/" + userMgr.getCSS(uid) + ".css\">");

        /*
		output.println("<script language=\"javascript\">"); 
		output.println("function validate()");
		output.println("{if (document.form1.ftpServerName.value.length()==0')");
		output.println("{alert('" + getResource("alert.destEqualsSource","new file name must be different") + "');}");
		output.println("else");
		output.println("{document.form1.submit();}}");
		output.println("</script>"); 
        */

        if (alertText != null)
        {
            javascriptAlert(alertText);
        }

		output.println("</head>"); 
		output.println("<body>");

		headLine(getResource("label.ftpBackupHead","Backup to FTP Server"));

		output.print("<form accept-charset=\"utf-8\" name=\"form1\" method=\"post\" action=\"/webfilesys/servlet\">");
		output.print("<input type=\"hidden\" name=\"command\" value=\"ftpBackup\">");
		output.print("<input type=\"hidden\" name=\"actPath\" value=\"" + actPath + "\">");

		output.println("<table class=\"dataForm\" width=\"100%\">");

		output.println("<tr>");
		output.println("<td class=\"formParm1\">");
		output.println(getResource("label.ftpLocalDir","Local Folder to backup") + ":");
		output.println("</td>");
		output.println("<td class=\"formParm2\">");
        output.println(CommonUtils.shortName(getHeadlinePath(actPath),40));
		output.println("</td>");
		output.println("</tr>");

		String val = getParameter("ftpServerName");
		if (val == null)
		{
			val="";
		}

		output.println("<tr>");
		output.println("<td class=\"formParm1\">");
		output.println(getResource("label.ftpServerName","Name or IP of FTP server") + ":");
		output.println("</td>");
		output.println("<td class=\"formParm2\">");
		output.println("<input name=\"ftpServerName\" value=\"" + val + "\" maxlength=\"128\" style=\"width:250px\">");
		output.println("</td>");
		output.println("</tr>");

		val = getParameter("userid");
		if (val == null)
		{
			val="";
		}

		output.println("<tr>");
		output.println("<td class=\"formParm1\">");
		output.println(getResource("label.ftpUserid","Userid") + ":");
		output.println("</td>");
		output.println("<td class=\"formParm2\">");
		output.println("<input type=\"text\" name=\"userid\" value=\"" + val + "\" maxlength=\"64\" style=\"width:250px;\">");
		output.println("</td>");
		output.println("</tr>");

		output.println("<tr>");
		output.println("<td class=\"formParm1\">");
		output.println(getResource("label.ftpPassword","Password") + ":");
		output.println("</td>");
		output.println("<td class=\"formParm2\">");
		output.println("<input type=\"password\" name=\"password\" maxlength=\"64\" style=\"width:250px;\">");
		output.println("</td>");
		output.println("</tr>");

		val = getParameter("remoteDir");
		if (val == null)
		{
			val="";
		}

		output.println("<tr>");
		output.println("<td class=\"formParm1\">");
		output.println(getResource("label.ftpDir","Remote Directory (optional)") + ":");
		output.println("</td>");
		output.println("<td class=\"formParm2\">");
		output.println("<input type=\"text\" name=\"remoteDir\" value=\"" + val + "\" maxlength=\"256\" style=\"width:250px;\">");
		output.println("</td>");
		output.println("</tr>");

		output.println("<tr>");
		output.println("<td colspan=\"2\" class=\"formParm1\">");
		output.print("<input type=\"checkbox\" class=\"cb2\" name=\"recursive\" value=\"true\"");
		if (getParameter("recursive") != null)
		{
			output.print(" checked");
		}
		output.println(">&nbsp;");
		output.println(getResource("label.ftpRecurse","Include Subfolders"));
		output.println("</td>");
		output.println("</tr>");

		output.println("<tr><td colspan=\"2\">&nbsp;</td></tr>");

		output.println("<tr>");
		output.println("<td>");
		output.println("<input type=\"submit\" value=\"" + getResource("button.ftpBackup","Start FTP Backup") + "\">");
		output.println("</td>");
		output.println("<td align=\"right\">");
		output.println("<input type=\"button\" value=\"" + getResource("button.cancel","Cancel") + "\" onclick=\"self.close()\">");
		output.println("</td>");
		output.println("</tr>");

		output.println("</table>");

		output.println("</form>");

		output.println("<script language=\"javascript\">");
		output.println("document.form1.ftpServerName.focus();");
		output.println("</script>");

		output.println("</body>");

		output.println("</html>");
		output.flush();
    }
    
    private boolean backupDir(FTPClient ftpClient, String localPath,
                              boolean recursive, PrintWriter output)
    throws IOException
    {
    	boolean error = false;
    	
		File localDirFile = new File(localPath);
			
		if (localDirFile.isDirectory() && (localDirFile.canRead()))
		{
			File localFileList[] = localDirFile.listFiles();
				
			if (localFileList != null)
			{
				for (int i=0; i < localFileList.length; i++)
				{
					File localFile = localFileList[i];
						
					if (localFile.isDirectory())
					{
						if (recursive)
						{
							String subDir = localFile.getName();
							
							String localPathChild = null;
							
							if (localPath.endsWith(File.separator))
							{
								localPathChild = localPath + subDir;
							}
							else
							{
								localPathChild = localPath + File.separator + subDir;
							}

                            boolean remoteChdirOk = true;

							if (!ftpClient.changeWorkingDirectory(subDir))
							{
								if (!ftpClient.makeDirectory(subDir))
								{
									Logger.getLogger(getClass()).warn("FTP cannot create remote directory " + subDir);
                                    remoteChdirOk = false;
								}
								else
								{
									Logger.getLogger(getClass()).debug("FTP created new remote directory " + subDir);
									
									if (!ftpClient.changeWorkingDirectory(subDir))
									{
										Logger.getLogger(getClass()).warn("FTP cannot chdir to remote directory " + subDir);
										remoteChdirOk = false;
									}
								}
							}

                            if (remoteChdirOk)
                            {
								if (!backupDir(ftpClient, localPathChild, recursive, output))
								{
									error = true;
								}

								if (!ftpClient.changeWorkingDirectory(".."))
								{
									Logger.getLogger(getClass()).warn("FTP cannot chdir .. from " + subDir);
									return(false);
								}
                            }
                            else
                            {
                            	error = true;
                            }
						}
					}
					else
					{
						output.println("<script language=\"javascript\">");
						output.println("document.getElementById('currentFile').innerHTML='" + insertDoubleBackslash(CommonUtils.shortName(localPath.replace('\\','/') + "/" + localFile.getName(),64)) + "';");
						output.println("</script>");
						try
						{
							FileInputStream fin = new FileInputStream(localFile);
						
							if (!ftpClient.storeFile(localFile.getName(), fin))
							{
								Logger.getLogger(getClass()).warn("FTP put file " + localPath + "/" + localFile.getName() + " failed");
                        	
								error = true;
							}
							else
							{
								Logger.getLogger(getClass()).debug("FTP put of file " + localPath + "/" + localFile.getName() + " successful");
								
								filesTransferred ++;
								
								bytesTransferred += localFile.length();

                                String temp = filesTransferred + " files (" + numFormat.format(bytesTransferred / 1024) + " KB)";

								output.println("<script language=\"javascript\">");
								output.println("document.getElementById('xferStatus').innerHTML='" + temp + "';");
								output.println("</script>");
							}

							output.flush();
							
							fin.close();
                        }
                        catch (IOException ioex)
                        {
							Logger.getLogger(getClass()).warn("FTP put file " + localPath + "/" + localFile.getName() + " failed: " + ioex);
                      
                            output.println("<br>FTP put file " + localPath + "/" + localFile.getName() + " failed: " + ioex);
                      
                            error = true;
                        }
					}
				}
			}
		}
		else
		{
			Logger.getLogger(getClass()).warn("FTP local directory " + localPath + " is not readable");
			
			return(false);
		}

        return(!error);			
    }

}
