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
public class XslCreateFolderPromptHandler extends XslRequestHandlerBase
{
	public XslCreateFolderPromptHandler(
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

		String path = getParameter("path");		
		
        String currentPath = null;

        if (isMobile())
        {
            currentPath = getAbsolutePath(path);
        }
        else
        {
            currentPath = path;
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

		Element createFolderElement = doc.createElement("createFolder");
			
		doc.appendChild(createFolderElement);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/createFolder.xsl\"");

		doc.insertBefore(xslRef, createFolderElement);

		XmlUtil.setChildText(createFolderElement, "baseFolder", currentPath, false);

		XmlUtil.setChildText(createFolderElement, "baseFolderShort", CommonUtils.shortName(getHeadlinePath(path), 32), false);
		
		addMsgResource("label.mkdir", getResource("label.mkdir", "create new folder"));
		addMsgResource("label.parentDir", getResource("label.parentDir","parent folder"));
		addMsgResource("label.newDirName", getResource("label.newDirName","new folder name"));
		addMsgResource("alert.illegalCharInFilename", getResource("alert.illegalCharInFilename", "The new folder name contains illegal characters."));
		addMsgResource("button.create", getResource("button.create","Create"));
		addMsgResource("button.cancel", getResource("button.cancel","Cancel"));

		resp.setContentType("text/xml");
		
		BufferedWriter xmlOutFile = new BufferedWriter(output);
            
		XmlUtil.writeToStream(doc, xmlOutFile);

		output.flush();
	}
}