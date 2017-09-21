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

import de.webfilesys.graphics.VideoInfo;
import de.webfilesys.graphics.VideoInfoExtractor;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class MultiVideoConcatHandler extends XmlRequestHandlerBase {
	
	private static Logger LOG = Logger.getLogger(MultiVideoConcatHandler.class);
	
	public static final String LIST_PREFIX = "list-";
	
	private static final int LIST_PREFIX_LENGTH = LIST_PREFIX.length();
	
	private static final String FFMPEG_INPUT_LIST_FILE_NAME = "ffmpegInputFileList.txt";
	
	boolean clientIsLocal = false;

	public MultiVideoConcatHandler(HttpServletRequest req, HttpServletResponse resp, HttpSession session,
			PrintWriter output, String uid, boolean clientIsLocal) {
		super(req, resp, session, output, uid);

		this.clientIsLocal = clientIsLocal;
	}

	protected void process() {
		if (!checkWriteAccess()) {
			return;
		}
		
		String currentPath = getCwd();

		ArrayList<String> selectedFiles = new ArrayList<String>();

        Enumeration allKeys = req.getParameterNames();
		
		while (allKeys.hasMoreElements()) {
			String paramKey =(String) allKeys.nextElement();

            if (paramKey.startsWith(LIST_PREFIX)) {
				selectedFiles.add(paramKey.substring(LIST_PREFIX_LENGTH)); 
            }
		}
		
		File ffmpegInputFileListFilePath = new File(currentPath, FFMPEG_INPUT_LIST_FILE_NAME);
		
		PrintWriter ffmpegInputFileListFile = null;
		
		try {
	        ffmpegInputFileListFile = new PrintWriter(new OutputStreamWriter(new FileOutputStream(ffmpegInputFileListFilePath), "UTF-8"));
	        
	        VideoInfoExtractor videoInfoExtractor = new VideoInfoExtractor();
	        
	        for (int i = 0; i < selectedFiles.size(); i++) {
	            String filePath = null;

	            if (currentPath.endsWith(File.separator)) {
	                filePath = currentPath + selectedFiles.get(i);
	            } else {
	                filePath = currentPath + File.separator + selectedFiles.get(i);
	            }
	            
	            VideoInfo videoInfo = videoInfoExtractor.getVideoInfo(filePath);
	            
	            LOG.debug("video file to concatenate: " + filePath + ": codec=" + videoInfo.getCodec() + " width: " + videoInfo.getWidth() + " height: " + videoInfo.getHeight() + " fps=" + videoInfo.getFrameRate() + " duration=" + videoInfo.getDuration());
	        
	            ffmpegInputFileListFile.println("file " + '\'' +  filePath + '\'');
	        }
		} catch (IOException ioex) {
		    LOG.error("failed to write ffmpeg input list file for video concatenation", ioex);
		} finally {
		    if (ffmpegInputFileListFile != null) {
		        try {
		            ffmpegInputFileListFile.close();
		        } catch (Exception ex) {
		        }
		    }
		}

		Element resultElement = doc.createElement("result");

		XmlUtil.setChildText(resultElement, "success", Boolean.toString(true));
		
		doc.appendChild(resultElement);

		processResponse();
	}

}
