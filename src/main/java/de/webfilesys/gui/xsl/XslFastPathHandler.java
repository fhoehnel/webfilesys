package de.webfilesys.gui.xsl;

import java.io.File;
import java.io.PrintWriter;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.FastPathManager;
import de.webfilesys.FileSysBookmark;
import de.webfilesys.FileSysBookmarkManager;
import de.webfilesys.decoration.Decoration;
import de.webfilesys.decoration.DecorationManager;
import de.webfilesys.util.UTF8URLEncoder;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslFastPathHandler extends XslRequestHandlerBase
{
    private static final int PATH_TYPE_VISITED = 1;
    
    private static final int PATH_TYPE_BOOKMARK = 2;
    
	public XslFastPathHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid)
	{
        super(req, resp, session, output, uid);
	}
	
	protected void process()
	{
		Element fastPathElement = doc.createElement("folderTree");
			
		doc.appendChild(fastPathElement);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/fastPath.xsl\"");

		doc.insertBefore(xslRef, fastPathElement);

		XmlUtil.setChildText(fastPathElement, "css", userMgr.getCSS(uid), false);

		addMsgResource("label.fastpath", getResource("label.fastpath", "Fast Path (last visited directories)"));
		
		int docRootTokenCount = getDocRootTokenCount();
		
        Vector bookmarkList = FileSysBookmarkManager.getInstance().getListOfBookmarks(uid);
        
        if (bookmarkList != null) 
        {
            for (int i = 0; i < bookmarkList.size(); i++) 
            {
                FileSysBookmark bookmark = (FileSysBookmark) bookmarkList.elementAt(i);
                
                addFastPath(fastPathElement, bookmark.getPath(), docRootTokenCount, PATH_TYPE_BOOKMARK);
            }
        }

        Vector fastPathList = FastPathManager.getInstance().getPathList(uid);
        
        for (int i = 0; i < fastPathList.size(); i++)
        {
        	addFastPath(fastPathElement, (String) fastPathList.elementAt(i), docRootTokenCount, PATH_TYPE_VISITED);
        }
		
		this.processResponse("fastPath.xsl", false);
    }
	
	private void addFastPath(Element fastPathElement, String fastPath, int docRootTokenCount, int pathType)
	{
    	File testDir = new File(fastPath);
    	
    	if (!testDir.exists())
    	{
    		return;
    	}
		
        StringTokenizer pathParser = new StringTokenizer(fastPath, File.separator + "/");		
        
        String path = "";
        
        int tokenCounter = 0;
        
        Element folderElem = fastPathElement;
        
        while (pathParser.hasMoreTokens())
        {
        	String partOfPath = null;
        	
        	if ((File.separatorChar == '/') && (path.length() == 0))
        	{
        		partOfPath = "/";
        	}
        	else
        	{
            	partOfPath = pathParser.nextToken();
        	}
        	
        	if ((File.separatorChar == '\\') && partOfPath.endsWith(":"))
        	{
        		partOfPath = partOfPath + "\\";
        	}
        	
        	if (path.length() == 0)
        	{
        		path = partOfPath;
        	}
        	else
        	{
        		if (path.endsWith(File.separator))
        		{
                	path = path + partOfPath;
        		}
        		else
        		{
                	path = path + File.separator + partOfPath;
        		}
        	}
        	
        	tokenCounter++;
        	
        	if (tokenCounter >= docRootTokenCount)
        	{
            	NodeList children = folderElem.getChildNodes();
            	
            	boolean nodeFound = false;
            	
            	Element subFolderElem = null;
            	
                int listLength = children.getLength();

                for (int i = 0; (!nodeFound) && (i < listLength); i++)
                {
                    Node node = children.item(i);

                    int nodeType = node.getNodeType();

                    if (nodeType == Node.ELEMENT_NODE)
                    {
                    	subFolderElem = (Element) node;

                    	if (subFolderElem.getTagName().equals("folder"))
                    	{
                    		String subFolderName = subFolderElem.getAttribute("name");
                    		
                    		if (subFolderName.equals(partOfPath))
                    		{
                    			nodeFound = true;
                    		}
                    	}
                    }
                }
                
                if (!nodeFound)
                {
                	String encodedPath = UTF8URLEncoder.encode(path);
                	
                	subFolderElem = doc.createElement("folder");
                	
                	subFolderElem.setAttribute("name", partOfPath);
                	
    				subFolderElem.setAttribute("path", encodedPath);  
    				
                    String lowerCasePartOfPath = partOfPath.toLowerCase();
                	
                	boolean stop = false;
                	
                    for (int i = 0; (!stop) && (i < listLength); i++)
                    {
                        Node node = children.item(i);
                        
                        int nodeType = node.getNodeType();

                        if (nodeType == Node.ELEMENT_NODE)
                        {
                        	Element existingElem = (Element) node;

                        	if (existingElem.getTagName().equals("folder"))
                        	{
                        		String subFolderName = existingElem.getAttribute("name");
                        		
                        		if (subFolderName.toLowerCase().compareTo(lowerCasePartOfPath) > 0)
                        		{
                        			folderElem.insertBefore(subFolderElem, existingElem);
                        			
                        			stop = true;
                        		}
                        	}
                        }
                    }
                        
                    if (!stop)
                    {
                    	folderElem.appendChild(subFolderElem);
                    }
                    
                }
            	
                if (!pathParser.hasMoreTokens())
                {
                	subFolderElem.setAttribute("visited", "true");
                	
                    if (pathType == PATH_TYPE_BOOKMARK) 
                    {
                        subFolderElem.setAttribute("bookmark", "true");
                    }
                    
    				Decoration deco = DecorationManager.getInstance().getDecoration(path);
    				
    				if (deco != null) 
    				{
    					if (deco.getIcon() != null) 
    					{
    						subFolderElem.setAttribute("icon", deco.getIcon());
    					}
    					if (deco.getTextColor() != null) 
    					{
    						subFolderElem.setAttribute("textColor", deco.getTextColor());
    					}
    				}
                }

                folderElem = subFolderElem;
        	}
        }
	}
	
	private int getDocRootTokenCount()
	{
		if (isAdminUser(false))
		{
            return(0);
		}
        
        String docRoot = userMgr.getDocumentRoot(uid);

        if (docRoot == null)
        {
            return(0);
        }
        
        if ((File.separatorChar == '/') && (docRoot.length() == 1))
		{
            return(0);
		}
        		
        if ((File.separatorChar == '\\') && (docRoot.charAt(0) == '*'))
        {
            return(0);
        }
        
        int tokenCounter = 0;
        
        StringTokenizer docRootParser = new StringTokenizer(docRoot, File.separator + "/");
		
        while (docRootParser.hasMoreTokens())
        {
        	docRootParser.nextToken();
        	
        	tokenCounter++;
        }
        
        if (File.separatorChar == '/')
        {
            return(tokenCounter + 1);
        }
        
        return(tokenCounter);
	}

}