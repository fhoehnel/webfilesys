package de.webfilesys.gui.xsl;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.StringComparator;
import de.webfilesys.util.UTF8URLEncoder;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslTreeStatsHandler extends XslRequestHandlerBase
{
	public XslTreeStatsHandler(
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
		String currentPath = getParameter("actpath");

		Element treeStatsElement = doc.createElement("treeStats");
			
		doc.appendChild(treeStatsElement);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/treeStatistics.xsl\"");

		doc.insertBefore(xslRef, treeStatsElement);

		XmlUtil.setChildText(treeStatsElement, "css", userMgr.getCSS(uid), false);
		XmlUtil.setChildText(treeStatsElement, "currentPath", currentPath, false);
		XmlUtil.setChildText(treeStatsElement, "relativePath", this.getHeadlinePath(currentPath), false);
		XmlUtil.setChildText(treeStatsElement, "shortPath", CommonUtils.shortName(this.getHeadlinePath(currentPath), 60), false);
		
		addMsgResource("label.subdirs", getResource("label.subdirs","subdirectories"));
		addMsgResource("label.subdirlevels", getResource("label.subdirlevels","subdir levels"));
		addMsgResource("label.firstlevelfiles", getResource("label.firstlevelfiles","files in first level"));
		addMsgResource("label.firstlevelbytes", getResource("label.firstlevelbytes","bytes in first level"));
		addMsgResource("label.treefiles", getResource("label.treefiles","files in tree"));
		addMsgResource("label.treebytes", getResource("label.treebytes","bytes in tree"));

		addMsgResource("button.closewin", getResource("button.closewin","Close Window"));

        long bytesInFirstLevel = 0l;
        
        int filesInFirstLevel = 0;
        
        ArrayList folders = new ArrayList();
        
        File dirFile = new File(currentPath);
        
        String fileList[] = dirFile.list();

        if (fileList != null)
        {
            for (int i = 0; i < fileList.length; i++)
            {
                 File tempFile = new File(currentPath, fileList[i]);
                 
                 if (tempFile.isDirectory())
                 {
                     folders.add(fileList[i]);
                 }
                 else
                 {
                     if (tempFile.isFile())
                     {
                         bytesInFirstLevel += tempFile.length();

                         filesInFirstLevel++;
                     }
                 }
            }
            
            if (folders.size() > 1)
            {
                Collections.sort(folders, new StringComparator(StringComparator.SORT_IGNORE_CASE));
            }
            
            if (folders.size() > 0)
            {
                Element folderListElement = doc.createElement("folders");

                treeStatsElement.appendChild(folderListElement);
 
                Iterator iter = folders.iterator();
                
                while (iter.hasNext()) {
                    String folderName = (String) iter.next();
                    
                    String shortFolderName = CommonUtils.shortName(folderName, 20);
                    
                    Element folderElement = doc.createElement("folder");

                    folderElement.setAttribute("name", folderName);
                    if (!folderName.equals(shortFolderName))
                    {
                        folderElement.setAttribute("shortName", shortFolderName);
                    }
                    folderElement.setAttribute("path", UTF8URLEncoder.encode(CommonUtils.getFullPath(currentPath, folderName)));
                    
                    folderListElement.appendChild(folderElement);
                }
            }
        }

		XmlUtil.setChildText(treeStatsElement, "subdirNum", Integer.toString(folders.size()), false);

		XmlUtil.setChildText(treeStatsElement, "dirFiles", Integer.toString(filesInFirstLevel), false);

		XmlUtil.setChildText(treeStatsElement, "dirBytes", Long.toString(bytesInFirstLevel), false);

		this.processResponse("treeStatistics.xsl", false);
    }
}