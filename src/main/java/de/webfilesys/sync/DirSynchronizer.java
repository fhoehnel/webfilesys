package de.webfilesys.sync;

import java.util.*;
import java.io.*;

import de.webfilesys.WebFileSys;

public class DirSynchronizer
{
    /** the granularity of file timestamps for FAT filesystems is 2 sec ! */
    private static final long TIMESTAMP_GRANULARITY = 2000;
    
    /** 
     * The FAT filesystem stores the file modification time in local time.
     * NTFS stores the file modification time in UTC.
     * During daylight saving time there is a one hour gap between file timestamps
     * on FAT and NTFS. This would cause every file to be marked as out-of-sync.
     * Config Parameter SnycIgnoreOffsetDST can be set to ignore such a one hour offset,
     * files with exactly one hour time difference are not handled as different.
     */
    private static final long DST_OFFSET = 60l * 60l * 1000l;
    
    private ArrayList differencesList = new ArrayList();

    private int idCounter = 0;
    
    private int missingTargetFiles = 0;
    
    private int missingTargetFolders = 0;
    
    private int missingSourceFiles = 0;
    
    private int missingSourceFolders = 0;
    
    private int modifiedFiles = 0;
    
    /** ignore differences of the modification date */
    private boolean ignoreDifferentDate = false;

    public DirSynchronizer(String sourcePath, String targetPath)
    {
        diff(sourcePath, targetPath);
    }

    public DirSynchronizer(String sourcePath, String targetPath, boolean ignoreDate)
    {
        ignoreDifferentDate = ignoreDate;
        diff(sourcePath, targetPath);
    }

    private void diff(String sourcePath, String targetPath)
    {
        File sourceDir = new File(sourcePath);
        File targetDir = new File(targetPath);

        boolean targetDirMissing = false;

        if ((!targetDir.exists()) || (!targetDir.isDirectory()))
        {
            // target directory does not exist

            SyncItem syncItem = new SyncItem(idCounter++);

            String dirName = sourcePath.substring(sourcePath
                    .lastIndexOf(File.separatorChar) + 1);

            syncItem.setFileName(dirName);
            syncItem.getSource().setPath(sourcePath);
            syncItem.getTarget().setPath(targetPath);
            syncItem.setDiffType(SyncItem.DIFF_TYPE_MISSING_TARGET_DIR);
            differencesList.add(syncItem);
            
            missingTargetFolders++;

            targetDirMissing = true;
        }

        Hashtable targetFiles = new Hashtable();
        Hashtable targetFolders = new Hashtable();

        if (!targetDirMissing)
        {
            String targetFileList[] = targetDir.list();

            for (int i = 0; i < targetFileList.length; i++)
            {
                File targetFile = new File(targetPath, targetFileList[i]);

                if (targetFile.isDirectory())
                {
                    targetFolders.put(targetFileList[i], new Integer(0));
                }
                else
                {
                    targetFiles.put(targetFileList[i], new Integer(0));
                }
            }
        }

        String sourceFileList[] = sourceDir.list();

        for (int i = 0; i < sourceFileList.length; i++)
        {
            String sourceFileName = sourceFileList[i];

            File sourceFile = new File(sourcePath, sourceFileName);

            if (sourceFile.isDirectory())
            {
                Integer targetFolderExist = (Integer) targetFolders
                        .get(sourceFileName);

                if (targetFolderExist == null)
                {
                    // target folder does not exist

                    SyncItem syncItem = new SyncItem(idCounter++);

                    syncItem.setFileName(sourceFileName);
                    syncItem.getSource().setPath(
                            sourcePath + File.separatorChar + sourceFileName);
                    syncItem.getTarget().setPath(
                            targetPath + File.separatorChar + sourceFileName);
                    syncItem.setDiffType(SyncItem.DIFF_TYPE_MISSING_TARGET_DIR);
                    differencesList.add(syncItem);
                    
                    missingTargetFolders++;

                    determineMissingTree(sourcePath + File.separatorChar
                            + sourceFileName, targetPath + File.separatorChar
                            + sourceFileName, false);
                }
                else
                {
                    targetFolders.put(sourceFileName, new Integer(1));
                    diff(sourcePath + File.separatorChar + sourceFileName,
                            targetPath + File.separatorChar + sourceFileName);
                }
            }
            else
            {
                Integer targetFileExist = (Integer) targetFiles
                        .get(sourceFileName);

                if (targetFileExist == null)
                {
                    // target file does not exist

                    SyncItem syncItem = new SyncItem(idCounter++);

                    syncItem.setFileName(sourceFileName);
                    syncItem.getSource().setPath(
                            sourcePath + File.separatorChar + sourceFileName);
                    syncItem.getTarget().setPath(
                            targetPath + File.separatorChar + sourceFileName);
                    syncItem
                            .setDiffType(SyncItem.DIFF_TYPE_MISSING_TARGET_FILE);
                    differencesList.add(syncItem);
                    
                    missingTargetFiles++;
                }
                else
                {
                    // both files do exist

                    boolean differentSize = false;
                    boolean differentDate = false;

                    File targetFile = new File(targetPath, sourceFileName);

                    long sourceSize = sourceFile.length();
                    long targetSize = targetFile.length();

                    if (sourceSize != targetSize)
                    {
                        differentSize = true;
                    }

                    long sourceModified = sourceFile.lastModified();
                    long targetModified = targetFile.lastModified();

                    if (!ignoreDifferentDate) 
                    {
                        long timestampDiff = Math.abs(sourceModified - targetModified);
                        
                        if (timestampDiff > TIMESTAMP_GRANULARITY)
                        {
                            if (!WebFileSys.getInstance().isSyncIgnoreOffsetDST() ||
                                (timestampDiff < DST_OFFSET - TIMESTAMP_GRANULARITY) ||
                                (timestampDiff > DST_OFFSET + TIMESTAMP_GRANULARITY))
                            {
                                differentDate = true;
                            }
                        }
                    }

                    if (differentSize || differentDate)
                    {
                        // file size or last modification time different

                        SyncItem syncItem = new SyncItem(idCounter++);

                        syncItem.setFileName(sourceFileName);
                        syncItem.getSource().setPath(
                                sourcePath + File.separatorChar
                                        + sourceFileName);
                        syncItem.getTarget().setPath(
                                targetPath + File.separatorChar
                                        + sourceFileName);

                        syncItem.getSource().setSize(sourceSize);
                        syncItem.getTarget().setSize(targetSize);

                        syncItem.getSource()
                                .setModificationTime(sourceModified);
                        syncItem.getTarget()
                                .setModificationTime(targetModified);

                        if (differentSize && differentDate)
                        {
                            syncItem.setDiffType(SyncItem.DIFF_TYPE_SIZE_TIME);
                        }
                        else if (differentSize)
                        {
                            syncItem.setDiffType(SyncItem.DIFF_TYPE_SIZE);
                        }
                        else
                        {
                            syncItem
                                    .setDiffType(SyncItem.DIFF_TYPE_MODIFICATION_TIME);
                        }

                        differencesList.add(syncItem);
                        
                        modifiedFiles++;
                    }
                    else
                    {
                        if (File.separatorChar == '\\')
                        {
                            // todo: implementation for UNIX
                            // check OS file permissions
                            
                            if ((sourceFile.canRead() != targetFile.canRead())
                                    || (sourceFile.canWrite() != targetFile
                                            .canWrite()))
                            {
                                SyncItem syncItem = new SyncItem(idCounter++);

                                syncItem.setFileName(sourceFileName);
                                syncItem.getSource().setPath(
                                        sourcePath + File.separatorChar
                                                + sourceFileName);
                                syncItem.getTarget().setPath(
                                        targetPath + File.separatorChar
                                                + sourceFileName);
                                syncItem
                                        .setDiffType(SyncItem.DIFF_TYPE_ACCESS_RIGHTS);
                                syncItem.getSource().setCanRead(
                                        sourceFile.canRead());
                                syncItem.getSource().setCanWrite(
                                        sourceFile.canWrite());
                                syncItem.getTarget().setCanRead(
                                        targetFile.canRead());
                                syncItem.getTarget().setCanWrite(
                                        targetFile.canWrite());

                                differencesList.add(syncItem);
                                
                                modifiedFiles++;
                            }
                        }
                    }

                    targetFiles.put(sourceFileName, new Integer(1));
                }
            }
        }

        Enumeration targetKeys = targetFiles.keys();

        while (targetKeys.hasMoreElements())
        {
            String targetKey = (String) targetKeys.nextElement();

            int targetFileChecked = ((Integer) targetFiles.get(targetKey))
                    .intValue();

            if (targetFileChecked == 0)
            {
                // source file does not exist

                SyncItem syncItem = new SyncItem(idCounter++);

                syncItem.setFileName(targetKey);
                syncItem.getSource().setPath(
                        sourcePath + File.separatorChar + targetKey);
                syncItem.getTarget().setPath(
                        targetPath + File.separatorChar + targetKey);
                syncItem.setDiffType(SyncItem.DIFF_TYPE_MISSING_SOURCE_FILE);
                differencesList.add(syncItem);
                missingSourceFiles++;
            }
        }

        targetKeys = targetFolders.keys();

        while (targetKeys.hasMoreElements())
        {
            String targetKey = (String) targetKeys.nextElement();

            int targetFolderChecked = ((Integer) targetFolders.get(targetKey))
                    .intValue();

            if (targetFolderChecked == 0)
            {
                // source folder does not exist

                SyncItem syncItem = new SyncItem(idCounter++);

                syncItem.setFileName(targetKey);
                syncItem.getSource().setPath(
                        sourcePath + File.separatorChar + targetKey);
                syncItem.getTarget().setPath(
                        targetPath + File.separatorChar + targetKey);
                syncItem.setDiffType(SyncItem.DIFF_TYPE_MISSING_SOURCE_DIR);
                differencesList.add(syncItem);
                
                missingSourceFolders++;

                determineMissingTree(targetPath + File.separatorChar
                        + targetKey, sourcePath + File.separatorChar
                        + targetKey, true);
            }
        }
    }

    private void determineMissingTree(String path, String missingPath,
            boolean missingSource)
    {
        File folder = new File(path);

        String fileList[] = folder.list();

        for (int i = 0; i < fileList.length; i++)
        {
            String fileName = fileList[i];

            SyncItem syncItem = new SyncItem(idCounter++);

            syncItem.setFileName(fileName);

            if (missingSource)
            {
                syncItem.getSource().setPath(
                        missingPath + File.separatorChar + fileName);
                syncItem.getTarget().setPath(
                        path + File.separatorChar + fileName);
            }
            else
            {
                syncItem.getTarget().setPath(
                        missingPath + File.separatorChar + fileName);
                syncItem.getSource().setPath(
                        path + File.separatorChar + fileName);
            }

            File file = new File(path, fileName);

            if (file.isDirectory())
            {
                if (missingSource)
                {
                    syncItem.setDiffType(SyncItem.DIFF_TYPE_MISSING_SOURCE_DIR);
                }
                else
                {
                    syncItem.setDiffType(SyncItem.DIFF_TYPE_MISSING_TARGET_DIR);
                }

                determineMissingTree(path + File.separatorChar + fileName,
                        missingPath + File.separatorChar + fileName,
                        missingSource);
            }
            else
            {
                if (missingSource)
                {
                    syncItem.setDiffType(SyncItem.DIFF_TYPE_MISSING_SOURCE_FILE);
                    missingSourceFiles++;
                }
                else
                {
                    syncItem.setDiffType(SyncItem.DIFF_TYPE_MISSING_TARGET_FILE);
                    missingTargetFiles++;
                }
            }

            differencesList.add(syncItem);
        }
    }

    public ArrayList getDifferences()
    {
        return differencesList;
    }
    
    public int getMissingSourceFiles() {
        return missingSourceFiles;
    }
    
    public int getMissingSourceFolders() {
        return missingSourceFolders;
    }
    
    public int getMissingTargetFiles() {
        return missingTargetFiles;
    }
    
    public int getMissingTargetFolders() {
        return missingTargetFolders;
    }
    
    public int getModifiedFiles() {
        return modifiedFiles;
    }
    
    public void decrMissingSourceFiles() {
        missingSourceFiles--;
    }

    public void decrMissingTargetFiles() {
        missingTargetFiles--;
    }

    public void decrModifiedFiles() {
        modifiedFiles--;
    }
}
