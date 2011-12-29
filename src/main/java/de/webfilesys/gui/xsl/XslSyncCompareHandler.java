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
import de.webfilesys.gui.ajax.XmlSelectSyncFolderHandler;
import de.webfilesys.sync.DirSynchronizer;
import de.webfilesys.sync.SyncItem;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslSyncCompareHandler extends XslRequestHandlerBase
{
    public static final String SESSION_ATTRIB_SYNCHRONIZE_ITEMS = "DirSyncItems";
    
    private static final int MAX_PATH_LINE_LENGTH = 55;
    
    /** maximum number of differences to show in the compare result page */
    private static final int MAX_SHOW_DIFF_NUM = 200;
    
	public XslSyncCompareHandler(
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
        String syncSourcePath = (String) session.getAttribute(XmlSelectSyncFolderHandler.SESSION_ATTRIB_SYNC_SOURCE);
        String syncTargetPath = (String) session.getAttribute(XmlSelectSyncFolderHandler.SESSION_ATTRIB_SYNC_TARGET);
		
		Element syncElement = doc.createElement("synchronize");
			
		doc.appendChild(syncElement);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/syncCompare.xsl\"");

		doc.insertBefore(xslRef, syncElement);

		XmlUtil.setChildText(syncElement, "css", userMgr.getCSS(uid), false);
		XmlUtil.setChildText(syncElement, "syncSourcePath", getHeadlinePath(syncSourcePath), false);
		XmlUtil.setChildText(syncElement, "syncTargetPath", getHeadlinePath(syncTargetPath), false);
		
		addMsgResource("headline.syncCompare", getResource("headline.syncCompare", "Synchronize Folders"));
		addMsgResource("label.syncSource", getResource("label.syncSource","source folder"));
		addMsgResource("label.syncTarget", getResource("label.syncTarget","target folder"));
		addMsgResource("button.startSync", getResource("button.startSync","Start Synchronization"));
        addMsgResource("button.cancel", getResource("button.cancel","Cancel"));
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

        addMsgResource("sync.nothingSelected", getResource("sync.nothingSelected","Select at least one file/folder for synchronization!"));
        addMsgResource("sync.noDifference", getResource("sync.noDifference","No differences found (folders are in snyc)."));

        addMsgResource("sync.action.createMissingTarget", getResource("sync.action.createMissingTarget","create missing target folders and copy missing target files"));
        addMsgResource("sync.action.createMissingSource", getResource("sync.action.createMissingSource","create missing source folders and copy missing source files"));
        addMsgResource("sync.action.removeExtraTarget", getResource("sync.action.removeExtraTarget","remove target folders and files that do not exist in the source folder"));
        addMsgResource("sync.action.copyNewerToTarget", getResource("sync.action.copyNewerToTarget","copy newer files from source folder to target folder"));
        addMsgResource("sync.action.copyNewerToSource", getResource("sync.action.copyNewerToSource","copy newer files from target folder to source folder"));
        addMsgResource("sync.action.copyDateChangeToTarget", getResource("sync.action.copyDateChangeToTarget","copy all files with changed modification date from source folder to target folder"));
        addMsgResource("sync.action.copySizeChangeToTarget", getResource("sync.action.copySizeChangeToTarget","copy files with changed size from source folder to target folder"));
        addMsgResource("sync.action.copyAccessRights", getResource("sync.action.copyAccessRights","copy changed access rights from source folder to target folder"));
        
        addMsgResource("sync.confirmStartSync", getResource("sync.confirmStartSync","Are you sure you want to start the synchronization?"));
        addMsgResource("sync.confirmRemoveExtraTarget", getResource("sync.confirmRemoveExtraTarget","Synchronization might remove files and directories in the target folder. Are you sure?"));
        
        Element differencesElement = doc.createElement("differencesList");
        
        syncElement.appendChild(differencesElement);
        
		DirSynchronizer dirSync = new DirSynchronizer(syncSourcePath, syncTargetPath);
		
		ArrayList differences = dirSync.getDifferences();
        
        session.setAttribute(SESSION_ATTRIB_SYNCHRONIZE_ITEMS, differences);
        
        SimpleDateFormat dateFormat = LanguageManager.getInstance().getDateFormat(language);
        
        DecimalFormat numFormat = new DecimalFormat("#,###,###,###,###");
        
        for (int i = 0; (i < differences.size()) && (i < MAX_SHOW_DIFF_NUM); i++)
        {
            SyncItem syncItem = (SyncItem) differences.get(i);
            
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
		
        if (differences.size() > MAX_SHOW_DIFF_NUM) {
            XmlUtil.setChildText(syncElement, "invisibleItems", "true", false);
            addMsgResource("sync.invisibleItems", getResource("sync.invisibleItems", "The number of files out of sync is rather large. Not all differences are shown here."));
        }
        
		this.processResponse("syncCompare.xsl", true);
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