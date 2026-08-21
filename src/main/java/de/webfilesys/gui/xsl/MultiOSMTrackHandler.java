package de.webfilesys.gui.xsl;

import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.UTF8URLDecoder;
import de.webfilesys.util.XmlUtil;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Frank Hoehnel
 */
public class MultiOSMTrackHandler extends XslRequestHandlerBase {
	private static final Set<String> IGNORED_PARAMS =
			new HashSet<>(Arrays.asList("cb-setAll", "command", "cmd", "actpath"));

	public MultiOSMTrackHandler(HttpServletRequest req, HttpServletResponse resp, HttpSession session,
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
				"type=\"text/xsl\" href=\"/webfilesys/xsl/multiOSMTracks.xsl\"");
		doc.insertBefore(xslRef, gpxTrackElem);

		Element gpxFileListElem = doc.createElement("gpxFiles");
		gpxTrackElem.appendChild(gpxFileListElem);
		
		for (String fileName : selectedFiles) {
			String filePath = CommonUtils.joinFilesysPath(currentPath, fileName);
			Element gpxFileElem = doc.createElement("gpxFile");
			XmlUtil.setElementText(gpxFileElem, CommonUtils.escapeForJavascript(filePath));
			gpxFileListElem.appendChild(gpxFileElem);
		}
		
		processResponse("multiOSMTracks.xsl");
	}
}