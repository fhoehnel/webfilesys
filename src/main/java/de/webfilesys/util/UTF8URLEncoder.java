package de.webfilesys.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/**
 * URL encoding with charset UTF-8.
 */
public class UTF8URLEncoder
{
    public static final String encode(String val) 
    {
        try
        {
            return URLEncoder.encode(val, "UTF-8").replaceAll("\\+","%20");
        }
        catch (UnsupportedEncodingException uex)
        {
            LogManager.getLogger(UTF8URLEncoder.class).error(uex);
        }
        
        return null;
    }
}
