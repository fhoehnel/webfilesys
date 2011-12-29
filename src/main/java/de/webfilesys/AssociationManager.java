package de.webfilesys;

import java.io.File;
import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Handles associations of application programs to file types.
 * @author Admin
 */
public class AssociationManager 
{
    public static final String ASSOCIATION_CONFIG_FILE = "fileTypeAssociations.conf";    
	
	private static AssociationManager assocMgr = null;
	
	private Properties associationMap = new Properties();
	
	private AssociationManager() 
	{
		associationMap = new Properties();

        File assocFile = new File(WebFileSys.getInstance().getConfigBaseDir() + File.separator + ASSOCIATION_CONFIG_FILE);
        if (assocFile.exists() && assocFile.canRead())
        {
            try
            {
            	FileInputStream fin = new FileInputStream(assocFile);
            	
            	associationMap.load(fin);
            	
            	if (Logger.getLogger(getClass()).isDebugEnabled()) 
            	{
            		Logger.getLogger(getClass()).debug("filetype associations loaded from " + ASSOCIATION_CONFIG_FILE);
            	}
            	
            	fin.close();
            }
            catch (Exception e)
            {
                Logger.getLogger(getClass()).error("cannot load filetype associations from " + ASSOCIATION_CONFIG_FILE + ": " + e);
            }
        }
        else
        {
            Logger.getLogger(getClass()).error("cannot load filetype associations from " + ASSOCIATION_CONFIG_FILE);
        }
		
	}
	
	public static AssociationManager getInstance()
	{
		if (assocMgr == null) 
		{
			assocMgr = new AssociationManager();
		}
		return assocMgr;
	}
	
	/**
	 * Returns the path of the application program which is assigned to the filetype of the given filename.
	 * @param filename the file name
	 * @return path of application program or null, if no association found
	 */
    public String getAssociatedProgram(String filename)
    {
        Enumeration<Object> keys = associationMap.keys();

        while (keys.hasMoreElements())
        {
            String filePattern = (String) keys.nextElement();

            if (pattern_match(filename, 0, filePattern, 0))
            {
                return (associationMap.getProperty(filePattern));
            }
        }

        return (null);
    }

    private static boolean pattern_match(String filename, int i, String pattern, int j)
    {
        if (i == filename.length())
        {
            if (j == pattern.length())
                return (true);
            return (false);
        }
        if (j == pattern.length())
            return (false);
        if (pattern.charAt(j) == '*')
        {
            while ((j < pattern.length()) && (pattern.charAt(j) == '*'))
                j++;
            if (j == pattern.length())
                return (true);
            while ((i < filename.length())
                && (Character.toUpperCase(filename.charAt(i))
                    != Character.toUpperCase(pattern.charAt(j))))
                i++;
            if (i == filename.length())
                return (false);

            return (pattern_match(filename, i + 1, pattern, j + 1));
        }
        if (Character.toUpperCase(filename.charAt(i))
            != Character.toUpperCase(pattern.charAt(j)))
            return (false);
        return (pattern_match(filename, i + 1, pattern, j + 1));
    }
}
