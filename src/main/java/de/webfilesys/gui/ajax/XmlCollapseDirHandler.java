package de.webfilesys.gui.ajax;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import de.webfilesys.DirTreeStatus;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XmlCollapseDirHandler extends XmlRequestHandlerBase
{
	public XmlCollapseDirHandler(
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
		String collapsePath = this.getParameter("path");
		
		if (collapsePath == null)
		{
			return;
		}
		
		if (!accessAllowed(collapsePath))
		{
			Logger.getLogger(getClass()).warn("user " + this.getUid() + " tried to access directory outside the home directory: " + collapsePath);
			
            // todo: create "access denied" XML response before returning
			
			return;
		}

        DirTreeStatus dirTreeStatus = (DirTreeStatus) session.getAttribute("dirTreeStatus");
		
		if (dirTreeStatus != null)
		{
			dirTreeStatus.collapseDir(collapsePath);
		}
        	
      	Element resultElement = doc.createElement("result");

		XmlUtil.setChildText(resultElement, "success", "true");

		doc.appendChild(resultElement);
		
		this.processResponse();
	}
}
