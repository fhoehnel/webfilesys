package de.webfilesys.gui.xsl;

import java.io.File;
import java.io.PrintWriter;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.FileComparator;
import de.webfilesys.FileContainer;
import de.webfilesys.FileLinkSelector;
import de.webfilesys.FileSelectionStatus;
import de.webfilesys.WebFileSys;
import de.webfilesys.graphics.ThumbnailThread;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslCoBrowsingMasterHandler extends XslRequestHandlerBase
{
	public static final String SLIDESHOW_BUFFER = "slideshowBuffer";
	
	public static final String imgFileMasks[]={"*.gif","*.jpg","*.jpeg","*.png","*.bmp"};

	public XslCoBrowsingMasterHandler(
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
        String docRoot = userMgr.getDocumentRoot(uid);
		
   	    // TODO: remove this
		int delay = WebFileSys.getInstance().getSlideShowDelay();
		
		int imageIdx = 0;
		
		if (imageIdx <= 0)
		{
			session.removeAttribute(SLIDESHOW_BUFFER);
			getImageTree(docRoot, false);
			imageIdx=0;
		}
		else
		{
			Vector imageFiles=(Vector) session.getAttribute(SLIDESHOW_BUFFER); 
			if ((imageFiles==null) || (imageIdx>=imageFiles.size()))
			{
				session.removeAttribute(SLIDESHOW_BUFFER);
				getImageTree(docRoot, false);
				imageIdx=0;
			}
		}

		Vector imageFiles=(Vector) session.getAttribute(SLIDESHOW_BUFFER);
		if ((imageFiles==null) || (imageFiles.size()==0))
		{
            // todo: error handling
			return;
		}
		
		Element slideShowElement = doc.createElement("slideShow");
		
		doc.appendChild(slideShowElement);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/coBrowsingMaster.xsl\"");

		doc.insertBefore(xslRef, slideShowElement);

		XmlUtil.setChildText(slideShowElement, "css", userMgr.getCSS(uid), false);

		XmlUtil.setChildText(slideShowElement, "delay", Integer.toString(delay * 1000), false);

		// TODO: remove this
		XmlUtil.setChildText(slideShowElement, "autoForward", "false", false);
		
		XmlUtil.setChildText(slideShowElement, "numberOfImages", Integer.toString(imageFiles.size()), false);
		
		if (readonly)
		{
			XmlUtil.setChildText(slideShowElement, "readonly", "true", false);
		}
		else
		{
			XmlUtil.setChildText(slideShowElement, "readonly", "false", false);
		}
		
		addMsgResource("label.slideshow", getResource("label.slideshow", "Picture Slideshow"));
		addMsgResource("alt.exitslideshow", getResource("alt.exitslideshow","exit slideshow"));
		addMsgResource("alt.pause", getResource("alt.pause","pause slideshow"));
        addMsgResource("alt.next", getResource("alt.next","next picture"));
        addMsgResource("alt.back", getResource("alt.back","previous picture"));
        addMsgResource("titleCoBrowsingMaster", getResource("titleCoBrowsingMaster","WebFileSys Co-Browsing (Master)"));
        addMsgResource("exitCoBrowsing", getResource("exitCoBrowsing","Stop Co-Browsing"));
                
		this.processResponse("coBrowsingMaster.xsl", false);
	}
	
	public void getImageTree(String actPath, boolean recurse)
	{
		int i;

		String pathWithSlash=null;
		if (actPath.endsWith(File.separator))
		{
			pathWithSlash=actPath;
		}
		else
		{
			pathWithSlash=actPath + File.separator;
		}

		Vector imageTree=(Vector) session.getAttribute(SLIDESHOW_BUFFER);
		if (imageTree==null)
		{
			imageTree=new Vector();
			session.setAttribute(SLIDESHOW_BUFFER,imageTree);
		}

		FileLinkSelector fileSelector=new FileLinkSelector(actPath,FileComparator.SORT_BY_FILENAME);

		FileSelectionStatus selectionStatus=fileSelector.selectFiles(imgFileMasks,4096,null,null);

		Vector imageFiles=selectionStatus.getSelectedFiles();

		if (imageFiles!=null)
		{
			for (i=0;i<imageFiles.size();i++)
			{
				FileContainer fileCont = (FileContainer) imageFiles.elementAt(i);
				
				imageTree.addElement(fileCont.getRealFile().getAbsolutePath());
			}
		}

		// and now recurse into subdirectories

		if (!recurse)
		{
			return;
		}

		File dirFile;
		File tempFile;
		String subDir;
		String fileList[]=null;

		dirFile=new File(actPath);
		fileList=dirFile.list();

		if (fileList==null)
		{
			return;
		}

		for (i = 0; i < fileList.length; i++)
		{
			if (!fileList[i].equals(ThumbnailThread.THUMBNAIL_SUBDIR))
            {			
				tempFile = new File(pathWithSlash + fileList[i]);

				if (tempFile.isDirectory())
				{
					subDir = pathWithSlash + fileList[i];

					getImageTree(subDir,recurse);
				}
            }
		}
	}

}
