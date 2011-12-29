package de.webfilesys.gui.xsl;

import java.io.PrintWriter;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.Category;
import de.webfilesys.CategoryManager;
import de.webfilesys.MetaInfManager;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslAssignCategoryHandler extends XslRequestHandlerBase
{
	
	public XslAssignCategoryHandler(
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
		String filePath = getParameter("filePath");
		
		Element catListElement = doc.createElement("categoryList");
			
		doc.appendChild(catListElement);
			
		String cmd = getParameter("cmd");
		
		if (cmd != null)
		{
			if (cmd.equals("list"))
			{
				listAssignment(catListElement, filePath);
			}
			else if (cmd.equals("assign"))
			{
				assignCategory(catListElement, filePath);
			}
			else if (cmd.equals("unassign"))
			{
				unassignCategory(catListElement, filePath);
			}
			else
			{
				listAssignment(catListElement, filePath);
			}
		}
		else
		{
			listAssignment(catListElement, filePath);
		}
	}

    private void listAssignment(Element catListElement, String filePath)
    {
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/assignCategory.xsl\"");

		doc.insertBefore(xslRef, catListElement);

		XmlUtil.setChildText(catListElement, "css", userMgr.getCSS(uid), false);
		XmlUtil.setChildText(catListElement, "filePath",filePath, false);
		
		String relativePath = this.getHeadlinePath(filePath);
		
		XmlUtil.setChildText(catListElement, "shortFilePath", CommonUtils.shortName(relativePath,50), false);

		addMsgResource("label.assignCategories", getResource("label.assignCategories","Assign Categories"));
		addMsgResource("button.closewin", getResource("button.closewin","Close Window"));
		addMsgResource("button.ok", getResource("button.ok","OK"));

		addMsgResource("label.assignedCats", getResource("label.assignedCats","assigned categories"));
		addMsgResource("label.unassignedCats", getResource("label.unassignedCats","other categories"));

		addMsgResource("button.manageCategories", getResource("button.manageCategories","Manage Categories"));
		
        MetaInfManager metaInfMgr = MetaInfManager.getInstance();

        Vector assignedCategories = metaInfMgr.getListOfCategories(filePath);

		CategoryManager catMgr = CategoryManager.getInstance();

		Vector userCategories = catMgr.getListOfCategories(uid);
        
        if (userCategories != null)
        {
			for (int i = 0; i < userCategories.size(); i++)
			{
				Category cat = (Category) userCategories.elementAt(i);
        	
				Element catElement = doc.createElement("category");
			
				catElement.setAttribute("id", cat.getId());
        
				XmlUtil.setChildText(catElement, "name" , cat.getName());           
        	
				catListElement.appendChild(catElement);
			
			    if (assignedCategories != null)
			    {
					boolean found = false;
			
					for (int k=0; (!found) && (k < assignedCategories.size()); k++)
					{
						Category assignedCat = (Category) assignedCategories.elementAt(k);
				
						if (cat.getName().equals(assignedCat.getName()))
						{
							XmlUtil.setChildText(catElement, "assigned", "true");
					
							found = true;
						}
					}
			    }
			}
        }


        if ((userCategories == null) || (userCategories.size() == 0))
        {
			addMsgResource("label.noCategoryDefined", getResource("label.noCategoryDefined","No categories has been defined."));
        }

		this.processResponse("assignCategory.xsl", true);
    }
    
	private void unassignCategory(Element catListElement, String filePath)
	{
		String assigned = getParameter("assigned");
		
		if ((assigned == null) || (assigned.trim().length() == 0))
		{
			listAssignment(catListElement, filePath);
			return;
		}

		CategoryManager catMgr = CategoryManager.getInstance();
		
		Category category = catMgr.getCategory(uid, assigned);
		
		if (category != null)
		{
			MetaInfManager metaInfMgr = MetaInfManager.getInstance();
		
			metaInfMgr.removeCategoryByName(filePath, category.getName());
		}
		
		listAssignment(catListElement, filePath);
	}

	private void assignCategory(Element catListElement, String filePath)
	{
		String unassigned = getParameter("unassigned");
		
		if ((unassigned == null) || (unassigned.trim().length() == 0))
		{
			listAssignment(catListElement, filePath);
			return;
		}
		
		CategoryManager catMgr = CategoryManager.getInstance();
		
		Category category = catMgr.getCategory(uid, unassigned);
		
		if (category != null)
		{
			MetaInfManager metaInfMgr = MetaInfManager.getInstance();
		
			metaInfMgr.addCategory(filePath, category);
		}
		
		listAssignment(catListElement, filePath);
	}

}