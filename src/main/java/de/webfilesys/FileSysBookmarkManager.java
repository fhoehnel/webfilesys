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

public class FileSysBookmarkManager extends Thread
{
    public static final String BOOKMARK_DIR    = "bookmarks";
	
    Hashtable bookmarkTable = null;

    Hashtable indexTable = null;

    Hashtable cacheDirty = null;
    
    DocumentBuilder builder = null;
    
    String bookmarkFileName = null;
    
    boolean shutdownFlag = false;

    private static FileSysBookmarkManager bookmarkManager = null;
    
    private String bookmarkPath = null;
    
    private FileSysBookmarkManager()
    {
    	bookmarkPath = WebFileSys.getInstance().getConfigBaseDir() + "/" + BOOKMARK_DIR;
    	
        bookmarkTable = new Hashtable();
        
        indexTable = new Hashtable();
        
        cacheDirty = new Hashtable();

        shutdownFlag = false;
        
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

    public static FileSysBookmarkManager getInstance()
    {
        if (bookmarkManager == null)
        {
            bookmarkManager = new FileSysBookmarkManager();
        }

        return(bookmarkManager);
    }

    public Element getBookmarkList(String userid)
    {
        Element bookmarkList = (Element) bookmarkTable.get(userid);

        if (bookmarkList!=null)
        {
            return(bookmarkList);
        }
    
        bookmarkFileName = bookmarkPath + File.separator + userid + ".xml";

        File bookmarkFile = new File(bookmarkFileName);

        if (bookmarkFile.exists() && bookmarkFile.isFile())
        {
            if (!bookmarkFile.canRead())
            {
                Logger.getLogger(getClass()).error("cannot read bookmark file for user " + userid);
                return(null);
            }

            bookmarkList = readBookmarkList(bookmarkFile.getAbsolutePath());

            if (bookmarkList != null)
            {
                bookmarkTable.put(userid, bookmarkList);
                createIndex(bookmarkList, userid);

                return(bookmarkList);
            }
        }
        
        return(null);
    }

    Element readBookmarkList(String bookmarkFilePath)
    {
        File categoryFile = new File(bookmarkFilePath);

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
                Logger.getLogger(getClass()).debug("reading bookmarks from " + bookmarkFilePath);
            }

            doc = builder.parse(inputSource);
        }
        catch (SAXException saxex)
        {
            Logger.getLogger(getClass()).error("failed to load category file : " + bookmarkFilePath, saxex);
        }
        catch (IOException ioex)
        {
            Logger.getLogger(getClass()).error("failed to load category file : " + bookmarkFilePath, ioex);
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

    protected void createIndex(Element bookmarkList, String userid)
    {
        NodeList bookmarks = bookmarkList.getElementsByTagName("bookmark");

        if (bookmarks == null)
        {
            indexTable.remove(userid);
            return;
        }

        int listLength = bookmarks.getLength();

        Hashtable userIndex = new Hashtable();

        for (int i = 0; i < listLength; i++)
        {
             Element bookmark =(Element) bookmarks.item(i);

             String bookmarkId = bookmark.getAttribute("id");

             if (bookmarkId!=null)
             {
                 userIndex.put(bookmarkId, bookmark);
             }
        }

        indexTable.put(userid, userIndex);
    }

    public void disposeBookmarkList(String userid)
    {
        Boolean dirtyFlag = (Boolean) cacheDirty.get(userid);

        if ((dirtyFlag!=null) && dirtyFlag.booleanValue())
        {
            saveToFile(userid);
        }

        if (bookmarkTable.get(userid) != null)
        {
			Logger.getLogger(getClass()).debug("disposing bookmark list of user " + userid);
        }

        bookmarkTable.remove(userid);
        indexTable.remove(userid);
    }

    public void disposeAllBookmarks()
    {
        saveChangedUsers();

        bookmarkTable = new Hashtable();
        indexTable = new Hashtable();
    }

    public Vector getBookmarkIds(String userid)
    {
        Element bookmarkList = getBookmarkList(userid);

        Vector bookmarkIds = null;

        if (bookmarkList == null)
        {
            // System.out.println("bookmark list for user " + userid + " does not exist!");
            return(null);
        }

        NodeList bookmarks = bookmarkList.getElementsByTagName("bookmark");

        if (bookmarks != null)
        {
            int listLength = bookmarks.getLength();

            for (int i=0; i < listLength;i++)
            {
                Element bookmark = (Element) bookmarks.item(i);

                if (bookmarkIds == null)
                {
                    bookmarkIds = new Vector();
                }

                bookmarkIds.add(bookmark.getAttribute("id"));
            }
        }
        else
        {
            Logger.getLogger(getClass()).debug("no bookmarks found for userid " + userid);
        }
    
        return(bookmarkIds);
    }

    public Vector getListOfBookmarks(String userid)
    {
        return(getListOfBookmarks(userid, true));
    }
    
    public Vector getListOfBookmarks(String userid, boolean createIfMissing)
    {
        Element bookmarkList = getBookmarkList(userid);

        Vector listOfBookmarks = new Vector();

        if (bookmarkList==null)
        {
            Logger.getLogger(getClass()).debug("bookmark list for user " + userid + " does not exist!");

            return(listOfBookmarks);

            /*
            if (createIfMissing)
            {
                return(null);
            }
            */
        }

        NodeList bookmarks = bookmarkList.getElementsByTagName("bookmark");

        if (bookmarks != null)
        {
            int listLength = bookmarks.getLength();
            
            for (int i=0; i < listLength; i++)
            {
                Element bookmark = (Element) bookmarks.item(i);

                FileSysBookmark newBookmark = new FileSysBookmark(bookmark.getAttribute("id"));

                newBookmark.setName(XmlUtil.getChildText(bookmark, "name"));

                newBookmark.setPath(XmlUtil.getChildText(bookmark, "path"));

                long creationTime = 0L;
                
                String timeString = XmlUtil.getChildText(bookmark, "creationTime");
                
                try
                {
                    creationTime=Long.parseLong(timeString);
                }
                catch (NumberFormatException nfe)
                {
                    Logger.getLogger(getClass()).warn(nfe);
                    creationTime=(new Date()).getTime();
                }

                newBookmark.setCreationTime(new Date(creationTime));

                long updateTime=0L;
                timeString = XmlUtil.getChildText(bookmark, "updateTime");
                try
                {
                    updateTime=Long.parseLong(timeString);
                }
                catch (NumberFormatException nfe)
                {
                    Logger.getLogger(getClass()).warn(nfe);
                    updateTime=(new Date()).getTime();
                }

                newBookmark.setUpdateTime(new Date(updateTime));

                listOfBookmarks.add(newBookmark);
            }
        }
    
        if (listOfBookmarks != null)
        {
            Collections.sort(listOfBookmarks, new FileSysBookmarkComparator());
        }

        return(listOfBookmarks);
    }

    public FileSysBookmark getBookmark(String userid, String searchedId)
    {
        Element bookmark = getBookmarkElement(userid, searchedId);

        if (bookmark == null)
        {
            Logger.getLogger(getClass()).warn("bookmark for user " + userid + "id " + searchedId + " does not exist!");
            return(null);
        }

        FileSysBookmark foundBookmark = new FileSysBookmark(bookmark.getAttribute("id"));

        foundBookmark.setName(XmlUtil.getChildText(bookmark, "name"));

        foundBookmark.setPath(XmlUtil.getChildText(bookmark, "path"));

        long creationTime=0L;
        String timeString = XmlUtil.getChildText(bookmark, "creationTime");
        try
        {
            creationTime=Long.parseLong(timeString);
        }
        catch (NumberFormatException nfe)
        {
            Logger.getLogger(getClass()).error(nfe);
            creationTime=(new Date()).getTime();
        }

        foundBookmark.setCreationTime(new Date(creationTime));

        long updateTime = 0L;
        timeString = XmlUtil.getChildText(bookmark, "updateTime");
        try
        {
             updateTime=Long.parseLong(timeString);
        }
        catch (NumberFormatException nfe)
        {
			Logger.getLogger(getClass()).error(nfe);
            updateTime=(new Date()).getTime();
        }

        foundBookmark.setUpdateTime(new Date(updateTime));

        return(foundBookmark);
    }

    protected Element getBookmarkElement(String userid, String searchedId)
    {
        Element bookmarkList = getBookmarkList(userid);

        if (bookmarkList == null)
        {
            return(null);
        }

        Element bookmark = null;

        Hashtable userIndex = (Hashtable) indexTable.get(userid);

        if (userIndex!=null)
        {
            bookmark = (Element) userIndex.get(searchedId);

            if (bookmark != null)
            {
                return(bookmark);
            }
        }

        Logger.getLogger(getClass()).warn("bookmark with id " + searchedId + " not found in index");

        NodeList bookmarks = bookmarkList.getElementsByTagName("bookmark");

        if (bookmarks == null)
        {
            return(null);
        }

        int listLength = bookmarks.getLength();

        for (int i = 0; i < listLength; i++)
        {
            bookmark = (Element) bookmarks.item(i);

            if (bookmark.getAttribute("id").equals(searchedId))
            {
                return(bookmark);
            }
        }
    
        return(null);
    }

    protected Element createBookmarkList(String userid)
    {
        Logger.getLogger(getClass()).debug("creating new bookmark list for user : " + userid);
        
        Document doc = builder.newDocument();

        Element bookmarkListElement = doc.createElement("bookmarkList");

        Element lastIdElement = doc.createElement("lastId");
        XmlUtil.setElementText(lastIdElement,"0");

        bookmarkListElement.appendChild(lastIdElement);
        
        doc.appendChild(bookmarkListElement);

        bookmarkTable.put(userid, bookmarkListElement);

        indexTable.put(userid, new Hashtable());
        
        return(bookmarkListElement);
    }

    public Element createBookmark(String userid, FileSysBookmark newBookmark)
    {
        Element bookmarkList = getBookmarkList(userid);

        if (bookmarkList == null)
        {
            bookmarkList = createBookmarkList(userid);
        }

        Element newElement = null;
        
        synchronized (bookmarkList)
        {
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
            
            Hashtable userIndex = (Hashtable) indexTable.get(userid);
            userIndex.put(newIdString, newElement);
        }

        updateBookmark(userid, newBookmark);

        return(newElement);
    }

    protected int getLastId(String userid)
    {
        Element bookmarkList = getBookmarkList(userid);

        if (bookmarkList == null)
        {
            return(-1);
        }

        String lastIdString = XmlUtil.getChildText(bookmarkList, "lastId").trim();

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
        Element bookmarkList = getBookmarkList(userid);

        if (bookmarkList == null)
        {
            return;
        }

        XmlUtil.setChildText(bookmarkList, "lastId", Integer.toString(lastId));
    }

    public Element updateBookmark(String userid, FileSysBookmark changedBookmark)
    {
        Element bookmarkListElement = getBookmarkList(userid);

        synchronized (bookmarkListElement)
        {
            Element bookmarkElement = getBookmarkElement(userid, changedBookmark.getId());

            if (bookmarkElement == null)
            {
                Logger.getLogger(getClass()).warn("updateBookmark: bookmark for user " + userid + " with id " + changedBookmark.getId() +  " not found");
                return(null);
            }

            XmlUtil.setChildText(bookmarkElement, "name", changedBookmark.getName(), true);
            XmlUtil.setChildText(bookmarkElement, "path", changedBookmark.getPath(), true);
			XmlUtil.setChildText(bookmarkElement, "creationTime", "" + changedBookmark.getCreationTime().getTime());
			XmlUtil.setChildText(bookmarkElement, "updateTime", "" + changedBookmark.getUpdateTime().getTime());

            cacheDirty.put(userid, new Boolean(true));
            
            return(bookmarkElement);
        }
    }

    public Element getBookmarkElementByName(String uid, String searchedName)
    {
		Element bookmarkListElement = getBookmarkList(uid);
		
		if (bookmarkListElement == null)
		{
			return(null);
		}

		synchronized (bookmarkListElement)
		{
			NodeList bookmarks = bookmarkListElement.getElementsByTagName("bookmark");

			if (bookmarks == null)
			{
				return(null);
			}

			int listLength = bookmarks.getLength();

			for (int i = 0; i < listLength; i++)
			{
				Element bookmarkElement = (Element) bookmarks.item(i);
				
				String bookmarkName = XmlUtil.getChildText(bookmarkElement, "name");
				
				if ((bookmarkName != null) && bookmarkName.equals(searchedName))
				{
					return(bookmarkElement);
				}
			}
		}
		
		return(null);
    }
    
    public FileSysBookmark getBookmarkByName(String uid, String searchedName)
    {
    	Element bookmarkElement = getBookmarkElementByName(uid, searchedName);
    	
    	if (bookmarkElement == null)
    	{
    		return(null);
    	}

    	FileSysBookmark newBookmark = new FileSysBookmark(bookmarkElement.getAttribute("id"));

		newBookmark.setName(XmlUtil.getChildText(bookmarkElement, "name"));

		newBookmark.setPath(XmlUtil.getChildText(bookmarkElement, "path"));

		long creationTime = 0L;
                
		String timeString = XmlUtil.getChildText(bookmarkElement, "creationTime");
                
		try
		{
			creationTime=Long.parseLong(timeString);
		}
		catch (NumberFormatException nfe)
		{
			Logger.getLogger(getClass()).warn(nfe);
			creationTime=(new Date()).getTime();
		}

		newBookmark.setCreationTime(new Date(creationTime));

		long updateTime=0L;
		timeString = XmlUtil.getChildText(bookmarkElement, "updateTime");
		try
		{
			updateTime=Long.parseLong(timeString);
		}
		catch (NumberFormatException nfe)
		{
			Logger.getLogger(getClass()).warn(nfe);
			updateTime=(new Date()).getTime();
		}

		newBookmark.setUpdateTime(new Date(updateTime));
		
		return(newBookmark);
    }

    public void removeBookmark(String userid, String searchedId)
    {
        Element bookmarkListElement = getBookmarkList(userid);

        synchronized (bookmarkListElement)
        {
            Element bookmarkElement = getBookmarkElement(userid, searchedId);

            if (bookmarkElement == null)
            {
                Logger.getLogger(getClass()).warn("bookmark for user " + userid + " id " + searchedId + " not found");
                return;
            }

            Node bookmarkList = bookmarkElement.getParentNode();

            if (bookmarkList!=null)
            {
                Hashtable userIndex=(Hashtable) indexTable.get(userid);
                userIndex.remove(bookmarkElement.getAttribute("id"));
                
                bookmarkList.removeChild(bookmarkElement);

                cacheDirty.put(userid, new Boolean(true));
            }
        }

    }

    protected synchronized void saveToFile(String userid)
    {
        Element bookmarkListElement = getBookmarkList(userid);

        if (bookmarkListElement == null)
        {
            Logger.getLogger(getClass()).warn("bookmark list for user " + userid + " does not exist");
            return;
        }

        if (Logger.getLogger(getClass()).isDebugEnabled())
        {
            Logger.getLogger(getClass()).debug("saving bookmarks for user " + userid);
        }
        
        synchronized (bookmarkListElement)
        {
            String xmlFileName = bookmarkPath + File.separator + userid + ".xml";

            OutputStreamWriter xmlOutFile = null;

            try
            {
                FileOutputStream fos = new FileOutputStream(xmlFileName);
                
                xmlOutFile = new OutputStreamWriter(fos, "UTF-8");
                
                XmlUtil.writeToStream(bookmarkListElement, xmlOutFile);
                
                xmlOutFile.flush();
            }
            catch (IOException io1)
            {
                Logger.getLogger(getClass()).error("error saving bookmark file " + xmlFileName, io1);
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
        Enumeration cacheUserList = cacheDirty.keys();

        while (cacheUserList.hasMoreElements())
        {
            String userid = (String) cacheUserList.nextElement();

            boolean dirtyFlag =((Boolean) cacheDirty.get(userid)).booleanValue();

            if (dirtyFlag)
            {
                saveToFile(userid);
                cacheDirty.put(userid,new Boolean(false));
            }
        }
    }

    public void deleteUser(String userid)
    {
        bookmarkTable.remove(userid);
        indexTable.remove(userid);
        
        String bookmarkFileName = bookmarkPath + File.separator + userid + ".xml";
        
        File bookmarkFile = new File(bookmarkFileName);
        
        if (!bookmarkFile.exists() || !bookmarkFile.isFile())
        {
            return;
        }
        
        if (bookmarkFile.delete())
        {
            if (Logger.getLogger(getClass()).isDebugEnabled())
            {
                Logger.getLogger(getClass()).debug("bookmark file deleted for user " + userid);
            }
            else
            {
                Logger.getLogger(getClass()).warn("failed to delete bookmark file for user " + userid);
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

	/*
	static public void main(String args[])
	{
		FileSysBookmarkManager mgr = FileSysBookmarkManager.getInstance();
	
	    for (int i=0; i < 20; i++)
	    {
	    	FileSysBookmark newBookmark = new FileSysBookmark();
	    
	    	newBookmark.setName("BookmarkName-" + i);

	    	newBookmark.setName("BookmarkPath-" + i);
	
			mgr.createBookmark("testuser", newBookmark);	
			
			System.out.println("created bookmark" + newBookmark.getName());
	    }

		System.out.println("bookmarks of user testuser:");

        Vector userBookmarks = mgr.getListOfBookmarks("testuser");
        
        for (int i = 0; i < userBookmarks.size(); i++)
        {
        	FileSysBookmark bookmark = (FileSysBookmark) userBookmarks.elementAt(i);
        	
			System.out.println("  " + bookmark.getName() + "  " + bookmark.getPath());
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
	}
	*/
	
}
