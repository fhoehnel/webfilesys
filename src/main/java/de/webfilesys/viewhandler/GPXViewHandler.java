package de.webfilesys.viewhandler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Logger;

import com.ctc.wstx.exc.WstxParsingException;

import de.webfilesys.ViewHandlerConfig;
import de.webfilesys.WebFileSys;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.UTF8URLEncoder;

/**
 * GPS track file viewer.
 * 
 * @author Frank Hoehnel
 */
public class GPXViewHandler implements ViewHandler {
	private static final String STYLESHEET_REF = "<?xml-stylesheet type=\"text/xsl\" href=\"/webfilesys/xsl/gpxViewer.xsl\"?>";

	private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"yes\"?>";

	public void process(String filePath, ViewHandlerConfig viewHandlerConfig, HttpServletRequest req,
			HttpServletResponse resp) {
		
		String trackNumber = req.getParameter("trackNumber");
		
		if (trackNumber != null) {
			new GPXTrackHandler().process(filePath, viewHandlerConfig, req, resp);
			return;
		}
		
		String googleMapsAPIKey = null;
		if (req.getScheme().equalsIgnoreCase("https")) {
			googleMapsAPIKey = WebFileSys.getInstance().getGoogleMapsAPIKeyHTTPS();
		} else {
			googleMapsAPIKey = WebFileSys.getInstance().getGoogleMapsAPIKeyHTTP();
		}

		try {
			resp.setContentType("text/xml");

			PrintWriter xmlOut = resp.getWriter();

			BufferedReader gpxReader = new BufferedReader(new FileReader(filePath));

			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLStreamReader parser = factory.createXMLStreamReader(gpxReader);

			String tagName = null;

			boolean documentEnd = false;

			String ignoreUnknownTag = null;
			
			int trackCounter = 0;

			while (!documentEnd) {
				try {
					int event = parser.next();

					switch (event) {
					case XMLStreamConstants.END_DOCUMENT:
						parser.close();
						documentEnd = true;
						break;

					case XMLStreamConstants.START_DOCUMENT:
						/*
						 * xmlOut.println(XML_HEADER);
						 * xmlOut.println(STYLESHEET_REF);
						 */
						break;

					case XMLStreamConstants.START_ELEMENT:

						if (ignoreUnknownTag != null) {
							break;
						}

						tagName = parser.getLocalName();

						if (tagName.equals("gpx")) {
							xmlOut.println(XML_HEADER);
							xmlOut.println(STYLESHEET_REF);
						}

						if (tagName.equals("gpx")) {
							xmlOut.println("<gpx>");

							if (!CommonUtils.isEmpty(googleMapsAPIKey)) {
								xmlOut.println("  <googleMapsAPIKey>" + googleMapsAPIKey + "</googleMapsAPIKey>");
							}
							xmlOut.println("  <filePath>" + filePath + "</filePath>");
						}

						if (tagName.equals("trk")) {
							xmlOut.println("<track>" + trackCounter + "</track>");
							trackCounter++;
						}

						break;

					case XMLStreamConstants.END_ELEMENT:

						tagName = parser.getLocalName();

						if (tagName.equals("gpx")) {
							xmlOut.println("</gpx>");
						}
						break;

					default:
						// System.out.println("unhandled event: " + event);
					}
				} catch (WstxParsingException epex) {
					Logger.getLogger(getClass()).warn("GPX parsing error", epex);
				}
			}

			xmlOut.flush();
			gpxReader.close();
		} catch (IOException e) {
			Logger.getLogger(getClass()).error("failed to read target file", e);
		} catch (XMLStreamException xmlEx) {
			Logger.getLogger(getClass()).error("error parsing XML stream", xmlEx);
		} catch (Exception e) {
			Logger.getLogger(getClass()).error("failed to transform GPX file", e);
		}
	}

	/**
	 * Create the HTML response for viewing the given file contained in a ZIP
	 * archive..
	 * 
	 * @param zipFilePath
	 *            path of the ZIP entry
	 * @param zipIn
	 *            the InputStream for the file extracted from a ZIP archive
	 * @param req
	 *            the servlet request
	 * @param resp
	 *            the servlet response
	 */
	public void processZipContent(String zipFilePath, InputStream zipIn, ViewHandlerConfig viewHandlerConfig,
			HttpServletRequest req, HttpServletResponse resp) {
		// not yet supported
		Logger.getLogger(getClass())
				.warn("reading from ZIP archive not supported by ViewHandler " + this.getClass().getName());
	}

	/**
	 * Does this ViewHandler support reading the file from an input stream of a
	 * ZIP archive?
	 * 
	 * @return true if reading from ZIP archive is supported, otherwise false
	 */
	public boolean supportsZipContent() {
		return false;
	}
}
