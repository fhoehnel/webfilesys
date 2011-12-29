package de.webfilesys.user;

import java.util.Comparator;

/**
 * Compares two transient user objects. 
 * 
 * @author Frank Hoehnel
 */
public class UserComparator implements Comparator
{
	public static final int SORT_BY_USERID       = 1; 
	public static final int SORT_BY_FIRST_NAME   = 2; 
	public static final int SORT_BY_LAST_NAME    = 3; 
	public static final int SORT_BY_LAST_LOGIN   = 4; 
	public static final int SORT_BY_ROLE         = 5; 
    
	int sortBy;
	String path;

	public UserComparator(int sortBy)
	{
		this.sortBy=sortBy;
	}

	public int compare(Object o1,Object o2)
	{
		if (!o2.getClass().equals(o1.getClass()))
		{
			throw new ClassCastException();
		}
        
		TransientUser user1 = (TransientUser) o1;
		TransientUser user2 = (TransientUser) o2;
        
		if (sortBy==SORT_BY_USERID)
		{
			return(user1.getUserid().toUpperCase().compareTo(user2.getUserid().toUpperCase()));
		}

		if (sortBy==SORT_BY_ROLE)
		{
			return(user1.getRole().compareTo(user2.getRole()));
		}

		if (sortBy==SORT_BY_FIRST_NAME)
		{
			if (user1.getFirstName()==null)
			{
				if (user2.getFirstName()==null)
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
				if (user2.getFirstName()==null)
				{
					return(1);
				}
			}
			
			return(user1.getFirstName().toUpperCase().compareTo(user2.getFirstName().toUpperCase()));
		}

		if (sortBy==SORT_BY_LAST_NAME)
		{
			if (user1.getLastName()==null)
			{
				if (user2.getLastName()==null)
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
				if (user2.getLastName()==null)
				{
					return(1);
				}
			}

			return(user1.getLastName().toUpperCase().compareTo(user2.getLastName().toUpperCase()));
		}

		if (sortBy==SORT_BY_LAST_LOGIN)
		{
			if (user1.getLastLogin()==null)
			{
				if (user2.getLastLogin()==null)
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
				if (user2.getLastLogin()==null)
				{
					return(1);
				}
			}

			long date1=user1.getLastLogin().getTime();
			long date2=user2.getLastLogin().getTime();
			
			if (date1 > date2)
			{
				return(1);
			}

			if (date2 > date1)
			{
				return(-1);
			}
			
			return(0);
		}
        
		return(0);
	}

	/**
	 * returns true only if  obj == this
	 */
	public boolean equals(Object obj)
	{
		return obj.equals(this);
	}
}