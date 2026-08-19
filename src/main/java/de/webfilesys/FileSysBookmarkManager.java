package de.webfilesys;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import de.webfilesys.util.XmlUtil;

public class FileSysBookmarkManager extends Thread {

    private static final Logger LOG = LogManager.getLogger(FileSysBookmarkManager.class);

    public static final String BOOKMARK_DIR = "bookmarks";

    /**
     * Each entry in the bookmarkCache is a UserBookmarks object that contains the last assigned id and a list of bookmarks for a single user. The key is the userid.
     */
    HashMap<String, UserBookmarks> bookmarkCache;

    HashMap<String, HashMap<String, FileSysBookmark>> indexTable;

    HashMap<String, Boolean> cacheDirty;

    Gson gson;

    boolean shutdownFlag;

    private static FileSysBookmarkManager bookmarkManager = null;

    private final String bookmarkPath;

    /**
     * Holds all bookmarks of a single user together with the last assigned id.
     * This is the object graph that is serialized to / deserialized from JSON.
     */
    static class UserBookmarks {
        int lastId;
        ArrayList<FileSysBookmark> bookmarks;

        UserBookmarks() {
            lastId = 0;
            bookmarks = new ArrayList<FileSysBookmark>();
        }
    }

    private FileSysBookmarkManager() {
    	bookmarkPath = WebFileSys.getInstance().getConfigBaseDir() + "/" + BOOKMARK_DIR;
        bookmarkCache = new HashMap<>();
        indexTable = new HashMap<>();
        cacheDirty = new HashMap<>();
        shutdownFlag = false;

        gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, new JsonSerializer<Date>() {
                    public JsonElement serialize(Date date, Type type, JsonSerializationContext context) {
                        return new JsonPrimitive(date.getTime());
                    }
                })
                .registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                    public Date deserialize(JsonElement json, Type type, JsonDeserializationContext context)
                            throws JsonParseException {
                        return new Date(json.getAsLong());
                    }
                })
                .setPrettyPrinting()
                .create();

        migrateXmlBookmarks();

        this.start();
    }

    public static FileSysBookmarkManager getInstance() {
        if (bookmarkManager == null) {
            bookmarkManager = new FileSysBookmarkManager();
        }
        return bookmarkManager;
    }

    /**
     * One-time migration of legacy XML bookmark files to the JSON format.
     * For every {@code <userid>.xml} file for which no {@code <userid>.json}
     * file exists yet, the XML content is converted to a JSON file. The
     * original XML file is renamed to {@code <userid>.xml.migrated} as a
     * backup and to prevent it from being migrated again.
     */
    protected void migrateXmlBookmarks() {
        File bookmarkDir = new File(bookmarkPath);
        if (!bookmarkDir.exists() || !bookmarkDir.isDirectory()) {
            return;
        }
        File[] xmlFiles = bookmarkDir.listFiles(new java.io.FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".xml");
            }
        });
        if (xmlFiles == null) {
            return;
        }
        for (File xmlFile : xmlFiles) {
            String fileName = xmlFile.getName();
            String userid = fileName.substring(0, fileName.length() - ".xml".length());
            File jsonFile = new File(bookmarkPath + File.separator + userid + ".json");
            if (jsonFile.exists()) {
                continue;
            }
            LOG.info("migrating bookmark file to JSON for user " + userid);
            UserBookmarks userBookmarks = readBookmarkListFromXml(xmlFile.getAbsolutePath());
            if (userBookmarks == null) {
                LOG.warn("failed to migrate bookmark file " + xmlFile.getAbsolutePath());
                continue;
            }
            if (writeBookmarkListToJson(userid, userBookmarks)) {
                File backupFile = new File(xmlFile.getAbsolutePath() + ".migrated");
                if (!xmlFile.renameTo(backupFile)) {
                    LOG.warn("could not rename migrated bookmark file " + xmlFile.getAbsolutePath());
                }
            }
        }
    }

    /**
     * Reads a legacy XML bookmark file and converts it to the internal model.
     */
    protected UserBookmarks readBookmarkListFromXml(String xmlFilePath) {
        File xmlFile = new File(xmlFilePath);
        if ((!xmlFile.exists()) || (!xmlFile.canRead())) {
            return (null);
        }
        Element bookmarkListElement = null;
        FileInputStream fis = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            fis = new FileInputStream(xmlFile);
            InputSource inputSource = new InputSource(fis);
            inputSource.setEncoding("UTF-8");
            bookmarkListElement = builder.parse(inputSource).getDocumentElement();
        } catch (Exception ex) {
            LOG.error("failed to parse legacy bookmark file : " + xmlFilePath, ex);
            return null;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception ex) {
                }
            }
        }
        if (bookmarkListElement == null) {
            return null;
        }
        UserBookmarks userBookmarks = new UserBookmarks();
        String lastIdString = XmlUtil.getChildText(bookmarkListElement, "lastId");
        if (lastIdString != null) {
            try {
                userBookmarks.lastId = Integer.parseInt(lastIdString.trim());
            } catch (NumberFormatException nfe) {
                LOG.warn(nfe);
            }
        }
        NodeList bookmarks = bookmarkListElement.getElementsByTagName("bookmark");
        int listLength = bookmarks.getLength();
        for (int i = 0; i < listLength; i++) {
            Element bookmark = (Element) bookmarks.item(i);
            FileSysBookmark newBookmark = new FileSysBookmark(bookmark.getAttribute("id"));
            newBookmark.setName(XmlUtil.getChildText(bookmark, "name"));
            newBookmark.setPath(XmlUtil.getChildText(bookmark, "path"));
            newBookmark.setCreationTime(new Date(parseTime(XmlUtil.getChildText(bookmark, "creationTime"))));
            newBookmark.setUpdateTime(new Date(parseTime(XmlUtil.getChildText(bookmark, "updateTime"))));
            userBookmarks.bookmarks.add(newBookmark);
        }
        return userBookmarks;
    }

    private long parseTime(String timeString) {
        try {
            return Long.parseLong(timeString);
        } catch (NumberFormatException nfe) {
            LOG.warn(nfe);
            return (new Date()).getTime();
        }
    }

    protected boolean writeBookmarkListToJson(String userid, UserBookmarks userBookmarks) {
        String jsonFileName = bookmarkPath + File.separator + userid + ".json";
        OutputStreamWriter jsonOutFile = null;
        try {
            FileOutputStream fos = new FileOutputStream(jsonFileName);
            jsonOutFile = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
            gson.toJson(userBookmarks, jsonOutFile);
            jsonOutFile.flush();
            return true;
        } catch (IOException io1) {
            LOG.error("error saving bookmark file " + jsonFileName, io1);
            return false;
        } finally {
            if (jsonOutFile != null) {
                try {
                    jsonOutFile.close();
                } catch (Exception ex) {
                }
            }
        }
    }

    protected UserBookmarks getUserBookmarks(String userid) {
        UserBookmarks userBookmarks = bookmarkCache.get(userid);
        if (userBookmarks != null) {
            return userBookmarks;
        }

        String bookmarkFileName = bookmarkPath + File.separator + userid + ".json";
        File bookmarkFile = new File(bookmarkFileName);
        if (bookmarkFile.exists() && bookmarkFile.isFile()) {
            if (!bookmarkFile.canRead()) {
                LOG.error("cannot read bookmark file for user " + userid);
                return null;
            }
            userBookmarks = readBookmarkList(bookmarkFile.getAbsolutePath());
            if (userBookmarks != null) {
                bookmarkCache.put(userid, userBookmarks);
                createIndex(userBookmarks, userid);
                return userBookmarks;
            }
        }
        return null;
    }

    UserBookmarks readBookmarkList(String bookmarkFilePath) {
        File bookmarkFile = new File(bookmarkFilePath);
        if ((!bookmarkFile.exists()) || (!bookmarkFile.canRead())) {
            return (null);
        }
        UserBookmarks userBookmarks = null;
        FileInputStream fis = null;
        InputStreamReader reader = null;
        try {
            fis = new FileInputStream(bookmarkFile);
            reader = new InputStreamReader(fis, StandardCharsets.UTF_8);
            if (LOG.isDebugEnabled()) {
                LOG.debug("reading bookmarks from " + bookmarkFilePath);
            }
            userBookmarks = gson.fromJson(reader, UserBookmarks.class);
        } catch (Exception ex) {
            LOG.error("failed to load bookmark file : " + bookmarkFilePath, ex);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ex) {
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception ex) {
                }
            }
        }
        if (userBookmarks == null) {
            return null;
        }
        if (userBookmarks.bookmarks == null) {
            userBookmarks.bookmarks = new ArrayList<FileSysBookmark>();
        }
        return userBookmarks;
    }

    protected void createIndex(UserBookmarks userBookmarks, String userid) {
        HashMap<String, FileSysBookmark> userIndex = new HashMap<String, FileSysBookmark>();
        for (FileSysBookmark bookmark : userBookmarks.bookmarks) {
            String bookmarkId = bookmark.getId();
            if ((bookmarkId != null) && (!bookmarkId.isEmpty())) {
                userIndex.put(bookmarkId, bookmark);
            }
        }
        indexTable.put(userid, userIndex);
    }

    public ArrayList<FileSysBookmark> getListOfBookmarks(String userid) {
        ArrayList<FileSysBookmark> listOfBookmarks = new ArrayList<FileSysBookmark>();
        UserBookmarks userBookmarks = getUserBookmarks(userid);
        if (userBookmarks == null) {
            LOG.debug("bookmark list for user " + userid + " does not exist!");
            return listOfBookmarks;
        }
        listOfBookmarks.addAll(userBookmarks.bookmarks);
        Collections.sort(listOfBookmarks, new FileSysBookmarkComparator());
        return listOfBookmarks;
    }

    protected FileSysBookmark getBookmarkElement(String userid, String searchedId) {
        UserBookmarks userBookmarks = getUserBookmarks(userid);
        if (userBookmarks == null) {
            return (null);
        }
        HashMap<String, FileSysBookmark> userIndex = indexTable.get(userid);
        if (userIndex != null) {
            FileSysBookmark bookmark = userIndex.get(searchedId);
            if (bookmark != null) {
                return (bookmark);
            }
        }
        LOG.warn("bookmark with id " + searchedId + " not found in index");
        for (FileSysBookmark bookmark : userBookmarks.bookmarks) {
            if (bookmark.getId().equals(searchedId)) {
                return (bookmark);
            }
        }
        return null;
    }

    protected UserBookmarks createBookmarkList(String userid) {
        LOG.debug("creating new bookmark list for user : " + userid);
        UserBookmarks userBookmarks = new UserBookmarks();
        bookmarkCache.put(userid, userBookmarks);
        indexTable.put(userid, new HashMap<>());
        return userBookmarks;
    }

    public void createBookmark(String userid, FileSysBookmark newBookmark) {
        UserBookmarks userBookmarks = getUserBookmarks(userid);
        if (userBookmarks == null) {
            userBookmarks = createBookmarkList(userid);
        }
        synchronized (userBookmarks) {
            int lastId = userBookmarks.lastId;
            lastId++;
            userBookmarks.lastId = lastId;
            String newIdString = Integer.toString(lastId);
            newBookmark.setId(newIdString);
            userBookmarks.bookmarks.add(newBookmark);
            HashMap<String, FileSysBookmark> userIndex = indexTable.get(userid);
            userIndex.put(newIdString, newBookmark);
            cacheDirty.put(userid, Boolean.TRUE);
        }
    }

    public void updateBookmark(String userid, FileSysBookmark changedBookmark) {
        UserBookmarks userBookmarks = getUserBookmarks(userid);
        if (userBookmarks == null) {
            LOG.warn("updateBookmark: bookmark list for user " + userid + " not found");
            return;
        }
        synchronized (userBookmarks) {
            FileSysBookmark bookmark = getBookmarkElement(userid, changedBookmark.getId());
            if (bookmark == null) {
                LOG.warn("updateBookmark: bookmark for user " + userid + " with id " + changedBookmark.getId() + " not found");
                return;
            }
            bookmark.setName(changedBookmark.getName());
            bookmark.setPath(changedBookmark.getPath());
            bookmark.setCreationTime(changedBookmark.getCreationTime());
            bookmark.setUpdateTime(new Date());
            cacheDirty.put(userid, Boolean.TRUE);
        }
    }

    public FileSysBookmark getBookmarkElementByName(String uid, String searchedName) {
        UserBookmarks userBookmarks = getUserBookmarks(uid);
        if (userBookmarks == null) {
            return (null);
        }
        synchronized (userBookmarks) {
            for (FileSysBookmark bookmark : userBookmarks.bookmarks) {
                String bookmarkName = bookmark.getName();
                if ((bookmarkName != null) && bookmarkName.equals(searchedName)) {
                    return (bookmark);
                }
            }
        }
        return null;
    }

    public void removeBookmark(String userid, String searchedId) {
        UserBookmarks userBookmarks = getUserBookmarks(userid);
        if (userBookmarks == null) {
            LOG.warn("bookmark list for user " + userid + " not found");
            return;
        }
        synchronized (userBookmarks) {
            FileSysBookmark bookmark = getBookmarkElement(userid, searchedId);
            if (bookmark == null) {
                LOG.warn("bookmark for user " + userid + " id " + searchedId + " not found");
                return;
            }
            userBookmarks.bookmarks.remove(bookmark);
            HashMap<String, FileSysBookmark> userIndex = indexTable.get(userid);
            userIndex.remove(searchedId);
            cacheDirty.put(userid, Boolean.TRUE);
        }
    }

    protected synchronized void saveToFile(String userid) {
        UserBookmarks userBookmarks = getUserBookmarks(userid);
        if (userBookmarks == null) {
            LOG.warn("bookmark list for user " + userid + " does not exist");
            return;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("saving bookmarks for user " + userid);
        }
        synchronized (userBookmarks) {
            writeBookmarkListToJson(userid, userBookmarks);
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
        bookmarkCache.remove(userid);
        indexTable.remove(userid);
        String bookmarkFileName = bookmarkPath + File.separator + userid + ".json";
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
