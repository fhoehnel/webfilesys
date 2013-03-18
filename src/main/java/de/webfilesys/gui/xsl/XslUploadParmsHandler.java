package de.webfilesys.gui.xsl;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.Constants;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslUploadParmsHandler extends XslRequestHandlerBase
{
	public XslUploadParmsHandler(
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

		session.setAttribute(Constants.UPLOAD_SIZE, new Integer(0));

		String relativePath = this.getHeadlinePath(currentPath);

	    String shortPath = CommonUtils.shortName(relativePath, 60);

		Element uploadElement = doc.createElement("upload");
			
		doc.appendChild(uploadElement);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/uploadParms.xsl\"");

		doc.insertBefore(xslRef, uploadElement);

		XmlUtil.setChildText(uploadElement, "css", userMgr.getCSS(uid), false);

		XmlUtil.setChildText(uploadElement, "currentPath", currentPath, false);
		XmlUtil.setChildText(uploadElement, "shortPath", shortPath, false);
		
		addMsgResource("label.uploadfile", getResource("label.uploadfile","upload file"));
		addMsgResource("hint.upload.multizip", getResource("hint.upload.multizip","To upload multiple files or a whole folder tree at once, create a ZIP archive on your computer and upload it."));
		addMsgResource("label.destdir", getResource("label.destdir","destination directory"));
		addMsgResource("alert.nofileselected", getResource("alert.nofileselected","You did not select a file for upload"));
        /*
		addMsgResource("label.xfermode", getResource("label.xfermode","transfer mode"));
		addMsgResource("label.xferbin", getResource("label.xferbin","binary"));
		addMsgResource("label.xfertext", getResource("label.xfertext","text"));
		*/
		addMsgResource("label.upload.localFile", getResource("label.upload.localFile","file to be uploaded"));
		addMsgResource("label.upload.serverFileName", getResource("label.upload.serverFileName","file name on server"));
		addMsgResource("label.upload.description", getResource("label.upload.description","file description"));
		addMsgResource("label.upload.unzip", getResource("label.upload.unzip","unzip after upload"));
        addMsgResource("link.multiUpload", getResource("link.multiUpload","comfort upload"));

		addMsgResource("button.startUpload", getResource("button.startUpload","Start Upload"));
		addMsgResource("button.cancel", getResource("button.cancel","Cancel"));
		addMsgResource("label.uploadStatus", getResource("label.uploadStatus","Upload Status"));
		addMsgResource("label.of", getResource("label.of","of"));
        addMsgResource("upload.file.exists", getResource("upload.file.exists","a file with this name already exists - overwrite?"));
        addMsgResource("alert.illegalCharInFilename", getResource("alert.illegalCharInFilename","The file name contains illegal characters!"));
        
		this.processResponse("uploadParms.xsl", true);
    }
}