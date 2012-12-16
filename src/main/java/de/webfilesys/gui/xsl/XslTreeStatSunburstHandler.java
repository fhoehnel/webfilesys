package de.webfilesys.gui.xsl;

import java.io.File;
import java.io.PrintWriter;
import java.text.DecimalFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslTreeStatSunburstHandler extends XslRequestHandlerBase
{
    private DecimalFormat numFormat = new DecimalFormat("#,###,###,###,###");
    
    private int depthOfTree;
    
    private long treeFileNum;

	public XslTreeStatSunburstHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid)
	{
        super(req, resp, session, output, uid);
        
        depthOfTree = 0;
        treeFileNum = 0;
	}
	
	protected void process()
	{
		String path = getParameter("path");

		if ((path == null) || (!checkAccess(path)))
		{
		    return;	
		}

        File dirFile = new File(path);
        if ((!dirFile.exists()) || (!dirFile.isDirectory()))
        {
		    Logger.getLogger(getClass()).warn("folder is not a readable directory: " + path);
            return;
        }
		
		Element folderStatsElement = doc.createElement("folderStats");
		
		doc.appendChild(folderStatsElement);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/sunburstChart.xsl\"");

		doc.insertBefore(xslRef, folderStatsElement);

		XmlUtil.setChildText(folderStatsElement, "css", userMgr.getCSS(uid), false);

		addMsgResource("sunburst.noSubFolders", getResource("sunburst.noSubFolders","This folder does not contain any subfolders!"));
		addMsgResource("label.treefiles", getResource("label.treefiles","files in tree"));
		addMsgResource("label.treebytes", getResource("label.treebytes", "bytes in tree"));
		
		processFolder(folderStatsElement, path, 1);
		
		XmlUtil.setChildText(folderStatsElement, "treeDepth", Integer.toString(depthOfTree));

		if (((File.separatorChar == '/') && (path.length() > 1)) ||
			(path.length() > 3))
		{
			if (accessAllowed(dirFile.getParent()))
			{
				XmlUtil.setChildText(folderStatsElement, "parentFolder", escapeForJavascript(dirFile.getParent()));
				addMsgResource("sunburst.parentFolderLink", getResource("sunburst.parentFolderLink", "parent folder"));
			}
		}

		XmlUtil.setChildText(folderStatsElement, "relativePath", CommonUtils.shortName(escapeForJavascript(getHeadlinePath(path)), 100));
		
		processResponse("sunburstChart.xsl", false);
	}

	private long processFolder(Element parentFolderElem, String path, int level) {
        File dirFile = new File(path);
        File[] fileList = dirFile.listFiles();

        long folderTreeSize = 0;
        
        long rootFileSize = 0; // size sum of all files in root folder
        
        long rootFileNum = 0; // number of files in root folder
        
        if (fileList != null)
        {
            for (int i = 0; i < fileList.length; i++)
            {
            	File file = fileList[i];
            	
                if (file.isDirectory())
                {
            		Element folderElem = doc.createElement("folder");

            		folderElem.setAttribute("name", file.getName().replace('\'', '_'));
            		folderElem.setAttribute("shortName", CommonUtils.shortName(file.getName(), 23).replace('\'', '_'));
            		
                    if (level > depthOfTree) 
                    {
                        depthOfTree = level;
                    }

                    long subFolderTreeSize = processFolder(folderElem, file.getAbsolutePath(), level + 1);

                    if (level == 1) 
                    {
                    	// insert sort for folders of first level
                		NodeList folderList = parentFolderElem.getChildNodes();
                        
                		int listLength = folderList.getLength();
                		
            			boolean inserted = false;
            			for (int k = 0; (!inserted) && (k < listLength); k++) 
            			{
            				Node childNode = folderList.item(k);
            				if (childNode instanceof Element)
            				{
            					Element compElem = (Element) childNode;
            					if (compElem.getTagName().equals("folder"))
            					{
                    				long compSize = Long.parseLong(compElem.getAttribute("treeSize"));
                    				if (subFolderTreeSize > compSize)
                    				{
                    					parentFolderElem.insertBefore(folderElem, compElem);
                    					inserted = true;
                    				}
            					}
            				}
            			}
            			if (!inserted) 
            			{
                            parentFolderElem.appendChild(folderElem);
            			}
                    }
                    else
                    {
                        parentFolderElem.appendChild(folderElem);
                    }
                    
                    folderTreeSize += subFolderTreeSize;
                }
                else
                {
                    if (file.isFile())
                    {
                    	folderTreeSize += file.length();

                    	if (level == 1) 
                    	{
                    		rootFileSize += file.length();
                    		rootFileNum++;
                    	}
                    	
                    	treeFileNum++;
                    }
                }
            }
        }

        parentFolderElem.setAttribute("treeSize", Long.toString(folderTreeSize));
        parentFolderElem.setAttribute("formattedTreeSize", numFormat.format(folderTreeSize));
        parentFolderElem.setAttribute("path", path);
        parentFolderElem.setAttribute("shortPath", escapeForJavascript(CommonUtils.shortName(path, 60)));
        
        if (level == 1) 
        {
        	parentFolderElem.setAttribute("rootFileSize", Long.toString(rootFileSize));
        	parentFolderElem.setAttribute("rootFileNum", numFormat.format(rootFileNum));
        	parentFolderElem.setAttribute("formattedRootFileSize", numFormat.format(rootFileSize));
        	parentFolderElem.setAttribute("formattedTreeFileNum", numFormat.format(treeFileNum));
        }
        else if (level == 2)
        {
            parentFolderElem.setAttribute("pathForScript", escapeForJavascript(path));
        }
        
        return folderTreeSize;
	}
}