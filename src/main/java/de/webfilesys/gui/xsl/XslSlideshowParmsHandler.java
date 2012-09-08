package de.webfilesys.gui.xsl;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslSlideshowParmsHandler extends XslRequestHandlerBase
{
    public static final String SLIDESHOW_BUFFER = "slideshowBuffer";

	public XslSlideshowParmsHandler(
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
		String currentPath = getCwd();
		
		String startFile = getParameter("startFile");
		String startPath = getParameter("startPath");
        int screenWidth = getIntParam("screenWidth", 1024);
        int screenHeight = getIntParam("screenHeight", 768);
        
        session.removeAttribute(SLIDESHOW_BUFFER);

        session.setAttribute("screenWidth", new Integer(screenWidth));
        session.setAttribute("screenHeight", new Integer(screenHeight));

		String shortPath = CommonUtils.shortName(getHeadlinePath(currentPath), 80);
		
		Element slideShowParmsElement = doc.createElement("slideShowParms");
			
		doc.appendChild(slideShowParmsElement);

		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/slideShowParms.xsl\"");

		doc.insertBefore(xslRef, slideShowParmsElement);

		XmlUtil.setChildText(slideShowParmsElement, "css", userMgr.getCSS(uid), false);
		XmlUtil.setChildText(slideShowParmsElement, "currentPath", currentPath, false);
		XmlUtil.setChildText(slideShowParmsElement, "shortPath", shortPath, false);

		if ((startFile != null) && (startFile.trim().length() > 0) && 
			(startPath != null) && (startPath.trim().length() > 0)) {
			XmlUtil.setChildText(slideShowParmsElement, "startFile", startFile, false);
			XmlUtil.setChildText(slideShowParmsElement, "startPath", startPath, false);
			XmlUtil.setChildText(slideShowParmsElement, "encodedStartPath", insertDoubleBackslash(startPath), false);
			addMsgResource("label.startPic", getResource("label.startPic","start picture"));
		}
		
		addMsgResource("label.slideparmhead", getResource("label.slideparmhead","Slideshow Parameters"));
		addMsgResource("label.directory", getResource("label.directory", "folder"));
		addMsgResource("label.recurse", getResource("label.recurse","include subdirectories"));
		addMsgResource("label.autoForward", getResource("label.autoForward","automatic forward"));
		addMsgResource("label.delay", getResource("label.delay","delay (seconds)"));
		addMsgResource("label.fullScreen", getResource("label.fullScreen", "fullscreen"));
		
		if (browserManufacturer == BROWSER_MSIE) 
		{
	        addMsgResource("label.crossfade", getResource("label.crossfade", "crossfade"));
		}
		
		addMsgResource("button.startshow", getResource("button.startshow","start slideshow"));
		addMsgResource("button.cancel", getResource("button.cancel", "cancel"));
			
		this.processResponse("slideShowParms.xsl", true);
    }
}