package de.webfilesys.gui.user.windows;

import java.io.PrintWriter;
import java.text.DecimalFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.webfilesys.WinDiskUsage;
import de.webfilesys.WinDriveManager;
import de.webfilesys.gui.user.UserRequestHandler;

/**
 * @author Frank Hoehnel
 */
public class DriveInfoRequestHandler extends UserRequestHandler
{
	public DriveInfoRequestHandler(
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
		String path=getParameter("path");

		if (!checkAccess(path))
		{
			return;
		}

		output.println("<HTML>");
		output.println("<HEAD>");

		output.println("<TITLE>" + getResource("label.driveinfo","Drive Properties") + "</TITLE>");

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/css/" + userMgr.getCSS(uid) + ".css\">");

		output.println("</HEAD>"); 
		output.println("<BODY>");

		headLine(getResource("label.driveinfo","Drive Properties"));

		long diskFree=(new WinDiskUsage(path)).getFreeSpace();

		DecimalFormat numFormat=new DecimalFormat("#,###,###,###,###");

		int driveNum=0;

		if ((path.charAt(0) >= 'a') && (path.charAt(0) <= 'z'))
		{
			driveNum= (int) (path.charAt(0) - 'a' + 1);
		}
		else
		{
			driveNum= (int) (path.charAt(0) - 'A' + 1);
		}

		String driveLabel = WinDriveManager.getInstance().getDriveLabel(driveNum);

		output.println("<br>");
		output.println("<table border=\"0\" width=\"100%\">");
		output.println("<tr><td class=\"prompt\">");
		output.println(getResource("label.drive","Drive") + ":");
		output.println("</td><td class=\"value\">");
		output.println(path);
		output.println("</td></tr>");
		output.println("<tr><td class=\"prompt\">");
		output.println(getResource("label.drivelabel","Drive Label") + ":");
		output.println("</td><td class=\"value\">");
		output.println(driveLabel);
		output.println("</td></tr>");
		output.println("<tr><td class=\"prompt\">");
		output.println(getResource("label.bytesfree","Bytes free") + ":");
		output.println("</td><td class=\"value\">");
		output.println(numFormat.format(diskFree));
		output.println("</td></tr></table>");

		output.println("<br><center>");

		output.println("<form>");
		output.println("<input type=\"button\" value=\"" + getResource("button.closewin","Close Window") + "\" onclick=\"self.close()\">");
		output.println("</form>");

		output.println("</center></body></html>");
		output.flush();
	}
}
