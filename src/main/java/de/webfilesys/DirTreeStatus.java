package de.webfilesys;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

public class DirTreeStatus 
{
    private Hashtable expandedDirs = null;

    public DirTreeStatus()
    {
    	expandedDirs = new Hashtable();
    }
    
    public void expandDir(String path)
    {
        expandedDirs.put(path,new Boolean(true)); 
    }

    public void collapseDir(String path)
    {
        expandedDirs.remove(path);

        Vector collapsedSubdirs=new Vector();

        Enumeration dirList=expandedDirs.keys();

        while (dirList.hasMoreElements())
        {
            String dirName=(String) dirList.nextElement();

            if (dirName.indexOf(path)==0)
            {
                collapsedSubdirs.add(dirName);
            }
        }

        for (int i=0;i<collapsedSubdirs.size();i++)
        {
            expandedDirs.remove(collapsedSubdirs.elementAt(i));
        }
    }

    public void collapseAll()
    {
        expandedDirs.clear();
    }

    public void expandPath(String path)
    {
        StringTokenizer pathParser=new StringTokenizer(path,File.separator);

        StringBuffer partOfPath=new StringBuffer();
        
        boolean firstToken=true;
        
        if (File.separatorChar=='/')
        {
            expandedDirs.put("/",new Boolean(true)); 
            firstToken=false;
        }

        while (pathParser.hasMoreTokens())
        {
            String dirName=pathParser.nextToken();

            if (firstToken)
            {
                partOfPath.append(dirName);
                expandedDirs.put(partOfPath.toString() + File.separator,new Boolean(true)); 
                firstToken=false;
            }
            else
            {
                partOfPath.append(File.separatorChar);
                partOfPath.append(dirName);
                expandedDirs.put(partOfPath.toString(),new Boolean(true)); 
            }
        }
    }

    public boolean dirExpanded(String path)
    {
        return(expandedDirs.get(path)!=null);
    }
}
