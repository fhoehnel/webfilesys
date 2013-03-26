package de.webfilesys.gui.user;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.graphics.ScaledImage;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.UTF8URLEncoder;

/**
 * @author Frank Hoehnel
 */
public class CompareImageRequestHandler extends MultiImageRequestHandler
{
	public CompareImageRequestHandler(
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
		String actPath = (String) session.getAttribute("cwd");

		if (selectedFiles.size() > 0)
		{
			Collections.sort(selectedFiles);
		}

		String pathWithSlash = actPath;

		if (!actPath.endsWith(File.separator))
		{
			pathWithSlash = actPath + File.separator;
		}

		output.println("<HTML>");
		output.println("<HEAD>");
		output.println("<TITLE>WebFileSys - " + getResource("label.comparehead","Compare Images") + "</TITLE>");

        output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/css/" + userMgr.getCSS(uid) + ".css\">");

        output.println("<style type=\"text/css\">");
		output.println("body {margin-top:0px;margin-left:0px;background-color:white;}");
		output.println("td {font-size:8pt;color:black;font-family:Arial;}");
		output.println("</style>");
		
        output.println("<script type=\"text/javascript\">");
        output.println("function confirmDelImg(imgFileName)");
        output.println("{");
        output.println("if (confirm('" + getResource("confirm.delfile", "Are you sure you want to delete this file?") + "'))");
        output.println("{");
        output.println("window.location.href = '/webfilesys/servlet?command=fmdelete&fileName=' + encodeURIComponent(imgFileName) + '&closeWin=true&deleteRO=yes';");
        output.println("}");
        output.println("}");
        output.println("</script>");

		output.println("</head>");
		output.println("<body>");

		output.println("<table border=\"0\" width=\"100%\" cellpadding=\"0\" cellspacing=\"1\">");

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
		
		int displayWidth = 770;

		Integer screenWidth = (Integer) session.getAttribute("screenWidth");
		
		if (screenWidth != null)
		{
			displayWidth = screenWidth.intValue() - 20;
		}
		
		int displayHeight = 520;

		Integer screenHeight = (Integer) session.getAttribute("screenHeight");
		
		if (screenHeight != null)
		{
			displayHeight = screenHeight.intValue() - 130;
		}

		int fileNum = selectedFiles.size();

		int imgWidth[]=new int[fileNum];

		int widthSum=0;
		
		for (int i=0;i<fileNum;i++)
		{
			String imgFileName=pathWithSlash + (String) selectedFiles.elementAt(i);

			ScaledImage scaledImage=null;

			try
			{
				scaledImage=new ScaledImage(imgFileName,displayWidth,displayHeight);

				if (scaledImage.getScaledHeight() < scaledImage.getRealHeight())
				{
					imgWidth[i]=scaledImage.getScaledWidth();
				}
				else
				{
					imgWidth[i]=scaledImage.getRealWidth();
				}

				widthSum+=imgWidth[i];
			}
			catch (IOException io1)
			{
				Logger.getLogger(getClass()).error(io1);
			}
		}

		if (widthSum > displayWidth)
		{
			for (int i=0;i<fileNum;i++)
			{
				int scale=imgWidth[i] * 100 / widthSum;

				imgWidth[i]=(scale * displayWidth) / 100;
			}
		}

		int maxHeight=0;

		output.println("<tr>");
		
		for (int i=0;i<fileNum;i++)
		{
			output.println("<td style=\"vertical-align:top;text-align:center\">");

			String imgFileName=pathWithSlash + (String) selectedFiles.elementAt(i);

			ScaledImage scaledImage=null;

			try
			{
				scaledImage=new ScaledImage(imgFileName,imgWidth[i],displayHeight);

				if (scaledImage.getScaledHeight() > maxHeight)
				{
					maxHeight=scaledImage.getScaledHeight();
				}
			}
			catch (IOException io1)
			{
				Logger.getLogger(getClass()).error(io1);
				output.println("</body></html>");
				output.flush();
				return;
			}

			String srcFileName = "/webfilesys/servlet?command=getFile&filePath=" + UTF8URLEncoder.encode(imgFileName);

			output.println("<img src=\"" + srcFileName + "\" width=" + scaledImage.getScaledWidth() + " height=" + scaledImage.getScaledHeight() + " alt=\"" + imgFileName + "\" border=\"1\">");

			output.println("</td>");
		}

		output.println("</tr>");
		output.println("<tr>");

		for (int i = 0; i < fileNum; i++)
		{
		    String imgFileName = (String) selectedFiles.elementAt(i);
		    
			output.print("<td style=\"vertical-align:top;text-align:center\"");
			if (imgFileName.length() > 30) 
			{
			    output.print(" title=\"" + imgFileName + "\"");
			}
			output.println(">");
			
			output.println(CommonUtils.shortName(imgFileName, 30));
			
			if (!readonly) {
	            output.println("<a href=\"javascript:confirmDelImg('" + imgFileName + "')\"><img src=\"/webfilesys/images/trash.gif\" width=\"17\" height=\"16\" border=\"0\" title=\"" + getResource("alt.delpicture", "delete image file") + "\"/></a>");
			}
			
			output.println("</td>");
		}		
		
		output.println("</tr>");
		
		output.println("</table>");

		int windowHeight=maxHeight + 130;

		output.println("<script language=\"javascript\">");
		output.println("if (" + windowHeight + " < (screen.height-30))");
		output.println("{window.resizeTo(screen.width," + (windowHeight - 30) + ");}");
		output.println("else");
		output.println("{window.resizeTo(screen.width,screen.height-30);}");
		output.println("self.focus();");
		output.println("</script>");

		output.println("</body>");
		
        output.println("</html>");
		
		output.flush();
	}

}
