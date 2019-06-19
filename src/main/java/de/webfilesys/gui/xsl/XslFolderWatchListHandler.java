package de.webfilesys.gui.xsl;

import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.decoration.Decoration;
import de.webfilesys.decoration.DecorationManager;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.UTF8URLEncoder;
import de.webfilesys.util.XmlUtil;
import de.webfilesys.watch.FolderWatchManager;

/**
 * @author Frank Hoehnel
 */
public class XslFolderWatchListHandler extends XslRequestHandlerBase
{
	public XslFolderWatchListHandler(
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
		FolderWatchManager watchMgr = FolderWatchManager.getInstance();
		
		String cmd = getParameter("cmd");
		
		if (cmd != null)
		{
			if (cmd.equals("unwatch"))
			{
				if (this.checkWriteAccess())
				{
					String path = getParameter("path");
					
					if (path != null)
					{
						watchMgr.removeFolderChangeListener(path, uid);
					}
				}
			}
		}
		
		Element watchListElement = doc.createElement("watchList");
			
		doc.appendChild(watchListElement);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/folderWatchList.xsl\"");

		doc.insertBefore(xslRef, watchListElement);

		XmlUtil.setChildText(watchListElement, "currentPath", getCwd(), false);

		XmlUtil.setChildText(watchListElement, "currentPathEncoded", UTF8URLEncoder.encode(getCwd()), false);

	    ArrayList<String> watchedFolders = watchMgr.getWatchedFolders(uid);
		
		for (String watchedFolder : watchedFolders)
		{
			Element folderElement = doc.createElement("folder");
			
			XmlUtil.setChildText(folderElement, "path" , watchedFolder);

			XmlUtil.setChildText(folderElement, "shortPath" , CommonUtils.shortName(getHeadlinePath(watchedFolder), 40));

			XmlUtil.setChildText(folderElement, "relativePath" , getHeadlinePath(watchedFolder));

			XmlUtil.setChildText(folderElement, "encodedPath" , UTF8URLEncoder.encode(watchedFolder));    
			
			Decoration deco = DecorationManager.getInstance().getDecoration(watchedFolder);
			
			if (deco != null) 
			{
				if (deco.getIcon() != null) 
				{
					XmlUtil.setChildText(folderElement, "icon" , deco.getIcon());
				}
				if (deco.getTextColor() != null) 
				{
					XmlUtil.setChildText(folderElement, "textColor" , deco.getTextColor());
				}
			}
        	
			watchListElement.appendChild(folderElement);
		}

		processResponse("folderWatchList.xsl");
    }
}