package de.webfilesys.gui.blog;

import java.util.Comparator;

public class BlogDateComparator
implements Comparator
{
    public BlogDateComparator()
    {
    }

    public int compare(Object o1, Object o2)
    {
        if (!o2.getClass().equals(o1.getClass()))
        {
            throw new ClassCastException();
        }

        String blogDate1 = (String) o1;
        String blogDate2 = (String) o2;

        /*
        if (blogDate1 == null) {
        	if (blogDate2 == null) {
        		return 0;
        	}
        	return -1;
        } else {
        	if (blogDate2 == null) {
        		return 1;
        	}
        }
        */
        
        int compResult = blogDate1.compareTo(blogDate2);
        
        if (compResult == 0) {
        	return 0;
        }
        
        if (compResult > 0) {
        	return (-1);
        }
        
        return 1;
    }

    public boolean equals(Object obj)
    {
        return(obj.equals(this));
    }
}


