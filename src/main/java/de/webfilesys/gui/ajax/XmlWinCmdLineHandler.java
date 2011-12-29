package de.webfilesys.gui.ajax;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import de.webfilesys.WebFileSys;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XmlWinCmdLineHandler extends XmlRequestHandlerBase
{
	private static final String CMD_LINE_BATCH_FILE = "winCmdLine.bat";
	
	protected boolean clientIsLocal = false;
	
	public XmlWinCmdLineHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid,
            boolean clientIsLocal)
	{
        super(req, resp, session, output, uid);
        
        this.clientIsLocal = clientIsLocal;
	}
	
	protected void process()
	{
		if (!clientIsLocal)
		{
			return;
		}
		
		if (!checkWriteAccess())
		{
			return;
		}

		String path = getParameter("path");

		if (!checkAccess(path))
		{
			return;
		}

		boolean success = false;
		
		String execString = null;

		if (WebFileSys.getInstance().is32bitWindows())
		{
			String cmdLineCmdFile = WebFileSys.getInstance().getConfigBaseDir() + "/" + CMD_LINE_BATCH_FILE;
			
			execString="cmd /k start " + cmdLineCmdFile + " " + path.substring(0,2) + " \"" + path + "\"";

			try
			{
				Runtime.getRuntime().exec(execString);
				
				success = true;
			}
			catch (IOException rte)
			{
				Logger.getLogger(getClass()).error(rte);
			}
		}
		
		Element resultElement = doc.createElement("result");
		
		XmlUtil.setChildText(resultElement, "success", Boolean.toString(success));
			
		doc.appendChild(resultElement);
		
		this.processResponse();
	}
}
