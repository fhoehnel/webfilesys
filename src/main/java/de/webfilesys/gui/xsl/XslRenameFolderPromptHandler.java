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
public class XslRenameFolderPromptHandler extends XslRequestHandlerBase
{
	public XslRenameFolderPromptHandler(
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

		String currentName = CommonUtils.extractFileName(currentPath);
		
		Element renameFolderElement = doc.createElement("renameFolder");
			
		doc.appendChild(renameFolderElement);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/renameDir.xsl\"");

		doc.insertBefore(xslRef, renameFolderElement);

		XmlUtil.setChildText(renameFolderElement, "currentPath", currentPath, false);

		XmlUtil.setChildText(renameFolderElement, "currentName", currentName, false);

		XmlUtil.setChildText(renameFolderElement, "currentNameShort", CommonUtils.shortName(currentName, 24), false);
		
		addMsgResource("label.renamedir", getResource("label.renamedir", "rename folder"));
		addMsgResource("label.currentName", getResource("label.currentName","current name"));
		addMsgResource("label.newDirName", getResource("label.newDirName","new folder name"));
		addMsgResource("alert.illegalCharInFilename", getResource("alert.illegalCharInFilename", "The new folder name contains illegal characters."));
		addMsgResource("button.rename", getResource("button.rename","Rename"));
		addMsgResource("button.cancel", getResource("button.cancel","Cancel"));

		resp.setContentType("text/xml");
		
		BufferedWriter xmlOutFile = new BufferedWriter(output);
            
		XmlUtil.writeToStream(doc, xmlOutFile);

		output.flush();
	}
}