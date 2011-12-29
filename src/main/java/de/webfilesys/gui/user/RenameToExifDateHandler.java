package de.webfilesys.gui.user;

import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.Category;
import de.webfilesys.MetaInfManager;
import de.webfilesys.graphics.CameraExifData;
import de.webfilesys.gui.xsl.XslThumbnailHandler;

/**
 * Rename JPEG files according to the exposure date extracted from the Camera Exif
 * data.
 * @author Frank Hoehnel
 */
public class RenameToExifDateHandler extends MultiImageRequestHandler
{
	private static final String DATE_FORMAT = "yyyy-MM-dd-HH-mm-ss";
	
	boolean clientIsLocal = false;
	
	public RenameToExifDateHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid,
		    boolean clientIsLocal)
	{
        super(req, resp, session, output, uid);

		this.clientIsLocal = clientIsLocal;
	}

	protected void process()
	{
		if (!checkWriteAccess())
		{
			return;
		}

		for (int i=0;i<selectedFiles.size();i++)
		{
			String imgFileName=actPath + File.separator + selectedFiles.elementAt(i);

			String fileExt="";

			int extStart = imgFileName.lastIndexOf('.');
			if (extStart>0)
			{
				fileExt = imgFileName.substring(extStart).toUpperCase();
			}
            
            if (fileExt.equals(".JPG") || fileExt.equals(".JPEG"))
            {
				CameraExifData exifData=new CameraExifData(imgFileName);

				if (exifData.hasExifData())
				{
					Date exposureDate = exifData.getExposureDate();
				
					if (exposureDate != null)
					{
						SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

						File newImgFile = new File(getCwd(), dateFormat.format(exposureDate) + fileExt);
						
						File imgFile = new File(imgFileName);
						
						if (!imgFile.renameTo(newImgFile))
						{
							Logger.getLogger(getClass()).error("could not rename image file " + imgFileName + " to exif exposure date: + " + newImgFile);
						}
						else
						{
							newImgFile.setLastModified(exposureDate.getTime());
							
							MetaInfManager metaInfMgr=MetaInfManager.getInstance();

							String description=metaInfMgr.getDescription(imgFileName);

							if ((description!=null) && (description.trim().length()>0))
							{
								metaInfMgr.setDescription(newImgFile.getAbsolutePath(),description);
							}

							Vector assignedCategories = metaInfMgr.getListOfCategories(imgFileName);
		
							if (assignedCategories != null)
							{
								for (int k=0;k<assignedCategories.size();k++)
								{
									Category cat = (Category) assignedCategories.elementAt(k);
				
									metaInfMgr.addCategory(newImgFile.getAbsolutePath(), cat);
								}
							}

							metaInfMgr.removeMetaInf(imgFileName);
						}
					}
				}
            }
		}

	    (new XslThumbnailHandler(req, resp, session, output, uid, clientIsLocal)).handleRequest(); 

	}

}
