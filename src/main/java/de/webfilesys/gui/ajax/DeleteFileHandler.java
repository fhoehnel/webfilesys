package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import de.webfilesys.Constants;
import de.webfilesys.MetaInfManager;
import de.webfilesys.WebFileSys;
import de.webfilesys.graphics.ThumbnailThread;
import de.webfilesys.util.XmlUtil;

/**
 * Delete a picture file.
 */
public class DeleteFileHandler extends XmlRequestHandlerBase {
    public DeleteFileHandler(HttpServletRequest req, HttpServletResponse resp, HttpSession session,
            PrintWriter output, String uid) {
        super(req, resp, session, output, uid);
    }

    protected void process() {
        if (!checkWriteAccess()) {
            return;
        }

        String filePath = getParameter("filePath");

        if (!accessAllowed(filePath)) {
            Logger.getLogger(getClass()).warn("user " + uid + " tried to delete file outside of it's document root: " + filePath);
            return;
        }

        if (File.separatorChar == '\\') {
            filePath = filePath.replace('/', '\\');
        }

        String deleteWriteProtected = getParameter("deleteWriteProtected");
        
        boolean success = false;

        File imgFile = new File(filePath);
        
        String deletedFile = null;

        if (imgFile.exists() && imgFile.isFile() && 
            (imgFile.canWrite() || (deleteWriteProtected != null))) {
            deletedFile = imgFile.getName();
            
            MetaInfManager metaInfMgr = MetaInfManager.getInstance();

            if (WebFileSys.getInstance().isReverseFileLinkingEnabled()) {
                metaInfMgr.updateLinksAfterMove(filePath, null, uid);
            }

            metaInfMgr.removeMetaInf(filePath);

            String thumbnailPath = ThumbnailThread.getThumbnailPath(filePath);

            File thumbnailFile = new File(thumbnailPath);

            if (thumbnailFile.exists()) {
                if (!thumbnailFile.delete()) {
                    Logger.getLogger(getClass()).warn("failed to remove thumbnail file " + thumbnailPath);
                }
            }

            if (imgFile.delete()) {
                success = true;
            } else {
                Logger.getLogger(getClass()).warn("failed to delete file " + filePath);
            }
        }

        Element resultElement = doc.createElement("result");

        XmlUtil.setChildText(resultElement, "success", Boolean.toString(success));
        
        if (success) {
            XmlUtil.setChildText(resultElement, "deletedFile", deletedFile);
        }

        Integer viewMode = (Integer) session.getAttribute("viewMode");
        
        if (viewMode != null) {
            XmlUtil.setChildText(resultElement, "viewMode", Integer.toString(viewMode.intValue()));
        }
        
        doc.appendChild(resultElement);

        processResponse();
    }

}
