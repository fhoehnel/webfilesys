package de.webfilesys.gui.user.unix;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.WebFileSys;
import de.webfilesys.gui.user.UserRequestHandler;
import de.webfilesys.user.UserManager;

/**
 * @author Frank Hoehnel
 */
public class KillProcessRequestHandler extends UserRequestHandler
{
	public KillProcessRequestHandler(
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

		boolean allowProcessKill = WebFileSys.getInstance().isAllowProcessKill();
		
		UserManager userMgr = WebFileSys.getInstance().getUserMgr();

		if ((!allowProcessKill) ||
			(!userMgr.getDocumentRoot(uid).equals("/")))
		{
			return;
		}

		String pid=getParameter("pid");

		String prog_name_parms[];
		
		if (WebFileSys.getInstance().getOpSysType() == WebFileSys.OS_AIX)
		{
			prog_name_parms=new String[2];
			prog_name_parms[0]=new String("/bin/sh");
			prog_name_parms[1]=new String("kill -9 " + pid);

		}
		else
		{
			prog_name_parms=new String[3];
			prog_name_parms[0]=new String("/bin/sh");
			prog_name_parms[1]=new String("-c");
			prog_name_parms[2]=new String("kill -9 " + pid);
		}

		Runtime rt=Runtime.getRuntime();

		try
		{
			rt.exec(prog_name_parms);
		}
		catch (Exception e)
		{
			Logger.getLogger(getClass()).warn(e);
			return;
		}

		Logger.getLogger(getClass()).debug("killing process " + pid);

		output.print("<HTML>");
		output.print("<HEAD>");
		output.print("<META HTTP-EQUIV=\"REFRESH\" CONTENT=\"3; URL=/webfilesys/servlet?command=processList\">");
		output.print("</HEAD>"); 
		output.print("<body> trying to kill Process " + pid + " ... </body>"); 
		output.print("</HTML>");

		output.flush(); 
	}
}
