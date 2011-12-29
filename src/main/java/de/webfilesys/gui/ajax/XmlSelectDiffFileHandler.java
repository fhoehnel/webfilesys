package de.webfilesys.gui.ajax;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;

import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XmlSelectDiffFileHandler extends XmlRequestHandlerBase
{
	public static final String SESSION_ATTRIB_DIFF_SOURCE = "diffSourceFolder";
	public static final String SESSION_ATTRIB_DIFF_TARGET = "diffTargetFolder";
	
	public XmlSelectDiffFileHandler(
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
        String cmd = getParameter("cmd");
        
        if ((cmd != null) && cmd.equals("deselect"))
        {
            session.removeAttribute(SESSION_ATTRIB_DIFF_SOURCE);
            session.removeAttribute(SESSION_ATTRIB_DIFF_TARGET);
            
            Element resultElement = doc.createElement("result");
            
            XmlUtil.setChildText(resultElement, "success", "true");
                
            doc.appendChild(resultElement);
            
            this.processResponse();

            return;
        }
        
		String path = getParameter("path");

		if (!checkAccess(path))
		{
			return;
		}

		boolean targetSelected = false;
		
		String diffSource = (String) session.getAttribute(SESSION_ATTRIB_DIFF_SOURCE);
		
		if (diffSource == null) 
		{
			session.setAttribute(SESSION_ATTRIB_DIFF_SOURCE, path);
		}
		else
		{
			session.setAttribute(SESSION_ATTRIB_DIFF_TARGET, path);
			targetSelected = true;
		}
		
		Element resultElement = doc.createElement("result");
		
		if (targetSelected)
		{
			XmlUtil.setChildText(resultElement, "success", "targetSelected");
		}
		else
		{
			String resultMsg = getResource("selectDiffSourceResult", "Navigate to the compare target file and select Start Compare from the context menu!");
			
			XmlUtil.setChildText(resultElement, "message", resultMsg);

			XmlUtil.setChildText(resultElement, "success", "sourceSelected");
		}
			
		doc.appendChild(resultElement);
		
		this.processResponse();
	}
}
