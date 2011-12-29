package de.webfilesys.gui.xsl;

import java.io.File;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.LanguageManager;
import de.webfilesys.MetaInfManager;
import de.webfilesys.gui.ajax.XmlSelectCompFolderHandler;
import de.webfilesys.sync.DirSynchronizer;
import de.webfilesys.sync.SyncItem;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslCompareFolderHandler extends XslRequestHandlerBase
{
    public static final String SESSION_ATTRIB_SYNCHRONIZE_ITEMS = "DirSyncItems";
    
    private static final int MAX_PATH_LINE_LENGTH = 55;
    
    /** maximum number of differences to show in the compare result page */
    private static final int MAX_SHOW_DIFF_NUM = 1000;
    
	public XslCompareFolderHandler(
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
        String compSourcePath = (String) session.getAttribute(XmlSelectCompFolderHandler.SESSION_ATTRIB_COMP_SOURCE);
        String compTargetPath = (String) session.getAttribute(XmlSelectCompFolderHandler.SESSION_ATTRIB_COMP_TARGET);
		
		Element compElement = doc.createElement("compareFolder");
			
		doc.appendChild(compElement);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/compFolderResult.xsl\"");

		doc.insertBefore(xslRef, compElement);

		XmlUtil.setChildText(compElement, "css", userMgr.getCSS(uid), false);
        XmlUtil.setChildText(compElement, "sourcePath", getHeadlinePath(compSourcePath), false);
        XmlUtil.setChildText(compElement, "targetPath", getHeadlinePath(compTargetPath), false);
		
        addMsgResource("headline.compareFolders", getResource("headline.compareFolders", "Compare Folder Results"));
        addMsgResource("label.compSource", getResource("label.compSource", "compare folders"));
        addMsgResource("label.compSourceFolder", getResource("label.compSourceFolder", "source folder"));
        addMsgResource("label.compTargetFolder", getResource("label.compTargetFolder", "target folder"));
        addMsgResource("button.closewin", getResource("button.closewin","close window"));

        addMsgResource("diffType.missingSourceFile", getResource("diffType.missingSourceFile","file missing in source folder"));
        addMsgResource("diffType.missingTargetFile", getResource("diffType.missingTargetFile","file missing in target folder"));
        addMsgResource("diffType.missingSourceFolder", getResource("diffType.missingSourceFolder","directory missing in source folder"));
        addMsgResource("diffType.missingTargetFolder", getResource("diffType.missingTargetFolder","directory missing in target folder"));
        addMsgResource("diffType.size", getResource("diffType.size","size changed"));
        addMsgResource("diffType.modified", getResource("diffType.modified","last modification time changed"));
        addMsgResource("diffType.sizeAndModified", getResource("diffType.sizeAndModified","size and last modification time changed"));
        addMsgResource("diffType.accessRights", getResource("diffType.accessRights","access rights changed"));

        addMsgResource("sync.tableHeadPath", getResource("sync.tableHeadPath","path"));
        addMsgResource("sync.tableHeadSource", getResource("sync.tableHeadSource","source"));
        addMsgResource("sync.tableHeadTarget", getResource("sync.tableHeadTarget","target"));
        addMsgResource("sync.missing", getResource("sync.missing","missing"));

        addMsgResource("sync.bytes", getResource("sync.bytes","bytes"));

        addMsgResource("sync.access.none", getResource("sync.access.none","access forbidden"));
        addMsgResource("sync.access.readonly", getResource("sync.access.readonly","read"));
        addMsgResource("sync.access.readwrite", getResource("sync.access.readwrite","write"));

        addMsgResource("sync.noDifference", getResource("sync.noDifference","No differences found (folders are in snyc)."));

        Element differencesElement = doc.createElement("differencesList");
        
        compElement.appendChild(differencesElement);
        
        boolean ignoreDate = req.getParameter("ignoreDate") != null;
        
        boolean ignoreMetainf = req.getParameter("ignoreMetainf") != null;

        DirSynchronizer dirSync = new DirSynchronizer(compSourcePath, compTargetPath, ignoreDate);
		
		ArrayList differences = dirSync.getDifferences();
        
        SimpleDateFormat dateFormat = LanguageManager.getInstance().getDateFormat(language);
        
        DecimalFormat numFormat = new DecimalFormat("#,###,###,###,###");
        
        for (int i = 0; (i < differences.size()) && (i < MAX_SHOW_DIFF_NUM); i++)
        {
            SyncItem syncItem = (SyncItem) differences.get(i);
            
            String diffPath = null;
            
            if ((syncItem.getDiffType() == SyncItem.DIFF_TYPE_MISSING_SOURCE_DIR) ||
                (syncItem.getDiffType() == SyncItem.DIFF_TYPE_MISSING_SOURCE_FILE))
            {
                diffPath = syncItem.getTarget().getPath();
            }
            else
            {
                diffPath = syncItem.getSource().getPath();
            }
            
            if ((!ignoreMetainf) || (!diffPath.endsWith(MetaInfManager.METAINF_FILE))) {
                Element diffElement = doc.createElement("difference");
                
                XmlUtil.setChildText(diffElement, "id", Integer.toString(syncItem.getId()));

                XmlUtil.setChildText(diffElement, "diffType", Integer.toString(syncItem.getDiffType()));

                XmlUtil.setChildText(diffElement, "fileName", syncItem.getFileName());
                
                Element sourceElement = doc.createElement("source");
                
                XmlUtil.setChildText(sourceElement, "path", syncItem.getSource().getPath());
                XmlUtil.setChildText(sourceElement, "displayPath", getForcedLineBreakPath(getHeadlinePath(syncItem.getSource().getPath())));
                XmlUtil.setChildText(sourceElement, "size", numFormat.format(syncItem.getSource().getSize()));
                XmlUtil.setChildText(sourceElement, "modified", dateFormat.format(new Date(syncItem.getSource().getModificationTime())));
                XmlUtil.setChildText(sourceElement, "canRead", "" + syncItem.getSource().getCanRead());
                XmlUtil.setChildText(sourceElement, "canWrite", "" + syncItem.getSource().getCanWrite());
                
                diffElement.appendChild(sourceElement);            
                
                Element targetElement = doc.createElement("target");
                
                XmlUtil.setChildText(targetElement, "path", syncItem.getTarget().getPath());
                XmlUtil.setChildText(targetElement, "displayPath", getForcedLineBreakPath(getHeadlinePath(syncItem.getTarget().getPath())));
                XmlUtil.setChildText(targetElement, "size", numFormat.format(syncItem.getTarget().getSize()));
                XmlUtil.setChildText(targetElement, "modified", dateFormat.format(new Date(syncItem.getTarget().getModificationTime())));
                XmlUtil.setChildText(targetElement, "canRead", "" + syncItem.getTarget().getCanRead());
                XmlUtil.setChildText(targetElement, "canWrite", "" + syncItem.getTarget().getCanWrite());
                
                diffElement.appendChild(targetElement);            
                
                differencesElement.appendChild(diffElement);
            }
        }
		
        if (differences.size() > MAX_SHOW_DIFF_NUM) {
            XmlUtil.setChildText(compElement, "invisibleItems", "true", false);
            addMsgResource("sync.invisibleItems", getResource("sync.invisibleItems", "The number of files out of sync is rather large. Not all differences are shown here."));
        }
        
		processResponse("compFolderResult.xsl", true);
    }
    
    private String getForcedLineBreakPath(String path)
    {
        if (path.length() <= MAX_PATH_LINE_LENGTH)
        {
            return path;
        }
        
        StringBuffer buff = new StringBuffer();
        
        String restOfPath = path;
        
        boolean done = false;
        
        while (!done)
        {
            if (restOfPath.length() <= MAX_PATH_LINE_LENGTH)
            {
                buff.append(restOfPath);
                done = true;
            }
            else
            {
                String partOfPath = restOfPath.substring(0, MAX_PATH_LINE_LENGTH);
                
                boolean sepFound = true;
                
                int idx = MAX_PATH_LINE_LENGTH - 1;
                
                while ((idx > 0) && (partOfPath.charAt(idx) != File.separatorChar))
                {
                    idx--;
                }
                
                if (idx == 0)
                {
                    // no separator found
                    sepFound = false;
                    idx = MAX_PATH_LINE_LENGTH;
                }
                
                buff.append(partOfPath.substring(0, idx));
                
                if (sepFound)
                {
                    buff.append(File.separatorChar);
                    idx++;
                }
                
                // buff.append("<br/>");
                buff.append(' ');
                
                restOfPath = restOfPath.substring(idx);
            }
        }
        
        return buff.toString();
    }
}