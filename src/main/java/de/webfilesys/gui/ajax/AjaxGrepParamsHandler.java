package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.PrintWriter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.w3c.dom.Element;

import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 *
 */
public class AjaxGrepParamsHandler extends XmlRequestHandlerBase
{
	public AjaxGrepParamsHandler(
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
        String fileName = req.getParameter("param1");

        String filePath = CommonUtils.joinFilesysPath(getCwd(), fileName);

        if (!checkAccess(filePath)) {
            return;
        }

        Element resultElement = doc.createElement("result");
        
        XmlUtil.setChildText(resultElement, "fileName", fileName);
        XmlUtil.setChildText(resultElement, "shortPath", CommonUtils.shortName(getHeadlinePath(filePath), 40));
        
        doc.appendChild(resultElement);

        addMsgResource("grepPrompt", getResource("grepPrompt", "filter lines containing"));
        addMsgResource("button.startGrep", getResource("button.startGrep", "grep"));
        addMsgResource("button.cancel", getResource("button.cancel", "Cancel"));
        
		this.processResponse();
	}
}
