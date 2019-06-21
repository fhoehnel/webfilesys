package de.webfilesys.gui.ajax;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

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
		if (!clientIsLocal) {
			Logger.getLogger(getClass()).warn("remote user tried to start local video player");
			return;
		}
		
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
        String videoPlayerExePath = WebFileSys.getInstance().getVideoPlayerExePath();
        
        if (CommonUtils.isEmpty(videoPlayerExePath)) {
            return -1;
        }

        try {
            ArrayList<String> progNameAndParams = new ArrayList<String>();
            progNameAndParams.add(videoPlayerExePath);
        	
        	String addParams = WebFileSys.getInstance().getVideoPlayerAddParams();
            if (addParams != null) {
            	String[] params = addParams.split(" ");
            	for (String param : params) {
                    progNameAndParams.add(param);
            	}
            }
            
            progNameAndParams.add(videoFilePath);

            if (Logger.getLogger(getClass()).isDebugEnabled()) {
            	StringBuilder buff = new StringBuilder();
                for (String cmdToken : progNameAndParams) {
                	buff.append(cmdToken);
                	buff.append(' ');
                }
                Logger.getLogger(getClass()).debug("ffplay call with params: " + buff.toString());
            }
        	
			Process ffplayProcess = Runtime.getRuntime().exec(progNameAndParams.toArray(new String[0]));
			
	        DataInputStream ffplayOut = new DataInputStream(ffplayProcess.getErrorStream());
	        
	        String outLine = null;
	        
	        while ((outLine = ffplayOut.readLine()) != null) {
                if (Logger.getLogger(getClass()).isDebugEnabled()) {
                    Logger.getLogger(getClass()).debug("video player output: " + outLine);
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
