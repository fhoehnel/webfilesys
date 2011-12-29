package de.webfilesys.gui.xsl;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslCoBrowsingClientHandler extends XslRequestHandlerBase
{
	public static final String SLIDESHOW_BUFFER = "slideshowBuffer";
	
	public static final String imgFileMasks[]={"*.gif","*.jpg","*.jpeg","*.png","*.bmp"};

	public XslCoBrowsingClientHandler(
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
		Element slideShowElement = doc.createElement("slideShow");
		
		doc.appendChild(slideShowElement);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/coBrowsingClient.xsl\"");

		doc.insertBefore(xslRef, slideShowElement);

		XmlUtil.setChildText(slideShowElement, "css", userMgr.getCSS(uid), false);

        addMsgResource("titleCoBrowsingClient", getResource("titleCoBrowsingClient", "WebFileSys Co-Browsing (Client)"));
        addMsgResource("headCoBrowsingClient", getResource("headCoBrowsingClient", "WebFileSys Co-Browsing Slideshow: waiting for first picture"));
        
		this.processResponse("coBrowsingClient.xsl", false);
	}

}
