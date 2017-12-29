package de.webfilesys.gui.ajax;

import java.io.DataInputStream;
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

import de.webfilesys.WebFileSys;
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
	
	private static final int ERROR_CODE_FRAMERATE_MISSMATCH = 1;
	private static final int ERROR_CODE_CODEC_MISSMATCH = 2;
	private static final int ERROR_CODE_RESOLUTION_MISSMATCH = 3;
	private static final int ERROR_CODE_CONVERSION_FAILED = 4;
	
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
		
		boolean videoParameterMissmatch = false;
		
		int errorCode = 0;
		
		String codec = null;
		int frameRate = 0;
		int videoWidth = 0;
		int videoHeight = 0;
		
		File ffmpegFileListFile = new File(currentPath, FFMPEG_INPUT_LIST_FILE_NAME);
		
		PrintWriter ffmpegInputFileListFile = null;
		
		try {
	        ffmpegInputFileListFile = new PrintWriter(new OutputStreamWriter(new FileOutputStream(ffmpegFileListFile), "UTF-8"));
	        
	        VideoInfoExtractor videoInfoExtractor = new VideoInfoExtractor();
	        
	        for (int i = 0; i < selectedFiles.size(); i++) {
	            String filePath = null;

	            if (currentPath.endsWith(File.separator)) {
	                filePath = currentPath + selectedFiles.get(i);
	            } else {
	                filePath = currentPath + File.separator + selectedFiles.get(i);
	            }
	            
	            VideoInfo videoInfo = videoInfoExtractor.getVideoInfo(filePath);
	            
	            if (codec == null) {
	            	codec = videoInfo.getCodec();
	            } else {
	            	if (!videoInfo.getCodec().equals(codec)) {
	            		videoParameterMissmatch = true;
	            		errorCode = ERROR_CODE_CODEC_MISSMATCH;
	            	}
	            }
	            
	            if (frameRate == 0) {
	            	frameRate = videoInfo.getFrameRate();
	            } else {
	            	if (videoInfo.getFrameRate() != frameRate) {
	            		videoParameterMissmatch = true;
	            		errorCode = ERROR_CODE_FRAMERATE_MISSMATCH;
	            	}
	            }
	            
	            if (videoWidth == 0) {
	            	videoWidth = videoInfo.getWidth();
	            } else {
	            	if (videoInfo.getWidth() != videoWidth) {
	            		videoParameterMissmatch = true;
	            		errorCode = ERROR_CODE_RESOLUTION_MISSMATCH;
	            	}
	            }

	            if (videoHeight == 0) {
	            	videoHeight = videoInfo.getHeight();
	            } else {
	            	if (videoInfo.getHeight() != videoHeight) {
	            		videoParameterMissmatch = true;
	            		errorCode = ERROR_CODE_RESOLUTION_MISSMATCH;
	            	}
	            }
	            
	            LOG.debug("video file to concatenate: " + filePath + ": codec=" + videoInfo.getCodec() + " width: " + videoInfo.getWidth() + " height: " + videoInfo.getHeight() + " fps=" + videoInfo.getFrameRate() + " duration=" + videoInfo.getDuration());
	        
	            ffmpegInputFileListFile.println("file " + '\'' +  filePath + '\'');
	        }
		} catch (IOException ioex) {
		    LOG.error("failed to write ffmpeg input list file for video concatenation", ioex);
		} finally {
		    if (ffmpegInputFileListFile != null) {
		        try {
		            ffmpegInputFileListFile.close();
		            
			        if (videoParameterMissmatch) {
			        	File fileListFile = new File("ffmpegInputFileListPath");
			        	if (fileListFile.exists()) {
			        		fileListFile.delete();
			        	}
			        }
		        } catch (Exception ex) {
		        }
		    }
		}

		if (!videoParameterMissmatch) {
            String targetPath = currentPath + File.separator + "_converted";
        	
            File targetDirFile = new File(targetPath);
            if (!targetDirFile.exists()) {
                if (!targetDirFile.mkdir()) {
                    Logger.getLogger(getClass()).error("failed to create target folder for video conversion: " + targetPath);
                    return;
                }
            }
            
            String firstFileName = selectedFiles.get(0);
            
            String fileNameOnly = firstFileName.substring(0,  firstFileName.lastIndexOf('.'));
            String ext = firstFileName.substring(firstFileName.lastIndexOf('.') + 1);
            String targetFileName = fileNameOnly + "_concat." + ext;
            
            String targetFilePath = targetPath + File.separator + targetFileName;
			
	        String ffmpegExePath = WebFileSys.getInstance().getFfmpegExePath();
			
        	String progNameAndParams = ffmpegExePath + " -f concat -safe 0 -i " + ffmpegFileListFile.getAbsolutePath() + " -c copy " + targetFilePath;

            if (Logger.getLogger(getClass()).isDebugEnabled()) {
                Logger.getLogger(getClass()).debug("ffmpeg call with params: " + progNameAndParams);
            }
        	
			try {
				Process convertProcess = Runtime.getRuntime().exec(progNameAndParams);
				
		        DataInputStream grabProcessOut = new DataInputStream(convertProcess.getErrorStream());
		        
		        String outLine = null;
		        
		        while ((outLine = grabProcessOut.readLine()) != null) {
		        	if (Logger.getLogger(getClass()).isDebugEnabled()) {
		                Logger.getLogger(getClass()).debug("ffmpeg output: " + outLine);
		        	}
		        }
				
				int convertResult = convertProcess.waitFor();
				
				if (convertResult == 0) {
					File resultFile = new File(targetFilePath);
					
					if (!resultFile.exists()) {
	                    Logger.getLogger(getClass()).error("result file from ffmpeg video conversion not found: " + targetFilePath);
					}
				} else {
					Logger.getLogger(getClass()).warn("ffmpeg returned error " + convertResult);
				}
			} catch (IOException ioex) {
				Logger.getLogger(getClass()).error("failed to concatente videos", ioex);
				errorCode = ERROR_CODE_CONVERSION_FAILED;
			} catch (InterruptedException iex) {
				Logger.getLogger(getClass()).error("failed to concatente videos", iex);
				errorCode = ERROR_CODE_CONVERSION_FAILED;
			}
		}
		
		Element resultElement = doc.createElement("result");

		if (errorCode != 0) {
			XmlUtil.setChildText(resultElement, "errorCode", Integer.toString(errorCode));
		} else {
			XmlUtil.setChildText(resultElement, "success", Boolean.toString(true));
		}
		
		doc.appendChild(resultElement);

		processResponse();
	}

}
