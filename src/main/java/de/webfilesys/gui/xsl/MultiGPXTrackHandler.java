package de.webfilesys.gui.xsl;

import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.webfilesys.WebFileSysConfig;

import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.UTF8URLDecoder;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class MultiGPXTrackHandler extends XslRequestHandlerBase {

	private static final Set<String> IGNORED_PARAMS = new HashSet<>(Arrays.asList("cb-setAll", "command", "cmd", "actpath"));

	public MultiGPXTrackHandler(HttpServletRequest req, HttpServletResponse resp, HttpSession session,
			PrintWriter output, String uid) {
		super(req, resp, session, output, uid);
	}

	protected void process() {
		String currentPath = getCwd();

		Map<String, String[]> params = req.getParameterMap();
		List<String> selectedFiles = params.keySet().stream()
				.filter(paramKey -> !IGNORED_PARAMS.contains(paramKey))
				.map(UTF8URLDecoder::decode)
				.filter(Objects::nonNull)
				.toList();

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
			googleMapsAPIKey = WebFileSysConfig.getInstance().getGoogleMapsAPIKeyHTTPS();
		} else {
			googleMapsAPIKey = WebFileSysConfig.getInstance().getGoogleMapsAPIKeyHTTP();
		}
		
		Element apiKeyElem = doc.createElement("googleMapsAPIKey");
		XmlUtil.setElementText(apiKeyElem, googleMapsAPIKey);
		gpxTrackElem.appendChild(apiKeyElem);
		
		processResponse("multiGPXTracks.xsl");
	}
}