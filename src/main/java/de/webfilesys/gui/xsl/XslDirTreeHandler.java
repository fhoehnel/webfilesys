package de.webfilesys.gui.xsl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.DirTreeStatus;
import de.webfilesys.SubdirExistCache;
import de.webfilesys.TestSubDirThread;
import de.webfilesys.decoration.Decoration;
import de.webfilesys.decoration.DecorationManager;
import de.webfilesys.graphics.ThumbnailThread;
import de.webfilesys.util.StringComparator;
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

        dirTreeStatus = (DirTreeStatus) session.getAttribute("dirTreeStatus");
		
		if (dirTreeStatus == null)
		{
			dirTreeStatus = new DirTreeStatus();
			
			session.setAttribute("dirTreeStatus", dirTreeStatus);
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

		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/folderTree.xsl\"");

		folderTreeElement = doc.createElement("folderTree");
			
		doc.appendChild(folderTreeElement);
			
		doc.insertBefore(xslRef, folderTreeElement);
		
		if (this.isBrowserXslEnabled())
		{
			Element xslEnabledElement = doc.createElement("browserXslEnabled");
			
			XmlUtil.setElementText(xslEnabledElement, "true", false);
			
			folderTreeElement.appendChild(xslEnabledElement);
		}
		
		dirCounter=0;
		currentDirNum=0;

		String loginEvent = (String) session.getAttribute("loginEvent");
		
        if (loginEvent != null)
        {
			if (isWebspaceUser() && (!readonly))
			{
				Element loginEventElement = doc.createElement("loginEvent");
			
				folderTreeElement.appendChild(loginEventElement);
			}

            session.removeAttribute("loginEvent");
        }
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

		Vector subdirList = new Vector();

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
					(
					(docRoot.indexOf(subdirPath.replace('\\','/'))==0) &&
					(
					(subdirPathLength==docRoot.length()) ||
					(docRoot.charAt(subdirPathLength)=='/')
					)
					)
				   )
				{
					if (!subdirName.equals(ThumbnailThread.THUMBNAIL_SUBDIR))
					{
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
			Collections.sort(subdirList,new StringComparator(StringComparator.SORT_IGNORE_CASE));
		}

		DecorationManager decoMgr = DecorationManager.getInstance();
		
		for (int i=0;i<subdirList.size();i++)
		{
			String subdirPath=(String) subdirList.elementAt(i);

			boolean access = (belowDocRoot || accessAllowed(subdirPath));

            Element parentForSubdirs = parentFolder;

			Element folderElement = null;

			if (access)
			{
				dirCounter++;
				
				boolean hasSubdirs = true;

				Integer subdirExist = SubdirExistCache.getInstance().existsSubdir(subdirPath);

				if (subdirExist == null)
				{
			        (new TestSubDirThread(subdirPath)).start();
				}
				else
				{
					hasSubdirs = (subdirExist.intValue()==1);
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
				
				if (!hasSubdirs)
				{
					folderElement.setAttribute("leaf","true");    
				}

				if (subdirPath.equals(actPath))
				{
					currentDirNum = dirCounter;
					
					folderElement.setAttribute("current","true");
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