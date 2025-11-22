package de.webfilesys.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.logging.log4j.LogManager;

import de.webfilesys.WebFileSys;

public class CSSManager {
    public static final String DEFAULT_LAYOUT = "fmweb";

    private static final String CSS_DIR = "styles/skins";
    
    private final ArrayList<String> availableCss;
    
    private static CSSManager instance = null;
    
    private final String cssPath;

    private CSSManager() {
    	cssPath = WebFileSys.getInstance().getWebAppRootDir() + "/" + CSS_DIR;
        availableCss = new ArrayList<>();
        readAvailableCss();
    }

    public static CSSManager getInstance() {
        if (instance == null) {
            instance = new CSSManager();
        }
        return instance;
    }

    protected void readAvailableCss() {
        File cssDir = new File(cssPath);

        if ((!cssDir.exists()) || (!cssDir.isDirectory()) || (!cssDir.canRead())) {
            LogManager.getLogger(getClass()).error("CSS directory not found or not readable: {}", cssPath);
            return;
        } 

        File[] cssFileList = cssDir.listFiles();
        if (cssFileList != null) {
            for (File cssFile : cssFileList) {
                if (cssFile.getName().endsWith(".css")) {
                    if (cssFile.isFile() && cssFile.canRead() && (cssFile.length() > 0L)) {
                        String cssName = cssFile.getName().substring(0, cssFile.getName().lastIndexOf('.'));
                        availableCss.add(cssName);
                    }
                }
            }
        }
        if (availableCss.size() > 1) {
            Collections.sort(availableCss);
        }
    }

    public ArrayList<String> getAvailableCss() {
        return availableCss;
    }
}

