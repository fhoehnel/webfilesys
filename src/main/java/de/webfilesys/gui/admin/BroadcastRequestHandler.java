package de.webfilesys.gui.admin;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Create an broadcast email.
 * @author Frank Hoehnel
 */
public class BroadcastRequestHandler extends AdminRequestHandler
{
	private String errorMsg = null;
	
    public BroadcastRequestHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid,
            String alert)
	{
        super(req, resp, session, output, uid);
        
        errorMsg = alert;
    }

    protected void process()
    {
        output.println("<html>");
        output.println("<head>");

		String title = "WebFileSys Administration: broadcast e-mail";
        
        output.println("<title>" + title + "</title>");

        output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/css/" + userMgr.getCSS(uid) + ".css\">");

        if (errorMsg != null)
        {
        	output.println("<script language=\"javascript\">");
        	output.println("alert('" + errorMsg + "');");
        	output.println("</script>");
        }

        output.println("</head>");
        output.println("<body>");

        headLine(title);

        output.println("<br>");

        output.println("<form accept-charset=\"utf-8\" method=\"post\" action=\"/webfilesys/servlet\">");

        output.println("<input type=\"hidden\" name=\"command\" value=\"admin\">");
        output.println("<input type=\"hidden\" name=\"cmd\" value=\"sendEmail\">");

        output.println("<table class=\"dataForm\" width=\"100%\">");

        output.println("<tr><td colspan=\"2\" class=\"formParm1\">&nbsp;</td></tr>");

        output.println("<tr>");
        output.println("<td class=\"formParm1\">users of this role will receive the e-mail:</td>");
        output.println("<td class=\"formParm2\">");
        output.println("<select name=\"role\" size=\"1\">");
        output.println("<option value=\"all\">all roles</option>");
        output.println("<option value=\"admin\">admin</option>");
        output.println("<option value=\"user\">user</option>");
        output.println("<option value=\"webspace\">webspace</option>");
        output.println("</select>");
        output.println("</td></tr>");

        output.println("<tr>");
        output.println("<td class=\"formParm1\">e-mail subject</td>");
        output.println("<td class=\"formParm2\">");
        output.println("<input type=\"text\" name=\"subject\" maxlength=\"80\" value=\"\" style=\"width:300;\">");
        output.println("</td>");
        output.println("</tr>");

        output.println("<tr>");
        output.println("<td class=\"formParm1\" valign=\"top\">e-mail text</td>");
        output.println("<td class=\"formParm2\">");
        output.println("<textarea name=\"content\" rows=\"6\" cols=\"40\" style=\"width:300px;\"></textarea>");
        output.println("</td>");
        output.println("</tr>");

        output.println("<tr><td colspan=\"2\" class=\"formParm1\">&nbsp;</td></tr>");

        output.println("<tr>");
        output.println("<td class=\"formButton\">");
        output.println("<input type=\"submit\" value=\"&nbsp;Send&nbsp;\">");
        output.println("</td>");
        output.println("<td class=\"formButton\" style=\"text-align:right\">");
        output.println("<input type=\"button\" value=\"&nbsp;Cancel&nbsp;\" onclick=\"javascript:window.location.href='/webfilesys/servlet?command=admin&cmd=menu'\">");
        output.println("</td>");
        output.println("</tr>");

        output.println("</table>");

        output.println("</form>");

        output.println("</body>");
        output.println("</html>");
        output.flush();
    }

}
