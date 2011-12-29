package de.webfilesys.gui.user;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.util.UTF8URLEncoder;

/**
 * @author Frank Hoehnel
 */
public class EditorSaveRequestHandler extends UserRequestHandler
{
	public EditorSaveRequestHandler(
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

		String fileName=getParameter("filename");

		if (!checkAccess(fileName))
		{
			return;
		}

		String alertText=null;

        File destFile = new File(fileName);
        
        if (!destFile.canWrite())
        {
        	alertText = getResource("alert.saveEditReadOnly","The file is write-protected") + ":\\n" + insertDoubleBackslash(fileName);
        }
        else
        {
			String actPath=getParameter("actPath");

			String text=getParameter("text");

			String tmpFileName = fileName + "_tmp$edit";

			String fileEncoding = (String) req.getSession(true).getAttribute(RemoteEditorRequestHandler.SESSION_KEY_FILE_ENCODING);

			req.getSession(true).removeAttribute(RemoteEditorRequestHandler.SESSION_KEY_FILE_ENCODING);
			
			PrintWriter fout = null;

			try
			{
                FileOutputStream fos = new FileOutputStream(tmpFileName);

                if (fileEncoding == null) 
			    {
			        // use OS default encoding
	                fout = new PrintWriter(fos);
			    }
			    else 
			    {
	                Logger.getLogger(getClass()).debug("saving editor file " + fileName + " with character encoding " + fileEncoding);
			        
			        if (fileEncoding.equals("UTF-8-BOM")) {
			            // write UTF-8 BOM
                        fos.write(0xef);
                        fos.write(0xbb);
                        fos.write(0xbf);
                        fileEncoding = "UTF-8";
			        }
			        
			        fout = new PrintWriter(new OutputStreamWriter(fos, fileEncoding));
			    }

                if (File.separatorChar == '/')
                {
                    boolean endsWithLineFeed = text.charAt(text.length() - 1) == '\n';
                    
                    BufferedReader textReader = new BufferedReader(new StringReader(text));
                    
                    String line = null;
                    boolean firstLine = true;
                    
                    while ((line = textReader.readLine()) != null) 
                    {
                        if (firstLine) 
                        {
                            firstLine = false;
                        }
                        else
                        {
                            fout.print('\n');
                        }
                        
                        fout.print(line);    
                    }
                    
                    if (endsWithLineFeed)
                    {
                        fout.print('\n');
                    }
                }
                else
                {
                    fout.print(text);
                }

				fout.flush();

				fout.close();

				if (!copy_file(tmpFileName,fileName,false))
				{
					String logMsg = "cannot copy temporary file to edited file " + fileName;
					Logger.getLogger(getClass()).error(logMsg);
					alertText = getResource("alert.saveEditError","The changed file could not be written to")
					          + "\\n" + insertDoubleBackslash(fileName);
				}
				else
				{
					File tmpFile = new File(tmpFileName);
				
					if (!tmpFile.delete())
					{
						Logger.getLogger(getClass()).warn("cannot delete temporary file " + tmpFile);
					}
				}
			}
			catch (IOException ioex)
			{
				String logMsg="cannot save changed content of edited file " + fileName + ": " + ioex;
				Logger.getLogger(getClass()).error(logMsg);
				alertText = getResource("alert.saveEditError","The changed file could not be written to") 
				          + "\\n" + insertDoubleBackslash(fileName);
                
				if (fout != null) 
                {
                    fout.close();
                }
			}
        }

        String mobile = (String) session.getAttribute("mobile");

        output.println("<HTML>");
        output.println("<HEAD>");

        if (alertText!=null)
        {
            javascriptAlert(alertText);
        }

        output.println("<script language=\"javascript\">");
        if (mobile != null)
        {
            output.println("window.location.href='/webfilesys/servlet?command=mobile&cmd=folderFileList';");
        }
        else
        {
            output.println("window.opener.location.href='/webfilesys/servlet?command=listFiles&actpath=" + UTF8URLEncoder.encode(getCwd()) + "&mask=*&keepListStatus=true';");
            output.println("self.close();");
        }
        output.println("</script>");

        output.println("</head></html>");
        output.flush();
	}
}
