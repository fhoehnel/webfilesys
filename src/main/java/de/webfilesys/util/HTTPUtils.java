package de.webfilesys.util;

import java.util.Date;

/**
 * @author Frank Hoehnel
 */
public class HTTPUtils {

	public static final String RESPONSE_OK_HEADER = "HTTP/1.1 200 Document follows\r\n";

	public static String createHTMLHeader() {
		Date today = new Date();
		StringBuilder buff = new StringBuilder();
		buff.append(RESPONSE_OK_HEADER);
		buff.append("Date: " + today.toString() + "\r\n");
		buff.append("Content-type: text/html\r\n");
		buff.append("Connection: close\r\n");
		buff.append("\r\n");
		return(buff.toString());
	}

}
