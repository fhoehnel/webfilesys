package de.webfilesys.gui.ajax;

import java.io.PrintWriter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.w3c.dom.Element;

import de.webfilesys.SystemEditor;
import de.webfilesys.util.XmlUtil;

public class XmlLocalEditorHandler extends XmlRequestHandlerBase {
	public XmlLocalEditorHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid) {
        super(req, resp, session, output, uid);
	}
	
	protected void process() {
		if (!checkWriteAccess()) {
			return;
		}

		String filePath = getRequestedFilePath();
		if (!checkAccess(filePath)) {
			return;
		}

		SystemEditor editor = new SystemEditor(filePath);
		editor.start();
		
		Element resultElement = doc.createElement("result");
		XmlUtil.setChildText(resultElement, "success", "true");
		doc.appendChild(resultElement);
		
		this.processResponse();
	}
}
