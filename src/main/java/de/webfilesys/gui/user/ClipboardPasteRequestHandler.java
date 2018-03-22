package de.webfilesys.gui.user;

import java.io.File;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.Category;
import de.webfilesys.ClipBoard;
import de.webfilesys.CopyStatus;
import de.webfilesys.FileSysStat;
import de.webfilesys.GeoTag;
import de.webfilesys.MetaInfManager;
import de.webfilesys.TestSubDirThread;
import de.webfilesys.WebFileSys;
import de.webfilesys.graphics.AutoThumbnailCreator;
import de.webfilesys.graphics.ThumbnailThread;
import de.webfilesys.graphics.VideoThumbnailCreator;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.UTF8URLEncoder;

/**
 * @author Frank Hoehnel
 */
public class ClipboardPasteRequestHandler extends UserRequestHandler
{
	public ClipboardPasteRequestHandler(
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

		boolean pasteToFileWin=false;

		String actPath=getParameter("actpath");

		boolean ignoreExist=false;
		String tmp=getParameter("ignoreExist");
		if ((tmp!=null) && tmp.equals("true"))
		{
			ignoreExist=true;
		}

        boolean thumbView = false;

		if ((actPath==null) || (actPath.length()==0))
		{
			actPath = getCwd();
			pasteToFileWin=true;
			
			String viewMode = getParameter("viewMode");
			
			if ((viewMode != null) && viewMode.equals("thumbnail"))
			{
				thumbView = true;
			}
		}

		if (!checkAccess(actPath))
		{
			return;
		}

		output.println("<HTML>");
		output.println("<HEAD>");

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/common.css\">");
		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/skins/" + userMgr.getCSS(uid) + ".css\">");

		output.println("</HEAD>");

		output.println("<BODY>");

		ClipBoard clipBoard = (ClipBoard) session.getAttribute("clipBoard");
		
		if (clipBoard == null)
		{
			Logger.getLogger(getClass()).warn("clipboard is empty in paste operation");

		    return;
		}

		if (clipBoard.isCopyOperation())
		{
		    headLine(getResource("label.copyfromclip","Copying files from Clipboard") + " ...");
	    }
	    else
   	    {
		    headLine(getResource("label.movefromclip","Moving files from Clipboard") + " ...");
	    }

		output.println("<br>");

		output.println("<form accept-charset=\"utf-8\" name=\"form1\">");

		output.println("<table class=\"dataForm\" width=\"100%\">");

		output.println("<tr>");
		output.println("<td class=\"formParm1\">");
		output.println(getResource("label.currentcopy","current file") + ":");
		output.println("</td>");
		output.println("</tr>");
		
		output.println("<tr>");
		output.println("<td class=\"formParm2\">");
		output.println("<div id=\"currentFile\" />");
		output.println("</td>");
		output.println("</tr>");
		
		output.println("<tr>");
		output.println("<td class=\"formParm1\">");
		
		String labelText;
		if (clipBoard.isCopyOperation()) {
			labelText = getResource("label.copyresult", "files copied");
		} else {
			labelText = getResource("label.moveresult", "files moved");
		}
		
		output.println(labelText + ":");
		output.println("</td>");
		output.println("</tr>");

		output.println("<tr>");
		output.println("<td class=\"formParm2\">");
		output.println("<div id=\"fileCount\" />");
		output.println("</td>");
		output.println("</tr>");

		output.println("<tr>");
		output.println("<td class=\"formParm2\">");
		output.println("<div class=\"progressBar\">");
		output.println("<img id=\"copyProgressBar\" src=\"/webfilesys/images/bluedot.gif\" style=\"width:1px\" />");
		output.println("</div>");
		output.println("</td>");
		output.println("</tr>");
		
		output.println("<tr>");
		output.println("<td class=\"formParm1\">");
		
		if (clipBoard.isCopyOperation()) {
			labelText = getResource("bytesCopied", "bytes copied");
		} else {
			labelText = getResource("bytesMoved", "bytes moved");
		}
		
		output.println(labelText + ":");
		output.println("</td>");
		output.println("</tr>");

		output.println("<tr>");
		output.println("<td class=\"formParm2\">");
		output.println("<div id=\"bytesCopied\" />");
		output.println("</td>");
		output.println("</tr>");

		output.println("</table>");

		output.println("</form>");
		
		output.flush();

		ArrayList<String> clipFiles = clipBoard.getAllFiles();

		if (clipFiles != null)
		{
			String destDir=actPath;

			if (!destDir.endsWith(File.separator))
			{
				destDir=destDir + File.separator;
			}

			int copyFileCounter = 0;
			
			for (String sourceFile : clipFiles) 
			{
				int lastSepIdx=sourceFile.lastIndexOf(File.separatorChar);
                
				String destFile=destDir + sourceFile.substring(lastSepIdx+1);

				if (sourceFile.equals(destFile))
				{
					int extIdx=destFile.lastIndexOf('.');

					if ((extIdx>0) && (extIdx < destFile.length()-1))
					{
						destFile=destFile.substring(0,extIdx) + getResource("label.copyOf","-copy") + destFile.substring(extIdx);
					}
					else
					{
						destFile=destFile + getResource("label.copyOf","-copy");
					}
				}

				if ((copyFileCounter <= 100) ||
				    ((copyFileCounter < 300) && (copyFileCounter % 5 == 0)) ||
				    ((copyFileCounter < 1000) && (copyFileCounter % 10 == 0)) ||
				    (copyFileCounter % 50 == 0)) {
					
	                output.println("<script language=\"javascript\">");

	                output.println("document.getElementById('currentFile').innerHTML='" + escapeForJavascript(CommonUtils.shortName(getHeadlinePath(sourceFile), 40)) + "';");
	                
	                output.println("</script>");
				}
				
				output.flush();

				boolean copyOk = copyFile(sourceFile, destFile);

				if (copyOk)
				{
					copyFileCounter++;
					
	                if ((copyFileCounter <= 100) ||
	                    ((copyFileCounter < 300) && (copyFileCounter % 5 == 0)) ||
	                    ((copyFileCounter < 1000) && (copyFileCounter % 10 == 0)) ||
	                    (copyFileCounter % 50 == 0))
	                {
	                    output.println("<script language=\"javascript\">");

	                    output.println("document.getElementById('fileCount').innerHTML='" + copyFileCounter +  "';");
	                    
	                    output.println("</script>");
	                }

					MetaInfManager metaInfMgr=MetaInfManager.getInstance();

					String description=metaInfMgr.getDescription(sourceFile);
					
					ArrayList<Category> assignedCategories = metaInfMgr.getListOfCategories(sourceFile);
					
					GeoTag geoData =  metaInfMgr.getGeoTag(sourceFile);

					if (clipBoard.isMoveOperation())
					{
						File delFile=new File(sourceFile);

						if (!delFile.delete())
						{
							javascriptAlert(getResource("alert.delmovederror","Cannot delete moved file") + "\\n" + insertDoubleBackslash(sourceFile)); 
						}
						else
						{
					        if (WebFileSys.getInstance().isReverseFileLinkingEnabled())
					        {
	                            metaInfMgr.updateLinksAfterMove(sourceFile, destFile, uid);
					        }
						    
							metaInfMgr.removeMetaInf(sourceFile);
							
							String thumbnailPath = ThumbnailThread.getThumbnailPath(sourceFile);
				
							File thumbnailFile = new File(thumbnailPath);
				
							if (thumbnailFile.exists())
							{
								if (!thumbnailFile.delete())
								{
									Logger.getLogger(getClass()).debug("cannot remove thumbnail file " + thumbnailPath);
								}
							}
							
				            if (WebFileSys.getInstance().getFfmpegExePath() != null) {
				                String videoThumbnailPath = VideoThumbnailCreator.getThumbnailPath(sourceFile);

				                File videoThumbnailFile = new File(videoThumbnailPath);

				                if (videoThumbnailFile.exists()) {
				                    if (!videoThumbnailFile.delete()) {
				                        Logger.getLogger(getClass()).warn("failed to remove video thumbnail file " + videoThumbnailPath);
				                    }
				                }
				            }
						}
					}

					if ((description!=null) && (description.trim().length()>0))
					{
						metaInfMgr.setDescription(destFile,description);
					}
					
					if (assignedCategories != null)
					{
						for (int i = 0; i < assignedCategories.size(); i++)
						{
							metaInfMgr.addCategory(destFile, (Category) assignedCategories.get(i));
						}
					}
					
					if (geoData != null) {
					    metaInfMgr.setGeoTag(destFile, geoData);	
					}
				}
				else
				{
					javascriptAlert(insertDoubleBackslash(sourceFile) + "\\n" + getResource("alert.copycliperror","cannot be copied to") + "\\n" + insertDoubleBackslash(destFile)); 
				}

			}
			
			if (copyFileCounter > 100)
			{
                output.println("<script language=\"javascript\">");
                output.println("document.getElementById('fileCount').innerHTML='" + copyFileCounter +  "';");
                output.println("</script>");
			}
		}

		ArrayList<String> clipDirs = clipBoard.getAllDirs();

		if (clipDirs != null) {
			String destDir=actPath;

			if (!destDir.endsWith(File.separator)) {
				destDir=destDir + File.separator;
			}

			for (String sourceDir : clipDirs) {
				String destSubdir = destDir + sourceDir.substring(sourceDir.lastIndexOf(File.separatorChar)+1);

				File destDirFile = new File(destSubdir);

				if (!destDirFile.exists()) {
					if (!destDirFile.mkdir()) {
						output.println("<script language=\"javascript\">");
						output.println("alert('" + getResource("alert.mkdirfail","Cannot create directory") + "\\n" + insertDoubleBackslash(destSubdir) + "!');");

						if (pasteToFileWin) {
							output.println("window.location.href='/webfilesys/servlet?command=listFiles';");
						} else {
							output.println("window.location.href='/webfilesys/servlet?command=exp&expand=" + UTF8URLEncoder.encode(actPath) + "';");
						}

						output.println("</script>");
						output.println("</BODY></html>");
						output.flush();
						return; 
					}
				}

				DecimalFormat numFormat = new DecimalFormat("#,###,###,###,###");
				
		        FileSysStat fileSysStat = new FileSysStat(sourceDir);
		        fileSysStat.getStatistics();        
				
				CopyStatus copyStatus = new CopyStatus();
				copyStatus.setTreeFileSize(fileSysStat.getTotalSizeSum());
				copyStatus.setTreeFileNum(fileSysStat.getTotalFileNum());
				
				boolean copyOK = copyFolderTreeWithStatus(sourceDir, destSubdir, true, copyStatus, numFormat);
                
                if (!copyOK)
                {
                    javascriptAlert(getResource("alert.copyDirError", "Failed to copy folder tree!"));
                }
                else 
                {
                    if (clipBoard.isMoveOperation()) 
                    {
                        if (copyOK) 
                        {
                            if (!delDirTree(sourceDir))
                            {
                                javascriptAlert(getResource("alert.delMovedDirError", "Failed to delete the source folder of the move operation!"));
                            }
                            
                            String parentOfSourceDir = CommonUtils.getParentDir(sourceDir);
                            
                            if (parentOfSourceDir != null) 
                            {
                                TestSubDirThread subDirThread = new TestSubDirThread(parentOfSourceDir);
                                subDirThread.start();
                            }
                        }

                        (new TestSubDirThread(destSubdir)).start();
                    }
                }
			}     

	        TestSubDirThread subDirThread = new TestSubDirThread(actPath);

	        subDirThread.start();
		}

		output.println("<script language=\"javascript\">");

		if (clipBoard.isMoveOperation()) {
			clipBoard.reset();
		}
		
		if (pasteToFileWin)
		{
			if (thumbView)
			{
				output.println("window.location.href='/webfilesys/servlet?command=thumbnail&zoom=no&random="  + (new Date()).getTime() + "';");
			}
			else
			{
				output.println("window.location.href='/webfilesys/servlet?command=listFiles&mask=*';");
			}
			
			if (clipDirs != null)
			{
				output.print("window.parent.DirectoryPath.location.href='/webfilesys/servlet?command=refresh&path=" + UTF8URLEncoder.encode(getCwd()) + "';");
			}
		}
		else
		{
			output.println("window.location.href='/webfilesys/servlet?command=exp&expand=" + UTF8URLEncoder.encode(actPath) + "&fastPath=true';");
		}
		output.println("</script>");

		output.println("</body></html>");
		output.flush();
		
		if (WebFileSys.getInstance().isAutoCreateThumbs())
		{
			AutoThumbnailCreator.getInstance().queuePath(actPath, AutoThumbnailCreator.SCOPE_TREE);
		}
	}

}
