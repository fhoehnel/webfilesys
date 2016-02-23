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
public class XmlRotateImagePromptHandler extends XmlRequestHandlerBase
{
	public XmlRotateImagePromptHandler(
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

		String imagePath = req.getParameter("imagePath");
		
		if ((imagePath == null) || (imagePath.trim().length() == 0))
		{
			Logger.getLogger(getClass()).error("required parameter imagePath missing");
			
			return;
		}
		
		int lastSepIdx = imagePath.lastIndexOf(File.separatorChar);
		
		if ((lastSepIdx < 0) || (lastSepIdx == (imagePath.length() - 1)))
		{
			return;
		}
		
		Element rotateImageElement = doc.createElement("rotateImage");
			
		doc.appendChild(rotateImageElement);
			
		XmlUtil.setChildText(rotateImageElement, "imagePath", imagePath, false);
		
		processResponse();
	}
}