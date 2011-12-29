package de.webfilesys.gui.user;

import java.io.IOException;
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
import de.webfilesys.MetaInfManager;
import de.webfilesys.WebFileSys;
import de.webfilesys.graphics.ScaledImage;
import de.webfilesys.util.UTF8URLEncoder;

/**
 * @author Frank Hoehnel
 */
public class PictureStoryRequestHandler extends UserRequestHandler
{
	public PictureStoryRequestHandler(
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
		int i;

		MetaInfManager metaInfMgr=MetaInfManager.getInstance();

		String actPath = getParameter("actPath");
		if ((actPath == null) || (actPath.length() == 0))
		{
			actPath = getParameter("actpath");
			if ((actPath == null) || (actPath.length() == 0))
			{
				actPath = getCwd();
			}
		}
		
		boolean dirHasMetaInf=metaInfMgr.dirHasMetaInf(actPath);


		int screenWidth = getIntParam("screenWidth", 0);
		int screenHeight = getIntParam("screenHeight", 0);
		
		if (screenWidth > 0)
		{
			session.setAttribute("screenWidth", new Integer(screenWidth));
		}
		else
		{
			Integer sessionScreenWidth = (Integer) session.getAttribute("screenWidth");
			
			if (sessionScreenWidth == null)
			{
				screenWidth = 1024;
			}
			else
			{
				screenWidth = sessionScreenWidth.intValue();
			}
		}

		if (screenHeight > 0)
		{
			session.setAttribute("screenHeight", new Integer(screenHeight));
		}
		else
		{
			Integer sessionScreenHeight = (Integer) session.getAttribute("screenHeight");
			
			if (sessionScreenHeight == null)
			{
				screenHeight = 768;
			}
			else
			{
				screenHeight = sessionScreenHeight.intValue();
			}
		}
		
		int thumbnailSize=200;

		thumbnailSize=(screenWidth-70)/2;

		String beforeName=getParameter("beforeName");
		String afterName=getParameter("afterName");

		output.println("<HTML>");
		output.println("<HEAD>");
		output.println("<TITLE>WebFilesys " + getResource("label.story", "picture story") + "</TITLE>");

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/css/" + userMgr.getCSS(uid) + ".css\">");

		output.println("<SCRIPT language=\"JavaScript\" src=\"javascript/fmweb.js\" type=\"text/javascript\"></SCRIPT>");
		output.println("<SCRIPT language=\"JavaScript\" src=\"javascript/viewMode.js\" type=\"text/javascript\"></SCRIPT>");

		output.println("</HEAD>"); 

		output.println("<body class=\"story\">");

		String description=metaInfMgr.getDescription(actPath,".");

		if ((description==null) || (description.trim().length()==0))
		{
			description=getHeadlinePath(actPath);
		}

		headLine(description);
		
		output.println("<br>");

		int pageSize = WebFileSys.getInstance().getThumbnailsPerPage();

		String temp=getParameter("pageSize");
		
		if ((temp!=null) && (temp.trim().length() > 0))
		{
			try
			{
				pageSize = Integer.parseInt(temp);

				Integer sessionThumbPageSize = (Integer) session.getAttribute("thumbPageSize");
				
				if ((sessionThumbPageSize == null) || (sessionThumbPageSize.intValue() != pageSize))
				{
					session.setAttribute("thumbPageSize", new Integer(pageSize));

					if (!readonly)
					{
						userMgr.setPageSize(uid,pageSize);
					}
				}
			}
			catch (NumberFormatException nfex)
			{
			}
		}
		else
		{
			Integer sessionThumbPageSize = (Integer) session.getAttribute("thumbPageSize");
			
			if ((sessionThumbPageSize != null) && (sessionThumbPageSize.intValue() != 0))
			{
				pageSize = sessionThumbPageSize.intValue();
			}
		}

		FileLinkSelector fileSelector=new FileLinkSelector(actPath,FileComparator.SORT_BY_FILENAME);

		FileSelectionStatus selectionStatus=fileSelector.selectFiles(Constants.imgFileMasks,pageSize,afterName,beforeName);

        int fileNum = 0;

		Vector imageFiles=selectionStatus.getSelectedFiles();

		if (imageFiles != null)
		{
			fileNum = selectionStatus.getNumberOfFiles();
		}

        if (fileNum == 0)
        {
			output.println("<script language=\"javascript\">");
			output.println("alert('" + getResource("alert.nopictures","No picture files (JPG,GIF,PNG) exist in this directory") + "!');");

			output.println("self.close();");

			output.println("</script>");
			output.println("</body></html>");
			output.flush();
			return;
		}

		output.println("<table border=\"0\" width=\"100%\">");
		output.println("<tr>");

		if ((selectionStatus.getBeginIndex()>0) || (!selectionStatus.getIsLastPage()))
		{
			// paging top
				
			output.println("<td valign=\"center\" nowrap>");
			if (selectionStatus.getBeginIndex()>0)
			{
				output.print("<a href=\"/webfilesys/servlet?command=pictureStory\"><img src=\"/webfilesys/images/first.gif\" border=\"0\"></a>");

				output.println("&nbsp;");

				output.print("<a href=\"/webfilesys/servlet?command=pictureStory");
				output.print("&beforeName=" + UTF8URLEncoder.encode(selectionStatus.getFirstFileName()) + "\">");
				output.println("<img src=\"/webfilesys/images/previous.gif\" border=\"0\"></a>");
			}
			else
			{
				// output.println("<img src=\"/images/firstDisabled.gif\" border=\"0\">");
				output.println("&nbsp;");
				// output.println("<img src=\"/images/previousDisabled.gif\" border=\"0\">");
			}

			output.println("</td>");

			output.println("<td>&nbsp;</td>");

			output.println("<td align=\"right\" valign=\"center\" nowrap>");

			if (!selectionStatus.getIsLastPage())
			{
				output.print("<a href=\"/webfilesys/servlet?command=pictureStory");
				output.print("&afterName=" + UTF8URLEncoder.encode(selectionStatus.getLastFileName()) + "\">");
				output.println("<img src=\"/webfilesys/images/next.gif\" border=\"0\"></a>");

				output.println("&nbsp;");

				output.print("<a href=\"/webfilesys/servlet?command=pictureStory");
				output.print("&beforeName=" + UTF8URLEncoder.encode("zzzzzzzzzz") + "\">");
				output.println("<img src=\"/webfilesys/images/last.gif\" border=\"0\"></a>");
			}
			else
			{
				// output.println("<img src=\"/images/nextDisabled.gif\" border=\"0\">");
				output.println("&nbsp;");
				// output.println("<img src=\"/images/lastDisabled.gif\" border=\"0\">");
			}

			output.println("</td>");

			// end paging top
		}			

		output.println("</tr>");
		output.println("</table>");

		if (fileNum == 0)
		{
			output.println("<br><br>");
			output.println("<table border=\"0\"><tr><td class=\"value\">");
			output.println(getResource("alert.nopictures","No picture files (JPG,GIF,PNG) exist in this directory"));
			output.println("</td></tr></table>");
			output.println("</body></html>");
			output.flush();
			return;
		}
		
		output.println("<table border=\"0\" width=\"100%\">");

		int imgCounter=0;

		for (i=0;i<imageFiles.size();i++)
		{
			FileContainer fileCont = (FileContainer) imageFiles.elementAt(i);
			
			String actImageFile = fileCont.getName();

			String fullFileName = fileCont.getRealFile().getAbsolutePath();

			String srcFileName = "/webfilesys/servlet?command=getFile&filePath=" + UTF8URLEncoder.encode(fullFileName);

			int xsize=100;
			int ysize=100;
			int wDisplay=100;
			int hDisplay=100;

			boolean imgFound=true;

			ScaledImage scaledImage=null;

			try
			{
				scaledImage=new ScaledImage(fullFileName,screenWidth-80,screenHeight-110);
			}
			catch (IOException io1)
			{
				System.out.println(io1);
				imgFound=false;                 
			}

			if (imgFound)
			{
				xsize=scaledImage.getRealWidth();
				ysize=scaledImage.getRealHeight();

				wDisplay=scaledImage.getScaledWidth();
				
				if (wDisplay < 580)
				{
					wDisplay = 580;
				}
				
				hDisplay=scaledImage.getScaledHeight();
			}

			int yDisplay;
			int xDisplay;

			if (ysize>xsize)
			{
				if (scaledImage.getRealHeight() > thumbnailSize)
				{
					yDisplay=thumbnailSize;
					xDisplay=xsize * thumbnailSize / ysize;
				}
				else
				{
					yDisplay=scaledImage.getRealHeight();
					xDisplay=xsize * scaledImage.getRealHeight() / ysize;
				}
			}
			else
			{
				if (scaledImage.getRealWidth() > thumbnailSize)
				{
					xDisplay=thumbnailSize;
					yDisplay=ysize*thumbnailSize/xsize;
				}
				else
				{
					xDisplay=scaledImage.getRealWidth();
					yDisplay=ysize * scaledImage.getRealWidth() / xsize;
				}
			}


		  // output.print("<td><a href=\"/_showimg?imgname=" + fullFileName + "\" target=\"_blank\"><img src=\"" + srcFileName +"\" border=0 width=" + xDisplay + " height=" + yDisplay + "></td>");

			description=null;

			if (dirHasMetaInf)
			{
				description=metaInfMgr.getDescription(fullFileName);
			}

			if ((description==null) || (description.trim().length()==0))
			{
				description=actImageFile;
			}

			String filenameForScript;

			filenameForScript=UTF8URLEncoder.encode(fullFileName);

			String newWindowName="img" + System.currentTimeMillis() + imgCounter;

			output.println("<tr>");

			if (imgCounter % 2 == 1)
			{
				output.println("<td class=\"story\" valign=\"top\" align=\"right\" width=\"50%\">");
				output.println("<br>");
				output.println(description);
				output.println("</td>");

				output.println("<td><img src=\"/webfilesys/images/space.gif\" border=\"0\" width=\"10\" height=\"1\"></td>");

				output.println("<td valign=\"top\">");
			}
			else
			{
				output.println("<td align=\"right\" valign=\"top\">");
			}

			output.print("<a name=\"" + imgCounter + "\" href=\"#" + imgCounter + "\" onclick=\"window.open('/webfilesys/servlet?command=showImg&imgname=" + filenameForScript + "','" + newWindowName + "','status=no,toolbar=no,location=no,menu=no,width=" + (wDisplay + 20) + ",height=" + (hDisplay+52) + ",resizable=yes,left=1,top=1,screenX=1,screenY=1')\">");
			output.println("<img src=\"" + srcFileName + "\" border=0 width=" + xDisplay + " height=" + yDisplay + " title=\"" + fullFileName + "\"></a></td>");

			if (imgCounter % 2 == 0)
			{
				output.println("<td align=\"right\"><img src=\"/webfilesys/images/space.gif\" border=\"0\" width=\"10\" height=\"1\"></td>");

				output.println("<td class=\"story\" valign=\"top\">");
				output.println("<br>");
				output.println(description);
				output.println("</td>");
			}

			output.println("</tr>");

			output.println("<tr><td colspan=\"3\"><img src=\"/webfilesys/images/space.gif\" border=\"0\" width=\"1\" height=\"10\"></td></tr>");

			imgCounter++;
		}

		output.println("</table>");

		if ((selectionStatus.getBeginIndex()>0) || (!selectionStatus.getIsLastPage()))
        {
			// paging bottom
			output.println("<table border=\"0\" width=\"100%\">");
			output.println("<tr>");

			output.println("<td valign=\"center\">");
			if (selectionStatus.getBeginIndex()>0)
			{
				output.print("<a href=\"/webfilesys/servlet?command=pictureStory\"><img src=\"/webfilesys/images/first.gif\" border=\"0\"></a>");

				output.println("&nbsp;");

				output.print("<a href=\"/webfilesys/servlet?command=pictureStory");
				output.print("&beforeName=" + UTF8URLEncoder.encode(selectionStatus.getFirstFileName()) + "\">");
				output.println("<img src=\"/webfilesys/images/previous.gif\" border=\"0\"></a>");
			}
			else
			{
				// output.println("<img src=\"/images/firstDisabled.gif\" border=\"0\">");
				output.println("&nbsp;");
				// output.println("<img src=\"/images/previousDisabled.gif\" border=\"0\">");
			}

			output.println("</td>");

			output.println("<td align=\"center\" valign=\"center\">");

			// output.println("<font size=2 face=arial color=red> files " + (selectionStatus.getBeginIndex()+1) + " to " + selectionStatus.getEndIndex()  + " of " + file_num + " </font>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
			output.println("&nbsp;");

			output.println("</td>");

			output.println("<td align=\"right\" valign=\"center\">");

			if (!selectionStatus.getIsLastPage())
			{
				output.print("<a href=\"/webfilesys/servlet?command=pictureStory");
				output.print("&afterName=" + UTF8URLEncoder.encode(selectionStatus.getLastFileName()) + "\">");
				output.println("<img src=\"/webfilesys/images/next.gif\" border=\"0\"></a>");

				output.println("&nbsp;");

				output.print("<a href=\"/webfilesys/servlet?command=pictureStory");
				output.print("&beforeName=" + UTF8URLEncoder.encode("zzzzzzzzzz") + "\">");
				output.println("<img src=\"/webfilesys/images/last.gif\" border=\"0\"></a>");
			}
			else
			{
				// output.println("<img src=\"/images/nextDisabled.gif\" border=\"0\">");
				output.println("&nbsp;");
				// output.println("<img src=\"/images/lastDisabled.gif\" border=\"0\">");
			}

			output.println("</td>");
			output.println("</tr>");
			output.println("</table>");
			// end paging bottom
        }

		output.println("</body></html>");
		output.flush();
	}
}
