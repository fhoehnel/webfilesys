package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;

import de.webfilesys.util.XmlUtil;

/**
 * Set the last modified time of the file to the current time (touch command in UNIX).
 * @author Frank Hoehnel
 */
public class XmlTouchFileHandler extends XmlRequestHandlerBase
{
	public XmlTouchFileHandler(
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
		
		String fileName = getParameter("fileName");

		String targetFilePath = getCwd();
		
		if (targetFilePath.endsWith(File.separator))
		{
			targetFilePath = targetFilePath + fileName;
		}
		else
		{
			targetFilePath = targetFilePath + File.separator + fileName;
		}

		if (!checkAccess(targetFilePath))
		{
			return;
		}
        
        boolean touchOk = true;
        
        File targetFile = new File(targetFilePath);
        
        if (!targetFile.exists() || (!targetFile.isFile() || (!targetFile.canWrite())))
        {
            touchOk = false;
        }
        else
        {
            if (!targetFile.setLastModified(System.currentTimeMillis()))
            {
                touchOk = false;
            }
        }
        
        Element resultElement = doc.createElement("result");
        
		XmlUtil.setChildText(resultElement, "success", Boolean.toString(touchOk));
		
		if (!touchOk)
		{
	        XmlUtil.setChildText(resultElement, "message", getResource("alert.touchFailed", "Failed to set new modification time for file!"));
		}
		
		doc.appendChild(resultElement);
		
		this.processResponse();
	}
}
