package de.webfilesys.gui.user;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.MetaInfManager;
import de.webfilesys.WebFileSys;
import de.webfilesys.graphics.VideoFrameExtractor;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.MimeTypeMap;

/**
 * @author Frank Hoehnel
 */
public class VideoFramePreviewHandler extends UserRequestHandler {
	private static final int PREVIEW_PIC_SIZE = 160;
	
	protected HttpServletResponse resp = null;
	
	public VideoFramePreviewHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid) {
        super(req, resp, session, output, uid);
        
        this.resp = resp;
	}

	protected void process() {
		String currentPath = getCwd();
		
		if (CommonUtils.isEmpty(currentPath)) {
			return;
		}
		
		String videoFileName = getParameter("videoFile");
		
		String videoFilePath = getCwd();
		if (videoFilePath.endsWith(File.separator)) {
			videoFilePath = videoFilePath + videoFileName;
		} else {
			videoFilePath = videoFilePath + File.separator + videoFileName;
		}

        String videoWidthParam = getParameter("videoWidth");
        String videoHeightParam = getParameter("videoHeight");

        int videoWidth = 0;
        int videoHeight = 0;
        try {
        	videoWidth = Integer.parseInt(videoWidthParam);
        	videoHeight = Integer.parseInt(videoHeightParam);
        } catch (NumberFormatException numEx) {
        }
		
        String hourParam = getParameter("hour");
        String minParam = getParameter("min");
        String secParam = getParameter("sec");
		
		String extractTime = "00:00:00.00";

        DecimalFormat numFormat = new DecimalFormat("00");
        
        if ((!CommonUtils.isEmpty(hourParam)) && 
            (!CommonUtils.isEmpty(minParam)) &&    
            (!CommonUtils.isEmpty(secParam))) {
            try {
                int hour = Integer.parseInt(hourParam);
                int min = Integer.parseInt(minParam);
                int sec = Integer.parseInt(secParam);

                extractTime = numFormat.format(hour) + ":" + numFormat.format(min) + ":" + numFormat.format(sec) + ".00";
            } catch (Exception ex) {
                Logger.getLogger(getClass()).warn("invalid video extract time value", ex);
                return;
            }
        }
        
		String extractedFramePath = VideoFrameExtractor.getFrameTargetPath(videoFilePath);
		
		File existingFrameFile = new File(extractedFramePath);
		if (existingFrameFile.exists()) {
			existingFrameFile.delete();
		}

		VideoFrameExtractor frameExtractor = new VideoFrameExtractor(videoFilePath, extractTime, videoWidth, videoHeight, Integer.toString(PREVIEW_PIC_SIZE));

		frameExtractor.start();
		
		serveExtractedFileWhenReady(extractedFramePath);
	}
	
	private void serveExtractedFileWhenReady(String extractedFramePath) {
		int loopCounter = 0;
		File extractedFrameFile = new File(extractedFramePath);
        do {
    		try {
    			Thread.sleep(200);
    		} catch (InterruptedException iex) {
            	Logger.getLogger(getClass()).warn("failed to wait for video frame extraction");
            	loopCounter = Integer.MAX_VALUE;
    		}
    		loopCounter++;
        } while ((loopCounter < 100) && (!extractedFrameFile.exists()));
		
        if (extractedFrameFile.exists()) {
            serveImageFromFile(extractedFramePath, false);
            if (!extractedFrameFile.delete()) {
            	Logger.getLogger(getClass()).warn("failed to delete preview video frame file " + extractedFramePath);
            }
        }
	}
	
	private void serveImageFromFile(String imgPath, boolean isThumbnail) {
		
        File fileToSend = new File(imgPath);
        
        if (fileToSend.exists() && fileToSend.isFile() && (fileToSend.canRead())) {
        	
    		resp.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate, no-store");
    		resp.setDateHeader("expires", 0l); 
    		
    		String mimeType = MimeTypeMap.getInstance().getMimeType(imgPath);
    		
    		resp.setContentType(mimeType);
        	
        	long fileSize = fileToSend.length();
        	
        	resp.setContentLength((int) fileSize);

        	byte buffer[] = null;
        	
            if (fileSize < 16192) {
                buffer = new byte[16192];
            } else {
                buffer = new byte[65536];
            }
        	
        	FileInputStream fileInput = null;

        	try {
        		OutputStream byteOut = resp.getOutputStream();

        		fileInput = new FileInputStream(fileToSend);
        		
        		int bytesRead = 0;
        		long bytesWritten = 0;
        		
                while ((bytesRead = fileInput.read(buffer)) >= 0) {
                    byteOut.write(buffer, 0, bytesRead);
                    bytesWritten += bytesRead;
                }

                if (bytesWritten != fileSize) {
                    Logger.getLogger(getClass()).warn(
                        "only " + bytesWritten + " bytes of " + fileSize + " have been written to output");
                } 

                byteOut.flush();
                
                buffer = null;

                if (!isThumbnail) {
            		if (WebFileSys.getInstance().isDownloadStatistics()) {
            			MetaInfManager.getInstance().incrementDownloads(imgPath);
            		}
                }
        	} catch (IOException ioEx) {
            	Logger.getLogger(getClass()).warn(ioEx);
            } finally {
        		if (fileInput != null) {
        		    try {
        	            fileInput.close();
        		    } catch (Exception ex) {
        		    }
        		}
        	}
        } else {
        	Logger.getLogger(getClass()).error(imgPath + " is not a readable file");
        }
	}
	
}
