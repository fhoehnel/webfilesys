package de.webfilesys.gui.ajax;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import de.webfilesys.graphics.ImageDimensions;
import de.webfilesys.graphics.ImageUtils;
import de.webfilesys.graphics.ScaledImage;
import de.webfilesys.util.SessionKey;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XmlSlideShowImageHandler extends XmlRequestHandlerBase
{
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
		ArrayList<String> imageFiles = (ArrayList<String>) session.getAttribute(SessionKey.SLIDESHOW_BUFFER);
		
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
        
		String imgFileName = (String) imageFiles.get(imgIdx);
 
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
		
		ImageDimensions scaledDim = ImageUtils.getScaledImageDimensions(imgFileName, screenWidth - 4, screenHeight - 10);
		
		Element resultElement = doc.createElement("result");
		
		XmlUtil.setChildText(resultElement, "success", "true");
		
		XmlUtil.setChildText(resultElement, "message", "");

		XmlUtil.setChildText(resultElement, "imagePath", imgFileName);

		XmlUtil.setChildText(resultElement, "displayWidth", Integer.toString(scaledDim.getWidth()));
		XmlUtil.setChildText(resultElement, "displayHeight", Integer.toString(scaledDim.getHeight()));

		doc.appendChild(resultElement);
		
		this.processResponse();
	}
	
}
