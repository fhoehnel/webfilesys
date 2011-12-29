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
import de.webfilesys.gui.xsl.XslUnixDirTreeHandler;
import de.webfilesys.util.CommonUtils;

/**
 * @author Frank Hoehnel
 */
public class UnixOwnerRequestHandler extends UserRequestHandler
{
    private boolean valuesProvided = false;

    public UnixOwnerRequestHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid,
            boolean valuesProvided)
    {
        super(req, resp, session, output, uid);

        this.valuesProvided = valuesProvided;
    }

    protected void process()
    {
        if (File.separatorChar != '/')
        {
            return;
        }

        if ((!checkWriteAccess()) || 
            (isWebspaceUser() && (!WebFileSys.getInstance().isChmodAllowed())))
        {
            return;
        }

        if (!valuesProvided)
        {
            ownerForm();

            return;
        }

        String new_owner = null;
        String new_group = null;
        char rwx[] = new char[4];
        String new_rwx = null;

        rwx[0] = '0';
        rwx[1] = '0';
        rwx[2] = '0';
        rwx[3] = '0';

        String isDirectory = getParameter("isDirectory");
        String cancelButton = getParameter("cancelbutton");
        String file_name = getParameter("filename");

        if (!checkAccess(file_name))
        {
            return;
        }

        if ((cancelButton == null) || (cancelButton.length() == 0))
        {
            new_owner = getParameter("owner");
            new_group = getParameter("group");

            String tmp = getParameter("rowner");
            if (tmp != null)
            {
                rwx[1] += 4;
            }
            tmp = getParameter("wowner");
            if (tmp != null)
            {
                rwx[1] += 2;
            }
            tmp = getParameter("xowner");
            if (tmp != null)
            {
                rwx[1] += 1;
            }

            tmp = getParameter("rgroup");
            if (tmp != null)
            {
                rwx[2] += 4;
            }
            tmp = getParameter("wgroup");
            if (tmp != null)
            {
                rwx[2] += 2;
            }
            tmp = getParameter("xgroup");
            if (tmp != null)
            {
                rwx[2] += 1;
            }

            tmp = getParameter("rall");
            if (tmp != null)
            {
                rwx[3] += 4;
            }
            tmp = getParameter("wall");
            if (tmp != null)
            {
                rwx[3] += 2;
            }
            tmp = getParameter("xall");
            if (tmp != null)
            {
                rwx[3] += 1;
            }

            tmp = getParameter("sowner");
            if (tmp != null)
            {
                rwx[0] += 4;
            }

            tmp = getParameter("sgroup");
            if (tmp != null)
            {
                rwx[0] += 2;
            }

            new_rwx = new String(rwx);
            // System.out.println("new owner : " + new_owner);
            // System.out.println("new group : " + new_group);
            // System.out.println("new rights : " + new_rwx);

            String prog_name_parms[];
            prog_name_parms = new String[3];
            prog_name_parms[0] = "/bin/sh";
            prog_name_parms[1] = "-c";
            prog_name_parms[2] = "chmod " + new_rwx + " \"" + file_name + "\" 2>&1";

            Runtime rt = Runtime.getRuntime();

            try
            {
                rt.exec(prog_name_parms);
            }
            catch (Exception e)
            {
                Logger.getLogger(getClass()).error(e);
            }

            if (!isWebspaceUser())
            {
                prog_name_parms[0] = new String("/bin/sh");
                prog_name_parms[1] = new String("-c");
                prog_name_parms[2] =
                    new String(
                        "chown "
                            + new_owner
                            + ":"
                            + new_group
                            + " "
                            + file_name
                            + " 2>&1");

                try
                {
                    rt.exec(prog_name_parms);
                }
                catch (Exception e)
                {
                    Logger.getLogger(getClass()).error(e);
                }
            }
        }

        if (isDirectory.equals("true"))
        {
            setParameter("actPath", file_name);

            setParameter("expand", file_name);
        	
			(new XslUnixDirTreeHandler(req, resp, session, output, uid, false)).handleRequest();

            return;
        }

        output.println("<HTML>");
        output.println("<HEAD>");

        output.println("<script language=\"javascript\">");
        output.println("self.close();");
        output.println("</script>");

        output.println("</HEAD>");
        output.println("</html>");
        output.flush();
    }

    protected void ownerForm()
    {
        String checkbox[];
        int i;

        String file_name = getParameter("actpath");

        if (!checkAccess(file_name))
        {
            return;
        }

        String isDirectory = getParameter("isDirectory");

        String owner = getParameter("owner");
        String group = getParameter("group");
        String rwx = getParameter("access");

        if ((owner == null) || (group == null) || (rwx == null))
        {
            StringTokenizer ls_tokener;
            String prog_name_parms[];
            String stdout_line = null;

            if (WebFileSys.getInstance().getOpSysType() == WebFileSys.OS_AIX)
            {
                prog_name_parms = new String[2];
                prog_name_parms[0] = "/bin/sh";
                prog_name_parms[1] = "ls -al \"" + file_name + "\"";
            }
            else // LINUX
                {
                prog_name_parms = new String[3];
                prog_name_parms[0] = "/bin/sh";
                prog_name_parms[1] = "-c";
                prog_name_parms[2] = "ls -al \"" + file_name + "\"";
            }

            Runtime rt = Runtime.getRuntime();
            Process opSysProcess = null;

            try
            {
                opSysProcess = rt.exec(prog_name_parms);
                
                DataInputStream processOut =
                    new DataInputStream(opSysProcess.getInputStream());

                boolean done = false;

                int lineCounter = 0;

                try
                {
                    while (!done)
                    {
                        stdout_line = processOut.readLine();

                        if (stdout_line == null)
                        {
                            done = true;
                        }
                        else
                        {
                            if (isDirectory.equalsIgnoreCase("true"))
                            {
                                if (lineCounter == 1)
                                {
                                    ls_tokener = new StringTokenizer(stdout_line);
                                    rwx = ls_tokener.nextToken();
                                    ls_tokener.nextToken();
                                    owner = ls_tokener.nextToken();
                                    group = ls_tokener.nextToken();

                                    done = true;
                                }
                            }
                            else
                            {
                                ls_tokener = new StringTokenizer(stdout_line);
                                rwx = ls_tokener.nextToken();
                                ls_tokener.nextToken();
                                owner = ls_tokener.nextToken();
                                group = ls_tokener.nextToken();
                            }
                        }

                        lineCounter++;
                    }
                }
                catch (IOException ioEx)
                {
                	Logger.getLogger(getClass()).error(ioEx);
                }
                
            }
            catch (Exception e)
            {
                Logger.getLogger(getClass()).error(e);
            }

        }

        output.print("<html>");
        output.print("<head>");
        output.print(
            "<title>"
                + getResource("label.accessrights", "owner and access rights")
                + "</title>");

        // output.println(CSSManager.getInstance().getCss(fmweb.userMgr.getCSS(uid)));
        output.println(
            "<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/css/" + userMgr.getCSS(uid) + ".css\">");

        output.println("</head><body>");

        headLine(getResource("label.accessrights", "owner and access rights"));

        output.println("<form accept-charset=\"utf-8\" method=\"post\" action=\"/webfilesys/servlet\">");
        output.println("<input type=\"hidden\" name=\"command\" value=\"setUnixRights\">");
        output.println(
            "<input type=\"hidden\" name=\"filename\" value=\""
                + file_name
                + "\">");
        output.println(
            "<input type=\"hidden\" name=\"isDirectory\" value=\""
                + isDirectory
                + "\">");
        
        output.println("<table class=\"dataForm\" width=\"100%\">");
        output.println("<tr><td colspan=\"2\" class=\"formParm1\">");
        if (isDirectory.equalsIgnoreCase("true"))
        {
            output.println(getResource("label.directory", "directory") + ":");
        }
        else
        {
            output.println(getResource("label.file", "file") + ":");
        }
        output.println("</td></tr>");
        output.println("<tr><td colspan=\"2\" class=\"formParm2\">");
        output.println(getHeadlinePath(CommonUtils.shortName(file_name, 60)));
        output.println("</td></tr>");

        checkbox = new String[12];

        for (i = 1; i <= 9; i++)
        {
            if (rwx.charAt(i) == '-')
                checkbox[i] = new String("");
            else
                checkbox[i] = new String("checked");
        }

        if (rwx.charAt(3) == 'S')
        {
            checkbox[3] = new String("");
            checkbox[10] = new String("checked");
        }
        else
        {
            if (rwx.charAt(3) == 's')
            {
                checkbox[10] = new String("checked");
            }
            else
                checkbox[10] = new String("");
        }

        if (rwx.charAt(6) == 'S')
        {
            checkbox[6] = new String("");
            checkbox[11] = new String("checked");
        }
        else
        {
            if (rwx.charAt(6) == 's')
            {
                checkbox[11] = new String("checked");
            }
            else
                checkbox[11] = new String("");
        }

        String disableOwner = "";
        
        if (isWebspaceUser())
        {
        	disableOwner = " readonly=\"readonly\"";
        }
        
        output.println("<tr><td colspan=\"2\" class=\"formParm2\">");
        output.print(
            "<INPUT TYPE=\"TEXT\" NAME=\"owner\" MAXLENGTH=60 style=\"width:120px\" VALUE=\"" 
        		+ owner 
                + "\""
		        + disableOwner
                + ">");
        output.println(" " + getResource("label.owner", "owner"));
        output.println("</td></tr>");
        
        output.println("<tr><td colspan=\"2\" class=\"formParm2\">");
        output.print(
            "<INPUT TYPE=\"TEXT\" NAME=\"group\" MAXLENGTH=60 style=\"width:120px\" VALUE=\""
                + group
                + "\""
		        + disableOwner
                + ">");
        output.println(" " + getResource("label.group", "group"));
        output.println("</td></tr>");

        // owner rights
        output.println("<tr><td colspan=\"2\" class=\"formParm2\">");
        output.println(
            "<INPUT TYPE=\"CHECKBOX\" NAME=\"rowner\""
                + checkbox[1]
                + "> owner can read");
        output.println("</td></tr>");

        output.println("<tr><td colspan=\"2\" colspan=\"2\" class=\"formParm2\">");
        output.print(
            "<INPUT TYPE=\"CHECKBOX\" NAME=\"wowner\""
                + checkbox[2]
                + "> owner can write");
        output.println("</td></tr>");

        output.println("<tr><td class=\"formParm2\">");
        output.print(
            "<INPUT TYPE=\"CHECKBOX\" NAME=\"xowner\""
                + checkbox[3]
                + "> owner can execute");
        output.println("</td>");
        output.println("<td class=\"formParm2\">");
        output.print(
            "<INPUT TYPE=\"CHECKBOX\" NAME=\"sowner\""
                + checkbox[10]
                + "> SUID bit");
        output.println("</td></tr>");

        output.println("<tr><td colspan=\"2\" style=\"border-top:1px solid #a0a0a0\">&nbsp;</td></tr>");

        // group rights
        output.println("<tr><td colspan=\"2\" class=\"formParm2\">");
        output.print(
            "<INPUT TYPE=\"CHECKBOX\" NAME=\"rgroup\""
                + checkbox[4]
                + "> group can read");
        output.println("</td></tr>");

        output.println("<tr><td colspan=\"2\" class=\"formParm2\">");
        output.print(
            "<INPUT TYPE=\"CHECKBOX\" NAME=\"wgroup\""
                + checkbox[5]
                + "> group can write");
        output.println("</td></tr>");

        output.println("<tr><td class=\"formParm2\">");
        output.print(
            "<INPUT TYPE=\"CHECKBOX\" NAME=\"xgroup\""
                + checkbox[6]
                + "> group can execute &nbsp;&nbsp;");
        output.println("</td>");
        output.println("<td class=\"formParm2\">");
        output.print(
            "<INPUT TYPE=\"CHECKBOX\" NAME=\"sgroup\""
                + checkbox[11]
                + "> SGID bit");
        output.println("</td></tr>");

        output.println("<tr><td colspan=\"2\" style=\"border-top:1px solid #a0a0a0\">&nbsp;</td></tr>");
        		   
        // everyone's rights
        output.println("<tr><td colspan=\"2\" class=\"formParm2\">");
        output.print(
            "<INPUT TYPE=\"CHECKBOX\" NAME=\"rall\""
                + checkbox[7]
                + "> everyone can read");
        output.println("</td></tr>");
        
        output.println("<tr><td colspan=\"2\" class=\"formParm2\">");
        output.print(
            "<INPUT TYPE=\"CHECKBOX\" NAME=\"wall\""
                + checkbox[8]
                + "> everyone can write");
        output.println("</td></tr>");

        output.println("<tr><td colspan=\"2\" class=\"formParm2\">");
        output.print(
            "<INPUT TYPE=\"CHECKBOX\" NAME=\"xall\""
                + checkbox[9]
                + "> everyone can execute");
        output.println("</td></tr>");

        output.println("<tr><td colspan=\"2\" style=\"border-top:1px solid #a0a0a0\">&nbsp;</td></tr>");

        output.println("<tr><td class=\"formButton\">");
        output.print(
            "<input type=\"submit\" name=\"changebutton\" value=\""
                + getResource("button.save", "Save")
                + "\">");
        output.println("</td>");

        output.println("<td class=\"formButton\" style=\"text-align:right\">");
        if (isDirectory.equalsIgnoreCase("true"))
        {
            output.print(
                "<input type=\"submit\" name=\"cancelbutton\" value=\""
                    + getResource("button.cancel", "Cancel")
                    + "\">");
        }
        else
        {
            output.print(
                "<input type=\"button\" name=\"cancelbutton\" value=\""
                    + getResource("button.cancel", "Cancel")
                    + "\" onclick=\"javascript:self.close()\">");
        }
        output.println("</td></tr>");
        output.println("</table>");

        output.print("</form>");

        output.println("</body></html>");
        output.flush();
    }

}
