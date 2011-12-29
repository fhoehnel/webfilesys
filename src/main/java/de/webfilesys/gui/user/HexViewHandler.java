package de.webfilesys.gui.user;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.util.UTF8URLEncoder;

/**
 * @author Frank Hoehnel
 */
public class HexViewHandler extends UserRequestHandler
{
    public static final int MAX_BYTES_PER_PAGE = 50000;
    
    public static final int MAX_START_IDX = 100000000;
    
	public HexViewHandler(
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
		String fileName = getParameter("fileName");
		
		if ((fileName == null) || (fileName.trim().length() == 0) || (fileName.indexOf("..") >= 0))  
		{
		    return;
		}

        String currentPath = (String) session.getAttribute("cwd");

        int startIdx = 0;
        
        String startIdxParam = getParameter("startIdx");
        
        try
        {
            startIdx = Integer.parseInt(startIdxParam);
            
            if (startIdx > MAX_START_IDX)
            {
                startIdx = MAX_START_IDX;
            }
        }
        catch (Exception ex)
        {
        }
        
		output.println("<html>");
		output.println("<head>");

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/css/" + userMgr.getCSS(uid) + ".css\">");
		
		File hexFile = new File(currentPath, fileName);

		if (!hexFile.exists()) 
		{
		    return;
		}
		
		if ((!hexFile.isFile()) || (!hexFile.canRead()))
		{
		    Logger.getLogger(getClass()).warn(hexFile.getAbsolutePath() + " is not a readable file");
		    return;
		}
		
        output.print("<title>"); 
		output.println("WebFileSys " + getResource("title.hexView","Hex File Viewer") + ": " + fileName);
        output.print("</title>"); 
		
        output.println("<script type=\"text/javascript\">");
        output.println("function paging(startIdx)");
        output.println("{");
        output.println("window.location.href = '/webfilesys/servlet?command=hexView&fileName=" + UTF8URLEncoder.encode(fileName) + "&startIdx=' + startIdx;");
        output.println("}");
        output.println("</script>");
        
        output.println("</head>"); 

		output.println("<body>");

		headLine(getHeadlinePath(hexFile.getAbsolutePath()));

		boolean hasMoreBytes = printFileContent(hexFile, startIdx);
		
		output.println("<br/>");
		
        output.println("<form>");
        
        output.println("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">");
	    output.println("<tr>");

        output.println("<td style=\"text-align:left\">");

        if (startIdx > 0)
        {
            int prevStartIdx = startIdx - MAX_BYTES_PER_PAGE;
            
            if (prevStartIdx < 0)
            {
                prevStartIdx = 0;
            }

            output.println("<input type=\"button\" onclick=\"paging(" + prevStartIdx + ")\" value=\"" + getResource("button.previous","Previous") + "\"/>");
        } 
        else
        {
            output.println("&nbsp;");
        }
        
        output.println("</td>");

        output.println("<td style=\"text-align:center\">");

		output.println("<input type=\"button\" onclick=\"self.close()\" value=\"" + getResource("button.closewin","Close Window") + "\"/>");

		output.println("</td>");
		
        output.println("<td style=\"text-align:right\">");

        if (hasMoreBytes)
		{
	        output.println("<input type=\"button\" onclick=\"paging(" + (startIdx + MAX_BYTES_PER_PAGE) + ")\" value=\"" + getResource("button.next","Next") + "\"/>");
		}
        else
        {
            output.println("&nbsp;");
        }

        output.println("</td>");
				
		output.println("</tr>");
		output.println("</table>");
			
        output.println("</form>");
        
		output.println("</body>");
		output.println("</html>");

		output.flush();
	}

	private boolean printFileContent(File hexFile, int startIdx) 
	{
        int index = startIdx;
        
	    output.println("<table class=\"hexView\" border=\"0\" width=\"100%\">");
	    
	    byte[] buff = new byte[16];

	    BufferedInputStream fileIn = null;
	    
	    try
	    {
	        fileIn = new BufferedInputStream(new FileInputStream(hexFile));
	        
	        // skip over previous pages
	        int skipChar = 0;
	        
	        for (int k = 0; (k < startIdx) && (skipChar >= 0); k++)
	        {
	            skipChar = fileIn.read();
	        }
	        
	        int bytesRead = (-1);
	        
            while ((index < startIdx + MAX_BYTES_PER_PAGE) && ((bytesRead = fileIn.read(buff)) >= 0))
            {
                output.println("<tr>");

                output.print("<td class=\"index\">");
                String idxAsString = Integer.toHexString(index).toUpperCase();
                if (idxAsString.length() == 1)
                {
                    output.print('0');    
                }
                output.print(idxAsString);
                output.print("</td>");
                
                int i = 0;
                
                for (; i < bytesRead; i++)
                {
                    output.print("<td class=\"hexValue\">");
                    int byteAsInt = buff[i];
                    byteAsInt &= 0x000000FF;                    
                    
                    String byteAsString = Integer.toHexString(byteAsInt).toUpperCase();
                    if (byteAsString.length() == 1)
                    {
                        output.print('0');    
                    }
                    output.print(byteAsString);
                    output.println("</td>");
                }
                
                for (; i < 16; i++) 
                {
                    output.print("<td class=\"hexValue\">&nbsp;</td>");
                }

                i = 0;
                
                for (; i < bytesRead; i++)
                {
                    output.print("<td class=\"ascii\">");

                    char ch = (char) buff[i];
                    ch &= 0x000000FF;
                    
                    if (((ch >= 32) &&  (ch <= 126)) || Character.isLetterOrDigit(ch)) 
                    {
                        output.print(ch);
                    }
                    else
                    {
                        output.println('.');
                    }
                    
                    output.println("</td>");
                }
                
                for (; i < 16; i++) 
                {
                    output.print("<td class=\"ascii\">&nbsp;</td>");
                }
                
                output.println("</tr>");
                
                index += 16;
            }
	    }
	    catch (IOException ioex)
	    {
	        Logger.getLogger(getClass()).error("error in reading hex viewer file " + hexFile.getAbsolutePath(), ioex);
	        
            output.println("<script type=\"text/javascript\">");
            output.println("alert('error occured while reading file " + hexFile.getName() + "');");
            output.println("</script>");
	    }
	    finally
	    {
	        if (fileIn != null)
	        {
	            try
	            {
	                fileIn.close();
	            }
	            catch (Exception ex)
	            {
	            }
	        }
	    }

	    output.println("</table>");
	    
	    return (index >= startIdx + MAX_BYTES_PER_PAGE);
	}
	
}
