package de.webfilesys.graphics;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

public class ScaledImage
{
    public static final int IMG_TYPE_UNKNOWN = 0;
    public static final int IMG_TYPE_JPEG    = 1;
    public static final int IMG_TYPE_GIF     = 2;
    public static final int IMG_TYPE_PNG     = 3;
    public static final int IMG_TYPE_BMP     = 4;
    
    /** maximum size (width or height) of a thumbnail contained in the EXIF data */
    private static final int MAX_EXIF_THUMBNAIL_SIZE = 640;

    private int xDisplay;
    private int yDisplay;
    private int xSize;
    private int ySize;

    private int imageType = IMG_TYPE_UNKNOWN;

    public ScaledImage(String imgFileName, int maxWidth, int maxHeight) throws IOException
    {
        BufferedInputStream imgFile = null;

        try
        {
            imgFile = new BufferedInputStream(new FileInputStream(imgFileName));

            int last = 0;

            int byte1 = imgFile.read();
            int byte2 = imgFile.read();

            boolean done = false;

            if ((byte1 == 0xff) && (byte2 == 0xd8)) // it's a jpeg
            {
                imageType=IMG_TYPE_JPEG;

                ySize = 0;
                xSize = 0;
                int sofCounter = 0;

                while (!done)
                {
                    int ch = imgFile.read();

                    if (ch == (-1))
                    {
                        done = true;
                    }
                    else
                    {
                        if ((last == 0xff) && (ch == 0xc2) || (ch == 0xc0))
                        {
                            if (imgFile.read() == 0)
                            {
                                int ch2 = imgFile.read();
                             
                                // this byte is usually 0x11 in normal JPEG files
                                // but can be 0x0b in (old format?) black/white JPEG files
                                
                                if ((ch2 == 0x11) || (ch2 == 0x0b))
                                {
                                    if (imgFile.read() == 8)
                                    {
                                        int height=(imgFile.read()<<8) + imgFile.read();
                                        int width=(imgFile.read()<<8) + imgFile.read();
                                        
                                        if ((height>ySize) || (width>xSize))
                                        {
                                            ySize = height;
                                            xSize = width;
                                            
                                            if ((width > MAX_EXIF_THUMBNAIL_SIZE) || (height > MAX_EXIF_THUMBNAIL_SIZE))
                                            {
                                                done = true;
                                            }
                                              
                                            sofCounter++;
                                            
                                            if (sofCounter > 2)
                                            {
                                                done = true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        last = ch;
                    }
                }
            }
            else
            {
                int byte3 = imgFile.read();
                int byte4 = imgFile.read();
                int byte5 = imgFile.read();
                int byte6 = imgFile.read();

                if ((byte1==137) && (byte2==80) && (byte3==78) && (byte4==71) &&
                    (byte5==13) && (byte6==10) && (imgFile.read()==26) && (imgFile.read()==10))            // PNG
                {
                    imageType=IMG_TYPE_PNG;
                    
                    for (int i = 0; i < 8; i++)
                        imgFile.read();

                    xSize=(imgFile.read()<<24) + (imgFile.read()<<16) + (imgFile.read()<<8) + imgFile.read();               
                    ySize=(imgFile.read()<<24) + (imgFile.read()<<16) + (imgFile.read()<<8) + imgFile.read();               
                }
                else
                {
                    if ((byte1==0x42) && (byte2==0x4D))   // BMP
                    {
                        imageType=IMG_TYPE_BMP;

                        for (int k=7;k<19;k++)
                        {
                            imgFile.read();
                        }

                        int byte19=imgFile.read();
                        int byte20=imgFile.read();
                        imgFile.read();
                        imgFile.read();
                        
                        int byte23=imgFile.read();
                        int byte24=imgFile.read();

                        xSize=(byte20 << 8) + byte19;
                        ySize=(byte24 << 8) + byte23;
                        
                    }
                    else // GIF
                    {
                        imageType=IMG_TYPE_GIF;

                        xSize=imgFile.read() + (imgFile.read()<<8);
                        ySize=imgFile.read() + (imgFile.read()<<8);
                    }
                }
            }
        }
        catch (IOException ioex)
        {
            Logger.getLogger(getClass()).error("failed to determined image dimesions", ioex);
            throw ioex;
        }
        finally
        {
            if (imgFile != null)
            {
                try 
                {
                    imgFile.close();
                }
                catch (Exception ex) 
                {
                }
            }
        }
        
        if (ySize == 0)
        {
            Logger.getLogger(getClass()).warn("could not determine height of image " + imgFileName);
            ySize = 100;
        }

        if (xSize == 0)
        {
            Logger.getLogger(getClass()).warn("could not determine width of image " + imgFileName);
            xSize = 100;
        }

        if ((ySize <= maxHeight) && (xSize <= maxWidth)) 
        {
            // picture is smaller than available display size, nothing to calculate
            yDisplay = ySize;
            xDisplay = xSize;
            return;
        }
        
        int yscale = 100000;
        int xscale = 100000;

        if (ySize > maxHeight)
        {
            yscale = maxHeight * 100000 / ySize;
        }
        
        if (xSize > maxWidth)
        {
            xscale = maxWidth * 100000 / xSize;
        }

        if (yscale < xscale)
        {
            xDisplay = xSize * yscale / 100000 + 1;
            yDisplay = maxHeight;
        }
        else
        {
            xDisplay = maxWidth;
            yDisplay = ySize * xscale / 100000 + 1;
        }
    }

    public int getScaledWidth()
    {
        return(xDisplay);
    }

    public int getScaledHeight()
    {
        return(yDisplay);
    }

    public int getRealWidth()
    {
        return(xSize);
    }

    public int getRealHeight()
    {
        return(ySize);
    }

    public int getImageType()
    {
        return(imageType);
    }
}