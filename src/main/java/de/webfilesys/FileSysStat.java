package de.webfilesys;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.log4j.Logger;

public class FileSysStat {
    private String path = null;

    private int tree_file_num;
    private long tree_file_size;
    private int depth_of_tree;
    private long totalSizeSum;
    private long totalFileNum;

    private long firstLevelSizeSum;
    private long firstLevelFileNum;

    private long totalSubdirNum;

    private int maxLevel;

    private long maxDirSize;

    public FileSysStat(String path) {
        this.path = path;

        totalSizeSum = 0L;
        totalFileNum = 0L;
        firstLevelSizeSum = 0L;
        firstLevelFileNum = 0L;

        totalSubdirNum = 0;

        maxDirSize = 0L;

        maxLevel = 0;
    }

    public ArrayList<DirStat> getStatistics() {
    	ArrayList<DirStat> statList = new ArrayList<DirStat>();

        File dirFile = new File(path);
        
        File[] fileList = dirFile.listFiles();

        if (fileList == null) {
            return(null);
        }

        for (File file : fileList) {
             if (file.isDirectory()) {
                 totalSubdirNum++;

                 tree_file_num = 0;
                 tree_file_size = 0;
                 depth_of_tree = 0;

                 explore(0, file.getAbsolutePath());

                 DirStat dirStat = new DirStat(file.getName());

                 dirStat.setFileNum(tree_file_num);
                 dirStat.setTreeSize(tree_file_size);

                 statList.add(dirStat);

                 if (tree_file_size > maxDirSize) {
                     maxDirSize = tree_file_size;
                 }
             } else if (file.isFile()) {
                 totalSizeSum += file.length();
                 totalFileNum++;
                 firstLevelSizeSum += file.length();
                 firstLevelFileNum++;
             }
        }

        if (statList.size() == 0) {
            return(null);
        }

        Collections.sort(statList, new DirComparator());

        return(statList);
    }

    public void explore(int level, String currentPath) {
        if (level + 1 > maxLevel) {
            maxLevel = level + 1;
        }

        File dirFile = new File(currentPath);
        
        File[] fileList = dirFile.listFiles();
        
        if (fileList != null) {
            for (File file : fileList) {
                if (file.isDirectory()) {
                    totalSubdirNum++;
                    if (level > depth_of_tree) {
                        depth_of_tree = level;
                    }
                    explore(level + 1, file.getAbsolutePath());
                    
                } else if (file.isFile()) {
                    tree_file_size += file.length();
                    tree_file_num++;

                    totalSizeSum += file.length();
                    totalFileNum++;
                }
            }
        } else {
        	Logger.getLogger(getClass()).warn("cannot get dir entries for " + currentPath);
        }
    }

    public long getSizeSum()
    {
        return(totalSizeSum);
    }
    
    public long getMaxDirSize()
    {
        return(maxDirSize);
    }

    public long getTotalSizeSum()
    {
        return(totalSizeSum);
    }

    public long getTotalFileNum()
    {
        return(totalFileNum);
    }

    public long getTotalSubdirNum()
    {
        return(totalSubdirNum);
    }

    public long getFirstLevelSizeSum()
    {
        return(firstLevelSizeSum);
    }

    public long getFirstLevelFileNum()
    {
        return(firstLevelFileNum);
    }

    public int getMaxLevel()
    {
        return(maxLevel);
    }

    public class DirComparator implements Comparator
    {
    
        public int compare(Object o1,Object o2)
        {
            if (!o2.getClass().equals(o1.getClass()))
            {
                throw new ClassCastException();
            }
        
            DirStat dirStat1=(DirStat) o1;
            DirStat dirStat2=(DirStat) o2;

            if (dirStat1.getTreeSize()==dirStat2.getTreeSize())
            {
                return(0);
            }

            if (dirStat1.getTreeSize()<dirStat2.getTreeSize())
            {
                return(1);
            }

            return(-1);
        }
        
        public boolean equals(Object obj)
        {
            return obj.equals(this);
        }
    }

}
