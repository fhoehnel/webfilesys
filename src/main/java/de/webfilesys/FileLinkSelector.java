package de.webfilesys;

import java.io.File;
import java.util.Collections;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.webfilesys.util.PatternComparator;

public class FileLinkSelector
{
	private int sortBy;
	private Vector selectedFiles;
	private String path;
	private boolean hideMetaInf;

	public FileLinkSelector(String path,int sortBy)
	{
		this.path=path;
		this.sortBy=sortBy;
		selectedFiles=null;
		this.hideMetaInf=false;
	}

	public FileLinkSelector(String path,int sortBy,boolean hideMetaInf)
	{
		this.path=path;
		this.sortBy=sortBy;
		selectedFiles=null;
		this.hideMetaInf=hideMetaInf;
	}

    /**
     * Get a page of sorted files (including links).
     * @param searchMask
     * @param pageSize
     * @param afterName
     * @param beforeName
     * @return a FileSelectionStatus object containing selected FileContainer objects 
     */
	public FileSelectionStatus selectFiles(String searchMask[],int pageSize,
												  String afterName,String beforeName)
	{
		FileSelectionStatus selectionStatus=new FileSelectionStatus();

        Vector filesAndLinks = this.getFilesAndLinks();
        
		if ((filesAndLinks.size() ==0))
		{
			return(selectionStatus);
		}

		selectedFiles=new Vector();

		int selectedFileNumber=0;

		for (int i=0;i<filesAndLinks.size();i++)
		{
			FileContainer fileCont = (FileContainer) filesAndLinks.elementAt(i);

			String fileName = fileCont.getName();

			if ((!hideMetaInf) || (fileName.charAt(0)!='_') ||
				(!fileName.equals(MetaInfManager.METAINF_FILE)))
			{
				if (fileCont.getRealFile().isFile())
				{
					boolean maskMatch=false;

					for (int j=0;(!maskMatch) && (j<searchMask.length);j++)
					{
						if (PatternComparator.patternMatch(fileName,searchMask[j]))
						{
							maskMatch=true;
						}
					}

					if (maskMatch)
					{
						selectedFiles.addElement(fileCont);

						selectedFileNumber++;
					}
				}
				else
				{
					if (fileCont.isLink())
					{
						MetaInfManager.getInstance().removeLink(path,fileName);
						
						Logger.getLogger(getClass()).info("removing invalid link " + fileName + " in directory " + path + " pointing to " + fileCont.getRealFile().getAbsolutePath());
					}
				}
			}
		}

		if (selectedFiles.size() > 1)
		{
			Collections.sort(selectedFiles,new FileContainerComparator(sortBy));
		}

		selectionStatus.setNumberOfFiles(selectedFileNumber);
        
		// String lastFileOfAll=(String) selectedFiles.elementAt(selectedFiles.size()-1);

		String lastFileOfAll=null;
        
		if (selectedFiles.size()>0)
		{
			lastFileOfAll = ((FileContainer) selectedFiles.elementAt(selectedFiles.size()-1)).getName();
		}

		int beginIndex=(-1);
		int endIndex=0;
		boolean isLastPage=false;

		int i;
        
		Vector filesOnPage=new Vector();

		if ((afterName==null) && (beforeName==null))
		{
			if (selectedFiles.size()>pageSize)
			{
				for (i=0;i<pageSize;i++)
				{
					filesOnPage.addElement(selectedFiles.elementAt(i));
				}
			}
			else
			{
				filesOnPage=selectedFiles;
			}

			beginIndex=0;
			endIndex=filesOnPage.size();
		}
		else
		{
			if (afterName!=null)
			{
				boolean found=false;
				
				String upperCaseAfterName = afterName.toUpperCase();

				for (i=0;(i<selectedFiles.size()) && (!found);i++)
				{
					FileContainer fileCont = (FileContainer) selectedFiles.elementAt(i);
					
					String upperCaseFile = fileCont.getName().toUpperCase();

					if (upperCaseFile.compareTo(upperCaseAfterName)>0)
					{
						found=true;

						beginIndex=i;
					}
				}
                
				if (!found)
				{
					beginIndex = selectedFiles.size() - 1;
				}
			}

			if (beforeName!=null)
			{
				boolean found=false;

				String upperCaseBeforeName = beforeName.toUpperCase();

				for (i=selectedFiles.size()-1;(i>=0) && (!found);i--)
				{
					FileContainer fileCont = (FileContainer) selectedFiles.elementAt(i);
					
					String upperCaseFile = fileCont.getName().toUpperCase();

					if (upperCaseFile.compareTo(upperCaseBeforeName)<0)
					{
						found=true;
					}
				}

				beginIndex=i-pageSize+2;

				if (beginIndex<0)
				{
					beginIndex=0;
				}
			}
        
			endIndex=beginIndex + pageSize;

			if (endIndex>selectedFiles.size())
			{
				endIndex=selectedFiles.size();
			}

			for (i=beginIndex;i<endIndex;i++)
			{
				filesOnPage.addElement(selectedFiles.elementAt(i));
			}
		}

		selectionStatus.setBeginIndex(beginIndex);
		selectionStatus.setEndIndex(endIndex);

		if (selectedFiles.size()>0)
		{
		    FileContainer lastSelectedFile=(FileContainer) filesOnPage.elementAt(filesOnPage.size()-1);

			selectionStatus.setIsLastPage(lastSelectedFile.getName().equals(lastFileOfAll));
			selectionStatus.setFirstFileName(((FileContainer) filesOnPage.elementAt(0)).getName());
			selectionStatus.setLastFileName(lastSelectedFile.getName());
		}
         
		selectionStatus.setSelectedFiles(filesOnPage);

		return(selectionStatus);
	}

	/**
	 * Get a page of sorted files (including links) by start index.
	 * @param searchMask
	 * @param pageSize
	 * @param startIdx
	 * @return a FileSelectionStatus object containing selected FileContainer objects 
	 */
	public FileSelectionStatus selectFiles(String searchMask[], int pageSize, int startIdx)
	{
		return(selectFiles(searchMask, (-1), pageSize, startIdx));
	}

	/**
	 * Get a page of sorted files (including links) by start index.
	 * @param searchMask
	 * @param minRating minimum rating by owner
	 * @param pageSize
	 * @param startIdx
	 * @return a FileSelectionStatus object containing selected FileContainer objects 
	 */
	public FileSelectionStatus selectFiles(String searchMask[], int minRating, int pageSize, int startIdx)
	{
		long fileSizeSum = 0;
		
		FileSelectionStatus selectionStatus=new FileSelectionStatus();

		Vector filesAndLinks = this.getFilesAndLinks();
        
		if ((filesAndLinks.size() ==0))
		{
			return(selectionStatus);
		}

		MetaInfManager metaInfMgr = MetaInfManager.getInstance();
		
		selectedFiles=new Vector();

		int selectedFileNumber=0;

		for (int i=0;i<filesAndLinks.size();i++)
		{
			FileContainer fileCont = (FileContainer) filesAndLinks.elementAt(i);

			String fileName = fileCont.getName();

			if ((!hideMetaInf) || (fileName.charAt(0)!='_') ||
				(!fileName.equals(MetaInfManager.METAINF_FILE)))
			{
				if (fileCont.getRealFile().isFile())
				{
					boolean maskMatch=false;

					for (int j=0;(!maskMatch) && (j<searchMask.length);j++)
					{
						if (PatternComparator.patternMatch(fileName,searchMask[j]))
						{
							maskMatch=true;
						}
					}

					if (maskMatch)
					{
						if ((minRating < 0) || (metaInfMgr.getOwnerRating(fileCont.getRealFile().getAbsolutePath()) >= minRating))
						{
							selectedFiles.addElement(fileCont);

							selectedFileNumber++;
						}
					}
					
					if (!fileCont.isLink()) {
						fileSizeSum += fileCont.getRealFile().length();
					}
				}
				else
				{
					if (fileCont.isLink())
					{
						metaInfMgr.removeLink(path,fileName);
						
						Logger.getLogger(getClass()).info("removing invalid link " + fileName + " in directory " + path + " pointing to " + fileCont.getRealFile().getAbsolutePath());
					}
				}
			}
		}

		if (selectedFiles.size() > 1)
		{
			Collections.sort(selectedFiles,new FileContainerComparator(sortBy));
		}

		selectionStatus.setNumberOfFiles(selectedFileNumber);
        
		int beginIdx = startIdx;

		if (beginIdx > selectedFiles.size() - 1)
		{
			if (selectedFiles.size() > 0)
			{
				beginIdx = selectedFiles.size() - 1;
				
				while ((beginIdx > 0) && (beginIdx % pageSize !=0))
				{
					beginIdx--;
				}
			}
		}

		int endIdx = startIdx + pageSize -1;
		
		if (endIdx > selectedFiles.size() -1)
		{
			endIdx = selectedFiles.size() - 1;
		}

		Vector filesOnPage = new Vector();

        for (int i = beginIdx; i <= endIdx; i++)
        {
        	FileContainer fileOnPage = (FileContainer) selectedFiles.elementAt(i);
			filesOnPage.addElement(fileOnPage);
        }

		selectionStatus.setBeginIndex(beginIdx);
		selectionStatus.setEndIndex(endIdx);

        selectionStatus.setIsLastPage(endIdx == (selectedFiles.size() - 1));

		int lastPageStartIdx = 0;
        
		for (int i = 0; i < selectedFiles.size(); i += pageSize) 
		{
			selectionStatus.addStartIndex(i);
			
			lastPageStartIdx = i;
		}

		selectionStatus.setLastPageStartIdx(lastPageStartIdx);
		
		selectionStatus.setCurrentPage(startIdx / pageSize);
         
		selectionStatus.setSelectedFiles(filesOnPage);
		
		selectionStatus.setFileSizeSum(fileSizeSum);

		return(selectionStatus);
	}

    private Vector getFilesAndLinks()
    {
    	Vector filesAndLinks = new Vector();
    	
		File dirFile=new File(path);

		if ((!dirFile.exists()) || (!dirFile.canRead()))
		{
			return(filesAndLinks);
		}

		String fileList[] = dirFile.list();

		if ((fileList != null))
		{
            for (int i=0;i<fileList.length;i++)
            {
            	File tempFile = new File(path, fileList[i]);
            	
            	if (tempFile.isFile() && (tempFile.canRead()))
            	{
					filesAndLinks.add(new FileContainer(fileList[i],tempFile));
            	}
            }
		}

        Vector linkList = MetaInfManager.getInstance().getListOfLinks(path);
        
        if (linkList != null)
        {
        	for (int i=0;i<linkList.size();i++)
        	{
        		filesAndLinks.add(new FileContainer((FileLink) linkList.elementAt(i)));
        	}
        }
        
        return(filesAndLinks);
    }
    
}
