package de.webfilesys.gui.xsl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
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
		
		String oldFileName = imagePath.substring(lastSepIdx + 1);
		
		Element renameFileElement = doc.createElement("renameFile");
			
		doc.appendChild(renameFileElement);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/renameImage.xsl\"");

		doc.insertBefore(xslRef, renameFileElement);

		XmlUtil.setChildText(renameFileElement, "imagePath", imagePath, false);

		XmlUtil.setChildText(renameFileElement, "oldFileName", oldFileName, false);

		XmlUtil.setChildText(renameFileElement, "shortFileName", CommonUtils.shortName(oldFileName, 36), false);
		
		addMsgResource("label.renameImage", getResource("label.renameImage","Rename Picture File"));
		addMsgResource("label.oldName", getResource("label.oldName","current name"));
		addMsgResource("label.newname", getResource("label.newname","new name"));
		addMsgResource("button.rename", getResource("button.rename","Rename"));
		addMsgResource("button.cancel", getResource("button.cancel","Cancel"));
		addMsgResource("alert.destEqualsSource", getResource("alert.destEqualsSource", "The new file name must be different!"));
		addMsgResource("alert.illegalCharInFilename", getResource("alert.illegalCharInFilename", "The new file name contains illegal characters."));

		resp.setContentType("text/xml");
		
		BufferedWriter xmlOutFile = new BufferedWriter(output);
            
		XmlUtil.writeToStream(doc, xmlOutFile);

		output.flush();
	}
}