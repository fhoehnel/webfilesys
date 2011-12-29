package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.WebFileSys;
import de.webfilesys.gui.xsl.XslFileListHandler;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslSwitchReadonlyHandler extends XmlRequestHandlerBase
{
	public XslSwitchReadonlyHandler(
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
		
		String path = getParameter("filePath");

		if (!accessAllowed(path))
		{
			Logger.getLogger(getClass()).warn("user " + uid + " tried to access folder outside of his document root: " + path);
			
			return;
		}

		if (getParameter("readonly") != null)
		{
			File file = new File(path);

			if (file.canWrite())
			{
				file.setReadOnly();
			}
			else
			{
				String execString;

				if (WebFileSys.getInstance().is32bitWindows())
				{
					execString = "cmd /c attrib -R " + path;
				}
				else
				{
					execString = "attrib -R " + path;
				}

				Process attribProcess=null;

				try
				{
					attribProcess = Runtime.getRuntime().exec(execString);
				}
				catch (IOException rte)
				{
					Logger.getLogger(getClass()).error(rte);
				}

				try
				{
					attribProcess.waitFor();
				}
				catch (InterruptedException iex)
				{
					Logger.getLogger(getClass()).error(iex);
				}
			}
			
			setParameter("actpath", getCwd());

			setParameter("mask","*");

			(new XslFileListHandler(req, resp, session, output, uid, false)).handleRequest();

			return;
		}
		
        String headLinePath = this.getHeadlinePath(path);

		String shortPath = headLinePath;

		int pathLength = headLinePath.length();

		if (pathLength > 50)
		{
			shortPath = headLinePath.substring(0,15) + "..." + headLinePath.substring(pathLength - 31);
		}
		
		Element statusElement = doc.createElement("readWriteStatus");
			
		doc.appendChild(statusElement);

		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/switchReadWrite.xsl\"");

		doc.insertBefore(xslRef, statusElement);

		XmlUtil.setChildText(statusElement, "css", userMgr.getCSS(uid), false);
		XmlUtil.setChildText(statusElement, "path", path, false);
		XmlUtil.setChildText(statusElement, "shortPath", shortPath, false);
		
		addMsgResource("label.readWriteStatus", getResource("label.readWriteStatus", "Read/Write status"));
		addMsgResource("label.switchReadOnly", getResource("label.switchReadOnly", "Switch Read/Write"));
		addMsgResource("label.statusWritable", getResource("label.statusWritable", "writable"));
		addMsgResource("label.statusReadOnly", getResource("label.statusReadOnly","read-only"));
		addMsgResource("label.setro", getResource("label.setro","Set read-only"));
		addMsgResource("label.setrw", getResource("label.setrw","Set read-write"));
		addMsgResource("button.cancel", getResource("button.cancel","Cancel"));

		File statusFile = new File(path);
		
		if (!statusFile.canWrite())
		{
			XmlUtil.setChildText(statusElement, "readonly", "true", false);
		}
			
		processResponse();
    }
}