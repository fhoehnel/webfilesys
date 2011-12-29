package de.webfilesys.gui.user.unix;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.googlecode.compress_j2me.lzc.LZCInputStream;
import com.googlecode.compress_j2me.lzc.LZCOutputStream;

import de.webfilesys.gui.user.UserRequestHandler;
import de.webfilesys.util.CommonUtils;

/**
 * @author Frank Hoehnel
 */
public class CompressLZCRequestHandler extends UserRequestHandler
{
	public CompressLZCRequestHandler(
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

		String fileName = getParameter("actPath");

		String fileNameExt = CommonUtils.getFileExtension(fileName);
		
		output.println("<html>");
		output.println("<head>");

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/css/" + userMgr.getCSS(uid) + ".css\">");

		output.println("</head><body>");

        if (fileNameExt.equals(".z"))
        {
            headLine(getResource("label.uncompress", "uncompress"));
        }
        else
        {
            headLine(getResource("label.compress", "compress"));
        }
		
		output.println("<br>");
		output.println("<table class=\"dataForm\" width=\"100%\">");
		output.println("<tr><td class=\"formParm1\">");
		output.println(getResource("label.sourcefile", "source file") + ":");
		output.println("</td></tr>");
		output.println("<tr><td class=\"formParm2\">");
		output.println(CommonUtils.shortName(fileName, 60));
		output.println("</td></tr>");

        String targetFileName = null;

        try 
        {
            if (fileNameExt.equals(".z"))
            {
                targetFileName = uncompress(fileName);
            }
            else
            {
                targetFileName = compress(fileName);
            }

            output.println("<tr><td class=\"formParm1\">");
            output.println(getResource("label.targetfile", "target file") + ":");
            output.println("</td></tr>");
            output.println("<tr><td class=\"formParm2\">");
            output.println(CommonUtils.shortName(targetFileName, 60));
            output.println("</td></tr>");
        }
        catch (IOException ioex)
        {
            Logger.getLogger(getClass()).error("error during compress/uncompress of file " + fileName, ioex);
            javascriptAlert(getResource("alert.compresserror","Error during compress/uncompress!"));
        }
        
        output.println("</table>");

        output.println("<br>");

		output.println("<form>");
		output.println("<input type=\"button\" value=\"" + getResource("button.ok","OK") + "\" onclick=\"javascript:window.location.href='/webfilesys/servlet?command=listFiles&keepListStatus=true'\">");

		output.println("</form>");
		
		output.println("</body>");
		output.println("</html>");
		output.flush();
	}
	
	private String uncompress(String compressedFileName) 
	throws IOException
	{
	    int lastDotIdx = compressedFileName.lastIndexOf('.');
	    
        String uncompressedFileName = compressedFileName.substring(0, lastDotIdx);

        String appendix = "";
        
        int i = 1;
        
        boolean targetFileExists = false;

        do
        {
            targetFileExists = false;
            
            File targetFile = new File(uncompressedFileName + appendix);
            
            if (targetFile.exists())
            {
                targetFileExists = true;
                appendix = "_" + i;
            }
            
            i++;
        }
        while (targetFileExists);
        
        uncompressedFileName = uncompressedFileName + appendix;
        
        FileOutputStream uncompressedOut = new FileOutputStream(uncompressedFileName);
	    
        FileInputStream compressedIn = new FileInputStream(compressedFileName);

	    LZCInputStream lzwIn = new LZCInputStream(compressedIn);
	    byte[] buffer = new byte[128];
	    int bytesRead;
	    while ((bytesRead = lzwIn.read(buffer)) >= 0) 
	    {
	        uncompressedOut.write(buffer, 0, bytesRead);
	    }
	    uncompressedOut.flush();

	    compressedIn.close();
	    
	    uncompressedOut.close();
	    
	    return uncompressedFileName;
	}

    private String compress(String uncompressedFileName) 
    throws IOException
    {
        String compressedFileName = uncompressedFileName + ".Z";
        
        String appendix = "";
        
        int i = 1;
        
        boolean targetFileExists = false;

        do
        {
            targetFileExists = false;
            
            File targetFile = new File(compressedFileName + appendix);
            
            if (targetFile.exists())
            {
                targetFileExists = true;
                appendix = "_" + i;
            }
            
            i++;
        }
        while (targetFileExists);
        
        compressedFileName = compressedFileName + appendix;
        
        FileInputStream uncompressedIn = new FileInputStream(uncompressedFileName);

        FileOutputStream compressedOut = new FileOutputStream(compressedFileName);
        
        LZCOutputStream lzwOut = new LZCOutputStream(compressedOut);
        byte[] buffer = new byte[128];
        int bytesRead;
        while ((bytesRead = uncompressedIn.read(buffer)) >= 0) {
            lzwOut.write(buffer, 0, bytesRead);
        }
        
        lzwOut.flush();
        lzwOut.end();

        compressedOut.close();
        uncompressedIn.close();
        
        return compressedFileName;
    }
}
