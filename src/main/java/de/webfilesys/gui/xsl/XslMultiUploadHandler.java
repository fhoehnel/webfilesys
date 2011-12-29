package de.webfilesys.gui.xsl;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslMultiUploadHandler extends XslRequestHandlerBase
{
	public XslMultiUploadHandler(
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

		String currentPath = getParameter("actpath");
		
		if (currentPath == null)
		{
		    currentPath = getCwd();
		} 
		else 
		{
		    session.setAttribute("cwd", currentPath);
		}

		if (!checkAccess(currentPath))
		{
			return;
		}

		String relativePath = this.getHeadlinePath(currentPath);

	    String shortPath = CommonUtils.shortName(relativePath, 60);

		Element uploadElement = doc.createElement("upload");
			
		doc.appendChild(uploadElement);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/multiUpload.xsl\"");

		doc.insertBefore(xslRef, uploadElement);

		XmlUtil.setChildText(uploadElement, "css", userMgr.getCSS(uid), false);

		XmlUtil.setChildText(uploadElement, "currentPath", currentPath, false);
		XmlUtil.setChildText(uploadElement, "shortPath", shortPath, false);
		
		addMsgResource("headline.multiUpload", getResource("headline.multiUpload", "Extended Upload"));
        addMsgResource("upload.selected.pictures", getResource("upload.selected.pictures", "pictures selected for upload"));
        addMsgResource("upload.dropZone", getResource("upload.dropZone", "Drag and drop Image files for upload here!"));
        addMsgResource("upload.lastSent", getResource("upload.lastSent", "last uploaded file"));
        addMsgResource("upload.selectedFiles", getResource("upload.selectedFiles", "files selected for upload"));
        addMsgResource("upload.selectFilePrompt", getResource("upload.selectFilePrompt", "select file(s)"));

        addMsgResource("label.destdir", getResource("label.destdir","destination directory"));
		addMsgResource("alert.nofileselected", getResource("alert.nofileselected","You did not select a file for upload"));

        addMsgResource("button.startUpload", getResource("button.startUpload","Start Upload"));
        addMsgResource("upload.button.done", getResource("upload.button.done","Done"));

		addMsgResource("button.cancel", getResource("button.cancel","Cancel"));
		addMsgResource("label.uploadStatus", getResource("label.uploadStatus","Upload Status"));
        addMsgResource("label.of", getResource("label.of","of"));
        addMsgResource("upload.file.too.large", getResource("upload.file.too.large","The file is too large for uploading with the extended upload function. Use the basic upload dialog for really big files!"));
        addMsgResource("upload.file.exists", getResource("upload.file.exists","a file with this name already exists - overwrite?"));
		
		this.processResponse("multiUpload.xsl", true);
    }
}