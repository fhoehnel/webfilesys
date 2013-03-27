package de.webfilesys.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.webfilesys.WebFileSys;

public class CSSManager
{
    public static final String DEFAULT_LAYOUT = "fmweb";

    private static final String CSS_DIR    = "css";
    
    private static final String CALENDAR_CSS_FILENAME = "calendar.css";
    
    private static final String MOBILE_CSS_FILENAME = "mobile.css";

    private Hashtable cssTable;

    private Vector availableCss;
    
    private static CSSManager layoutMgr=null;
    
    private String cssPath = null;

    private CSSManager()
    {
    	cssPath = WebFileSys.getInstance().getWebAppRootDir() + "/" + CSS_DIR;
    	
        cssTable=new Hashtable(5);

        availableCss=new Vector();

        readAvailableCss();      
    }

    public static CSSManager getInstance()
    {
        if (layoutMgr==null)
        {
            layoutMgr=new CSSManager();
        }

        return(layoutMgr);
    }

    protected void readAvailableCss()
    {
        File cssDir=new File(cssPath);

        if ((!cssDir.exists()) || (!cssDir.isDirectory()) || (!cssDir.canRead()))
        {
            Logger.getLogger(getClass()).error("CSS directory not found or not readable: " + cssPath);
             
            return;
        } 

        String cssList[]=cssDir.list();

        for (int i=0;i<cssList.length;i++)
        {
             String cssFileName=cssList[i];

             if (cssFileName.endsWith(".css") &&
            	(!cssFileName.equals(CALENDAR_CSS_FILENAME)) && 
            	(!cssFileName.equals(MOBILE_CSS_FILENAME)))
             {
                 File cssFile = new File(cssPath + "/" + cssFileName);

                 if (cssFile.isFile() && cssFile.canRead() && (cssFile.length() > 0L))
                 {
                     String cssName=cssFileName.substring(0,cssFileName.lastIndexOf('.'));

                     availableCss.add(cssName);
                 }
             }
        }

        if (availableCss.size() > 1)
        {
            Collections.sort(availableCss);
        }
    }

    public Vector getAvailableCss()
    {
        return(availableCss);
    }

    public String getCss(String cssName)
    {
        if (cssName.length()==0)
        {
            cssName=DEFAULT_LAYOUT;
        }

        String cssText=(String) cssTable.get(cssName);
        
        if (cssText==null)
        {
            cssText=loadCss(cssName);
            
            if (cssText==null)
            {
                cssText=(String) cssTable.get(DEFAULT_LAYOUT);
                
                if (cssText==null)
                {
                    cssText=loadCss(DEFAULT_LAYOUT);
                    
                    if (cssText==null)
                    {
                        // System.out.println("*** default CSS file " + DEFAULT_LAYOUT + " cannot be loaded");
                        
                        return("");
                    }
                }
            }
            else
            {
                cssTable.put(cssName,cssText);
            }
        }  

        return(cssText);
    }

    protected String loadCss(String cssName)
    {
        String cssFilename = cssPath + "/" + cssName + "." + "css";

        BufferedReader cssFile=null;

        // System.out.println("Loading Layout from " + cssFilename);

        StringBuffer buff=new StringBuffer();

        try
        {
            cssFile=new BufferedReader(new FileReader(cssFilename));
        
            String line=null;

            while ((line=cssFile.readLine())!=null)
            {
                buff.append(line);
                buff.append('\n');
            }

            cssFile.close();
        }
        catch (FileNotFoundException fnfe)
        {
            System.err.println("CSSManager.loadCSS: CSS file not found : " + cssFilename);
            return(null);
        }
        catch(IOException ioex)
        {
            System.err.println("CSSManager.loadCSS: " + ioex);
            return(null);
        }
        
        return(buff.toString());
    }

    public void clearAllCss()
    {
    	cssTable.clear();
    }
}

