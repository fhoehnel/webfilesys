package de.webfilesys.gui.xsl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Element;

import de.webfilesys.Constants;
import de.webfilesys.DirTreeStatus;
import de.webfilesys.SubdirExistCache;
import de.webfilesys.SubdirExistTester;
import de.webfilesys.decoration.Decoration;
import de.webfilesys.decoration.DecorationManager;
import de.webfilesys.graphics.ThumbnailThread;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.UTF8URLEncoder;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslDirTreeHandler extends XslRequestHandlerBase
{
	protected int dirCounter;
	protected int currentDirNum;
	
	protected Element folderTreeElement = null;
	
	Element resourcesElement = null;
	
    protected DirTreeStatus dirTreeStatus = null;
    
    protected String actPath = null;
    
	public XslDirTreeHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid)
	{
        super(req, resp, session, output, uid);
        
        dirTreeStatus = (DirTreeStatus) session.getAttribute(Constants.SESSION_KEY_DIR_TREE_STATUS);
		
		if (dirTreeStatus == null)
		{
			dirTreeStatus = new DirTreeStatus();
			
			session.setAttribute(Constants.SESSION_KEY_DIR_TREE_STATUS, dirTreeStatus);
		}
		
		actPath = getParameter("actPath");
		
        String expand = getParameter("expand");

        String expandPath = getParameter("expandPath");
        
        if (expandPath != null)
        {
            // expand complete path from root dir and keep other expanded open
        	// dirTreeStatus.collapseAll();

            dirTreeStatus.expandPath(expandPath);

            actPath = expandPath;
        }
        else
        {
            if (expand != null)
            {
            	dirTreeStatus.expandDir(expand);

                actPath = expand;
            }
        }

        String collapsePath = getParameter("collapse");

        if (collapsePath != null)
        {
        	dirTreeStatus.collapseDir(collapsePath);

        	actPath = collapsePath;
        }

        if (actPath == null) {
        	actPath = getCwd();
        }
        
        String viewModeParam = getParameter("viewMode");
        if (!CommonUtils.isEmpty(viewModeParam)) {
        	try {
    			session.setAttribute("viewMode", new Integer(Integer.parseInt(viewModeParam)));
        	} catch (Exception ex) {
        	}
        }
        
		folderTreeElement = doc.createElement("folderTree");
			
		doc.appendChild(folderTreeElement);
			
		if (this.isBrowserXslEnabled())
		{
			Element xslEnabledElement = doc.createElement("browserXslEnabled");
			
			XmlUtil.setElementText(xslEnabledElement, "true", false);
			
			folderTreeElement.appendChild(xslEnabledElement);
		}
		
		String errorMsg = getParameter("errorMsg");
		
	    if (errorMsg != null)
		{
		    XmlUtil.setChildText(folderTreeElement, "errorMsg", errorMsg, false);
		}
		
		dirCounter=0;
		currentDirNum=0;

		String loginEvent = (String) session.getAttribute(Constants.SESSION_KEY_LOGIN_EVENT);
		
        if (loginEvent != null)
        {
			if (isWebspaceUser() && (!readonly))
			{
				Element loginEventElement = doc.createElement("loginEvent");
			
				folderTreeElement.appendChild(loginEventElement);
			}

            session.removeAttribute(Constants.SESSION_KEY_LOGIN_EVENT);
        }
	}
	
	protected void dirSubTree(Element parentFolder, String actPath, String partOfPath, boolean belowDocRoot)
	{
		File folderFile = new File(partOfPath);
		
		File fileList[] = folderFile.listFiles();

		if (fileList == null)
		{
			LogManager.getLogger(getClass()).warn("filelist is null for " + partOfPath);
			
			dirTreeStatus.collapseDir(partOfPath);
			return;
		}

		if (fileList.length == 0)
		{
			return;
		}

		String docRoot = userMgr.getDocumentRoot(uid);

		String pathWithSlash = partOfPath;

		if (!partOfPath.endsWith(File.separator))
		{
			pathWithSlash = partOfPath + File.separator;
		}

		ArrayList<String> subdirList = new ArrayList<String>();

		for (File file : fileList) {
		
			if (file.isDirectory()) {
				String subdirName = file.getName();

				String subdirPath = pathWithSlash + file.getName();

				int subdirPathLength = subdirPath.length();

				if (belowDocRoot || accessAllowed(subdirPath) ||
					((docRoot.indexOf(subdirPath.replace('\\','/')) == 0) &&
					 ((subdirPathLength == docRoot.length()) ||
					  (docRoot.charAt(subdirPathLength) == '/')))) {
					
					if (!subdirName.equals(ThumbnailThread.THUMBNAIL_SUBDIR)) {
						subdirList.add(subdirPath);
					}
				}
			}
		}

		if (subdirList.size() == 0) {
			return;
		}

		if (subdirList.size() > 1) {
			Collections.sort(subdirList, (folder1, folder2) -> folder1.compareToIgnoreCase(folder2));
		}

		DecorationManager decoMgr = DecorationManager.getInstance();
		
		for (int i=0;i<subdirList.size();i++)
		{
			String subdirPath=(String) subdirList.get(i);

			boolean access = (belowDocRoot || accessAllowed(subdirPath));

            Element parentForSubdirs = parentFolder;

			Element folderElement = null;

			if (access)
			{
				dirCounter++;
				
				Integer subdirExist = SubdirExistCache.getInstance().existsSubdir(subdirPath);

				if (subdirExist == null)
				{
			        SubdirExistTester.getInstance().queuePath(subdirPath, 1, false);	        
				}

                folderElement = doc.createElement("folder");
                
                parentFolder.appendChild(folderElement);
                
                String folderName = subdirPath.substring(subdirPath.lastIndexOf(File.separatorChar) + 1);
                
				folderElement.setAttribute("name", folderName);                

				folderElement.setAttribute("id", Integer.toString(dirCounter));

				String encodedPath = null;
				
	            if (subdirPath.indexOf('\'') > 0) {
	                encodedPath = UTF8URLEncoder.encode(subdirPath.replace('\'', '`'));
	            } else {
	                encodedPath = UTF8URLEncoder.encode(subdirPath);
	            }
				
				folderElement.setAttribute("path", encodedPath);      

				folderElement.setAttribute("menuPath", insertDoubleBackslash(subdirPath));  
				
				if (subdirExist == null) {
					folderElement.setAttribute("leaf", "unknown");    
				} else if (subdirExist.intValue() != 1) {
					folderElement.setAttribute("leaf", "true");    
				}

				if (subdirPath.equals(actPath))
				{
					currentDirNum = dirCounter;
					
					folderElement.setAttribute("current","true");
				}
				
				if (subdirPath.replace('\\','/').equals(docRoot)) {
					folderElement.setAttribute("root", "true");
				}
				
				Decoration deco = decoMgr.getDecoration(subdirPath);
				
				if (deco != null) 
				{
					if (deco.getIcon() != null) 
					{
		                folderElement.setAttribute("icon", deco.getIcon());
					}
					if (deco.getTextColor() != null) 
					{
		                folderElement.setAttribute("textColor", deco.getTextColor());
					}
				}
				
				if (File.separatorChar=='/')
				{
		            // there is no way to detect NTFS symbolic links / junctions with Java functions
		            // see http://stackoverflow.com/questions/3249117/cross-platform-way-to-detect-a-symbolic-link-junction-point
	                
				    File linkTestFile = new File(subdirPath);

	                if (dirIsLink(linkTestFile))
	                {
	                    try
	                    {
	                        folderElement.setAttribute("link", "true");
	                    
	                        folderElement.setAttribute("linkDir", linkTestFile.getCanonicalPath());
	                    }
	                    catch (IOException ioex)
	                    {
	                        LogManager.getLogger(getClass()).error(ioex);
	                    }
	                }
				}
				
				parentForSubdirs = folderElement;
			}

			if (dirTreeStatus.dirExpanded(subdirPath))
			{
				dirSubTree(parentForSubdirs, actPath, subdirPath, access);
			}
		}
	}
	
}