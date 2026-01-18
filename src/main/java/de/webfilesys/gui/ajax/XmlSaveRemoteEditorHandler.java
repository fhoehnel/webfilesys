package de.webfilesys.gui.ajax;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Element;

import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XmlSaveRemoteEditorHandler extends XmlRequestHandlerBase {
	public XmlSaveRemoteEditorHandler(
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

		String filePath = getParameter("filePath");

		if (!checkAccess(filePath)) {
			return;
		}

		boolean writeError = false;

        File destFile = new File(filePath);
        
        if (!destFile.canWrite()) {
            LogManager.getLogger(getClass()).warn("failed to save editor content - file is not writable: " + filePath);
        	writeError = true;
        } else {
            String text = req.getParameter("text");

			String tmpFileName = filePath + "_tmp$edit";

            String fileEncoding = guessFileEncoding(filePath);

			PrintWriter fout = null;

			try {
                FileOutputStream fos = new FileOutputStream(tmpFileName);

                if (fileEncoding == null) {
			        // use OS default encoding
	                fout = new PrintWriter(fos);
			    } else {
	                LogManager.getLogger(getClass()).debug("saving editor file " + filePath + " with character encoding " + fileEncoding);
			        
			        if (fileEncoding.equals("UTF-8-BOM")) {
			            // write UTF-8 BOM
                        fos.write(0xef);
                        fos.write(0xbb);
                        fos.write(0xbf);
                        fileEncoding = "UTF-8";
			        }
			        
			        fout = new PrintWriter(new OutputStreamWriter(fos, fileEncoding));
			    }

                if (File.separatorChar == '/') {
                    boolean endsWithLineFeed = text.charAt(text.length() - 1) == '\n';
                    
                    BufferedReader textReader = new BufferedReader(new StringReader(text));
                    
                    String line = null;
                    boolean firstLine = true;
                    
                    while ((line = textReader.readLine()) != null) {
                        if (firstLine) {
                            firstLine = false;
                        } else {
                            fout.print('\n');
                        }
                        
                        fout.print(line);    
                    }
                    
                    if (endsWithLineFeed) {
                        fout.print('\n');
                    }
                } else {
                    fout.print(text);
                }

				fout.flush();

				fout.close();

				if (!copyFile(tmpFileName, filePath)) {
					String logMsg = "cannot copy temporary file to edited file " + filePath;
					LogManager.getLogger(getClass()).error(logMsg);
					writeError = true;
				} else {
					File tmpFile = new File(tmpFileName);
				
					if (!tmpFile.delete()) {
						LogManager.getLogger(getClass()).warn("cannot delete temporary file " + tmpFile);
					}
				}
			} catch (Exception ex) {
				String logMsg="cannot save changed content of edited file " + filePath + ": " + ex;
				LogManager.getLogger(getClass()).error(logMsg);
				writeError = true;
                
				if (fout != null) {
					try {
	                    fout.close();
					} catch (Exception ex2) {
					}
                }
			}
        }
		
		String resultMsg;

		Element resultElement = doc.createElement("result");

		if (writeError) {
			resultMsg = "failed to save editor content";
			XmlUtil.setChildText(resultElement, "message", resultMsg);
			XmlUtil.setChildText(resultElement, "success", "false");
		} else {
			resultMsg = "editor content saved successfully";
			XmlUtil.setChildText(resultElement, "message", resultMsg);
			XmlUtil.setChildText(resultElement, "success", "true");
			
    		XmlUtil.setChildText(resultElement, "mobile", Boolean.toString(session.getAttribute("mobile") != null));
		}
			
		doc.appendChild(resultElement);
		
		processResponse();
	}
}
