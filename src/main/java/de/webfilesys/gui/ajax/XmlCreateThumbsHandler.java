package de.webfilesys.gui.ajax;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;

import de.webfilesys.WebFileSys;
import de.webfilesys.graphics.ThumbnailThread;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XmlCreateThumbsHandler extends XmlRequestHandlerBase
{
	public XmlCreateThumbsHandler(
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

		String path = getParameter("path");

		if (!checkAccess(path))
		{
			return;
		}

		String resultMsg = null;

		if (WebFileSys.getInstance().isThumbThreadRunning())
		{
			resultMsg = getResource("msg.concurrentThumbThread", "Another thumbnail creation thread is still running. Try again later!");
		}
		else
		{
			ThumbnailThread thumbThread = new ThumbnailThread(path);
			
			(new Thread(thumbThread)).start();

			resultMsg = getResource("msg.createThumbsStarted", "Generation of permanent picture thumbnails has been started for folder") + "<br/>" + CommonUtils.shortName(path, 35);
		}
		
		Element resultElement = doc.createElement("result");
		
		XmlUtil.setChildText(resultElement, "message", resultMsg);

		XmlUtil.setChildText(resultElement, "success", "true");
			
		doc.appendChild(resultElement);
		
		this.processResponse();
	}
}
