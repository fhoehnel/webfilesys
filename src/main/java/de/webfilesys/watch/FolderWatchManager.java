package de.webfilesys.watch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.util.Iterator;

import de.webfilesys.LanguageManager;
import de.webfilesys.WebFileSys;
import de.webfilesys.mail.Email;
import de.webfilesys.mail.MailTemplate;
import de.webfilesys.user.UserManager;
import de.webfilesys.util.XmlUtil;

/**
 * Watches folders for changes (new, changed, deleted files and subfolders).
 */
public class FolderWatchManager extends Thread
{
    public static final String FOLDER_WATCH_DIR = "folderWatch";
    
    public static final String WATCH_CONFIG_FILE = FOLDER_WATCH_DIR + "/folderWatch.xml";
    
    private static FolderWatchManager watchMgr = null;

    private boolean changed = false;

    Document doc;

    DocumentBuilder builder;

    Element folderWatchElement = null;

    String watchConfigFilePath = null;

    private FolderWatchManager()
    {
        File folderWatchDirFile = new File(WebFileSys.getInstance().getConfigBaseDir() + "/" + FOLDER_WATCH_DIR);
        
        if (!folderWatchDirFile.exists()) {
            if (!folderWatchDirFile.mkdirs()) 
            {
                Logger.getLogger(getClass()).error("cannot create folder watch directory " + folderWatchDirFile); 
                return;
            }
        }
        
        watchConfigFilePath = WebFileSys.getInstance().getConfigBaseDir() + "/"
                + WATCH_CONFIG_FILE;

        builder = null;

        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            builder = factory.newDocumentBuilder();

            folderWatchElement = loadFromFile();

            if (folderWatchElement == null)
            {
                doc = builder.newDocument();

                folderWatchElement = doc.createElement("folderWatch");
            }
        }
        catch (ParserConfigurationException pcex)
        {
            Logger.getLogger(getClass()).error(pcex.toString());
        }

        changed = false;

        this.start();
        
        if (Logger.getLogger(getClass()).isDebugEnabled()) 
        {
            Logger.getLogger(getClass()).debug("FolderWatchManager started");
        }
    }

    public static FolderWatchManager getInstance()
    {
        if (watchMgr == null)
        {
            watchMgr = new FolderWatchManager();
        }

        return (watchMgr);
    }

    public void saveToFile()
    {
        if (folderWatchElement == null)
        {
            return;
        }

        File folderWatchFile = new File(watchConfigFilePath);

        if (folderWatchFile.exists() && (!folderWatchFile.canWrite()))
        {
            Logger.getLogger(getClass()).error(
                    "cannot write folder watch config file "
                            + folderWatchFile.getAbsolutePath());
            return;
        }

        synchronized (folderWatchElement)
        {
            OutputStreamWriter xmlOutFile = null;

            try
            {
                FileOutputStream fos = new FileOutputStream(folderWatchFile);

                xmlOutFile = new OutputStreamWriter(fos, "UTF-8");

                if (Logger.getLogger(getClass()).isDebugEnabled())
                {
                    Logger.getLogger(getClass()).debug(
                            "Saving folder watch configuration to file "
                                    + folderWatchFile.getAbsolutePath());
                }

                XmlUtil.writeToStream(folderWatchElement, xmlOutFile);

                xmlOutFile.flush();

                changed = false;
            }
            catch (IOException io1)
            {
                Logger.getLogger(getClass()).error(
                        "error saving folder watch config file "
                                + folderWatchFile.getAbsolutePath(), io1);
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

    public Element loadFromFile()
    {
        File folderWatchFile = new File(watchConfigFilePath);

        if ((!folderWatchFile.exists()) || (!folderWatchFile.canRead()))
        {
            return (null);
        }

        Logger.getLogger(getClass()).info(
                "reading folder watch config from "
                        + folderWatchFile.getAbsolutePath());

        doc = null;

        FileInputStream fis = null;

        try
        {
            fis = new FileInputStream(folderWatchFile);

            InputSource inputSource = new InputSource(fis);

            inputSource.setEncoding("UTF-8");

            doc = builder.parse(inputSource);
        }
        catch (SAXException saxex)
        {
            Logger.getLogger(getClass()).error(
                    "failed to load folder watch config file : "
                            + folderWatchFile.getAbsolutePath(), saxex);
        }
        catch (IOException ioex)
        {
            Logger.getLogger(getClass()).error(
                    "failed to load folder watch config file : "
                            + folderWatchFile.getAbsolutePath(), ioex);
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

    private Element getFolderElement(String folderPath)
    {
        NodeList folders = folderWatchElement.getElementsByTagName("folder");

        if (folders != null)
        {
            int listLength = folders.getLength();
            for (int i = 0; i < listLength; i++)
            {
                Element folder = (Element) folders.item(i);
                if (folder.getAttribute("path").equals(folderPath))
                {
                    return folder;
                }
            }
        }
        return null;
    }

    public void addFolderChangeListener(String folderPath, String userid)
    {
        String normalizedPath = normalizePath(folderPath);
        
        Element folderElem = getFolderElement(normalizedPath);

        if (folderElem == null)
        {
            folderElem = doc.createElement("folder");
            folderElem.setAttribute("path", normalizedPath);
            folderWatchElement.appendChild(folderElem);

            XmlUtil.setChildText(folderElem, "statusFile", FolderStatusManager.getInstance().createStatusFile(normalizedPath));
            
            XmlUtil.setChildText(folderElem, "checksum", Long.toString(FolderStatusManager.getInstance().getFolderChecksum(normalizedPath)));
            
            changed = true;
        }

        NodeList listeners = folderElem.getElementsByTagName("listener");

        Element listenerElem = null;

        if (listeners != null)
        {
            int listLength = listeners.getLength();
            for (int i = 0; (i < listLength) && (listenerElem == null); i++)
            {
                Element listener = (Element) listeners.item(i);
                if (XmlUtil.getElementText(listener).equals(userid))
                {
                    listenerElem = listener;
                }
            }
        }

        if (listenerElem == null)
        {
            listenerElem = doc.createElement("listener");
            XmlUtil.setElementText(listenerElem, userid);
            folderElem.appendChild(listenerElem);
            changed = true;
        }
    }

    public void removeFolderChangeListener(String folderPath, String userid)
    {
        String normalizedPath = normalizePath(folderPath);
        
        Element folderElem = getFolderElement(normalizedPath);

        if (folderElem == null)
        {
            // nothing to do
            return;
        }

        NodeList listeners = folderElem.getElementsByTagName("listener");

        if (listeners == null)
        {
            // nothing to do
            return;
        }

        boolean removed = false;
        
        int listLength = listeners.getLength();
        for (int i = 0; (i < listLength) && (!removed); i++)
        {
            Element listener = (Element) listeners.item(i);
            if (XmlUtil.getElementText(listener).equals(userid))
            {
                folderElem.removeChild(listener);
                removed = true;
                changed = true;
            }
        }

        if (removed) 
        {
            listeners = folderElem.getElementsByTagName("listener");

            if ((listeners == null) || (listeners.getLength() == 0)) 
            {
                // last listener has been removed - remove the status file and the folder too

                String statusFileName = XmlUtil.getChildText(folderElem, "statusFile");
                if (statusFileName != null) 
                {
                    FolderStatusManager.getInstance().removeStatusFile(statusFileName);
                }
                
                folderWatchElement.removeChild(folderElem);
            }
        }
    }
    
    public boolean isListener(String folderPath, String userid)
    {
        String normalizedPath = normalizePath(folderPath);

        Element folderElem = getFolderElement(normalizedPath);

        if (folderElem == null)
        {
            return false;
        }

        NodeList listeners = folderElem.getElementsByTagName("listener");

        if (listeners == null)
        {
            return false;
        }

        int listLength = listeners.getLength();
        for (int i = 0; i < listLength; i++)
        {
            Element listener = (Element) listeners.item(i);
            if (XmlUtil.getElementText(listener).equals(userid))
            {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * TODO: Only for testing - remove this.
     * @param folderPath
     */
    public void updateFolderStatus(String folderPath)
    {
        String normalizedPath = normalizePath(folderPath);
        
        Element folderElem = getFolderElement(normalizedPath);

        if (folderElem == null)
        {
            // nothing to do
            return;
        }
        
        String statusFileName = XmlUtil.getChildText(folderElem, "statusFile");
        
        FolderStatusManager.getInstance().updateFolderStatus(normalizedPath, statusFileName);
    }    
    
    public void setChecksum(String folderPath, long newChecksum)
    {
        String normalizedPath = normalizePath(folderPath);
        
        Element folderElem = getFolderElement(normalizedPath);

        if (folderElem == null)
        {
            // TODO: checksum allowed for folder without listeners?
            folderElem = doc.createElement("folder");
            folderElem.setAttribute("path", normalizedPath);
            folderWatchElement.appendChild(folderElem);
            changed = true;
        }
        
        String oldChecksum = XmlUtil.getChildText(folderElem, "checksum");
        
        if (oldChecksum != null) {
            if (oldChecksum.equals(Long.toString(newChecksum)))
            {
                return;
            }
        }
        
        XmlUtil.setChildText(folderElem, "checksum", Long.toString(newChecksum));
        changed = true;
    }
    
    public boolean checksumChanged(String folderPath)
    {
        String normalizedPath = normalizePath(folderPath);

        long newChecksum = FolderStatusManager.getInstance().getFolderChecksum(normalizedPath);
        
        Element folderElem = getFolderElement(normalizedPath);

        if (folderElem == null)
        {
            return true;
        }
        
        String oldChecksum = XmlUtil.getChildText(folderElem, "checksum");
        
        if (oldChecksum == null) 
        {
            return true;
        }
        
        return (!oldChecksum.equals(Long.toString(newChecksum)));
    }
    
    private String normalizePath(String folderPath)
    {
        String normalizedPath = folderPath;
        
        if (File.separatorChar == '\\')
        {
            if (folderPath.length() > 1) 
            {
                if (folderPath.charAt(1) == ':') 
                {
                    if (Character.isUpperCase(folderPath.charAt(0))) 
                    {
                        normalizedPath = Character.toLowerCase(folderPath.charAt(0)) + folderPath.substring(1);
                    }
                }
            }
            normalizedPath = normalizedPath.replace('\\', '/');
        }
        return normalizedPath;
    }
    
    private void checkForChanges() 
    {
        NodeList folders = folderWatchElement.getElementsByTagName("folder");

        if (folders != null)
        {
            int listLength = folders.getLength();
            for (int i = listLength - 1; i >= 0; i--)
            {
                Element folderElem = (Element) folders.item(i);
                
                String statusFileName = XmlUtil.getChildText(folderElem, "statusFile");
                
                String folderPath = folderElem.getAttribute("path");
                
                File folderFile = new File(folderPath);
                
                if (folderFile.exists()) 
                {
                    if (checksumChanged(folderPath))
                    {
                        XmlUtil.setChildText(folderElem, "checksum", Long.toString(FolderStatusManager.getInstance().getFolderChecksum(folderPath)));

                        HashMap folderChanges = FolderStatusManager.getInstance().getFolderChanges(folderPath, statusFileName);
                        
                        if (folderChanges.size() > 0) 
                        {
                            if (Logger.getLogger(getClass()).isDebugEnabled()) 
                            {
                                Logger.getLogger(getClass()).debug("changes detected in folder " + folderPath);
                            }
                            
                            NodeList listeners = folderElem.getElementsByTagName("listener");
                            if (listeners != null)
                            {
                                int listenerNum = listeners.getLength();
                                for (int k = 0; k < listenerNum; k++)
                                {
                                    Element listenerElem = (Element) listeners.item(k);
                                    String userid = XmlUtil.getElementText(listenerElem);
                                    
                                    sendNotificationMail(folderPath, folderChanges, userid);                            
                                }
                            }
                        }
                        else
                        {
                            if (Logger.getLogger(getClass()).isDebugEnabled()) 
                            {
                                Logger.getRootLogger().debug("checksum changed but no changes found in folder " + folderPath);
                            }
                        }
                        
                        FolderStatusManager.getInstance().updateFolderStatus(folderPath, statusFileName);
                    }
                } 
                else 
                {
                    if (Logger.getLogger(getClass()).isDebugEnabled()) 
                    {
                        Logger.getLogger(getClass()).debug("watched folder has been removed: " + folderPath);
                    }
                    folderWatchElement.removeChild(folderElem);
                    
                    changed = true;
                    
                    if (statusFileName != null) 
                    {
                        String statusFilePath = WebFileSys.getInstance().getConfigBaseDir() + "/" 
                                                + FolderWatchManager.FOLDER_WATCH_DIR + "/" + statusFileName; 
                        File statusFile = new File(statusFilePath);
                        if (!statusFile.delete()) 
                        {
                            Logger.getLogger(getClass()).warn("failed to delete status file for removed watched folder: " + statusFilePath);
                        }
                    }
                }
            }
        }
    }

    private String getChangesNotificationText(HashMap folderChanges, String userLanguage)
    {
        StringBuffer notificationText = new StringBuffer();
        
        Iterator iter = folderChanges.keySet().iterator();
        
        while (iter.hasNext()) 
        {
            String fileName = (String) iter.next();
            int changeType = ((Integer) folderChanges.get(fileName)).intValue();
            
            String changeText = "";
            
            if (changeType == FolderStatusManager.CHANGE_TYPE_NEW_FILE)
            {
                changeText = LanguageManager.getInstance().getResource(userLanguage, "changeType.newFile", "new file created");
            }
            else if (changeType == FolderStatusManager.CHANGE_TYPE_NEW_DIR)
            {
                changeText = LanguageManager.getInstance().getResource(userLanguage, "changeType.newFolder", "new folder created");
            }
            else if (changeType == FolderStatusManager.CHANGE_TYPE_REMOVED)
            {
                changeText = LanguageManager.getInstance().getResource(userLanguage, "changeType.fileRemoved", "file removed");
            }
            else if (changeType == FolderStatusManager.CHANGE_TYPE_DATE)
            {
                changeText = LanguageManager.getInstance().getResource(userLanguage, "changeType.dateChanged", "modification time changed for");
            }
            else if (changeType == FolderStatusManager.CHANGE_TYPE_SIZE)
            {
                changeText = LanguageManager.getInstance().getResource(userLanguage, "changeType.sizeChanged", "file size changed for");
            }

            notificationText.append(changeText + ": "  + fileName);
            notificationText.append("\r\n");
        }
        
        return notificationText.toString();
    }
    
    private void sendNotificationMail(String folderPath, HashMap folderChanges, String userid)
    {
        UserManager userMgr = WebFileSys.getInstance().getUserMgr();
        
        String userLanguage = userMgr.getLanguage(userid);

        if (userLanguage == null) 
        {
            // user does not exist
            return;
        }

        String email = userMgr.getEmail(userid);
        
        try
        {
            String templateFilePath = WebFileSys.getInstance().getConfigBaseDir() + "/languages/folderChange_" + userLanguage + ".template";
            
            MailTemplate folderChangeTemplate = new MailTemplate(templateFilePath);

            folderChangeTemplate.setVarValue("FOLDER", getHeadlinePath(folderPath, userMgr, userid));

            folderChangeTemplate.setVarValue("CHANGES", getChangesNotificationText(folderChanges, userLanguage));
            
            String mailText = folderChangeTemplate.getText();

            String subject = LanguageManager.getInstance().getResource(userLanguage,"subject.folderChange", "Folder Change");

            (new Email(email, subject, mailText)).send();
        }
        catch (IllegalArgumentException iaex)
        {
            Logger.getLogger(getClass()).error("failed to send notification e-mail for folder change", iaex);
        }
    }
    
    /**
     * Calculate the part of the path that is shown to the user.
     * For webspace users the root of the path to the document root is hidden.
     */
    public String getHeadlinePath(String fullPath, UserManager userMgr, String userid)
    {
        String docRoot = null;

        String role = userMgr.getRole(userid);

        if (role == null)
        {
            return fullPath;
        }
        
        if ((role == null) || role.equals("user") || role.equals("admin"))
        {
            return fullPath;   
        }

        docRoot = userMgr.getDocumentRoot(userid);

        if (docRoot == null)
        {
            docRoot = fullPath;
        }

        String headlinePath = fullPath;

        if ((File.separatorChar == '/')
            && (docRoot.length() != 1)
            || (File.separatorChar == '\\')
            && (docRoot.charAt(0) != '*'))
        {
            int idx = docRoot.length() - 1;

            if (fullPath.length() > idx)
            {
                while ((idx > 0)
                    && (fullPath.charAt(idx) != File.separatorChar)
                    && (fullPath.charAt(idx) != '/'))
                {
                    idx--;
                }

                headlinePath = fullPath.substring(idx);
            }
        }

        return (headlinePath);
    }
    
    public synchronized void run()
    {
        int folderWatchIntervalMinutes = WebFileSys.getInstance().getFolderWatchInterval();
        
        boolean stop = false;
        
        int counter = 0;

        while (!stop)
        {
            try
            {
                this.wait(60000);

                if (changed)
                {
                    saveToFile();

                    changed = false;
                }
                
                if (counter == folderWatchIntervalMinutes)
                {
                    counter = 0;
                    checkForChanges();
                }
                else
                {
                    counter++;
                }
            }
            catch (InterruptedException e)
            {
                if (changed)
                {
                    saveToFile();
                }

                stop = true;

                if (Logger.getLogger(getClass()).isDebugEnabled())
                {
                    Logger.getLogger(getClass()).debug(
                            "FolderWatchManager ready for shutdown");
                }
            }
        }
    }

}
