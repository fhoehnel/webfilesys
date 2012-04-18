package de.webfilesys.gui.xsl;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.LanguageManager;
import de.webfilesys.graphics.CameraExifData;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.UTF8URLEncoder;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslExifDataHandler extends XslRequestHandlerBase
{
	public XslExifDataHandler(
			HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid)
	{
        super(req, resp, session, output, uid);
	}
	  
	protected void process()
	{
        String imgFileName = getParameter("imgFile");

        if (!this.checkAccess(imgFileName))
        {
            return;
        }

		Element cameraDataElement = doc.createElement("cameraData");
			
		doc.appendChild(cameraDataElement);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/cameraData.xsl\"");

		doc.insertBefore(xslRef, cameraDataElement);

        XmlUtil.setChildText(cameraDataElement, "css", userMgr.getCSS(uid), false);

        String shortImgName = CommonUtils.shortName(this.getHeadlinePath(imgFileName), 48);
        
		XmlUtil.setChildText(cameraDataElement, "shortImgName", shortImgName, false);
		
        addMsgResource("alt.cameradata", getResource("alt.cameradata", "Camera Data"));
		addMsgResource("alert.nocameradata", getResource("alert.nocameradata", "No camera data available"));
		addMsgResource("label.picturefile", getResource("label.picturefile", "picture file"));
        addMsgResource("label.manufacturer", getResource("label.manufacturer", "camera manufacturer"));
        addMsgResource("label.cameramodel", getResource("label.cameramodel","camera model"));
        addMsgResource("label.exposuredate", getResource("label.exposuredate", "exposure date"));
        addMsgResource("label.exposuretime", getResource("label.exposuretime", "exposure time"));
        addMsgResource("label.aperture", getResource("label.aperture", "aperture"));
        addMsgResource("label.isoValue", getResource("label.isoValue", "ISO equivalent"));
        addMsgResource("label.flashfired", getResource("label.flashfired", "flash fired"));
        addMsgResource("label.imgwidth", getResource("label.imgwidth", "image width"));
        addMsgResource("label.imgheight", getResource("label.imgheight", "image height"));
        addMsgResource("label.thumbexists", getResource("label.thumbexists", "thumbnail included"));
        addMsgResource("label.gpsLatitude", getResource("label.gpsLatitude", "GPS latitude")); 
        addMsgResource("label.gpsLongitude", getResource("label.gpsLongitude", "GPS longitude")); 
        addMsgResource("button.closewin", getResource("button.closewin", "Close Window"));
        
        CameraExifData exifData=new CameraExifData(imgFileName);

        if (exifData.hasExifData())
        {
            Element exifDataElement = doc.createElement("exifData");
            
            cameraDataElement.appendChild(exifDataElement);
            
            String manufacturer=exifData.getManufacturer();

            if (manufacturer != null)
            {
                XmlUtil.setChildText(exifDataElement, "manufacturer", manufacturer);
            }
            
            String cameraModel=exifData.getCameraModel();

            if (cameraModel != null)
            {
                XmlUtil.setChildText(exifDataElement, "cameraModel", cameraModel);
            } 
            
            Date exposureDate=exifData.getExposureDate();

            if (exposureDate!=null)
            {
                SimpleDateFormat dateFormat = LanguageManager.getInstance().getDateFormat(language);

                String formattedDate = dateFormat.format(exposureDate);

                XmlUtil.setChildText(exifDataElement, "exposureDate", formattedDate);
            }
            
            String exposureTime = exifData.getExposureTime();

            if (exposureTime!=null)
            {
                XmlUtil.setChildText(exifDataElement, "exposureTime", exposureTime);
            }
            
            String aperture = exifData.getAperture();

            if (aperture != null)
            {
                XmlUtil.setChildText(exifDataElement, "aperture", aperture);
            }
            
            String isoValue = exifData.getISOValue();
            
            if (isoValue != null)
            {
                XmlUtil.setChildText(exifDataElement, "isoValue", isoValue);
            }
            
            int flashFired = exifData.getFlashFired();

            if (flashFired >= 0)
            {
                if ((flashFired == 0) || (flashFired == 16) || (flashFired == 24) || (flashFired == 32))
                {
                    XmlUtil.setChildText(exifDataElement, "flashFired", getResource("label.no","no"));
                }
                else
                {
                    XmlUtil.setChildText(exifDataElement, "flashFired", getResource("label.yes","yes"));
                }
            }    
            
            int imageWidth = exifData.getImageWidth();

            if (imageWidth >= 0)
            {
                XmlUtil.setChildText(exifDataElement, "imgWidth", Integer.toString(imageWidth));
            }
            
            int imageHeight = exifData.getImageHeigth();

            if (imageHeight >= 0)
            {
                XmlUtil.setChildText(exifDataElement, "imgHeight", Integer.toString(imageHeight));
            }
            
            int thumbLength = exifData.getThumbnailLength();

            if (thumbLength > 0)
            {
                String srcFileName = "/webfilesys/servlet?command=exifThumb&imgFile=" + UTF8URLEncoder.encode(imgFileName);

                XmlUtil.setChildText(exifDataElement, "thumbnailPath", srcFileName);
            }
            
            int thumbWidth = exifData.getThumbWidth();
            int thumbHeight = exifData.getThumbHeight();
            
            if ((thumbWidth > 0) && (thumbHeight > 0))
            {
                XmlUtil.setChildText(exifDataElement, "thumbnailWidth", Integer.toString(thumbWidth));
                XmlUtil.setChildText(exifDataElement, "thumbnailHeight", Integer.toString(thumbHeight));
            }
            
            float gpsLatitude = exifData.getGpsLatitude();
            
            if (gpsLatitude >= 0.0f)
            {
                XmlUtil.setChildText(exifDataElement, "gpsLatitude", Float.toString(gpsLatitude) +  " " + exifData.getGpsLatitudeRef());
            }
            
            float gpsLongitude = exifData.getGpsLongitude();
            
            if (gpsLongitude >= 0.0f)
            {
                XmlUtil.setChildText(exifDataElement, "gpsLongitude", Float.toString(gpsLongitude) +  " " + exifData.getGpsLongitudeRef());
            }
            
            int orientation = exifData.getOrientation();
            
            if (orientation != CameraExifData.ORIENTATION_UNKNOWN)
            {
                XmlUtil.setChildText(exifDataElement, "orientation", Integer.toString(orientation));
                
                addMsgResource("label.imgOrientation", getResource("label.imgOrientation", "orientation")); 
                addMsgResource("orientation.landscape", getResource("orientation.landscape", "lanscape")); 
                addMsgResource("orientation.portrait", getResource("orientation.portrait", "portrait")); 
            }
        }

        this.processResponse("cameraData.xsl", false);
	}
}