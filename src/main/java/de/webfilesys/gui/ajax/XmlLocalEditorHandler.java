package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;

import de.webfilesys.SystemEditor;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XmlLocalEditorHandler extends XmlRequestHandlerBase
{
	public XmlLocalEditorHandler(
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

        String fullPath = null;

		String filePath = getParameter("filePath");
        
		if (filePath != null)
		{
            fullPath = filePath;
		}
		else
		{
			String fileName = getParameter("fileName");
 
			String actPath = getCwd();
			
			if (actPath.endsWith(File.separator))
			{
				fullPath = actPath + fileName;
			}
			else
			{
				fullPath = actPath + File.separator + fileName;
			}
		}

		if (!checkAccess(fullPath))
		{
			return;
		}

		SystemEditor editor = new SystemEditor(fullPath);

		editor.start();
		
		Element resultElement = doc.createElement("result");
		
		XmlUtil.setChildText(resultElement, "success", "true");
			
		doc.appendChild(resultElement);
		
		this.processResponse();
	}
}
