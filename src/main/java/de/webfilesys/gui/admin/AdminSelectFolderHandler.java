package de.webfilesys.gui.admin;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.Constants;
import de.webfilesys.DirTreeStatus;
import de.webfilesys.SubdirExistCache;
import de.webfilesys.SubdirExistTester;
import de.webfilesys.graphics.ThumbnailThread;
import de.webfilesys.gui.xsl.XslRequestHandlerBase;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.StringComparator;
import de.webfilesys.util.UTF8URLEncoder;
import de.webfilesys.util.XmlUtil;

/**
 * Select a folder for an administrative task, for example to select the document root for an user.
 * Base class for Win and Unix version.
 * 
 * @author Frank Hoehnel
 */
public class AdminSelectFolderHandler extends XslRequestHandlerBase
{
	protected int dirCounter;
	protected int currentDirNum;
	
	protected Element folderTreeElement = null;
	
	Element resourcesElement = null;
	
    protected DirTreeStatus dirTreeStatus = null;
    
    protected String actPath = null;
	
	public AdminSelectFolderHandler(
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

        // fastpath - expand complete path from root dir and collapse all other
        String expandPath = getParameter("expandPath");
        
        if (expandPath != null)
        {
            dirTreeStatus.collapseAll();

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

		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/adminSelectFolder.xsl\"");

		folderTreeElement = doc.createElement("folderTree");
			
		doc.appendChild(folderTreeElement);
			
		doc.insertBefore(xslRef, folderTreeElement);
		
		if (this.isBrowserXslEnabled())
		{
			Element xslEnabledElement = doc.createElement("browserXslEnabled");
			
			XmlUtil.setElementText(xslEnabledElement, "true", false);
			
			folderTreeElement.appendChild(xslEnabledElement);
		}
		
		dirCounter = 0;
		currentDirNum = 0;
	}
	
	protected void addMsgResource(String key, String value)
	{
		if (resourcesElement == null)
		{
			resourcesElement = doc.createElement("resources");
			
			folderTreeElement.appendChild(resourcesElement);
		}
		
		Element msgElement = doc.createElement("msg");
		
		resourcesElement.appendChild(msgElement);
		
		msgElement.setAttribute("key", key);
		msgElement.setAttribute("value", value);
	}
	
	protected void dirSubTree(Element parentFolder, String actPath, String partOfPath, boolean belowDocRoot)
	{
		File subdirFile = new File(partOfPath);

		String fileList[] = subdirFile.list();

		if (fileList == null)
		{
			Logger.getLogger(getClass()).warn("filelist is null for " + partOfPath);
			
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

		for (int i=0; i<fileList.length; i++)
		{
			String subdirPath = null;

			subdirPath = pathWithSlash + fileList[i];

			File tempFile = new File(subdirPath);

			if (tempFile.isDirectory())
			{
				String subdirName = fileList[i];

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

		if (subdirList.size()==0)
		{
			return;
		}

		if (subdirList.size()>1)
		{
			Collections.sort(subdirList, new StringComparator(StringComparator.SORT_IGNORE_CASE));
		}

		for (String subdirPath : subdirList) {

			boolean access = (belowDocRoot || accessAllowed(subdirPath));

            Element parentForSubdirs = parentFolder;

			Element folderElement = null;

			if (access)
			{
				dirCounter++;
				
				String encodedPath = CommonUtils.replaceAll(UTF8URLEncoder.encode(subdirPath), "+", "%20");

				boolean hasSubdirs = true;

				Integer subdirExist = SubdirExistCache.getInstance().existsSubdir(subdirPath);

				if (subdirExist == null)
				{
			        SubdirExistTester.getInstance().queuePath(subdirPath, 1, false);	        
				}
				else
				{
					hasSubdirs = (subdirExist.intValue()==1);
				}

                folderElement = doc.createElement("folder");
                
                parentFolder.appendChild(folderElement);
                
				folderElement.setAttribute("name", subdirPath.substring(subdirPath.lastIndexOf(File.separatorChar)+1));                

				folderElement.setAttribute("id", Integer.toString(dirCounter));

				folderElement.setAttribute("path", encodedPath);      

				if (!hasSubdirs)
				{
					folderElement.setAttribute("leaf","true");    
				}

				if (subdirPath.equals(actPath))
				{
					currentDirNum = dirCounter;
					
					folderElement.setAttribute("current","true");
				}
				
				if (File.separatorChar=='/')
				{
					File tempFile = new File(subdirPath);

					if (dirIsLink(tempFile))
					{
						try
						{
							folderElement.setAttribute("link", "true");
                        
							folderElement.setAttribute("linkDir", tempFile.getCanonicalPath());
						}
						catch (IOException ioex)
						{
							Logger.getLogger(getClass()).error(ioex);
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
