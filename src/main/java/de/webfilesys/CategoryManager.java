package de.webfilesys;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.webfilesys.util.XmlUtil;

public class CategoryManager extends Thread
{
    public static final String CATEGORIES_DIR    = "categories";
	
    Hashtable categoryTable = null;

    Hashtable indexTable = null;

    Hashtable cacheDirty = null;
    
    DocumentBuilder builder = null;
    
    String categoryFileName = null;
    
    boolean shutdownFlag = false;

    private static CategoryManager categoryManager = null;
    
    private String categoryPath = null;
    
    private CategoryManager()
    {
    	categoryPath = WebFileSys.getInstance().getConfigBaseDir() + "/" + CATEGORIES_DIR;
    	
        categoryTable=new Hashtable();
        
        indexTable=new Hashtable();
        
        cacheDirty=new Hashtable();

        shutdownFlag=false;
        
        builder = null;

        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
        }
        catch (ParserConfigurationException pcex)
        {
            Logger.getLogger(getClass()).error(pcex);
        }

        this.start();
    }

    public static CategoryManager getInstance()
    {
        if (categoryManager==null)
        {
            categoryManager=new CategoryManager();
        }

        return(categoryManager);
    }

    public Element getCategoryList(String userid)
    {
        Element categoryList=(Element) categoryTable.get(userid);

        if (categoryList!=null)
        {
            return(categoryList);
        }
    
        categoryFileName = categoryPath + File.separator + userid + ".xml";

        File categoryFile = new File(categoryFileName);

        if (categoryFile.exists() && categoryFile.isFile())
        {
            if (!categoryFile.canRead())
            {
                Logger.getLogger(getClass()).error("cannot read categories file for user " + userid);
                return(null);
            }

            categoryList = readCategoryList(categoryFile.getAbsolutePath());

            if (categoryList!=null)
            {
                categoryTable.put(userid, categoryList);
                createIndex(categoryList, userid);

                return(categoryList);
            }
        }
        
        return(null);
    }

    Element readCategoryList(String categoryFilePath)
    {
        File categoryFile = new File(categoryFilePath);

        if ((!categoryFile.exists()) || (!categoryFile.canRead()))
        {
            return(null);
        }
        
        Document doc = null;

        FileInputStream fis = null;

        try
        {
            fis = new FileInputStream(categoryFile);
            
            InputSource inputSource = new InputSource(fis);
            
            inputSource.setEncoding("UTF-8");

            if (Logger.getLogger(getClass()).isDebugEnabled())
            {
                Logger.getLogger(getClass()).debug("reading categories from " + categoryFilePath);
            }

            doc = builder.parse(inputSource);
        }
        catch (SAXException saxex)
        {
            Logger.getLogger(getClass()).error("failed to load category file : " + categoryFilePath, saxex);
        }
        catch (IOException ioex)
        {
            Logger.getLogger(getClass()).error("failed to load category file : " + categoryFilePath, ioex);
        }
        finally 
        {
            if (fis != null)
            {
                try
                {
                    fis.close();
                }
                catch (Exception ex)
                {
                }
            }
        }
        
        return(doc.getDocumentElement());
    }

    protected void createIndex(Element categoryList, String userid)
    {
        NodeList categories = categoryList.getElementsByTagName("category");

        if (categories == null)
        {
            indexTable.remove(userid);
            return;
        }

        int listLength = categories.getLength();

        Hashtable userIndex = new Hashtable();

        for (int i = 0; i < listLength; i++)
        {
             Element category =(Element) categories.item(i);

             String categoryId = category.getAttribute("id");

             if (categoryId!=null)
             {
                 userIndex.put(categoryId, category);
             }
        }

        indexTable.put(userid,userIndex);
    }

    public void disposeCategoryList(String userid)
    {
        Boolean dirtyFlag = (Boolean) cacheDirty.get(userid);

        if ((dirtyFlag!=null) && dirtyFlag.booleanValue())
        {
            saveToFile(userid);
        }

        if (categoryTable.get(userid) != null)
        {
			Logger.getLogger(getClass()).debug("disposing category list of user " + userid);
        }

        categoryTable.remove(userid);
        indexTable.remove(userid);
    }

    public void disposeAllCategories()
    {
        saveChangedUsers();

        categoryTable = new Hashtable();
        indexTable = new Hashtable();
    }

    public Vector getCategoryIds(String userid)
    {
        Element categoryList = getCategoryList(userid);

        Vector categoryIds = null;

        if (categoryList == null)
        {
            // System.out.println("contact list for user " + userid + " does not exist!");
            return(null);
        }

        NodeList categories = categoryList.getElementsByTagName("category");

        if (categories != null)
        {
            int listLength = categories.getLength();

            for (int i=0; i < listLength;i++)
            {
                Element category =(Element) categories.item(i);

                if (categoryIds == null)
                {
                    categoryIds = new Vector();
                }

                categoryIds.add(category.getAttribute("id"));
            }
        }
        else
        {
            Logger.getLogger(getClass()).debug("no categories found for userid " + userid);
        }
    
        return(categoryIds);
    }

    public Vector getListOfCategories(String userid)
    {
        return(getListOfCategories(userid,true));
    }
    
    public Vector getListOfCategories(String userid, boolean createIfMissing)
    {
        Element categoryList = getCategoryList(userid);

        Vector listOfCategories = new Vector();

        if (categoryList==null)
        {
            Logger.getLogger(getClass()).debug("category list for user " + userid + " does not exist!");

            return(listOfCategories);

            /*
            if (createIfMissing)
            {
                return(null);
            }
            */
        }

        NodeList categories = categoryList.getElementsByTagName("category");

        if (categories != null)
        {
            int listLength = categories.getLength();
            
            for (int i=0; i < listLength; i++)
            {
                Element category = (Element) categories.item(i);

                Category newCategory = new Category(category.getAttribute("id"));

                newCategory.setName(XmlUtil.getChildText(category, "name"));

                long creationTime = 0L;
                
                String timeString = XmlUtil.getChildText(category, "creationTime");
                
                try
                {
                    creationTime=Long.parseLong(timeString);
                }
                catch (NumberFormatException nfe)
                {
                    Logger.getLogger(getClass()).warn(nfe);
                    creationTime=(new Date()).getTime();
                }

                newCategory.setCreationTime(new Date(creationTime));

                long updateTime=0L;
                timeString = XmlUtil.getChildText(category, "updateTime");
                try
                {
                    updateTime=Long.parseLong(timeString);
                }
                catch (NumberFormatException nfe)
                {
                    Logger.getLogger(getClass()).warn(nfe);
                    updateTime=(new Date()).getTime();
                }

                newCategory.setUpdateTime(new Date(updateTime));

                listOfCategories.add(newCategory);
            }
        }
    
        if (listOfCategories != null)
        {
            Collections.sort(listOfCategories, new CategoryComparator());
        }

        return(listOfCategories);
    }

    public Category getCategory(String userid, String searchedId)
    {
        Element category = getCategoryElement(userid, searchedId);

        if (category == null)
        {
            Logger.getLogger(getClass()).warn("category for user " + userid + "id " + searchedId + " does not exist!");
            return(null);
        }

        Category foundCategory = new Category(category.getAttribute("id"));

        foundCategory.setName(XmlUtil.getChildText(category, "name"));

        long creationTime=0L;
        String timeString = XmlUtil.getChildText(category, "creationTime");
        try
        {
            creationTime=Long.parseLong(timeString);
        }
        catch (NumberFormatException nfe)
        {
            Logger.getLogger(getClass()).error(nfe);
            creationTime=(new Date()).getTime();
        }

        foundCategory.setCreationTime(new Date(creationTime));

        long updateTime = 0L;
        timeString = XmlUtil.getChildText(category, "updateTime");
        try
        {
             updateTime=Long.parseLong(timeString);
        }
        catch (NumberFormatException nfe)
        {
			Logger.getLogger(getClass()).error(nfe);
            updateTime=(new Date()).getTime();
        }

        foundCategory.setUpdateTime(new Date(updateTime));

        return(foundCategory);
    }

    protected Element getCategoryElement(String userid, String searchedId)
    {
        Element categoryList = getCategoryList(userid);

        if (categoryList == null)
        {
            return(null);
        }

        Element category = null;

        Hashtable userIndex = (Hashtable) indexTable.get(userid);

        if (userIndex!=null)
        {
            category = (Element) userIndex.get(searchedId);

            if (category != null)
            {
                return(category);
            }
        }

        Logger.getLogger(getClass()).warn("category with id " + searchedId + " not found in index");

        NodeList categories = categoryList.getElementsByTagName("category");

        if (categories == null)
        {
            return(null);
        }

        int listLength = categories.getLength();

        for (int i = 0; i < listLength; i++)
        {
            category = (Element) categories.item(i);

            if (category.getAttribute("id").equals(searchedId))
            {
                return(category);
            }
        }
    
        return(null);
    }

    protected Element createCategoryList(String userid)
    {
        Logger.getLogger(getClass()).debug("creating new category list for user : " + userid);
        
        Document doc = builder.newDocument();

        Element categoryListElement = doc.createElement("categoryList");

        Element lastIdElement = doc.createElement("lastId");
        XmlUtil.setElementText(lastIdElement,"0");

        categoryListElement.appendChild(lastIdElement);
        
        doc.appendChild(categoryListElement);

        categoryTable.put(userid, categoryListElement);

        indexTable.put(userid,new Hashtable());
        
        return(categoryListElement);
    }

    public Element createCategory(String userid, Category newCategory)
    {
        Element categoryList = getCategoryList(userid);

        if (categoryList == null)
        {
            categoryList = createCategoryList(userid);
        }

        Element newElement = null;
        
        synchronized (categoryList)
        {
            Document doc = categoryList.getOwnerDocument();

            newElement = doc.createElement("category");

            newElement.appendChild(doc.createElement("name"));
            newElement.appendChild(doc.createElement("creationTime"));
            newElement.appendChild(doc.createElement("updateTime"));

            categoryList.appendChild(newElement);

            int lastId=getLastId(userid);

            lastId++;

            setLastId(userid,lastId);

            String newIdString = "" + lastId;

            newCategory.setId(newIdString);
            newElement.setAttribute("id", newIdString);
            
            Hashtable userIndex = (Hashtable) indexTable.get(userid);
            userIndex.put(newIdString,newElement);
        }

        updateCategory(userid, newCategory);

        return(newElement);
    }

    protected int getLastId(String userid)
    {
        Element categoryList = getCategoryList(userid);

        if (categoryList == null)
        {
            return(-1);
        }

        String lastIdString = XmlUtil.getChildText(categoryList, "lastId").trim();

        int lastId=0;
        try
        {
            lastId=Integer.parseInt(lastIdString);
        }
        catch (NumberFormatException nfe)
        {
            Logger.getLogger(getClass()).warn(nfe);
        }

        return(lastId);
    }

    protected void setLastId(String userid, int lastId)
    {
        Element categoryList = getCategoryList(userid);

        if (categoryList == null)
        {
            return;
        }

        XmlUtil.setChildText(categoryList, "lastId", Integer.toString(lastId));
    }

    public Element updateCategory(String userid, Category changedCategory)
    {
        Element categoryListElement = getCategoryList(userid);

        synchronized (categoryListElement)
        {
            Element categoryElement = getCategoryElement(userid, changedCategory.getId());

            if (categoryElement == null)
            {
                Logger.getLogger(getClass()).warn("updateCategory: category for user " + userid + " with id " + changedCategory.getId() +  " not found");
                return(null);
            }

            XmlUtil.setChildText(categoryElement, "name", changedCategory.getName(),true);
			XmlUtil.setChildText(categoryElement, "creationTime", "" + changedCategory.getCreationTime().getTime());
			XmlUtil.setChildText(categoryElement, "updateTime","" + changedCategory.getUpdateTime().getTime());

            cacheDirty.put(userid,new Boolean(true));
            
            return(categoryElement);
        }
    }

    public Element getCategoryElementByName(String uid, String searchedName)
    {
		Element categoryListElement = getCategoryList(uid);
		
		if (categoryListElement == null)
		{
			return(null);
		}

		synchronized (categoryListElement)
		{
			NodeList categories = categoryListElement.getElementsByTagName("category");

			if (categories == null)
			{
				return(null);
			}

			int listLength = categories.getLength();

			for (int i = 0; i < listLength; i++)
			{
				Element categoryElement = (Element) categories.item(i);
				
				String catName = XmlUtil.getChildText(categoryElement, "name");
				
				if ((catName != null) && catName.equals(searchedName))
				{
					return(categoryElement);
				}
			}
		}
		
		return(null);
    }
    
    public Category getCategoryByName(String uid, String searchedName)
    {
    	Element categoryElement = getCategoryElementByName(uid, searchedName);
    	
    	if (categoryElement == null)
    	{
    		return(null);
    	}

		Category newCategory = new Category(categoryElement.getAttribute("id"));

		newCategory.setName(XmlUtil.getChildText(categoryElement, "name"));

		long creationTime = 0L;
                
		String timeString = XmlUtil.getChildText(categoryElement, "creationTime");
                
		try
		{
			creationTime=Long.parseLong(timeString);
		}
		catch (NumberFormatException nfe)
		{
			Logger.getLogger(getClass()).warn(nfe);
			creationTime=(new Date()).getTime();
		}

		newCategory.setCreationTime(new Date(creationTime));

		long updateTime=0L;
		timeString = XmlUtil.getChildText(categoryElement, "updateTime");
		try
		{
			updateTime=Long.parseLong(timeString);
		}
		catch (NumberFormatException nfe)
		{
			Logger.getLogger(getClass()).warn(nfe);
			updateTime=(new Date()).getTime();
		}

		newCategory.setUpdateTime(new Date(updateTime));
		
		return(newCategory);
    }

    public void removeCategory(String userid, String searchedId)
    {
        Element categoryListElement = getCategoryList(userid);

        synchronized (categoryListElement)
        {
            Element categoryElement = getCategoryElement(userid,searchedId);

            if (categoryElement == null)
            {
                Logger.getLogger(getClass()).warn("category for user " + userid + " id " + searchedId + " not found");
                return;
            }

            Node categoryList = categoryElement.getParentNode();

            if (categoryList!=null)
            {
                Hashtable userIndex=(Hashtable) indexTable.get(userid);
                userIndex.remove(categoryElement.getAttribute("id"));
                
                categoryList.removeChild(categoryElement);

                cacheDirty.put(userid,new Boolean(true));
            }
        }

    }

    protected synchronized void saveToFile(String userid)
    {
        Element categoryListElement = getCategoryList(userid);

        if (categoryListElement == null)
        {
            Logger.getLogger(getClass()).warn("category list for user " + userid + " does not exist");
            return;
        }

        if (Logger.getLogger(getClass()).isDebugEnabled())
        {
            Logger.getLogger(getClass()).debug("saving categories for user " + userid);
        }
        
        synchronized (categoryListElement)
        {
            String xmlFileName = categoryPath + File.separator + userid + ".xml";

            OutputStreamWriter xmlOutFile = null;

            try
            {
                FileOutputStream fos = new FileOutputStream(xmlFileName);
                
                xmlOutFile = new OutputStreamWriter(fos, "UTF-8");
                
                XmlUtil.writeToStream(categoryListElement, xmlOutFile);
                
                xmlOutFile.flush();
            }
            catch (IOException io1)
            {
				Logger.getLogger(getClass()).error("error saving category file " + xmlFileName, io1);
            }
            finally
            {
                if (xmlOutFile != null)
                {
                    try 
                    {
                        xmlOutFile.close();
                    }
                    catch (Exception ex) 
                    {
                    }
                }
            }
        }
    }

    public synchronized void saveChangedUsers()
    {
        Enumeration cacheUserList=cacheDirty.keys();

        while (cacheUserList.hasMoreElements())
        {
            String userid=(String) cacheUserList.nextElement();

            boolean dirtyFlag=((Boolean) cacheDirty.get(userid)).booleanValue();

            if (dirtyFlag)
            {
                saveToFile(userid);
                cacheDirty.put(userid,new Boolean(false));
            }
        }
    }

	public synchronized void run()
	{
		boolean stop = false;

		while (!stop)
		{
			try
			{
				this.wait(120000);

				saveChangedUsers();
			}
			catch (InterruptedException e)
			{
				saveChangedUsers();
				
				stop = true;
			}
		}
	}

	static public void main(String args[])
	{
		/*
		CategoryManager mgr = CategoryManager.getInstance();
	
	    for (int i=0; i < 20; i++)
	    {
			Category newCategory = new Category();
	    
			newCategory.setName("Testkategorie-" + i);
	
			mgr.createCategory("testuser", newCategory);	
			
			System.out.println("created category" + newCategory.getName());
	    }

		System.out.println("categories of user testuser:");

        Vector userCategories = mgr.getListOfCategories("testuser");
        
        for (int i = 0; i < userCategories.size(); i++)
        {
        	Category cat = (Category) userCategories.elementAt(i);
        	
			System.out.println("  " + cat.getName());
        }
	    
	    while (true)
	    {
	    	try
	    	{
	    		sleep(1000);
	    	}
	    	catch (InterruptedException iex)
	    	{
	    	}
	    }
	    */
	}

}
