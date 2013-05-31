package de.webfilesys.gui.xsl;

import java.io.File;
import java.io.PrintWriter;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;

import de.webfilesys.util.UTF8URLEncoder;

public class XslFileListHandlerBase extends XslRequestHandlerBase {

	public XslFileListHandlerBase(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid)
	{
        super(req, resp, session, output, uid);
	}
	
	protected void addCurrentTrail(Element fileListElement, String currentPath, String docRoot, String mask) 
	{
		Element currentTrailElem = fileListElement.getOwnerDocument().createElement("currentTrail");
		
		fileListElement.appendChild(currentTrailElem);
		
		String relativePath = getHeadlinePath(currentPath);
		
		currentTrailElem.setAttribute("path", relativePath);
		currentTrailElem.setAttribute("separator", File.separator);
		currentTrailElem.setAttribute("mask", mask);

		String docRootWithSep = null;;
		
        if (((File.separatorChar == '\\') && (docRoot.charAt(0) != '*')) ||
            ((File.separatorChar == '/') && (docRoot.length() > 1)))
        {
    		if (File.separatorChar == '\\')
    		{
    			docRootWithSep = docRoot.replace('/', File.separatorChar);
    		}
    		else
    		{
    			docRootWithSep = docRoot;
    		}
    			
			if (!docRootWithSep.endsWith(File.separator)) {
				docRootWithSep = docRootWithSep + File.separatorChar;
			}
        }
        
		StringTokenizer pathParser = new StringTokenizer(relativePath, File.separator);
		
		StringBuffer partialPath = new StringBuffer();

        if ((File.separatorChar == '/') && (docRoot.length() == 1))
		{
			partialPath.append('/');
			if (currentPath.length() > 1) {
				currentTrailElem.setAttribute("unixRoot", "true");
			}
		}
		
		StringBuffer linkPath = new StringBuffer();
		
		boolean firstPart = true;
		
		while (pathParser.hasMoreTokens())
		{
			String partOfPath = pathParser.nextToken();
			
			partialPath.append(partOfPath);
			
			Element partOfPathElem = doc.createElement("pathElem");
			
			currentTrailElem.appendChild(partOfPathElem);
			
			partOfPathElem.setAttribute("name", partOfPath);
			
			if (docRootWithSep != null) 
			{
				if (!firstPart) 
				{
					linkPath.append(partOfPath);
				}
				partOfPathElem.setAttribute("path", UTF8URLEncoder.encode(docRootWithSep + linkPath.toString()));
			}
			else
			{
				if (firstPart && (File.separatorChar == '\\')) 
				{
					// drive letter must be suffixed with backslash
					partOfPathElem.setAttribute("path", UTF8URLEncoder.encode(partialPath.toString() + File.separator));
				}
				else
				{
					partOfPathElem.setAttribute("path", UTF8URLEncoder.encode(partialPath.toString()));
				}
			}

			if (pathParser.hasMoreTokens())
			{
				partialPath.append(File.separatorChar);		

				if ((docRootWithSep != null) && (!firstPart)) {
					linkPath.append(File.separatorChar);
				}
			}
			
			firstPart = false;
		}
	}

}
