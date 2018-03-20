package de.webfilesys.gui.user;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.CopyStatus;
import de.webfilesys.LanguageManager;
import de.webfilesys.MetaInfManager;
import de.webfilesys.ViewHandlerConfig;
import de.webfilesys.WebFileSys;
import de.webfilesys.gui.ProtectedRequestHandler;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.FileEncodingMap;
import de.webfilesys.util.HTTPUtils;
import de.webfilesys.viewhandler.ViewHandler;
 
/**
 * Handles all non-anonymous and non-admin requests.
 * @author Frank Hoehnel
 */
public class UserRequestHandler extends ProtectedRequestHandler
{ 
	protected String language = null;
	
	protected boolean readonly = true;
	
	private int fileCopyCounter = 0;
	
    public UserRequestHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid)
    {
        super(req, resp, session, output, uid);
        
        if (uid != null)
        {
            language = userMgr.getLanguage(uid);
        }
        else
        {
        	language = LanguageManager.getInstance().getDefaultLanguage();
        }

        if (uid != null)
        {
            String sessionReadonly = (String) session.getAttribute("readonly");
    		
    		readonly = (sessionReadonly != null) || userMgr.isReadonly(uid);
        }
    }

    public void handleRequest()
    {
        process();
    }

    public String getResource(String key, String defaultValue)
    {
        return (
            LanguageManager.getInstance().getResource(
                language,
                key,
                defaultValue));
    }

    protected boolean isWebspaceUser()
    {
        String role = userMgr.getRole(uid);

        return ((role != null) && (role.equals("webspace") || role.equals("album")));
    }

    /**
     * Calculate the path that is displayed as headline for file window, slideshow window, ...
     * For webspace users the root of the path to the document root is hidden.
     * For anonymous access, only the last part of the path is displayed.
     */
    public String getHeadlinePath(String fullPath)
    {
        String docRoot = null;

        if (uid == null)
        {
            docRoot = fullPath;
        }
        else
        {
            if (!isWebspaceUser())
            {
                return (fullPath);
            }

            docRoot = userMgr.getDocumentRoot(uid);

            if (docRoot == null)
            {
                docRoot = fullPath;
            }
        }

        String headlinePath = fullPath;

        if ((File.separatorChar == '/')
            && (docRoot.length() != 1)
            || (File.separatorChar == '\\')
            && (docRoot.charAt(0) != '*'))
        {
            int idx = docRoot.length() - 1;

            if (fullPath.length() > idx)
            {
                while ((idx > 0)
                    && (fullPath.charAt(idx) != File.separatorChar)
                    && (fullPath.charAt(idx) != '/'))
                {
                    idx--;
                }

                headlinePath = fullPath.substring(idx);
            }
        }

        return (headlinePath);
    }

    public boolean checkWriteAccess()
    {
    	boolean sessionReadonly = false;
    	
    	Boolean sessRO = (Boolean)session.getAttribute("readonly");
    	
    	if (sessRO != null)
    	{
    		sessionReadonly = sessRO.booleanValue();
    	}
    	
        boolean readonly =
            sessionReadonly || userMgr.isReadonly(uid);

        if (!readonly)
        {
            return (true);
        }

        Logger.getLogger(getClass()).warn(
            "read-only user " + uid + " tried write access");

        output.print(HTTPUtils.createHTMLHeader());

        output.println("<HTML>");
        output.println("<HEAD>");
        output.println("<TITLE> Unauthorized access </TITLE>");
        output.println("<script language=\"javascript\">");
        output.println(
            "alert('Write access is required to perform this operation!');");
        output.println("history.back();");
        output.println("</script>");

        output.println("</html>");
        output.flush();

        return (false);
    }

    public static boolean dirIsLink(File f)
    {
        if (File.separatorChar != '/')
        {
            // there is no way to detect NTFS symbolic links / junctions with Java functions
            // see http://stackoverflow.com/questions/3249117/cross-platform-way-to-detect-a-symbolic-link-junction-point
            // possible workaround: if the directory is not empty, files in the linked directory
            // should have a canonical path that does not start with the path of the parent dir
            
            return (false);
        }

        try
        {
            return (!(f.getCanonicalPath().equals(f.getAbsolutePath())));
        }
        catch (IOException ioex)
        {
            Logger.getLogger(UserRequestHandler.class).warn(ioex);
            return (false);
        }
    }
    
    public boolean copyFolderTreeWithStatus(String sourcePath, String destPath, boolean ignoreExistingDir, CopyStatus copyStatus, DecimalFormat numFormat) {
        boolean copyError = false;
        
        File sourceFolderFile = new File(sourcePath);
        
        File fileList[] = sourceFolderFile.listFiles();

        if (fileList != null) {
        	
        	String formattedTreeFileNum = numFormat.format(copyStatus.getTreeFileNum());
        	String formattedTreeFileSize = numFormat.format(copyStatus.getTreeFileSize());
        	
            for (int i = 0; i < fileList.length; i++) {
            	
                File sourceFile = fileList[i];

            	if (sourceFile.canRead()) {
                    String destFileName = destPath + File.separator + sourceFile.getName();

                    if (sourceFile.isFile()) {
                        if ((copyStatus.getFilesCopied() <= 100) ||
                            ((copyStatus.getFilesCopied() < 300) && (copyStatus.getFilesCopied() % 5 == 0)) ||
                            ((copyStatus.getFilesCopied() < 1000) && (copyStatus.getFilesCopied() % 10 == 0)) ||
                            (copyStatus.getFilesCopied() % 50 == 0)) {
                            output.println("<script language=\"javascript\">");
                            output.println("document.getElementById('currentFile').innerHTML='" + insertDoubleBackslash(CommonUtils.shortName(getHeadlinePath(sourceFile.getAbsolutePath()), 40)) + "';");
                            output.println("</script>");
                            output.flush();
                        }                    

                        if (copyFile(sourceFile.getAbsolutePath(), destFileName)) {
                            copyStatus.setFilesCopied(copyStatus.getFilesCopied() + 1);
                            copyStatus.setBytesCopied(copyStatus.getBytesCopied() + sourceFile.length());
                                    
                            if ((copyStatus.getFilesCopied() <= 100) ||
                                ((copyStatus.getFilesCopied() < 300) && (copyStatus.getFilesCopied() % 5 == 0)) ||
                                ((copyStatus.getFilesCopied() < 1000) && (copyStatus.getFilesCopied() % 10 == 0)) ||
                                (copyStatus.getFilesCopied() % 50 == 0)) {
                                    	
                            	long progress = copyStatus.getBytesCopied() * 300l / copyStatus.getTreeFileSize();
                            	
                                output.println("<script language=\"javascript\">");
                                output.println("document.getElementById('fileCount').innerHTML='" + numFormat.format(copyStatus.getFilesCopied()) + " / " + formattedTreeFileNum +  "';");
                                output.println("document.getElementById('bytesCopied').innerHTML='" + numFormat.format(copyStatus.getBytesCopied()) + " / " + formattedTreeFileSize +  "';");
                                output.println("document.getElementById('copyProgressBar').style.width='" + progress + "px';");
                                output.println("</script>");
                            }
                            
                            if (sourceFile.getName().equals(MetaInfManager.METAINF_FILE)) {
                            	MetaInfManager.getInstance().releaseMetaInf(destPath);
                            }
                        } else {
                            copyError = true;
                        }
                    } else if (sourceFile.isDirectory()) {
                        File newDir = new File(destFileName);

                        if ((!newDir.mkdir()) && (!ignoreExistingDir)) {
                            javascriptAlert(getResource("alert.mkdirfail", "cannot create directory")
                                            + "\\n"
                                            + insertDoubleBackslash(destFileName));
                            copyError = true;
                        } else {
                            if (!copyFolderTreeWithStatus(sourceFile.getAbsolutePath(), destFileName, ignoreExistingDir, copyStatus, numFormat)) {
                                copyError = true;
                            }
                        }
                    }
            	}
            }
            
            if (copyStatus.getFilesCopied() > 100) {
            	long progress = copyStatus.getBytesCopied() * 300l / copyStatus.getTreeFileSize();

                output.println("<script language=\"javascript\">");
                output.println("document.getElementById('fileCount').innerHTML='" + numFormat.format(copyStatus.getFilesCopied()) + " / " + formattedTreeFileNum +  "';");
                output.println("document.getElementById('bytesCopied').innerHTML='" + numFormat.format(copyStatus.getBytesCopied()) + " / " + formattedTreeFileSize +  "';");
                output.println("document.getElementById('copyProgressBar').style.width='" + progress + "px';");
                output.println("</script>");
            }
        }

        if (copyError) {
        	copyStatus.setError(true);
        }
        
        return(!copyError);
    }
    
    protected boolean delDirTree(String path)
    { 
        boolean deleteError=false;

        File dirToBeDeleted = new File(path);
        String fileList[] = dirToBeDeleted.list();

        if (fileList != null)
        {
            for (int i = 0; i < fileList.length; i++)
            {
                File tempFile=new File(path + File.separator + fileList[i]);
                if (tempFile.isDirectory())
                {
                    if (!delDirTree(path + File.separator + fileList[i]))
                        deleteError=true;
                }
                else
                {
                    String absolutePath = tempFile.getAbsolutePath();
                    
                    if (!tempFile.delete())
                    {
                        deleteError=true;
                        Logger.getLogger(getClass()).warn("cannot delete " + tempFile);
                    }
                    else
                    {
                        if (WebFileSys.getInstance().isReverseFileLinkingEnabled())
                        {
                            MetaInfManager.getInstance().updateLinksAfterMove(absolutePath, null, uid);
                        }
                        MetaInfManager.getInstance().removeMetaInf(absolutePath);
                    }
                }
            }
        }

        if (!dirToBeDeleted.delete())
        {
            deleteError=true;
        } 
        else 
        {
            MetaInfManager.getInstance().releaseMetaInf(path);
        }

        return(!(deleteError));
    }
    
    /**
     * Calculate the absolute path from a given relative path and the document root.
     * 
     * @param relativePath path relative to the document root
     * @return absolute path
     */
    protected String getAbsolutePath(String relativePath) {
        
        String docRoot = userMgr.getDocumentRoot(uid);
        
        String docRootOS = docRoot;
        
        if (File.separatorChar == '\\')
        {
            docRootOS = docRoot.replace('/', File.separatorChar);
        }
        
        if (relativePath == null) 
        {
            if (File.separatorChar == '\\')
            {
                if (docRootOS.charAt(0) == '*') 
                {
                    return "C:\\";
                }
            }
            
            return docRootOS;
        }            
        else if (File.separatorChar == '\\') 
        {
            relativePath = relativePath.replace('/', File.separatorChar);
        }
        
        if ((File.separatorChar == '\\') && (docRoot.charAt(0) == '*')) 
        {
            if ((relativePath.charAt(0) == '\\') && (relativePath.length() > 1))
            {
                return relativePath.substring(1);
            }
            else
            {
                return relativePath;
            }
        }
        
        if (docRootOS.endsWith(File.separator) || (relativePath.charAt(0) == File.separatorChar))
        {
            return(docRootOS + relativePath);
        }

        return(docRootOS + File.separator + relativePath);
    }

    protected boolean isMobile() 
    {
        return(session.getAttribute("mobile") != null);
    }
    
    protected boolean delegateToViewHandler(ViewHandlerConfig viewHandlerConfig, String filePath, InputStream zipIn)
    {
        String viewHandlerClassName = viewHandlerConfig.getHandlerClass();
        
        try
        {
            ViewHandler viewHandler =
                (ViewHandler) (Class
                    .forName(viewHandlerClassName)
                    .newInstance());

            Logger.getLogger(getClass()).debug("ViewHandler instantiated: " + viewHandler.getClass().getName());
            
            if (zipIn == null) 
            {
                viewHandler.process(filePath, viewHandlerConfig, req, resp);

                if (WebFileSys.getInstance().isDownloadStatistics())
                {
                    MetaInfManager.getInstance().incrementDownloads(filePath);
                }
                return true;
            } 

            if (!viewHandler.supportsZipContent()) 
            {
                return false;
            }
            
            viewHandler.processZipContent(filePath, zipIn, viewHandlerConfig, req, resp);
            
            return(true);
        }
        catch (ClassNotFoundException cnfex)
        {
            Logger.getLogger(getClass()).error("Viewhandler class "
                                         + viewHandlerClassName
                                         + " cannot be found: " + cnfex);
        }
        catch (InstantiationException instEx)
        {
            Logger.getLogger(getClass()).error("Viewhandler class "
                    + viewHandlerClassName
                    + " cannot be instantiated: " + instEx);
        }
        catch (IllegalAccessException iaEx)
        {
            Logger.getLogger(getClass()).error("Viewhandler class "
                    + viewHandlerClassName
                    + " cannot be instantiated: " + iaEx);
        }
        catch (ClassCastException cex)
        {
            Logger.getLogger(getClass()).error("Viewhandler class "
                    + viewHandlerClassName
                    + " does not implement the ViewHandler interface: "
                    + cex);
        }
        
        return(false);
    }
    
    /**
     * Guess the character encoing of the file.
     * @param filePath path and filename
     * @return encoding or null, if unknown
     */
    protected String guessFileEncoding(String filePath) {
        try 
        {
            FileInputStream fin = new FileInputStream(filePath);
            
            int byte1 = fin.read();
            if (byte1 != (-1)) 
            {
                int byte2 = fin.read();
                if (byte2 != (-1)) 
                {
                    int byte3 = fin.read();
                    if ((byte1 == 0xef) && (byte2 == 0xbb) && (byte3 == 0xbf)) 
                    {
                        // BOM found - UTF-8
                        return "UTF-8-BOM";
                    }
                }
            }
            fin.close();
            
        } catch (IOException ioex) {
            Logger.getLogger(getClass()).warn("cannot determine file encoding for " + filePath);
        }

        return FileEncodingMap.getInstance().getFileEncoding(filePath);
    }
    
    /**
     * Checks if the file is a text file.
     * The first check is done based on the filename extension.
     * Then we look into the file and search for linefeed characters (0x0a, 0x0d).
     *  
     * @param filePath the filesystem path of the file
     * @param maxBytesWithoutLineBreak maximum line length allowed to be a text file
     * @param bytesToCheck how many bytes of the file to read
     * @return true if probably text file, false if probably binary file
     */
    protected boolean isTextFile(String filePath, int maxBytesWithoutLineBreak, int bytesToCheck) {
        String fileExt = CommonUtils.getFileExtension(filePath);
        
        if (fileExt.equals(".zip") || 
           	fileExt.equals(".exe") ||
           	fileExt.equals(".jar") ||
           	fileExt.equals(".war") ||
           	fileExt.equals(".ear") ||
           	fileExt.equals(".gif") ||
           	fileExt.equals(".jpeg") ||
           	fileExt.equals(".jpg") ||
           	fileExt.equals(".png") ||
           	fileExt.equals(".bmp") ||
           	fileExt.equals(".tif") ||
           	fileExt.equals(".mpg") ||
           	fileExt.equals(".mpeg") ||
           	fileExt.equals(".mov") ||
           	fileExt.equals(".avi") ||
           	fileExt.equals(".mp4") ||
           	fileExt.equals(".wmf") ||
           	fileExt.equals(".mp3")) 
        {
        	// TODO: add more extensions here
            return false;	
        } 
        
        boolean seemsToBeBinary = false;
        
        int byteCounter = 0;
        
    	BufferedInputStream fin = null;
    	
    	try 
    	{
        	fin = new BufferedInputStream(new FileInputStream(filePath));

        	int bytesWithoutLineBreak = 0;

    		int c;
    		while ((!seemsToBeBinary) && (byteCounter < bytesToCheck) && ((c = fin.read()) != (-1)))
    		{
    			if ((c == 0x0d) || (c == 0x0a))
    			{
    				bytesWithoutLineBreak = 0;
    			}
    			else 
    			{
    				bytesWithoutLineBreak++;
    				
    				if (bytesWithoutLineBreak > maxBytesWithoutLineBreak)
    				{
    					seemsToBeBinary = true;
    				}
    			}
    			
    			byteCounter++;
    		}
    	}
    	catch (IOException ioex)
    	{
    		Logger.getLogger(getClass()).error("failed to check if text file", ioex);
    	}
    	finally
    	{
    		if (fin != null) 
    		{
    			try
    			{
    				fin.close();
    			}
    			catch (IOException ex)
    			{
    			}
    		}
    	}

    	return (!seemsToBeBinary);
    }
    
}
