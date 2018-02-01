package de.webfilesys.gui.xsl;

import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.Category;
import de.webfilesys.IconManager;
import de.webfilesys.MetaInfManager;
import de.webfilesys.WebFileSys;
import de.webfilesys.graphics.ThumbnailThread;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.PatternComparator;
import de.webfilesys.util.UTF8URLEncoder;
import de.webfilesys.util.XmlUtil;

/**
 * Search for files and present search results as tree.
 * 
 * @author Frank Hoehnel
 */
public class XslFindFileHandler extends XslRequestHandlerBase
{
	int filesFoundNum;
	
	int docRootTokenCount;
	
	Element searchResultElement = null;

	MetaInfManager metaInfMgr = null;

	public XslFindFileHandler(
			HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid)
	{
        super(req, resp, session, output, uid);

		metaInfMgr = MetaInfManager.getInstance();
	}

	protected void process()
	{
		String actPath = getParameter("actpath");
		
		if ((actPath == null) || (actPath.trim().length() == 0))
		{
			actPath = getCwd();
		}

		if (!checkAccess(actPath))
		{
			return;
		}

		String fileNamePattern = getParameter("FindMask");
		if ((fileNamePattern == null) || (fileNamePattern.length() == 0))
		{
			fileNamePattern = "*";
		}

		String includeSubdirs = getParameter("includeSubdirs");

        String datePickerFormat = getResource("datePickerFormat", "mm/dd/yy").replace("m",  "M");
        
        SimpleDateFormat dateParser = new SimpleDateFormat(datePickerFormat);
        
		Date fromDate = new Date(0L);
		Date toDate = new Date();

		String dateRangeFrom = getParameter("dateRangeFrom");
		String dateRangeUntil = getParameter("dateRangeUntil");
		
		try {
			if (!CommonUtils.isEmpty(dateRangeFrom)) {
				fromDate = dateParser.parse(dateRangeFrom);
				fromDate.setHours(0);
				fromDate.setMinutes(0);
				fromDate.setSeconds(0);
			}
			if (!CommonUtils.isEmpty(dateRangeUntil)) {
				toDate = dateParser.parse(dateRangeUntil);
                toDate.setHours(23);
                toDate.setMinutes(59);
                toDate.setSeconds(59);
			}
		} catch (Exception ex) {
			Logger.getLogger(getClass()).warn("invalid date format in search date range", ex);
		}

        Category category = null;

        String categoryName = getParameter("category");

        if (!categoryName.equals("-1"))
        {
            category = new Category();
            category.setName(categoryName);
        }
        
        searchResultElement = doc.createElement("searchResult");
        
        doc.appendChild(searchResultElement);
            
        ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/findFileResult.xsl\"");

        doc.insertBefore(xslRef, searchResultElement);

        XmlUtil.setChildText(searchResultElement, "css", userMgr.getCSS(uid), false);

        addMsgResource("label.searchresults", getResource("label.searchresults", "Search Results"));
        addMsgResource("label.in", getResource("label.in", "in"));
        addMsgResource("button.closewin", getResource("button.closewin", "Close Window"));
        
        XmlUtil.setChildText(searchResultElement, "fileNamePattern", fileNamePattern);

        XmlUtil.setChildText(searchResultElement, "shortPath", CommonUtils.shortName(getHeadlinePath(actPath), 40));

        docRootTokenCount = getDocRootTokenCount();
        
		filesFoundNum = 0;
			
		findFile(actPath, fileNamePattern, (includeSubdirs != null), fromDate.getTime(), toDate.getTime(), category);

		XmlUtil.setChildText(searchResultElement, "matchCount", Integer.toString(filesFoundNum));
		
        processResponse("findFileResult.xsl", false);
	}
	
	public void findFile(String actPath, String fileNamePattern, boolean includeSubdirs, 
	    long fromDate, long toDate, Category category)
	{
		boolean filePatternGiven = (!fileNamePattern.equals("*")) && (!fileNamePattern.equals("*.*"));

        File dirFile = new File(actPath);
        String[] fileList = dirFile.list();

		if (fileList != null)
		{
			for (int i = 0; i < fileList.length; i++)
			{
                File tempFile = new File(actPath, fileList[i]);

				if (tempFile.isDirectory())
				{
					if (includeSubdirs)
					{
						if (!dirIsLink(tempFile))
						{
							if (!fileList[i].equals(ThumbnailThread.THUMBNAIL_SUBDIR))
							{
                                String subDir = null;

								if (actPath.endsWith(File.separator))
								{
									subDir = actPath + fileList[i];
								}
								else
								{
									subDir = actPath + File.separator + fileList[i];
								}
									
								findFile(subDir, fileNamePattern, includeSubdirs, fromDate, toDate, category);
							}
						}
					}
				}
				else
				{
					if (PatternComparator.patternMatch(fileList[i], fileNamePattern))
					{
						if (filePatternGiven || (!fileList[i].equals(MetaInfManager.METAINF_FILE)))
						{
							// if any file with given date range is searched, ignore the metainf files
							
							if ((tempFile.lastModified() >= fromDate) && (tempFile.lastModified() <= toDate))
                            {
                                if ((category == null) || 
                                    metaInfMgr.isCategoryAssigned(actPath, fileList[i], category))
                                {
                                    addSearchResult(searchResultElement, tempFile.getAbsolutePath());
                                    
                                    filesFoundNum++;
                                }
                            }
						}
					}
				}
			}
			
            if (category != null)
            {
                metaInfMgr.releaseMetaInf(actPath);
            }
		}
		else
		{
		    Logger.getLogger(getClass()).error("cannot get dir entries for " + actPath);
		}

		fileList = null;
	}
	
    private void addSearchResult(Element searchResultElement, String filePath)
    {
        File testDir = new File(filePath);
        
        if (!testDir.exists())
        {
            return;
        }
        
        StringTokenizer pathParser = new StringTokenizer(filePath, File.separator + "/");       
        
        String path = "";
        
        int tokenCounter = 0;
        
        Element folderElem = searchResultElement;
        
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
                    
                    if (!pathParser.hasMoreTokens()) 
                    {
                        subFolderElem.setAttribute("file", "true");

                        subFolderElem.setAttribute("icon" , getFileIcon(partOfPath));
                    }
                    
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
    
    private String getFileIcon(String filePath)
    {
        if (!WebFileSys.getInstance().isShowAssignedIcons()) {
            return(IconManager.DEFAULT_ICON);
        }
        
        int extIdx = filePath.lastIndexOf('.');

        if ((extIdx > 0) && (extIdx < (filePath.length() - 1)))
        {
            return(IconManager.getInstance().getAssignedIcon(filePath.substring(extIdx + 1)));
        }
        
        return(IconManager.DEFAULT_ICON);
    }
    
}
