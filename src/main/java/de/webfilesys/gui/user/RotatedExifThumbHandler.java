package de.webfilesys.gui.user;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.graphics.CameraExifData;
import de.webfilesys.graphics.RotateFilter;
import de.webfilesys.util.MimeTypeMap;

/**
 * @author Frank Hoehnel
 */
public class RotatedExifThumbHandler extends UserRequestHandler
{
	protected HttpServletResponse resp = null;
	
	public RotatedExifThumbHandler(
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

		byte imgData[] = exifData.getThumbnailData();

		if (imgData == null)
		{
            Logger.getLogger(getClass()).warn("missing EXIF data for picture " + imgFileName);
			return;
		}
        
		String mimeType = MimeTypeMap.getInstance().getMimeType("*.png");
		
		resp.setContentType(mimeType);
        
        int orientation = exifData.getOrientation();

        // System.out.println("img: " + imgFileName + " orientation: " + orientation);
        
        double degree;

        if ((orientation == 6)) 
        {
            degree = 270;
        }
        else if (orientation == 8)
        {
            degree = 90;
        }
        else 
        {
            degree = 0;
        }
        
        int newWidth = exifData.getThumbHeight();
        int newHeight = exifData.getThumbWidth();
        
        Image origImage = Toolkit.getDefaultToolkit().createImage(imgData);

        Canvas dummyComponent = new Canvas();

        MediaTracker tracker = new MediaTracker(dummyComponent);
        tracker.addImage (origImage,0);

        try
        {
            tracker.waitForAll();
        }
        catch(InterruptedException intEx1)
        {
            Logger.getLogger(getClass()).warn("rotateImage: " + intEx1);
        }
        
        tracker.removeImage(origImage);
        
        ImageFilter filter = new RotateFilter((Math.PI / 180) * degree);
        ImageProducer producer = new FilteredImageSource(origImage.getSource(), filter);
        Image rotatedImg = dummyComponent.createImage(producer);

        tracker.addImage (rotatedImg,1);

        try
        {
            tracker.waitForAll();
        }
        catch(InterruptedException intEx2)
        {
           Logger.getLogger(getClass()).error("rotateImage: " + intEx2);
        }

        tracker.removeImage(rotatedImg);

        origImage.flush();
        
        BufferedImage bufferedImg = null;

        Graphics g = null;

        try
        {
            bufferedImg = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);

            g = bufferedImg.getGraphics();

            g.drawImage(rotatedImg,0,0,null);

            g.dispose();

            rotatedImg.flush();

            byte[] pngBytes;
            com.keypoint.PngEncoder pngEncoder = new com.keypoint.PngEncoderB(bufferedImg);

            // pngEncoder.setCompressionLevel(1);
            // pngEncoder.setFilter(com.keypoint.PngEncoder.FILTER_LAST);

            OutputStream out = resp.getOutputStream();

            pngBytes = pngEncoder.pngEncode();
            
            long endTime = System.currentTimeMillis();
            
            if (pngBytes == null)
            {
                Logger.getLogger(getClass()).warn("PNG Encoder : Null image");
            }
            else
            {
                resp.setContentLength(pngBytes.length);
                
                out.write(pngBytes);
            }
            
            out.flush();
        }
        catch (IOException ioex1)
        {
            Logger.getLogger(getClass()).error("rotateImage: " + ioex1);
            return;
        }
        catch (OutOfMemoryError memErr)
        {
            Logger.getLogger(getClass()).error("not enough memory to complete image rotation");
        }
        finally
        {
            bufferedImg.flush();

            g.dispose();
        }
	}
}
