package de.webfilesys.gui.ajax;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import de.webfilesys.WebFileSys;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class VideoLocalPlayerHandler extends XmlRequestHandlerBase {
	
	private boolean clientIsLocal = false;
	
	public VideoLocalPlayerHandler(HttpServletRequest req, HttpServletResponse resp, HttpSession session,
			PrintWriter output, String uid, boolean requestIsLocal) {
		super(req, resp, session, output, uid);
		
		clientIsLocal = requestIsLocal;
	}

	protected void process() {
		/*
		if (!clientIsLocal) {
			Logger.getLogger(getClass()).warn("remote user tried to start local video player");
			return;
		}
		*/
		
        String videoFilePath = getParameter("videoPath");
        
		if (!checkAccess(videoFilePath)) {
			return;
		}

		int rc = playVideoInLocalPlayer(videoFilePath);
		
		Element resultElement = doc.createElement("result");

		XmlUtil.setChildText(resultElement, "success", Boolean.toString(rc == 0));

		doc.appendChild(resultElement);

		this.processResponse();
	}

	private int playVideoInLocalPlayer(String videoFilePath) {
        String ffplayExePath = WebFileSys.getInstance().getFfplayExePath();
        
        if (CommonUtils.isEmpty(ffplayExePath)) {
            return -1;
        }

        try {
        	String progNameAndParams = ffplayExePath + " " + videoFilePath;

        	if (Logger.getLogger(getClass()).isDebugEnabled()) {
        		Logger.getLogger(getClass()).debug("ffplay call with parameters: " + progNameAndParams);
        	}
        	
			Process ffplayProcess = Runtime.getRuntime().exec(progNameAndParams);
			
	        DataInputStream ffplayOut = new DataInputStream(ffplayProcess.getErrorStream());
	        
	        String outLine = null;
	        
	        while ((outLine = ffplayOut.readLine()) != null) {
                if (Logger.getLogger(getClass()).isDebugEnabled()) {
                    Logger.getLogger(getClass()).debug("ffplay output: " + outLine);
                }
	        }
			
			int ffplayResult = ffplayProcess.waitFor();
			return ffplayResult;
		} catch (IOException ioex) {
			Logger.getLogger(getClass()).error("failed to play video " + videoFilePath, ioex);
		} catch (InterruptedException iex) {
			Logger.getLogger(getClass()).error("failed to play video " + videoFilePath, iex);
		}

        return -1;
	}
}
