package de.webfilesys;

import java.util.*;

/**
 * Comparator for FileSysBookmark objects.
 *
 */
public class FileSysBookmarkComparator implements Comparator
{
    public FileSysBookmarkComparator()
    {
    }

    public int compare(Object o1,Object o2)
    {
        if (!o2.getClass().equals(o1.getClass()))
        {
            throw new ClassCastException();
        }

        FileSysBookmark bookmark1 = (FileSysBookmark) o1;
        FileSysBookmark bookmark2 = (FileSysBookmark) o2;

        if ((bookmark1 == null) || (bookmark2 == null))
        {
            return(0);
        }
        
        if (bookmark1.getName()==null)
        {
            if (bookmark2.getName()==null)
            {
                return(0);
            }
            else
            {
                return(-1);
            }
        }
        else
        {
            if (bookmark2.getName()==null)
            {
                return(1);
            }
        }


        return(bookmark1.getName().compareToIgnoreCase(bookmark2.getName()));
    }

    public boolean equals(Object obj)
    {
        return(obj.equals(this));
    }
}

