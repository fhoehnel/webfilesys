package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;

import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 *
 */
public class AjaxDeleteFilePromptHandler extends XmlRequestHandlerBase
{
	public AjaxDeleteFilePromptHandler(
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
        
        String fileName = req.getParameter("param1");

        String delPath = getCwd();

        if (!delPath.endsWith(File.separator))
        {
            delPath = delPath + File.separatorChar;
        }

        delPath = delPath + fileName;
        
        if (!checkAccess(delPath))
        {
            return;
        }

        Element resultElement = doc.createElement("result");
        
        File delFile = new File(delPath);
        
        String deletePromptMsg = null;

        if (delFile.exists())
        {
            if (delFile.canWrite())
            {
                deletePromptMsg = getResource("confirm.delfile", "Are you sure you want to delete this file?");      
            }
            else
            {
                deletePromptMsg = getResource("confirm.forcefiledel", "This file is write-protected. Delete it anyway ?");
            }
        }
        else
        {
            deletePromptMsg = getResource("alert.delFileError", "The file cannot be deleted!");
            XmlUtil.setChildText(resultElement, "error", "missingFile");
        }
        
        // XmlUtil.setChildText(resultElement, "fileName", UTF8URLEncoder.encode(fileName));
        XmlUtil.setChildText(resultElement, "fileName", fileName);
        XmlUtil.setChildText(resultElement, "filePath", CommonUtils.shortName(getHeadlinePath(delPath), 40));
        XmlUtil.setChildText(resultElement, "deletePromptMsg", deletePromptMsg);
        
        doc.appendChild(resultElement);

        addMsgResource("checkbox.confirmdel", getResource("checkbox.confirmdel", "Confirm delete"));
        addMsgResource("button.delete", getResource("button.delete", "Delete"));
        addMsgResource("button.cancel", getResource("button.cancel", "Cancel"));
        addMsgResource("button.return", getResource("button.return", "Return"));
        
		this.processResponse();
	}
}
