package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import de.webfilesys.FileLink;
import de.webfilesys.MetaInfManager;
import de.webfilesys.util.CommonUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

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
	  
	protected void process() {
		if (!checkWriteAccess()) {
			return;
		}

        String fileName = getParameter("fileName");
        boolean isLink = getParameter("isLink") != null;

        String filePath = null;
        if (isLink) {
            FileLink fileLink = MetaInfManager.getInstance().getLink(getCwd(), fileName);
            if (fileLink != null) {
                filePath = fileLink.getDestPath();
            }
        }
        if (filePath == null) {
            filePath = CommonUtils.joinFilesysPath(getCwd(), fileName);
        }

		if (getParameter("readonly") != null) {
			File file = new File(filePath);
			if (file.canWrite()) {
				file.setReadOnly();
			} else {
				String execString = "cmd /c attrib -R " + filePath;
				Process attribProcess=null;
				try {
					attribProcess = Runtime.getRuntime().exec(execString);
				} catch (IOException rte) {
					LogManager.getLogger(getClass()).error(rte);
				}
				try {
					attribProcess.waitFor();
				} catch (InterruptedException iex) {
					LogManager.getLogger(getClass()).error(iex);
				}
			}
			
			setParameter("actpath", getCwd());
			setParameter("mask","*");
			(new XslFileListHandler(req, resp, session, output, uid)).handleRequest();
			return;
		}
		
		Element statusElement = doc.createElement("readWriteStatus");
		doc.appendChild(statusElement);

		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/switchReadWrite.xsl\"");
		doc.insertBefore(xslRef, statusElement);

		XmlUtil.setChildText(statusElement, "css", userMgr.getCSS(uid));
		XmlUtil.setChildText(statusElement, "fileName", fileName);
		XmlUtil.setChildText(statusElement, "shortFileName", CommonUtils.shortName(fileName, 50));

        if (isLink) {
            XmlUtil.setChildText(statusElement, "isLink", "true");
        }

		addMsgResource("label.readWriteStatus", getResource("label.readWriteStatus", "Read/Write status"));
		addMsgResource("label.switchReadOnly", getResource("label.switchReadOnly", "Switch Read/Write"));
		addMsgResource("label.statusWritable", getResource("label.statusWritable", "writable"));
		addMsgResource("label.statusReadOnly", getResource("label.statusReadOnly","read-only"));
		addMsgResource("label.setro", getResource("label.setro","Set read-only"));
		addMsgResource("label.setrw", getResource("label.setrw","Set read-write"));
		addMsgResource("button.cancel", getResource("button.cancel","Cancel"));

		File statusFile = new File(filePath);
		
		if (!statusFile.canWrite()) {
			XmlUtil.setChildText(statusElement, "readonly", "true", false);
		}
		processResponse();
    }
}