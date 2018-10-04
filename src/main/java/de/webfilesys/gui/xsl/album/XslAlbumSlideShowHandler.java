package de.webfilesys.gui.xsl.album;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.Constants;
import de.webfilesys.FileComparator;
import de.webfilesys.FileContainer;
import de.webfilesys.FileLinkSelector;
import de.webfilesys.FileSelectionStatus;
import de.webfilesys.MetaInfManager;
import de.webfilesys.WebFileSys;
import de.webfilesys.graphics.ScaledImage;
import de.webfilesys.graphics.ThumbnailThread;
import de.webfilesys.gui.xsl.XslRequestHandlerBase;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.UTF8URLEncoder;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslAlbumSlideShowHandler extends XslRequestHandlerBase
{
	public static final String SLIDESHOW_BUFFER = "slideshowBuffer";
	
	public XslAlbumSlideShowHandler(
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
		
        Element slideShowElement = doc.createElement("slideShow");
        
        doc.appendChild(slideShowElement);
            
        ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/album/albumSlideShow.xsl\"");

        doc.insertBefore(xslRef, slideShowElement);

        XmlUtil.setChildText(slideShowElement, "css", userMgr.getCSS(uid), false);
	    XmlUtil.setChildText(slideShowElement, "language", language, false);

        if (readonly)
        {
            XmlUtil.setChildText(slideShowElement, "readonly", "true", false);
        }
        else
        {
            XmlUtil.setChildText(slideShowElement, "readonly", "false", false);
        }
		
        // String encodedPath = UTF8URLEncoder.encode(actPath);
        
        String windowWidth = getParameter("windowWidth");
        String windowHeight = getParameter("windowHeight");

        int windowXSize = 300;
        int windowYSize = 300;
        
        try
        {
            windowXSize = Integer.parseInt(windowWidth);
            windowYSize = Integer.parseInt(windowHeight);   
        }
        catch (NumberFormatException nfex)
        {
        }
        
        String screenWidthParm = getParameter("screenWidth");
        String screenHeightParm = getParameter("screenHeight");

        if (screenWidthParm!=null)
        {
            try
            {
                int newScreenWidth = Integer.parseInt(screenWidthParm);

                session.setAttribute("screenWidth", new Integer(newScreenWidth));
            }
            catch (NumberFormatException nfex)
            {
            }
        }

        if (screenHeightParm!=null)
        {
            try
            {
                int newScreenHeight = Integer.parseInt(screenHeightParm);

                session.setAttribute("screenHeight", new Integer(newScreenHeight));
            }
            catch (NumberFormatException nfex)
            {
            }
        }

        int screenWidth = Constants.DEFAULT_SCREEN_WIDTH;
        int screenHeight = Constants.DEFAULT_SCREEN_HEIGHT;

        Integer widthScreen = (Integer) session.getAttribute("screenWidth");
        
        if (widthScreen != null)
        {
            screenWidth = widthScreen.intValue();
        }

        Integer heightScreen = (Integer) session.getAttribute("screenHeight");
        
        if (heightScreen != null)
        {
            screenHeight = heightScreen.intValue();
        }

        int delay = WebFileSys.getInstance().getSlideShowDelay();
        
        int imageIdx = (-1);
        try
        {
            String delayParam = getParameter("delay");
            if (delayParam != null)
            {
                delay = Integer.parseInt(delayParam);
            }
            
            String indexParam = getParameter("imageIdx");
            if (!CommonUtils.isEmpty(indexParam))
            {
                imageIdx = Integer.parseInt(indexParam);
            }
        }
        catch (NumberFormatException nfe)
        {
        }

        boolean autoForward = (!CommonUtils.isEmpty(getParameter("autoForward")));

        boolean recurse = (!CommonUtils.isEmpty(getParameter("recurse")));

        boolean crossfade = (!CommonUtils.isEmpty(req.getParameter("crossfade")));

        String role = userMgr.getRole(uid);
        
        if ((role != null) && role.equals("album"))
        {
            XmlUtil.setChildText(slideShowElement, "album", "true", false);
        }        
        
        ArrayList imageFiles = null;
        
        if (imageIdx < 0)
        {
            session.removeAttribute(SLIDESHOW_BUFFER);
            getImageTree(actPath, recurse);

            String startFilePath = getParameter("startFilePath");
			if (startFilePath == null) {
				imageIdx = 0;
			} 
			else 
			{
				imageIdx = getStartFileIndex(startFilePath);
			}
        }
        else
        {
            imageFiles = (ArrayList) session.getAttribute(SLIDESHOW_BUFFER); 
            if ((imageFiles == null) || (imageIdx>=imageFiles.size()))
            {
                session.removeAttribute(SLIDESHOW_BUFFER);
                getImageTree(actPath,recurse);
                imageIdx=0;
            }
        }

        imageFiles = (ArrayList) session.getAttribute(SLIDESHOW_BUFFER); 
        
        XmlUtil.setChildText(slideShowElement, "imageCount", Integer.toString(imageFiles.size()), false);
        
        if (imageFiles.size() == 0) {
            XmlUtil.setChildText(slideShowElement, "shortPath", getHeadlinePath(CommonUtils.shortName(actPath, 50)), false);

            processResponse("album/albumSlideShow.xsl", false);
            return;
        }
        
        XmlUtil.setChildText(slideShowElement, "delay", Integer.toString(delay), false);

        if (autoForward)
        {
            XmlUtil.setChildText(slideShowElement, "autoForward", "true", false);
        }

        if (crossfade)
        {
            XmlUtil.setChildText(slideShowElement, "crossfade", "true", false);
        }
        
        XmlUtil.setChildText(slideShowElement, "imgIdx", Integer.toString(imageIdx), false);

        String imgPath = (String) imageFiles.get(imageIdx);
        
        XmlUtil.setChildText(slideShowElement, "imgPath", imgPath, false);
        
        String imgFileName = CommonUtils.extractFileName(imgPath);
        
        XmlUtil.setChildText(slideShowElement, "shortImgName", getHeadlinePath(CommonUtils.shortName(imgFileName, 50)), false);
        
        XmlUtil.setChildText(slideShowElement, "encodedPath", UTF8URLEncoder.encode(imgPath), false);

        if (imageIdx == 0) 
        {
            XmlUtil.setChildText(slideShowElement, "firstImg", "true", false);
        }
        
        int nextImageIdx = imageIdx + 1;
        
        if (nextImageIdx >= imageFiles.size())
        {
            nextImageIdx = 0;
        }
        
        XmlUtil.setChildText(slideShowElement, "nextImgIdx", Integer.toString(nextImageIdx), false);

        String nextFileName = (String) imageFiles.get(nextImageIdx);

        nextFileName = UTF8URLEncoder.encode(nextFileName);
		
        XmlUtil.setChildText(slideShowElement, "nextImgPath", nextFileName, false);
		
        if (imageIdx > 0) 
        {
            XmlUtil.setChildText(slideShowElement, "prevImgIdx", Integer.toString(imageIdx - 1), false);
        }
        
        try
        {
            ScaledImage scaledImage = new ScaledImage(imgPath, windowXSize - 20, windowYSize - 130);

            ScaledImage bigImage = new ScaledImage(imgPath, screenWidth - 40, screenHeight - 155);

            if ((role == null) || (!role.equals("album")))
            {
                int fullScreenWidth = bigImage.getScaledWidth() + 20;
                        
                if (fullScreenWidth < 600)
                {
                    fullScreenWidth = 600;
                }

                XmlUtil.setChildText(slideShowElement, "detailWinWidth", Integer.toString(fullScreenWidth), false);
                XmlUtil.setChildText(slideShowElement, "detailWinHeight", Integer.toString((bigImage.getScaledHeight() + 70)), false);
            }

            XmlUtil.setChildText(slideShowElement, "displayWidth", Integer.toString(scaledImage.getScaledWidth()), false);
            XmlUtil.setChildText(slideShowElement, "displayHeight", Integer.toString(scaledImage.getScaledHeight()), false);
        }
        catch (IOException io1)
        {
            Logger.getLogger(getClass()).error(io1);
        }
		
        if (autoForward)
        {
            String pause = getParameter("pause");

            if ((pause != null) && pause.equalsIgnoreCase("true"))
            {
                XmlUtil.setChildText(slideShowElement, "paused", "true", false);
            }
        }
        
        MetaInfManager metaInfMgr = MetaInfManager.getInstance();

        String description = metaInfMgr.getDescription(imgPath);

        if ((description != null) && (description.trim().length() > 0))
        {
            XmlUtil.setChildText(slideShowElement, "description", description, true);
        }
        
		processResponse("album/albumSlideShow.xsl", false);
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

        ArrayList imageTree = (ArrayList) session.getAttribute(SLIDESHOW_BUFFER);
        if (imageTree == null)
        {
            imageTree = new ArrayList();
            session.setAttribute(SLIDESHOW_BUFFER,imageTree);
        }

        FileLinkSelector fileSelector=new FileLinkSelector(actPath,FileComparator.SORT_BY_FILENAME);

        FileSelectionStatus selectionStatus=fileSelector.selectFiles(Constants.imgFileMasks,4096,null,null);

        ArrayList<FileContainer> imageFiles = selectionStatus.getSelectedFiles();

        if (imageFiles!=null)
        {
            for (i=0;i<imageFiles.size();i++)
            {
                FileContainer fileCont = (FileContainer) imageFiles.get(i);
                
                imageTree.add(fileCont.getRealFile().getAbsolutePath());
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

        dirFile = new File(actPath);
        fileList = dirFile.list();

        if (fileList == null)
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
    
    private int getStartFileIndex(String startFilePath) 
    {
		ArrayList imageTree = (ArrayList) session.getAttribute(SLIDESHOW_BUFFER);
		if (imageTree == null)
		{
			return 0;
		}

		for (int i = 0; i < imageTree.size(); i++) 
		{
			String fileName = (String) imageTree.get(i);
			if (fileName.equals(startFilePath))
			{
				return i;
			}
		}
		return 0;
    }
	
}
