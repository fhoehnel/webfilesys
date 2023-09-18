package de.webfilesys.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/**
 * URL decoding with charset UTF-8.
 */
public class UTF8URLDecoder
{
    public static final String decode(String val) 
    {
        try
        {
            return URLDecoder.decode(val, "UTF-8");
        }
        catch (UnsupportedEncodingException uex)
        {
            LogManager.getLogger(UTF8URLDecoder.class).error(uex);
        }
        
        return null;
    }
}
