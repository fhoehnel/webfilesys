package de.webfilesys.gui.ajax;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import de.webfilesys.CoBrowsingManager;
import de.webfilesys.graphics.ScaledImage;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XmlCoBrowsingMasterImageHandler extends XmlRequestHandlerBase
{
	public static final String SESSION_KEY_SLIDESHOW_BUFFER = "slideshowBuffer";

	public XmlCoBrowsingMasterImageHandler(
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
        
		String imgPath = (String) imageFiles.elementAt(imgIdx);
 
		int windowWidth = 1000;
		int windowHeight = 720;
		
        String windowWidthParm = req.getParameter("windowWidth");
        String windowHeightParm = req.getParameter("windowHeight");

        try
        {
            windowWidth = Integer.parseInt(windowWidthParm);
            windowHeight = Integer.parseInt(windowHeightParm);
        }
        catch (NumberFormatException numEx)
        {
        }
		
		ScaledImage scaledImage = null;

		try
		{
			if (browserManufacturer == BROWSER_MSIE)
			{
	            scaledImage=new ScaledImage(imgPath, windowWidth - 12, windowHeight - 22);
			}
			else
			{
	            scaledImage=new ScaledImage(imgPath, windowWidth - 12, windowHeight - 16);
			}
		}
		catch (IOException io1)
		{
			Logger.getLogger(getClass()).error(io1, io1);
			return;
		}
		
        CoBrowsingManager.getInstance().setCoBrowsingImage(uid, imgPath);		
		
		Element resultElement = doc.createElement("result");
		
		XmlUtil.setChildText(resultElement, "success", "true");
		
		XmlUtil.setChildText(resultElement, "message", "");

		XmlUtil.setChildText(resultElement, "imagePath", imgPath);

		XmlUtil.setChildText(resultElement, "imageWidth", Integer.toString(scaledImage.getRealWidth()));
		XmlUtil.setChildText(resultElement, "imageHeight", Integer.toString(scaledImage.getRealHeight()));
		XmlUtil.setChildText(resultElement, "displayWidth", Integer.toString(scaledImage.getScaledWidth()));
		XmlUtil.setChildText(resultElement, "displayHeight", Integer.toString(scaledImage.getScaledHeight()));

		doc.appendChild(resultElement);
		
		this.processResponse();
	}
	
}
