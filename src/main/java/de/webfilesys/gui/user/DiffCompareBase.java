package de.webfilesys.gui.user;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import name.fraser.neil.plaintext.diff_match_patch;
import name.fraser.neil.plaintext.diff_match_patch.Diff;
import name.fraser.neil.plaintext.diff_match_patch.Operation;

import org.apache.log4j.Logger;

import de.webfilesys.util.CommonUtils;

public class DiffCompareBase extends UserRequestHandler
{
    private static final long MAX_COMPARE_FILE_SIZE = 200 * 1024L;
    
    public DiffCompareBase(
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
        String file1Path = getParameter("file1Path");
        String file2Path = getParameter("file2Path");

        if ((file1Path == null) || (file2Path == null))
        {
            Logger.getLogger(getClass()).error("missing file path for diff");
        }
        
        if ((!checkAccess(file1Path)) || (!checkAccess(file2Path)))
        {
            return;
        }
        
        output.println("<html>");
        output.println("<head>");
        output.print("<title>");
        output.print("WebFileSys: " + getResource("title.diff","Compare Files (diff)"));
        output.println("</title>");

        output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/css/" + userMgr.getCSS(uid) + ".css\">");

        File file1 = new File(file1Path);
        File file2 = new File(file2Path);
        
        if ((file1.length() > MAX_COMPARE_FILE_SIZE) || (file2.length() > MAX_COMPARE_FILE_SIZE))
        {
            output.println("<script type=\"text/javascript\">");
            output.println("alert('" + getResource("compareFileSizeExceeded", "One or both of the files are too large for compare operation!") + "');");
            output.println("self.close();");
            output.println("</script>");
            output.println("</head>");
            output.println("</html>");
            output.flush();
            return;
        }

        output.println("<script type=\"text/javascript\">");
        output.println("function addScrollListener() {");
        output.println("var file1Div = document.getElementById('file1Cont')");
        output.println("var file2Div = document.getElementById('file2Cont')");
        output.println("var diffDiv = document.getElementById('diffCont')");
        output.println("diffDiv.onscroll = function reportScroll() {");
        output.println("var scrollPos = diffDiv.scrollTop;");
        output.println("file1Div.scrollTop = scrollPos");
        output.println("file2Div.scrollTop = scrollPos");
        output.println("}");
        output.println("}");
        output.println("</script>");
        
        String file1Content = readIntoBuffer(file1); 
        String file2Content = readIntoBuffer(file2); 
        
        diff_match_patch differ = new diff_match_patch();
        
        LinkedList differences = differ.diff_main(file1Content, file2Content);  

        Iterator iter = differences.iterator();

        int diffCount = 0;

        while (iter.hasNext())
        {
            Diff diffElem = (Diff) iter.next();
            
            if ((diffElem.operation == Operation.DELETE) || (diffElem.operation == Operation.INSERT)) 
            {
                diffCount++;
            }
        }
        
        int screenWidth = getScreenWidth();

        int screenHeight = getScreenHeight();
        
        int compareResultWidth = (screenWidth - 60) / 2;
        int compareSourceWidth = (screenWidth - 60 - compareResultWidth) / 2;
        
        int compareHeight = screenHeight - 130;
            
        output.println("</head>");
        output.println("<body class=\"diff\" onload=\"addScrollListener()\">");
        
        output.println("<table width=\"100%\" border=\"0\">");
        output.println("<tr>");
        output.println("<td class=\"diff\">");
        
        output.println("<span class=\"diff\">");
        output.println(CommonUtils.shortName(getHeadlinePath(file1.getAbsolutePath()), 36));
        output.println("</span>");
       
        output.println("<div id=\"file1Cont\" class=\"diff\" style=\"width:" + compareSourceWidth + "px;height:" + compareHeight + "px;\">");
        
        output.println("<pre>");

        StringTokenizer lineParser = new StringTokenizer(file1Content, "\n", true);
        
        while (lineParser.hasMoreTokens())
        {
            String token = lineParser.nextToken();
            
            if (token.equals("\n"))
            {
                output.println();
            }
            else
            {
                output.print("<font class=\"diff\">");
                output.print(encodeSpecialChars(token));
                output.print("</font>");
            }
        }
        
        output.println("</pre>");
        output.println("</div>");

        output.println("</td>");
        output.println("<td class=\"diff\">");
        output.println("<span class=\"diff\">");
        output.print(getResource("compareResult", "compare result"));
        output.println(": " + diffCount + " " + getResource("differences", "differences"));
        output.println("</span>");
        
        output.println("<div id=\"diffCont\" class=\"diff\" style=\"width:" + compareResultWidth + "px;height:" + compareHeight + "px;\">");
        
        output.println("<pre>");
        
        boolean prevElemEndsWithLinefeed = false;

        iter = differences.iterator();

        while (iter.hasNext())
        {
            Diff diffElem = (Diff) iter.next();
            
            String diffText = diffElem.text;
            
            boolean currentElemEndsWithLinefeed = (diffText.charAt(diffText.length() - 1) == '\n');
            
            if ((diffElem.operation == Operation.INSERT) || (diffElem.operation == Operation.DELETE))
            {
                diffText = highlightLonelyLinefeeds(diffText, prevElemEndsWithLinefeed);
            }
            else
            {
                diffText = encodeSpecialChars(diffText);
            }
            
            // diffText = newLineToHTML(diffText);
            
            if (diffElem.operation == Operation.EQUAL)
            {
                output.print("<font class=\"diffEqual\">");
                output.print(diffText);
                output.print("</font>");
            }
            else if (diffElem.operation == Operation.DELETE)
            {
                output.print("<font class=\"diffDel\">");
                output.print(diffText);
                output.print("</font>");
                diffCount++;
            }
            else if (diffElem.operation == Operation.INSERT)
            {
                output.print("<font class=\"diffIns\">");
                output.print(diffText);
                output.print("</font>");
                diffCount++;
            }
            
            prevElemEndsWithLinefeed = currentElemEndsWithLinefeed;
        }

        output.println("</pre>");

        output.println("</div>");

        output.println("</td>");
        output.println("<td class=\"diff\">");
        output.println("<span class=\"diff\">");
        output.println(CommonUtils.shortName(getHeadlinePath(file2.getAbsolutePath()), 36));
        output.println("</span>");
        
        output.println("<div id=\"file2Cont\" class=\"diff\" style=\"width:" + compareSourceWidth + "px;height:" + compareHeight + "px;\">");

        output.println("<pre>");

        lineParser = new StringTokenizer(file2Content, "\n", true);
        
        while (lineParser.hasMoreTokens())
        {
            String token = lineParser.nextToken();
            
            if (token.equals("\n"))
            {
                output.println();
            }
            else
            {
                output.print("<font class=\"diff\">");
                output.print(encodeSpecialChars(token));
                output.print("</font>");
            }
        }
        
        output.println("</pre>");
        output.println("</div>");
        output.println("</td>");
        output.println("</tr>");
        output.println("</table>");
        
        if (diffCount == 0) {
            output.println("<script type=\"text/javascript\">");
            output.println("alert('" + getResource("noDifferences", "No differences found.") + "');");
            output.println("</script>");
        }

        output.println("</body>");
        output.println("</html>");
        output.flush();
    }
    
    private boolean checkForLonelyLinefeeds(String diffText)
    {
        boolean noneLinefeedChar = false;

        boolean lonelyLinefeed = false;
        
        for (int i = 0; (!lonelyLinefeed) && (i < diffText.length()); i++) 
        {
            char ch = diffText.charAt(i);
            
            if ((ch != '\n') && (ch != '\r'))
            {
                noneLinefeedChar = true;
            }
            else
            {
                if ((ch == '\n'))
                {
                    if (!noneLinefeedChar)
                    {
                        lonelyLinefeed = true;
                    }
                    else
                    {
                        noneLinefeedChar = false;
                    }
                }
            }
        }
        
        return lonelyLinefeed;
    }
    
    private String highlightLonelyLinefeeds(String diffText, boolean prevElemEndsWithLinefeed)
    {
        if (!checkForLonelyLinefeeds(diffText))
        {
            return encodeSpecialChars(diffText);
        }
        
        StringBuffer highlightBuff = new StringBuffer(diffText.length() + 1);
        
        boolean noneLinefeedChar = false;
        
        boolean crPending = false;
        
        for (int i = 0; i < diffText.length(); i++) 
        {
            char ch = diffText.charAt(i);
            
            if ((ch != '\n') && (ch != '\r'))
            {
                noneLinefeedChar = true;
                highlightBuff.append(ch);
            }
            else
            {
                if ((ch == '\n'))
                {
                    if (!noneLinefeedChar)
                    {
                        if (prevElemEndsWithLinefeed) 
                        {
                            highlightBuff.append(' ');
                        }
                    }

                    if (crPending)
                    {
                        highlightBuff.append('\r');
                        crPending = false;
                    }
                    
                    highlightBuff.append(ch);

                    if (!noneLinefeedChar)
                    {
                        if (!prevElemEndsWithLinefeed) 
                        {
                            highlightBuff.append(' ');
                        }
                    }
                    
                    noneLinefeedChar = false;
                }
                else
                {
                    crPending = true;
                }
            }
        }
        
        return highlightBuff.toString();
    }
    
    private String readIntoBuffer(File file) {
        
        String fileEncoding = guessFileEncoding(file.getAbsolutePath());
        
        if (fileEncoding != null) {
            Logger.getLogger(getClass()).debug("reading diff file " + file.getAbsolutePath() + " with character encoding " + fileEncoding);
        }
        
        StringBuffer buff = new StringBuffer();     
        
        BufferedReader fileIn = null;
        
        try
        {
            if (fileEncoding == null) 
            {
                // unknown - use OS default encoding
                fileIn = new BufferedReader(new FileReader(file));
            }
            else 
            {
                FileInputStream fis = new FileInputStream(file);
                
                if (fileEncoding.equals("UTF-8-BOM")) {
                    // skip over BOM
                    fis.read();
                    fis.read();
                    fis.read();
                    fileEncoding = "UTF-8";
                }
                
                fileIn = new BufferedReader(new InputStreamReader(fis, fileEncoding));
            }
            
            String line = null;
            
            boolean firstLine = true;
            
            while ((line = fileIn.readLine()) != null)
            {
                if (firstLine) 
                {
                    firstLine = false;
                }
                else
                {
                    buff.append("\n");
                }
                buff.append(line);
            }
        }
        catch (IOException ioex)
        {
            Logger.getLogger(getClass()).error(ioex);
        }
        finally
        {
            try 
            {
                if (fileIn != null)
                {
                    fileIn.close();
                }
            }
            catch (Exception ex)
            {
                Logger.getLogger(getClass()).error(ex);
            }
        }
        
        return buff.toString();
    }
    
    private String encodeSpecialChars(String line)
    {
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
                if ((ch < 0x20) && (ch != 0x0a) && (ch != 0x0d))
                {
                    buff.append('.');
                }
                else
                {
                    buff.append(ch);
                }
            }
        }

        return(buff.toString());
    }
    
    private String newLineToHTML(String line)
    {
        StringBuffer buff = new StringBuffer();

        for (int i = 0; i < line.length(); i++)
        {
            char ch = line.charAt(i);

            if (ch == '\r')
            {
                buff.append("<br/>");
                if ((i + 1 < line.length()) && (line.charAt(i + 1) == '\n'))
                {
                    // ignore newline
                    i++;
                }
            }
            else if (ch == '\n')
            {
                buff.append("<br/>");
            }
            else 
            {
                buff.append(ch);
            }
        }

        return(buff.toString());
    }
    
}
