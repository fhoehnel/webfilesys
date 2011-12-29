package de.webfilesys.gui.ajax;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;

import de.webfilesys.FileSysStat;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XmlDirStatsHandler extends XmlRequestHandlerBase
{
	public XmlDirStatsHandler(
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
		String currentPath = getParameter("path");

		if (!checkAccess(currentPath)) 
		{
		    return;
		}
		
        FileSysStat fileSysStat = new FileSysStat(currentPath);
        
        fileSysStat.getStatistics();        
		
		Element resultElement = doc.createElement("result");

		doc.appendChild(resultElement);
		
        XmlUtil.setChildText(resultElement, "bytesInTree", Long.toString(fileSysStat.getTotalSizeSum()));
        XmlUtil.setChildText(resultElement, "foldersInTree", Long.toString(fileSysStat.getTotalSubdirNum()));
        XmlUtil.setChildText(resultElement, "filesInTree", Long.toString(fileSysStat.getTotalFileNum()));
        XmlUtil.setChildText(resultElement, "subdirLevels", Long.toString(fileSysStat.getMaxLevel()));
		
		this.processResponse();
	}
}
