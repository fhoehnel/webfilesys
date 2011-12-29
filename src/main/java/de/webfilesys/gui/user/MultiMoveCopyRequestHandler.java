package de.webfilesys.gui.user;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.webfilesys.ClipBoard;
import de.webfilesys.util.UTF8URLEncoder;

/**
 * Not used anymore? Replaced by MultiFileCopyHandler?
 * @author Frank Hoehnel
 */
public class MultiMoveCopyRequestHandler extends MultiFileRequestHandler
{
	public MultiMoveCopyRequestHandler(
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

		output.println("<HTML>");
		output.println("<HEAD>");
		output.println("<TITLE>WebFileSys move/copy files</TITLE>");

		ClipBoard clipBoard = (ClipBoard) session.getAttribute("clipBoard");
		
		if (clipBoard == null)
		{
			clipBoard = new ClipBoard();
			
			session.setAttribute("clipBoard", clipBoard);
		}
		else
		{
			clipBoard.reset();
		}

		for (int i = 0; i < selectedFiles.size(); i++)
		{
			String sourceFilename=actPath + File.separator + selectedFiles.elementAt(i);
			clipBoard.addFile(sourceFilename);
		}

		output.println("<script language=\"javascript\">");

		if (cmd.equals("copy"))
		{
			clipBoard.setCopyOperation();
			output.println("alert('" + selectedFiles.size() + " " + getResource("alert.filescopied","files copied to clipboard") + "');");
		}

		if (cmd.equals("move"))
		{
			clipBoard.setMoveOperation();
			output.println("alert('" + selectedFiles.size() + " " + getResource("alert.filesmoved","files moved to clipboard") + "');");
		}

		output.println("</script>");

		output.println("<META HTTP-EQUIV=\"REFRESH\" CONTENT=\"0; URL=/webfilesys/servlet?command=listFiles&actpath=" + UTF8URLEncoder.encode(actPath) + "\">");
		output.println("</HEAD>");
		output.println("</html>");
		output.flush();
	}
}
