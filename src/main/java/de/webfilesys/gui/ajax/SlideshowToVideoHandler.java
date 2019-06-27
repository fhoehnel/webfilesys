package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import de.webfilesys.Constants;
import de.webfilesys.graphics.ScaledImage;
import de.webfilesys.graphics.SlideshowToVideoThread;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class SlideshowToVideoHandler extends XmlRequestHandlerBase {
	
	private static Logger LOG = Logger.getLogger(SlideshowToVideoHandler.class);
	
	public static final String LIST_PREFIX = "list-";
	
	private static final int LIST_PREFIX_LENGTH = LIST_PREFIX.length();
	
	private static final String TARGET_VIDEO_SUBDIR = "_slideVideo";
	
	private static final String FFMPEG_INPUT_LIST_FILE_NAME = "ffmpegInputFileList.txt";

	private static final int ERROR_CODE_DIMENSION_MISSMATCH = 1;
	private static final int ERROR_CODE_PROCESSING_FAILED = 2;
	
	private static final int MAX_VIDEO_RESOLUTION = 1920;
	
	public SlideshowToVideoHandler(HttpServletRequest req, HttpServletResponse resp, HttpSession session,
			PrintWriter output, String uid) {
		super(req, resp, session, output, uid);
	}

	protected void process() {
		if (!checkWriteAccess()) {
			return;
		}
		
		String currentPath = getCwd();
		
        String targetPath = currentPath + File.separator + TARGET_VIDEO_SUBDIR;

		ArrayList<String> selectedFiles = new ArrayList<String>();

        Enumeration allKeys = req.getParameterNames();
		
		while (allKeys.hasMoreElements()) {
			String paramKey = (String) allKeys.nextElement();

            if (paramKey.startsWith(LIST_PREFIX)) {
				selectedFiles.add(paramKey.substring(LIST_PREFIX_LENGTH)); 
            }
		}
		
		int videoResolutionWidth = 640;
		int videoResolutionHeight = 480;
		
		File ffmpegInputListFile = new File(currentPath, FFMPEG_INPUT_LIST_FILE_NAME);
		
		PrintWriter ffmpegInputFileListFile = null;
		
		int errorCode = 0;
		
		try {
	        ffmpegInputFileListFile = new PrintWriter(new OutputStreamWriter(new FileOutputStream(ffmpegInputListFile), "UTF-8"));
	        
	        int commonImgWidth = 0;
	        int commonImgHeight = 0;
	        
	        for (int i = 0; (errorCode == 0) && (i < selectedFiles.size()); i++) {
	            String filePath = null;

	            if (currentPath.endsWith(File.separator)) {
	                filePath = currentPath + selectedFiles.get(i);
	            } else {
	                filePath = currentPath + File.separator + selectedFiles.get(i);
	            }
	            
	            ScaledImage scaledImg = new ScaledImage(filePath, MAX_VIDEO_RESOLUTION, MAX_VIDEO_RESOLUTION);
	            
	            if (commonImgWidth == 0) {
	            	commonImgWidth = scaledImg.getRealWidth();
	            	commonImgHeight = scaledImg.getRealHeight();
	            	videoResolutionWidth = scaledImg.getScaledWidth();
	            	videoResolutionHeight = scaledImg.getScaledHeight();
	            } else {
	            	if (scaledImg.getRealWidth() != commonImgWidth) {
	            		errorCode = ERROR_CODE_DIMENSION_MISSMATCH;
	            	}
	            	if (scaledImg.getRealHeight() != commonImgHeight) {
	            		errorCode = ERROR_CODE_DIMENSION_MISSMATCH;
	            	}
	            }

	            if (errorCode == 0) {
		            if (LOG.isDebugEnabled()) {
			            LOG.debug("picture file to add to video: " + filePath);
		            }
		        
		            ffmpegInputFileListFile.println("file " + '\'' +  filePath.replace('\\',  '/') + '\'');
		            ffmpegInputFileListFile.println("duration 5");
		            
		            if (i == selectedFiles.size() - 1) {
			            ffmpegInputFileListFile.println("file " + '\'' +  filePath.replace('\\',  '/') + '\'');
		            }
	            }
	        }
		} catch (IOException ioex) {
		    LOG.error("failed to write ffmpeg input list file for slideshow video", ioex);
    		errorCode = ERROR_CODE_PROCESSING_FAILED;
		} finally {
		    if (ffmpegInputFileListFile != null) {
		        try {
		            ffmpegInputFileListFile.close();
		        } catch (Exception ex) {
		        }
		    }
		}

		if (errorCode == 0) {
			(new SlideshowToVideoThread(ffmpegInputListFile.getAbsolutePath(), targetPath, videoResolutionWidth, videoResolutionHeight)).start();			
		}
		
		Element resultElement = doc.createElement("result");

		if (errorCode != 0) {
			XmlUtil.setChildText(resultElement, "errorCode", Integer.toString(errorCode));
		} else {
			XmlUtil.setChildText(resultElement, "success", Boolean.toString(true));
			
			XmlUtil.setChildText(resultElement, "targetFolder", TARGET_VIDEO_SUBDIR);

			XmlUtil.setChildText(resultElement, "targetPath", targetPath);
			
			session.setAttribute("viewMode", new Integer(Constants.VIEW_MODE_VIDEO));
		}
		
		doc.appendChild(resultElement);

		processResponse();
	}

}
