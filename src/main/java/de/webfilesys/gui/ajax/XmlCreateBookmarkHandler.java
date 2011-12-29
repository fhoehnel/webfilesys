package de.webfilesys.gui.ajax;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;

import de.webfilesys.FileSysBookmark;
import de.webfilesys.FileSysBookmarkManager;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XmlCreateBookmarkHandler extends XmlRequestHandlerBase
{
	public XmlCreateBookmarkHandler(
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

		String path = getParameter("path");

		if (!checkAccess(path))
		{
			return;
		}

        String bookmarkName = getParameter("bookmarkName");
        
        FileSysBookmarkManager bookmarkMgr = FileSysBookmarkManager.getInstance();
        
        FileSysBookmark newBookmark = new FileSysBookmark();
        
        newBookmark.setPath(path);
        
        newBookmark.setName(bookmarkName);
        
        bookmarkMgr.createBookmark(uid, newBookmark);
		
		String resultMsg = getResource("alert.bookmarkCreated","New Bookmark created for directory") 
		                   + "<br/> " + insertDoubleBackslash(CommonUtils.shortName(getHeadlinePath(path), 40));
		
		Element resultElement = doc.createElement("result");
		
		XmlUtil.setChildText(resultElement, "message", resultMsg);

		XmlUtil.setChildText(resultElement, "success", "true");
			
		doc.appendChild(resultElement);
		
		this.processResponse();
	}
}
