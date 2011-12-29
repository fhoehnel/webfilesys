package de.webfilesys.gui.xsl;

import java.io.BufferedWriter;
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
public class XslAddBookmarkPromptHandler extends XslRequestHandlerBase
{
	public XslAddBookmarkPromptHandler(
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

		String currentPath = getParameter("path");

		if ((currentPath == null) || (currentPath.trim().length() == 0))
		{
		    currentPath = getCwd();
		}
		
		if (!checkAccess(currentPath))
		{
			return;
		}

		Element addBookmarkElement = doc.createElement("addBookmark");
			
		doc.appendChild(addBookmarkElement);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/addBookmark.xsl\"");

		doc.insertBefore(xslRef, addBookmarkElement);

		XmlUtil.setChildText(addBookmarkElement, "currentPath", currentPath, false);

		XmlUtil.setChildText(addBookmarkElement, "currentPathShort", CommonUtils.shortName(getHeadlinePath(currentPath), 40), false);
		
		addMsgResource("label.addBookmark", getResource("label.addBookmark", "bookmark this folder"));
		addMsgResource("label.directory", getResource("label.directory","folder"));
		addMsgResource("label.bookmarkName", getResource("label.bookmarkName","bookmark name"));
		addMsgResource("alert.bookmarkMissingName", getResource("alert.bookmarkMissingName", "The bookmark name may not be empty!"));
		addMsgResource("button.create", getResource("button.create","Create"));
		addMsgResource("button.cancel", getResource("button.cancel","Cancel"));

		resp.setContentType("text/xml");
		
		BufferedWriter xmlOutFile = new BufferedWriter(output);
            
		XmlUtil.writeToStream(doc, xmlOutFile);

		output.flush();
	}
}