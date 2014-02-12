package de.webfilesys.gui;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.WebFileSys;
import de.webfilesys.user.UserManager;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.HTTPUtils;

/**
 * @author Frank Hoehnel
 */
public class ProtectedRequestHandler extends RequestHandler
{  
    public String uid = null;
     
    protected long treeFileSize = 0L;
    
    protected UserManager userMgr = null;
    
    public ProtectedRequestHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid)
    {
        super(req, resp, session, output);

        this.uid = uid;
        
        userMgr = WebFileSys.getInstance().getUserMgr();
    }

    public void handleRequest()
    {
        process();
    }

    protected void process()
    {
    }
    
	protected boolean isAdminUser(boolean sendErrorPage)
	{
		String role = userMgr.getRole(uid);

		if ((role != null) && role.equals("admin"))
		{
			return(true);
		}

		if (!sendErrorPage)
		{
			return(false);
		}

        output.print(HTTPUtils.createHTMLHeader());

		output.println("<HTML>");
		output.println("<HEAD>");
		output.println("<TITLE> WebFileSys Administration </TITLE>");
		output.println("</HEAD>");
		output.println("<BODY>");
		output.println("You are not an admin user!");
		output.println("</html></body>");
		output.flush();
		return(false);
	}
	
	protected boolean accessAllowed(String fileName)
	{
		if (fileName.indexOf("..") >=0)
		{
			return(false);
		}

		if (File.separatorChar=='\\')   // WIN
		{
			String lowerCaseDocRoot = userMgr.getLowerCaseDocRoot(uid);

			String formattedDocName=fileName.toLowerCase().replace('\\','/');

			if (lowerCaseDocRoot.charAt(0)=='*')
			{
				// may be this branch is not needed
				// because if document root starts with "*" the user has full access anyway
				// if this branch makes sense it should get the same test for
				// doc length or slash at doc root length index as below

				return(formattedDocName.substring(2).startsWith(lowerCaseDocRoot.substring(2)));
			}

			return(formattedDocName.startsWith(lowerCaseDocRoot) &&
				   ((formattedDocName.length()==lowerCaseDocRoot.length()) ||
					(formattedDocName.charAt(lowerCaseDocRoot.length())=='/')));
		}

		String docRoot = userMgr.getDocumentRoot(uid);

		if (docRoot.equals("/"))
		{
			return(true);
		}

		return(fileName.startsWith(docRoot) &&
			   ((fileName.length()==docRoot.length()) ||
				(fileName.charAt(docRoot.length())=='/')));
	}

	protected boolean checkAccess(String fileName)
	{
		if (accessAllowed(fileName))
		{
			return(true);
		}

		Logger.getLogger(getClass()).warn("user " + uid + " tried to access file outside of the document root: " + fileName);

		if (output == null)
		{
			try
			{
				output = new PrintWriter(resp.getWriter());
			}
			catch (IOException ioex)
			{
				return(false);
			}
		}
		
		output.println("<HTML>");
		output.println("<HEAD>");
		output.println("<TITLE> Unauthorized access </TITLE>");
		output.println("<script language=\"javascript\">");
		output.println("alert('You are trying to access a file outside of your document root!');");
		output.println("history.back();");
		output.println("</script>");

		output.println("</html>");
		output.flush();

		return(false);
	}
	
	public boolean copy_file(String source_filename,String dest_filename,boolean display_result)
	{
		if (source_filename.equals(dest_filename))
		{
			Logger.getLogger(getClass()).warn("copy_file: copy source equals destination: " + source_filename); 
			return(false);
		}

		BufferedInputStream f_in;
		BufferedOutputStream f_out;
		int count;
		int copy_sum;
		boolean copy_failed;
		byte [] buff = new byte[4096];

		long lastChangeDate=(new Date()).getTime();

		File sourceFile=new File(source_filename);
		lastChangeDate=sourceFile.lastModified();

		copy_failed=false;
		copy_sum=0;

		try
		{
			f_in = new BufferedInputStream(new FileInputStream(source_filename));
			f_out = new BufferedOutputStream(new FileOutputStream(dest_filename));

			while (( count = f_in.read(buff))>=0 )
			{
				f_out.write(buff,0,count);
				copy_sum+=count;
			}

			f_in.close();
			f_out.close();
		}
		catch (Exception e)
		{
			Logger.getLogger(getClass()).error("failed to copy file", e);
			copy_failed=true;
		}

		if (!copy_failed)
		{
			File destFile=new File(dest_filename);
			destFile.setLastModified(lastChangeDate);
		}

		if (display_result)
		{
			if (copy_failed)
			{
				output.println("*** cannot copy " + source_filename + " to " + dest_filename + "<br>");
			}
			else
			{
				output.println("<nobr>" + source_filename + " successfully copied</nobr><br>");
			}

			output.flush();
		}
		
		return(!copy_failed);
	}
	
	public int zipTree(String actPath,String relativePath,ZipOutputStream zipOut,int fileCount)
	{
		int zipFileNum=fileCount;

		File actDir=new File(actPath);

		String fileList[]=actDir.list();

		if (fileList.length==0)
		{
			return(zipFileNum);
		}

		for (int i=0;i<fileList.length;i++)
		{
			File tempFile=new File(actPath + File.separator + fileList[i]);

			if (tempFile.isDirectory())
			{
				zipFileNum=zipTree(actPath + File.separator + fileList[i],
								   relativePath + fileList[i] + "/",
								   zipOut,zipFileNum);
			}
			else
			{
				String fullFileName = actPath + File.separator + fileList[i];
				String relativeFileName = relativePath + fileList[i];

				try
				{
					ZipEntry newZipEntry=new ZipEntry(relativeFileName);

					zipOut.putNextEntry(newZipEntry);

					FileInputStream inStream=null;

					try
					{
						File originalFile=new File(fullFileName);

						inStream=new FileInputStream(originalFile);

						byte buff[]=new byte[4096];
						int count;

						while ((count=inStream.read(buff)) >= 0)
						{
							zipOut.write(buff,0,count);
						}

						long originalSize=originalFile.length();

						treeFileSize+=originalSize;

						zipFileNum++;

						 // long compressedSize=newZipEntry.getCompressedSize();

		                boolean showStatus = false;
		                
		                if (zipFileNum < 100)
		                {
		                    showStatus = true;
		                }
		                else if (zipFileNum < 1000)
		                {
		                    if (zipFileNum % 10 == 0)
		                    {
		                        showStatus = true;
		                    }
		                } 
		                else if (zipFileNum < 5000)
		                {
		                    if (zipFileNum % 50 == 0)
		                    {
		                        showStatus = true;
		                    }
		                }
		                else
		                {
		                    if (zipFileNum % 100 == 0)
		                    {
		                        showStatus = true;
		                    }
		                }

		                if (showStatus)
						{
	                        output.println("<script language=\"javascript\">");
	                        output.println("document.getElementById('currentDir').innerHTML=\"" + insertDoubleBackslash(CommonUtils.shortName(relativeFileName, 50)) + "\";");
                            output.println("document.getElementById('compressCount').innerHTML=\"" + zipFileNum + "\";");
	                        output.println("</script>");
	                        output.flush();
						}
					}
					catch (Exception zioe)
					{
						Logger.getLogger(getClass()).error("failed to zip file " + fullFileName, zioe);
						output.println("<font color=\"red\">failed to zip file " + fullFileName + "</font><br/>");
						output.flush();
					}

					inStream.close();
				}
				catch (IOException ioex)
				{
                    Logger.getLogger(getClass()).error("error during zipping file " + fullFileName, ioex);
					output.println("<font color=\"red\">failed to zip file " + fullFileName + "</font><br/>");
				}
			}
		}

		return(zipFileNum);
	}

    public String getUserCSSName()
    {
		return(userMgr.getCSS(uid));    		
    }
    
    public String getUid()
    {
    	return(uid);
    }
}
