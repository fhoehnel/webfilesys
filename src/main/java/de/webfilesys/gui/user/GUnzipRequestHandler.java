package de.webfilesys.gui.user;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.zip.GZIPInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

/**
 * @author Frank Hoehnel
 */
public class GUnzipRequestHandler extends UserRequestHandler
{
	public GUnzipRequestHandler(
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
		String gzipFileName=getParameter("filename");

		output.println("<HTML>");
		output.println("<HEAD>");


		if (!gunzipFile(gzipFileName))
		{
			javascriptAlert(getResource("alert.compresserror","Error during unzip"));
		}

		output.println("<META HTTP-EQUIV=\"REFRESH\" CONTENT=\"0; URL=/webfilesys/servlet?command=listFiles\">");
		output.println("</HEAD>");
		output.println("</html>");
		output.flush();
	}
	
	protected boolean gunzipFile(String gzipFileName)
	{
		int extIdx=gzipFileName.lastIndexOf('.');

		String destFileName=gzipFileName.substring(0,extIdx);

		try
		{
			GZIPInputStream zipIn=new GZIPInputStream(new FileInputStream(gzipFileName));

			FileOutputStream destFile=new FileOutputStream(destFileName);

			byte buff[]=new byte[4096];

			int bytesRead=0;

			while ((bytesRead=zipIn.read(buff)) > (-1))
			{
				destFile.write(buff,0,bytesRead);
			}

			zipIn.close();

			destFile.close();
		}
		catch (IOException ioex)
		{
			System.out.println("gunzipFile: " + ioex);
			Logger.getLogger(getClass()).warn(ioex.toString());
			return(false);
		}

		return(true);
	}
}
