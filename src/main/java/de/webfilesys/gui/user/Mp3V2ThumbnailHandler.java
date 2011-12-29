package de.webfilesys.gui.user;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.farng.mp3.MP3File;
import org.farng.mp3.TagException;
import org.farng.mp3.id3.AbstractID3v2;
import org.farng.mp3.id3.FrameBodyAPIC;
import org.farng.mp3.id3.ID3v2_2Frame;

/**
 * @author Frank Hoehnel
 */
public class Mp3V2ThumbnailHandler extends UserRequestHandler
{
	protected HttpServletResponse resp = null;
	
	public Mp3V2ThumbnailHandler(
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
		String mp3FilePath = getParameter("path");

		if (!checkAccess(mp3FilePath))
		{
		    return;	
		}

		byte[] pictureData = null;
		
		String mimeType = null;
		
        try
        {
            MP3File mp3File = new MP3File(mp3FilePath);
            
            if (!mp3File.hasID3v2Tag())
            {
                Logger.getLogger(getClass()).warn("no ID3V2 tag found in MP3 file " + mp3FilePath);
                return;
            }

            AbstractID3v2 id3v2 = mp3File.getID3v2Tag();

            Iterator iter = id3v2.getFrameIterator();
                
            boolean pictureFound = false;
            
            while ((!pictureFound) && iter.hasNext())
            {
                Object o = iter.next();
                if (o instanceof ID3v2_2Frame)
                {
                    ID3v2_2Frame frame = (ID3v2_2Frame) o;
                       
                    if (frame.getIdentifier().startsWith("APIC"))
                    {
                        FrameBodyAPIC frameBody = (FrameBodyAPIC) frame.getBody();
                         
                        Object data = frameBody.getObject("Picture Data");
                            
                        if (data instanceof byte[])
                        {
                            pictureData = (byte[]) data;
                        }
                        
                        mimeType = (String) frameBody.getObject("MIME Type");
                        
                        pictureFound = true;
                    }
                }
            }
        }
        catch (IOException nfex)
        {
            Logger.getLogger(getClass()).error("cannot read MP3 file: " + nfex);
            return;
        }
        catch (TagException tagEx)
        {
            Logger.getLogger(getClass()).error(tagEx);
            return;
        }
        
		resp.setContentLength(pictureData.length - 1);
		
		resp.setContentType(mimeType);

		try
		{
			OutputStream byteOut = resp.getOutputStream();

			byteOut.write(pictureData, 1, pictureData.length - 1);

			byteOut.flush();
		}
        catch (IOException ioEx)
        {
        	Logger.getLogger(getClass()).warn(ioEx);
        }
	}
}
