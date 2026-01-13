package de.webfilesys.decoration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import de.webfilesys.MetaInfManager;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.webfilesys.WebFileSys;
import de.webfilesys.util.XmlUtil;

/**
 * Manager for decoration of folders with individual icons and text colors.
 * @author Frank Hoehnel
 * @deprecated will be removed after migration to MetaInfManager has run
 */
public class DecorationManager {
	
    public static final String DECORATION_FILE_NAME = "decorations.xml";
	
    private static DecorationManager decoMgr = null;

    Document doc;

    DocumentBuilder builder;

    Element decorationRoot = null;
    
    String decorationFilePath = null;
    
    HashMap<String, Decoration> index = null;

    private DecorationManager()
    {
    	decorationFilePath = WebFileSys.getInstance().getConfigBaseDir() + "/" + DECORATION_FILE_NAME;
    	
    	index = new HashMap<>();
    	
        builder = null;

        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
            
            decorationRoot = loadFromFile();

            if (decorationRoot == null)
            {
                doc = builder.newDocument();

                decorationRoot = doc.createElement("decorations");
            } else {
                createIndex(decorationRoot);
                migrateToMetaInf();
            }
        }
        catch (ParserConfigurationException pcex)
        {
        	LogManager.getLogger(getClass()).error(pcex.toString());
        }
    }

    public static DecorationManager getInstance() {
        if (decoMgr == null) {
            decoMgr = new DecorationManager();
        }
        return decoMgr;
    }

    public void saveToFile()
    {
        if (decorationRoot == null)
        {
            return;
        }
            
        File decoFile = new File(decorationFilePath);
        
        if (decoFile.exists() && (!decoFile.canWrite()))
        {
        	LogManager.getLogger(getClass()).error("cannot write to decoration file " + decoFile.getAbsolutePath());
            return;
        }

        synchronized (decorationRoot)
        {
            OutputStreamWriter xmlOutFile = null;

            try
            {
                FileOutputStream fos = new FileOutputStream(decoFile);
                
                xmlOutFile = new OutputStreamWriter(fos, "UTF-8");
                
                if (LogManager.getLogger(getClass()).isDebugEnabled())
                {
                    LogManager.getLogger(getClass()).debug("Saving decorations to file " + decoFile.getAbsolutePath());
                }
                
                XmlUtil.writeToStream(decorationRoot, xmlOutFile);
                
                xmlOutFile.flush();
            }
            catch (IOException io1)
            {
                LogManager.getLogger(getClass()).error("error saving decoration to file " + decoFile.getAbsolutePath(), io1);
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
       File decorationFile = new File(decorationFilePath);

       if ((!decorationFile.exists()) || (!decorationFile.canRead()))
       {
           return(null);
       }

       LogManager.getLogger(getClass()).info("reading decorations from " + decorationFile.getAbsolutePath());

       doc = null;
       
       FileInputStream fis = null;

       try
       {
           fis = new FileInputStream(decorationFile);
           
           InputSource inputSource = new InputSource(fis);
           
           inputSource.setEncoding("UTF-8");

           doc = builder.parse(inputSource);
       }
       catch (SAXException saxex)
       {
           LogManager.getLogger(getClass()).error("failed to load decoration from file : " + decorationFile.getAbsolutePath(), saxex);
       }
       catch (IOException ioex)
       {
           LogManager.getLogger(getClass()).error("failed to load decoration from file : " + decorationFile.getAbsolutePath(), ioex);
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
           return(null);
       }

       return(doc.getDocumentElement());
    }
    
    private void createIndex(Element decorationRoot) {
        NodeList decorationList = decorationRoot.getElementsByTagName("decoration");

        int listLength = decorationList.getLength();

        for (int i = 0; i < listLength; i++)
        {
        	Element decorationElement = (Element) decorationList.item(i);
            
            String path = XmlUtil.getChildText(decorationElement, "path");
            
            Decoration deco = new Decoration();
            String icon = XmlUtil.getChildText(decorationElement, "icon");
            if ((icon != null) && (icon.length() > 0))
            {
                deco.setIcon(icon);
            }
            String textColor = XmlUtil.getChildText(decorationElement, "textColor");
            if ((textColor != null) && (textColor.length() > 0))
            {
                deco.setTextColor(textColor);
            }
            
            index.put(path, deco);
        }
    }

    public void removeDecoration(String path) {
        String normalizedPath = path.replace('\\', '/');
        if (index.get(normalizedPath) == null) {
            LogManager.getLogger(getClass()).warn("decoration to remove not found in index: {}", path);
            return;
        }
        NodeList decorationList = decorationRoot.getElementsByTagName("decoration");
        int listLength = decorationList.getLength();
        for (int i = 0; i < listLength; i++) {
            Element decorationElement = (Element) decorationList.item(i);
            String existingPath = XmlUtil.getChildText(decorationElement, "path");
            if (existingPath.equals(normalizedPath)) {
                decorationRoot.removeChild(decorationElement);
                index.remove(normalizedPath);
                return;
            }
        }
        LogManager.getLogger(getClass()).warn("decoration to remove is in index but not in element list: " + path);
    }

    // TODO: remove when all servers are migrated
    private void migrateToMetaInf() {
        if (index.isEmpty()) {
            // nothing to migrate
            return;
        }
        ArrayList<String> pathToRemove = new ArrayList<>();
        index.forEach((key, value) -> {
            MetaInfManager.getInstance().setDecoration(key, ".", value);
            LogManager.getLogger(getClass()).debug("decoration migrated for path {}", key);
            pathToRemove.add(key);
        });
        pathToRemove.forEach(this::removeDecoration);
        saveToFile();
    }
}
