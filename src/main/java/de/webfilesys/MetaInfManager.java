package de.webfilesys;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

public class MetaInfManager extends Thread
{
    public static final String METAINF_FILE = "_metainf.fmweb";

	/** maximum length of description meta info text */
	public static final int MAX_DESCRIPTION_LENGTH = 1024;

    private static MetaInfManager metaInfMgr=null;

    private Hashtable dirList=null;

    private Hashtable cacheDirty=null;

    DocumentBuilder builder;

    private MetaInfManager()
    {
       builder = null;

       try
       {
           DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
           builder = factory.newDocumentBuilder();
       }
       catch (ParserConfigurationException pcex)
       {
           Logger.getLogger(getClass()).error(pcex);
       }

       dirList=new Hashtable();

       cacheDirty=new Hashtable();

       this.start();
    }

    public static MetaInfManager getInstance()
    {
        if (metaInfMgr==null)
        {
            metaInfMgr=new MetaInfManager();
        }

        return(metaInfMgr);
    }

    public void saveMetaInfFile(String path)
    {
        Element metaInfRoot=(Element) dirList.get(path);

        if (metaInfRoot==null)
        {
            return;
        }
        
        String metaInfFileName=null;

        if (path.endsWith(File.separator))
        {
            metaInfFileName=path + METAINF_FILE;
        }
        else
        {
            metaInfFileName=path + File.separator + METAINF_FILE;
        }

        NodeList metaInfList = metaInfRoot.getElementsByTagName("metainf");

        if ((metaInfList==null) || (metaInfList.getLength()==0))
        {
            File metaInfFile=new File(metaInfFileName);

            if (metaInfFile.exists() && metaInfFile.canWrite())
            {
                if (Logger.getLogger(getClass()).isDebugEnabled())
                {
                    Logger.getLogger(getClass()).debug("removing empty meta inf file " + metaInfFileName);
                }
                
                metaInfFile.delete();
            }

            return;
        }

        if (Logger.getLogger(getClass()).isDebugEnabled())
        {
            Logger.getLogger(getClass()).debug("saving meta info to file: " + metaInfFileName);
        }

        synchronized (metaInfRoot)
        {
            OutputStreamWriter xmlOutFile = null;
            
            try
            {
                FileOutputStream fos = new FileOutputStream(metaInfFileName);
                
                xmlOutFile = new OutputStreamWriter(fos, "UTF-8");
                
                XmlUtil.writeToStream(metaInfRoot, xmlOutFile);
                
                xmlOutFile.flush();
            }
            catch (IOException ioex)
            {
                Logger.getLogger(getClass()).error("error saving metainf file : " + metaInfFileName, ioex);
            }
            finally
            {
                if (xmlOutFile != null)
                {
                    try 
                    {
                        xmlOutFile.close();
                    }
                    catch (Exception ex) 
                    {
                    }
                }
            }
        }
    }

    public synchronized Element loadMetaInfFile(String path)
    {
       String metaInfFileName=null;
       
       if (path.endsWith(File.separator))
       {
           metaInfFileName=path + METAINF_FILE;
       }
       else
       {
           metaInfFileName=path + File.separator + METAINF_FILE;
       }

       File metaInfFile = new File(metaInfFileName);

       if ((!metaInfFile.exists()) || (!metaInfFile.canRead()))
       {
           return(null);
       }
       
       Document doc = null;

       FileInputStream fis = null;

       try
       {
           fis = new FileInputStream(metaInfFile);
           
           InputSource inputSource = new InputSource(fis);
           
           inputSource.setEncoding("UTF-8");

           if (Logger.getLogger(getClass()).isDebugEnabled())
           {
               Logger.getLogger(getClass()).debug("reading meta info from " + metaInfFileName);
           }

           doc = builder.parse(inputSource);
       }
       catch (SAXException saxex)
       {
           Logger.getLogger(getClass()).error("Failed to load metainf file : " + metaInfFileName, saxex);
       }
       catch (IOException ioex)
       {
           Logger.getLogger(getClass()).error("Failed to load metainf file : " + metaInfFileName, ioex);
       }
       finally 
       {
           if (fis != null)
           {
               try
               {
                   fis.close();
               }
               catch (Exception ex)
               {
               }
           }
       }

       if (doc == null)
       {
           return(null);
       }

       return doc.getDocumentElement();
    }

    public boolean dirHasMetaInf(String path)
    {
        Element metaInfRoot=(Element) dirList.get(path);

        if (metaInfRoot==null)
        {
            metaInfRoot=loadMetaInfFile(path);
        
            if (metaInfRoot!=null)
            {
                dirList.put(path,metaInfRoot);
            }
        }

        return(metaInfRoot!=null);
    }

    public Element getMetaInfElement(String absoluteFileName)
    {
        String path=null;
        String fileName=null;

        int separatorIdx=absoluteFileName.lastIndexOf(File.separator);

        if (separatorIdx<0)
        {
            separatorIdx=absoluteFileName.lastIndexOf('/');
        }

        if (separatorIdx<0)
        {
            return(getMetaInfElement(path,absoluteFileName));
        }

        return(getMetaInfElement(absoluteFileName.substring(0,separatorIdx),
                                 absoluteFileName.substring(separatorIdx+1)));
    }

    public synchronized Element getMetaInfElement(String path,String fileName)
    {
        Element metaInfRoot = null;
        
        metaInfRoot = (Element) dirList.get(path);

        if (metaInfRoot==null)
        {
            metaInfRoot = loadMetaInfFile(path);
            
            if (metaInfRoot == null)
            {
                return(null);
            }
            else
            {
                dirList.put(path,metaInfRoot);
            }
        }

        NodeList metaInfList = metaInfRoot.getElementsByTagName("metainf");

        if (metaInfList == null)
        {
            return null;
        }

        int listLength=metaInfList.getLength();

        for (int i=0;i<listLength;i++)
        {
            Element metaInfElement=(Element) metaInfList.item(i);

            String key=metaInfElement.getAttribute("filename");

            if ((key!=null) && key.equals(fileName))
            {
                return(metaInfElement);
            }
        }
        
        return(null);
    }

	public Element createMetaInfElement(String absoluteFileName)
	{
		String path = null;
		String fileName = null;

		int separatorIdx = absoluteFileName.lastIndexOf(File.separator);

		if (separatorIdx < 0)
		{
			separatorIdx=absoluteFileName.lastIndexOf('/');
		}

		if (separatorIdx < 0)
		{
			return(createMetaInfElement(path, absoluteFileName));
		}

		return(createMetaInfElement(absoluteFileName.substring(0,separatorIdx),
								    absoluteFileName.substring(separatorIdx+1)));
	}

    protected Element createMetaInfElement(String path, String fileName)
    {
        Element metaInfRoot=(Element) dirList.get(path);

        if (metaInfRoot==null)
        {
            metaInfRoot=loadMetaInfFile(path);
        }

        if (metaInfRoot==null)
        {
            Document doc = builder.newDocument();

            metaInfRoot = doc.createElement("metainfroot");

            dirList.put(path, metaInfRoot);
        }

        Element metaInfElement = metaInfRoot.getOwnerDocument().createElement("metainf");

        metaInfElement.setAttribute("filename",fileName);
        
        metaInfRoot.appendChild(metaInfElement);

        cacheDirty.put(path, new Boolean(true));

        return(metaInfElement);
    }

    public String getDescription(String absoluteFileName)
    {
        Element metaInfElement=getMetaInfElement(absoluteFileName);

        if (metaInfElement==null)
        {
            return(null);
        }

        return(XmlUtil.getChildText(metaInfElement,"description"));
    }

    public String getDescription(String path,String fileName)
    {
        Element metaInfElement=getMetaInfElement(path,fileName);

        if (metaInfElement==null)
        {
            return(null);
        }

        return(XmlUtil.getChildText(metaInfElement,"description"));
    }

    public String getShortDescription(String absoluteFileName,int maxLength)
    {
        Element metaInfElement=getMetaInfElement(absoluteFileName);

        if (metaInfElement==null)
        {
            return(null);
        }

        String description=XmlUtil.getChildText(metaInfElement,"description");

        if ((description.length() > maxLength) && (maxLength>3))
        {
            description=description.substring(0,maxLength-3) + "...";
        }

        return(description);
    }

    public String getShortDescription(String path,String fileName,int maxLength)
    {
        Element metaInfElement=getMetaInfElement(path,fileName);

        if (metaInfElement==null)
        {
            return(null);
        }

        String description=XmlUtil.getChildText(metaInfElement,"description");

        if ((description.length() > maxLength) && (maxLength>3))
        {
            description=description.substring(0,maxLength-3) + "...";
        }

        return(description);
    }

    public void setDescription(String path,String newDescription)
    {
        String dir=null;

        String fileName=null;

        int separatorIdx=path.lastIndexOf(File.separatorChar);

        if (separatorIdx>0)
        {
            dir=path.substring(0,separatorIdx);
            fileName=path.substring(separatorIdx+1);
        }
        else
        {
            if (separatorIdx==0)
            {
                dir=path.substring(0,1);
                fileName=path.substring(1);
            }
        }

        setDescription(dir,fileName,newDescription);
    }

    public void setDescription(String path,String fileName,String newDescription)
    {
        Element metaInfElement=getMetaInfElement(path,fileName);
        
        if (metaInfElement==null)
        {
            metaInfElement=createMetaInfElement(path,fileName);
        }

        XmlUtil.setChildText(metaInfElement,"description",newDescription,true);
        
        cacheDirty.put(path,new Boolean(true));
        // saveMetaInfFile(path);
    }

    public void removeMetaInf(String absoluteFileName)
    {
        String path=null;
        String fileName=null;

        int separatorIdx=absoluteFileName.lastIndexOf(File.separator);

        if (separatorIdx<0)
        {
            separatorIdx=absoluteFileName.lastIndexOf('/');
        }

        if (separatorIdx<0)
        {
            // removeDescription(path,absoluteFileName);
            return;
        }

        removeMetaInf(absoluteFileName.substring(0,separatorIdx),
                      absoluteFileName.substring(separatorIdx+1));
    }

    public void removeMetaInf(String path,String fileName)
    {
    	Element metaInfElement = getMetaInfElement(path,fileName);
    	
    	if (metaInfElement!=null)
    	{
			Element metaInfRoot=(Element) dirList.get(path);

			metaInfRoot.removeChild(metaInfElement);                
                
			cacheDirty.put(path,new Boolean(true));
    	}
    }
    
    public void removeDescription(String absoluteFileName)
    {
		Element metaInfElement = getMetaInfElement(absoluteFileName);
		
		if (metaInfElement!=null)
		{
			Element descElement = XmlUtil.getChildByTagName(metaInfElement,"description");
			
			if (descElement != null)
			{
				metaInfElement.removeChild(descElement);
			}
		}
    }

    public void removePath(String path)
    {
        dirList.remove(path);

        cacheDirty.remove(path);
    }

    public void addComment(String absoluteFileName,Comment newComment)
    {
        String path=null;
        String fileName=null;

        int separatorIdx=absoluteFileName.lastIndexOf(File.separator);

        if (separatorIdx<0)
        {
            separatorIdx=absoluteFileName.lastIndexOf('/');
        }

        if (separatorIdx<0)
        {
            addComment(absoluteFileName,".",newComment);
            return;
        }

        addComment(absoluteFileName.substring(0,separatorIdx),
                   absoluteFileName.substring(separatorIdx+1),
                   newComment);
    }

    public void addComment(String path,String fileName,Comment newComment)
    {
        Element metaInfElement=getMetaInfElement(path,fileName);
        
        if (metaInfElement==null)
        {
            metaInfElement=createMetaInfElement(path,fileName);
        }

        Document doc=metaInfElement.getOwnerDocument();

        Element commentListElement=XmlUtil.getChildByTagName(metaInfElement,"comments");

        if (commentListElement==null)
        {
            commentListElement=doc.createElement("comments");

            metaInfElement.appendChild(commentListElement);
        }

        Element commentElement=doc.createElement("comment");

        commentListElement.appendChild(commentElement);

        XmlUtil.setChildText(commentElement,"user",newComment.getUser(),false);
        XmlUtil.setChildText(commentElement,"time",Long.toString(newComment.getCreationTime()),false);
        XmlUtil.setChildText(commentElement,"message",newComment.getMessage(),true);
        
        cacheDirty.put(path,new Boolean(true));
    }

    public void removeComments(String absoluteFileName)
    {
        String path=null;
        String fileName=null;

        int separatorIdx=absoluteFileName.lastIndexOf(File.separator);

        if (separatorIdx<0)
        {
            separatorIdx=absoluteFileName.lastIndexOf('/');
        }

        if (separatorIdx<0)
        {
            removeComments(absoluteFileName,".");
            return;
        }

        removeComments(absoluteFileName.substring(0,separatorIdx),
                       absoluteFileName.substring(separatorIdx+1));
    }

    public void removeComments(String path,String fileName)
    {
        Element metaInfElement=getMetaInfElement(path,fileName);
        
        if (metaInfElement==null)
        {
            return;
        }
    
        Element commentListElement=XmlUtil.getChildByTagName(metaInfElement,"comments");

        if (commentListElement==null)
        {
            return;
        }

        metaInfElement.removeChild(commentListElement);

        cacheDirty.put(path,new Boolean(true));
    }

    public Vector getListOfComments(String absoluteFileName)
    {
        String path=null;
        String fileName=null;

        int separatorIdx=absoluteFileName.lastIndexOf(File.separator);

        if (separatorIdx<0)
        {
            separatorIdx=absoluteFileName.lastIndexOf('/');
        }

        if (separatorIdx<0)
        {
            return(getListOfComments(absoluteFileName,"."));
        }

        return(getListOfComments(absoluteFileName.substring(0,separatorIdx),
                                 absoluteFileName.substring(separatorIdx+1)));
    }

    public Vector getListOfComments(String path, String fileName)
    {
        Element metaInfElement=getMetaInfElement(path,fileName);

        if (metaInfElement==null)
        {
            return(null);
        }

        Element commentListElement=XmlUtil.getChildByTagName(metaInfElement,"comments");

        if (commentListElement==null)
        {
            return(null);
        }
        
        NodeList commentList=commentListElement.getElementsByTagName("comment");

        if (commentList==null)
        {
            return(null);
        }

        int listLength=commentList.getLength();

        if (listLength==0)
        {
            return(null);
        }

        Vector listOfComments=new Vector();

        for (int i=0;i<listLength;i++)
        {
            Element commentElement=(Element) commentList.item(i);

            String user=XmlUtil.getChildText(commentElement,"user");
            String message=XmlUtil.getChildText(commentElement,"message");

            String tmp=XmlUtil.getChildText(commentElement,"time");

            long creationTime=0L;

            try
            {
                creationTime=Long.parseLong(tmp);
            }
            catch (NumberFormatException nfex)
            {
                Logger.getLogger(getClass()).warn("invalid creation time: " + tmp);
            }

            Comment comment=new Comment(user,new Date(creationTime),message);

            listOfComments.add(comment);
        }

        return(listOfComments);
    }

	public int countComments(String absoluteFileName)
	{
		int separatorIdx=absoluteFileName.lastIndexOf(File.separator);

		if (separatorIdx<0)
		{
			separatorIdx=absoluteFileName.lastIndexOf('/');
		}

		if (separatorIdx<0)
		{
			return(countComments(absoluteFileName,"."));
		}

		return(countComments(absoluteFileName.substring(0,separatorIdx),
						     absoluteFileName.substring(separatorIdx+1)));
	}

	public int countComments(String path, String fileName)
	{
		Element metaInfElement=getMetaInfElement(path,fileName);

		if (metaInfElement==null)
		{
			return(0);
		}

		Element commentListElement=XmlUtil.getChildByTagName(metaInfElement,"comments");

		if (commentListElement==null)
		{
			return(0);
		}
        
		NodeList commentList=commentListElement.getElementsByTagName("comment");

		if (commentList==null)
		{
			return(0);
		}

		return(commentList.getLength());
	}

	public void setOwnerRating(String path, int rating)
	{
		String dir = null;

		String fileName = null;

		int separatorIdx = path.lastIndexOf(File.separatorChar);

		if (separatorIdx > 0)
		{
			dir = path.substring(0, separatorIdx);
			fileName = path.substring(separatorIdx + 1);
		}
		else
		{
			if (separatorIdx == 0)
			{
				dir = path.substring(0, 1);
				fileName = path.substring(1);
			}
		}

		setOwnerRating(dir, fileName, rating);
	}

	public void setOwnerRating(String path, String fileName, int rating)
	{
		Element metaInfElement = getMetaInfElement(path, fileName);
        
		if (metaInfElement == null)
		{
			metaInfElement = createMetaInfElement(path, fileName);
		}

		Document doc = metaInfElement.getOwnerDocument();

		Element ratingElement = XmlUtil.getChildByTagName(metaInfElement,"rating");

		if (ratingElement == null)
		{
			ratingElement = doc.createElement("rating");

			metaInfElement.appendChild(ratingElement);
		}

		XmlUtil.setChildText(ratingElement, "owner", Integer.toString(rating));
        
		cacheDirty.put(path, new Boolean(true));
	}

	public int getOwnerRating(String path)
	{
		Element metaInfElement = getMetaInfElement(path);

		if (metaInfElement == null)
		{
			return(-1);
		}

		Element ratingElement = XmlUtil.getChildByTagName(metaInfElement, "rating");

		if (ratingElement == null)
		{
			return(-1);
		}
        
	    String ownerRating = XmlUtil.getChildText(ratingElement, "owner");

        if (ownerRating == null)
        {
        	return(-1);
        }
        
		int rating = (-1);

        try
        {
			rating = Integer.parseInt(ownerRating);
        }
        catch (NumberFormatException nfe)
        {
        }

		return(rating);
	}

	public void addVisitorRating(String path, int rating)
	{
		String dir = null;

		String fileName = null;

		int separatorIdx = path.lastIndexOf(File.separatorChar);

		if (separatorIdx > 0)
		{
			dir = path.substring(0, separatorIdx);
			fileName = path.substring(separatorIdx + 1);
		}
		else
		{
			if (separatorIdx == 0)
			{
				dir = path.substring(0, 1);
				fileName = path.substring(1);
			}
		}

		addVisitorRating(dir, fileName, rating);
	}

	public void addVisitorRating(String path, String fileName, int rating)
	{
		Element metaInfElement = getMetaInfElement(path, fileName);
        
		if (metaInfElement == null)
		{
			metaInfElement = createMetaInfElement(path, fileName);
		}

		Document doc = metaInfElement.getOwnerDocument();

		Element ratingElement = XmlUtil.getChildByTagName(metaInfElement,"rating");

		if (ratingElement == null)
		{
			ratingElement = doc.createElement("rating");

			metaInfElement.appendChild(ratingElement);
		}

		Element visitorElement = XmlUtil.getChildByTagName(metaInfElement, "visitor");

		if (visitorElement == null)
		{
			visitorElement = doc.createElement("visitor");

			ratingElement.appendChild(visitorElement);
		}

	    Element voteElement = doc.createElement("vote");

		visitorElement.appendChild(voteElement);

        XmlUtil.setElementText(voteElement, Integer.toString(rating));

		cacheDirty.put(path, new Boolean(true));
	}

	public PictureRating getPictureRating(String path)
	{
		String dir = null;

		String fileName = null;

		int separatorIdx = path.lastIndexOf(File.separatorChar);

		if (separatorIdx > 0)
		{
			dir = path.substring(0, separatorIdx);
			fileName = path.substring(separatorIdx + 1);
		}
		else
		{
			if (separatorIdx == 0)
			{
				dir = path.substring(0, 1);
				fileName = path.substring(1);
			}
		}

		return(getPictureRating(dir, fileName));
	}

	public PictureRating getPictureRating(String path, String fileName)
	{
		Element metaInfElement = getMetaInfElement(path, fileName);
        
		if (metaInfElement == null)
		{
			return null;
		}

		Element ratingElement = XmlUtil.getChildByTagName(metaInfElement,"rating");

		if (ratingElement == null)
		{
			return null;
		}

		PictureRating pictureRating = null;
		
        String ownerRating = XmlUtil.getChildText(ratingElement, "owner");

        if (ownerRating != null)
        {
            try
            {
                int rating = Integer.parseInt(ownerRating);
                pictureRating = new PictureRating();
                pictureRating.setOwnerRating(rating);
            }
            catch (NumberFormatException nfe)
            {
            }
        }
		
		Element visitorElement = XmlUtil.getChildByTagName(metaInfElement, "visitor");

		if (visitorElement == null)
		{
			return(pictureRating);
		}

		NodeList voteList = visitorElement.getElementsByTagName("vote");

		if (voteList == null)
		{
			return(pictureRating);
		}

        int voteSum = 0;
        
        int voteCount = 0;

		int listLength = voteList.getLength();

		for (int i = 0; i < listLength; i++)
		{
			Element voteElement = (Element) voteList.item(i);

            String tmp = XmlUtil.getElementText(voteElement);
            
            int voteVal = (-1);
            
            try
            {
            	voteVal = Integer.parseInt(tmp);
            	
            	voteSum += voteVal;
            	
            	voteCount++;
            }
            catch (NumberFormatException nfe)
            {
            	Logger.getLogger(getClass()).error("invalid vote value: " + tmp);
            }
		}

        if (voteCount > 0)
        {
            if (pictureRating == null) 
            {
                pictureRating = new PictureRating();            
            }
            
            pictureRating.setNumberOfVotes(voteCount);
            
            float averageVote = ((float) voteSum) / ((float) voteCount);
            
            pictureRating.setAverageVisitorRating(Math.round(averageVote));
        }

        return pictureRating;
	}

	public int getVisitorRatingCount(String path)
	{
		Element metaInfElement = getMetaInfElement(path);

		if (metaInfElement == null)
		{
			return(0);
		}

		Element ratingElement = XmlUtil.getChildByTagName(metaInfElement, "rating");

		if (ratingElement == null)
		{
			return(0);
		}
		
		Element visitorElement = XmlUtil.getChildByTagName(metaInfElement, "visitor");

		if (visitorElement == null)
		{
			return(0);
		}

		NodeList voteList = visitorElement.getElementsByTagName("vote");

		if (voteList == null)
		{
			return(0);
		}

		return(voteList.getLength());
	}

	public void addCategory(String absoluteFileName, Category newCategory)
	{
		String path=null;
		String fileName=null;

		int separatorIdx=absoluteFileName.lastIndexOf(File.separator);

		if (separatorIdx<0)
		{
			separatorIdx=absoluteFileName.lastIndexOf('/');
		}

		if (separatorIdx<0)
		{
			addCategory(absoluteFileName,".",newCategory);
			return;
		}

		addCategory(absoluteFileName.substring(0,separatorIdx),
				   absoluteFileName.substring(separatorIdx+1),
				   newCategory);
	}

	public void addCategory(String path, String fileName, Category newCategory)
	{
		Element metaInfElement=getMetaInfElement(path,fileName);
        
		if (metaInfElement==null)
		{
			metaInfElement=createMetaInfElement(path,fileName);
		}

		Document doc = metaInfElement.getOwnerDocument();

		Element catListElement = XmlUtil.getChildByTagName(metaInfElement,"categories");

		if (catListElement == null)
		{
			catListElement = doc.createElement("categories");

			metaInfElement.appendChild(catListElement);
		}

		Element categoryElement = doc.createElement("category");

		catListElement.appendChild(categoryElement);

		XmlUtil.setChildText(categoryElement,"name",newCategory.getName(),false);
        
		cacheDirty.put(path, new Boolean(true));
	}

	public void removeCategoryByName(String absoluteFileName, String categoryName)
	{
		String path=null;
		String fileName=null;

		int separatorIdx = absoluteFileName.lastIndexOf(File.separator);

		if (separatorIdx < 0)
		{
			separatorIdx = absoluteFileName.lastIndexOf('/');
		}

		if (separatorIdx < 0)
		{
			removeCategoryByName(absoluteFileName, ".", categoryName);
			return;
		}

		removeCategoryByName(absoluteFileName.substring(0,separatorIdx),
			   	       absoluteFileName.substring(separatorIdx+1),
				       categoryName);
	}
	
	public boolean removeCategoryByName(String path, String fileName, String categoryName)
	{
		Element metaInfElement = getMetaInfElement(path, fileName);
        
		if (metaInfElement == null)
		{
			return(false);
		}
    
		Element catListElement = XmlUtil.getChildByTagName(metaInfElement,"categories");

		if (catListElement == null)
		{
			return(false);
		}

		NodeList categoryList = catListElement.getElementsByTagName("category");

		if (categoryList == null)
		{
			return(false);
		}

		int listLength = categoryList.getLength();

		if (listLength == 0)
		{
			return(false);
		}

        Element categoryToRemove = null;

		for (int i=0; (categoryToRemove == null) && (i < listLength); i++)
		{
			Element categoryElement = (Element) categoryList.item(i);
			
			String catName = XmlUtil.getChildText(categoryElement, "name");
			
			if ((catName != null) && catName.equals(categoryName))
			{
				categoryToRemove = categoryElement;
			}
		}

        if (categoryToRemove != null)
        {
			catListElement.removeChild(categoryToRemove);

			cacheDirty.put(path, new Boolean(true));
			
			return(true);
        }
        
        return(false);
	}

	public void removeCategories(String absoluteFileName)
	{
		String path=null;
		String fileName=null;

		int separatorIdx=absoluteFileName.lastIndexOf(File.separator);

		if (separatorIdx<0)
		{
			separatorIdx=absoluteFileName.lastIndexOf('/');
		}

		if (separatorIdx<0)
		{
			removeCategories(absoluteFileName,".");
			return;
		}

		removeCategories(absoluteFileName.substring(0,separatorIdx),
					     absoluteFileName.substring(separatorIdx+1));
	}

	public void removeCategories(String path,String fileName)
	{
		Element metaInfElement = getMetaInfElement(path,fileName);
        
		if (metaInfElement==null)
		{
			return;
		}
    
		Element catListElement = XmlUtil.getChildByTagName(metaInfElement,"categories");

		if (catListElement==null)
		{
			return;
		}

		metaInfElement.removeChild(catListElement);

		cacheDirty.put(path, new Boolean(true));
	}

	public boolean isCategoryAssigned(String absoluteFileName, Category category)
	{
		String path=null;
		String fileName=null;

		int separatorIdx = absoluteFileName.lastIndexOf(File.separator);

		if (separatorIdx<0)
		{
			separatorIdx=absoluteFileName.lastIndexOf('/');
		}

		if (separatorIdx<0)
		{
			return(isCategoryAssigned(absoluteFileName, ".", category));
		}

		return(isCategoryAssigned(absoluteFileName.substring(0,separatorIdx),
								  absoluteFileName.substring(separatorIdx+1),
								  category));
	}

	public boolean isCategoryAssigned(String path, String fileName, Category category)
	{
		Element metaInfElement = getMetaInfElement(path,fileName);

		if (metaInfElement == null)
		{
			return(false);
		}

		Element catListElement = XmlUtil.getChildByTagName(metaInfElement,"categories");

		if (catListElement == null)
		{
			return(false);
		}
        
		NodeList categoryList = catListElement.getElementsByTagName("category");

		if (categoryList == null)
		{
			return(false);
		}

		int listLength = categoryList.getLength();

		if (listLength == 0)
		{
			return(false);
		}

		Vector listOfCategories = new Vector();

		for (int i=0; i<listLength; i++)
		{
			Element categoryElement = (Element) categoryList.item(i);

			String catName = XmlUtil.getChildText(categoryElement, "name");

            if ((catName != null) && catName.equals(category.getName()))
            {
            	return(true);
            }
		}

		return(false);
	}

	public Vector getListOfCategories(String absoluteFileName)
	{
		String path=null;
		String fileName=null;

		int separatorIdx=absoluteFileName.lastIndexOf(File.separator);

		if (separatorIdx<0)
		{
			separatorIdx=absoluteFileName.lastIndexOf('/');
		}

		if (separatorIdx<0)
		{
			return(getListOfCategories(absoluteFileName,"."));
		}

		return(getListOfCategories(absoluteFileName.substring(0,separatorIdx),
								   absoluteFileName.substring(separatorIdx+1)));
	}

	public Vector getListOfCategories(String path, String fileName)
	{
		Element metaInfElement = getMetaInfElement(path,fileName);

		if (metaInfElement==null)
		{
			return(null);
		}

		Element catListElement = XmlUtil.getChildByTagName(metaInfElement,"categories");

		if (catListElement==null)
		{
			return(null);
		}
        
		NodeList categoryList = catListElement.getElementsByTagName("category");

		if (categoryList == null)
		{
			return(null);
		}

		int listLength = categoryList.getLength();

		if (listLength == 0)
		{
			return(null);
		}

		Vector listOfCategories = new Vector();

		for (int i=0; i<listLength; i++)
		{
			Element categoryElement = (Element) categoryList.item(i);

            String catName = XmlUtil.getChildText(categoryElement, "name");

            Category category = new Category();
            
            category.setName(catName);
            
			listOfCategories.add(category);
		}

		return(listOfCategories);
	}

    public void setGeoTag(String absoluteFileName, GeoTag geoTag)
    {
        int separatorIdx = absoluteFileName.lastIndexOf(File.separator);

        if (separatorIdx < 0)
        {
            separatorIdx = absoluteFileName.lastIndexOf('/');
        }

        if (separatorIdx < 0)
        {
            setGeoTag(absoluteFileName, ".", geoTag);
            return;
        }

        setGeoTag(absoluteFileName.substring(0,separatorIdx),
                  absoluteFileName.substring(separatorIdx+1),
                  geoTag);
    }

    public void setGeoTag(String path, String fileName, GeoTag newGeoTag)
    {
        Element metaInfElement = getMetaInfElement(path, fileName);
        
        if (metaInfElement == null)
        {
            metaInfElement = createMetaInfElement(path,fileName);
        }

        Document doc = metaInfElement.getOwnerDocument();

        Element geoTagElement = XmlUtil.getChildByTagName(metaInfElement, "geoTag");

        if (geoTagElement == null)
        {
            geoTagElement = doc.createElement("geoTag");

            metaInfElement.appendChild(geoTagElement);
        }

        XmlUtil.setChildText(geoTagElement, "latitude", Float.toString(newGeoTag.getLatitude()), false);
        XmlUtil.setChildText(geoTagElement, "longitude", Float.toString(newGeoTag.getLongitude()), false);
        XmlUtil.setChildText(geoTagElement, "zoomFactor", Integer.toString(newGeoTag.getZoomFactor()),false);
        
        if (newGeoTag.getInfoText() != null)
        {
            XmlUtil.setChildText(geoTagElement, "infoText", newGeoTag.getInfoText(), true);
        }
        
        cacheDirty.put(path, new Boolean(true));
    }

    public void removeGeoTag(String absoluteFileName)
    {
        int separatorIdx = absoluteFileName.lastIndexOf(File.separator);

        if (separatorIdx < 0)
        {
            separatorIdx = absoluteFileName.lastIndexOf('/');
        }

        if (separatorIdx < 0)
        {
            removeGeoTag(absoluteFileName, ".");
            return;
        }

        removeGeoTag(absoluteFileName.substring(0,separatorIdx),
                     absoluteFileName.substring(separatorIdx+1));
    }

    public void removeGeoTag(String path, String fileName)
    {
        Element metaInfElement = getMetaInfElement(path,fileName);
        
        if (metaInfElement == null)
        {
            return;
        }
    
        Element geoTagElement = XmlUtil.getChildByTagName(metaInfElement, "geoTag");

        if (geoTagElement == null)
        {
            return;
        }

        metaInfElement.removeChild(geoTagElement);

        cacheDirty.put(path, new Boolean(true));
    }
	
    public GeoTag getGeoTag(String absoluteFileName)
    {
        int separatorIdx = absoluteFileName.lastIndexOf(File.separator);

        if (separatorIdx < 0)
        {
            separatorIdx = absoluteFileName.lastIndexOf('/');
        }

        if (separatorIdx < 0)
        {
            return(getGeoTag(absoluteFileName, "."));
        }

        return(getGeoTag(absoluteFileName.substring(0,separatorIdx),
                         absoluteFileName.substring(separatorIdx+1)));
    }

    public GeoTag getGeoTag(String path, String fileName)
    {
        Element metaInfElement = getMetaInfElement(path, fileName);
        
        if (metaInfElement == null)
        {
            return(null);
        }
    
        Element geoTagElement = XmlUtil.getChildByTagName(metaInfElement, "geoTag");

        if (geoTagElement == null)
        {
            return(null);
        }

    	String latitude = XmlUtil.getChildText(geoTagElement, "latitude");        
        String longitude = XmlUtil.getChildText(geoTagElement, "longitude");        
        String zoomFactor = XmlUtil.getChildText(geoTagElement, "zoomFactor");        
        
        GeoTag geoTag = new GeoTag(latitude, longitude, zoomFactor);

        String infoText = XmlUtil.getChildText(geoTagElement, "infoText");
        
        if (infoText != null)
        {
        	geoTag.setInfotext(infoText);
        }
        
        return(geoTag);
    }
	
	public void createLink(String path, FileLink newLink)
	throws FileNotFoundException
	{
		createLink(path, newLink, false);
	}
	
	public void createLink(String path, FileLink newLink, boolean suppressReverseLink)
	throws FileNotFoundException
	{
		File destFile = new File(newLink.getDestPath());
		
		if ((!destFile.exists()) || (!destFile.isFile()) || (!destFile.canRead()))
		{
			throw new FileNotFoundException("MetaInfManager.createLink: file not found: " + destFile);
		}
		
		Element metaInfElement=getMetaInfElement(path,".");
        
		if (metaInfElement==null)
		{
			metaInfElement=createMetaInfElement(path,".");
		}

		Document doc=metaInfElement.getOwnerDocument();

		Element linkListElement = XmlUtil.getChildByTagName(metaInfElement,"links");

		if (linkListElement==null)
		{
			linkListElement=doc.createElement("links");

			metaInfElement.appendChild(linkListElement);
		}

		Element linkElement=doc.createElement("link");

		linkListElement.appendChild(linkElement);

		XmlUtil.setChildText(linkElement,"name",newLink.getName(),false);
		XmlUtil.setChildText(linkElement,"destPath",newLink.getDestPath(),false);
		XmlUtil.setChildText(linkElement,"creationTime",Long.toString(newLink.getCreationTime()),false);
		XmlUtil.setChildText(linkElement,"creator",newLink.getCreator(),false);
        
		if (!suppressReverseLink) 
		{
			if (WebFileSys.getInstance().isReverseFileLinkingEnabled())
			{
		        createReverseLinkRef(path, newLink);        
			}
		}
		
		cacheDirty.put(path,new Boolean(true));
	}

	/**
	 * Stores the backward reference from the linked file to the linking file.
	 * @param sourcePath the path of the linking file
	 * @param newLink the link info
	 */
	private void createReverseLinkRef(String sourcePath, FileLink newLink)
	{
        String linkingFilePath = null;
        
        if (sourcePath.endsWith(File.separator)) 
        {
            linkingFilePath = sourcePath + newLink.getName();
        }
        else
        {
            linkingFilePath = sourcePath + File.separator + newLink.getName();
        }
	    
        Element metaInfElement = getMetaInfElement(newLink.getDestPath());
        
        if (metaInfElement == null)
        {
            metaInfElement = createMetaInfElement(newLink.getDestPath());
        }

        Element linkedByElement = null;

        Element linkedByListElement = XmlUtil.getChildByTagName(metaInfElement, "linkedBy");

        if (linkedByListElement == null)
        {
            linkedByListElement = metaInfElement.getOwnerDocument().createElement("linkedBy");
            metaInfElement.appendChild(linkedByListElement);
        } 

        NodeList linkingFileList = linkedByListElement.getElementsByTagName("linkingFile");

        if (linkingFileList != null)
        {
            int listLength = linkingFileList.getLength();

            if (listLength > 0)
            {
                boolean existingFound = false;
                
                for (int i = 0; (!existingFound) && (i < listLength); i++) 
                {
                    Element existingLinkedByElement = (Element) linkingFileList.item(i);
                    
                    String existingLinkingFile = XmlUtil.getElementText(existingLinkedByElement);

                    if (existingLinkingFile.equals(linkingFilePath))
                    {
                        linkedByElement = existingLinkedByElement;
                        existingFound = true;
                    }
                }
            }
        }

        if (linkedByElement == null) 
        {
            Document doc = metaInfElement.getOwnerDocument();

            linkedByElement = doc.createElement("linkingFile");
            
            XmlUtil.setElementText(linkedByElement, linkingFilePath);

            linkedByListElement.appendChild(linkedByElement);
            
            cacheDirty.put(CommonUtils.getParentDir(newLink.getDestPath()), new Boolean(true));
        }
	}
	
	/**
	 * Removes a backward link from the link target file to the linking file.
	 * 
	 * @param sourcePath path of the directory containing the link
	 * @param linkName name of the link
	 * @param linkTargetPath path of the link target file
	 */
	public void removeReverseLink(String sourcePath, String linkName, String linkTargetPath) 
	{
	    String linkPath = null;
	    if (sourcePath.endsWith(File.separator))
	    {
	        linkPath = sourcePath + linkName;
	    }
	    else
	    {
            linkPath = sourcePath + File.separator + linkName;
	    }
	    
        Element metaInfElement = getMetaInfElement(linkTargetPath);
        
        if (metaInfElement == null)
        {
            return;
        }
        
        Element linkedByElement = XmlUtil.getChildByTagName(metaInfElement, "linkedBy");

        if (linkedByElement == null)
        {
            return;
        }

        NodeList linkingFileList = linkedByElement.getElementsByTagName("linkingFile");

        if (linkingFileList == null)
        {
            return;
        }
        
        int listLength = linkingFileList.getLength();

        for (int i = 0; i < listLength; i++) 
        {
             Element linkingFileElement = (Element) linkingFileList.item(i);
             
             String linkingFilePath = XmlUtil.getElementText(linkingFileElement);
             
             if (linkPath.equals(linkingFilePath)) 
             {
                 linkedByElement.removeChild(linkingFileElement);
                 
                 if (listLength == 1) 
                 {
                     metaInfElement.removeChild(linkedByElement);

                     cacheDirty.put(CommonUtils.getParentDir(linkTargetPath), new Boolean(true));
                 }
                 return;
             }
        }   
	}
	
	/**
	 * Updates all links pointing to a moved file with the new path of the file.
	 * This makes sense only if backward linking is enabled.
	 * If newPath is null, the file is to be deleted and the links pointing to the
	 * file have to be removed only.
	 * 
	 * @param oldPath old path of the (possibly) linked file
	 * @param newPath new path of the (possibly) linked file or null if the file is to be deleted
	 * @param userid the uid of the current user
	 */
	public void updateLinksAfterMove(String oldPath, String newPath, String userid)
	{
	    if (!WebFileSys.getInstance().isReverseFileLinkingEnabled())
	    {
	        return;
	    }
	    
	    ArrayList linkingFiles = getLinkingFiles(oldPath);
	    
	    if (linkingFiles == null)
	    {
	        // file is not linked - nothing to do
	        return;
	    }
	    
	    Iterator iter = linkingFiles.iterator();
	    while (iter.hasNext()) 
	    {
	        String linkingFilePath = (String) iter.next();
	        
	        String linkName = CommonUtils.extractFileName(linkingFilePath);
	        
	        String linkPath = CommonUtils.getParentDir(linkingFilePath);
	        
	        removeLink(linkPath, linkName);
	        
	        if (newPath != null)
	        {
	            try
	            {
	                createLink(linkPath, new FileLink(linkName, newPath, userid));
	            }
	            catch (IOException ex)
	            {
	                Logger.getLogger(getClass()).error("cannot update link " + linkName + " after file move: " + oldPath + " - " + newPath);
	            }
	        }
	    }
	}
	
	/**
	 * Updates the target path of a link.
	 * @param linkingFilePath path of the link
	 * @param newLinkTargetPath new link target path
	 * @param uid userid
	 */
	public void updateLinkTarget(String linkingFilePath, String newLinkTargetPath, String uid)
	{
        String linkName = CommonUtils.extractFileName(linkingFilePath);
        
        String linkPath = CommonUtils.getParentDir(linkingFilePath);
        
        removeLink(linkPath, linkName);
        
        try
        {
            createLink(linkPath, new FileLink(linkName, newLinkTargetPath, uid));
        }
        catch (IOException ex)
        {
            Logger.getLogger(getClass()).error("cannot update link " + linkName + " newPath=" + newLinkTargetPath);
        }
	}
	
	/**
	 * TODO: never tested because not used up to now
	 * @param path path of the link target file
	 * @param newLinkingFiles list of Strings (path of linking files)
	 */
	public void addLinkingFiles(String path, ArrayList newLinkingFiles)
	{
	    if ((newLinkingFiles == null) || (newLinkingFiles.size() == 0)) 
	    {
	        return;
	    }
	    
        Element metaInfElement = getMetaInfElement(path);
        
        if (metaInfElement == null)
        {
            metaInfElement = createMetaInfElement(path);
        }

        Document doc = metaInfElement.getOwnerDocument();

        Element linkedByListElement = XmlUtil.getChildByTagName(metaInfElement, "linkedBy");

        if (linkedByListElement == null)
        {
            linkedByListElement = doc.createElement("linkedBy");
            metaInfElement.appendChild(linkedByListElement);
        } 
	    
	    Iterator iter = newLinkingFiles.iterator();
	    
	    while (iter.hasNext()) 
	    {
	        String linkingFilePath = (String) iter.next();
	        
	        Element linkedByElement = doc.createElement("linkingFile");
            
            XmlUtil.setElementText(linkedByElement, linkingFilePath);

            linkedByListElement.appendChild(linkedByElement);
	    }
	    
        cacheDirty.put(CommonUtils.getParentDir(path), new Boolean(true));
	}
	
	public ArrayList getLinkingFiles(String path)
	{
	    Element metaInfElement = getMetaInfElement(path);
        
        if (metaInfElement == null)
        {
            return null;
        }

        Element linkedByListElement = XmlUtil.getChildByTagName(metaInfElement, "linkedBy");

        if (linkedByListElement == null)
        {
            // no links pointing to this file
            return null;
        }

        NodeList linkingFileList = linkedByListElement.getElementsByTagName("linkingFile");

        if (linkingFileList == null)
        {
            return null;
        }
        
        int listLength = linkingFileList.getLength();

        if (listLength == 0)
        {
            return null;
        }

        ArrayList linkingFiles = new ArrayList();
            
        for (int i = 0; i < listLength; i++) 
        {
             Element linkingFileElement = (Element) linkingFileList.item(i);
             
             linkingFiles.add(XmlUtil.getElementText(linkingFileElement));
        }   
        
        return linkingFiles;
	}
	
	public Vector getListOfLinks(String path)
	{
		Element metaInfElement=getMetaInfElement(path,".");

		if (metaInfElement==null)
		{
			return(null);
		}

		Element linkListElement=XmlUtil.getChildByTagName(metaInfElement,"links");

		if (linkListElement==null)
		{
			return(null);
		}
        
		NodeList linkList = linkListElement.getElementsByTagName("link");

		if (linkList==null)
		{
			return(null);
		}

		int listLength = linkList.getLength();

		if (listLength==0)
		{
			return(null);
		}

		Vector listOfLinks = new Vector();

		for (int i=0;i<listLength;i++)
		{
			Element linkElement=(Element) linkList.item(i);

			String name = XmlUtil.getChildText(linkElement,"name");
			String creator = XmlUtil.getChildText(linkElement,"creator");
			String destPath = XmlUtil.getChildText(linkElement,"destPath");

			String tmp = XmlUtil.getChildText(linkElement,"creationTime");

			long creationTime=0L;

			try
			{
				creationTime=Long.parseLong(tmp);
			}
			catch (NumberFormatException nfex)
			{
				Logger.getLogger(getClass()).warn("invalid creation time: " + tmp);
			}

            FileLink link = new FileLink(name, destPath, creator, new Date(creationTime));

			listOfLinks.add(link);
		}

        // Collections.sort(listOfLinks, new FileLinkComparator(FileLinkComparator.SORT_BY_NAME));

		return(listOfLinks);
	}

	public boolean removeLink(String path, String linkToRemove)
	{
		Element metaInfElement=getMetaInfElement(path,".");

		if (metaInfElement==null)
		{
			return(false);
		}

		Element linkListElement=XmlUtil.getChildByTagName(metaInfElement,"links");

		if (linkListElement==null)
		{
			return(false);
		}
        
		NodeList linkList = linkListElement.getElementsByTagName("link");

		if (linkList==null)
		{
			return(false);
		}

		int listLength = linkList.getLength();

		if (listLength == 0)
		{
			return(false);
		}

		for (int i=0;i<listLength;i++)
		{
			Element linkElement=(Element) linkList.item(i);

			String linkName = XmlUtil.getChildText(linkElement,"name");

            if (linkName.equals(linkToRemove))
            {
                if (WebFileSys.getInstance().isReverseFileLinkingEnabled())
                {
                    String linkTargetPath = XmlUtil.getChildText(linkElement, "destPath");
                    
                    removeReverseLink(path, linkToRemove, linkTargetPath);
                }

                linkListElement.removeChild(linkElement);
            	
				cacheDirty.put(path,new Boolean(true));

            	return(true);
            }
		}

		return(false);
	}

	public boolean renameLink(String path, String oldLinkName, String newLinkName)
	{
		Element metaInfElement=getMetaInfElement(path,".");

		if (metaInfElement==null)
		{
			return(false);
		}

		Element linkListElement=XmlUtil.getChildByTagName(metaInfElement,"links");

		if (linkListElement==null)
		{
			return(false);
		}
        
		NodeList linkList = linkListElement.getElementsByTagName("link");

		if (linkList==null)
		{
			return(false);
		}

		int listLength = linkList.getLength();

		if (listLength == 0)
		{
			return(false);
		}

		for (int i=0;i<listLength;i++)
		{
			Element linkElement=(Element) linkList.item(i);

			String linkName = XmlUtil.getChildText(linkElement,"name");

			if (linkName.equals(oldLinkName))
			{
				XmlUtil.setChildText(linkElement, "name", newLinkName);
            	
				cacheDirty.put(path,new Boolean(true));

				return(true);
			}
		}

		return(false);
	}

    public void incrementDownloads(String path)
    {
        String dir=null;

        String fileName=null;

        int separatorIdx=path.lastIndexOf('/');

        if (separatorIdx < 0)
        {
            separatorIdx=path.lastIndexOf(File.separatorChar);
        }

        if (separatorIdx < 0)
        {
            return;
        }

        if (separatorIdx>0)
        {
            dir=path.substring(0,separatorIdx);
            fileName=path.substring(separatorIdx+1);
        }
        else
        {
            if (separatorIdx==0)
            {
                dir=path.substring(0,1);
                fileName=path.substring(1);
            }
        }

        if (File.separatorChar!='/')
        {
            dir=dir.replace('/',File.separatorChar);
        }

        incrementDownloads(dir,fileName);
    }

    public synchronized void incrementDownloads(String path,String fileName)
    {
        Element metaInfElement=getMetaInfElement(path,fileName);
        
        if (metaInfElement==null)
        {
            metaInfElement=createMetaInfElement(path,fileName);
        }

        String oldValue=XmlUtil.getChildText(metaInfElement,"downloads");

        int downloadNum=0;

        if (oldValue!=null)
        {
            try
            {
                downloadNum=Integer.parseInt(oldValue);
            }
            catch (NumberFormatException nfex)
            {
            }
        }

        downloadNum++;
        
        XmlUtil.setChildText(metaInfElement,"downloads",Integer.toString(downloadNum),false);
        
        cacheDirty.put(path,new Boolean(true));
        // saveMetaInfFile(path);
    }

	public synchronized int getNumberOfDownloads(String absoluteFileName)
	{
		Element metaInfElement = getMetaInfElement(absoluteFileName);

		if (metaInfElement == null)
		{
			return(0);
		}

        int downLoadNum = 0;
        
		String temp = XmlUtil.getChildText(metaInfElement,"downloads");
		
		if (temp != null)
		{
			try
			{
				downLoadNum = Integer.parseInt(temp);
			}
			catch (NumberFormatException nfex)
			{
			}
		}
		
		return(downLoadNum);
	}

    public Vector getTopDownloadList(String path)
    {
        Element metaInfRoot=(Element) dirList.get(path);

        if (metaInfRoot==null)
        {
            metaInfRoot=loadMetaInfFile(path);
            
            if (metaInfRoot==null)
            {
                return(null);
            }
            else
            {
                dirList.put(path,metaInfRoot);
            }
        }

        Vector sortedTopList=new Vector();

        NodeList metaInfList=metaInfRoot.getElementsByTagName("metainf");

        if (metaInfList==null)
        {
            return(null);
        }

        int listLength=metaInfList.getLength();

        for (int i=0;i<listLength;i++)
        {
            Element metaInfElement=(Element) metaInfList.item(i);

            String filename=metaInfElement.getAttribute("filename");

            if (filename!=null)
            {
                String temp=XmlUtil.getChildText(metaInfElement,"downloads");

                if (temp!=null)
                {
                    try
                    {
                        int downloadNumber=Integer.parseInt(temp);

                        if (downloadNumber > 0)
                        {
                            sortedTopList.add(metaInfElement);
                        }
                    }
                    catch (NumberFormatException nfex)
                    {
                    }
                }
            }
        }

        if (sortedTopList.size() > 1)
        {
            Collections.sort(sortedTopList,new MetaInfComparator(MetaInfComparator.SORT_BY_DOWNLOADS));
        }

        return(sortedTopList);
    }

	public Date getStatisticsResetDate(String path)
	{
		Element metaInfRoot=(Element) dirList.get(path);

		if (metaInfRoot==null)
		{
			metaInfRoot=loadMetaInfFile(path);
            
			if (metaInfRoot==null)
			{
				return(null);
			}
			else
			{
				dirList.put(path,metaInfRoot);
			}
		}

        String resetDateString = XmlUtil.getChildText(metaInfRoot,"statsResetDate");
        
        if (resetDateString == null)
        {
        	return(null);
        }
        
        try
        {
        	return(new Date(Long.parseLong(resetDateString)));
        }
        catch (NumberFormatException nfex)
        {
        }
        
        return(null);
	}

    public void resetStatistics(String path)
    {
		Element metaInfRoot=(Element) dirList.get(path);

		if (metaInfRoot==null)
		{
			metaInfRoot=loadMetaInfFile(path);
            
			if (metaInfRoot==null)
			{
				return;
			}
			else
			{
				dirList.put(path,metaInfRoot);
			}
		}
		
	    XmlUtil.setChildText(metaInfRoot,"statsResetDate",Long.toString(System.currentTimeMillis()));

		NodeList metaInfList=metaInfRoot.getElementsByTagName("metainf");

		if (metaInfList!=null)
		{
			int listLength=metaInfList.getLength();

			for (int i=0;i<listLength;i++)
			{
				Element metaInfElement=(Element) metaInfList.item(i);

				Element downloadElement = XmlUtil.getChildByTagName(metaInfElement,"downloads");
		
				if (downloadElement != null)
				{
					metaInfElement.removeChild(downloadElement);
				}
			}
		}

		cacheDirty.put(path,new Boolean(true));
    }

    /**
     * Explicitly remove the meta information of a directory from the cache.
     * Used in the search function which can cause hundreds of meta info files to get loaded.
     * 
     * @param dirPath the absolute path of the directory
     */
    public void releaseMetaInf(String dirPath)
    {
    	if (dirList.get(dirPath) != null)
    	{
    		dirList.remove(dirPath);
        }
    	
    	if (cacheDirty.containsKey(dirPath)) {
    	    cacheDirty.remove(dirPath);
    	}
    }
    
    public synchronized void run()
    {
        int counter = 0;

        boolean stop = false;
        
        while (!stop)
        {
            try
            {
                this.wait(60000);

                Enumeration cacheDirtyList=cacheDirty.keys();

                while (cacheDirtyList.hasMoreElements())
                {
                    String path=(String) cacheDirtyList.nextElement();

                    saveMetaInfFile(path);

                    cacheDirty.remove(path);
                }

                if (counter == 15)
                {
                    counter=0;

                    if (dirList.size()>0)
                    {
                        synchronized (dirList)
                        {
                            Logger.getLogger(getClass()).debug("removing " + dirList.size() + " elements from metainf cache");

                            dirList.clear();
                        }
                    }
                }

                counter++;
            }
            catch (InterruptedException e)
            {
				Enumeration cacheDirtyList=cacheDirty.keys();

				while (cacheDirtyList.hasMoreElements())
				{
					String path=(String) cacheDirtyList.nextElement();

					saveMetaInfFile(path);
				}
				
				Logger.getLogger(getClass()).debug("MetaInfmanager ready for shutdown");

				stop = true;
            }
        }
    }

    public boolean isMetaInfFile(String path)
    {
        return(path.endsWith(METAINF_FILE));
    }

    static public void main(String args[])
    {
        MetaInfManager mgr = MetaInfManager.getInstance();
        
		/*
		mgr.removeCategories("c:\\temp", "test.html");

        Category testCat = new Category();
        
        testCat.setName("TestCategory");
        
        mgr.addCategory("c:\\temp", "test.html", testCat);
        
        Vector catList = mgr.getListOfCategories("c:\\temp\\test.html");
        
        if ((catList != null) && (catList.size() > 0))
        {
        	System.out.println("categories of file c:\\temp\\test.html:");
        	
        	for (int i=0; i < catList.size(); i++)
        	{
        		Category cat = (Category) catList.elementAt(i);
        		
        		System.out.println("  " + cat.getName());
        	}
        }
        
        if (!mgr.removeCategory("c:\\temp", "test.html", "TestCategory"))
        {
        	System.out.println("category could not be removed");
        }
        
		catList = mgr.getListOfCategories("c:\\temp\\test.html");
        
		if ((catList != null) && (catList.size() > 0))
		{
			System.out.println("file c:\\temp\\test.html has still " + catList.size() + " categories assigned");
		}
		else
		{
			System.out.println("file c:\\temp\\test.html has no categories assigned");
		}

        System.out.println("===================== XML data for test.html in d:\\tmp:");

        Element metaInfElement=mgr.getMetaInfElement("d:\\tmp","test.html");

        XmlUtil.writeToStream(metaInfElement,new PrintWriter(System.out));
        
        System.out.println("=====================Description for test.html in d:\\tmp:");
        System.out.println(mgr.getDescription("d:\\tmp","test.html"));

        System.out.println("===================== XML data for d:\\tmp\\test.html:");
        
        metaInfElement=mgr.getMetaInfElement("d:\\tmp\\test.html");

        XmlUtil.writeToStream(metaInfElement,new PrintWriter(System.out));
        
        System.out.println("=====================Description for d:\\tmp\\test.html:");
        System.out.println(mgr.getDescription("d:\\tmp\\test.html"));

        mgr.setDescription("d:\\tmp","rrr.rrr","Das ist die Datei rrr.rrr");
        mgr.setDescription("d:\\tmp\\muell","uuu.uuu","Das ist die Datei uuu.uuu");

        mgr.addComment("d:\\tmp","test.html",new Comment("nobody",new Date(),"Das ist mein Kommentar."));

        try
        {
			mgr.createLink("c:\\temp", new FileLink("link2","C:/84.Grundschule/album.html","user1"));
			mgr.createLink("c:\\temp", new FileLink("link1","c:/flowers/alcea1.jpg","admin"));
			mgr.createLink("c:\\temp", new FileLink("link3","C:/84.Grundschule/kontakt.htm","user1"));
        }
        catch (FileNotFoundException fnfex)
        {
        	System.out.println(fnfex);
        }
        
		System.out.println("sorted list of links:");

        Vector linkList = mgr.getSortedListOfLinks("c:\\temp");
        
        if (linkList != null)
        {
        	for (int i=0;i<linkList.size();i++)
        	{
        		FileLink link = (FileLink) linkList.elementAt(i);
				System.out.println("  name=" + link.getName() + " destPath=" + link.getDestPath() + " creator=" + link.getCreator() + " creation time=" + link.getCreationDate());
        	}
        }
        */
    }

}
