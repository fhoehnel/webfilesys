package de.webfilesys.gui.xsl;

import java.io.BufferedWriter;
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
public class XslRenameFilePromptHandler extends XslRequestHandlerBase
{
	public XslRenameFilePromptHandler(
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

		String oldFileName = req.getParameter("fileName");
		
		if ((oldFileName == null) || (oldFileName.trim().length() == 0))
		{
			Logger.getLogger(getClass()).error("required parameter fileName missing");
			
			return;
		}
		
		Element renameFileElement = doc.createElement("renameFile");
			
		doc.appendChild(renameFileElement);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/renameFile.xsl\"");

		doc.insertBefore(xslRef, renameFileElement);

		XmlUtil.setChildText(renameFileElement, "oldFileName", oldFileName, false);

		XmlUtil.setChildText(renameFileElement, "shortFileName", CommonUtils.shortName(oldFileName, 36), false);
		
        String mobile = getParameter("mobile");
        
        if (mobile != null)
        {
            XmlUtil.setChildText(renameFileElement, "mobile", "true", false);
        }
		
		addMsgResource("label.renameFile", getResource("label.renameFile","Rename File"));
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