package de.webfilesys.gui.blog;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import de.webfilesys.MetaInfManager;
import de.webfilesys.gui.ajax.XmlRequestHandlerBase;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * Delete a folder tree.
 * Mobile version.
 */
public class BlogDeleteCommentsHandler extends XmlRequestHandlerBase
{
	public BlogDeleteCommentsHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid)
	{
        super(req, resp, session, output, uid);
	}
	
	protected void process()
	{
		if (!checkWriteAccess())
		{
			return;
		}
		
		String filePath = getParameter("filePath");

		if (CommonUtils.isEmpty(filePath)) {
			Logger.getLogger(getClass()).error("missing parameter filePath");
			return;
		}
		
		Element resultElement = doc.createElement("result");

		String currentPathOS = filePath.replace('/', File.separatorChar);
		
		MetaInfManager.getInstance().removeComments(currentPathOS);

		XmlUtil.setChildText(resultElement, "success", "true");
		
		doc.appendChild(resultElement);
		
		this.processResponse();
	}
}
