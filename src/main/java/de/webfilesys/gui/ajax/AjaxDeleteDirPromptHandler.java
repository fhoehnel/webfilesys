package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;

import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.UTF8URLEncoder;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class AjaxDeleteDirPromptHandler extends XmlRequestHandlerBase
{
	public AjaxDeleteDirPromptHandler(
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
        if (!checkWriteAccess())
        {
            return;
        }
        
        String delPath = req.getParameter("param1");

        if (!checkAccess(delPath))
        {
            return;
        }

        Element resultElement = doc.createElement("result");
        
        File delFile = new File(delPath);
        
        String deletePromptMsg = null;

        if (delFile.exists() && delFile.isDirectory())
        {
            String[] filesInDir = delFile.list();
            
            if (filesInDir.length > 0)
            {
                deletePromptMsg = getResource("confirm.forcedirdel", "The folder is not empty. Delete it anyway?");      
            }
            else
            {
                deletePromptMsg = getResource("confirm.removeDir", "Are you sure you want to delete this directory?");
            }
        }
        else
        {
            deletePromptMsg = getResource("alert.delDirError", "The folder cannot be deleted!");
            XmlUtil.setChildText(resultElement, "error", "missingDir");
        }
        
        // XmlUtil.setChildText(resultElement, "folderName", CommonUtils.shortName(delFile.getName(), 36));
        XmlUtil.setChildText(resultElement, "folderShortPath", CommonUtils.shortName(getHeadlinePath(delPath), 36));
        XmlUtil.setChildText(resultElement, "folderPath", UTF8URLEncoder.encode(delPath));
        XmlUtil.setChildText(resultElement, "deletePromptMsg", deletePromptMsg);
        
        doc.appendChild(resultElement);

        addMsgResource("checkbox.confirmdel", getResource("checkbox.confirmdel", "Confirm delete"));
        addMsgResource("button.delete", getResource("button.delete", "Delete"));
        addMsgResource("button.cancel", getResource("button.cancel", "Cancel"));
        addMsgResource("button.return", getResource("button.return", "Return"));
		
		this.processResponse();
	}
}
