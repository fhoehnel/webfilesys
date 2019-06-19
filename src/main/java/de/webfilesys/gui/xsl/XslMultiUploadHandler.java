package de.webfilesys.gui.xsl;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.Constants;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslMultiUploadHandler extends XslRequestHandlerBase
{
	public XslMultiUploadHandler(
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

		String currentPath = getParameter("actpath");
		
		if (currentPath == null)
		{
		    currentPath = getCwd();
		} 
		else 
		{
		    session.setAttribute(Constants.SESSION_KEY_CWD, currentPath);
		}

		if (!checkAccess(currentPath))
		{
			return;
		}

		String relativePath = this.getHeadlinePath(currentPath);

	    String shortPath = CommonUtils.shortName(relativePath, 60);

		Element uploadElement = doc.createElement("upload");
			
		doc.appendChild(uploadElement);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/multiUpload.xsl\"");

		doc.insertBefore(xslRef, uploadElement);

		XmlUtil.setChildText(uploadElement, "currentPath", currentPath, false);
		XmlUtil.setChildText(uploadElement, "shortPath", shortPath, false);
		
		if (session.getAttribute("mobile") != null) {
		    XmlUtil.setChildText(uploadElement, "mobile", "true", false);
		}
		
		this.processResponse("multiUpload.xsl");
    }
}