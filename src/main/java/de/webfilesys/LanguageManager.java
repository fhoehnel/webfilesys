package de.webfilesys;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;

public class LanguageManager
{
	public static final String DEFAULT_LANGUAGE = "Default";

	public static final String DEFAULT_HELP_LANGUAGE = "English";

    public static final String LANGUAGE_DIR    = "languages";

    private static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm");

    private Hashtable resourceTable;

    private Vector availableLanguages;

    private String defaultLanguage;

    private Hashtable dateFormats;

    private static LanguageManager languageMgr=null;
    
    private String languagePath = null;

    private LanguageManager(String defaultLang)
    {
    	languagePath = WebFileSys.getInstance().getConfigBaseDir() + "/" + LANGUAGE_DIR;
    	
        resourceTable=new Hashtable(5);

        availableLanguages=new Vector();

        defaultLanguage=defaultLang;

        dateFormats=new Hashtable(5);

        readAvailableLanguages();      
    }

    public static LanguageManager getInstance()
    {
        if (languageMgr==null)
        {
            languageMgr=new LanguageManager(null);
        }

        return(languageMgr);
    }

    public static LanguageManager getInstance(String defaultLang)
    {
        if (languageMgr==null)
        {
            languageMgr=new LanguageManager(defaultLang);
        }

        return(languageMgr);
    }
    
    protected void readAvailableLanguages()
    {
        File languageDir = new File(languagePath);

        if ((!languageDir.exists()) || (!languageDir.isDirectory()) || (!languageDir.canRead()))
        {
        	Logger.getLogger(getClass()).error("language directory not found or not readable: " + languageDir);
             
            return;
        } 

        String languageList[]=languageDir.list();

        for (int i=0;i<languageList.length;i++)
        {
             String langFileName=languageList[i];

             if (langFileName.endsWith(".resources"))
             {
                 File langFile=new File(languagePath + "/" + langFileName);

                 if (langFile.isFile() && langFile.canRead() && (langFile.length() > 0L))
                 {
                     String languageName=langFileName.substring(0,langFileName.lastIndexOf('.'));

                     availableLanguages.add(languageName);
                 }
             }
        }

        if (availableLanguages.size()>1)
        {
            Collections.sort(availableLanguages);
        }
    }

    public Vector getAvailableLanguages()
    {
        return(availableLanguages);
    }

    public String getResource(String language,String resource,String defaultValue)
    {
        if ((language==null) || language.equals(DEFAULT_LANGUAGE))
        {
            return(defaultValue);
        }

        Properties langResources=(Properties) resourceTable.get(language);

        if (langResources==null)        
        {
            String resourceFileName = languagePath + "/" + language + "." + "resources";

            langResources=new Properties();

            if (!loadResources(resourceFileName,langResources))
            {
                return(defaultValue);
            }
           
            resourceTable.put(language,langResources);
        }

        return(langResources.getProperty(resource,defaultValue));
    }

    protected synchronized boolean loadResources(String configFilename,Properties langResources)
    {
        FileInputStream configFile=null;

        Logger.getLogger(getClass()).info("Loading Resources from " + configFilename);

        try
        {
            configFile = new FileInputStream(configFilename);

            langResources.load(configFile);
        
            configFile.close();
        }
        catch(FileNotFoundException fnfe)
        {
            Logger.getLogger(getClass()).error("LanguageManager.loadResources: resource file not found : " + configFilename);
            return(false);
        }
        catch(IOException ioex)
        {
        	Logger.getLogger(getClass()).error("LanguageManager.loadResources: " + ioex);
            return(false);
        }

        return(true);
    }

    public void addDateFormat(String language,String dateFormat)
    {
        dateFormats.put(language,new SimpleDateFormat(dateFormat));
    }

    public SimpleDateFormat getDateFormat(String language)
    {
        if ((language==null) || (language.trim().length()==0) || language.equals(LanguageManager.DEFAULT_LANGUAGE))
        {
            return(DEFAULT_DATE_FORMAT);
        }
        
        SimpleDateFormat dateFormat=(SimpleDateFormat) dateFormats.get(language);

        if (dateFormat==null)
        {
            return(DEFAULT_DATE_FORMAT);
        }

        return(dateFormat);
    }

    public void listAvailableLanguages()
    {
        LanguageManager langMgr=LanguageManager.getInstance();

        Vector languageNames=langMgr.getAvailableLanguages();

        for (int i=0;i<languageNames.size();i++)
        {
        	Logger.getLogger(getClass()).info("available language: " + (String) languageNames.elementAt(i));
        }
    }
    
    public String getDefaultLanguage()
    {
    	return(defaultLanguage);
    }
}

