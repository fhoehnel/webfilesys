package de.webfilesys.gui.xsl;

import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

import de.webfilesys.LanguageManager;
import de.webfilesys.MetaInfManager;
import de.webfilesys.gui.ajax.XmlSelectCompFolderHandler;
import de.webfilesys.sync.DirSynchronizer;
import de.webfilesys.sync.SyncItem;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.PatternComparator;
import de.webfilesys.util.UTF8URLEncoder;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslFolderDiffTreeHandler extends XslRequestHandlerBase
{
    public static final String SESSION_ATTRIB_SYNCHRONIZE_ITEMS = "DirSyncItems";
    
    SimpleDateFormat dateFormat = null;

    public XslFolderDiffTreeHandler(
			HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid)
	{
        super(req, resp, session, output, uid);
        
        dateFormat = LanguageManager.getInstance().getDateFormat(language);
	}
	  
	protected void process()
	{
        String compSourcePath = getParameter("sourcePath");
	    String compTargetPath = getParameter("targetPath");
	    
	    if ((compSourcePath != null) && (compTargetPath != null))
	    {
	        if ((!checkAccess(compSourcePath)) || (!checkAccess(compTargetPath))) {
	            return;
	        }
	    }
	    else
	    {
	        compSourcePath = (String) session.getAttribute(XmlSelectCompFolderHandler.SESSION_ATTRIB_COMP_SOURCE);
	        compTargetPath = (String) session.getAttribute(XmlSelectCompFolderHandler.SESSION_ATTRIB_COMP_TARGET);
	    }

        if ((compSourcePath == null) || (compTargetPath == null))
        {
            Logger.getLogger(getClass()).warn("missing parameter source or target path");
            return;
        }
        
        int sourcePathLength = compSourcePath.length();
        int targetPathLength = compTargetPath.length();
        
		Element compElement = doc.createElement("folderDiff");
			
		doc.appendChild(compElement);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/folderDiffTree.xsl\"");

		doc.insertBefore(xslRef, compElement);

		XmlUtil.setChildText(compElement, "css", userMgr.getCSS(uid), false);
		
	    XmlUtil.setChildText(compElement, "language", language, false);
		
        XmlUtil.setChildText(compElement, "sourcePath", CommonUtils.shortName(getHeadlinePath(compSourcePath), 50), false);
        XmlUtil.setChildText(compElement, "targetPath", CommonUtils.shortName(getHeadlinePath(compTargetPath), 50), false);
		
        String sourceAbsolutePath = compSourcePath;
        if (!sourceAbsolutePath.endsWith(File.separator))
        {
            sourceAbsolutePath = sourceAbsolutePath + File.separator;
        }
        
        String targetAbsolutePath = compTargetPath;
        if (!targetAbsolutePath.endsWith(File.separator))
        {
            targetAbsolutePath = targetAbsolutePath + File.separator;
        }

        XmlUtil.setChildText(compElement, "sourceAbsolutePath", UTF8URLEncoder.encode(sourceAbsolutePath), false);
        XmlUtil.setChildText(compElement, "targetAbsolutePath", UTF8URLEncoder.encode(targetAbsolutePath), false);

        XmlUtil.setChildText(compElement, "sourceViewPath", encodeViewPath(sourceAbsolutePath), false);
        XmlUtil.setChildText(compElement, "targetViewPath", encodeViewPath(targetAbsolutePath), false);
        
        addMsgResource("timeLastModified", getResource("timeLastModified","last modified"));

        Element diffTreeElem = doc.createElement("differenceTree");
        
        compElement.appendChild(diffTreeElem);
        
        boolean ignoreDate = req.getParameter("ignoreDate") != null;
        
        boolean ignoreMetainf = req.getParameter("ignoreMetainf") != null;

        boolean ignorePattern = req.getParameter("ignorePattern") != null;
        
        String excludePattern = null;
        
        if (ignorePattern)
        {
            excludePattern = req.getParameter("excludePattern");
            if ((excludePattern != null) && (excludePattern.trim().length() == 0))
            {
                excludePattern = null;
            }
        }
        
        if (excludePattern != null)
        {
            XmlUtil.setChildText(compElement, "excludePattern", excludePattern);
            addMsgResource("excludePattern", getResource("excludePattern","exclude pattern"));
        }
        
        DirSynchronizer dirSync = new DirSynchronizer(compSourcePath, compTargetPath, ignoreDate);
		
        ArrayList differences = dirSync.getDifferences();
        
        for (int i = 0; i < differences.size(); i++)
        {
            SyncItem syncItem = (SyncItem) differences.get(i);
            
            String diffPath = null;
            
            if ((syncItem.getDiffType() == SyncItem.DIFF_TYPE_MISSING_SOURCE_DIR) ||
                (syncItem.getDiffType() == SyncItem.DIFF_TYPE_MISSING_SOURCE_FILE))
            {
                diffPath = syncItem.getTarget().getPath().substring(targetPathLength + 1);
            }
            else
            {
                diffPath = syncItem.getSource().getPath().substring(sourcePathLength + 1);
            }

            if ((excludePattern == null) || !PatternComparator.patternMatch(diffPath, excludePattern))
            {
                if (ignoreMetainf) {
                    if (diffPath.endsWith(MetaInfManager.METAINF_FILE)) {
                        if (syncItem.getDiffType() == SyncItem.DIFF_TYPE_MISSING_SOURCE_FILE) {
                            dirSync.decrMissingSourceFiles();
                        } else if (syncItem.getDiffType() == SyncItem.DIFF_TYPE_MISSING_TARGET_FILE) {
                            dirSync.decrMissingTargetFiles();
                        } else {
                            dirSync.decrModifiedFiles();
                        }
                    } else {
                        addDifferencePath(diffTreeElem, diffPath, syncItem);
                    }
                } else {
                    addDifferencePath(diffTreeElem, diffPath, syncItem);
                }
            }
        }
        
        XmlUtil.setChildText(compElement, "missingSourceFiles", Integer.toString(dirSync.getMissingSourceFiles()));
        XmlUtil.setChildText(compElement, "missingSourceFolders", Integer.toString(dirSync.getMissingSourceFolders()));
        XmlUtil.setChildText(compElement, "missingTargetFiles", Integer.toString(dirSync.getMissingTargetFiles()));
        XmlUtil.setChildText(compElement, "missingTargetFolders", Integer.toString(dirSync.getMissingTargetFolders()));
        XmlUtil.setChildText(compElement, "modifiedFiles", Integer.toString(dirSync.getModifiedFiles()));
        
        session.removeAttribute(XmlSelectCompFolderHandler.SESSION_ATTRIB_COMP_SOURCE);
        session.removeAttribute(XmlSelectCompFolderHandler.SESSION_ATTRIB_COMP_TARGET);
        session.removeAttribute(XslSyncCompareHandler.SESSION_ATTRIB_SYNCHRONIZE_ITEMS);
        
		processResponse("folderDiffTree.xsl", true);
    }
    
    private void addDifferencePath(Element differenceTreeElem, String diffPath, SyncItem syncItem)
    {
        int diffType = syncItem.getDiffType();
        
        StringTokenizer pathParser = new StringTokenizer(diffPath, File.separator + "/");       
        
        String path = "";
        
        Element folderElem = differenceTreeElem;
        
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
                
                if (!pathParser.hasMoreTokens()) 
                {
                    subFolderElem.setAttribute("leaf", "true");

                    if ((diffType == SyncItem.DIFF_TYPE_MODIFICATION_TIME) ||
                        (diffType == SyncItem.DIFF_TYPE_SIZE) ||
                        (diffType == SyncItem.DIFF_TYPE_SIZE_TIME)) 
                    {
                        XmlUtil.setChildText(subFolderElem, "relPath", insertDoubleBackslash(path));
                        XmlUtil.setChildText(subFolderElem, "lastModified", dateFormat.format(new Date(syncItem.getTarget().getModificationTime())));
                    }
                    else
                    {
                        String viewPath = encodeViewPath(path);
                        
                        if (viewPath.startsWith("/")) 
                        {
                            viewPath = viewPath.substring(1);
                        }
                        
                        if (diffType == SyncItem.DIFF_TYPE_MISSING_TARGET_FILE)
                        {
                            XmlUtil.setChildText(subFolderElem, "viewPath", viewPath);
                        }
                        if (diffType == SyncItem.DIFF_TYPE_MISSING_SOURCE_FILE) 
                        {
                            XmlUtil.setChildText(subFolderElem, "viewPath", viewPath);
                            
                            File targetFile = new File(syncItem.getTarget().getPath());
                            if (targetFile.exists()) {
                                XmlUtil.setChildText(subFolderElem, "lastModified", dateFormat.format(new Date(targetFile.lastModified())));
                            }
                        }
                    }
                }
                
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
                
                subFolderElem.setAttribute("diffType", Integer.toString(diffType));
            }

            folderElem = subFolderElem;
        }
    }
    
    private String encodeViewPath(String path) 
    {
        StringBuffer encodedPath = new StringBuffer();
        
        StringTokenizer pathParser = new StringTokenizer(path, "/\\", true);
        
        while (pathParser.hasMoreTokens())
        {
            String token = pathParser.nextToken();
            if (token.equals("/") || token.equals("\\"))
            {
                encodedPath.append('/');
            }
            else 
            {
                encodedPath.append(UTF8URLEncoder.encode(token));
            }
        }
        return encodedPath.toString();
    }
}