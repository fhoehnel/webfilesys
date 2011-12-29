package de.webfilesys.gui.user;

import java.io.File;
import java.io.PrintWriter;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.webfilesys.Constants;
import de.webfilesys.FileComparator;
import de.webfilesys.FileContainer;
import de.webfilesys.FileLinkSelector;
import de.webfilesys.FileSelectionStatus;
import de.webfilesys.graphics.ThumbnailThread;

/**
 * @author Frank Hoehnel
 *
 */
public class SlideShowHandlerBase extends UserRequestHandler
{
	public static final String SLIDESHOW_BUFFER = "slideshowBuffer";
	
    public SlideShowHandlerBase(
            HttpServletRequest req, 
            HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid)
    {
        super(req, resp, session, output, uid);
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

		Vector imageTree = (Vector) session.getAttribute(SLIDESHOW_BUFFER);
		if (imageTree == null)
		{
			imageTree = new Vector();
			session.setAttribute(SLIDESHOW_BUFFER,imageTree);
		}

		FileLinkSelector fileSelector=new FileLinkSelector(actPath,FileComparator.SORT_BY_FILENAME);

		FileSelectionStatus selectionStatus=fileSelector.selectFiles(Constants.imgFileMasks,4096,null,null);

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
}
