package de.webfilesys.gui.user;

import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;


import de.webfilesys.util.UTF8URLDecoder;

/**
 * @author Frank Hoehnel
 */
public class MultiFileRequestHandler extends UserRequestHandler
{
	private static final Set<String> IGNORED_PARAMS = new HashSet<>(Arrays.asList("cb-setAll", "command", "cmd", "actpath"));

	protected String actPath = null;
	
	protected List<String> selectedFiles = null;

	protected String cmd = null;
	
	public MultiFileRequestHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid)
	{
        super(req, resp, session, output, uid);

		Map<String, String[]> params = req.getParameterMap();
		selectedFiles = params.keySet().stream()
				.filter(paramKey -> !IGNORED_PARAMS.contains(paramKey))
				.map(UTF8URLDecoder::decode)
				.filter(Objects::nonNull)
				.toList();

		session.setAttribute("selectedFiles", selectedFiles);
		
		if (actPath == null) {
		    actPath = getCwd();
		} else {
	        if (isMobile()) {
	            actPath = getAbsolutePath(actPath);
	        }
		}
	}

	public void handleRequest()
	{
		if (!accessAllowed(actPath))
		{
			LogManager.getLogger(getClass()).warn("user " + uid + " tried to access folder outside of it's document root: " + actPath);
			return;
		}
		
		if (selectedFiles.size()==0)
		{
			output.print("<HTML>");
			output.print("<HEAD>");

			javascriptAlert(getResource("alert.noFilesSelected","No files have been selected"));

			output.println("<script language=\"javascript\">");
			output.println("window.location.href='/webfilesys/servlet?command=listFiles';");
			output.println("</script>");
			output.println("</HEAD></HTML>");
			output.flush();
			return;
		}

		process();
	}
}
