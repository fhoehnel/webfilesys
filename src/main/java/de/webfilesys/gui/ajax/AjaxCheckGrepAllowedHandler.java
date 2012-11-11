package de.webfilesys.gui.ajax;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;

import de.webfilesys.WebFileSys;
import de.webfilesys.util.XmlUtil;

/**
 * Checks if grep function is allowed for this file.
 * Grep on large binary files could kill the VM because the readLine() method has no limit for amount of data.
 * @author Frank Hoehnel
 */
public class AjaxCheckGrepAllowedHandler extends XmlRequestHandlerBase
{
	private static final int BYTES_TO_CHECK = 1024 * 1024;
	
	public AjaxCheckGrepAllowedHandler(
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
        String filePath = getParameter("param1");
        
        if (filePath == null) {
            return;
        }
        
        if (!checkAccess(filePath)) {
        	return;
        }

        boolean grepForbidden = (!isTextFile(filePath, WebFileSys.getInstance().getTextFileMaxLineLength(), BYTES_TO_CHECK));
        
        Element resultElement = doc.createElement("result");
        
        if (grepForbidden)
        {
            String errorText = getResource("grepNotAllowed", "grep function allowed only for text files");
            
            XmlUtil.setElementText(resultElement, errorText);
        }
        else
        {
            XmlUtil.setElementText(resultElement, "true");
        }
        
        doc.appendChild(resultElement);
		
		processResponse();
	}
}
