package de.webfilesys.gui.ajax;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import de.webfilesys.FileLink;
import de.webfilesys.MetaInfManager;
import de.webfilesys.WebFileSys;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class GetVideoDimensionsHandler extends XmlRequestHandlerBase {
	
    private static final Logger LOG = Logger.getLogger(GetVideoDimensionsHandler.class);
	
	public GetVideoDimensionsHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid) {
        super(req, resp, session, output, uid);
	}
	
	protected void process() {
        String fileName = getParameter("fileName");

        if (CommonUtils.isEmpty(fileName)) {
        	LOG.warn("parameter fileName missing");
        	return;
        }
        
        String path = getCwd();
        
        File videoFile = new File(path, fileName);

        String isLink = getParameter("link");
        if (!CommonUtils.isEmpty(isLink)) {
        	FileLink fileLink = MetaInfManager.getInstance().getLink(path, fileName);
        	if (fileLink != null) {
        		if (!accessAllowed(fileLink.getDestPath())) {
        			LOG.error("user " + uid + " tried to access a linked file outside his docuemnt root: " + fileLink.getDestPath());
        			return;
        		}
        		videoFile = new File(fileLink.getDestPath());
        	} else {
    			LOG.error("link does not exist: " + path + " " + fileName);
    			return;
        	}
        } else {
            videoFile = new File(path, fileName);
        }
        
        if ((!videoFile.exists()) || (!videoFile.isFile()) || (!videoFile.canRead())) {
        	LOG.warn("not a readable file: " + path + " " + fileName);
        	return;
        }

        Element resultElement = doc.createElement("result");
        
        String ffprobeExePath = WebFileSys.getInstance().getFfprobeExePath();
        
        if (!CommonUtils.isEmpty(ffprobeExePath)) {
    		try {

            	String progNameAndParams = ffprobeExePath +  " -v error -of flat=s=_ -select_streams v:0 -show_entries stream=height,width " + videoFile.getAbsolutePath();

                if (Logger.getLogger(getClass()).isDebugEnabled()) {
                    Logger.getLogger(getClass()).debug("ffmpeg call with params: " + progNameAndParams);
                }
            	
    			Process ffprobeProcess = Runtime.getRuntime().exec(progNameAndParams);
    			
    	        DataInputStream ffprobeOut = new DataInputStream(ffprobeProcess.getInputStream());
    	        
    	        String outLine = null;
    	        
    	        while ((outLine = ffprobeOut.readLine()) != null) {
    	        	// TODO: extract video dimensions from output
    	        	if (Logger.getLogger(getClass()).isDebugEnabled()) {
    	                Logger.getLogger(getClass()).debug("ffprobe output: " + outLine);
    	        	}
    	        }
    			
    			int ffprobeResult = ffprobeProcess.waitFor();
    			
    			if (ffprobeResult == 0) {
    	        	// TODO: set real video dimensions

    				XmlUtil.setChildText(resultElement, "xpix", Integer.toString(99));
        	        XmlUtil.setChildText(resultElement, "ypix", Integer.toString(66));
        	        XmlUtil.setChildText(resultElement, "videoType", "MP4");
    			} else {
    				Logger.getLogger(getClass()).warn("ffprobe returned error " + ffprobeResult);
    			}
    		} catch (IOException ioex) {
    			Logger.getLogger(getClass()).error("failed to get video dimensions for video " + videoFile, ioex);
    		} catch (InterruptedException iex) {
    			Logger.getLogger(getClass()).error("failed to get video dimensions for video " + videoFile, iex);
    		}
        }        
        	
        doc.appendChild(resultElement);
		
		processResponse();
	}
}
