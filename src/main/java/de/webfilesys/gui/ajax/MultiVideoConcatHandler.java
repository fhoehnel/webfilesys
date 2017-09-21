package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class MultiVideoConcatHandler extends XmlRequestHandlerBase {
	
	private static Logger LOG = Logger.getLogger(MultiVideoConcatHandler.class);
	
	public static final String LIST_PREFIX = "list-";
	
	private static final int LIST_PREFIX_LENGTH = LIST_PREFIX.length();
	
	boolean clientIsLocal = false;

	public MultiVideoConcatHandler(HttpServletRequest req, HttpServletResponse resp, HttpSession session,
			PrintWriter output, String uid, boolean clientIsLocal) {
		super(req, resp, session, output, uid);

		this.clientIsLocal = clientIsLocal;
	}

	protected void process() {
		if (!checkWriteAccess()) {
			return;
		}
		
		String currentPath = getCwd();

		ArrayList<String> selectedFiles = new ArrayList<String>();

        Enumeration allKeys = req.getParameterNames();
		
		while (allKeys.hasMoreElements()) {
			String paramKey =(String) allKeys.nextElement();

            if (paramKey.startsWith(LIST_PREFIX)) {
				selectedFiles.add(paramKey.substring(LIST_PREFIX_LENGTH)); 
            }
		}
		
		for (int i = 0; i < selectedFiles.size(); i++) {
			String filePath = null;

			if (currentPath.endsWith(File.separator)) {
				filePath = currentPath + selectedFiles.get(i);
			} else {
				filePath = currentPath + File.separator + selectedFiles.get(i);
			}
			
			LOG.debug("video file to concatenate: " + filePath);
		}

		Element resultElement = doc.createElement("result");

		XmlUtil.setChildText(resultElement, "success", Boolean.toString(true));
		
		doc.appendChild(resultElement);

		processResponse();
	}

}
