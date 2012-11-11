package de.webfilesys.gui.user;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DecimalFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import sun.io.MalformedInputException;
import de.webfilesys.WebFileSys;
import de.webfilesys.util.CommonUtils;

/**
 * @author Frank Hoehnel
 */
public class GrepRequestHandler extends UserRequestHandler
{
    private static final String ENCODING_ERROR = "#### failed to read line due to charcater encoding problems";
    
	private static final int BYTES_TO_CHECK = 2 * 1024 * 1024;
    
	public GrepRequestHandler(
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
        String fileName = req.getParameter("fileName");

        String filePath = getCwd();

        if (!filePath.endsWith(File.separator))
        {
            filePath = filePath + File.separatorChar + fileName;
        }
        else 
        {
            filePath = filePath + fileName;
        }
        
        if (!checkAccess(filePath))
        {
            return;
        }

        String filter = req.getParameter("filter");
        
        boolean error = false;
        
        File fileToSend = new File(filePath);
        
        if (!fileToSend.exists())
        {
        	Logger.getLogger(getClass()).warn("requested file does not exist: " + filePath);
        	
        	error = true;
        }
        else if ((!fileToSend.isFile()) || (!fileToSend.canRead()))
        {
        	Logger.getLogger(getClass()).warn("requested file is not a readable file: " + filePath);
        	
        	error = true;
        }

        if (error)
        {
            resp.setStatus(404);
            output.println("File not found or not readable: " + filePath);
            output.flush();
            return;
        }
		
		// prevent out of memory by readLine() a very large piece of data without linebreak
		// this is not 100 % save as we check only the beginning of very large files
		// alternative would be to write our own readLine() method with limited line length
        
        if (!isTextFile(filePath, WebFileSys.getInstance().getTextFileMaxLineLength(), BYTES_TO_CHECK))
        {
            resp.setStatus(404);
            output.println("This file seems not to be a text file: " + getHeadlinePath(filePath));
            output.flush();
            return;
        }
        
        output.println("<HTML>");
        output.println("<HEAD>");

        output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/css/" + userMgr.getCSS(uid) + ".css\">");

        output.println("<title>WebFileSys: grep " + fileName + "</title>");
        
        output.println("</head>");
        output.println("<body>");

        output.println("<pre>");
        
        boolean anyMatchFound = false;
        
        String fileEncoding = guessFileEncoding(filePath);
		
        BufferedReader fin = null;
        FileInputStream fis = null;
        
        try
        {
            if (fileEncoding == null) 
            {
                // unknown - use OS default encoding
                fin = new BufferedReader(new FileReader(filePath));
            }
            else 
            {
                fis = new FileInputStream(filePath);
                
                fin = new BufferedReader(new InputStreamReader(fis, fileEncoding));
            }
            
            DecimalFormat numberFormat = new DecimalFormat("000000");
            
            String line = null;
            
            boolean eof = false;
            
            int excCounter = 0;
            
            int lineCounter = 0;
            
            while ((!eof) && (excCounter < 5))
            {
                try 
                {
                    line = fin.readLine();
                    
                    if (line == null) 
                    {
                        eof = true;
                    } 
                    else 
                    {
                    	lineCounter++;
                    	
                        excCounter = 0;
                        
                        if (line.contains(filter)) 
                        {
                        	output.print("<span style=\"color:#a0a0a0\">");
                        	output.print(numberFormat.format(lineCounter));
                        	output.print(": </span>");
                            output.println(formatLine(line, filter));
                            output.flush();
                            anyMatchFound = true;
                        }
                    }
                } 
                catch (MalformedInputException miEx) {
                    Logger.getLogger(getClass()).warn("error during reading file for grep", miEx);
                    excCounter++;
                    output.println(ENCODING_ERROR);
                }
            }
        }
        catch (IOException ioex)
        {
            Logger.getLogger(getClass()).error("failed to read file for grep", ioex);
        }
        finally
        {
            try
            {
                if (fin != null)
                {
                    fin.close();
                }
                if (fis != null)
                {
                    fis.close();
                }
            }
            catch (Exception ex)
            {
            }
        }
        
        output.println("</pre>");

        if (!anyMatchFound) 
        {
        	output.print("<span style=\"color:red;\">");
        	output.print(getResource("grepResultEmpty", "no lines found containing") + " \"" + filter + "\"");
        	output.print("</span>");
        }
        
        output.println("</body>");
        output.println("</html>");
        output.flush();
	}
	
	private String formatLine(String line, String filter) 
	{
		StringBuffer formattedLine = new StringBuffer();
		
		int filterLength = filter.length();
		
		String escapedFilter = CommonUtils.escapeHTML(filter);
		
		String restOfLine = line;
		
		int foundIdx = 0;
		
		while (foundIdx >= 0) 
		{
			foundIdx = restOfLine.indexOf(filter);
			if (foundIdx >= 0) 
			{
				if (foundIdx > 0) 
				{
					formattedLine.append(CommonUtils.escapeHTML(restOfLine.substring(0, foundIdx)));
				}
				formattedLine.append("<span style=\"color:#0000c0\">");
				formattedLine.append(escapedFilter);
				formattedLine.append("</span>");

				if (restOfLine.length() > foundIdx + filterLength) 
				{
					restOfLine = restOfLine.substring(foundIdx + filterLength);
				}
				else 
				{
					foundIdx = (-1);
				}
			}
			else 
			{
				if (restOfLine.length() > 0) 
				{
					formattedLine.append(CommonUtils.escapeHTML(restOfLine));
				}
			}
		}
		
		return formattedLine.toString();
	}
	
}
