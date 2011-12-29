package de.webfilesys.gui.user;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.MetaInfManager;
import de.webfilesys.WebFileSys;
import de.webfilesys.util.CommonUtils;

/**
 * @author Frank Hoehnel
 */
public class MultiZipRequestHandler extends MultiFileRequestHandler
{
	public MultiZipRequestHandler(
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
		output.print("<TITLE>");
		output.print("WebFileSys: " + getResource("label.ziphead", "Create ZIP Archive"));
		output.println("</TITLE>");

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/css/" + userMgr.getCSS(uid) + ".css\">");

		output.println("</head>");
		output.println("<body>");

		headLine(getResource("label.ziphead","Create ZIP archive"));

		String pathWithSlash=null;

		if (actPath.endsWith(File.separator))
		{
			pathWithSlash=actPath;
		}
		else
		{
			pathWithSlash=actPath + File.separator;
		}

		String zip_dest=null;

		File zipDestFile=null;

		boolean destFileExists=true;

		for (int j=0;destFileExists;j++)
		{
			zip_dest=pathWithSlash + "SELECT_" + j + ".ZIP";

			zipDestFile=new File(zip_dest);

			if (!zipDestFile.exists())
			{
				destFileExists=false;
			}
		}

		ZipOutputStream zip_out=null;

		try
		{
			zip_out = new ZipOutputStream(new FileOutputStream(zipDestFile));
		}
		catch (IOException ioex)
		{
			System.out.println(ioex);
			output.println("<script language=\"javascript\">");
			output.println("alert('The destination file\\n" + insertDoubleBackslash(zip_dest) + "\\n cannot be opened!');");
			
			output.println("window.location.href='/webfilesys/servlet?command=listFiles';");
			output.println("</script>");

			output.println("</body>");
			output.println("</html>");
			output.flush();
			return;
		}

        output.println("<form accept-charset=\"utf-8\" name=\"form1\">");

        output.println("<table class=\"dataForm\" width=\"100%\">");
		output.println("<tr>");
		output.println("<td class=\"formParm1\">");
		output.println(getResource("label.ziparchive","ZIP archive") + ":");
		output.println("</td>");
        output.println("</tr>");
        output.println("<tr>");
		output.println("<td class=\"formParm2\">");
		output.println(CommonUtils.shortName(zip_dest, 60));
		output.println("</td>");
        output.println("</tr>");
		
        output.println("<tr><td>&nbsp;</td></tr>");

		output.println("<tr><td class=\"formParm1\">");
		output.println(getResource("label.currentzip","current file") + ":");
		output.println("</td></tr>");

        output.println("<tr><td class=\"formParm2\">");
        output.println("<div id=\"currentFile\" />");
        output.println("</td></tr>");
        
        output.println("<tr><td>&nbsp;</td></tr>");

        String returnUrl="/webfilesys/servlet?command=listFiles";

        output.println("<tr><td class=\"formButton\">");
        output.println("<input id=\"returnButton\" type=\"button\" value=\"" + getResource("button.return","Return") + "\" style=\"visibility:hidden\" onclick=\"window.location.href='" + returnUrl + "';\">");
        output.println("</td></tr>");
        output.println("</table>");
        output.println("</form>");

		for (int i = 0; i < selectedFiles.size(); i++)
		{
            FileInputStream f_in = null;

            try
			{
				zip_out.putNextEntry(new ZipEntry((String) selectedFiles.elementAt(i)));

				String fullPath=pathWithSlash + selectedFiles.elementAt(i);

				output.println("<script language=\"javascript\">");
                output.println("document.getElementById('currentFile').innerHTML=\"" + insertDoubleBackslash(CommonUtils.shortName(fullPath, 50)) + "\";");
				output.println("</script>");
				output.flush();

				f_in=new FileInputStream(fullPath);

				byte [] buff = new byte[4096];

				int count=0;

				while (( count = f_in.read(buff))>=0 )
				{
					zip_out.write(buff,0,count);
				}

			}
			catch (Exception zioe)
			{
                Logger.getLogger(getClass()).error(zioe);
				javascriptAlert(zioe.toString());
				return;
			}
			finally 
			{
			    try 
			    {
	                f_in.close();
			    }
                catch (Exception ex)
                {
                    Logger.getLogger(getClass()).error(ex);
                }
			}
		}

		try
		{
			zip_out.close();
		}
		catch (IOException io4)
		{
			Logger.getLogger(getClass()).error(io4);
		}

		for (int i = 0; i < selectedFiles.size(); i++)
		{
			File temp_file = new File(pathWithSlash + selectedFiles.elementAt(i));
			if (temp_file.delete()) 
			{
	            if (WebFileSys.getInstance().isReverseFileLinkingEnabled())
	            {
	                MetaInfManager.getInstance().updateLinksAfterMove(pathWithSlash + selectedFiles.elementAt(i), null, uid);
	            }
	            
	            MetaInfManager.getInstance().removeMetaInf(actPath, (String) selectedFiles.elementAt(i));
			}
			else
			{
			    Logger.getLogger(getClass()).error("failed to delete file " + temp_file.getAbsolutePath() + " after adding to ZIP archive");
			}
			
		}

        output.println("<script language=\"javascript\">");
        output.println("document.getElementById('returnButton').style.visibility = 'visible';");
        output.println("</script>");
		
		output.println("</body></html>");
		output.flush();
	}
}
