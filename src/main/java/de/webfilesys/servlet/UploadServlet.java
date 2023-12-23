package de.webfilesys.servlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


import de.webfilesys.Constants;
import de.webfilesys.LanguageManager;
import de.webfilesys.MetaInfManager;
import de.webfilesys.WebFileSys;
import de.webfilesys.graphics.AutoThumbnailCreator;
import de.webfilesys.gui.user.ZipFileRequestHandler;
import de.webfilesys.gui.xsl.XslFileListHandler;
import de.webfilesys.gui.xsl.XslLogonHandler;
import de.webfilesys.gui.xsl.XslThumbnailHandler;
import de.webfilesys.gui.xsl.mobile.MobileFolderFileListHandler;
import de.webfilesys.user.UserManager;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.UTF8URLDecoder;

public class UploadServlet extends WebFileSysServlet
{
	private static final long serialVersionUID = 1L;
	
	public void doPost ( HttpServletRequest req, HttpServletResponse resp )
    throws ServletException, java.io.IOException
    {
        // prevent caching
		resp.setDateHeader("expires", 0l); 
    	
        String userid = null;
    	
		HttpSession session = req.getSession(false);
    	
		if (session != null)
		{
			userid = (String) session.getAttribute("userid");
			
			if (userid == null)
			{
				(new XslLogonHandler(req, resp, session, resp.getWriter(), false)).handleRequest(); 
				
				return;
			}
	    }
		else
		{
		    session = req.getSession(true);

		    (new XslLogonHandler(req, resp, session, resp.getWriter(), false)).handleRequest(); 
		    
		    return;
		}    	
    	
        if (!checkWriteAccess(userid, session))
        {
            throw new ServletException("write access forbidden");
        }
		
		if (req.getRequestURI().indexOf("singleBinary") > 0) {
		    handleSingleBinaryUpload(req, resp);
		    return;
		}

		resp.setContentType("text/html");
		resp.setCharacterEncoding("UTF-8");
		
		long contentLength = req.getContentLength();
		
		// System.out.println("content length: " + contentLength);
		
		InputStream input = req.getInputStream();
		
        session.removeAttribute(Constants.UPLOAD_LIMIT_EXCEEDED);
        session.removeAttribute(Constants.UPLOAD_CANCELED);

        UserManager userMgr = WebFileSys.getInstance().getUserMgr();
        
        String language = userMgr.getLanguage(userid);
        
        String actPath = null;

        int bytesUploaded = 0;

        Long uploadCounter = new Long(bytesUploaded);

        session.setAttribute(Constants.UPLOAD_COUNTER, uploadCounter);
        session.setAttribute(Constants.UPLOAD_SIZE, new Long(0));

        Boolean uploadSuccess = Boolean.valueOf(false);

        session.setAttribute(Constants.UPLOAD_SUCCESS, uploadSuccess);

        String full_path = null;
        String fn_only = null;
        String text_line = null;
        boolean stop = false;
        int compare_length = 0;
        boolean fn_found = false;
        String delimiter_str = null;

        String exceptionText = null;

        String xferMode = null;

        long prefixLength = 0;

		boolean uploadLimitExceeded = false;

		boolean unzipAfterUpload = false;
		
		String destFileName = null;
		
		String description = null;
		
        String temp;

        for (int i = 0; i < 3; i++)
        {
            temp = readLineAsUTF8(input);
            prefixLength += temp.length();

			// System.out.println("read line: " + temp);
        }

        temp = readLineAsUTF8(input);
        prefixLength += temp.length();

		// System.out.println("read line: " + temp);

        actPath = temp;

        if (!accessAllowed(actPath, userid))
        {
            throw new ServletException("access forbidden");
        }

		for (int i = 0; i < 3; i++)
		{
			temp = readLineAsUTF8(input);
			prefixLength += temp.length();

			// System.out.println("read line: " + temp);
		}

		temp = readLineAsUTF8(input);
		prefixLength += temp.length();

		// System.out.println("read line: " + temp);

		unzipAfterUpload = (temp.equalsIgnoreCase("true"));

		for (int i = 0; i < 3; i++)
		{
			temp = readLineAsUTF8(input);
			prefixLength += temp.length();

			// System.out.println("read line: " + temp);
		}

		temp = readLineAsUTF8(input);
		prefixLength += temp.length();

		// System.out.println("read line: " + temp);

        destFileName = temp.trim();

		// System.out.println("destFileName: " + destFileName);
		
		for (int i = 0; i < 3; i++)
		{
			temp = readLineAsUTF8(input);
			prefixLength += temp.length();

			// System.out.println("read line: " + temp);
		}

		temp = readLineAsUTF8(input);
		prefixLength += temp.length();

		// System.out.println("read line: " + temp);

		description = temp.trim();

        stop = false;
        while (!stop)
        {
            text_line = readLineAsUTF8(input);
            
            // System.out.println("read line in loop: >>" + text_line + "<<");

            prefixLength += text_line.length();

            if (delimiter_str == null)
            {
                delimiter_str = text_line;
                compare_length = delimiter_str.length();
                
            }
            else
            {
                if ((!fn_found) && (text_line.indexOf("filename=") > 0))
                {
                    full_path =
                        text_line.substring(
                            text_line.indexOf("filename=") + 10,
                            text_line.length() - 1);

                    if (full_path.indexOf("\\") > 0)
                        fn_only =
                            full_path.substring(
                                full_path.lastIndexOf("\\") + 1);
                    else
                        fn_only =
                            full_path.substring(
                                full_path.lastIndexOf("/") + 1);

                    fn_found = true;
                    
                    // System.out.println("filename: " + fn_only);
                }
            }

            if (text_line.equals(""))
            {
                stop = true;
            }
        }

        if (contentLength > 0)
        {
            session.setAttribute(
                Constants.UPLOAD_SIZE,
                new Long(contentLength - prefixLength - ((long) compare_length)));
        }

        String out_file_name = null;

		if ((destFileName != null) && (destFileName.length() > 0))
        {
			if (!CommonUtils.isEmpty(fn_only)) 
			{
				String origFileExt = CommonUtils.getFileExtension(fn_only);

				if (!CommonUtils.isEmpty(origFileExt)) 
				{
					if (isPictureFile(origFileExt)) 
					{
						String destFileExt = CommonUtils.getFileExtension(destFileName);
						if (CommonUtils.isEmpty(destFileExt) || (!destFileExt.equalsIgnoreCase(origFileExt))) 
						{
							destFileName = destFileName + origFileExt;
						}
					}
				}
			}
			
		    destFileName = replaceIllegalChars(destFileName);
		    
			if (actPath.endsWith(File.separator))
			{
				out_file_name = actPath + destFileName;
			}
			else
			{
				out_file_name = actPath + File.separator + destFileName;
			}

			File outFile = new File(out_file_name);
        
			try
			{
				outFile.getCanonicalPath();
			}
			catch (IOException ioex)
			{
				LogManager.getLogger(getClass()).debug("cannot write upload to file " + out_file_name + " - using original file name " + fn_only);
			    
			    out_file_name = null;
			}
        }

        if (out_file_name == null)
        {
            fn_only = replaceIllegalChars(fn_only);
            
			if (actPath.endsWith(File.separator))
			{
				out_file_name = actPath + fn_only;
			}
			else
			{
				out_file_name = actPath + File.separator + fn_only;
			}
        }

        long uploadLimit = WebFileSys.getInstance().getUploadLimit();
        
        byte delimiterBytes[] = delimiter_str.getBytes();

        byte equalBuff[] = new byte[compare_length + 1];

        byte inBuffer[] = new byte[4096];

        int inBufferByteNum = 0;

        int inIdx = 0;

        byte outBuffer[] = new byte[4096 + compare_length + 1];

        byte buff2 = 0;
        byte buff1 = 0;

        int outIdx = 0;

        int delimiterIdx = 0;

        FileOutputStream outFile = null;

        try
        {
            outFile = new FileOutputStream(out_file_name);

            stop = false;
            while (!stop)
            {
                if (inIdx >= inBufferByteNum)
                {
                    inBufferByteNum = input.read(inBuffer);
                    
                    session.setAttribute("lastActiveTime", new Date());

                    if (inBufferByteNum < 0)
                    {
                        stop = true;

                        LogManager.getLogger(getClass()).warn("unexpected end of upload stream of file " + out_file_name + " at byte index "
                                + bytesUploaded);
                    }
                    else
                    {
                        Boolean uploadCanceled = (Boolean) session.getAttribute(Constants.UPLOAD_CANCELED);
                
                        if (uploadCanceled != null)
                        {
                            LogManager.getLogger(getClass()).warn("upload of file " + out_file_name + " canceled by user at byte index " + bytesUploaded);
                            stop = true;
                        }
                    }

                    inIdx = 0;
                }

                if (!stop)
                {
                    byte b = inBuffer[inIdx];

                    inIdx++;
                    
                    if (delimiterBytes[delimiterIdx] == b)
                    {
                        equalBuff[delimiterIdx] = b;

                        delimiterIdx++;

                        if (delimiterIdx >= compare_length)
                        {
                            stop = true;
                        }
                    }
                    else
                    {
                        if (delimiterIdx > 0)
                        {
                            for (int i = 0; i < delimiterIdx; i++)
                            {
                                if (bytesUploaded > 1)
                                {
                                    outBuffer[outIdx] = buff2;
                                    outIdx++;
                                }

                                buff2 = buff1;

                                buff1 = equalBuff[i];
                            }

                            bytesUploaded += delimiterIdx;
                        }

                        delimiterIdx = 0;

                        if (bytesUploaded > 1)
                        {
                            outBuffer[outIdx] = buff2;
                            outIdx++;
                        }

                        buff2 = buff1;

                        buff1 = b;

                        bytesUploaded++;
                    }

                    if (outIdx >= 4096)
                    {
                        if (!uploadLimitExceeded)
                        {
                            outFile.write(outBuffer, 0, outIdx);
                        }

                        outIdx = 0;

                        if (bytesUploaded > uploadLimit)
                        {
                            if (!uploadLimitExceeded)
                            {
                                uploadLimitExceeded = true;
                                
                                session.setAttribute(Constants.UPLOAD_LIMIT_EXCEEDED,new Boolean(true));
                                
                                LogManager.getLogger(getClass()).warn("upload limit exceeded for user " + userid + " for file " + out_file_name);

                                exceptionText = LanguageManager.getInstance().getResource(language, "alert.uploadLimitExceeded", "The size of the uploaded file exceeds the limit");
                            }
                        }

                        uploadCounter = new Long(bytesUploaded);
                        session.setAttribute(
                            Constants.UPLOAD_COUNTER,
                            uploadCounter);
                    }
                }
            }

            if (outIdx > 0)
            {
                if (!uploadLimitExceeded)
                {
                    outFile.write(outBuffer, 0, outIdx);
                }
            }

            uploadSuccess = new Boolean(!uploadLimitExceeded);
            session.setAttribute(Constants.UPLOAD_SUCCESS, uploadSuccess);
        }
        catch (IOException e)
        {
            LogManager.getLogger(getClass()).error("error writing upload file to " + out_file_name, e);

            if (e instanceof FileNotFoundException)
            {
                exceptionText = LanguageManager.getInstance().getResource(language, "error.upload", "Error writing uploaded file");
            }
            else
            {
                exceptionText = e.toString();
            }
            stop = true;
        }
        finally
        {
        	if (outFile != null) 
        	{
        		try
        		{
        			outFile.close();
        		}
        		catch (Exception ex) 
        		{
        			LogManager.getLogger(getClass()).error("error closing upload file", ex);
        		}
        	}
        }

        PrintWriter output = resp.getWriter();
        
        if (exceptionText != null)
        {
            output.println("<HTML>");
            output.println("<HEAD>");

            output.println("<script labguage=\"javascript\">");
            output.println("alert('" + exceptionText + "');");
            output.println("</script>");

			output.print(
				"<META HTTP-EQUIV=\"REFRESH\" CONTENT=\"0; URL=/webfilesys/servlet?command=listFiles\">");

            output.print("</HEAD>");
            output.println("</html>");
            output.flush();
        }
        else
        {
        	if ((description != null) && (description.length() > 0))
        	{
        		MetaInfManager.getInstance().setDescription(out_file_name, description);
        	}
        	
			String ext = CommonUtils.getFileExtension(out_file_name);
			
        	if (unzipAfterUpload && ext.equals(".zip"))
        	{
        		req.setAttribute("filePath", out_file_name);
        		req.setAttribute("delZipFile", "true");
        		
    		    (new ZipFileRequestHandler(req, resp, session, output, userid)).handleRequest(); 
        	}
        	else
        	{
        	    String mobile = (String) session.getAttribute("mobile");
        	    
        	    if (mobile != null)
        	    {
                    (new MobileFolderFileListHandler(req, resp, session, output, userid)).handleRequest(); 
        	    }
        	    else
        	    {
                    int viewMode = Constants.VIEW_MODE_LIST;

                    Integer sessionViewMode = (Integer) session.getAttribute("viewMode");
                    
                    if (sessionViewMode != null)
                    {
                        viewMode = sessionViewMode.intValue();
                    }
        	        
                    if (viewMode == Constants.VIEW_MODE_THUMBS) 
                    {
                        (new XslThumbnailHandler(req, resp, session, output, userid, false)).handleRequest(); 
                    }
                    else
                    {
                       (new XslFileListHandler(req, resp, session, output, userid)).handleRequest();
                    }
        	    }

				if (WebFileSys.getInstance().isAutoCreateThumbs())
				{
					if (ext.equals(".jpg") || ext.equals(".jpeg") || (ext.equals("png")))
					{
						AutoThumbnailCreator.getInstance().queuePath(out_file_name, AutoThumbnailCreator.SCOPE_FILE);
					}
				}
        	}
        }

		session.setAttribute(Constants.UPLOAD_COUNTER, new Integer(0));
    }
    
    public void handleSingleBinaryUpload(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, java.io.IOException
    {
        HttpSession session = req.getSession(true);
        
        String currentPath = (String) session.getAttribute(Constants.SESSION_KEY_CWD);
        
        if (currentPath == null) {
            LogManager.getLogger(getClass()).error("current working directory unknown");
            return;
        }
        
        long uploadLimit = WebFileSys.getInstance().getUploadLimit();
        
        String requestPath = req.getRequestURI();
        
        int lastPathDelimiterIdx = requestPath.lastIndexOf('/');
        
        String fileName = UTF8URLDecoder.decode(requestPath.substring(lastPathDelimiterIdx + 1));

        fileName = replaceIllegalChars(fileName);

        File outFile = new File(currentPath, fileName);
        
        if (LogManager.getLogger(getClass()).isDebugEnabled())
        {
            LogManager.getLogger(getClass()).debug("ajax binary file upload: " + outFile.getAbsolutePath());
        }
        
        long uploadSize = 0l;
        
        byte[] buff = new byte[4096];
        
        FileOutputStream uploadOut = null;
        
        try
        {
            uploadOut = new FileOutputStream(outFile);
            
            InputStream input = req.getInputStream();
            
            int bytesRead;
            
            while ((bytesRead = input.read(buff)) > 0) 
            {
                uploadSize += bytesRead;
                if (uploadSize > uploadLimit) {
                    LogManager.getLogger(getClass()).warn("upload limit of " + uploadLimit + " bytes exceeded for file " + outFile.getAbsolutePath());
                    uploadOut.flush();
                    uploadOut.close();
                    outFile.delete();
                    throw new ServletException("upload limit of " + uploadLimit + " bytes exceeded");
                }
                
                uploadOut.write(buff, 0, bytesRead);
            }
            
            uploadOut.flush();
            
        }
        catch (IOException ex) 
        {
            LogManager.getLogger(getClass()).error("error in ajax binary upload", ex);
            throw ex;
        }
        finally 
        {
            if (uploadOut != null)
            {
                try
                {
                    uploadOut.close();
                }
                catch (Exception closeEx)
                {
                }
            }
        }
        
        if (WebFileSys.getInstance().isAutoCreateThumbs())
        {
            String ext = CommonUtils.getFileExtension(fileName);
            
            if (ext.equals(".jpg") || ext.equals(".jpeg") || (ext.equals(".png")))
            {
                AutoThumbnailCreator.getInstance().queuePath(outFile.getAbsolutePath(), AutoThumbnailCreator.SCOPE_FILE);
            }
        }
    }
    
    private String replaceIllegalChars(String fileName) 
    {
        StringBuffer buff = new StringBuffer(fileName.length());
        
        for (int i = 0; i < fileName.length(); i++) 
        {
            char c = fileName.charAt(i);
            
            if ((c == '\'') || (c == '#') || (c == '`') || (c == '%') || (c == '!') || (c == '§') || 
                (c == '&') || (c == '[') || (c == ']') || (c == '\"'))
            {
                c = '_';
            }

            buff.append(c);
        }
        
        return (buff.toString());
    }
    
    private String readLineAsUTF8(InputStream input) 
    throws IOException
    {
        byte[] buff = new byte[1024];
        int idx = 0;
        boolean stop = false;
        
        do 
        {
            int c = input.read();
            if (c < 0)
            {
                // end of stream
                stop = true;
            }
            else
            {
                if (c == '\r') 
                {
                    // found CR, skip over LF
                    input.read();
                    stop = true;
                }
                else if (c == '\n') 
                {
                    // LF without preceding CR - ignore
                    stop = true;
                }
                else
                {
                    buff[idx] = (byte) c;
                    idx++;
                    if (idx == 1024)
                    {
                        stop = true;
                    }
                }
            }
        }
        while (!stop);
        
        return new String(buff, 0, idx, "UTF8");
    }
    
    public boolean checkWriteAccess(String userid, HttpSession session)
    {
        boolean sessionReadonly = false;
        
        Boolean sessRO = (Boolean) session.getAttribute("readonly");
        
        if (sessRO != null)
        {
            sessionReadonly = sessRO.booleanValue();
        }
        
        boolean readonly =
            sessionReadonly || WebFileSys.getInstance().getUserMgr().isReadonly(userid);

        if (!readonly)
        {
            return (true);
        }

        LogManager.getLogger(getClass()).warn("read-only user " + userid + " tried write access");

        return (false);
    }

    protected boolean accessAllowed(String fileName, String userid)
    {
        if (fileName.indexOf("..") >=0)
        {
            return(false);
        }

        if (File.separatorChar=='\\')   // WIN
        {
            String lowerCaseDocRoot = WebFileSys.getInstance().getUserMgr().getLowerCaseDocRoot(userid);

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

        String docRoot = WebFileSys.getInstance().getUserMgr().getDocumentRoot(userid);

        if (docRoot.equals("/"))
        {
            return(true);
        }

        return(fileName.startsWith(docRoot) &&
               ((fileName.length()==docRoot.length()) ||
                (fileName.charAt(docRoot.length())=='/')));
    }
    
    private boolean isPictureFile(String fileExt) {
    	if (CommonUtils.isEmpty(fileExt)) {
    		return false;
    	}
    	return(fileExt.equalsIgnoreCase(".jpg") ||
     		   fileExt.equalsIgnoreCase(".gif") ||
    		   fileExt.equalsIgnoreCase(".png") ||
    		   fileExt.equalsIgnoreCase(".bmp") ||
    		   fileExt.equalsIgnoreCase(".jpeg"));
    }
    
}


