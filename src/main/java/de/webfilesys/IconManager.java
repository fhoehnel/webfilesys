package de.webfilesys;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

public class IconManager
{
    private static final String ICON_FILE = "iconAssignment.conf";

    public static final String DEFAULT_ICON = "doc.gif";

    private Properties iconTable=null;

    private static IconManager iconMgr=null;
    
    private String iconFilePath = null;

    public static IconManager getInstance()
    {
        if (iconMgr==null)
        {
            iconMgr=new IconManager();
        }

        return(iconMgr);
    }

    private IconManager()
    {
        iconTable=new Properties();

        loadIconAssignment();
    }

    protected void loadIconAssignment()
    {
    	iconFilePath = WebFileSys.getInstance().getConfigBaseDir() + File.separator + ICON_FILE;

    	File iconFile = new File(iconFilePath);

         if ((!iconFile.exists()) || (!iconFile.isFile()) || (!iconFile.canRead()))
         {
             Logger.getLogger(getClass()).error("icon assignment file " + iconFilePath + " can not be read");

             return;
         }

         try
         {
             iconTable.load(new FileInputStream(iconFile));
         }
         catch (IOException ioex)
         {
        	 Logger.getLogger(getClass()).error(ioex);
         }
    }

    public String getAssignedIcon(String fileExtension)
    {
        return(iconTable.getProperty(fileExtension.toLowerCase(),DEFAULT_ICON));
    }
    
    public String getIconForFileName(String fileName) {
        String iconImg = DEFAULT_ICON;

        int extIdx = fileName.lastIndexOf('.');

        if ((extIdx > 0) && (extIdx < (fileName.length() - 1)))
        {
            iconImg = getAssignedIcon(fileName.substring(extIdx + 1));
        }
        
        return iconImg;
    }
}


