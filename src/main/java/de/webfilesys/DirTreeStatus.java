package de.webfilesys;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

public class DirTreeStatus 
{
    private HashMap<String, Boolean> expandedDirs = null;
    
    private HashMap<String, Long> subdirNameLengthSumMap = null;

    public DirTreeStatus()
    {
    	expandedDirs = new HashMap<String, Boolean>();
    	subdirNameLengthSumMap = new HashMap<String, Long>();
    }
    
    public void expandDir(String path)
    {
        expandedDirs.put(path,new Boolean(true)); 
    }

    public void collapseDir(String path)
    {
        expandedDirs.remove(path);

        ArrayList<String> collapsedSubdirs = new ArrayList<String>();

        for (String dirName : expandedDirs.keySet()) {
            if (dirName.indexOf(path) == 0) {
                collapsedSubdirs.add(dirName);
            }
        }

        for (int i = 0; i < collapsedSubdirs.size(); i++) {
            expandedDirs.remove(collapsedSubdirs.get(i));
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

    public ArrayList<String> getExpandedFolders() {
    	ArrayList<String> expandedFolders = new ArrayList<String>();
    	
    	for (String path : expandedDirs.keySet()) {
    		Boolean expanded = expandedDirs.get(path);
    		if ((expanded != null) && expanded.booleanValue()) {
    			expandedFolders.add(path);
    		}
    	}
    	return expandedFolders;
    }
    
    public boolean dirExpanded(String path)
    {
        return(expandedDirs.get(path)!=null);
    }
    
    public void setSubdirNameLengthSum(String path, long nameLengthSum) {
    	subdirNameLengthSumMap.put(path, Long.valueOf(nameLengthSum));
    }
    
    public HashMap<String, Long> getSubdirNameLengthSumMap() {
    	return subdirNameLengthSumMap;
    }
    
    public long getSubdirNameLenghtSum(String path) {
    	Long nameLengthSum = subdirNameLengthSumMap.get(path);
    	if (nameLengthSum == null) {
    		return (-1);
    	}
    	return nameLengthSum.longValue();
    }
}
