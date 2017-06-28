package de.webfilesys.util;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * @author Frank Hoehnel
 */
public class CommonUtils
{
	public static String formatNumberSpaces(long number,int width)
	{
		char buffer[]=new char[width];

		StringBuffer formattedNumber=new StringBuffer();

		int idx=width-1;

		int digitNum=0;

		long num=number;

		while (num>0)
		{
			buffer[idx]=(char) (num % 10 + '0');

			num=num / 10;

			idx--;

			digitNum++;

			if ((digitNum % 3 == 0) && (num>0))
			{
				buffer[idx]='.';
				idx--;
			}
		}

		if (digitNum==0)
		{
			buffer[idx]='0';
			idx--;
		}

		int numberIdx=idx+1;

		while (idx>=0)
		{
			formattedNumber.append("&nbsp;");

			idx--;
		}                     

		formattedNumber.append(new String(buffer,numberIdx,width-numberIdx));

		return(formattedNumber.toString());
	}

	public static String formatNumber(long number,int width,boolean leadingZeros)
	{
		char buffer[]=new char[width];
		int idx=width-1;

		int digitNum=0;

		long num=number;

		while (num>0)
		{
			buffer[idx]=(char) (num % 10 + '0');

			num=num / 10;

			idx--;

			digitNum++;

			if ((digitNum % 3 == 0) && (num>0))
			{
				buffer[idx]='.';
				idx--;
			}
		}

		if (digitNum==0)
		{
			buffer[idx]='0';
			idx--;
		}

		while (idx>=0)
		{
			if (leadingZeros)
			{
				buffer[idx]='0';
			}
			else
			{
				buffer[idx]=' ';
			}

			idx--;
		}                     

		return(new String(buffer));
	}

	public static boolean isEmpty(String val) {
		if (val == null) {
			return true;
		}
		return (val.trim().length() == 0);
	}
	
	public static boolean containsString(String elementName,String searched)
	{
		if (elementName==null)
		{
			return(false);
		}

		return(elementName.toLowerCase().indexOf(searched.toLowerCase())>=0);
	}
	
	public static String shortName(String longName, int length)
	{
		int nameLength = longName.length();

		if (nameLength > length)
		{
			int headTailLength = (length - 4) / 2;
			
			return(longName.substring(0,headTailLength) + "..." + longName.substring(nameLength - headTailLength));
		}
		
		return(longName);
	}
	
	public static String getFileExtension(String fileName)
	{
        if (fileName == null)
        {
        	return("");
        }
        
		int extIdx = fileName.lastIndexOf(".");
			
		if (extIdx < 0)
		{
			return("");
		}

		return(fileName.substring(extIdx).toLowerCase());
	}
	
	public static String getFullPath(String path, String fileOrFolderName)
	{
	    if (path.endsWith(File.separator))
	    {
	        return path + fileOrFolderName;
	    }
	    
	    return(path + File.separator + fileOrFolderName);
	}
	
	public static final String extractFileName(String path)
	{
		String fileName = null;
		
		int lastSepIdx = path.lastIndexOf(File.separatorChar);
		
		if (lastSepIdx < 0)
		{
			lastSepIdx = path.lastIndexOf('/');
		}
		
		if ((lastSepIdx >= 0) && (lastSepIdx < path.length() - 1)) {
			fileName = path.substring(lastSepIdx + 1);
		}
		
		return(fileName);
	}
	
	public static boolean dirIsLink(File f)
	{
		if (File.separatorChar != '/')
		{
			return(false);
		}

		try
		{
			return(!(f.getCanonicalPath().equals(f.getAbsolutePath())));
		}
		catch (IOException ioex)
		{
			Logger.getLogger(CommonUtils.class).error(ioex);
			return(false);
		}
	}
	
	public static String readyForJavascript(String source)
	{
		StringBuffer result = new StringBuffer();
		
		for (int i = 0; i < source.length(); i++)
		{
			char c = source.charAt(i);
			
			if (c == '\n')
			{
				result.append("<br/>");				
			}
			else if (c == '\r')
			{
				result.append("<br/>");				
			}
			else if (c == '\'')
			{
				result.append('*');
			}
			else if (c == '\"')
			{
				result.append('*');
			}
			else
			{
				result.append(c);
			}
		}
		
		return(result.toString());
	}

	public static boolean deleteDirTree(String actPath)
	{ 
		boolean delError = false;

		File dirFile = new File(actPath);

		String fileList[] = dirFile.list();

		if (fileList!=null)
		{
			for (int i=0; i < fileList.length; i++)
			{
				File tempFile = new File(actPath, fileList[i]);
				
				if (tempFile.isDirectory())
				{
					if (!deleteDirTree(actPath + File.separator + fileList[i]))
					{
						delError=true;
					}
				}
				else
				{
					if (!tempFile.delete())
					{
						delError=true;
						Logger.getLogger(CommonUtils.class).warn("cannot delete " + tempFile);
					}
				}
			}
		}
		else
		{
			Logger.getLogger(CommonUtils.class).warn("cannot get dir entries for " + actPath);
		}
		
		fileList=null;

		if (!dirFile.delete())
		{
			delError=true;
		}

		return(!(delError));
	}
    
	/**
	 * Get the path of the parent directory of a given directory path.
	 * @param dirPath the path of the directory
	 * @return the path of the parent directory or null
	 */
	public static String getParentDir(String dirPath) 
	{
	    if (dirPath.length() < 2)
	    {
	        return null;
	    }
	    
	    int sepIdx = dirPath.lastIndexOf(File.separatorChar);
	    
	    if (sepIdx < 0) 
	    {
	        return null;
	    }
	    
	    if (sepIdx == 0) 
	    {
	        if (File.separatorChar == '/')
	        {
	            // return UNIX root dir
	            return "/";
	        }
	        return null;
	    }
	    
	    return dirPath.substring(0, sepIdx);
	}
	
    /**
     * String.replaceAll() exists only since Java 1.4 !
     * 
     * @param source
     * @param toReplace
     * @param replacement
     * @return
     */
    public static String replaceAll(String source, String toReplace, String replacement) 
    {
        int idx = source.lastIndexOf(toReplace);
        
        if (idx != (-1)) 
        {
            StringBuffer ret = new StringBuffer(source);

            ret.replace(idx, idx + toReplace.length(), replacement);
            
            while ((idx = source.lastIndexOf(toReplace, idx-1)) != (-1)) 
            {
                ret.replace(idx, idx + toReplace.length(), replacement);
            }

            source = ret.toString();
        }

        return source;
    }
    
    public static String encodeSpecialChars(String line)
    {
        StringBuffer buff = new StringBuffer();

        for (int i = 0; i < line.length(); i++)
        {
            char ch = line.charAt(i);

            if (ch=='&')
            {
                buff.append("&amp;");
            }
            else if (ch == '<')
            {
                buff.append("&lt;");
            }
            else if (ch == '>')
            {
                buff.append("&gt;");
            }
            else if (ch == '"')
            {
                buff.append("&quot;");
            }
            else
            {
                buff.append(ch);
            }
        }

        return(buff.toString());
    }

    public static final String escapeJSON(String s)
    {
        StringBuffer sb = new StringBuffer();
        int n = s.length();
        for (int i = 0; i < n; i++) 
        {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\"");
  		                  break;
                case '\'': sb.append("\\\'");
  		                  break;
              
               default:  sb.append(c);
           }
        }
        return sb.toString();
     }
    
	public static String escapeForJavascript(String source) {
		StringBuffer dest = new StringBuffer();

		for (int i = 0; i < source.length(); i++) {
			if (source.charAt(i)=='\\') {
				dest.append("\\\\");
			} else if (source.charAt(i)=='\'') {
				dest.append("\\\'");
			} else {
				dest.append(source.charAt(i));
			}
		}    
		return(dest.toString());
	}
    
    /**
     * This code is from http://www.rgagnon.com/javadetails/java-0306.html
     * @param s the original String
     * @return the HTML escaped String
     */
    public static final String escapeHTML(String s){
        StringBuffer sb = new StringBuffer();
        int n = s.length();
        for (int i = 0; i < n; i++) {
           char c = s.charAt(i);
           switch (c) {
              case '<': sb.append("&lt;"); break;
              case '>': sb.append("&gt;"); break;
              case '&': sb.append("&amp;"); break;
              case '"': sb.append("&quot;"); break;
              case 'à': sb.append("&agrave;");break;
              case 'À': sb.append("&Agrave;");break;
              case 'â': sb.append("&acirc;");break;
              case 'Â': sb.append("&Acirc;");break;
              case 'ä': sb.append("&auml;");break;
              case 'Ä': sb.append("&Auml;");break;
              case 'å': sb.append("&aring;");break;
              case 'Å': sb.append("&Aring;");break;
              case 'æ': sb.append("&aelig;");break;
              case 'Æ': sb.append("&AElig;");break;
              case 'ç': sb.append("&ccedil;");break;
              case 'Ç': sb.append("&Ccedil;");break;
              case 'é': sb.append("&eacute;");break;
              case 'É': sb.append("&Eacute;");break;
              case 'è': sb.append("&egrave;");break;
              case 'È': sb.append("&Egrave;");break;
              case 'ê': sb.append("&ecirc;");break;
              case 'Ê': sb.append("&Ecirc;");break;
              case 'ë': sb.append("&euml;");break;
              case 'Ë': sb.append("&Euml;");break;
              case 'ï': sb.append("&iuml;");break;
              case 'Ï': sb.append("&Iuml;");break;
              case 'ô': sb.append("&ocirc;");break;
              case 'Ô': sb.append("&Ocirc;");break;
              case 'ö': sb.append("&ouml;");break;
              case 'Ö': sb.append("&Ouml;");break;
              case 'ø': sb.append("&oslash;");break;
              case 'Ø': sb.append("&Oslash;");break;
              case 'ß': sb.append("&szlig;");break;
              case 'ù': sb.append("&ugrave;");break;
              case 'Ù': sb.append("&Ugrave;");break;         
              case 'û': sb.append("&ucirc;");break;         
              case 'Û': sb.append("&Ucirc;");break;
              case 'ü': sb.append("&uuml;");break;
              case 'Ü': sb.append("&Uuml;");break;
              case '®': sb.append("&reg;");break;         
              case '©': sb.append("&copy;");break;   
              case '€': sb.append("&euro;"); break;
              
              default:  sb.append(c); break;
           }
        }
        return sb.toString();
     }
    
	public static String[] splitPath(String path) {
		String dir = null;

		String fileName = null;

		int separatorIdx = path.lastIndexOf(File.separatorChar);
		if (separatorIdx < 0) {
	        separatorIdx = path.lastIndexOf('/');
	    }		

		if (separatorIdx > 0) {
			dir = path.substring(0, separatorIdx);
			fileName = path.substring(separatorIdx + 1);
		} else {
			if (separatorIdx == 0) {
				dir = path.substring(0, 1);
				fileName = path.substring(1);
			} else {
				dir = path;
				fileName = ".";
			}
		}

		String[] partsOfPath = new String[2];
		partsOfPath[0] = dir;
		partsOfPath[1] = fileName;

        return partsOfPath;		
	}
	
    public static String filterForbiddenChars(String text) {
    	StringBuffer cleanText = new StringBuffer();
    	
    	for (int i = 0; i < text.length(); i++) {
    		char c = text.charAt(i);
    		
    		if (c < 0xd800) {
    			cleanText.append(c);
    		} 
    	}
    	
    	return cleanText.toString();
    }
    
}
