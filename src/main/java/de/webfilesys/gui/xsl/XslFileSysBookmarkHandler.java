package de.webfilesys.gui.xsl;

import java.io.PrintWriter;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.FileSysBookmark;
import de.webfilesys.FileSysBookmarkManager;
import de.webfilesys.decoration.Decoration;
import de.webfilesys.decoration.DecorationManager;
import de.webfilesys.util.UTF8URLEncoder;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslFileSysBookmarkHandler extends XslRequestHandlerBase
{
	public XslFileSysBookmarkHandler(
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
		FileSysBookmarkManager bookmarkMgr = FileSysBookmarkManager.getInstance();

		String cmd = getParameter("cmd");
		
		if (cmd != null)
		{
			if (cmd.equals("delete"))
			{
				if (this.checkWriteAccess())
				{
					String bookmarkId = getParameter("id");
					
					if (bookmarkId != null)
					{
						bookmarkMgr.removeBookmark(uid, bookmarkId);
					}
				}
			}
		}
		
		Element bookmarkListElement = doc.createElement("bookmarkList");
			
		doc.appendChild(bookmarkListElement);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/bookmarkList.xsl\"");

		doc.insertBefore(xslRef, bookmarkListElement);

		XmlUtil.setChildText(bookmarkListElement, "css", userMgr.getCSS(uid), false);

		XmlUtil.setChildText(bookmarkListElement, "currentPath", this.getCwd(), false);
		
		String mobile = (String) session.getAttribute("mobile");
		
		if (mobile != null) {
		    XmlUtil.setChildText(bookmarkListElement, "mobile", "true", false);
		}
		
        if (readonly)
        {
        	XmlUtil.setChildText(bookmarkListElement, "readonly", "true", false);
        }
        else
        {
    		addMsgResource("label.deleteBookmark", getResource("label.deleteBookmark","Delete Bookmark"));
        }

		addMsgResource("label.bookmarks", getResource("label.bookmarks","Bookmarked Folders"));
		addMsgResource("label.noBookmarksDefined", getResource("label.noBookmarksDefined","No bookmarks have been defined"));
		addMsgResource("button.return", getResource("button.return", "Return"));
		
		Vector userBookmarks = bookmarkMgr.getListOfBookmarks(uid, true);
        
		for (int i = 0; i < userBookmarks.size(); i++)
		{
			FileSysBookmark bookmark = (FileSysBookmark) userBookmarks.elementAt(i);
        	
			Element bookmarkElement = doc.createElement("bookmark");
			
			bookmarkElement.setAttribute("id", bookmark.getId());
        
			XmlUtil.setChildText(bookmarkElement, "name" , bookmark.getName());           

			XmlUtil.setChildText(bookmarkElement, "path" , this.getHeadlinePath(bookmark.getPath()));

			XmlUtil.setChildText(bookmarkElement, "encodedPath" , UTF8URLEncoder.encode(bookmark.getPath()));    
			
			Decoration deco = DecorationManager.getInstance().getDecoration(bookmark.getPath());
			
			if (deco != null) 
			{
				if (deco.getIcon() != null) 
				{
					XmlUtil.setChildText(bookmarkElement, "icon" , deco.getIcon());
				}
				if (deco.getTextColor() != null) 
				{
					XmlUtil.setChildText(bookmarkElement, "textColor" , deco.getTextColor());
				}
			}
        	
			bookmarkListElement.appendChild(bookmarkElement);
		}

		this.processResponse("bookmarkList.xsl", true);
    }
}