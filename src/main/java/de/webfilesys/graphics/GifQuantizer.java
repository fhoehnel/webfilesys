package de.webfilesys.graphics;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.PixelGrabber;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;

import org.apache.log4j.Logger;

public class GifQuantizer
{
    public static BufferedImage process(BufferedImage sourceImg)
    {
        try
        {
        	int width = sourceImg.getWidth();
        	int height = sourceImg.getHeight();
        	
            NeuQuant quantizer = new NeuQuant(sourceImg, width, height);

            quantizer.init();

            int pixels[] = new int[width * height];
            
            // pixels = sourceImg.getData().getPixels(0,0,width,height,pixels);            

			PixelGrabber grabber = new PixelGrabber(sourceImg, 0, 0, width, height, pixels, 0, width);

			try
			{
				if (grabber.grabPixels() != true)
				{
					Logger.getLogger(GifQuantizer.class).error(
						"PixelGrabber failure: " + grabber.status());
				}
			}
			catch (InterruptedException intEx)
			{
				Logger.getLogger(GifQuantizer.class).error(intEx);
			}

            byte indices[] = quantizer.direct2index(pixels);

        	int colorTable[] = quantizer.getColorTable();

        	IndexColorModel icm = new IndexColorModel(8, colorTable.length, 
        	                                          colorTable, 0, false, -1, DataBuffer.TYPE_BYTE);

        	DataBufferByte buffer = new DataBufferByte(indices, indices.length);	

        	WritableRaster wraster ;
        	wraster = Raster.createPackedRaster(buffer, width, height, 8, null);

        	return(new BufferedImage(icm, wraster, false, null));
        } 
        catch (IOException ioEx)
        {
            Logger.getLogger(GifQuantizer.class).error(ioEx.toString());
            return(null);
        }
    }

}

