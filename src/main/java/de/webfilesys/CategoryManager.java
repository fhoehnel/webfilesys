package de.webfilesys;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import de.webfilesys.util.CommonUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.webfilesys.util.XmlUtil;

public class CategoryManager extends Thread {
    private static final Logger LOG = LogManager.getLogger(CategoryManager.class);
    
    public static final String CATEGORIES_DIR    = "categories";
	
    HashMap<String, Element> userToCategoryListMap;

    HashMap<String, HashMap<String, Element>> indexTable;

    HashMap<String, Boolean> cacheDirty;
    
    DocumentBuilder builder;
    
    String categoryFileName = null;
    
    boolean shutdownFlag;

    private static CategoryManager categoryManager = null;
    
    private final String categoryPath;
    
    private CategoryManager() {
    	categoryPath = WebFileSys.getInstance().getConfigBaseDir() + "/" + CATEGORIES_DIR;
        userToCategoryListMap = new HashMap<>();
        indexTable = new HashMap<>();
        cacheDirty = new HashMap<>();
        shutdownFlag = false;
        builder = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException pcex) {
            LOG.error(pcex);
        }
        this.start();
    }

    public static CategoryManager getInstance() {
        if (categoryManager == null) {
            categoryManager = new CategoryManager();
        }
        return categoryManager;
    }

    public Element getCategoryList(String userid) {
        Element categoryList = userToCategoryListMap.get(userid);
        if (categoryList != null) {
            return categoryList;
        }
        categoryFileName = categoryPath + File.separator + userid + ".xml";
        File categoryFile = new File(categoryFileName);
        if (categoryFile.exists() && categoryFile.isFile()) {
            if (!categoryFile.canRead()) {
                LOG.error("cannot read categories file for user {}", userid);
                return null;
            }
            categoryList = readCategoryList(categoryFile.getAbsolutePath());
            if (categoryList != null) {
                userToCategoryListMap.put(userid, categoryList);
                createIndex(categoryList, userid);
                return categoryList;
            }
        }
        return null;
    }

    Element readCategoryList(String categoryFilePath) {
        File categoryFile = new File(categoryFilePath);
        if (!categoryFile.exists() || !categoryFile.canRead()) {
            return null;
        }
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(categoryFile);
            InputSource inputSource = new InputSource(fis);
            inputSource.setEncoding("UTF-8");
            if (LOG.isDebugEnabled()) {
                LOG.debug("reading categories from " + categoryFilePath);
            }
            Document doc = builder.parse(inputSource);
            return doc.getDocumentElement();
        } catch (SAXException | IOException saxex) {
            LOG.error("failed to load category file : {}", categoryFilePath, saxex);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception ex) {
                }
            }
        }
        return null;
    }

    protected void createIndex(Element categoryList, String userid) {
        NodeList categories = categoryList.getElementsByTagName("category");
        int listLength = categories.getLength();
        HashMap<String, Element> indexOfUser = new HashMap<>();
        for (int i = 0; i < listLength; i++) {
             Element category = (Element) categories.item(i);
             String categoryId = category.getAttribute("id");
             if (!CommonUtils.isEmpty(categoryId)) {
                 indexOfUser.put(categoryId, category);
             }
        }
       indexTable.put(userid, indexOfUser);
    }

    public void disposeCategoryList(String userid) {
        Boolean dirtyFlag = cacheDirty.get(userid);
        if ((dirtyFlag != null) && dirtyFlag) {
            saveToFile(userid);
        }
        if (userToCategoryListMap.get(userid) != null) {
            LOG.debug("disposing category list of user {}", userid);
        }
        userToCategoryListMap.remove(userid);
        indexTable.remove(userid);
    }

    public void disposeAllCategories() {
        saveChangedUsers();
        userToCategoryListMap = new HashMap<>();
        indexTable = new HashMap<>();
    }

    public ArrayList<String> getCategoryIds(String userid) {
        Element categoryList = getCategoryList(userid);
        ArrayList<String> categoryIds = null;
        if (categoryList == null) {
            return null;
        }
        NodeList categories = categoryList.getElementsByTagName("category");
        int listLength = categories.getLength();
        for (int i = 0; i < listLength; i++) {
            Element category =(Element) categories.item(i);
            if (categoryIds == null) {
                categoryIds = new ArrayList<String>();
            }
            categoryIds.add(category.getAttribute("id"));
        }
        return categoryIds;
    }

    public ArrayList<Category> getListOfCategories(String userid) {
        Element categoryList = getCategoryList(userid);
        ArrayList<Category> listOfCategories = new ArrayList<>();
        if (categoryList == null) {
            LOG.debug("category list for user " + userid + " does not exist!");
            return listOfCategories;
        }
        NodeList categories = categoryList.getElementsByTagName("category");
        int listLength = categories.getLength();
        for (int i = 0; i < listLength; i++) {
            Element category = (Element) categories.item(i);
            Category newCategory = new Category(category.getAttribute("id"));
            newCategory.setName(XmlUtil.getChildText(category, "name"));
            long creationTime;
            String timeString = XmlUtil.getChildText(category, "creationTime");
            try {
                creationTime=Long.parseLong(timeString);
            } catch (NumberFormatException nfe) {
                LOG.warn(nfe);
                creationTime = (new Date()).getTime();
            }
            newCategory.setCreationTime(new Date(creationTime));
            long updateTime;
            timeString = XmlUtil.getChildText(category, "updateTime");
            try {
                updateTime=Long.parseLong(timeString);
            }
            catch (NumberFormatException nfe) {
                LOG.warn(nfe);
                updateTime=(new Date()).getTime();
            }
            newCategory.setUpdateTime(new Date(updateTime));
            listOfCategories.add(newCategory);
        }
        if (listOfCategories.size() > 1) {
            Collections.sort(listOfCategories, new CategoryComparator());
        }
        return listOfCategories;
    }

    public Category getCategory(String userid, String searchedId) {
        Element category = getCategoryElement(userid, searchedId);
        if (category == null) {
            LOG.warn("category for user {}id {} does not exist!", userid, searchedId);
            return null;
        }
        Category foundCategory = new Category(category.getAttribute("id"));
        foundCategory.setName(XmlUtil.getChildText(category, "name"));
        long creationTime;
        String timeString = XmlUtil.getChildText(category, "creationTime");
        try {
            creationTime = Long.parseLong(timeString);
        } catch (NumberFormatException nfe) {
            LOG.error(nfe);
            creationTime = (new Date()).getTime();
        }
        foundCategory.setCreationTime(new Date(creationTime));
        long updateTime;
        timeString = XmlUtil.getChildText(category, "updateTime");
        try {
             updateTime = Long.parseLong(timeString);
        }
        catch (NumberFormatException nfe) {
			LOG.error(nfe);
            updateTime = (new Date()).getTime();
        }
        foundCategory.setUpdateTime(new Date(updateTime));
        return foundCategory;
    }

    protected Element getCategoryElement(String userid, String searchedId) {
        Element categoryList = getCategoryList(userid);
        if (categoryList == null) {
            return null;
        }
        Element category;
        HashMap<String, Element> userIndex = indexTable.get(userid);
        if (userIndex != null) {
            category = userIndex.get(searchedId);
            if (category != null) {
                return(category);
            }
        }
        LOG.warn("category with id {} not found in index", searchedId);
        NodeList categories = categoryList.getElementsByTagName("category");
        int listLength = categories.getLength();
        for (int i = 0; i < listLength; i++) {
            category = (Element) categories.item(i);
            if (category.getAttribute("id").equals(searchedId)) {
                return(category);
            }
        }
        return(null);
    }

    protected Element createCategoryList(String userid) {
        LOG.debug("creating new category list for user : " + userid);
        Document doc = builder.newDocument();
        Element categoryListElement = doc.createElement("categoryList");
        Element lastIdElement = doc.createElement("lastId");
        XmlUtil.setElementText(lastIdElement,"0");
        categoryListElement.appendChild(lastIdElement);
        doc.appendChild(categoryListElement);
        userToCategoryListMap.put(userid, categoryListElement);
        indexTable.put(userid, new HashMap<>());
        return categoryListElement;
    }

    public Element createCategory(String userid, Category newCategory) {
        Element categoryList = getCategoryList(userid);
        if (categoryList == null) {
            categoryList = createCategoryList(userid);
        }
        Element newElement;
        synchronized (categoryList) {
            Document doc = categoryList.getOwnerDocument();
            newElement = doc.createElement("category");
            newElement.appendChild(doc.createElement("name"));
            newElement.appendChild(doc.createElement("creationTime"));
            newElement.appendChild(doc.createElement("updateTime"));
            categoryList.appendChild(newElement);
            int lastId = getLastId(userid);
            lastId++;
            setLastId(userid, lastId);
            String newIdString = "" + lastId;
            newCategory.setId(newIdString);
            newElement.setAttribute("id", newIdString);
            HashMap<String, Element> userIndex = indexTable.get(userid);
            userIndex.put(newIdString,newElement);
        }
        updateCategory(userid, newCategory);
        return newElement;
    }

    protected int getLastId(String userid) {
        Element categoryList = getCategoryList(userid);
        if (categoryList == null) {
            return -1;
        }
        String lastIdString = XmlUtil.getChildText(categoryList, "lastId").trim();
        int lastId = 0;
        try {
            lastId = Integer.parseInt(lastIdString);
        } catch (NumberFormatException nfe) {
            LOG.warn(nfe);
        }
        return(lastId);
    }

    protected void setLastId(String userid, int lastId) {
        Element categoryList = getCategoryList(userid);
        if (categoryList == null) {
            return;
        }
        XmlUtil.setChildText(categoryList, "lastId", Integer.toString(lastId));
    }

    public Element updateCategory(String userid, Category changedCategory) {
        Element categoryListElement = getCategoryList(userid);
        synchronized (categoryListElement) {
            Element categoryElement = getCategoryElement(userid, changedCategory.getId());
            if (categoryElement == null) {
                LOG.warn("updateCategory: category for user {} with id {} not found", userid, changedCategory.getId());
                return null;
            }
            XmlUtil.setChildText(categoryElement, "name", changedCategory.getName(),true);
			XmlUtil.setChildText(categoryElement, "creationTime", "" + changedCategory.getCreationTime().getTime());
			XmlUtil.setChildText(categoryElement, "updateTime","" + changedCategory.getUpdateTime().getTime());
            cacheDirty.put(userid, Boolean.TRUE);
            return categoryElement;
        }
    }

    public Element getCategoryElementByName(String uid, String searchedName) {
		Element categoryListElement = getCategoryList(uid);
        if (categoryListElement == null) {
			return(null);
		}
		synchronized (categoryListElement) {
			NodeList categories = categoryListElement.getElementsByTagName("category");
            int listLength = categories.getLength();
			for (int i = 0; i < listLength; i++) {
				Element categoryElement = (Element) categories.item(i);
				String catName = XmlUtil.getChildText(categoryElement, "name");
				if (catName.equals(searchedName)) {
					return(categoryElement);
				}
			}
		}
		return null;
    }
    
    public Category getCategoryByName(String uid, String searchedName) {
    	Element categoryElement = getCategoryElementByName(uid, searchedName);
    	if (categoryElement == null) {
    		return(null);
    	}
        Category newCategory = new Category(categoryElement.getAttribute("id"));
		newCategory.setName(XmlUtil.getChildText(categoryElement, "name"));
		long creationTime;
		String timeString = XmlUtil.getChildText(categoryElement, "creationTime");
		try {
			creationTime = Long.parseLong(timeString);
		} catch (NumberFormatException nfe) {
			LOG.warn(nfe);
			creationTime = (new Date()).getTime();
		}
		newCategory.setCreationTime(new Date(creationTime));
		long updateTime;
		timeString = XmlUtil.getChildText(categoryElement, "updateTime");
		try {
			updateTime=Long.parseLong(timeString);
		} catch (NumberFormatException nfe) {
			LOG.warn(nfe);
			updateTime=(new Date()).getTime();
		}
		newCategory.setUpdateTime(new Date(updateTime));
		return(newCategory);
    }

    public void removeCategory(String userid, String searchedId) {
        Element categoryListElement = getCategoryList(userid);
        synchronized (categoryListElement) {
            Element categoryElement = getCategoryElement(userid, searchedId);
            if (categoryElement == null) {
                LOG.warn("category for user {} id {} not found", userid, searchedId);
                return;
            }
            Node categoryList = categoryElement.getParentNode();
            if (categoryList != null) {
                HashMap<String, Element> userIndex = indexTable.get(userid);
                userIndex.remove(categoryElement.getAttribute("id"));
                categoryList.removeChild(categoryElement);
                cacheDirty.put(userid, Boolean.TRUE);
            }
        }
    }

    protected synchronized void saveToFile(String userid) {
        Element categoryListElement = getCategoryList(userid);
        if (categoryListElement == null) {
            LOG.warn("category list for user " + userid + " does not exist");
            return;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("saving categories for user " + userid);
        }
        synchronized (categoryListElement) {
            String xmlFileName = categoryPath + File.separator + userid + ".xml";
            OutputStreamWriter xmlOutFile = null;
            try {
                FileOutputStream fos = new FileOutputStream(xmlFileName);
                xmlOutFile = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                XmlUtil.writeToStream(categoryListElement, xmlOutFile);
                xmlOutFile.flush();
            } catch (IOException io1) {
                LOG.error("error saving category file {}", xmlFileName, io1);
            } finally {
                if (xmlOutFile != null) {
                    try {
                        xmlOutFile.close();
                    } catch (Exception ex) {
                    }
                }
            }
        }
    }

    public synchronized void saveChangedUsers() {
        cacheDirty.forEach((key, value) -> {
            if (value) {
                saveToFile(key);
            }
        });
        cacheDirty.keySet().forEach((userid) -> {
            cacheDirty.put(userid, false);
        });
    }

	public synchronized void run() {
		boolean stop = false;
		while (!stop) {
			try {
				this.wait(120000);
				saveChangedUsers();
			} catch (InterruptedException e) {
				saveChangedUsers();
				stop = true;
                LOG.info("CategoryManager ready for shutdown");
			}
		}
	}

}
