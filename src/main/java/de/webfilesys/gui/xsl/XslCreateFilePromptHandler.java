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
public class XslCreateFilePromptHandler extends XslRequestHandlerBase
{
	public XslCreateFilePromptHandler(
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
 
		String relPath = null;
		
		String currentPath = getParameter("path");

        if (isMobile()) 
        {
            relPath = currentPath;
            currentPath = getAbsolutePath(currentPath);
        }
		
		if ((currentPath == null) || (currentPath.trim().length() == 0))
		{
			Logger.getLogger(getClass()).error("required parameter path missing");
			
			return;
		}
		
		if (!checkAccess(currentPath))
		{
			return;
		}

		Element createFileElement = doc.createElement("createFile");
			
		doc.appendChild(createFileElement);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/createFile.xsl\"");

		doc.insertBefore(xslRef, createFileElement);

		XmlUtil.setChildText(createFileElement, "baseFolder", currentPath, false);

        String shortPath = null;
        
        if (relPath != null) 
        {
            shortPath = CommonUtils.shortName(relPath, 32);
        }
        else
        {
            shortPath = CommonUtils.shortName(getHeadlinePath(currentPath), 32);
        }
		
		XmlUtil.setChildText(createFileElement, "baseFolderShort", shortPath, false);
		
		addMsgResource("label.createfile", getResource("label.createfile", "create new empty file"));
		addMsgResource("label.directory", getResource("label.directory","folder"));
		addMsgResource("label.newFileName", getResource("label.newFileName","name of new file"));
		addMsgResource("alert.illegalCharInFilename", getResource("alert.illegalCharInFilename", "The new file name contains illegal characters."));
		addMsgResource("button.create", getResource("button.create","Create"));
		addMsgResource("button.cancel", getResource("button.cancel","Cancel"));

		resp.setContentType("text/xml");
		
		BufferedWriter xmlOutFile = new BufferedWriter(output);
            
		XmlUtil.writeToStream(doc, xmlOutFile);

		output.flush();
	}
}