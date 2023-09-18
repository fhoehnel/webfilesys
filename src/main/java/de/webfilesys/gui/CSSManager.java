package de.webfilesys.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


import de.webfilesys.WebFileSys;

public class CSSManager
{
    public static final String DEFAULT_LAYOUT = "fmweb";

    private static final String CSS_DIR    = "styles/skins";
    
    private ArrayList<String> availableCss;
    
    private static CSSManager layoutMgr=null;
    
    private String cssPath = null;

    private CSSManager()
    {
    	cssPath = WebFileSys.getInstance().getWebAppRootDir() + "/" + CSS_DIR;
    	
        availableCss = new ArrayList<String>();

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
        File cssDir = new File(cssPath);

        if ((!cssDir.exists()) || (!cssDir.isDirectory()) || (!cssDir.canRead()))
        {
            LogManager.getLogger(getClass()).error("CSS directory not found or not readable: " + cssPath);
             
            return;
        } 

        String cssList[] = cssDir.list();

        for (int i=0;i<cssList.length;i++)
        {
             String cssFileName=cssList[i];

             if (cssFileName.endsWith(".css"))
             {
                 File cssFile = new File(cssPath + "/" + cssFileName);

                 if (cssFile.isFile() && cssFile.canRead() && (cssFile.length() > 0L))
                 {
                     String cssName = cssFileName.substring(0, cssFileName.lastIndexOf('.'));

                     availableCss.add(cssName);
                 }
             }
        }

        if (availableCss.size() > 1)
        {
            Collections.sort(availableCss);
        }
    }

    public ArrayList<String> getAvailableCss()
    {
        return(availableCss);
    }
}

