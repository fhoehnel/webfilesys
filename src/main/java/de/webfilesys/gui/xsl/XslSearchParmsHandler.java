package de.webfilesys.gui.xsl;

import java.io.PrintWriter;
import java.util.Date;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.Category;
import de.webfilesys.CategoryManager;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslSearchParmsHandler extends XslRequestHandlerBase
{
	public XslSearchParmsHandler(
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
		String currentPath = getParameter("actpath");

        if (isMobile()) 
        {
            currentPath = getAbsolutePath(currentPath);
        }
        else
        {
            if ((currentPath == null) || (currentPath.trim().length() == 0))
            {
                currentPath = getCwd();
            }
        }

		if (!accessAllowed(currentPath))
		{
			Logger.getLogger(getClass()).warn("user " + uid + " tried to access folder outside of his document root: " + currentPath);
			
			return;
		}

		String relativePath = this.getHeadlinePath(currentPath);

		Element searchParmsElement = doc.createElement("searchParms");
			
		doc.appendChild(searchParmsElement);

		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/searchParms.xsl\"");

		doc.insertBefore(xslRef, searchParmsElement);

		XmlUtil.setChildText(searchParmsElement, "css", userMgr.getCSS(uid), false);
		XmlUtil.setChildText(searchParmsElement, "currentPath", currentPath, false);
		XmlUtil.setChildText(searchParmsElement, "relativePath", relativePath, false);
		
		addMsgResource("label.searchHead", getResource("label.searchHead", "File and fulltext search"));
		addMsgResource("label.searchPath", getResource("label.searchPath", "Search in Folder tree"));
		addMsgResource("label.filemask", getResource("label.filemask", "file name filter"));
		addMsgResource("label.searcharg", getResource("label.searcharg","Text in File"));
		addMsgResource("label.argdesc1", getResource("label.argdesc1","searches for files that contain all of the words above"));
		addMsgResource("label.argdesc2", getResource("label.argdesc2","search phrase containing spaces"));
		addMsgResource("label.includeSubdirs", getResource("label.includeSubdirs","include subdirectories in search"));
		addMsgResource("label.includemetainf", getResource("label.includemetainf","include meta info/description in search"));
		addMsgResource("label.metainfonly", getResource("label.metainfonly","search in meta info/description only"));

		addMsgResource("label.dateRangeFrom", getResource("label.dateRangeFrom","modification date from"));
		addMsgResource("label.dateRangeUntil", getResource("label.dateRangeUntil","modification date until"));

		addMsgResource("label.searchCalendar", getResource("label.searchCalendar", "select date from calendar"));
		addMsgResource("label.searchDateConflict", getResource("label.searchDateConflict", "the end date must be after start date!"));

		addMsgResource("label.assignedToCategory", getResource("label.assignedToCategory","assigned to category"));
		addMsgResource("label.selectCategory", getResource("label.selectCategory","- select categories -"));
		
        addMsgResource("label.searchResultAsTree", getResource("label.searchResultAsTree","show results as folder tree"));
		
		addMsgResource("button.startsearch", getResource("button.startsearch","Start Search"));
		addMsgResource("button.cancel", getResource("button.cancel","Cancel"));

		CategoryManager catMgr = CategoryManager.getInstance();
        
		Vector categoryList = catMgr.getListOfCategories(uid);
        
		if (categoryList != null)
		{
			Element categoriesElement = doc.createElement("categories");
			
			searchParmsElement.appendChild(categoriesElement);
			
			for (int i = 0; i < categoryList.size(); i++)
			{
				Category category = (Category) categoryList.elementAt(i);
        		
				Element categoryElement = doc.createElement("category");

                categoryElement.setAttribute("name", category.getName());
			
				categoriesElement.appendChild(categoryElement);
			}
		}
		
		Date now = new Date();
		
		Element currentDateElement = doc.createElement("currentDate");
			
		searchParmsElement.appendChild(currentDateElement);
		
		XmlUtil.setChildText(currentDateElement, "year", Integer.toString(now.getYear() + 1900));	
		XmlUtil.setChildText(currentDateElement, "month", Integer.toString(now.getMonth() + 1));	
		XmlUtil.setChildText(currentDateElement, "day", Integer.toString(now.getDate()));	
			
		this.processResponse("searchParms.xsl", true);
    }
}