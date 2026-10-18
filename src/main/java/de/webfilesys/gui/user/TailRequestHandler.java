package de.webfilesys.gui.user;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import de.webfilesys.WebFileSysConfig;
import de.webfilesys.util.UTF8URLEncoder;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import de.webfilesys.util.CommonUtils;

/**
 * @author Frank Hoehnel
 */
public class TailRequestHandler extends UserRequestHandler {
    private static final Logger LOG = LogManager.getLogger(TailRequestHandler.class);
    
    private static final int NUMBER_OF_LINES_TO_PRINT = 40;
    
    private static final String ENCODING_ERROR = "#### failed to read line due to charcater encoding problems";
    
	private static final int BYTES_TO_CHECK = 2 * 1024 * 1024;
    
    private final ArrayList<String> lineQueue;
    
	public TailRequestHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid) {
        super(req, resp, session, output, uid);
        lineQueue = new ArrayList<>();
	}

	protected void process() {
		String fileName = getParameter("fileName");
        String filePath = CommonUtils.joinFilesysPath(getCwd(), fileName);

		String initial = req.getParameter("initial");
		
		if (initial != null) {
			// prevent out of memory by readLine() a very large piece of data without linebreak
			// this is not 100 % save as we check only the beginning of very large files
			// and only on the initial call of tail
			// alternative would be to write our own readLine() method with limited line length
			
	        if (!isTextFile(filePath, WebFileSysConfig.getInstance().getTextFileMaxLineLength(), BYTES_TO_CHECK))
	        {
	            resp.setContentType("text/plain");
	            output.println(getResource("tail.noTextFile", "This file seems not to be a text file") + ": " + getHeadlinePath(filePath));
	            output.flush();
	            return;
	        }
		}

        int lineCount = getIntParameter("lineCount", NUMBER_OF_LINES_TO_PRINT);
		boolean autoRefresh = getParameter("autoRefresh") != null;
		boolean error = false;
		
        File fileToSend = new File(filePath);
        if (!fileToSend.exists()) {
        	LOG.warn("requested file does not exist: " + filePath);
        	error = true;
        } else if ((!fileToSend.isFile()) || (!fileToSend.canRead())) {
        	LOG.warn("requested file is not a readable file: " + filePath);
        	error = true;
        }

        if (error) {
            resp.setContentType("text/plain");
            output.println("File not found or not readable: " + filePath);
            output.flush();
            return;
        }
		
        String fileEncoding = guessFileEncoding(filePath);
		
        BufferedReader fin = null;
        FileInputStream fis = null;
        try {
            if (fileEncoding == null) {
                // unknown - use OS default encoding
                fin = new BufferedReader(new FileReader(filePath));
            } else {
                fis = new FileInputStream(filePath);
                fin = new BufferedReader(new InputStreamReader(fis, fileEncoding));
            }
            boolean eof = false;
            int excCounter = 0;
            
            while ((!eof) && (excCounter < 5)) {
                try {
                    String line = fin.readLine();
                    if (line == null) {
                        eof = true;
                    } else {
                        excCounter = 0;
                        queueLine(line, lineCount);
                    }
                } catch (Exception miEx) {
                    LOG.warn("error during reading file for tail", miEx);
                    excCounter++;
                    queueLine(ENCODING_ERROR, lineCount);
                }
            }
        } catch (IOException ioex) {
            LOG.error("failed to read file for tail", ioex);
        } finally {
            try {
                if (fin != null) {
                    fin.close();
                }
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception ex) {
            }
        }
        
        output.println("<HTML>");
        output.println("<HEAD>");

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/common.css\">");
        output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/skins/" + userMgr.getCSS(uid) + ".css\">");

        output.println("<title>WebFileSys: tail</title>");
        
        output.println("<script src=\"/webfilesys/javascript/ajax.js\" type=\"text/javascript\"></script>");
        output.println("<script src=\"/webfilesys/javascript/ajaxCommon.js\" type=\"text/javascript\"></script>");
        output.println("<script src=\"/webfilesys/javascript/tail.js\" type=\"text/javascript\"></script>");
        
        output.println("<script type=\"text/javascript\">");
        output.println("var autoRefresh = " + (autoRefresh ? "true" : "false") + ";");    
        output.println("var pathForScript = '" + insertDoubleBackslash(filePath) + "';");
        output.println("var lastModified = '" + fileToSend.lastModified() + "';");
        output.println("var fileSize = '" + fileToSend.length() + "';");
        
        output.println("</script>");
        
        output.println("</head>");
        output.println("<body onload=\"startAutoRefresh()\" class=\"tail\">");
        
        headLine(getHeadlinePath(filePath));
        
        output.println("<div style=\"float:right;border:1px solid black;padding:5px;background-color:ivory;\">");
        output.println("<form id=\"tailForm\" method=\"get\" accept-charset=\"utf-8\" action=\"/webfilesys/servlet\" style=\"display:inline;\">");
        output.println("<input type=\"hidden\" name=\"command\" value=\"tail\" />");
        output.println("<input type=\"hidden\" name=\"fileName\" value=\"" + UTF8URLEncoder.encode(fileName) + "\" />");
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

        for (String line : lineQueue) {
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
	
	private void queueLine(String nextLine, int linesToPrintCount) {
	    lineQueue.add(nextLine);
	    if (lineQueue.size() > linesToPrintCount) {
	        // discard lines that are not to be printed
	        lineQueue.remove(0);
	    }
	}
}
