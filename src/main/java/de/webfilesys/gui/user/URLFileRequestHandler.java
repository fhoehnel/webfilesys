package de.webfilesys.gui.user;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

/**
 * @author Frank Hoehnel
 */
public class URLFileRequestHandler extends UserRequestHandler
{
	public URLFileRequestHandler(
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
		String actPath=getParameter("actPath");

		if (!checkAccess(actPath))
		{
			return;
		}

		String webLink=null;

		try
		{
			BufferedReader fin=new BufferedReader(new FileReader(actPath));

			String line=null;

			while ((webLink==null) && ((line=fin.readLine())!=null))
			{
				if (line.startsWith("URL="))
				{
					webLink=line.substring(4);
				}
			}

			fin.close();
		}
		catch (IOException ioex)
		{
			Logger.getLogger(getClass()).warn("openUrlFile: " + ioex);
		}

		output.println("<HTML>");
		output.println("<HEAD>");
		output.println("<TITLE> WebFileSys URL file </TITLE>");

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/css/" + userMgr.getCSS(uid) + ".css\">");

		if (webLink==null)
		{
			javascriptAlert("URL cannot be extracted from URL file\\n" + this.insertDoubleBackslash(actPath));

			output.println("<script language=\"javascript\">");
			output.println("self.close();");
			output.println("</script>");
		}
		else
		{
			output.println("<META HTTP-EQUIV=\"REFRESH\" CONTENT=\"0; URL=" + webLink + "\">");

			/*
			output.println("<script language=\"javascript\">");
			output.println("window.open('" + webLink + "','webLink" + System.currentTimeMillis() + "','');");
			output.println("setTimeout('self.close()',1000);");
			output.println("</script>");
			*/
		}

		output.println("</HEAD>");
		output.println("</body></html>");
		output.flush();
	}
}
