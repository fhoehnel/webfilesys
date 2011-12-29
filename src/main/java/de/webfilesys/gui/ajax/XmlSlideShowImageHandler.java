package de.webfilesys.gui.ajax;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import de.webfilesys.graphics.ScaledImage;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XmlSlideShowImageHandler extends XmlRequestHandlerBase
{
	public static final String SESSION_KEY_SLIDESHOW_BUFFER = "slideshowBuffer";

	public XmlSlideShowImageHandler(
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
		Vector imageFiles = (Vector) session.getAttribute(SESSION_KEY_SLIDESHOW_BUFFER);
		
		if (imageFiles == null)
		{
			Logger.getLogger(getClass()).warn("slideshow buffer not found in session");
			return;
		}
		
		String imageIdx = getParameter("imageIdx");
        
        int imgIdx = 0;
        
        if (imageIdx != null)
        {
            try
            {
            	imgIdx = Integer.parseInt(imageIdx);
            }
            catch (NumberFormatException numEx)
            {
            	
            }
        }
        
		String imgFileName = (String) imageFiles.elementAt(imgIdx);
 
		int screenWidth = 1024;
		int screenHeight = 768;
		
		try
		{
            String winWidth = req.getParameter("windowWidth");
            if (winWidth != null) {
                screenWidth = Integer.parseInt(winWidth);
            }
            String winHeight = req.getParameter("windowHeight");
            if (winHeight != null) {
                screenHeight = Integer.parseInt(winHeight);
            }
		}
		catch (Exception ex)
		{
		    Logger.getLogger(getClass()).error("failed to determine window dimensions");
		}
		
		ScaledImage scaledImage = null;

		try
		{
            scaledImage=new ScaledImage(imgFileName, screenWidth - 4, screenHeight - 10);
		}
		catch (IOException io1)
		{
			Logger.getLogger(getClass()).error(io1, io1);
			return;
		}
		
		Element resultElement = doc.createElement("result");

		
		XmlUtil.setChildText(resultElement, "success", "true");
		
		XmlUtil.setChildText(resultElement, "message", "");

		XmlUtil.setChildText(resultElement, "imagePath", imgFileName);

		XmlUtil.setChildText(resultElement, "imageWidth", Integer.toString(scaledImage.getRealWidth()));
		XmlUtil.setChildText(resultElement, "imageHeight", Integer.toString(scaledImage.getRealHeight()));
		XmlUtil.setChildText(resultElement, "displayWidth", Integer.toString(scaledImage.getScaledWidth()));
		XmlUtil.setChildText(resultElement, "displayHeight", Integer.toString(scaledImage.getScaledHeight()));

		doc.appendChild(resultElement);
		
		this.processResponse();
	}
	
}
