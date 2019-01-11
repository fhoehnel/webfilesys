package de.webfilesys.gui.user.unix;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.ice.tar.TarEntry;
import com.ice.tar.TarOutputStream;

import de.webfilesys.gui.user.MultiFileRequestHandler;

/**
 * @author Frank Hoehnel
 */
public class MultiTarArchiveHandler extends MultiFileRequestHandler
{
	public MultiTarArchiveHandler(
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
		output.println("<TITLE>WebFileSys create tar archive</TITLE>");

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/common.css\">");
		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/skins/" + userMgr.getCSS(uid) + ".css\">");

		output.println("</head>");
		output.println("<body>");

		headLine(getResource("label.tarhead","create tar archive"));

        FileOutputStream tarStream = null;
        TarOutputStream tarFile = null;

        try 
		{
            File tarArchiveFile = new File(actPath, "SELECT_.tar");

	        tarStream = new FileOutputStream(tarArchiveFile);
	        
	        tarFile = new TarOutputStream(tarStream);

	        byte [] buff = new byte[4096];

	        for (String selectedFile : selectedFiles)
	        {
                output.println("adding " + selectedFile + "<br/>");

                File input = new File(actPath, selectedFile);

	            TarEntry tarEntry = new TarEntry(input);

	            tarEntry.setName(selectedFile);
	            
	            tarFile.putNextEntry(tarEntry);
	            
                FileInputStream fin = new FileInputStream(input);

                int count;
                
                while (( count = fin.read(buff)) >= 0 )
                {
                    tarFile.write(buff, 0, count);
                }

                fin.close();
	            
	            tarFile.closeEntry();
	        }

            tarFile.flush();
		}
		catch (Exception ex) 
		{
		    Logger.getLogger(getClass()).error("failed to create tar archive", ex);
            output.println("error creating tar archive");
		}
		finally
		{
		    if (tarFile != null) 
		    {
		        try
		        {
		            tarFile.close();
		        }
		        catch (Exception ex)
		        {
		        }
		    }
            if (tarStream != null) 
            {
                try
                {
                    tarStream.close();
                }
                catch (Exception ex)
                {
                }
            }
		}

        output.println("<br/>");
        output.println("<form>");
		output.println("<input type=\"button\" onclick=\"window.location.href='/webfilesys/servlet?command=listFiles'\" value=\"" + getResource("button.ok", "OK") + "\" />");
        output.println("</form>");
		
		output.print("</body>");

		output.println("</html>");
		output.flush();
	}
}
