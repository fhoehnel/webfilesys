package de.webfilesys.gui.user;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import sun.io.MalformedInputException;
import de.webfilesys.WebFileSys;
import de.webfilesys.gui.ajax.AjaxCheckGrepAllowedHandler;
import de.webfilesys.util.CommonUtils;

/**
 * @author Frank Hoehnel
 */
public class TailRequestHandler extends UserRequestHandler
{
    private static final int NUMBER_OF_LINES_TO_PRINT = 40;
    
    private static final String ENCODING_ERROR = "#### failed to read line due to charcater encoding problems";
    
	private static final int BYTES_TO_CHECK = 2 * 1024 * 1024;
    
    private ArrayList lineQueue = null;
    
    private int lineCount = NUMBER_OF_LINES_TO_PRINT;
    
	public TailRequestHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid)
	{
        super(req, resp, session, output, uid);
        
        lineQueue = new ArrayList();
	}

	protected void process()
	{
		String filePath = getParameter("filePath");
		
		if (filePath == null)
		{
		    String fileName = getParameter("fileName");
		    
		    if (fileName == null)
		    {
		        Logger.getLogger(getClass()).warn("parameters filePath and fileName missing");
		        return;
		    }
		    else
		    {
		        String currentPath = getCwd();
		        if (currentPath.endsWith(File.separator))
		        {
		            filePath = currentPath + fileName;
		        }
		        else
		        {
                    filePath = currentPath + File.separator + fileName;
		        }
		    }
		}

		if (!checkAccess(filePath))
		{
		    return;	
		}

		String initial = req.getParameter("initial");
		
		if (initial != null) 
		{
			// prevent out of memory by readLine() a very large piece of data without linebreak
			// this is not 100 % save as we check only the beginning of very large files
			// and only on the initial call of tail
			// alternative would be to write our own readLine() method with limited line length
			
	        if (!isTextFile(filePath, WebFileSys.getInstance().getTextFileMaxLineLength(), BYTES_TO_CHECK))
	        {
	            resp.setStatus(404);
	            output.println("This file seems not to be a text file: " + getHeadlinePath(filePath));
	            output.flush();
	            return;
	        }
		}
		
		lineCount = NUMBER_OF_LINES_TO_PRINT;
		
		String lineCountParam = getParameter("lineCount");
		if (lineCountParam != null)
		{
		    try
		    {
		        lineCount = Integer.parseInt(lineCountParam);
		    }
		    catch (Exception ex)
		    {
		    }
		}
		
		boolean autoRefresh = false;
		
		String autoRefreshParam = getParameter("autoRefresh");
		
		if (autoRefreshParam != null) 
		{
		    autoRefresh = true;
		}
		
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
            
            String line = null;
            
            boolean eof = false;
            
            int excCounter = 0;
            
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
                        excCounter = 0;
                        queueLine(line);
                    }
                } 
                catch (MalformedInputException miEx) {
                    Logger.getLogger(getClass()).warn("error during reading file for tail", miEx);
                    excCounter++;
                    queueLine(ENCODING_ERROR);
                }
            }
        }
        catch (IOException ioex)
        {
            Logger.getLogger(getClass()).error("failed to read file for tail", ioex);
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
        
        output.println("<HTML>");
        output.println("<HEAD>");

        output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/css/" + userMgr.getCSS(uid) + ".css\">");

        output.println("<title>WebFileSys: tail</title>");
        
        output.println("<script src=\"/webfilesys/javascript/ajax.js\" type=\"text/javascript\"></script>");
        output.println("<script src=\"/webfilesys/javascript/ajaxCommon.js\" type=\"text/javascript\"></script>");
        
        output.println("<script type=\"text/javascript\">");
        output.println("var pollTimeout;");        
        output.println("var autoRefresh = " + (autoRefresh ? "true" : "false") + ";");        

        output.println("function startAutoRefresh() {");
        output.println("    if (autoRefresh) {");
        output.println("        startPollForChanges(3000);");
        output.println("    }");
        output.println("}");

        output.println("function startPollForChanges(startDelay) {");
        output.println("    pollTimeout = setTimeout(pollForChange, startDelay);");
        output.println("}");

        output.println("function pollForChange() {");
        output.println("    if (checkFileChange('" + insertDoubleBackslash(filePath) + "', '" + fileToSend.lastModified() + "', '" + fileToSend.length() + "')) {");
        output.println("        document.getElementById('tailForm').submit();");
        output.println("    } else {");
        output.println("        pollTimeout = setTimeout(pollForChange, 3000);");
        output.println("    }");
        output.println("}");

        output.println("function changeAutoRefresh() {");
        output.println("    var autoRefreshCheckbox = document.getElementById('autoRefresh');");
        output.println("    if (autoRefreshCheckbox.checked) {");
        output.println("        startPollForChanges(500);");
        output.println("    } else {");
        output.println("        window.clearTimeout(pollTimeout);");
        output.println("    }");
        output.println("}");
        
        output.println("</script>");
        
        output.println("</head>");
        output.println("<body onload=\"startAutoRefresh()\">");
        
        headLine(getHeadlinePath(filePath));
        
        output.println("<div style=\"float:right;border:1px solid black;padding:5px;background-color:ivory;\">");
        output.println("<form id=\"tailForm\" method=\"get\" accept-charset=\"utf-8\" action=\"/webfilesys/servlet\" style=\"display:inline;\">");
        output.println("<input type=\"hidden\" name=\"command\" value=\"tail\" />");
        output.println("<input type=\"hidden\" name=\"filePath\" value=\"" + filePath + "\" />");
        output.println(getResource("tail.lineCount", "number of lines from end of file") + ":");
        output.println("<input type=\"text\" id=\"lineCount\" name=\"lineCount\" value=\"" + lineCount + "\" style=\"width:80px\" />");
        output.println("<input type=\"submit\" value=\"" + getResource("button.reload", "Reload") + "\" style=\"width:80px\" />");
        output.println("&nbsp;");
        output.println("<input id=\"autoRefresh\" type=\"checkbox\" class=\"cb2\" name=\"autoRefresh\"" + (autoRefresh ? " checked=\"checked\"" : "") + " onclick=\"changeAutoRefresh()\" />");
        output.println(getResource("tail.autoRefresh", "automatic refresh"));
        output.println("</form>");
        output.println("</div>");
        
        output.println("<br/><br/>");

        output.println("<pre>");

        for (int i = 0; i < lineQueue.size(); i++)
        {
            String line = (String) lineQueue.get(i);
            output.println(CommonUtils.escapeHTML(line));
        }
        
        output.println("</pre>");
        
        output.println("<script type=\"text/javascript\">");
        output.println("document.getElementById('lineCount').select();");
        output.println("document.getElementById('lineCount').focus();");
        output.println("</script>");
        
        output.println("</body>");
        output.println("</html>");
        output.flush();
	}
	
	private void queueLine(String nextLine)
	{
	    lineQueue.add(nextLine);
	    
	    if (lineQueue.size() > lineCount)
	    {
	        // discard lines that are not to be printed
	        lineQueue.remove(0);
	    }
	}
}
