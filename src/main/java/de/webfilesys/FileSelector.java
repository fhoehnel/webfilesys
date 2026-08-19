package de.webfilesys;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import de.webfilesys.util.PatternComparator;

public class FileSelector
{
    int sortBy;
    FileComparator fileComparator;
    ArrayList<String> selectedFiles;
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

    public FileSelectionStatus selectFiles(String[] searchMask,int pageSize,
                                                  String afterName,String beforeName) {
        FileSelectionStatus selectionStatus = new FileSelectionStatus();
        
        File dirFile = new File(path);
        if (!dirFile.exists() || !dirFile.canRead()) {
            return selectionStatus;
        }

        File[] fileList = dirFile.listFiles();

        if ((fileList == null) || (fileList.length == 0)) {
            return selectionStatus;
        }

        selectedFiles = new ArrayList<>();

        int selectedFileNumber = 0;

        for (File file : fileList) {
            String fileName = file.getName();

            if (!hideMetaInf || fileName.charAt(0) != '_' ||
                !fileName.equals(MetaInfManager.METAINF_FILE)) {
                if (file.isFile()) {
                    boolean maskMatch = false;
                    for (int j = 0; !maskMatch && j < searchMask.length; j++) {
                        if (PatternComparator.patternMatch(fileName, searchMask[j])) {
                            maskMatch = true;
                        }
                    }
                    if (maskMatch) {
                        selectedFiles.add(fileName);
                        selectedFileNumber++;
                    }
                }
            }
        }

        if (selectedFiles.size() > 1) {
            Collections.sort(selectedFiles,new FileComparator(path,sortBy));
        }

        selectionStatus.setNumberOfFiles(selectedFileNumber);
        
        // String lastFileOfAll=(String) selectedFiles.elementAt(selectedFiles.size()-1);

        String lastFileOfAll = null;
        if (!selectedFiles.isEmpty()) {
            lastFileOfAll = selectedFiles.get(selectedFiles.size() - 1);
        }

        int beginIndex = (-1);
        int endIndex;

        ArrayList<String> filesOnPage = new ArrayList<>();

        if (afterName == null && beforeName == null) {
            int toIndex = Math.min(pageSize, selectedFiles.size());
            filesOnPage = new ArrayList<>(selectedFiles.subList(0, toIndex));
            beginIndex = 0;
            endIndex = filesOnPage.size();
        } else {
            if (afterName != null) {
                boolean found = false;

                for (int i = 0; i < selectedFiles.size() && !found; i++) {
                    String upperCaseFile = selectedFiles.get(i).toUpperCase();
                    if (upperCaseFile.compareTo(afterName.toUpperCase()) > 0) {
                        found = true;
                        beginIndex = i;
                    }
                }
                if (!found) {
                	beginIndex = selectedFiles.size() - 1;
                }
            }

            if (beforeName != null) {
                boolean found = false;
                int i;
                for (i = selectedFiles.size() - 1; i >= 0 && !found; i--) {
                    String upperCaseFile= selectedFiles.get(i).toUpperCase();
                    if (upperCaseFile.compareTo(beforeName.toUpperCase()) < 0) {
                        found = true;
                    }
                }
                beginIndex = i - pageSize + 2;
                if (beginIndex < 0) {
                    beginIndex = 0;
                }
            }
        
            endIndex = beginIndex + pageSize;
            if (endIndex > selectedFiles.size()) {
                endIndex = selectedFiles.size();
            }

            for (int i = beginIndex; i < endIndex; i++) {
                filesOnPage.add(selectedFiles.get(i));
            }
        }

        selectionStatus.setBeginIndex(beginIndex);
        selectionStatus.setEndIndex(endIndex);

        if (!selectedFiles.isEmpty()) {
            String lastSelectedFile = filesOnPage.get(filesOnPage.size()-1);
            selectionStatus.setIsLastPage(lastSelectedFile.equals(lastFileOfAll));
            selectionStatus.setFirstFileName(filesOnPage.get(0));
            selectionStatus.setLastFileName(lastSelectedFile);
        }
        selectionStatus.setSelectedFileNames(filesOnPage);
        return selectionStatus;
    }

}
