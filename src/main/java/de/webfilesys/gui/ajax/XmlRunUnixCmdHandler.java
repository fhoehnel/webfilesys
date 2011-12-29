package de.webfilesys.gui.ajax;

import java.io.DataInputStream;
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
public class XmlRunUnixCmdHandler extends XmlRequestHandlerBase
{
	public XmlRunUnixCmdHandler(
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
		
        String cmdOutput = "";
        
        String unixCmd = req.getParameter("unixCmd");
        
        if (unixCmd != null)
        {
            unixCmd = unixCmd.trim();
            
            if (unixCmd.length() > 0)
            {
                cmdOutput = runUnixCmd(unixCmd);
            }
        }
        
		Element resultElement = doc.createElement("result");
		
		XmlUtil.setChildText(resultElement, "cmdOutput", cmdOutput);

		XmlUtil.setChildText(resultElement, "success", "true");
			
		doc.appendChild(resultElement);
		
		this.processResponse();
	}
	
	private String runUnixCmd(String unixCmd)
	{
	    StringBuffer buff = new StringBuffer();
	    
	    String cmdToExecute = unixCmd;
	    
        if (WebFileSys.getInstance().getOpSysType() == WebFileSys.OS_AIX)
        {
            cmdToExecute = unixCmd + " 2>&1";
        }
 
        Logger.getLogger(getClass()).debug("executing command : " + cmdToExecute);

        String cmdWithParms[];
        
        if (WebFileSys.getInstance().getOpSysType() == WebFileSys.OS_AIX)
        {
            cmdWithParms = new String[2];
            cmdWithParms[0] = "/bin/sh";
            cmdWithParms[1] = cmdToExecute;
        }
        else
        {
            cmdWithParms = new String[3];
            cmdWithParms[0] = "/bin/sh";
            cmdWithParms[1] = "-c";
            cmdWithParms[2] = cmdToExecute + " 2>&1";
        }

        Runtime rt = Runtime.getRuntime();

        Process cmdProcess = null;

        try
        {
            cmdProcess = rt.exec(cmdWithParms);
        }
        catch (Exception e)
        {
            Logger.getLogger(getClass()).error("failed to run OS command", e);
        }

        DataInputStream cmdOut = new DataInputStream(cmdProcess.getInputStream());
        String stdoutLine = null;

        boolean done = false;

        try
        {
            while (!done)
            {
                stdoutLine = cmdOut.readLine();

                if (stdoutLine == null)
                {
                    done = true;
                }
                else
                {
                    buff.append(stdoutLine);
                    buff.append('\n');
                }
            }
        }
        catch (IOException ioe)
        {
            Logger.getLogger(getClass()).error("failed to read OS command output", ioe);
        }
        
        return buff.toString();
	}
}
