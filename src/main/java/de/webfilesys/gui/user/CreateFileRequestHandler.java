package de.webfilesys.gui.user;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.util.UTF8URLEncoder;

/**
 * @author Frank Hoehnel
 */
public class CreateFileRequestHandler extends UserRequestHandler
{
	public CreateFileRequestHandler(
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
		
		String newFileName = getParameter("NewFileName");

		if ((newFileName == null) || (newFileName.trim().length()==0))
		{
			Logger.getLogger(getClass()).error("required parameter newFileName missing");
			
			return;
		}

		String actPath=getParameter("actpath");

		if (!checkAccess(actPath))
		{
			return;
		}

		output.println("<HTML>");
		output.println("<HEAD>");

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/css/" + userMgr.getCSS(uid) + ".css\">");

		String newFilePath = null;

		if (actPath.endsWith(File.separator))
		{
			newFilePath=actPath + newFileName;
		}
		else
		{
			newFilePath=actPath + File.separator + newFileName;
		}

		File fileToCreate=new File(newFilePath);

		if (fileToCreate.exists())
		{
			output.println("<script language=\"javascript\">");
			output.println("alert('" + getResource("alert.mkfileDuplicate", "A file with this name already exists") + ":\\n" + insertDoubleBackslash(newFilePath) + "');");
			output.println("</script>");
		}
		else
		{
			try
			{
				fileToCreate.createNewFile();
			}
			catch (IOException ioex)
			{
				output.println("<script language=\"javascript\">");
				output.println("alert('" + getResource("alert.mkfileFail", "The file could not be created") + ":\\n" + insertDoubleBackslash(newFilePath) + "');");
				output.println("</script>");
			}
		}

		String mobile = (String) session.getAttribute("mobile");
		
		if (mobile != null) 
		{
            output.println("<META HTTP-EQUIV=\"REFRESH\" CONTENT=\"0; URL=/webfilesys/servlet?command=mobile&cmd=folderFileList&absPath=" + UTF8URLEncoder.encode(actPath) + "\">");
		}
		else
		{
	        output.println("<META HTTP-EQUIV=\"REFRESH\" CONTENT=\"0; URL=/webfilesys/servlet?command=exp&expand=" + UTF8URLEncoder.encode(actPath) + "&fastPath=true\">");
		}

		output.println("</head></html>");
		output.flush();
	}

}
