package de.webfilesys.gui;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


import de.webfilesys.WebFileSys;
import de.webfilesys.user.UserManager;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.HTTPUtils;

/**
 * @author Frank Hoehnel
 */
public class ProtectedRequestHandler extends RequestHandler {  
    private static final Logger LOG = LogManager.getLogger(ProtectedRequestHandler.class);
	
	public static final String LIST_PREFIX = "list-";
	
	private static final int LIST_PREFIX_LENGTH = LIST_PREFIX.length();

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

		LOG.warn("user " + uid + " tried to access file outside of the document root: " + fileName);

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
	
	public boolean copyFile(String sourceFilePath, String destFilePath) {
		if ((sourceFilePath == null) || (destFilePath == null)) {
			throw new IllegalArgumentException("source or target file for copy opertaion is null");
		}
		
		if (sourceFilePath.equals(destFilePath)) {
			LOG.warn("copy source equals destination: " + sourceFilePath); 
			return(false);
		}

		File sourceFile = new File(sourceFilePath);
		long lastChangeDate = sourceFile.lastModified();

		boolean copyFailed = false;

		BufferedInputStream fin = null;
		BufferedOutputStream fout = null;

		try {
			fin = new BufferedInputStream(new FileInputStream(sourceFilePath));
			fout = new BufferedOutputStream(new FileOutputStream(destFilePath));

			byte [] buff = new byte[4096];
			int count;

			while ((count = fin.read(buff)) >= 0) {
				fout.write(buff, 0, count);
			}
		} catch (Exception e) {
			LOG.error("failed to copy file " + sourceFilePath + " to " + destFilePath, e);
			copyFailed = true;
		} finally {
			if (fin != null) {
				try {
					fin.close();
				} catch (Exception ex) {
				}
			}
			if (fout != null) {
				try {
					fout.close();
				} catch (Exception ex) {
				}
			}
		}

		if (!copyFailed) {
			File destFile = new File(destFilePath);
			destFile.setLastModified(lastChangeDate);
		}
		
		return(!copyFailed);
	}
	
	public int zipTree(String currentPath, String relativePath, ZipOutputStream zipOut, int fileCount) {
		int zipFileNum = fileCount;

		File currentDir = new File(currentPath);

		String fileList[] = currentDir.list();
		
		if (fileList == null) {
			LOG.warn("could not get dir entries for " + currentPath);
			return zipFileNum;
		}

		if (fileList.length == 0) {
			try {
	            zipOut.putNextEntry(new ZipEntry(relativePath + File.separator));
	            zipOut.closeEntry();
			} catch (IOException ex) {
				LOG.error("failed to ad zip entry for empty directory " + currentPath);
			}
			return zipFileNum;
		}

		byte buff[] = new byte[4096];

		for (int i = 0; i < fileList.length; i++) {
			File sourceFile = new File(currentPath, fileList[i]);

			if (sourceFile.isDirectory()) {
				zipFileNum = zipTree(currentPath + File.separator + fileList[i],
								     relativePath + fileList[i] + "/",
								     zipOut, zipFileNum);
			} else {
				String fullFileName = currentPath + File.separator + fileList[i];
				String relativeFileName = relativePath + fileList[i];

				try {
					ZipEntry newZipEntry = new ZipEntry(relativeFileName);

					zipOut.putNextEntry(newZipEntry);

					FileInputStream inStream = null;

					try {
						inStream = new FileInputStream(sourceFile);

						int count;

						while ((count = inStream.read(buff)) >= 0) {
							zipOut.write(buff, 0, count);
						}

						zipOut.closeEntry();
						
						long originalSize = sourceFile.length();

						treeFileSize += originalSize;

						zipFileNum++;

						 // long compressedSize=newZipEntry.getCompressedSize();

		                boolean showStatus = false;
		                
		                if (zipFileNum < 100) {
		                    showStatus = true;
		                } else if (zipFileNum < 1000) {
		                    if (zipFileNum % 10 == 0) {
		                        showStatus = true;
		                    }
		                } else if (zipFileNum < 5000) {
		                    if (zipFileNum % 50 == 0) {
		                        showStatus = true;
		                    }
		                } else {
		                    if (zipFileNum % 100 == 0) {
		                        showStatus = true;
		                    }
		                }

		                if (showStatus) {
	                        output.println("<script language=\"javascript\">");
	                        output.println("document.getElementById('currentDir').innerHTML=\"" + insertDoubleBackslash(CommonUtils.shortName(relativeFileName, 50)) + "\";");
                            output.println("document.getElementById('compressCount').innerHTML=\"" + zipFileNum + "\";");
	                        output.println("</script>");
	                        output.flush();
						}
					} catch (Exception zioe) {
						LOG.error("failed to zip file " + fullFileName, zioe);
						output.println("<font color=\"red\">failed to zip file " + fullFileName + "</font><br/>");
						output.flush();
					} finally {
                        if (inStream != null) {
                        	try {
            					inStream.close();
                        	} catch (Exception ex) {
                        	}
                        }
					}
				} catch (IOException ioex) {
                    LOG.error("error during zipping file " + fullFileName, ioex);
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
    
	protected List<String> getSelectedFiles() {
		ArrayList<String> selectedFiles = new ArrayList<String>();

        Enumeration allKeys = req.getParameterNames();
		
		while (allKeys.hasMoreElements()) {
			String paramKey =(String) allKeys.nextElement();

            if (paramKey.startsWith(LIST_PREFIX)) {
				selectedFiles.add(paramKey.substring(LIST_PREFIX_LENGTH)); 
            }
		}
		return selectedFiles;
	}
}
