package de.webfilesys.gui.xsl;

import java.io.BufferedWriter;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslRenameImagePromptHandler extends XslRequestHandlerBase
{
	public XslRenameImagePromptHandler(
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

		String imageFile = req.getParameter("imageFile");
		
		if (CommonUtils.isEmpty(imageFile))
		{
			LogManager.getLogger(getClass()).error("required parameter imageFile missing");
			return;
		}
		
		Element renameFileElement = doc.createElement("renameFile");
			
		doc.appendChild(renameFileElement);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/renameImage.xsl\"");

		doc.insertBefore(xslRef, renameFileElement);

		XmlUtil.setChildText(renameFileElement, "oldFileName", imageFile, false);
		XmlUtil.setChildText(renameFileElement, "oldFileNameForScript", escapeForJavascript(imageFile), false);

		XmlUtil.setChildText(renameFileElement, "shortFileName", CommonUtils.shortName(imageFile, 36), false);
		
		resp.setContentType("text/xml");
		
		BufferedWriter xmlOutFile = new BufferedWriter(output);
            
		XmlUtil.writeToStream(doc, xmlOutFile);

		output.flush();
	}
}