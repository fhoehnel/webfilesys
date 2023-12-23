package de.webfilesys;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


import de.webfilesys.util.CommonUtils;

public class IconManager {
    private static final String ICON_FILE = "iconAssignment.conf";

    public static final String DEFAULT_ICON = "doc.gif";

    private static final HashMap<String, String> fileIconFontMap;
    
    private Properties iconTable=null;

    private static IconManager iconMgr=null;
    
    private String iconFilePath = null;

    static {
    	fileIconFontMap = new HashMap<>();
    	fileIconFontMap.put(".jpg", "camera");
    	fileIconFontMap.put(".jpeg", "camera");
    	fileIconFontMap.put(".png", "pic");
    	fileIconFontMap.put(".gif", "pic");
    	fileIconFontMap.put(".bmp", "pic");
    	fileIconFontMap.put(".pdf", "pdf");
    	fileIconFontMap.put(".doc", "doc");
    	fileIconFontMap.put(".docx", "doc");
    	fileIconFontMap.put(".xlsm", "xls");
    	fileIconFontMap.put(".xlsx", "xls");
    	fileIconFontMap.put(".xlsb", "xls");
    	fileIconFontMap.put(".mov", "mov");
    	fileIconFontMap.put(".mp4", "mov");
    	fileIconFontMap.put(".mpg", "mov");
    	fileIconFontMap.put(".mpeg", "mov");
    	fileIconFontMap.put(".ogg", "mov");
    	fileIconFontMap.put(".webm", "mov");
    	fileIconFontMap.put(".avi", "mov");
    	fileIconFontMap.put(".flv", "mov");
    	fileIconFontMap.put(".mkv", "mov");
    	fileIconFontMap.put(".3gp", "mov");
    	fileIconFontMap.put(".zip", "zip");
    	fileIconFontMap.put(".gzip", "zip");
    	fileIconFontMap.put(".gz", "zip");
    	fileIconFontMap.put(".jar", "zip");
    	fileIconFontMap.put(".war", "zip");
    	fileIconFontMap.put(".ear", "zip");
    	fileIconFontMap.put(".tar", "zip");
    	fileIconFontMap.put(".xml", "xml");
    	fileIconFontMap.put(".xsl", "xml");
    	fileIconFontMap.put(".xsd", "xml");
    	fileIconFontMap.put(".wsdl", "xml");
    	fileIconFontMap.put(".mp3", "audio");
    	fileIconFontMap.put(".txt", "txt");
    	fileIconFontMap.put(".gpx", "map");
    	fileIconFontMap.put(".kml", "map");
    	fileIconFontMap.put(".kmz", "map");
    	fileIconFontMap.put(".log", "log");
    	fileIconFontMap.put(".java", "java");
    	fileIconFontMap.put(".eot", "font");
    	fileIconFontMap.put(".ttf", "font");
    	fileIconFontMap.put(".woff", "font");
    	fileIconFontMap.put(".otf", "font");
    	fileIconFontMap.put(".conf", "conf");
    	fileIconFontMap.put(".properties", "conf");
    	fileIconFontMap.put(".html", "html");
    	fileIconFontMap.put(".htm", "html");
    }
    
    public static IconManager getInstance() {
        if (iconMgr == null) {
            iconMgr = new IconManager();
        }

        return(iconMgr);
    }

    private IconManager() {
        iconTable = new Properties();

        loadIconAssignment();
    }

    protected void loadIconAssignment() {
    	iconFilePath = WebFileSys.getInstance().getConfigBaseDir() + File.separator + ICON_FILE;

    	File iconFile = new File(iconFilePath);

        if ((!iconFile.exists()) || (!iconFile.isFile()) || (!iconFile.canRead())) {
             LogManager.getLogger(getClass()).error("icon assignment file " + iconFilePath + " is not a readable file");
             return;
         }

         FileInputStream fis = null;

         try {
             fis = new FileInputStream(iconFile);
             iconTable.load(fis);
         } catch (IOException ioex) {
        	 LogManager.getLogger(getClass()).error("failed to load icon assignment file", ioex);
         } finally {
 			if (fis != null) {
 				try {
 					fis.close();
 				} catch (IOException ex) {
 				}
 			}
 		 }
    }

    public String getAssignedIcon(String fileExtension) {
        return(iconTable.getProperty(fileExtension.toLowerCase(),DEFAULT_ICON));
    }
    
    public String getIconForFileName(String fileName) {
        String iconImg = DEFAULT_ICON;

        int extIdx = fileName.lastIndexOf('.');

        if ((extIdx > 0) && (extIdx < (fileName.length() - 1))) {
            iconImg = getAssignedIcon(fileName.substring(extIdx + 1));
        }
        
        return iconImg;
    }

    public String getFileIconNoDefault(String fileName) {
        String iconImg = null;
        
        String fileExt = CommonUtils.getFileExtension(fileName);

        if (fileExt.length() > 0) {
        	iconImg = iconTable.getProperty(fileExt.substring(1));
        }
        
        return iconImg;
    }
    
    public String getFileIconFont(String fileName) {
    	String fileExt = CommonUtils.getFileExtension(fileName);
    	return fileIconFontMap.get(fileExt);
    }

}


