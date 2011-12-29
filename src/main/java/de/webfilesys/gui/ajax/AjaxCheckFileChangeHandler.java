package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class AjaxCheckFileChangeHandler extends XmlRequestHandlerBase
{
	public AjaxCheckFileChangeHandler(
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
        String filePath = getParameter("filePath");
        
        if (filePath == null) {
            return;
        }
        
        if (!checkAccess(filePath))
        {
            return;
        }
        
        String lastModifiedParam = getParameter("lastModified");
        
        if (lastModifiedParam == null)
        {
            return;
        }
        
        long lastModifiedOld;
        
        try
        {
            lastModifiedOld = Long.parseLong(lastModifiedParam);
        }
        catch (Exception ex)
        {
            Logger.getLogger(getClass()).warn(ex);
            return;
        }

        String sizeParam = getParameter("size");
        
        if (sizeParam == null)
        {
            return;
        }
        
        long sizeOld;
        
        try
        {
            sizeOld = Long.parseLong(sizeParam);
        }
        catch (Exception ex)
        {
            Logger.getLogger(getClass()).warn(ex);
            return;
        }
        
        File fileToCheck = new File(filePath);
        
        if ((!fileToCheck.exists()) || (!fileToCheck.canRead()))
        {
            Logger.getLogger(getClass()).warn(filePath + " is not a readable file");
            return;
        }
        
        Element resultElement = doc.createElement("result");
        
        boolean fileChanged = (fileToCheck.lastModified() > lastModifiedOld) || (fileToCheck.length() != sizeOld);
        
        XmlUtil.setElementText(resultElement, Boolean.toString(fileChanged));
        
        doc.appendChild(resultElement);
		
		processResponse();
	}
}
