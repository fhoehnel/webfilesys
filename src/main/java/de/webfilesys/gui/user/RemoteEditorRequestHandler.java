package de.webfilesys.gui.user;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

/**
 * @author Frank Hoehnel
 */
public class RemoteEditorRequestHandler extends UserRequestHandler
{
	public static final int MAX_FILE_SIZE = 512000;
	
	public static String SESSION_KEY_FILE_ENCODING = "fileEncoding";
	
	public RemoteEditorRequestHandler(
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

		String actPath = getCwd();

		String editFile = getParameter("filePath");
        
		if (editFile == null)
		{
			String fileName=getParameter("filename");

			if (actPath.endsWith(File.separator))
			{
				editFile=actPath + fileName;
			}
			else
			{
				editFile=actPath + File.separator + fileName;
			}
		}

		output.println("<HTML>");
		output.println("<HEAD>");
		output.println("<TITLE>WebFileSys Remote Editor</TITLE>");

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/css/" + userMgr.getCSS(uid) + ".css\">");

		File tmpFile = new File(editFile);

		if (tmpFile.length() > MAX_FILE_SIZE)
		{
            output.println("<script langauge=\"javascript\">");
			output.println("alert('" + getResource("alert.editFileSize", "The file is too large for remote editing.") + "');");
            output.println("self.close();");
            output.println("</script>");
    		output.println("</head>");
		}
		else
		{
			output.println("</head>");
			output.println("<body>");
			
            int textAreaHeight = 500;
			
	        String mobile = (String) session.getAttribute("mobile");
	        
	        if (mobile != null) 
	        {
	            textAreaHeight = 140;
	        }			
	        else
	        {
	            String screenHeight = getParameter("screenHeight");
	            
	            try
	            {
	                textAreaHeight = Integer.parseInt(screenHeight) - 120;
	            }
	            catch (NumberFormatException nfex)
	            {
	            }
	        }
			
			output.println("<center>");

			headLine(this.getHeadlinePath(editFile));

			output.println("<form accept-charset=\"utf-8\" method=\"post\" action=\"/webfilesys/servlet\" style=\"margin-top:20px\">");
			output.println("<input type=\"hidden\" name=\"command\" value=\"saveEditor\">");
			output.println("<input type=\"hidden\" name=\"actPath\" value=\"" + actPath + "\">");
			output.println("<input type=\"hidden\" name=\"filename\" value=\"" + editFile + "\">");
			output.print("<textarea name=\"text\" rows=\"18\" cols=\"60\" style=\"width:100%;height:" + textAreaHeight + "px;font-family:monospace;\" wrap=\"virtual\">");

            req.getSession(true).removeAttribute(SESSION_KEY_FILE_ENCODING);

			String fileEncoding = guessFileEncoding(editFile);
			
			if (fileEncoding != null) {
			    Logger.getLogger(getClass()).debug("reading editor file " + editFile + " with character encoding " + fileEncoding);
			}
			
			boolean readError = false;
			
			BufferedReader fin = null;

			try
			{
				if (fileEncoding == null) 
				{
				    // unknown - use OS default encoding
	                fin = new BufferedReader(new FileReader(editFile));
				}
				else 
				{
				    req.getSession(true).setAttribute(SESSION_KEY_FILE_ENCODING, fileEncoding);
				    
		            FileInputStream fis = new FileInputStream(editFile);
		            
		            if (fileEncoding.equals("UTF-8-BOM")) {
                        // skip over BOM
		                fis.read();
                        fis.read();
                        fis.read();
                        fileEncoding = "UTF-8";
		            }
		            
                    fin = new BufferedReader(new InputStreamReader(fis, fileEncoding));
				}

				String line = null;

				while ((line = fin.readLine()) != null)
				{
					output.println();

					output.print(encodeSpecialChars(line));
				}

				output.println();

				fin.close();
			}
			catch (IOException ioex)
			{
				Logger.getLogger(getClass()).error("cannot read file for remote editing", ioex);
				readError = true;
			}

			output.println("</textarea>");

			output.println("<div style=\"padding-top:15px;\">");
			
			if (!readError) 
			{
	            output.println("<input type=\"submit\" value=\"" + getResource("button.save","Save") + "\">");
	            output.println("&nbsp;&nbsp;&nbsp;");
			}
			
			if (mobile != null)
			{
                output.println("<input type=\"button\" value=\"" + getResource("button.cancel","Cancel") + "\" onclick=\"window.location.href='/webfilesys/servlet?command=mobile&cmd=folderFileList';\">");
			} 
			else
			{
	            output.println("<input type=\"button\" value=\"" + getResource("button.cancel","Cancel") + "\" onclick=\"javascript:self.close()\">");
			}
			output.println("</div>");
			output.println("</form>");

			output.println("</center>");
			
			if (readError) 
			{
	            output.println("<script langauge=\"javascript\">");
	            output.println("alert('The file to edit could not be read (maybe character encoding problem)!');");
	            output.println("</script>");
			}
			
			output.println("</body>");
		}

		output.println("</html>");
		output.flush();
	}
	
	protected String encodeSpecialChars(String line)
	{
        /*
		if (line.indexOf('&') < 0)
		{
			return(line);
		}
		*/

		StringBuffer buff = new StringBuffer();

		for (int i = 0; i < line.length(); i++)
		{
			char ch = line.charAt(i);

			if (ch=='&')
			{
				buff.append("&amp;");
			}
			else if (ch == '<')
			{
				buff.append("&lt;");
			}
			else if (ch == '>')
			{
				buff.append("&gt;");
			}
			else if (ch == '"')
			{
				buff.append("&quot;");
			}
			else
			{
				buff.append(ch);
			}
		}

		return(buff.toString());
	}

}
