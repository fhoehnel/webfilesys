package de.webfilesys;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


import de.webfilesys.util.CommonUtils;

public class LanguageManager {
    private static final Logger LOG = LogManager.getLogger(LanguageManager.class);

	public static final String DEFAULT_LANGUAGE = "English";
    public static final String LANGUAGE_DIR = "languages";

    private static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm");

    private final HashMap<String, Properties> resourceTable;

    private final ArrayList<String> availableLanguages;

    private final String defaultLanguage;

    private final HashMap<String, SimpleDateFormat> dateFormats;

    private static LanguageManager languageMgr = null;
    
    private String languagePath;

    private LanguageManager(String defaultLang) {
    	languagePath = WebFileSys.getInstance().getConfigBaseDir() + "/" + LANGUAGE_DIR;
        resourceTable = new HashMap<>(5);
        availableLanguages = new ArrayList<String>();
        defaultLanguage = defaultLang;
        dateFormats = new HashMap<>(5);
        readAvailableLanguages();
    }

    public static LanguageManager getInstance() {
        if (languageMgr == null) {
            languageMgr=new LanguageManager(null);
        }
        return languageMgr;
    }

    public static LanguageManager getInstance(String defaultLang) {
        if (languageMgr == null) {
            languageMgr = new LanguageManager(defaultLang);
        }
        return languageMgr;
    }
    
    protected void readAvailableLanguages() {
        File languageDir = new File(languagePath);
        if (!languageDir.exists() || !languageDir.isDirectory() || !languageDir.canRead()) {
            LOG.error("language directory not found or not readable: {}", languageDir);
            return;
        } 
        String[] languageList = languageDir.list();
        if (languageList != null) {
            for (String langFileName : languageList) {
                if (langFileName.endsWith(".resources")) {
                    File langFile = new File(languagePath + "/" + langFileName);
                    if (langFile.isFile() && langFile.canRead() && langFile.length() > 0L) {
                        String languageName = langFileName.substring(0, langFileName.lastIndexOf('.'));
                        availableLanguages.add(languageName);
                    }
                }
            }
        }
        if (availableLanguages.size()>1) {
            Collections.sort(availableLanguages);
        }
    }

    public ArrayList<String> getAvailableLanguages()
    {
        return availableLanguages;
    }

    public String getResource(String language,String resource,String defaultValue) {
        if (CommonUtils.isEmpty(language)) {
            return(defaultValue);
        }
        Properties langResources = resourceTable.get(language);
        if (langResources == null) {
            String resourceFileName = languagePath + "/" + language + "." + "resources";
            langResources = new Properties();
            if (!loadResources(resourceFileName,langResources, language)) {
                return defaultValue;
            }
        }
        return langResources.getProperty(resource,defaultValue);
    }

    public Properties getLanguageResources(String language) {
   	    Properties langResources = resourceTable.get(language);

        if (langResources == null) {
            String resourceFileName = languagePath + "/" + language + "." + "resources";
            langResources = new Properties();
            loadResources(resourceFileName, langResources, language);
        }
        return langResources;
    }
    
    protected synchronized boolean loadResources(String configFilename, Properties langResources, String language) {
        InputStreamReader configFile = null;
        LOG.info("Loading Resources from " + configFilename);
        try {
            configFile = new InputStreamReader(Files.newInputStream(Paths.get(configFilename)), StandardCharsets.UTF_8);
            langResources.load(configFile);
            resourceTable.put(language,langResources);
        } catch (IOException ioex) {
        	LOG.error("failed to load language resources", ioex);
            return(false);
        } finally {
			if (configFile != null) {
				try {
					configFile.close();
				} catch (IOException ex) {
				}
			}
		}
        if (!language.equals(DEFAULT_LANGUAGE)) {
        	mergeMissingResources(langResources);
        }
        return true;
    }

    private void mergeMissingResources(Properties langResources) {
        Properties defaultLangResources = resourceTable.get(DEFAULT_LANGUAGE);
        if (defaultLangResources == null) {
            String resourceFileName = languagePath + "/" + DEFAULT_LANGUAGE + "." + "resources";
            defaultLangResources = new Properties();
            if (!loadResources(resourceFileName, defaultLangResources, DEFAULT_LANGUAGE)) {
                return;
            }
        }
        Enumeration<Object> defaultLangKeys = defaultLangResources.keys();
        while (defaultLangKeys.hasMoreElements()) {
        	Object defaultLangKey = defaultLangKeys.nextElement();
        	if (!langResources.containsKey(defaultLangKey)) {
        		langResources.put(defaultLangKey, defaultLangResources.get(defaultLangKey));
        	}
        }
    }
    
    public void addDateFormat(String language,String dateFormat) {
        dateFormats.put(language,new SimpleDateFormat(dateFormat));
    }

    public SimpleDateFormat getDateFormat(String language) {
        if (language == null || language.trim().isEmpty() || language.equals(LanguageManager.DEFAULT_LANGUAGE)) {
            return DEFAULT_DATE_FORMAT;
        }
        SimpleDateFormat dateFormat = dateFormats.get(language);
        if (dateFormat == null) {
            return DEFAULT_DATE_FORMAT;
        }
        return dateFormat;
    }

    public void listAvailableLanguages() {
        LanguageManager langMgr = LanguageManager.getInstance();
        ArrayList<String> languageNames = langMgr.getAvailableLanguages();
        for (String languageName : languageNames) {
            LOG.info("available language: {}", languageName);
        }
    }
    
    public String getDefaultLanguage()
    {
    	return(defaultLanguage);
    }
}

