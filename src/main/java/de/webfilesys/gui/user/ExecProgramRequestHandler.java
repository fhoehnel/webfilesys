package de.webfilesys.gui.user;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.webfilesys.SystemCmdParms;
import de.webfilesys.SystemCommand;
import de.webfilesys.gui.xsl.XslFileListHandler;

/**
 * @author Frank Hoehnel
 */
public class ExecProgramRequestHandler extends UserRequestHandler
{
	private HttpServletRequest req = null; 
	private HttpServletResponse resp = null;
    private String uid = null;
	
	public ExecProgramRequestHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid)
	{
        super(req, resp, session, output, uid);
        
        this.req = req;
        this.resp = resp;
        this.uid = uid;
	}

	protected void process()
	{
		if (!this.isAdminUser(true))
		{
			return;
		}

		String progName=getParameter("progname");

		String file_ext=progName.substring(progName.lastIndexOf('.'),progName.length()).toUpperCase();

		if (file_ext.equals(".BAT") || file_ext.equals(".CMD"))
		{
			SystemCmdParms sysCmdWithParms=new SystemCmdParms("CMD.EXE","\"/C " + progName + "\"");
			sysCmdWithParms.start();
		}
		else
		{
			SystemCommand sysCmd=new SystemCommand(progName);
			sysCmd.start();
		}

		(new XslFileListHandler(req, resp, session, output, uid, false)).handleRequest();
	}
}
