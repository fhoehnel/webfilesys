package de.webfilesys.gui.xsl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import de.webfilesys.WebFileSysConfig;
import de.webfilesys.util.XmlUtil;
import org.apache.logging.log4j.LogManager;


import com.ctc.wstx.exc.WstxParsingException;

import de.webfilesys.util.CommonUtils;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

/**
 * GPS track file viewer for Google maps.
 * 
 * @author Frank Hoehnel
 */
public class GPXViewHandler extends XslRequestHandlerBase {

	public GPXViewHandler(
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

        Element gpxElem = doc.createElement("gpx");
        doc.appendChild(gpxElem);

        ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/gpxViewer.xsl\"");
        doc.insertBefore(xslRef, gpxElem);

		BufferedReader gpxReader = null;

		try {
			gpxReader = new BufferedReader(new FileReader(filePath));

			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLStreamReader parser = factory.createXMLStreamReader(gpxReader);

			String tagName;

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
                            if (!CommonUtils.isEmpty(googleMapsAPIKey)) {
                                XmlUtil.setChildText(gpxElem, "googleMapsAPIKey", googleMapsAPIKey);
                            }
                            XmlUtil.setChildText(gpxElem, "filePath", CommonUtils.escapeForJavascript(filePath));
                            XmlUtil.setChildText(gpxElem, "language", language);
                        }
                        if (tagName.equals("trk")) {
                            Element trackElem = doc.createElement("track");
                            XmlUtil.setElementText(trackElem, Integer.toString(trackCounter));
                            gpxElem.appendChild(trackElem);
                            trackCounter++;
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
        processResponse("gpxViewer.xsl");
    }
}
