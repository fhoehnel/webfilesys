package de.webfilesys;

public class DirStat
{
   String dirName=null;
   long treeSize;
   int fileNum;

   public DirStat(String dirName)
   {
       this.dirName=dirName;
       treeSize=0L;
       fileNum=0;
   }

   public String getDirName()
   {
       return(dirName);
   }

   public void setTreeSize(long newTreeSize)
   {
       treeSize=newTreeSize;
   }

   public long getTreeSize()
   {
       return(treeSize);
   }

   public void setFileNum(int newFileNum)
   {
       fileNum=newFileNum;
   }

   public int getFileNum()
   {
       return(fileNum);
   }
}

