package de.webfilesys;

import java.io.File;
import java.util.Collections;
import java.util.Vector;

import de.webfilesys.util.PatternComparator;

public class FileSelector
{
    int sortBy;
    FileComparator fileComparator;
    Vector selectedFiles;
    String path;
    boolean hideMetaInf;

    public FileSelector(String path,int sortBy)
    {
        this.path=path;
        this.sortBy=sortBy;
        fileComparator=new FileComparator(path,sortBy);
        selectedFiles=null;
        this.hideMetaInf=false;
    }

    public FileSelector(String path,int sortBy,boolean hideMetaInf)
    {
        this.path=path;
        this.sortBy=sortBy;
        fileComparator=new FileComparator(path,sortBy);
        selectedFiles=null;
        this.hideMetaInf=hideMetaInf;
    }

    public FileSelectionStatus selectFiles(String searchMask[],int pageSize,
                                                  String afterName,String beforeName)
    {
        FileSelectionStatus selectionStatus=new FileSelectionStatus();
        
        File dirFile=new File(path);
        if ((!dirFile.exists()) || (!dirFile.canRead()))
        {
            return(selectionStatus);
        }

        String fileList[]=null;
        
        fileList=dirFile.list();

        if ((fileList==null) || (fileList.length==0))
        {
            return(selectionStatus);
        }

        selectedFiles=new Vector();

        int selectedFileNumber=0;

        for (int i=0;i<fileList.length;i++)
        {
            String fileName=fileList[i];

            if ((!hideMetaInf) || (fileName.charAt(0)!='_') ||
                (!fileName.equals(MetaInfManager.METAINF_FILE)))
            {
                File tempFile=new File(path + File.separator + fileName);

                if (tempFile.isFile())
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
                        selectedFiles.addElement(fileName);

                        selectedFileNumber++;
                    }
                }
            }
        }

        if (selectedFiles.size() > 1)
        {
            Collections.sort(selectedFiles,new FileComparator(path,sortBy));
        }

        selectionStatus.setNumberOfFiles(selectedFileNumber);
        
        // String lastFileOfAll=(String) selectedFiles.elementAt(selectedFiles.size()-1);

        String lastFileOfAll=null;
        
        if (selectedFiles.size()>0)
        {
            lastFileOfAll=(String) selectedFiles.elementAt(selectedFiles.size()-1);
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

                for (i=0;(i<selectedFiles.size()) && (!found);i++)
                {
                    String upperCaseFile=((String) selectedFiles.elementAt(i)).toUpperCase();

                    if (upperCaseFile.compareTo(afterName.toUpperCase())>0)
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

                for (i=selectedFiles.size()-1;(i>=0) && (!found);i--)
                {
                    String upperCaseFile=((String) selectedFiles.elementAt(i)).toUpperCase();

                    if (upperCaseFile.compareTo(beforeName.toUpperCase())<0)
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
            String lastSelectedFile=(String) filesOnPage.elementAt(filesOnPage.size()-1);

            selectionStatus.setIsLastPage(lastSelectedFile.equals(lastFileOfAll));
            selectionStatus.setFirstFileName((String) filesOnPage.elementAt(0));
            selectionStatus.setLastFileName(lastSelectedFile);
        }
        
        selectionStatus.setSelectedFiles(filesOnPage);

        return(selectionStatus);
    }

}
