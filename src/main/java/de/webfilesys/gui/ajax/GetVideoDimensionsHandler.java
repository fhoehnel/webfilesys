package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import de.webfilesys.FileLink;
import de.webfilesys.MetaInfManager;
import de.webfilesys.WebFileSys;
import de.webfilesys.graphics.VideoInfo;
import de.webfilesys.graphics.VideoInfoExtractor;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/*
{
    "streams": [
        {
            "index": 0,
            "codec_name": "h264",
            "codec_long_name": "H.264 / AVC / MPEG-4 AVC / MPEG-4 part 10",
            "codec_type": "video",
            "codec_time_base": "1001/48000",
            "codec_tag_string": "avc1",
            "codec_tag": "0x31637661",
            "width": 1280,
            "height": 720,
            "has_b_frames": 0,
            "pix_fmt": "yuv420p",
            "level": 31,
            "is_avc": "1",
            "nal_length_size": "4",
            "r_frame_rate": "35029/1461",
            "avg_frame_rate": "35029/1461",
            "time_base": "1/35029",
            "start_time": "0.000000",
            "duration": "1239.195267",
            "bit_rate": "1782423",
            "nb_frames": "29711",
            "tags": {
                "creation_time": "1970-01-01 00:00:00",
                "language": "und",
                "handler_name": "VideoHandler"
            }
        },
        {
            "index": 1,
            "codec_name": "aac",
            "codec_long_name": "Advanced Audio Coding",
            "codec_type": "audio",
            "codec_time_base": "1/48000",
            "codec_tag_string": "mp4a",
            "codec_tag": "0x6134706d",
            "sample_fmt": "s16",
            "sample_rate": "48000",
            "channels": 2,
            "bits_per_sample": 0,
            "r_frame_rate": "0/0",
            "avg_frame_rate": "0/0",
            "time_base": "1/48000",
            "start_time": "0.000000",
            "duration": "1239.059396",
            "bit_rate": "127966",
            "nb_frames": "58081",
            "tags": {
                "creation_time": "2012-04-01 15:42:28",
                "language": "jpn",
                "handler_name": "GPAC ISO Audio Handler"
            }
        }
    ],
    "format": {
        "filename": "lolwut.mp4",
        "nb_streams": 2,
        "format_name": "mov,mp4,m4a,3gp,3g2,mj2",
        "format_long_name": "QuickTime/MPEG-4/Motion JPEG 2000 format",
        "start_time": "0.000000",
        "duration": "1239.195000",
        "size": "296323860",
        "bit_rate": "1913008",
        "tags": {
            "major_brand": "isom",
            "minor_version": "1",
            "compatible_brands": "isom",
            "creation_time": "2012-04-01 15:42:24"
        }
    }
}
*/
    


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
        			LOG.error("user " + uid + " tried to access a linked file outside the docuemnt root: " + fileLink.getDestPath());
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
        	// String progNameAndParams = ffprobeExePath +  " -v error -of flat=s=_ -select_streams v:0 -show_entries stream=height,width,codec_name,duration,avg_frame_rate -sexagesimal " + videoFile.getAbsolutePath();
            
	        VideoInfoExtractor videoInfoExtractor = new VideoInfoExtractor();
            VideoInfo videoInfo = videoInfoExtractor.getVideoInfo(videoFile.getAbsolutePath());

            if (videoInfo.getFfprobeResult() == 0) {
				XmlUtil.setChildText(resultElement, "xpix", Integer.toString(videoInfo.getWidth()));
    	        XmlUtil.setChildText(resultElement, "ypix", Integer.toString(videoInfo.getHeight()));
                XmlUtil.setChildText(resultElement, "codec", videoInfo.getCodec());
                XmlUtil.setChildText(resultElement, "duration", videoInfo.getDuration());
                XmlUtil.setChildText(resultElement, "fps", Integer.toString(videoInfo.getFrameRate()));
			}
			if (videoInfo.isFfprobeEmptyOutput()) {
				XmlUtil.setChildText(resultElement, "error", "ffprobe result empty");
			}
        }        
        	
        doc.appendChild(resultElement);
		
		processResponse();
	}
}
