package de.webfilesys.watch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.webfilesys.MetaInfManager;
import de.webfilesys.WebFileSys;
import de.webfilesys.graphics.ThumbnailCreatorBase;
import de.webfilesys.util.XmlUtil;

public class FolderStatusManager extends Thread
{
    private static FolderStatusManager statusMgr = null;

    public static final int CHANGE_TYPE_NEW_FILE = 1;
    public static final int CHANGE_TYPE_NEW_DIR = 2;
    public static final int CHANGE_TYPE_REMOVED = 3;
    public static final int CHANGE_TYPE_DATE = 4;
    public static final int CHANGE_TYPE_SIZE = 5;
    
    Element folderStatusElement = null;

    private FolderStatusManager()
    {
    }

    public static FolderStatusManager getInstance()
    {
        if (statusMgr == null)
        {
            statusMgr = new FolderStatusManager();
        }

        return (statusMgr);
    }

    private String getStatusFileName(String folderPath)
    {
        String dirName = null;
        
        if (folderPath.endsWith("/"))
        {
            dirName = "fsroot";
        }
        else 
        {
            dirName = folderPath.substring(folderPath.lastIndexOf('/') + 1);
        }
        
        return "status-" + dirName + "-" + System.currentTimeMillis() + ".xml";
    }
    
    public String createStatusFile(String folderPath)
    {
        String statusFileName = getStatusFileName(folderPath);

        Document doc = null;

        DocumentBuilder builder = null;

        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            builder = factory.newDocumentBuilder();

            doc = builder.newDocument();

            Element folderStatusElem = doc.createElement("folderStatus");
            
            folderStatusElem.setAttribute("path", folderPath);
            
            determineFolderStatus(folderPath, folderStatusElem);
            
            saveToFile(statusFileName, folderStatusElem);
        }
        catch (ParserConfigurationException pcex)
        {
            Logger.getLogger(getClass()).error(pcex.toString());
        }
        
        return statusFileName;
    }

    public void removeStatusFile(String statusFileName) 
    {
        String statusFilePath = WebFileSys.getInstance().getConfigBaseDir() + "/" 
                                + FolderWatchManager.FOLDER_WATCH_DIR + "/" + statusFileName; 

        File statusFile = new File(statusFilePath);
        
        if (!statusFile.exists())
        {
            return;
        }
        statusFile.delete();
    }
    
    private void saveToFile(String statusFileName, Element folderStatusElem)
    {
        if (folderStatusElem == null)
        {
            return;
        }

        String statusFilePath = WebFileSys.getInstance().getConfigBaseDir() + "/" 
                                + FolderWatchManager.FOLDER_WATCH_DIR + "/" + statusFileName; 
        
        File statusFile = new File(statusFilePath);

        if (statusFile.exists() && (!statusFile.canWrite()))
        {
            Logger.getLogger(getClass()).error(
                    "cannot write folder status file "
                            + statusFile.getAbsolutePath());
            return;
        }

        synchronized (folderStatusElem)
        {
            OutputStreamWriter xmlOutFile = null;

            try
            {
                FileOutputStream fos = new FileOutputStream(statusFile);

                xmlOutFile = new OutputStreamWriter(fos, "UTF-8");

                if (Logger.getLogger(getClass()).isDebugEnabled())
                {
                    Logger.getLogger(getClass()).debug(
                            "Saving folder status to file "
                                    + statusFile.getAbsolutePath());
                }

                XmlUtil.writeToStream(folderStatusElem, xmlOutFile);

                xmlOutFile.flush();
            }
            catch (IOException ioEx)
            {
                Logger.getLogger(getClass()).error(
                        "error saving folder status file "
                                + statusFile.getAbsolutePath(), ioEx);
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

    public HashMap getFolderChanges(String folderPath, String statusFileName) {
        String statusFilePath = WebFileSys.getInstance().getConfigBaseDir() + "/" 
                                + FolderWatchManager.FOLDER_WATCH_DIR + "/" + statusFileName; 

        Element folderStatusElem = loadFromFile(statusFilePath);    
        
        HashMap fileCache = new HashMap();
        
        NodeList files = folderStatusElem.getElementsByTagName("file");

        if (files != null)
        {
            int listLength = files.getLength();
            for (int i = 0; i < listLength; i++)
            {
                Element fileElem = (Element) files.item(i);
                
                fileCache.put(XmlUtil.getChildText(fileElem, "name"), fileElem);
            }
        }
        
        NodeList directories = folderStatusElem.getElementsByTagName("dir");

        if (directories != null)
        {
            int listLength = directories.getLength();
            for (int i = 0; i < listLength; i++)
            {
                Element dirElem = (Element) directories.item(i);
                fileCache.put(XmlUtil.getChildText(dirElem, "name"), dirElem);
            }
        }

        HashMap changes = new HashMap();
        
        File folder = new File(folderPath);
        
        File[] fileList = folder.listFiles();
        
        for (int i = 0; i < fileList.length; i++)
        {
            File file = fileList[i];
            
            Element savedStatusElem = (Element) fileCache.get(file.getName());
            
            if (savedStatusElem != null)
            {
                savedStatusElem.setAttribute("modificationChecked", "true");
                
                if (file.isFile())
                {
                    if (!file.getName().equals(MetaInfManager.METAINF_FILE))
                    {
                        String savedModified = XmlUtil.getChildText(savedStatusElem, "modified");
                        if (!savedModified.equals(Long.toString(file.lastModified())))
                        {
                            changes.put(file.getName(), new Integer(CHANGE_TYPE_DATE));
                        }
                        String savedSize = XmlUtil.getChildText(savedStatusElem, "size");
                        if (!savedSize.equals(Long.toString(file.length())))
                        {
                            changes.put(file.getName(), new Integer(CHANGE_TYPE_SIZE));
                        }
                    }
                } 
                else if (file.isDirectory())
                {
                    if (!file.getName().equals(ThumbnailCreatorBase.THUMBNAIL_SUBDIR)) 
                    {
                        String savedModified = XmlUtil.getChildText(savedStatusElem, "modified");
                        if (!savedModified.equals(Long.toString(file.lastModified())))
                        {
                            changes.put(file.getName(), new Integer(CHANGE_TYPE_DATE));
                        }
                    }
                }
            }
            else
            {
                if (file.isFile())
                {
                    if (!file.getName().equals(MetaInfManager.METAINF_FILE))
                    {
                        changes.put(file.getName(), new Integer(CHANGE_TYPE_NEW_FILE));
                    }
                }
                else if (file.isDirectory())
                {
                    changes.put(file.getName(), new Integer(CHANGE_TYPE_NEW_DIR));
                }
            }
        }
        
        Iterator iter = fileCache.keySet().iterator();
        
        while (iter.hasNext()) 
        {
            String fileDirName = (String) iter.next();
            
            Element fileDirElem = (Element) fileCache.get(fileDirName);

            String modificationChecked = fileDirElem.getAttribute("modificationChecked");
            
            if ((modificationChecked == null) || (modificationChecked.length() == 0))
            {
                changes.put(fileDirName, new Integer(CHANGE_TYPE_REMOVED));
            }
        }
        
        return changes;
    }
    
    public void updateFolderStatus(String folderPath, String statusFileName)
    {
        String statusFilePath = WebFileSys.getInstance().getConfigBaseDir() + "/" 
                                + FolderWatchManager.FOLDER_WATCH_DIR + "/" + statusFileName; 
        
        Element folderStatusElem = loadFromFile(statusFilePath);
        
        determineFolderStatus(folderPath, folderStatusElem);
        
        saveToFile(statusFileName, folderStatusElem);
    }
    
    private void determineFolderStatus(String folderPath, Element folderStatusElem)
    {
        XmlUtil.removeAllChilds(folderStatusElem);

        File folder = new File(folderPath);
        
        if (folder.exists() && folder.isDirectory() && folder.canRead()) 
        {
            File[] fileList = folder.listFiles();
            
            for (int i = 0; i < fileList.length; i++)
            {
                File file = fileList[i];
                
                if (file.isFile())
                {
                    Element fileElem = folderStatusElem.getOwnerDocument().createElement("file");
                    XmlUtil.setChildText(fileElem, "name", file.getName());
                    XmlUtil.setChildText(fileElem, "size", Long.toString(file.length()));
                    XmlUtil.setChildText(fileElem, "modified", Long.toString(file.lastModified()));
                    folderStatusElem.appendChild(fileElem);
                } 
                else if (file.isDirectory())
                {
                    Element dirElem = folderStatusElem.getOwnerDocument().createElement("dir");
                    XmlUtil.setChildText(dirElem, "name", file.getName());
                    XmlUtil.setChildText(dirElem, "modified", Long.toString(file.lastModified()));
                    folderStatusElem.appendChild(dirElem);
                }
            }
        }
    }
    
    public long getFolderChecksum(String folderPath)
    {
        long checksum = 0;
        
        File folder = new File(folderPath);
        
        if (folder.exists() && folder.isDirectory() && folder.canRead()) 
        {
            File[] fileList = folder.listFiles();
            
            for (int i = 0; i < fileList.length; i++)
            {
                File file = fileList[i];
                
                if (file.isFile())
                {
                    if (!file.getName().equals(MetaInfManager.METAINF_FILE))
                    {
                        checksum += (file.lastModified() % 10000);
                        checksum += (file.length() % 10000);
                        checksum += getFileNameChecksum(file.getName());
                    }
                }
                else if (file.isDirectory())
                {
                    if (!file.getName().equals(ThumbnailCreatorBase.THUMBNAIL_SUBDIR)) 
                    {
                        checksum += (file.lastModified() % 10000);
                        checksum += getFileNameChecksum(file.getName());
                    }
                }
            }
        }
        
        return checksum;
    }
    
    private long getFileNameChecksum(String fileName)
    {
        long checksum = 0;
        
        for (int i = 0; i < fileName.length(); i++)
        {
            checksum += fileName.charAt(i);
        }
        
        return checksum;
    }
    
    public Element loadFromFile(String statusFilePath) 
    {
        File folderStatusFile = new File(statusFilePath);

        if ((!folderStatusFile.exists()) || (!folderStatusFile.canRead()))
        {
            Logger.getLogger(getClass()).warn("Folder status file does not exist or is not readable: " + statusFilePath);
            return (null);
        }
        
        FileInputStream fis = null;

        Document doc = null;
        
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            fis = new FileInputStream(folderStatusFile);

            InputSource inputSource = new InputSource(fis);

            inputSource.setEncoding("UTF-8");

            doc = builder.parse(inputSource);
        }
        catch (SAXException saxex)
        {
            Logger.getLogger(getClass()).error(
                    "failed to load folder status file : "
                            + folderStatusFile.getAbsolutePath(), saxex);
        }
        catch (IOException ioex)
        {
            Logger.getLogger(getClass()).error(
                    "failed to load folder status file : "
                            + folderStatusFile.getAbsolutePath(), ioex);
        }
        catch (ParserConfigurationException pcex)
        {
            Logger.getLogger(getClass()).error(pcex.toString());
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

        if (doc == null)
        {
            return null;
        }

        return doc.getDocumentElement();
    }
    
}
