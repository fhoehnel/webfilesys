package de.webfilesys.gui.ajax;

import java.io.PrintWriter;
import java.text.DecimalFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;

import de.webfilesys.Constants;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XmlUploadStatusHandler extends XmlRequestHandlerBase
{
	public XmlUploadStatusHandler(    		
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
		if (!checkWriteAccess())
		{
			return;
		}
		
		DecimalFormat numFormat = new DecimalFormat("#,###,###,###");
		
		int fileSize = 0;
		
		Integer uploadSize = (Integer) session.getAttribute(Constants.UPLOAD_SIZE);
		
		if (uploadSize != null)
		{
			fileSize = uploadSize.intValue();
		}
		
		int bytesUploaded = 0;
		
		Integer uploadCounter = (Integer) session.getAttribute(Constants.UPLOAD_COUNTER);

		if (uploadCounter != null)
		{
			bytesUploaded = uploadCounter.intValue();
		}

		int percentUploaded = 0;

        if (fileSize > 0)
        {
			percentUploaded = (int) ( (((long) bytesUploaded) * 100) / ((long) fileSize) );
        }

        Boolean uploadSuccess = (Boolean) session.getAttribute(Constants.UPLOAD_SUCCESS);       
        
		Element resultElement = doc.createElement("result");
		
		XmlUtil.setChildText(resultElement, "fileSize", numFormat.format(fileSize));
		XmlUtil.setChildText(resultElement, "bytesUploaded", numFormat.format(bytesUploaded));
		XmlUtil.setChildText(resultElement, "percent", Integer.toString(percentUploaded));

		XmlUtil.setChildText(resultElement, "success", Boolean.toString((uploadSuccess != null) && uploadSuccess.booleanValue()));
			
		doc.appendChild(resultElement);
		
		this.processResponse();
	}
}
