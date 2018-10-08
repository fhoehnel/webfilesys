package de.webfilesys.gui.xsl;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.WebFileSys;
import de.webfilesys.graphics.ScaledImage;
import de.webfilesys.graphics.VideoThumbnailCreator;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.UTF8URLEncoder;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class ExtractVideoFrameParamHandler extends XslRequestHandlerBase {
    private static final Logger LOG = Logger.getLogger(ExtractVideoFrameParamHandler.class);
	
	public ExtractVideoFrameParamHandler(
			HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid) {
        super(req, resp, session, output, uid);
	}
	  
	protected void process() {
		if (!checkWriteAccess()) {
			return;
		}

		String currentPath = getCwd();

		String videoFileName = getParameter("videoFile");
		
		String videoFilePath = currentPath + File.separator + videoFileName;
		if (currentPath.endsWith(File.separator)) {
			videoFilePath = currentPath + videoFileName;
		}

		Element editParamsElem = doc.createElement("editParams");
			
		doc.appendChild(editParamsElem);

		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/extractVideoFrameParams.xsl\"");

		doc.insertBefore(xslRef, editParamsElem);

		XmlUtil.setChildText(editParamsElem, "css", userMgr.getCSS(uid), false);

		XmlUtil.setChildText(editParamsElem, "language", language, false);
		
		XmlUtil.setChildText(editParamsElem, "videoFileName", videoFileName, false);
		XmlUtil.setChildText(editParamsElem, "shortVideoFileName", CommonUtils.shortName(videoFileName, 40), false);
		XmlUtil.setChildText(editParamsElem, "videoFilePath", videoFilePath, false);
		
        String ffprobeExePath = WebFileSys.getInstance().getFfprobeExePath();
        
        if (!CommonUtils.isEmpty(ffprobeExePath)) {
    		try {

            	String progNameAndParams = ffprobeExePath +  " -v error -of flat=s=_ -select_streams v:0 -show_entries stream=height,width,codec_name,duration,avg_frame_rate -sexagesimal " + videoFilePath;

                if (Logger.getLogger(getClass()).isDebugEnabled()) {
                    Logger.getLogger(getClass()).debug("ffprobe call with params: " + progNameAndParams);
                }
            	
                String videoWidth = "";
                String videoHeight = "";
                String codec = "";
                String duration = "";
                String frameRate = "";
                int durationSeconds = 0;
    			Process ffprobeProcess = Runtime.getRuntime().exec(progNameAndParams);
    			
    	        DataInputStream ffprobeOut = new DataInputStream(ffprobeProcess.getInputStream());
    	        
    	        String outLine = null;
    	        
    	        while ((outLine = ffprobeOut.readLine()) != null) {
                    if (Logger.getLogger(getClass()).isDebugEnabled()) {
                        Logger.getLogger(getClass()).debug("ffprobe output: " + outLine);
                    }

                    if ((videoWidth.length() == 0) && outLine.contains("_width")) {
    	                String[] tokens = outLine.split("=");
    	                videoWidth = tokens[1];
    	            } else if ((videoHeight.length() == 0) && outLine.contains("_height")) {
                        String[] tokens = outLine.split("=");
                        videoHeight = tokens[1];
                    } else if ((codec.length() == 0) && outLine.contains("_codec")) {
                        String[] tokens = outLine.split("=");
                        codec = tokens[1].substring(1, tokens[1].length() - 1);
                    } else if ((duration.length() == 0) && outLine.contains("_duration")) {
                        // streams_stream_0_duration="0:04:36.400000"
                        String[] tokens = outLine.split("=");
                        duration = tokens[1].substring(1, 8);
                        
                        String[] partsOfDuration = duration.split(":");
                        if (partsOfDuration.length == 3) {
                            try {
                                durationSeconds = (Integer.parseInt(partsOfDuration[0]) * 3600) + (Integer.parseInt(partsOfDuration[1]) * 60) + Integer.parseInt(partsOfDuration[2]);
                            } catch (Exception ex) {
                                Logger.getLogger(getClass()).warn("invalid video duration: " + duration);
                            }
                        }
                    } else if ((frameRate.length() == 0) && outLine.contains("_avg_frame_rate")) {
                        String[] tokens = outLine.split("=");
                        String averageFrameRate = tokens[1].substring(1, tokens[1].length() - 1);
                        tokens =  averageFrameRate.split("/");
                        if (tokens.length == 2) {
                            try {
                                int frameRatePart1 = Integer.parseInt(tokens[0]);
                                int frameRatePart2 = Integer.parseInt(tokens[1]);
                                int fps = frameRatePart1 / frameRatePart2;
                                frameRate = Integer.toString(fps);
                            } catch (Exception ex) {
                                Logger.getLogger(getClass()).warn("invalid frame rate for " + videoFilePath + ": " + averageFrameRate);
                            }
                        }
                    }                    
    	        }
    			
    			int ffprobeResult = ffprobeProcess.waitFor();
    			
    			if (ffprobeResult == 0) {
    				Element videoInfoElem = doc.createElement("videoInfo");
    				editParamsElem.appendChild(videoInfoElem);
    				
    				XmlUtil.setChildText(videoInfoElem, "xpix", videoWidth);
        	        XmlUtil.setChildText(videoInfoElem, "ypix", videoHeight);
                    XmlUtil.setChildText(videoInfoElem, "codec", codec);
                    XmlUtil.setChildText(videoInfoElem, "duration", duration);
                    XmlUtil.setChildText(videoInfoElem, "durationSeconds", Integer.toString(durationSeconds));
                    XmlUtil.setChildText(videoInfoElem, "fps", frameRate);
                    
                    try {
                    	int videoWidthInt = Integer.parseInt(videoWidth);
                    	int videoHeightInt = Integer.parseInt(videoHeight);
                        if (videoWidthInt >= videoHeightInt) {
            				XmlUtil.setChildText(videoInfoElem, "sizeMaxDimension", videoWidth);
                        } else {
            				XmlUtil.setChildText(videoInfoElem, "sizeMaxDimension", videoHeight);
                        }
                    } catch (NumberFormatException numEx) {
                    }
                    
                    try {
                        int xResolution = Integer.parseInt(videoWidth);
                        int yResolution = Integer.parseInt(videoHeight);
                        
                        int maxDimension = xResolution;
                        if (yResolution > xResolution) {
                        	maxDimension = yResolution;
                        }
                        
        				Element targetResolutionElem = doc.createElement("targetResolution");
        				editParamsElem.appendChild(targetResolutionElem);
        				
        				addTargetResolutionOption(targetResolutionElem, maxDimension, 1920);
        				addTargetResolutionOption(targetResolutionElem, maxDimension, 1280);
        				addTargetResolutionOption(targetResolutionElem, maxDimension, 1024);
        				addTargetResolutionOption(targetResolutionElem, maxDimension, 800);
        				addTargetResolutionOption(targetResolutionElem, maxDimension, 640);
        				addTargetResolutionOption(targetResolutionElem, maxDimension, 400);
        				addTargetResolutionOption(targetResolutionElem, maxDimension, 200);
        				
                    } catch (Exception ex) {
                    	Logger.getLogger(getClass()).warn("invalid video resolution: " + videoWidth + " x " + videoHeight);
                    }
    			} else {
    				Logger.getLogger(getClass()).warn("ffprobe returned error " + ffprobeResult);
    			}
    			
    			XmlUtil.setChildText(editParamsElem, "videoFileName", videoFileName, false);
    			
    			XmlUtil.setChildText(editParamsElem, "thumbnailSource", "/webfilesys/servlet?command=videoThumb&videoFile=" + UTF8URLEncoder.encode(videoFileName), false);                    
    			
    			try {
    				String videoThumbnailPath = VideoThumbnailCreator.getThumbnailPath(videoFilePath);
    				
    				ScaledImage scaledImage = new ScaledImage(videoThumbnailPath, 100, 100);

        			XmlUtil.setChildText(editParamsElem, "thumbnailWidth", Integer.toString(scaledImage.getRealWidth()), false);                    
        			XmlUtil.setChildText(editParamsElem, "thumbnailHeight", Integer.toString(scaledImage.getRealHeight()), false);                    
    				
    			} catch (IOException ioEx) {
    				Logger.getLogger(getClass()).error(ioEx);
    			}
    		} catch (IOException ioex) {
    			Logger.getLogger(getClass()).error("failed to get video dimensions for video " + videoFilePath, ioex);
    		} catch (InterruptedException iex) {
    			Logger.getLogger(getClass()).error("failed to get video dimensions for video " + videoFilePath, iex);
    		}
        }        
		
		processResponse("extractVideoFrameParams.xsl", true);
    }
	
	private void addTargetResolutionOption(Element targetResolutionElem, int maxDimension, int resolution) {
		if (maxDimension >= resolution) {
			Element resolutionOptionElem = doc.createElement("option");
			XmlUtil.setElementText(resolutionOptionElem, Integer.toString(resolution));
			targetResolutionElem.appendChild(resolutionOptionElem);
		}
	}
}