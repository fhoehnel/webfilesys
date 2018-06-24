package de.webfilesys.gui.user;

import java.io.File;
import java.io.PrintWriter;
import java.text.DecimalFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.CopyStatus;
import de.webfilesys.FileSysStat;
import de.webfilesys.util.UTF8URLEncoder;

/**
 * @author Frank Hoehnel
 */
public class CloneFolderRequestHandler extends UserRequestHandler
{
	protected HttpServletRequest req = null;

	protected HttpServletResponse resp = null;

	public CloneFolderRequestHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid)
	{
        super(req, resp, session, output, uid);
        
        this.req = req;
        
        this.resp = resp;
	}

	protected void process()
	{
		if (!checkWriteAccess())
		{
			return;
		}

		String newFolderName = getParameter("newFolderName");

		String sourceFolderPath = getParameter("sourceFolderPath");

		File sourceFolderFile = new File(sourceFolderPath);
		
		if ((!sourceFolderFile.exists()) || (!sourceFolderFile.canRead())) 
		{
			Logger.getLogger(getClass()).error("clone source folder is not a readable folder: " + sourceFolderPath);
			return;
		}

		File parentFolder = sourceFolderFile.getParentFile();
		
		if (parentFolder == null)
		{
			Logger.getLogger(getClass()).error("could not determine clone parent folder");
			return;
		}

		if (!checkAccess(parentFolder.getAbsolutePath()))
		{
			return;
		}
		
		String path = parentFolder.getAbsolutePath();

		String newFolderPath = null;

		if (path.endsWith(File.separator))
		{
			newFolderPath = path + newFolderName;     
		}
		else
		{
			newFolderPath = path + File.separator + newFolderName;
		}

		output.println("<html>");
		output.println("<head>");

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/common.css\">");
		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/skins/" + userMgr.getCSS(uid) + ".css\">");

		output.println("</head>");

		output.println("<body>");

	    headLine(getResource("label.cloneFolderTitle","Cloning folder"));

		output.println("<br>");

		output.println("<form accept-charset=\"utf-8\" name=\"form1\">");

		output.println("<table class=\"dataForm\" width=\"100%\">");

		output.println("<tr>");
		output.println("<td class=\"formParm1\">");
		output.println(getResource("label.currentcopy","current file") + ":");
		output.println("</td>");
		output.println("</tr>");
		
		output.println("<tr>");
		output.println("<td class=\"formParm2\">");
		output.println("<div id=\"currentFile\" />");
		output.println("</td>");
		output.println("</tr>");
		
		output.println("<tr>");
		output.println("<td class=\"formParm1\">");
		output.println(getResource("label.copyresult","files copied") + ":");
		output.println("</td>");
		output.println("</tr>");

		output.println("<tr>");
		output.println("<td class=\"formParm2\">");
		output.println("<div id=\"fileCount\" />");
		output.println("</td>");
		output.println("</tr>");

		output.println("<tr>");
		output.println("<td class=\"formParm2\">");
		output.println("<div class=\"progressBar\">");
		output.println("<img id=\"copyProgressBar\" src=\"/webfilesys/images/bluedot.gif\" style=\"width:1px\" />");
		output.println("</div>");
		output.println("</td>");
		output.println("</tr>");
		
		output.println("<tr>");
		output.println("<td class=\"formParm1\">");
		output.println(getResource("bytesCopied", "bytes copied") + ":");
		output.println("</td>");
		output.println("</tr>");

		output.println("<tr>");
		output.println("<td class=\"formParm2\">");
		output.println("<div id=\"bytesCopied\" />");
		output.println("</td>");
		output.println("</tr>");
		
		output.println("</table>");

		output.println("</form>");

		output.flush();
		
		String errorMsg = null;
		
		File destFile = new File(newFolderPath);

		if (destFile.exists()) 
		{
		    errorMsg = getResource("alert.cloneTargetFolderExists", "Failed to clone folder, target folder already exists!");		    
		}
		else
		{
			if (!destFile.mkdir()) 
			{
                errorMsg = getResource("alert.cloneFolderFailed", "Failed to clone folder.");
			}
			else 
			{
				DecimalFormat numFormat = new DecimalFormat("#,###,###,###,###");
				
		        FileSysStat fileSysStat = new FileSysStat(sourceFolderPath);
		        fileSysStat.getStatistics();        
				
				CopyStatus copyStatus = new CopyStatus();
				copyStatus.setTreeFileSize(fileSysStat.getTotalSizeSum());
				copyStatus.setTreeFileNum(fileSysStat.getTotalFileNum());
				
				if (!copyFolderTreeWithStatus(sourceFolderPath, newFolderPath, false, copyStatus, numFormat))
			    {
	                errorMsg = getResource("alert.cloneFolderFailed", "Failed to clone folder.");
			    }
			}
		}
		
		output.println("<script type=\"text/javascript\">");

		if (errorMsg != null)
		{
			output.println("alert('" + errorMsg + "');");
			
        }

	    output.println("window.location.href='/webfilesys/servlet?command=exp&expandPath=" + UTF8URLEncoder.encode(path) + "';");
		output.println("</script>");
		output.println("</body>");
		output.println("</html>");
		output.flush();
	}
}
