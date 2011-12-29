package de.webfilesys;

import java.util.*;

/**
 * Comparator for Categories
 *
 */
public class CategoryComparator implements Comparator
{
    public CategoryComparator()
    {
    }

    public int compare(Object o1,Object o2)
    {
        if (!o2.getClass().equals(o1.getClass()))
        {
            throw new ClassCastException();
        }

        Category cat1 = (Category) o1;
        Category cat2 = (Category) o2;

        if ((cat1 == null) || (cat2 == null))
        {
            return(0);
        }
        
        if (cat1.getName()==null)
        {
            if (cat2.getName()==null)
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
            if (cat2.getName()==null)
            {
                return(1);
            }
        }


        return(cat1.getName().compareToIgnoreCase(cat2.getName()));
    }

    public boolean equals(Object obj)
    {
        return(obj.equals(this));
    }
}

