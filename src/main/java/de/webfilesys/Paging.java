package de.webfilesys;

import java.util.*;

public class Paging
{
    public static final String PARAM_PAGE_SIZE   = "pageSize";
    public static final String PARAM_START_INDEX = "startIndex";

    public static int DEFAULT_PAGE_SIZE = 10;

    protected int pageSize=0;

    /** total number of elements in the list */
    private int elementNumber;

    /** the current page is the last one */
    private boolean lastPage;

    /** the current page is the first one */
    private boolean firstPage;

    /** index of the first element to be displayed (starting with 1) */
    private int startIndex;

    /** index of the last element to be displayed (starting with 1) */
    private int endIndex;

    /** start index of the following page (starting with 0) */
    private int nextStartIndex;

    /** start index of the previous page (starting with 0) */
    private int prevStartIndex;

    /** start index of last page */
    private int lastPageStartIndex;

    /** the name of the current pipeline */
    private String pipelineName;

    Vector pageStartIndices;

    Vector elementsOnPage=null;

    String listServletClass=null;

    protected void init()
    {
        elementNumber=0;
        lastPage=false;
        firstPage=false;
        startIndex=0;
        endIndex=0;
        nextStartIndex=0;
        prevStartIndex=0;
        pageStartIndices=new Vector();
        listServletClass=null;
    }

    public Paging(Vector elementList, int pageSize, int startIdx)
    {
        init();

        preparePaging(elementList, pageSize, startIdx);
    }

    public Paging(Enumeration elementList, int pageSize, int startIdx)
    {
        init();

        Vector elements=new Vector();

        while (elementList.hasMoreElements())
        {
            elements.add(elementList.nextElement());
        }

        preparePaging(elements, pageSize, startIdx);
    }

    public void preparePaging(Vector elementList, int pageSize, int startIdx)
    {
        if (startIdx > (elementList.size()-1)) {
            // element(s) removed from list - locate the previous page begin index
            if (startIdx > 0) 
            {
                startIdx--;
            }
            while ((startIdx > 0) && (startIdx % pageSize != 0)) 
            {
                startIdx--;
            }
        }
        
        int endIdx=startIdx + pageSize -1;

        if (endIdx>=(elementList.size()-1))
        {
            endIdx=elementList.size()-1;

            setLastPage(true);
        }
        else
        {
            setLastPage(false);
        }

        int lastPageStartIdx=0;

        for (int i=0;i<elementList.size();i+=pageSize)
        {
            addStartIndex(i);

            lastPageStartIdx=i;
        }

        setLastPageStartIndex(lastPageStartIdx);

        if (startIdx>0)
        {
            setFirstPage(false);
        }
        else
        {
            setFirstPage(true);
        }

        setStartIndex(startIdx+1);

        setEndIndex(endIdx+1);

        setElementNumber(elementList.size());

        setNextStartIndex(startIdx + pageSize);

        if (startIdx-pageSize>0)
        {
            setPrevStartIndex(startIdx - pageSize);
        }
        else
        {
            setPrevStartIndex(0);
        }

        elementsOnPage=new Vector();

        for (int i=startIdx;i<=endIdx;i++)
        {
            elementsOnPage.add(elementList.elementAt(i));
        }
    }

    public void setElementNumber(int newElementNumber)
    {
        elementNumber=newElementNumber;
    }

    public int getElementNumber()
    {
        return(elementNumber);
    }

    public void setLastPage(boolean lastPage)
    {
        this.lastPage=lastPage;
    }

    public boolean isLastPage()
    {
        return(lastPage);
    }

    public void setFirstPage(boolean firstPage)
    {
        this.firstPage=firstPage;
    }

    public boolean isFirstPage()
    {
        return(firstPage);
    }

    public void setStartIndex(int newStartIndex)
    {
        startIndex=newStartIndex;
    }

    public int getStartIndex()
    {
        return(startIndex);
    }

    public void setEndIndex(int newEndIndex)
    {
        endIndex=newEndIndex;
    }

    public int getEndIndex()
    {
        return(endIndex);
    }

    public void setNextStartIndex(int newStartIndex)
    {
        nextStartIndex=newStartIndex;
    }

    public int getPageSize()
    {
    	return(pageSize);
    }

    public int getNextStartIndex()
    {
        return(nextStartIndex);
    }

    public void setPrevStartIndex(int newStartIndex)
    {
        prevStartIndex=newStartIndex;
    }

    public int getPrevStartIndex()
    {
        return(prevStartIndex);
    }

    public void setLastPageStartIndex(int newIndex)
    {
        lastPageStartIndex=newIndex;
    }

    public int getLastPageStartIndex()
    {
        return(lastPageStartIndex);
    }

    public void addStartIndex(int startIndex)
    {
        pageStartIndices.addElement(new Integer(startIndex));
    }

    public Enumeration getStartIndices()
    {
        return(pageStartIndices.elements());
    }

    public Enumeration getElements()
    {
        return(elementsOnPage.elements());
    }

    public Vector getElementVector()
    {
        return(elementsOnPage);
    }
}


