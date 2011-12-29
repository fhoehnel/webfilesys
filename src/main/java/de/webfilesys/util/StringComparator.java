package de.webfilesys.util;

import java.util.Comparator;

public class StringComparator implements Comparator
{
    public static final int SORT_RESPECT_CASE = 1; 
    public static final int SORT_IGNORE_CASE  = 2; 
    
    int sortType;

    public StringComparator(int sortType)
    {
        this.sortType=sortType;
    }

    public int compare(Object o1,Object o2)
    {
        if (!o2.getClass().equals(o1.getClass()))
        {
            throw new ClassCastException();
        }
        
        String str1,str2;
        
        str1=(String) o1;
        str2=(String) o2;

        if (sortType==SORT_IGNORE_CASE)
        {
            return(str1.compareToIgnoreCase(str2));
        }
        
        return(str1.compareTo(str2));
    }

    public boolean equals(Object obj)
    {
        return obj.equals(this);
    }
}

