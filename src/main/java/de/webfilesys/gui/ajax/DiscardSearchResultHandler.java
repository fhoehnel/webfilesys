package de.webfilesys.gui.ajax;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;

import de.webfilesys.MetaInfManager;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class DiscardSearchResultHandler extends XmlRequestHandlerBase
{
	public DiscardSearchResultHandler(
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
        String searchResultDir = getParameter("resultDir");
        
        if (searchResultDir == null)
        {
        	return;
        }
        
        if (!searchResultDir.contains("searchResult-")) {
        	return;
        }

		if (!checkAccess(searchResultDir))
		{
			return;
		}
		
		if (!checkWriteAccess()) 
		{
		    return;
		}
		
		boolean success = CommonUtils.deleteDirTree(searchResultDir);
		
		MetaInfManager.getInstance().releaseMetaInf(searchResultDir);
		
		Element resultElement = doc.createElement("result");
		
		XmlUtil.setChildText(resultElement, "message", success ? "deleted" : "error");

		XmlUtil.setChildText(resultElement, "success", Boolean.toString(success));
			
		doc.appendChild(resultElement);
		
		processResponse();
	}
}
