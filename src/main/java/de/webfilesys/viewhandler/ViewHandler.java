package de.webfilesys.viewhandler;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.webfilesys.ViewHandlerConfig;

public interface ViewHandler 
{
	/**
	 * Create the HTML response for viewing the given file.
	 * 
	 * @param filePath the filesystem path of the file to view
	 * @param req the servlet request
	 * @param resp the servlet response
	 */
    public void process(String filePath, ViewHandlerConfig viewHandlerConfig, HttpServletRequest req, HttpServletResponse resp);

    /**
     * Create the HTML response for viewing the given file contained in a ZIP archive..
     * 
     * @param fileName file name of the ZIP entry
     * @param zipIn the InputStream for the file extracted from a ZIP archive
     * @param req the servlet request
     * @param resp the servlet response
     */
    public abstract void processZipContent(String fileName, InputStream zipIn, ViewHandlerConfig viewHandlerConfig, HttpServletRequest req, HttpServletResponse resp);

    /**
     * Does this ViewHandler support reading the file from an input stream of a ZIP archive?
     * @return true if reading from ZIP archive is supported, otherwise false
     */
    public boolean supportsZipContent();
}
