package de.webfilesys.gui.user;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.graphics.CameraExifData;
import de.webfilesys.util.MimeTypeMap;

/**
 * @author Frank Hoehnel
 */
public class ExifThumbRequestHandler extends UserRequestHandler
{
	protected HttpServletResponse resp = null;
	
	public ExifThumbRequestHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid)
	{
        super(req, resp, session, output, uid);
        
        this.resp = resp;
	}

	protected void process()
	{
		String imgFileName = getParameter("imgFile");

		if (!this.checkAccess(imgFileName))
		{
		    return;	
		}

		CameraExifData exifData = new CameraExifData(imgFileName);

		byte imgData[]=exifData.getThumbnailData();

		if (imgData==null)
		{
			return;
		}

		int docLength=exifData.getThumbnailLength();

		resp.setContentLength(docLength);
		
		String mimeType = MimeTypeMap.getInstance().getMimeType("*.jpg");
		
		resp.setContentType(mimeType);

		try
		{
			OutputStream byteOut = resp.getOutputStream();

			byteOut.write(imgData, 0, docLength);

			byteOut.flush();
		}
        catch (IOException ioEx)
        {
        	Logger.getLogger(getClass()).warn(ioEx);
        }
	}
}
