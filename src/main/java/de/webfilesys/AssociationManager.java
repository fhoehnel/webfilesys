package de.webfilesys;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


import de.webfilesys.util.PatternComparator;

/**
 * Handles associations of application programs to file types.
 * @author fho
 */
public class AssociationManager {
    public static final String ASSOCIATION_CONFIG_FILE = "fileTypeAssociations.conf";    
	
	private static AssociationManager assocMgr = null;
	
	private Properties associationMap;
	
	private AssociationManager() {
		associationMap = new Properties();

        File assocFile = new File(WebFileSys.getInstance().getConfigBaseDir() + File.separator + ASSOCIATION_CONFIG_FILE);
        if (assocFile.exists() && assocFile.isFile() && assocFile.canRead()) {
        	FileInputStream fin = null;
            try {
            	fin = new FileInputStream(assocFile);
            	
            	associationMap.load(fin);
            	
            	if (LogManager.getLogger(getClass()).isDebugEnabled()) {
            		LogManager.getLogger(getClass()).debug("filetype associations loaded from " + ASSOCIATION_CONFIG_FILE);
            	}
            } catch (Exception e) {
                LogManager.getLogger(getClass()).error("cannot load filetype associations from " + ASSOCIATION_CONFIG_FILE, e);
            } finally {
     			if (fin != null) {
     				try {
     					fin.close();
     				} catch (IOException ex) {
     				}
     			}
     		 }
        } else {
            LogManager.getLogger(getClass()).error("cannot load filetype associations from " + ASSOCIATION_CONFIG_FILE);
        }
	}
	
	public static AssociationManager getInstance() {
		if (assocMgr == null) {
			assocMgr = new AssociationManager();
		}
		return assocMgr;
	}
	
	/**
	 * Returns the path of the application program which is assigned to the filetype of the given filename.
	 * @param filename the file name
	 * @return path of application program or null, if no association found
	 */
    public String getAssociatedProgram(String filename) {
        Enumeration<Object> keys = associationMap.keys();

        while (keys.hasMoreElements()) {
            String filePattern = (String) keys.nextElement();

            if (PatternComparator.patternMatch(filename, filePattern)) {
                return (associationMap.getProperty(filePattern));
            }
        }

        return (null);
    }

}
