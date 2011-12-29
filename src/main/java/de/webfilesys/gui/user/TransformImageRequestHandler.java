package de.webfilesys.gui.user;

import java.io.File;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.webfilesys.graphics.ImageTransformation;
import de.webfilesys.util.CommonUtils;

/**
 * @author Frank Hoehnel
 */
public class TransformImageRequestHandler extends UserRequestHandler
{
	public TransformImageRequestHandler(
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
		String actPath = getCwd();

		Vector selectedFiles=new Vector();

		Enumeration allKeys = req.getParameterNames();

		while (allKeys.hasMoreElements())
		{
			String parmKey=(String) allKeys.nextElement();

			if (parmKey.startsWith("list-"))
			{
				selectedFiles.add(parmKey.substring(5));
			}
		}

		String pathWithSlash=actPath;

		if (!actPath.endsWith(File.separator))
		{
			pathWithSlash=actPath + File.separator;
		}

		String degrees=getParameter("degrees");

		String operation="";

		if (degrees.equals("90"))
		{
			operation=getResource("label.rotateright","rotate right");
		}
		else
		{
			operation=getResource("label.rotateleft","rotate left");
		}

		output.println("<html>");
		output.println("<head>");
		output.println("<title>WebFileSys Image Transformation</title>");

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/css/" + userMgr.getCSS(uid) + ".css\">");

		output.println("</head>");
		output.println("<body>");

		headLine(getResource("label.imgtransform","Transform Images"));

        output.println("<form accept-charset=\"utf-8\" name=\"form1\">");

		output.println("<table class=\"dataForm\" width=\"100%\" boder=\"0\">");
		output.println("<tr>");
		output.println("<td class=\"formParm1\">");
		output.println(getResource("label.directory","Directory") + ":");
		output.println("</td>");
		output.println("<td  colspan=\"2\" class=\"formParm2\">");
		output.println(CommonUtils.shortName(getHeadlinePath(actPath), 60));
		output.println("</td>");
		output.println("</tr>");

		output.println("<tr>");
		output.println("<td class=\"formParm1\">");
		output.println(getResource("label.operation","Operation") + ":");
		output.println("</td>");
		output.println("<td class=\"formParm2\">");
		output.println(operation);
		output.println("</td>");
		output.println("</tr>");

		output.println("<tr>");
		output.println("<td class=\"formParm1\">");
		output.println(getResource("label.currentfile","current file") + ": ");
		output.println("</td>");

		output.println("<td class=\"formParm2\" width=\"70%\">");
        output.println("<span id=\"currentFile\" />");
		output.println("</td>");
		output.println("</tr>");

		output.println("</table>");

        output.println("</form>");

		output.flush();

		for (int i=0;i<selectedFiles.size();i++)
		{
			String currentFile=(String) selectedFiles.elementAt(i);

			output.println("<script language=\"javascript\">");
			output.println("document.getElementById('currentFile').innerHTML='" + currentFile + "';");
			output.println("</script>");
			output.flush();

			ImageTransformation imgTrans=new ImageTransformation(pathWithSlash + currentFile,"rotate",degrees);

			imgTrans.execute(false);
		}

        output.println("<script language=\"javascript\">");
        output.println("window.location.href = '/webfilesys/servlet?command=listFiles&keepListStatus=true';");
        output.println("</script>");

		output.println("</body></html>");
		output.flush();
	}
}
