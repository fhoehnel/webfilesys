package de.webfilesys.gui.xsl;

import java.io.PrintWriter;
import java.util.ArrayList;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.Category;
import de.webfilesys.CategoryManager;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.UTF8URLEncoder;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslCategoryHandler extends XslRequestHandlerBase
{
    public static final String PARM_SEPARATOR = "~";
	
	public XslCategoryHandler(
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
		if (this.readonly)
		{
			LogManager.getLogger(getClass()).warn("read-only user tried to manage categories");
			
			return;
		}
		
		Element catListElement = doc.createElement("categoryList");
			
		doc.appendChild(catListElement);
        
        String fileName = getParameter("fileName");

        if (fileName == null) {
            return;
        }

        XmlUtil.setChildText(catListElement, "fileName", fileName);
        XmlUtil.setChildText(catListElement, "fileNameForScript", CommonUtils.escapeForJavascript(fileName));
        XmlUtil.setChildText(catListElement, "encodedFileName", UTF8URLEncoder.encode(fileName));
        XmlUtil.setChildText(catListElement, "shortFileName", UTF8URLEncoder.encode(CommonUtils.shortName(fileName,50)), false);

		String cmd = getParameter("cmd");
        if (cmd != null) {
			if (cmd.equals("list")) {
				listCategories(catListElement);
			} else if (cmd.equals("new")) {
				newCategory(catListElement);
			} else if (cmd.equals("delete")) {
				deleteCategory(catListElement);
			} else {
				listCategories(catListElement);
			}
		} else {
			listCategories(catListElement);
		}
	}

    private void listCategories(Element catListElement) {
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/categoryList.xsl\"");

		doc.insertBefore(xslRef, catListElement);

		addMsgResource("label.manageCategories", getResource("label.manageCategories","Manage File Categories"));
		addMsgResource("button.delete", getResource("button.delete","Delete"));
		addMsgResource("confirm.delCategory", getResource("confirm.delCategory","You will not be able to find files via this category name. Delete anyway?"));
		addMsgResource("button.return", getResource("button.return","Return"));
		addMsgResource("label.noCategoryDefined", getResource("label.noCategoryDefined","No categories have been defined"));
        addMsgResource("label.newCategory", getResource("label.newCategory","Create New Category"));
        addMsgResource("label.categoryName", getResource("label.categoryName","Category Name"));
        addMsgResource("button.createCategory", getResource("button.createCategory","Create Category"));
        addMsgResource("alert.noCategorySelected", getResource("alert.noCategorySelected","No category selected!"));
		
		CategoryManager catMgr = CategoryManager.getInstance();
        ArrayList<Category> userCategories = catMgr.getListOfCategories(uid);
		for (Category cat : userCategories) {
            Element catElement = doc.createElement("category");
			catElement.setAttribute("id", cat.getId());
			XmlUtil.setChildText(catElement, "name" , cat.getName());
			catListElement.appendChild(catElement);
		}
		this.processResponse("categoryList.xsl");
    }

	private void newCategory(Element catListElement) {
		CategoryManager catMgr = CategoryManager.getInstance();
		String newCategoryName = getParameter("newCategory");
		
		if (!CommonUtils.isEmpty(newCategoryName)) {
            if (catMgr.getCategoryElementByName(uid, newCategoryName) != null) {
				addMsgResource("error.duplicateCategory", getResource("error.duplicateCategory","Category already exists"));
                XmlUtil.setChildText(catListElement, "newCategory", newCategoryName);
			} else {
                Category newCategory = new Category();
                newCategory.setName(newCategoryName);
                catMgr.createCategory(uid, newCategory);
            }
		} else {
            XmlUtil.setChildText(catListElement, "newCategory", "");
            addMsgResource("error.missingCategoryName", getResource("error.missingCategoryName","Enter a name for the new category!"));
        }
        listCategories(catListElement);
	}

	private void deleteCategory(Element catListElement) {
		CategoryManager catMgr = CategoryManager.getInstance();
        String[] selectedIds = req.getParameterValues("categoryId");
        if (selectedIds != null) {
            for (String selectedId : selectedIds) {
                catMgr.removeCategory(uid, selectedId);
            }
        }
		listCategories(catListElement);
	}

}