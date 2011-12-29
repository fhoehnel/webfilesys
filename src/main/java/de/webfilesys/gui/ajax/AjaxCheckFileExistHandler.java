package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;

import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class AjaxCheckFileExistHandler extends XmlRequestHandlerBase
{
	public AjaxCheckFileExistHandler(
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
        String path = getCwd();

        String fileName = getParameter("param1");
        
        if (fileName == null) {
            return;
        }
        
        File fileToCheck = new File(path, fileName);
        
        Element resultElement = doc.createElement("result");
        
        XmlUtil.setElementText(resultElement, Boolean.toString(fileToCheck.exists()));
        
        doc.appendChild(resultElement);
		
		processResponse();
	}
}
