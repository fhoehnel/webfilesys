package de.webfilesys.gui.ajax;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;

import de.webfilesys.graphics.ThumbnailGarbageCollector;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XmlClearThumbsHandler extends XmlRequestHandlerBase
{
	public XmlClearThumbsHandler(
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
		if (!isAdminUser(false))
		{
			return;
		}
		
		if (!checkWriteAccess())
		{
			return;
		}

		String path = getParameter("path");

		if (!checkAccess(path))
		{
			return;
		}

		ThumbnailGarbageCollector thumbnailCleaner=new ThumbnailGarbageCollector(path);
		
		thumbnailCleaner.start();
		
		String resultMsg = getResource("alert.thumbclean","Thumbnail garbage collection started for directory") + "<br/> " + CommonUtils.shortName(path, 35);
		
		Element resultElement = doc.createElement("result");
		
		XmlUtil.setChildText(resultElement, "message", resultMsg);

		XmlUtil.setChildText(resultElement, "success", "true");
			
		doc.appendChild(resultElement);
		
		this.processResponse();
	}
}
