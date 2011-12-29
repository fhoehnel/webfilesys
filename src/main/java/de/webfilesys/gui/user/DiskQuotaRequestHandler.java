package de.webfilesys.gui.user;

import java.io.PrintWriter;
import java.text.DecimalFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.webfilesys.FileSysStat;

/**
 * @author Frank Hoehnel
 */
public class DiskQuotaRequestHandler extends UserRequestHandler
{
	public DiskQuotaRequestHandler(
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
		String userid=getParameter("userid");

		if ((userid==null) || (userid.trim().length()==0))
		{
			userid=uid;
		}
		else
		{
			if (!isAdminUser(true))
			{
				return;
			}
		}

		output.println("<html><head>");
		output.println("<title>" + getResource("label.accountSize","Account Size Usage") +  "</title>");

		long userDiskQuota = userMgr.getDiskQuota(userid);

		if (userDiskQuota < 0)
		{
			javascriptAlert("No disk quota defined for user " + userid);

			output.println("</head></html>");
			output.flush();
			return;
		}

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/css/" + userMgr.getCSS(uid) + ".css\">");

		output.println("</head>");

		output.print("<BODY>");

		headLine(getResource("label.accountSize","Account Size Usage"));
		output.print("<br>");
		output.flush();

		DecimalFormat numFormat = new DecimalFormat("#,###,###,###,###");

		String docRoot = userMgr.getDocumentRoot(userid);

		FileSysStat fileSysStat=new FileSysStat(docRoot);

		fileSysStat.getStatistics();

		output.println("<table class=\"dataForm\" width=\"100%\">");
		
		if (userid!=uid)
		{
			output.println("<tr><td class=\"formParm1\">");
			output.println(getResource("label.user","user") + ": ");
			output.println("</td><td class=\"formParm2\" align=\"right\">");
			output.println(userid);
			output.println("</td></tr>");
		}

		output.println("<tr><td class=\"formParm1\">" + getResource("label.diskQuota","disk quota") + ": </td>");
		output.println("<td class=\"formParm2\" align=\"right\">" + numFormat.format(userDiskQuota / 1024) + " KB </td></tr>");

		output.println("<tr><td class=\"formParm1\">" + getResource("label.spaceUsed","space used") + ": </td>");
		output.println("<td class=\"formParm2\" align=\"right\">" + numFormat.format(fileSysStat.getTotalSizeSum() / 1024) + " KB </td></tr>");

		output.println("</table><br>");

		output.println("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");

		output.println("<tr>");

		if (fileSysStat.getTotalSizeSum() <= userDiskQuota)
		{
			long imgWidth=fileSysStat.getTotalSizeSum() * 300L / userDiskQuota;
			output.println("<td bgcolor=\"red\" style=\"border:solid 1px navy\">");
			output.print("<img src=\"/webfilesys/images/space.gif\" border=\"0\" height=\"10\" width=\"" + imgWidth + "\">");
			output.println("</td>");
			output.println("<td bgcolor=\"white\" style=\"border:solid 1px navy\">");
			output.print("<img src=\"/webfilesys/images/space.gif\" border=\"0\" height=\"10\" width=\"" + (300L - imgWidth) + "\">");
			output.println("</td>");
			output.println("<td class=\"plaintext\">&nbsp;" + (fileSysStat.getTotalSizeSum() * 100L / userDiskQuota) + " %</td>");
		}
		else
		{
			long imgWidth = userDiskQuota * 300L / fileSysStat.getTotalSizeSum();
			output.println("<td bgcolor=\"red\" style=\"border:solid 1px navy\">");
			output.print("<img src=\"/webfilesys/images/space.gif\" border=\"0\" height=\"10\" width=\"" + imgWidth + "\">");
			output.println("</td>");
			output.println("<td bgcolor=\"red\" style=\"border:solid 1px navy\">");
			output.print("<img src=\"/webfilesys/images/space.gif\" border=\"0\" height=\"10\" width=\"" + (300L - imgWidth) + "\">");
			output.println("</td>");
			output.println("<td class=\"plaintext\">&nbsp;" + (fileSysStat.getTotalSizeSum() * 100L / userDiskQuota) + " %</td>");
		}

		output.println("</tr>");

		output.println("</table>");

        output.println("<br/>");

        output.println("<form>");
		output.println("<input type=\"button\" value=\"" + getResource("button.closewin","Close Window") + "\" href=\"#\" onclick=\"javascript:self.close()\">");
		output.println("</form>");

		output.println("</body>");

		output.println("</html>");
		output.flush();
	}
}
