package de.webfilesys.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author Frank Hoehnel
 */
public class HTTPUtils
{
	static final public String HTTP_VERSION = "HTTP/1.1";

	public static final String RESPONSE_OK_HEADER = "HTTP/1.1 200 Document follows\r\n";

	public static final String BAD_REQUEST_HEADER = "HTTP/1.1 400 Bad Request\r\n";

	public static final int OK_CODE         = 0;
	public static final int OK_INCOMPLETE   = 1;  
	public static final int NOT_MODIFIED    = 2;
	public static final int BAD_REQUEST     = 3;
	public static final int NOT_FOUND       = 4;
	public static final int NOT_IMPLEMENTED = 5;
	public static final int INTERNAL_ERROR  = 6;

	private static final int responseCodes[] = 
	{
		200,   // OK_CODE
		202,   // OK_INCOMPLETE
		304,   // NOT_MODIFIED
		400,   // BAD_REQUEST
		404,   // NOT_FOUND
		501,   // NOT_IMPLEMENTED
		500    // INTERNAL_ERROR
	};

	static final private String responseMessages[] = 
	{
		"Document follows",        // OK_CODE
		"Document incomplete",     // OK_INCOMPLETE
		"Not Modified",            // NOT_MODIFIED
		"Bad Request",             // BAD_REQUEST
		"Not Found",               // NOT_FOUND
		"Not Implemented",         // NOT_IMPLEMENTED
		"Internal Server Error"    // INTERNAL_ERROR
	};

    public static String createHTTPHeader(int responseIdx,String mime_type,int length,
                                          boolean closeConnection)
    {
    	StringBuffer buff = new StringBuffer();
    	
		int code = responseCodes[responseIdx];
		String message = responseMessages[responseIdx];
		
		Date today = new Date();

		SimpleDateFormat httpDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z",Locale.US);
		httpDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

		buff.append(HTTP_VERSION + " " + code + " " + message + "\r\n");
		
		buff.append("Date: " + httpDateFormat.format(today) + "\r\n");
		buff.append("Content-type: " + mime_type + "\r\n");

		if (length>=0)
		{
		    buff.append("Content-length: " + length + "\r\n");
		}

        if (closeConnection)
        {
			buff.append("Connection: close\r\n");
        }

		buff.append("\r\n");
		
		return(buff.toString());
	}

	public static String createExpirationHeader(String mimeType, int docLength)
    {
		return(createExpirationHeader(mimeType, docLength, 36000000));
    }

    public static String createExpirationHeader(String mimeType, int docLength, long expMillis)
    {
		Date today = new Date();

		SimpleDateFormat httpDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z",Locale.US);
		httpDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

		StringBuffer buff = new StringBuffer();

		buff.append(RESPONSE_OK_HEADER);
		
		buff.append("Date: " + httpDateFormat.format(today) + "\r\n");
		
		buff.append("Content-type: " + mimeType + "\r\n");
		
		buff.append("Expires: " + httpDateFormat.format(new Date(System.currentTimeMillis() + expMillis)) + "\r\n");

		if (docLength>=0)
		{
			buff.append("Content-length: " + docLength + "\r\n");
		}

		// buff.append("Connection: close\r\n");

		buff.append("\r\n");

		return(buff.toString());
	}
	
	public static String createCookieHeader(String sessionId)
	{
		Date   today      = new Date();

		StringBuffer buff = new StringBuffer();

		buff.append(RESPONSE_OK_HEADER);

		buff.append("Date: " + today.toString() + "\r\n");

		buff.append("Content-type: text/html \r\n");

		buff.append("Set-Cookie: fmwebuid=" + sessionId + "; Path=/; Version=1\r\n");

		buff.append("Connection: close\r\n");

		buff.append("\r\n");

		return(buff.toString());
	}
	
	public static String createHTMLHeader()
	{
		Date today = new Date();

		StringBuffer buff = new StringBuffer();

		buff.append(RESPONSE_OK_HEADER);

		buff.append("Date: " + today.toString() + "\r\n");
		buff.append("Content-type: text/html\r\n");

		buff.append("Connection: close\r\n");

		buff.append("\r\n");

		return(buff.toString());
	}

	public static String createXMLHeader()
	{
		Date today = new Date();

		StringBuffer buff = new StringBuffer();

		buff.append(RESPONSE_OK_HEADER);

		buff.append("Date: " + today.toString() + "\r\n");
		buff.append("Content-type: text/xml\r\n");
		buff.append("Expires: 0");

		buff.append("Connection: close\r\n");

		buff.append("\r\n");

		return(buff.toString());
	}
	
}
