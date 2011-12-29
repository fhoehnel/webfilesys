package de.webfilesys.gui.user.unix;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.WebFileSys;
import de.webfilesys.gui.user.UserRequestHandler;
import de.webfilesys.unix.LinuxProcessTree;
import de.webfilesys.unix.OldLinuxProcessTree;
import de.webfilesys.unix.ProcessTree;
import de.webfilesys.unix.SolarisProcessTree;
import de.webfilesys.user.UserManager;

/**
 * @author Frank Hoehnel
 */
public class ProcessListRequestHandler extends UserRequestHandler
{
	public ProcessListRequestHandler(
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
		if (File.separatorChar != '/')
		{
			return;
		}
	
	    if (this.isWebspaceUser())
	    {
	    	return;
	    }
		
		output.println("<HTML>");
		output.println("<HEAD>");
		
        output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/css/" + userMgr.getCSS(uid) + ".css\">");
		
		output.println("<TITLE>WebFileSys Process List</TITLE>");
		output.println("<SCRIPT LANGUAGE=\"JavaScript\">");

		output.println("function confirmKill(pid)");
		output.println("{if (confirm(\"Are you sure you want to stop this process (\" + pid + \") ?\"))");
		output.println("    {window.location='/webfilesys/servlet?command=killProcess&pid=' + pid;}");
		output.println("}");
		output.println("</SCRIPT>");

		output.print("</HEAD>"); 
		output.println("<BODY bgcolor=white>");

		boolean allowProcessKill = WebFileSys.getInstance().isAllowProcessKill();
		
		UserManager userMgr = WebFileSys.getInstance().getUserMgr();
		
		if (WebFileSys.getInstance().getOpSysType() == WebFileSys.OS_SOLARIS)
		{
			ProcessTree pTree = new SolarisProcessTree("all");

			output.print(pTree.toHTML(allowProcessKill && userMgr.getDocumentRoot(uid).equals("/")));

			output.println("</body></html>");
			output.flush();
			return;
		}

		if (WebFileSys.getInstance().getOpSysType() == WebFileSys.OS_LINUX)
		{
			ProcessTree pTree=null;

			if (WebFileSys.getInstance().isOldLinuxPsStyle())
			{
				pTree = new OldLinuxProcessTree("all");
			}
			else
			{
				pTree = new LinuxProcessTree("all");
			}

			output.print(pTree.toHTML(allowProcessKill && userMgr.getDocumentRoot(uid).equals("/")));

			output.println("</body></html>");
			output.flush();
			return;
		}

		String prog_name_parms[];
		
		if (WebFileSys.getInstance().getOpSysType() == WebFileSys.OS_AIX)
		{
			prog_name_parms=new String[2];
			prog_name_parms[0]=new String("/bin/sh");
			prog_name_parms[1]=new String("ps -ef");

		}
		else
		{
			prog_name_parms=new String[3];
			prog_name_parms[0]=new String("/bin/sh");
			prog_name_parms[1]=new String("-c");
			prog_name_parms[2]=new String("ps -ef" + " 2>&1");
		}

		Runtime rt=Runtime.getRuntime();

		Process dsmc_process=null;

		try
		{
			dsmc_process=rt.exec(prog_name_parms);
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}

		DataInputStream cmdOut=new DataInputStream(dsmc_process.getInputStream());
		String stdoutLine = null;

		StringTokenizer processListParser=null;

		String pidString=null;
		boolean done=false;
		boolean firstLine=true;
		String userid=null;
		String cpuTime;
		String tty;
		String startTime;
		String ppid;

		output.println("<table class=\"processList\" width=\"100%\">");

		while (!done)
		{
			try
			{
				stdoutLine = cmdOut.readLine();
			}
			catch (IOException ioe)
			{
				Logger.getLogger(getClass()).error(ioe);
			}
			if (stdoutLine==null)
			{
				done=true;
			}
			else
			{
				processListParser = new StringTokenizer(stdoutLine);

				if (firstLine)
				{
					output.println("<tr>");
                    if (allowProcessKill && userMgr.getDocumentRoot(uid).equals("/")) 
                    {
                        output.println("<th class=\"processList\">op</th>");
                    }

					while (processListParser.hasMoreTokens())
					{
                        output.print("<th class=\"processList\"> " + processListParser.nextToken() + "</th>");
					}

                    output.println("</tr>");

                    firstLine=false;
				}
				else
				{
					processListParser=new StringTokenizer(stdoutLine);

					pidString=null;
					if (processListParser.hasMoreTokens())
					{
						userid=processListParser.nextToken();
						if (processListParser.hasMoreTokens())
						{
							pidString=processListParser.nextToken();
						}
					}

					int pid= (-1);

					try
					{
						pid=Integer.parseInt(pidString);
					}
					catch (NumberFormatException nfe)
					{
					}

					if (pid >= 0)
					{
						if (userid.equals("root"))
							output.print("<tr class=\"processRowRoot\">");
						else
							output.print("<tr class=\"processRowOtherUser\">");

						if (allowProcessKill && userMgr.getDocumentRoot(uid).equals("/"))
						{
							output.print("<td class=\"processKill\" ><a href=\"javascript:confirmKill('" + pid + "')\"><IMG ALIGN=\"center\" BORDER=\"0\" src=\"/webfilesys/images/redx2.gif\" alt=\"kill process\"></a></td>");
						}

						output.println("<td class=\"processUID\">" + userid + "</td>");
						output.println("<td class=\"processPID\"> " + pid + "</td>");
						ppid = processListParser.nextToken();
						String c = processListParser.nextToken();
						startTime = processListParser.nextToken();
						if (startTime.length()==3)
						{
                            startTime=new String(startTime + " " + processListParser.nextToken());
						}
						output.println("<td class=\"processPID\">" + ppid + "</td>");
						output.println("<td class=\"processStartTime\">" + c + "</td>");
						output.println("<td class=\"processStartTime\"> " + startTime + "</td>");

						tty = processListParser.nextToken();
						cpuTime = processListParser.nextToken();
						output.println("<td class=\"processTTY\">" + tty + "</td>");
						output.println("<td class=\"processCPUTime\"> " + cpuTime + "</td>");

						String cmdStr = "";

						while (processListParser.hasMoreTokens())
						{
							cmdStr = cmdStr + " " + processListParser.nextToken();
						}

						output.print("<td class=\"processCMD\"> " + cmdStr + "</td>");
					}
				}
				output.println("</tr>"); 
				output.flush();
			}
		}     

		output.println("</table>");

		output.print("</body>");
		output.println("</html>");
		output.flush();
	}
}
