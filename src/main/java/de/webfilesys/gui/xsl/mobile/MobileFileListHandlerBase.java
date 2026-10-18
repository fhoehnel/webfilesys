package de.webfilesys.gui.xsl.mobile;

import de.webfilesys.WebFileSys;
import de.webfilesys.gui.xsl.XslRequestHandlerBase;
import de.webfilesys.util.UTF8URLEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.File;
import java.io.PrintWriter;
import java.util.StringTokenizer;

/**
 * @author Frank Hoehnel
 */
public class MobileFileListHandlerBase extends XslRequestHandlerBase {
	private static final Logger LOG = LogManager.getLogger(MobileFileListHandlerBase.class);

	public MobileFileListHandlerBase(
			HttpServletRequest req,
			HttpServletResponse resp,
			HttpSession session,
			PrintWriter output,
			String uid) {
		super(req, resp, session, output, uid);
	}

    public Element addCurrentTrail(Element fileListElem, String docRoot, String relativePath) {
		Element currentPathElem = doc.createElement("currentPath");
		fileListElem.appendChild(currentPathElem);

		currentPathElem.setAttribute("path", relativePath);
		currentPathElem.setAttribute("pathForScript", insertDoubleBackslash(relativePath));

		if ((File.separatorChar == '\\' && docRoot.charAt(0) != '*') ||
				(File.separatorChar == '/' && docRoot.length() > 1)) {
			// userid as first path element

			Element partOfPathElem = doc.createElement("pathElem");
			currentPathElem.appendChild(partOfPathElem);
			partOfPathElem.setAttribute("name", uid);
			partOfPathElem.setAttribute("path", "/");
		}

		if ((File.separatorChar == '\\' && docRoot.charAt(0) == '*') ||
				(File.separatorChar == '/' && docRoot.length() == 1)) {
			// host name as first path element

			Element partOfPathElem = doc.createElement("pathElem");
			currentPathElem.appendChild(partOfPathElem);
			partOfPathElem.setAttribute("name", WebFileSys.getInstance().getLocalHostName());
			partOfPathElem.setAttribute("path", "/");
		}

		StringTokenizer pathParser = new StringTokenizer(relativePath, File.separator);
		StringBuilder partialPath = new StringBuilder();
		boolean firstToken = true;

		while (pathParser.hasMoreTokens()) {
			String partOfPath = pathParser.nextToken();
			partialPath.append(partOfPath);

			if (pathParser.hasMoreTokens()) {
				partialPath.append(File.separatorChar);
			} else {
				if (firstToken && partOfPath.length() == 2 && partOfPath.charAt(1) == ':') {
					partialPath.append(File.separatorChar);
				}
			}

			Element partOfPathElem = doc.createElement("pathElem");
			currentPathElem.appendChild(partOfPathElem);
			partOfPathElem.setAttribute("name", partOfPath);
			partOfPathElem.setAttribute("path", UTF8URLEncoder.encode(partialPath.toString()));
			firstToken = false;
		}
		return currentPathElem;
    }
}
