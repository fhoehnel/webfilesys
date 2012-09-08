package de.webfilesys.gui.xsl;

import java.io.File;
import java.io.PrintWriter;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
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
public class XslSlideShowHandler extends XslRequestHandlerBase
{
	public static final String SLIDESHOW_BUFFER = "slideshowBuffer";
	
	public static final String imgFileMasks[]={"*.gif","*.jpg","*.jpeg","*.png","*.bmp"};

	public XslSlideShowHandler(
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
		String actPath = getParameter("actpath");
		
		if (actPath == null)
		{
			actPath = getCwd();
		}
		
		if (!checkAccess(actPath))
		{
			return;
		}
		
		String recurseParm = getParameter("recurse");
		String delayString = getParameter("delay");
		String autoForwardParm = getParameter("autoForward");
		String startFilePath = getParameter("startFilePath");

		boolean autoForward=((autoForwardParm!=null) && autoForwardParm.equalsIgnoreCase("true"));

		int delay = WebFileSys.getInstance().getSlideShowDelay();
		int imageIdx=0;
		try
		{
			delay = Integer.parseInt(delayString);
		}
		catch (NumberFormatException nfe)
		{
		}
		
		boolean recurse=false;
		if (recurseParm.equalsIgnoreCase("true"))
		{
			recurse = true;
		}

		if (imageIdx<=0)
		{
			session.removeAttribute(SLIDESHOW_BUFFER);
			getImageTree(actPath,recurse);
			if (startFilePath == null) {
				imageIdx=0;
			} 
			else 
			{
				imageIdx = getStartFileIndex(startFilePath);
			}
		}
		else
		{
			Vector imageFiles=(Vector) session.getAttribute(SLIDESHOW_BUFFER); 
			if ((imageFiles==null) || (imageIdx>=imageFiles.size()))
			{
				session.removeAttribute(SLIDESHOW_BUFFER);
				getImageTree(actPath,recurse);
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
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/slideShow.xsl\"");

		doc.insertBefore(xslRef, slideShowElement);

		XmlUtil.setChildText(slideShowElement, "css", userMgr.getCSS(uid), false);

		XmlUtil.setChildText(slideShowElement, "delay", Integer.toString(delay * 1000), false);

		XmlUtil.setChildText(slideShowElement, "startIdx", Integer.toString(imageIdx), false);

		XmlUtil.setChildText(slideShowElement, "autoForward", "" + autoForward, false);
		
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
		addMsgResource("alt.continue", getResource("alt.continue","continue slideshow"));
        addMsgResource("alt.back", getResource("alt.back","previous picture"));
		
		this.processResponse("slideShow.xsl", false);
	}
	
    private int getStartFileIndex(String startFilePath) 
    {
		Vector imageTree = (Vector) session.getAttribute(SLIDESHOW_BUFFER);
		if (imageTree == null)
		{
			return 0;
		}

		for (int i = 0; i < imageTree.size(); i++) 
		{
			String fileName = (String) imageTree.elementAt(i);
			if (fileName.equals(startFilePath))
			{
				return i;
			}
		}
		return 0;
    }
	
	public void getImageTree(String actPath,boolean recurse)
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
