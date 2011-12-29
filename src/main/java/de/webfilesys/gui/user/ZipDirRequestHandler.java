package de.webfilesys.gui.user;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.UTF8URLEncoder;

/**
 * @author Frank Hoehnel
 */
public class ZipDirRequestHandler extends UserRequestHandler
{
	public ZipDirRequestHandler(
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

        String actPath=getParameter("actPath");

        if (isMobile()) 
        {
            actPath = getAbsolutePath(actPath);
        }
		
		if (!checkAccess(actPath))
		{
			return;
		}

		String dirName=actPath.substring(actPath.lastIndexOf(File.separatorChar)+1);

		output.println("<HTML>");
		output.println("<HEAD>");

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/css/" + userMgr.getCSS(uid) + ".css\">");

		output.println("</head>");
		output.println("<body>");

		headLine(getResource("label.zippingdir","compressing directory"));

		output.flush();

		String parentDir=actPath.substring(0,actPath.lastIndexOf(File.separatorChar));
		if (parentDir.equals(""))
		{
			parentDir=new String(File.separator);
		}
		else
		{
			if (parentDir.endsWith(":"))
			{
				parentDir=parentDir + File.separator;
			}
		}

		String pathWithSlash=null;

		if (parentDir.endsWith(File.separator))
		{
			pathWithSlash=parentDir;
		}
		else
		{
			pathWithSlash=parentDir + File.separator;
		}

		String zipDest=null;

		File zipDestFile=null;

		boolean destFileExists=true;

		for (int j=0;destFileExists;j++)
		{
			zipDest=pathWithSlash + dirName + "_" + j + ".ZIP";

			zipDestFile=new File(zipDest);

			if (!zipDestFile.exists())
			{
				destFileExists=false;
			}
		}

		String moveDestFileName=null;

		if (actPath.endsWith(File.separator))
		{
			moveDestFileName=actPath + dirName + "_.zip";
		}
		else
		{
			moveDestFileName=actPath + File.separator + dirName + "_.zip";
		}

        String relativePath = this.getHeadlinePath(moveDestFileName);

        output.println("<form accept-charset=\"utf-8\" name=\"form1\">");
        
        output.println("<table class=\"dataForm\" width=\"100%\">");
        
        output.println("<tr>");
        output.println("<td colspan=\"2\" class=\"formParm1\">");
        output.println(getResource("label.directory","folder") + ":");
        output.println("</td>");
        output.println("</tr>");

        output.println("<tr>");
        output.println("<td colspan=\"2\" class=\"formParm2\">");
        output.println(CommonUtils.shortName(getHeadlinePath(actPath), 50));
        output.println("</td>");
        output.println("</tr>");
        
        output.println("<tr>");
        output.println("<td colspan=\"2\" class=\"formParm1\">");
		output.println(getResource("label.ziparchive","ZIP archive") + ":");
		output.println("</td>");
		output.println("</tr>");
		
        output.println("<tr>");
		output.println("<td colspan=\"2\" class=\"formParm2\">");
		output.println(CommonUtils.shortName(relativePath, 50));
        output.println("</td>");
        output.println("</tr>");

        output.println("<tr>");
		output.println("<td colspan=\"2\" class=\"formParm1\">");
		output.println(getResource("label.currentfile","current file") + ":");
        output.println("</td>");
        output.println("</tr>");

        output.println("<tr>");
        output.println("<td colspan=\"2\" class=\"formParm2\">");
        output.println("<div id=\"currentDir\" />");
        output.println("</td>");
        output.println("</tr>");

        output.println("<tr>");
        output.println("<td colspan=\"2\" class=\"formParm1\">");
        output.println(getResource("label.zipCount","files zipped") + ":");
        output.println("</td>");
        output.println("</tr>");
        
        output.println("<tr>");
        output.println("<td colspan=\"2\" class=\"formParm2\">");
        output.println("<div id=\"compressCount\" />");
        output.println("</td>");
        output.println("</tr>");
        
        output.println("</table>");

        output.println("</form>");

		output.flush();

		ZipOutputStream zipOut=null;

		try
		{
			zipOut = new ZipOutputStream(new FileOutputStream(zipDestFile));
		}
		catch (IOException ioex)
		{
			Logger.getLogger(getClass()).error(ioex);
			
			output.println("<script language=\"javascript\">");
			output.println("alert('The destination file\\n" + insertDoubleBackslash(zipDest) + "\\n cannot be opened!');");

			if (isMobile()) 
			{
	            output.println("location.href='/webfilesys/servlet?command=mobile&cmd=folderFileList&absPath=" + UTF8URLEncoder.encode(actPath) + "';");
			}
			else
			{
	            output.println("location.href='/webfilesys/servlet?command=exp&expand=" + UTF8URLEncoder.encode(actPath) + "';");
			}

			output.println("</script>");

			output.println("</body>");
			output.println("</html>");
			output.flush();
			return;
		}

		treeFileSize=0l;

		int zipFileCount=zipTree(actPath,"",zipOut,0);

		try
		{
			zipOut.close();
		}
		catch (IOException io4)
		{
			System.out.println(io4);
		}

        output.println("<script language=\"javascript\">");
        output.println("document.getElementById('compressCount').innerHTML=\"" + zipFileCount + "\";");
        output.println("</script>");
		
        output.println("<br>");
        output.println("<form>");

        output.println("<table class=\"dataForm\" width=\"100%\">");

		if (zipFileCount < 1)
		{
			output.println("<tr><td colspan=\"2\" class=\"formParm2\">");
			output.println(getResource("label.zipnofile", "This directory tree contains no file to be zipped!"));
			output.println("</td></tr>");
		}
		else
		{
			long compressedSize=zipDestFile.length();

			if (treeFileSize > 0l)
			{
				long compressionRatio=(compressedSize*100) / treeFileSize;
				
				output.println("<tr><td class=\"formParm1\">");
				output.println(getResource("label.zipratio","compression ratio (% of original size)") + ":");
				output.println("</td>");

				output.println("<td class=\"formParm2\">");
				output.println(compressionRatio + "%"); 
				output.println("</td></tr>");
			}

			File moveDestFile=new File(moveDestFileName);

			if (!zipDestFile.renameTo(moveDestFile))
			{
				if (!copy_file(zipDest,moveDestFileName,false))
				{
					Logger.getLogger(getClass()).warn("cannot copy zip file " + zipDest + " to " + moveDestFileName);
				}
				else
				{
					if (!zipDestFile.delete())
					{
						Logger.getLogger(getClass()).warn("cannot delete temporary zip file " + zipDest);
					}
				}
			}
		}

		output.println("<tr><td colspan=\"2\">&nbsp;</td></tr>");

		output.println("<tr><td colspan=\"2\" class=\"formButton\" style=\"text-align:center;\">");
		
        String mobile = (String) session.getAttribute("mobile");
        
        if (mobile != null) 
        {
            output.println("<input type=\"button\" value=\"" + getResource("button.return","Return") + "\" onclick=\"window.location.href='/webfilesys/servlet?command=mobile&cmd=folderFileList&absPath=" + UTF8URLEncoder.encode(actPath) + "'\">");
        }
        else
        {
            output.println("<input type=\"button\" value=\"" + getResource("button.return","Return") + "\" onclick=\"window.location.href='/webfilesys/servlet?command=exp&expand=" + UTF8URLEncoder.encode(actPath) +"&fastPath=true'\">");
        }
		
		output.println("</td></tr>");

		output.println("</table>");

		output.println("</form>");

		output.println("</body></html>");
		output.flush();
	}
}
