package de.webfilesys.util;

import java.util.*;

public class PatternComparator
{
    /**
     * Checks if a string matches a search pattern. The pattern can use the asterisk (*)
     * as wildcard character.<br>
     * The search is not case-sensitive.
     *
     * @param elementName the String to be checked against the search pattern
     * @param pattern the search pattern containing wildcards
     */
    public static boolean patternMatch(String elementName,String searchPattern)
    {
        int idx=0;
        String compName=elementName.toUpperCase();
        String pattern=searchPattern.toUpperCase();

        StringTokenizer patternParser=new StringTokenizer(pattern,"*",true);

        while (patternParser.hasMoreTokens())
        {
            String patternPart=patternParser.nextToken();

            if (patternPart.equals("*"))
            {
                if (!patternParser.hasMoreTokens())
                {
                    return(true);
                }

                patternPart=patternParser.nextToken();

                int addIdx=(-1);
                
                boolean found=false;
                while ((addIdx=compName.substring(idx).indexOf(patternPart))>=0)
                {
                    found=true;

                    idx+=addIdx;
                    idx+=patternPart.length();
                }
            
                if (!found)
                {
                    return(false);
                }
            }
            else
            {
                if (!compName.substring(idx).startsWith(patternPart))
                {
                    return(false);
                }
                
                idx+=patternPart.length();
            }

        }

        if (idx>=compName.length())
        {
            return(true);
        }
    
        return(false);
    }
}

