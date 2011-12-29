package de.webfilesys;

import java.util.*;
import java.io.*;

public class FileContainerComparator implements Comparator
{
	public static final int SORT_BY_FILENAME=1; 
	public static final int SORT_BY_CASESENSITIVE=2; 
	public static final int SORT_BY_EXTENSION=3; 
	public static final int SORT_BY_SIZE=4; 
	public static final int SORT_BY_DATE=5; 
    public static final int SORT_BY_VOTE_VALUE = 6; 
    public static final int SORT_BY_VOTE_COUNT = 7; 
    public static final int SORT_BY_VIEW_COUNT = 8; 
    public static final int SORT_BY_COMMENT_COUNT = 9; 
    
	int sortBy;
	String path;

	public FileContainerComparator(int sortBy)
	{
		this.sortBy=sortBy;
	}

	public int compare(Object o1,Object o2)
	{
		if (!o2.getClass().equals(o1.getClass()))
		{
			throw new ClassCastException();
		}
        
		FileContainer fileCont1 = (FileContainer) o1;
		FileContainer fileCont2 = (FileContainer) o2;

		String fileName1 = fileCont1.getName();
		String fileName2 = fileCont2.getName();

		if (sortBy==SORT_BY_FILENAME)
		{
			return(fileName1.toUpperCase().compareTo(fileName2.toUpperCase()));
		}

		if (sortBy==SORT_BY_CASESENSITIVE)
		{
			return(fileName1.compareTo(fileName2));
		}

		if (sortBy==SORT_BY_EXTENSION)
		{
			String ext1="";
			String ext2="";

			int extIdx=fileName1.lastIndexOf(".");
			if (extIdx>=0)
			{
				ext1=fileName1.substring(extIdx);
			}
            
			extIdx=fileName2.lastIndexOf(".");
			if (extIdx>=0)
			{
				ext2=fileName2.substring(extIdx);
			}

			return(ext1.toUpperCase().compareTo(ext2.toUpperCase()));
		}

		if (sortBy==SORT_BY_SIZE)
		{
            long fileSize1 = fileCont1.getSize();

			if (fileSize1 < 0L)
			{
				fileSize1 = fileCont1.getRealFile().length();

                fileCont1.setSize(fileSize1);
			}

			long fileSize2 = fileCont2.getSize();

			if (fileSize2 < 0L)
			{
				fileSize2 = fileCont2.getRealFile().length();

				fileCont2.setSize(fileSize2);
			}

			if (fileSize1 < fileSize2)
			{
				return(1);
			}
            
			if (fileSize1 > fileSize2)
			{
				return(-1);
			}

			return(0);
		}
        
		if (sortBy==SORT_BY_DATE)
		{
			long fileDate1 = fileCont1.getLastModified();

			if (fileDate1 < 0L)
			{
				fileDate1 = fileCont1.getRealFile().lastModified();

				fileCont1.setLastModified(fileDate1);
			}

			long fileDate2 = fileCont2.getLastModified();

			if (fileDate2 < 0L)
			{
				fileDate2 = fileCont2.getRealFile().lastModified();

				fileCont2.setLastModified(fileDate2);
			}
            
			if (fileDate1 < fileDate2)
			{
				return(1);
			}
            
			if (fileDate1 > fileDate2)
			{
				return(-1);
			}

			return(0);
		}

        if (sortBy == SORT_BY_VOTE_COUNT)
        {
            PictureRating rating1 = getPictureRating(fileCont1);
            PictureRating rating2 = getPictureRating(fileCont2);
            
            if (rating1 != null)
            {
                if (rating2 == null)
                {
                    return (-1);
                }
                if (rating1.getNumberOfVotes() > rating2.getNumberOfVotes())
                {
                    return (-1);
                }
                if (rating2.getNumberOfVotes() > rating1.getNumberOfVotes())
                {
                    return 1;
                }
                
                // equal vote count - consider vote value
                
                if (rating1.getAverageVisitorRating() > rating2.getAverageVisitorRating())
                {
                    return (-1);
                }
                if (rating2.getAverageVisitorRating() > rating1.getAverageVisitorRating())
                {
                    return 1;
                }
                
                return 0;
            }
            else
            {
                if (rating2 == null)
                {
                    // both files not rated yet - fallback to compare by filename
                    return(fileName1.toUpperCase().compareTo(fileName2.toUpperCase()));
                }
                return 1;
            }
        }
		
        if (sortBy == SORT_BY_VOTE_VALUE)
        {
            PictureRating rating1 = getPictureRating(fileCont1);
            PictureRating rating2 = getPictureRating(fileCont2);
            
            if (rating1 != null)
            {
                if (rating2 == null)
                {
                    return (-1);
                }
                if (rating1.getAverageVisitorRating() > rating2.getAverageVisitorRating())
                {
                    return (-1);
                }
                if (rating2.getAverageVisitorRating() > rating1.getAverageVisitorRating())
                {
                    return 1;
                }
                
                // equal vote value - consider vote count
                
                if (rating1.getNumberOfVotes() > rating2.getNumberOfVotes())
                {
                    return (-1);
                }
                if (rating2.getNumberOfVotes() > rating1.getNumberOfVotes())
                {
                    return 1;
                }
                return 0;
            }
            else
            {
                if (rating2 == null)
                {
                    // both files not rated yet - fallback to compare by filename
                    return(fileName1.toUpperCase().compareTo(fileName2.toUpperCase()));
                }
                return 1;
            }
        }
        
        if (sortBy == SORT_BY_VIEW_COUNT)
        {
            int viewCount1 = MetaInfManager.getInstance().getNumberOfDownloads(fileCont1.getRealFile().getAbsolutePath());
            int viewCount2 = MetaInfManager.getInstance().getNumberOfDownloads(fileCont2.getRealFile().getAbsolutePath());
        
            if (viewCount1 > viewCount2)
            {
                return (-1);
            }
            if (viewCount2 > viewCount1)
            {
                return 1;
            }
            return(fileName1.toUpperCase().compareTo(fileName2.toUpperCase()));
        }

        if (sortBy == SORT_BY_COMMENT_COUNT)
        {
            int commentCount1 = MetaInfManager.getInstance().countComments(fileCont1.getRealFile().getAbsolutePath());
            int commentCount2 = MetaInfManager.getInstance().countComments(fileCont2.getRealFile().getAbsolutePath());
            if (commentCount1 > commentCount2)
            {
                return (-1);
            }
            if (commentCount2 > commentCount1)
            {
                return 1;
            }
            return(fileName1.toUpperCase().compareTo(fileName2.toUpperCase()));
        }
        
        return(fileName1.toUpperCase().compareTo(fileName2.toUpperCase()));
	}

	private PictureRating getPictureRating(FileContainer fileCont)
	{
        File raelFile = fileCont.getRealFile();
        
        if (raelFile != null)
        {
            String filePath = raelFile.getAbsolutePath();
            
            if (filePath != null)
            {
                return MetaInfManager.getInstance().getPictureRating(filePath);
            }
        }
        return null;
	}
	
	/**
	 * returns true only if  obj == this
	 */
	public boolean equals(Object obj)
	{
		return obj.equals(this);
	}
}