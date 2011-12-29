package de.webfilesys.gui.user;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.webfilesys.Constants;
import de.webfilesys.MetaInfManager;
import de.webfilesys.gui.xsl.XslFileListHandler;
import de.webfilesys.gui.xsl.XslThumbnailHandler;
import de.webfilesys.gui.xsl.mobile.MobileFolderFileListHandler;

/**
 * @author Frank Hoehnel
 */
public class RenameLinkRequestHandler extends UserRequestHandler
{
	boolean clientIsLocal = false;
	
	public RenameLinkRequestHandler(
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
		if (!checkWriteAccess())
		{
			return;
		}

		String newLinkName=getParameter("newLinkName");

		if (newLinkName == null)
		{
			renameLinkForm();

			return;
		}

		String actPath = getCwd();

		if (!checkAccess(actPath))
		{
			return;
		}
		
		String oldLinkName = getParameter("linkName");
		
		MetaInfManager.getInstance().renameLink(actPath, oldLinkName, newLinkName);

        int viewMode = Constants.VIEW_MODE_LIST;		
		
	    Integer sessionViewMode = (Integer) session.getAttribute("viewMode");
	    
	    if (sessionViewMode != null)
	    {
	    	viewMode = sessionViewMode.intValue();
	    }
	    
        String mobile = (String) session.getAttribute("mobile");
        
        if (mobile != null) 
        {
            (new MobileFolderFileListHandler(req, resp, session, output, uid)).handleRequest(); 
        }
        else
        {
            if (viewMode == Constants.VIEW_MODE_THUMBS)
            {
                (new XslThumbnailHandler(req, resp, session, output, uid, clientIsLocal)).handleRequest(); 
            }
            else
            {
                (new XslFileListHandler(req, resp, session, output, uid, false)).handleRequest();
            }
        }	    
	}

	protected void renameLinkForm()
	{
		String oldLinkName=getParameter("linkName");

		output.println("<html><head>");

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/css/" + userMgr.getCSS(uid) + ".css\">");

		output.println("<script language=\"javascript\">"); 
		output.println("function validate()");
		output.println("{if (document.form1.newLinkName.value.length==0)");
		output.println("{alert('" + getResource("alert.newLinkNameEmpty","Enter the new link name!") + "');");
		output.println("document.form1.newLinkName.focus();");
		output.println("return;}");
		output.println("if (document.form1.newLinkName.value=='" + oldLinkName + "')");
		output.println("{alert('" + getResource("alert.destEqualsSource","new file name must be different") + "');}");
		output.println("else");
		output.println("{document.form1.submit();}}");
		output.println("</script>"); 

		output.println("</HEAD>"); 
		output.println("<BODY>");

		headLine(getResource("label.renameLinkHead","Rename Link"));

        output.println("<br/>");
		output.println("<form accept-charset=\"utf-8\" name=\"form1\" method=\"get\" action=\"/webfilesys/servlet\">");
		output.println("<input type=\"hidden\" name=\"command\" value=\"renameLink\">");
		output.println("<input type=\"hidden\" name=\"linkName\" value=\"" + oldLinkName +"\">");

		output.println("<table class=\"dataForm\" width=\"100%\">");

		output.println("<tr>");
		output.println("<td class=\"formParm1\">");
		output.println(getResource("label.oldName","old name") + ":");
		output.println("</td>");
		output.println("<td class=\"formParm2\">");
		output.println(oldLinkName);
		output.println("</td>");
		output.println("</tr>");

		output.println("<tr>");
		output.println("<td class=\"formParm1\">");
		output.println(getResource("label.newname","new name") + ":");
		output.println("</td>");
		output.println("<td class=\"formParm2\">");
		output.println("<input type=\"text\" name=\"newLinkName\" size=\"30\" maxlength=\"256\" style=\"width:200px;\" value=\"" + oldLinkName + "\">");
		output.println("</td>");
		output.println("</tr>");

		output.println("<tr><td colspan=\"2\">&nbsp;</td></tr>");

		output.println("<tr>");
		output.println("<td class=\"formButton\">");
		output.println("<input type=\"button\" value=\"" + getResource("button.rename","Rename") + "\" onclick=\"validate()\">");
		output.println("</td>");
		output.println("<td class=\"formButton\" style=\"text-align:right\">");
		output.println("<input type=\"button\" value=\"" + getResource("button.cancel","Cancel") + "\" onclick=\"window.location.href='/webfilesys/servlet?command=listFiles';\">");
		output.println("</td>");
		output.println("</tr>");

		output.println("</table>");

		output.println("</form>");

		output.println("<script language=\"javascript\">");
		output.println("document.form1.newLinkName.focus();");
		output.println("document.form1.newLinkName.select();");
		output.println("</script>");

		output.println("</body>");

		output.println("</html>");
		output.flush();
	}
}
