package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;

import de.webfilesys.Constants;
import de.webfilesys.FileContainer;
import de.webfilesys.FileLinkSelector;
import de.webfilesys.FileSelectionStatus;
import de.webfilesys.graphics.VideoInfo;
import de.webfilesys.graphics.VideoInfoExtractor;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class VideoDurationSumHandler extends XmlRequestHandlerBase {
	
    private static final Logger LOG = LogManager.getLogger(VideoDurationSumHandler.class);
	
	public VideoDurationSumHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid) {
        super(req, resp, session, output, uid);
	}
	
	protected void process() {
        
		FileLinkSelector fileSelector = new FileLinkSelector(getCwd(), 1, true);

		FileSelectionStatus selectionStatus = fileSelector.selectFiles(Constants.VIDEO_FILE_MASKS, -1, 4096, 0);

		filterLinksOutsideDocRoot(selectionStatus);

		ArrayList<FileContainer> selectedFiles = selectionStatus.getSelectedFiles();
		
		long durationSumSeconds = 0;
		boolean error = false;
		
		for (int i = 0; i < selectedFiles.size(); i++) {
			FileContainer fileCont = (FileContainer) selectedFiles.get(i);
			File videoFile = fileCont.getRealFile();
			
	        VideoInfoExtractor videoInfoExtractor = new VideoInfoExtractor();
            VideoInfo videoInfo = videoInfoExtractor.getVideoInfo(videoFile.getAbsolutePath());

            if (videoInfo.getFfprobeResult() == 0) {
            	durationSumSeconds += videoInfo.getDurationSeconds();
            } else {
            	error = true;
            }
		}

		String formattedDuration = String.format("%02d:%02d:%02d", durationSumSeconds / 3600, durationSumSeconds % 3600 / 60, durationSumSeconds % 60);
		
        Element resultElement = doc.createElement("result");
        
        XmlUtil.setChildText(resultElement, "duration", formattedDuration);
        if (error) {
            XmlUtil.setChildText(resultElement, "error", "true");
        }
        
        doc.appendChild(resultElement);
		
		processResponse();
	}
	
	private void filterLinksOutsideDocRoot(FileSelectionStatus selectionStatus) {
		ArrayList<FileContainer> filteredOutList = new ArrayList<FileContainer>();

		ArrayList<FileContainer> selectedFiles = selectionStatus.getSelectedFiles();

		if (selectedFiles != null) {
			for (int i = 0; i < selectedFiles.size(); i++) {
				FileContainer fileCont = (FileContainer) selectedFiles.get(i);
				if (fileCont.isLink()) {
					if (!accessAllowed(fileCont.getRealFile().getAbsolutePath())) {
						filteredOutList.add(fileCont);
					}
				}
			}

			if (filteredOutList.size() > 0) {
				selectionStatus.setNumberOfFiles(selectionStatus.getNumberOfFiles() - filteredOutList.size());
				selectionStatus.getSelectedFiles().removeAll(filteredOutList);
			}
		}
	}
	
}
