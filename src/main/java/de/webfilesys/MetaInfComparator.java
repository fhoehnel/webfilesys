package de.webfilesys;

import java.util.Comparator;

import org.w3c.dom.Element;

import de.webfilesys.util.XmlUtil;

public class MetaInfComparator implements Comparator
{
    public static final int SORT_BY_FILENAME   = 1; 
    public static final int SORT_BY_DOWNLOADS  = 2; 
    
    int sortType;

    public MetaInfComparator(int sortType)
    {
        this.sortType=sortType;
    }

    public int compare(Object o1,Object o2)
    {
        Element metaInf1=(Element) o1;
        Element metaInf2=(Element) o2;

        if (sortType==SORT_BY_FILENAME)
        {
            String fileName1=metaInf1.getAttribute("filename");
            String fileName2=metaInf2.getAttribute("filename");

            return(fileName1.compareToIgnoreCase(fileName2));
        }

        if (sortType==SORT_BY_DOWNLOADS)
        {
            String temp1 = XmlUtil.getChildText(metaInf1,"downloads");
            String temp2 = XmlUtil.getChildText(metaInf2,"downloads");

            int downloadNum1=0;
            int downloadNum2=0;

            try
            {
                downloadNum1=Integer.parseInt(temp1);
                downloadNum2=Integer.parseInt(temp2);
            }
            catch (NumberFormatException nfex)
            {
            }

            if (downloadNum1==downloadNum2)
            {
                return(0);
            }

            if (downloadNum1 < downloadNum2)
            {
                return(1);
            }

            return(-1);
        }

        // should never happen
        return(0);
    }

    public boolean equals(Object obj)
    {
        return obj.equals(this);
    }
}

