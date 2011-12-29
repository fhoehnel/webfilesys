package de.webfilesys.stats;

import java.util.Comparator;

public class TypeCategoryComparator implements Comparator
{
	public static final int SORT_BY_FILE_EXT = 1;
	public static final int SORT_BY_FILE_NUM = 2;
	public static final int SORT_BY_SIZE_PERCENT = 3;
	
	private int sortBy = SORT_BY_FILE_NUM;
	
	public TypeCategoryComparator(int sortField) 
	{
	    sortBy = sortField;	
	}
	
    public int compare(Object o1,Object o2)
    {
    	if (!(o1 instanceof TypeCategory)) {
            throw new ClassCastException();
    	}
    	
        if (!o2.getClass().equals(o1.getClass()))
        {
            throw new ClassCastException();
        }

        TypeCategory cat1 = (TypeCategory) o1;
        TypeCategory cat2 = (TypeCategory) o2;

        if ((cat1 == null) || (cat2 == null))
        {
            return(0);
        }
        
        if (sortBy == SORT_BY_FILE_EXT) 
        {
        	return cat1.getFileExt().compareToIgnoreCase(cat2.getFileExt());
        }

        long val1 = 0L;
        long val2 = 0L;
        
        if (sortBy == SORT_BY_FILE_NUM) 
        {
        	val1 = cat1.getFileNum();
        	val2 = cat2.getFileNum();
        }

        if (sortBy == SORT_BY_SIZE_PERCENT) 
        {
        	val1 = cat1.getSizePercent();
        	val2 = cat2.getSizePercent();
        }

        if (val1 == val2) 
        {
            return cat1.getFileExt().compareToIgnoreCase(cat2.getFileExt());
        }
        if (val1 > val2) 
        {
        	return -1;
        }
        return 1;
    
    }

    public boolean equals(Object obj)
    {
        return(obj.equals(this));
    }

}
