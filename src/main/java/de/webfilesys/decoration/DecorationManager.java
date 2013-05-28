/*  
 * WebFileSys
 * Copyright (C) 2011 Frank Hoehnel

 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
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

import org.apache.log4j.Logger;
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
 */
public class DecorationManager extends Thread {
	
    public static final String DECORATION_FILE_NAME = "decorations.xml";
	
    private static DecorationManager decoMgr = null;

    private boolean modified = false;
    
    Document doc;

    DocumentBuilder builder;

    Element decorationRoot = null;
    
    String decorationFilePath = null;
    
    HashMap<String, Decoration> index = null;

    private DecorationManager()
    {
    	decorationFilePath = WebFileSys.getInstance().getConfigBaseDir() + "/" + DECORATION_FILE_NAME;
    	
    	index = new HashMap<String, Decoration>();
    	
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
            }
        }
        catch (ParserConfigurationException pcex)
        {
        	Logger.getLogger(getClass()).error(pcex.toString());
        }

        modified = false;

        start();
    }

    public static DecorationManager getInstance()
    {
        if (decoMgr == null)
        {
            decoMgr = new DecorationManager();
        }

        return(decoMgr);
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
        	Logger.getLogger(getClass()).error("cannot write to decoration file " + decoFile.getAbsolutePath());
            return;
        }

        synchronized (decorationRoot)
        {
            OutputStreamWriter xmlOutFile = null;

            try
            {
                FileOutputStream fos = new FileOutputStream(decoFile);
                
                xmlOutFile = new OutputStreamWriter(fos, "UTF-8");
                
                if (Logger.getLogger(getClass()).isDebugEnabled())
                {
                    Logger.getLogger(getClass()).debug("Saving decorations to file " + decoFile.getAbsolutePath());
                }
                
                XmlUtil.writeToStream(decorationRoot, xmlOutFile);
                
                xmlOutFile.flush();

                modified = false;
            }
            catch (IOException io1)
            {
                Logger.getLogger(getClass()).error("error saving decoration to file " + decoFile.getAbsolutePath(), io1);
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

       Logger.getLogger(getClass()).info("reading decorations from " + decorationFile.getAbsolutePath());

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
           Logger.getLogger(getClass()).error("failed to load decoration from file : " + decorationFile.getAbsolutePath(), saxex);
       }
       catch (IOException ioex)
       {
           Logger.getLogger(getClass()).error("failed to load decoration from file : " + decorationFile.getAbsolutePath(), ioex);
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

        if (decorationList == null)
        {
            return;
        }

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
    
    public Decoration getDecoration(String path)
    {
    	return (Decoration) index.get(path.replace('\\', '/'));
    }
    
    public void setDecoration(String path, Decoration newDeco) 
    {
        synchronized (decorationRoot) {
            String normalizedPath = path.replace('\\', '/');    	
        	
            boolean existingFound = false;
            
        	if (index.get(normalizedPath) != null) 
        	{
        		// decoration for this path exists
                NodeList decorationList = decorationRoot.getElementsByTagName("decoration");

                if (decorationList != null)
                {
                    int listLength = decorationList.getLength();

                    for (int i = 0; (!existingFound) && (i < listLength); i++)
                    {
                        Element decorationElement = (Element) decorationList.item(i);
                        String existingPath = XmlUtil.getChildText(decorationElement, "path");
                        
                        if (existingPath.equals(normalizedPath)) 
                        {
                        	if (newDeco.getIcon() != null) 
                        	{
                            	XmlUtil.setChildText(decorationElement, "icon", newDeco.getIcon());
                        	}
                        	else
                        	{
                        		Element oldIcon = XmlUtil.getChildByTagName(decorationElement, "icon");
                        		if (oldIcon != null) {
                        			decorationElement.removeChild(oldIcon);
                        		}
                        	}
                        	if (newDeco.getTextColor() != null) 
                        	{
                            	XmlUtil.setChildText(decorationElement, "textColor", newDeco.getTextColor());
                        	}
                        	else
                        	{
                        		Element oldTextColor = XmlUtil.getChildByTagName(decorationElement, "textColor");
                        		if (oldTextColor != null) {
                        			decorationElement.removeChild(oldTextColor);
                        		}
                        	}
                        	
                        	existingFound = true;
                        }
                    }
                }
        	}
        	
        	if (!existingFound) {
            	Element newDecoElem = decorationRoot.getOwnerDocument().createElement("decoration");
                
            	XmlUtil.setChildText(newDecoElem, "path", normalizedPath);
            	if (newDeco.getIcon() != null) 
            	{
                	XmlUtil.setChildText(newDecoElem, "icon", newDeco.getIcon());
            	}
            	if (newDeco.getTextColor() != null) 
            	{
                 	XmlUtil.setChildText(newDecoElem, "textColor", newDeco.getTextColor());
            	}
             	
            	decorationRoot.appendChild(newDecoElem);
        	}
        	
        	index.put(normalizedPath, newDeco);
        	
        	modified = true;
        }
    }
    
    /**
     * Icons available for folder decoration.
     * @return List of filenames of files in the icons directory.
     */
    public ArrayList<String> getAvailableIcons() 
    {
    	ArrayList<String> availableIcons = new ArrayList<String>();
    	
    	String iconDirPath = WebFileSys.getInstance().getWebAppRootDir() + "icons";
    	
    	File iconDir = new File(iconDirPath);
    	
    	if (iconDir.exists() && iconDir.isDirectory() && iconDir.canRead())
    	{
    		String[] iconFiles = iconDir.list();
    		
    		for (int i = 0; i < iconFiles.length; i++) 
    		{
    			availableIcons.add(iconFiles[i]);
    		}
    	}
    	
    	return availableIcons;
    }
    
    public void collectGarbage()
    {
        synchronized (decorationRoot) 
        {
            NodeList decorationList = decorationRoot.getElementsByTagName("decoration");

            if (decorationList == null)
            {
                return;
            }

            ArrayList<String> availableIcons = getAvailableIcons();
            
            int decoGarbageCounter = 0;
            
            int listLength = decorationList.getLength();

            for (int i = listLength - 1; i >= 0; i--)
            {
                Element decorationElement = (Element) decorationList.item(i);
                
                String path = XmlUtil.getChildText(decorationElement, "path");

            	File checkExistFile = new File(path);
            	
            	if (!checkExistFile.exists()) {
                    decorationRoot.removeChild(decorationElement);
                    index.remove(path);
                    modified = true;
                    decoGarbageCounter++;
            	} else {
            		String icon = XmlUtil.getChildText(decorationElement, "icon");
            		if ((icon != null) && (icon.length() > 0))
            		{
                		if (!availableIcons.contains(icon))
                		{
                			decorationElement.removeChild(XmlUtil.getChildByTagName(decorationElement, "icon"));
                            
                			Decoration deco = index.get(path);
                			if (deco != null) {
                				deco.setIcon(null);
                			}
                			
                			modified = true;
                			
                	        if (Logger.getLogger(getClass()).isInfoEnabled()) {
                	            Logger.getLogger(getClass()).info("removing folder decoration for non-existing icon " + icon);
                	        }
                		}
            		}
            	}
            }            
        	
            if (Logger.getLogger(getClass()).isInfoEnabled()) {
                Logger.getLogger(getClass()).info(decoGarbageCounter + " decorations for removed folders deleted");
            }
        }
    }

    public synchronized void run()
    {
        int counter = 1;

        int sleepHours = 1;

        boolean stop = false;
        
        while (!stop)
        {
            try
            {
                this.wait(60000);

                if (modified)
                {
                    saveToFile();

                    modified = false;
                }

                if (++counter == (sleepHours * 60))
                {
                	collectGarbage();

                    counter = 0;

                    sleepHours = 24;
                }
            }
            catch (InterruptedException e)
            {
                if (modified)
                {
                	saveToFile();
                }
				
				stop = true;
            }
        }
    }
}
