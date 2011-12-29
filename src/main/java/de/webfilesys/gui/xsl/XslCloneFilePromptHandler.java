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
public class XslCloneFilePromptHandler extends XslRequestHandlerBase
{
	public XslCloneFilePromptHandler(
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

		String sourceFileName = req.getParameter("fileName");
		
		if ((sourceFileName == null) || (sourceFileName.trim().length() == 0))
		{
			Logger.getLogger(getClass()).error("required parameter fileName missing");
			
			return;
		}
		
		Element cloneFileElement = doc.createElement("cloneFile");
			
		doc.appendChild(cloneFileElement);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/cloneFile.xsl\"");

		doc.insertBefore(xslRef, cloneFileElement);

		XmlUtil.setChildText(cloneFileElement, "sourceFileName", sourceFileName, false);

		XmlUtil.setChildText(cloneFileElement, "shortFileName", CommonUtils.shortName(sourceFileName, 36), false);
		
		addMsgResource("label.cloneFile", getResource("label.cloneFile", "Clone File"));
		addMsgResource("label.cloneSource", getResource("label.cloneSource", "source file"));
		addMsgResource("label.newname", getResource("label.newname","new name"));
		addMsgResource("button.clone", getResource("button.clone","Create Clone"));
		addMsgResource("button.cancel", getResource("button.cancel","Cancel"));
		addMsgResource("alert.destEqualsSource", getResource("alert.destEqualsSource", "The new file name must be different!"));
		addMsgResource("alert.illegalCharInFilename", getResource("alert.illegalCharInFilename", "The new file name contains illegal characters."));

		resp.setContentType("text/xml");
		
		BufferedWriter xmlOutFile = new BufferedWriter(output);
            
		XmlUtil.writeToStream(doc, xmlOutFile);

		output.flush();
	}
}