package de.webfilesys.gui.xsl;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslUnixCmdLineHandler extends XslRequestHandlerBase
{
	public XslUnixCmdLineHandler(
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
        if ((!isAdminUser(false)) || (!userMgr.getDocumentRoot(uid).equals("/")))
	    {
	        Logger.getLogger(getClass()).warn("UNIX command line is only available for admin users with root access");
	        return;
	    }
	    
		Element cmdLineElement = doc.createElement("cmdLine");
			
		doc.appendChild(cmdLineElement);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/unixCmdLine.xsl\"");

		doc.insertBefore(xslRef, cmdLineElement);

		XmlUtil.setChildText(cmdLineElement, "css", userMgr.getCSS(uid), false);
		XmlUtil.setChildText(cmdLineElement, "userid", uid, false);

		addMsgResource("label.cmdhead", getResource("label.cmdhead","execute OS command"));
        addMsgResource("label.cmdprompt", getResource("label.cmdprompt","operating system command"));
        addMsgResource("button.clearCmd", getResource("button.clearCmd", "Clear"));
		addMsgResource("button.run", getResource("button.run","Run"));
		addMsgResource("button.cancel", getResource("button.cancel","Cancel"));

		processResponse("unixCmdLine.xsl", false);
    }
}