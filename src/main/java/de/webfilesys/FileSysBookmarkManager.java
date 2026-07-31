package de.webfilesys;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.webfilesys.util.XmlUtil;

public class FileSysBookmarkManager extends Thread {

    private static final Logger LOG = LogManager.getLogger(FileSysBookmarkManager.class);

    public static final String BOOKMARK_DIR = "bookmarks";
	
    HashMap<String, Element> bookmarkTable;

    HashMap<String, HashMap<String, Element>> indexTable;

    HashMap<String, Boolean> cacheDirty;
    
    DocumentBuilder builder;
    
    boolean shutdownFlag;

    private static FileSysBookmarkManager bookmarkManager = null;
    
    private final String bookmarkPath;
    
    private FileSysBookmarkManager() {
    	bookmarkPath = WebFileSys.getInstance().getConfigBaseDir() + "/" + BOOKMARK_DIR;
        bookmarkTable = new HashMap<>();
        indexTable = new HashMap<>();
        cacheDirty = new HashMap<>();
        shutdownFlag = false;
        builder = null;

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException pcex) {
            LogManager.getLogger(getClass()).error(pcex);
        }

        this.start();
    }

    public static FileSysBookmarkManager getInstance() {
        if (bookmarkManager == null) {
            bookmarkManager = new FileSysBookmarkManager();
        }
        return bookmarkManager;
    }

    public Element getBookmarkList(String userid) {
        Element bookmarkList = bookmarkTable.get(userid);
        if (bookmarkList != null) {
            return bookmarkList;
        }
    
        String bookmarkFileName = bookmarkPath + File.separator + userid + ".xml";
        File bookmarkFile = new File(bookmarkFileName);
        if (bookmarkFile.exists() && bookmarkFile.isFile()) {
            if (!bookmarkFile.canRead()) {
                LogManager.getLogger(getClass()).error("cannot read bookmark file for user " + userid);
                return null;
            }
            bookmarkList = readBookmarkList(bookmarkFile.getAbsolutePath());
            if (bookmarkList != null) {
                bookmarkTable.put(userid, bookmarkList);
                createIndex(bookmarkList, userid);
                return bookmarkList;
            }
        }
        return null;
    }

    Element readBookmarkList(String bookmarkFilePath) {
        File bookmarkFile = new File(bookmarkFilePath);
        if ((!bookmarkFile.exists()) || (!bookmarkFile.canRead())) {
            return(null);
        }
        Document doc = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(bookmarkFile);
            InputSource inputSource = new InputSource(fis);
            inputSource.setEncoding("UTF-8");
            if (LogManager.getLogger(getClass()).isDebugEnabled()) {
                LogManager.getLogger(getClass()).debug("reading bookmarks from " + bookmarkFilePath);
            }
            doc = builder.parse(inputSource);
        } catch (SAXException | IOException saxex) {
            LogManager.getLogger(getClass()).error("failed to load category file : " + bookmarkFilePath, saxex);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception ex) {
                }
            }
        }
        return doc.getDocumentElement();
    }

    protected void createIndex(Element bookmarkList, String userid) {
        NodeList bookmarks = bookmarkList.getElementsByTagName("bookmark");
        int listLength = bookmarks.getLength();
        HashMap<String, Element> userIndex = new HashMap<String, Element>();
        for (int i = 0; i < listLength; i++) {
             Element bookmark = (Element) bookmarks.item(i);
             String bookmarkId = bookmark.getAttribute("id");
             if (bookmarkId.isEmpty()) {
                 userIndex.put(bookmarkId, bookmark);
             }
        }
        indexTable.put(userid, userIndex);
    }

    public ArrayList<FileSysBookmark> getListOfBookmarks(String userid) {
        Element bookmarkList = getBookmarkList(userid);
        ArrayList<FileSysBookmark> listOfBookmarks = new ArrayList<FileSysBookmark>();
        if (bookmarkList == null) {
            LOG.debug("bookmark list for user " + userid + " does not exist!");
            return listOfBookmarks;
        }
        NodeList bookmarks = bookmarkList.getElementsByTagName("bookmark");
        int listLength = bookmarks.getLength();
        if (listLength > 0) {
            for (int i = 0; i < listLength; i++) {
                Element bookmark = (Element) bookmarks.item(i);
                FileSysBookmark newBookmark = new FileSysBookmark(bookmark.getAttribute("id"));
                newBookmark.setName(XmlUtil.getChildText(bookmark, "name"));
                newBookmark.setPath(XmlUtil.getChildText(bookmark, "path"));
                long creationTime;
                String timeString = XmlUtil.getChildText(bookmark, "creationTime");
                try {
                    creationTime=Long.parseLong(timeString);
                } catch (NumberFormatException nfe) {
                    LOG.warn(nfe);
                    creationTime=(new Date()).getTime();
                }
                newBookmark.setCreationTime(new Date(creationTime));
                long updateTime;
                timeString = XmlUtil.getChildText(bookmark, "updateTime");
                try {
                    updateTime=Long.parseLong(timeString);
                } catch (NumberFormatException nfe) {
                    LOG.warn(nfe);
                    updateTime=(new Date()).getTime();
                }
                newBookmark.setUpdateTime(new Date(updateTime));
                listOfBookmarks.add(newBookmark);
            }
        }
        Collections.sort(listOfBookmarks, new FileSysBookmarkComparator());
        return listOfBookmarks;
    }

    protected Element getBookmarkElement(String userid, String searchedId) {
        Element bookmarkList = getBookmarkList(userid);
        if (bookmarkList == null) {
            return(null);
        }
        Element bookmark;
        HashMap<String, Element> userIndex = indexTable.get(userid);

        if (userIndex != null) {
            bookmark = userIndex.get(searchedId);
            if (bookmark != null) {
                return(bookmark);
            }
        }
        LOG.warn("bookmark with id " + searchedId + " not found in index");
        NodeList bookmarks = bookmarkList.getElementsByTagName("bookmark");
        int listLength = bookmarks.getLength();
        if (listLength == 0) {
            return(null);
        }
        for (int i = 0; i < listLength; i++) {
            bookmark = (Element) bookmarks.item(i);
            if (bookmark.getAttribute("id").equals(searchedId)) {
                return(bookmark);
            }
        }
        return null;
    }

    protected Element createBookmarkList(String userid) {
        LOG.debug("creating new bookmark list for user : " + userid);
        Document doc = builder.newDocument();
        Element bookmarkListElement = doc.createElement("bookmarkList");
        Element lastIdElement = doc.createElement("lastId");
        XmlUtil.setElementText(lastIdElement,"0");
        bookmarkListElement.appendChild(lastIdElement);
        doc.appendChild(bookmarkListElement);
        bookmarkTable.put(userid, bookmarkListElement);
        indexTable.put(userid, new HashMap<>());
        return bookmarkListElement;
    }

    public void createBookmark(String userid, FileSysBookmark newBookmark) {
        Element bookmarkList = getBookmarkList(userid);
        if (bookmarkList == null) {
            bookmarkList = createBookmarkList(userid);
        }
        Element newElement;
        synchronized (bookmarkList) {
            Document doc = bookmarkList.getOwnerDocument();
            newElement = doc.createElement("bookmark");
            newElement.appendChild(doc.createElement("name"));
            newElement.appendChild(doc.createElement("path"));
            newElement.appendChild(doc.createElement("creationTime"));
            newElement.appendChild(doc.createElement("updateTime"));
            bookmarkList.appendChild(newElement);
            int lastId = getLastId(userid);
            lastId++;
            setLastId(userid, lastId);
            String newIdString = Integer.toString(lastId);
            newBookmark.setId(newIdString);
            newElement.setAttribute("id", newIdString);
            HashMap<String, Element> userIndex = indexTable.get(userid);
            userIndex.put(newIdString, newElement);
        }
        updateBookmark(userid, newBookmark);
    }

    protected int getLastId(String userid) {
        Element bookmarkList = getBookmarkList(userid);
        if (bookmarkList == null) {
            return(-1);
        }
        String lastIdString = XmlUtil.getChildText(bookmarkList, "lastId").trim();
        int lastId = 0;
        try {
            lastId = Integer.parseInt(lastIdString);
        } catch (NumberFormatException nfe) {
            LOG.warn(nfe);
        }
        return lastId;
    }

    protected void setLastId(String userid, int lastId) {
        Element bookmarkList = getBookmarkList(userid);
        if (bookmarkList == null) {
            return;
        }
        XmlUtil.setChildText(bookmarkList, "lastId", Integer.toString(lastId));
    }

    public void updateBookmark(String userid, FileSysBookmark changedBookmark) {
        Element bookmarkListElement = getBookmarkList(userid);
        synchronized (bookmarkListElement) {
            Element bookmarkElement = getBookmarkElement(userid, changedBookmark.getId());
            if (bookmarkElement == null) {
                LOG.warn("updateBookmark: bookmark for user " + userid + " with id " + changedBookmark.getId() +  " not found");
                return;
            }
            XmlUtil.setChildText(bookmarkElement, "name", changedBookmark.getName(), true);
            XmlUtil.setChildText(bookmarkElement, "path", changedBookmark.getPath(), true);
			XmlUtil.setChildText(bookmarkElement, "creationTime", "" + changedBookmark.getCreationTime().getTime());
			XmlUtil.setChildText(bookmarkElement, "updateTime", "" + changedBookmark.getUpdateTime().getTime());
            cacheDirty.put(userid, new Boolean(true));
        }
    }

    public Element getBookmarkElementByName(String uid, String searchedName) {
		Element bookmarkListElement = getBookmarkList(uid);
        if (bookmarkListElement == null) {
			return(null);
		}
		synchronized (bookmarkListElement) {
			NodeList bookmarks = bookmarkListElement.getElementsByTagName("bookmark");
            int listLength = bookmarks.getLength();
			if (listLength == 0) {
				return null;
			}
			for (int i = 0; i < listLength; i++) {
				Element bookmarkElement = (Element) bookmarks.item(i);
				String bookmarkName = XmlUtil.getChildText(bookmarkElement, "name");
				if (bookmarkName.equals(searchedName)) {
					return(bookmarkElement);
				}
			}
		}
		return null;
    }

    public void removeBookmark(String userid, String searchedId) {
        Element bookmarkListElement = getBookmarkList(userid);
        synchronized (bookmarkListElement) {
            Element bookmarkElement = getBookmarkElement(userid, searchedId);
            if (bookmarkElement == null) {
                LOG.warn("bookmark for user " + userid + " id " + searchedId + " not found");
                return;
            }
            Node bookmarkList = bookmarkElement.getParentNode();
            if (bookmarkList != null) {
                HashMap<String, Element> userIndex = indexTable.get(userid);
                userIndex.remove(bookmarkElement.getAttribute("id"));
                bookmarkList.removeChild(bookmarkElement);
                cacheDirty.put(userid, new Boolean(true));
            }
        }
    }

    protected synchronized void saveToFile(String userid) {
        Element bookmarkListElement = getBookmarkList(userid);
        if (bookmarkListElement == null) {
            LOG.warn("bookmark list for user " + userid + " does not exist");
            return;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("saving bookmarks for user " + userid);
        }
        synchronized (bookmarkListElement) {
            String xmlFileName = bookmarkPath + File.separator + userid + ".xml";
            OutputStreamWriter xmlOutFile = null;
            try {
                FileOutputStream fos = new FileOutputStream(xmlFileName);
                xmlOutFile = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                XmlUtil.writeToStream(bookmarkListElement, xmlOutFile);
                xmlOutFile.flush();
            } catch (IOException io1) {
                LOG.error("error saving bookmark file " + xmlFileName, io1);
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
        Set<String> cacheUserList = cacheDirty.keySet();
        for (String userid : cacheUserList) {
            boolean dirtyFlag = cacheDirty.get(userid).booleanValue();
            if (dirtyFlag) {
                saveToFile(userid);
                cacheDirty.put(userid, Boolean.FALSE);
            }
        }
    }

    public void deleteUser(String userid) {
        bookmarkTable.remove(userid);
        indexTable.remove(userid);
        String bookmarkFileName = bookmarkPath + File.separator + userid + ".xml";
        File bookmarkFile = new File(bookmarkFileName);
        if (!bookmarkFile.exists() || !bookmarkFile.isFile()) {
            return;
        }
        if (bookmarkFile.delete()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("bookmark file deleted for user " + userid);
            } else {
                LOG.warn("failed to delete bookmark file for user " + userid);
            }
        }
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
			}
		}
	}
	
}
