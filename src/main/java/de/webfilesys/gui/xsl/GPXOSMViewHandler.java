package de.webfilesys.gui.xsl;

import com.ctc.wstx.exc.WstxParsingException;
import de.webfilesys.WebFileSysConfig;
import de.webfilesys.gui.user.UserRequestHandler;
import de.webfilesys.util.CommonUtils;
import org.apache.logging.log4j.LogManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * GPS track file viewer for Open Street Maps.
 * 
 * @author Frank Hoehnel
 */
public class GPXOSMViewHandler extends UserRequestHandler {
	private static final String STYLESHEET_REF = "<?xml-stylesheet type=\"text/xsl\" href=\"/webfilesys/xsl/gpxOSMViewer.xsl\"?>";

	private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"yes\"?>";

	public GPXOSMViewHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid) {
        super(req, resp, session, output, uid);
	}	
	
	protected void process() {

        String fileName = getParameter("fileName");
        String filePath = CommonUtils.joinFilesysPath(getCwd(), fileName);
		
		String googleMapsAPIKey = null;
		if (req.getScheme().equalsIgnoreCase("https")) {
			googleMapsAPIKey = WebFileSysConfig.getInstance().getGoogleMapsAPIKeyHTTPS();
		} else {
			googleMapsAPIKey = WebFileSysConfig.getInstance().getGoogleMapsAPIKeyHTTP();
		}

		BufferedReader gpxReader = null;

		try {
			resp.setContentType("text/xml");

			gpxReader = new BufferedReader(new FileReader(filePath));

			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLStreamReader parser = factory.createXMLStreamReader(gpxReader);

			String tagName = null;

			boolean documentEnd = false;

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
						break;

					case XMLStreamConstants.START_ELEMENT:
						tagName = parser.getLocalName();

						if (tagName.equals("gpx")) {
							output.println(XML_HEADER);
							output.println(STYLESHEET_REF);

							output.println("<gpx>");

							if (!CommonUtils.isEmpty(googleMapsAPIKey)) {
								output.println("  <googleMapsAPIKey>" + googleMapsAPIKey + "</googleMapsAPIKey>");
							}
							output.println("  <filePath>" + CommonUtils.escapeForJavascript(filePath) + "</filePath>");
							
							output.println("  <language>" + language + "</language>");
						}

						if (tagName.equals("trk")) {
							output.println("<track>" + trackCounter + "</track>");
							trackCounter++;
						}

						break;

					case XMLStreamConstants.END_ELEMENT:

						tagName = parser.getLocalName();
						if (tagName.equals("gpx")) {
							output.println("</gpx>");
						}
						break;

					default:
						// System.out.println("unhandled event: " + event);
					}
				} catch (WstxParsingException epex) {
					LogManager.getLogger(getClass()).warn("GPX parsing error", epex);
				}
			}

			output.flush();
		} catch (IOException e) {
			LogManager.getLogger(getClass()).error("failed to read GPX file", e);
		} catch (XMLStreamException xmlEx) {
			LogManager.getLogger(getClass()).error("error parsing XML stream", xmlEx);
		} catch (Exception e) {
			LogManager.getLogger(getClass()).error("failed to transform GPX file", e);
		} finally {
			if (gpxReader != null) {
				try {
					gpxReader.close();
				} catch (Exception ex) {
				}
			}
		}
	}
}
