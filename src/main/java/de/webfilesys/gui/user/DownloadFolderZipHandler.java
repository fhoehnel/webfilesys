package de.webfilesys.gui.user;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * @author Frank Hoehnel
 */
public class DownloadFolderZipHandler extends UserRequestHandler {

    private static final Logger LOG = LogManager.getLogger(DownloadFolderZipHandler.class);

	public DownloadFolderZipHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid) {
        super(req, resp, session, output, uid);
	}

	protected void process() {
		String path = getParameter("path");
		if (!checkAccess(path)) {
		    return;	
		}
		String errorMsg = null;
        File folderFile = new File(path);
        if (!folderFile.exists() || !folderFile.isDirectory() || !folderFile.canRead()) {
            errorMsg = "folder is not a readable directory: " + path;
        }
        String dirName = null;
        int lastSepIdx = path.lastIndexOf(File.separatorChar);
        if (lastSepIdx < 0) {
            lastSepIdx = path.lastIndexOf('/');
        }
        if ((lastSepIdx < 0) || (lastSepIdx == path.length() - 1)) {
            errorMsg = "invalid path for folder download: " + path;
        } else {
            dirName = path.substring(lastSepIdx + 1);
        }
        if (errorMsg != null) {
        	LOG.warn(errorMsg);
            resp.setStatus(404);
            try {
    			PrintWriter output = new PrintWriter(resp.getWriter());
    			output.println(errorMsg);
    			output.flush();
    			return;
    		} catch (IOException ioEx) {
            	LOG.warn(ioEx);
            }
        }
        resp.setContentType("application/zip");
        resp.setHeader("Content-Disposition", "attachment; filename=" + dirName + ".zip");

        BufferedOutputStream buffOut = null;
        ZipOutputStream zipOut = null;
		try {
			buffOut = new BufferedOutputStream(resp.getOutputStream());
			zipOut =  new ZipOutputStream(buffOut);
			zipFolderTree(path, "", zipOut);
			buffOut.flush();
		} catch (IOException ioEx) {
        	LOG.warn(ioEx);
        } finally {
            try {
                if (zipOut != null) {
                    zipOut.close();
                }
                if (buffOut != null) {
                	buffOut.close();
                }
            } catch (Exception ex) {
            }
        }
	}
	
    private void zipFolderTree(String actPath, String relativePath, ZipOutputStream zipOut) {
        File folderFile = new File(actPath);

        File[] fileList = folderFile.listFiles();
        if ((fileList == null) || (fileList.length == 0)) {
            return;
        }
        byte[] buff = new byte[4096];

        for (File file : fileList) {
            if (file.isDirectory()) {
                zipFolderTree(file.getAbsolutePath(),
                                   relativePath + file.getName() + "/",
                                   zipOut);
            } else {
                String relativeFileName = relativePath + file.getName();
                try {
                    ZipEntry newZipEntry = new ZipEntry(relativeFileName);
                    zipOut.putNextEntry(newZipEntry);
                    try (BufferedInputStream inStream = new BufferedInputStream(new FileInputStream(file))) {
                        int count;
                        while ((count = inStream.read(buff)) >= 0) {
                            zipOut.write(buff, 0, count);
                        }
                    } catch (Exception zioe) {
                        LOG.warn("failed to zip file {}", file.getAbsolutePath(), zioe);
                    }
                } catch (IOException ioex) {
                    LOG.error("failed to zip file {}", file.getAbsolutePath(), ioex);
                }
            }
        }
    }
}
