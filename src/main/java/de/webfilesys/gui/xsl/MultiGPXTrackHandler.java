package de.webfilesys.gui.xsl;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.WebFileSys;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.UTF8URLDecoder;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class MultiGPXTrackHandler extends XslRequestHandlerBase {
	private static final Logger LOG = Logger.getLogger(MultiGPXTrackHandler.class);

	public MultiGPXTrackHandler(HttpServletRequest req, HttpServletResponse resp, HttpSession session,
			PrintWriter output, String uid) {
		super(req, resp, session, output, uid);
	}

	protected void process() {
		String currentPath = getCwd();

		ArrayList<String> selectedFiles = new ArrayList<String>();

		// Enumeration allKeys=requestParms.keys();

		Enumeration allKeys = req.getParameterNames();

		while (allKeys.hasMoreElements()) {
			String paramKey = (String) allKeys.nextElement();

			if ((!paramKey.equals("cb-setAll")) && (!paramKey.equals("command")) && (!paramKey.equals("cmd")) && (!paramKey.equals("actpath"))) {
				try {
					String fileName = UTF8URLDecoder.decode(paramKey);
					selectedFiles.add(fileName);
				} catch (Exception ue1) {
					LOG.error(ue1);
				}
			}
		}

		Element gpxTrackElem = doc.createElement("gpxTracks");

		doc.appendChild(gpxTrackElem);

		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet",
				"type=\"text/xsl\" href=\"/webfilesys/xsl/multiGPXTracks.xsl\"");

		doc.insertBefore(xslRef, gpxTrackElem);

		Element gpxFileListElem = doc.createElement("gpxFiles");
		
		gpxTrackElem.appendChild(gpxFileListElem);
		
		for (String fileName : selectedFiles) {
			String filePath = CommonUtils.joinFilesysPath(currentPath, fileName);
			
			Element gpxFileElem = doc.createElement("gpxFile");
			XmlUtil.setElementText(gpxFileElem, CommonUtils.escapeForJavascript(filePath));
			gpxFileListElem.appendChild(gpxFileElem);
		}
		
        String googleMapsAPIKey = null;
		if (req.getScheme().equalsIgnoreCase("https")) {
			googleMapsAPIKey = WebFileSys.getInstance().getGoogleMapsAPIKeyHTTPS();
		} else {
			googleMapsAPIKey = WebFileSys.getInstance().getGoogleMapsAPIKeyHTTP();
		}
		
		Element apiKeyElem = doc.createElement("googleMapsAPIKey");
		XmlUtil.setElementText(apiKeyElem, googleMapsAPIKey);
		gpxTrackElem.appendChild(apiKeyElem);
		
		processResponse("multiGPXTracks.xsl");
	}
}