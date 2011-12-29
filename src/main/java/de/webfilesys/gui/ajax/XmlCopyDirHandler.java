package de.webfilesys.gui.ajax;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;

import de.webfilesys.ClipBoard;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XmlCopyDirHandler extends XmlRequestHandlerBase
{
	public XmlCopyDirHandler(
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
		
		String relPath = null;
		
		String path = getParameter("path");
		
		if (isMobile()) 
		{
		    relPath = path;
		    path = getAbsolutePath(path);
		}

		if (!checkAccess(path))
		{
			return;
		}

		ClipBoard clipBoard = (ClipBoard) session.getAttribute("clipBoard");
		
		if (clipBoard != null)
		{
			clipBoard.reset();
		}
		else
		{
			clipBoard = new ClipBoard();
			
			session.setAttribute("clipBoard", clipBoard);
		}
		
		clipBoard.addDir(path);

		clipBoard.setCopyOperation();
		
		String shortPath = null;
		
		if (relPath != null) 
		{
		    shortPath = CommonUtils.shortName(relPath, 35);
		}
		else
		{
            shortPath = CommonUtils.shortName(path, 35);
		}
		
		String resultMsg = getResource("label.directory","Directory") + "<br/> " + shortPath + "<br/> " + getResource("alert.dircopied","has been copied to clipboard");
		
		Element resultElement = doc.createElement("result");
		
		XmlUtil.setChildText(resultElement, "message", resultMsg);

		XmlUtil.setChildText(resultElement, "success", "true");
			
		doc.appendChild(resultElement);
		
		this.processResponse();
	}
}
