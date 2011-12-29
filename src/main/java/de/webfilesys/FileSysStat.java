package de.webfilesys;

import java.io.*;
import java.util.*;

public class FileSysStat
{
    String path=null;

    long size_sum;
    int file_num;
    int subdir_num;
    int tree_file_num;
    long tree_file_size;
    int depth_of_tree;
    long totalSizeSum;
    long totalFileNum;

    long firstLevelSizeSum;
    long firstLevelFileNum;

    long totalSubdirNum;

    int maxLevel;

    long maxDirSize;

    public FileSysStat(String path)
    {
        this.path=path;

        totalSizeSum=0L;
        totalFileNum=0L;
        firstLevelSizeSum=0L;
        firstLevelFileNum=0L;

        totalSubdirNum=0;

        maxDirSize=0L;

        maxLevel=0;
    }

    public Vector getStatistics()
    {
        Vector statList=new Vector();

        File dirFile=new File(path);
        
        String fileList[]=dirFile.list();

        if (fileList==null)
        {
            return(null);
        }

        for (int i=0;i<fileList.length;i++)
        {
             String subDir=path + File.separator + fileList[i];

             File tempFile=new File(subDir);
             
             if (tempFile.isDirectory())
             {
                 totalSubdirNum++;

                 size_sum=0;
                 file_num=0;
                 subdir_num=0;
                 tree_file_num=0;
                 tree_file_size=0;
                 depth_of_tree=0;

                 explore(0,subDir);

                 DirStat dirStat=new DirStat(fileList[i]);

                 dirStat.setFileNum(tree_file_num);
                 dirStat.setTreeSize(tree_file_size);

                 statList.add(dirStat);

                 if (tree_file_size>maxDirSize)
                 {
                     maxDirSize=tree_file_size;
                 }
             }
             else
             {
                 if (tempFile.isFile())
                 {
                     totalSizeSum+=tempFile.length();

                     totalFileNum++;

                     firstLevelSizeSum+=tempFile.length();

                     firstLevelFileNum++;
                 }
             }
        }

        if (statList.size()==0)
        {
            return(null);
        }

        Collections.sort(statList,new DirComparator());

        return(statList);
    }

    public void explore(int level,String act_path)
    {
        File dir_file;
        File temp_file;
        int i;
        String sub_dir;
        String file_list[]=null;

        if (level+1>maxLevel)
        {
            maxLevel=level+1;
        }

        dir_file=new File(act_path);
        file_list=dir_file.list();

        if (file_list!=null)
        {
            for (i=0;i<file_list.length;i++)
            {
                temp_file=new File(act_path + File.separator + file_list[i]);
                if (temp_file.isDirectory())
                {
                    subdir_num++;

                    totalSubdirNum++;

                    if (level>depth_of_tree)
                        depth_of_tree=level;
                    sub_dir=new String(act_path + File.separator + file_list[i]);
                    explore(level+1,sub_dir);
                }
                else
                {
                    if (temp_file.isFile())
                    {
                        tree_file_size+=temp_file.length();
                        tree_file_num++;

                        totalSizeSum+=temp_file.length();
                        totalFileNum++;

                        if (level==0)
                        {
                            size_sum+=temp_file.length();
                            file_num++;
                        }
                    }
                }
            }
        }
        else
        {
            System.out.println("cannot get dir entries for " + act_path + "<br>");
        }
        file_list=null;
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
