package de.webfilesys.graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class ImageTextStamp
{
	public static final int TEXT_POS_UPPER_LEFT  = 1;
	public static final int TEXT_POS_UPPER_RIGHT = 2;
	public static final int TEXT_POS_LOWER_LEFT  = 3;
	public static final int TEXT_POS_LOWER_RIGHT = 4;
	
	public static void stampText(BufferedImage bufferedImg, String stampText, int fontSize, Color textColor, int fontStyle, int textPosition)
	{
        int imageWidth = bufferedImg.getWidth();
        
        int imageHeight = bufferedImg.getHeight();
        
        Graphics g = bufferedImg.getGraphics();

        g.setColor(textColor);
        
        Font stampFont = new Font("SansSerif", fontStyle, fontSize);
        
        g.setFont(stampFont);

        int stampXPos = 0;
        
        if ((textPosition == TEXT_POS_UPPER_LEFT) || (textPosition == TEXT_POS_LOWER_LEFT))
		{
        	stampXPos = 10;
		}
        else
        {
            FontMetrics stampFontMetrics = g.getFontMetrics(stampFont);
            
            int textWidth = stampFontMetrics.stringWidth(stampText);
            
            stampXPos = imageWidth - textWidth - 20;
            
            if (stampXPos < 10)
            {
            	stampXPos = 10;
            }
        }
        
        int stampYPos = 0;
        
        if ((textPosition == TEXT_POS_UPPER_LEFT) || (textPosition == TEXT_POS_UPPER_RIGHT))
        {
        	stampYPos = 10 + fontSize;
        }
        else
        {
            stampYPos = imageHeight - 10;
        }
        
        g.drawString(stampText, stampXPos, stampYPos);
	}
}