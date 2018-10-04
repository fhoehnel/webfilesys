package de.webfilesys.gui.xsl;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
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
		
	    XmlUtil.setChildText(searchParmsElement, "language", language, false);
		
		CategoryManager catMgr = CategoryManager.getInstance();
        
		ArrayList<Category> categoryList = catMgr.getListOfCategories(uid);
        
		if (categoryList != null)
		{
			Element categoriesElement = doc.createElement("categories");
			
			searchParmsElement.appendChild(categoriesElement);
			
			for (Category category : categoryList) {
			
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