package de.webfilesys.gui.blog;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.MetaInfManager;
import de.webfilesys.graphics.ScaledImage;
import de.webfilesys.gui.xsl.XslRequestHandlerBase;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.UTF8URLEncoder;
import de.webfilesys.util.XmlUtil;

public class BlogEditEntryHandler extends XslRequestHandlerBase {
	
	public BlogEditEntryHandler(
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
		if (!checkWriteAccess()) {
			return;
		}
		
		String fileName = req.getParameter("fileName");
		
		if (CommonUtils.isEmpty(fileName)) {
	        Logger.getLogger(getClass()).error("missing parameter fileName");
			return;
		}
		
		String currentPath = userMgr.getDocumentRoot(uid).replace('/',  File.separatorChar);

		File picFile = new File(currentPath, fileName);
		
		if ((!picFile.exists()) || (!picFile.isFile()) || (!picFile.canRead())) {
	        Logger.getLogger(getClass()).error("not a readable file: " + fileName);
			return;
		}
		
		Element blogElement = doc.createElement("blog");
			
		doc.appendChild(blogElement);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/blog/blogEditEntry.xsl\"");

		doc.insertBefore(xslRef, blogElement);

		XmlUtil.setChildText(blogElement, "css", userMgr.getCSS(uid), false);
		
		Element blogEntryElement = doc.createElement("blogEntry");
		
		blogElement.appendChild(blogEntryElement);
		
		XmlUtil.setChildText(blogEntryElement, "fileName", fileName);

		String srcFileName = "/webfilesys/servlet?command=getFile&filePath=" + UTF8URLEncoder.encode(picFile.getAbsolutePath()) + "&cached=true";
		
		XmlUtil.setChildText(blogEntryElement, "imgPath", srcFileName);
		
		// TODO: make configurable
		int thumbnailSize = 400;
		
		int thumbWidth = thumbnailSize;
		int thumbHeight = thumbnailSize;
		
		ScaledImage scaledImage = null;

		try {
			scaledImage = new ScaledImage(picFile.getAbsolutePath(), thumbnailSize, thumbnailSize);
			
			thumbWidth = scaledImage.getScaledWidth();
			thumbHeight = scaledImage.getScaledHeight();
		} catch (IOException io1) {
        	Logger.getLogger(getClass()).error("failed to get scaled image dimensions", io1);
		}
		
		XmlUtil.setChildText(blogEntryElement, "thumbnailWidth", Integer.toString(thumbWidth));
		XmlUtil.setChildText(blogEntryElement, "thumbnailHeight", Integer.toString(thumbHeight));
		
		MetaInfManager metaInfMgr = MetaInfManager.getInstance();

		String blogText = metaInfMgr.getDescription(picFile.getAbsolutePath());

		XmlUtil.setChildText(blogEntryElement, "blogText", blogText, true);
		
		Element blogDateElement = doc.createElement("blogDate");
		blogEntryElement.appendChild(blogDateElement);
		
		XmlUtil.setChildText(blogDateElement, "year", fileName.substring(0, 4));
		XmlUtil.setChildText(blogDateElement, "month", fileName.substring(5, 7));
		XmlUtil.setChildText(blogDateElement, "day", fileName.substring(8, 10));
		
		processResponse("blog/blogEditEntry.xsl", true);
    }

}
