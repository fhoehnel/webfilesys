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
		String actPath = getCwd();

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

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/common.css\">");
        output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/skins/" + userMgr.getCSS(uid) + ".css\">");

        output.println("<script src=\"/webfilesys/javascript/browserCheck.js\" type=\"text/javascript\"></script>");
        output.println("<script src=\"/webfilesys/javascript/util.js\" type=\"text/javascript\"></script>");
        output.println("<script src=\"/webfilesys/javascript/ajaxCommon.js\" type=\"text/javascript\"></script>");
        output.println("<script src=\"/webfilesys/javascript/ajax.js\" type=\"text/javascript\"></script>");
        output.println("<script src=\"/webfilesys/javascript/showImage.js\" type=\"text/javascript\"></script>");
        
        output.println("<script src=\"/webfilesys/javascript/resourceBundle.js\" type=\"text/javascript\"></script>");
        output.println("<script src=\"/webfilesys/servlet?command=getResourceBundle&amp;lang=" + language + "\" type=\"text/javascript\"></script>");

        output.println("<script type=\"text/javascript\">");
        output.println("function confirmDelImg(imgFileName) {");
        output.println("deleteSelf(null, imgFileName);");
        output.println("}");
        output.println("</script>");

		output.println("</head>");
		output.println("<body class=\"compareImg\">");

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
			displayWidth = screenWidth.intValue() - 28;
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
		
		for (int i = 0; i < fileNum; i++)
		{
			String imgFileName = pathWithSlash + (String) selectedFiles.elementAt(i);

			ScaledImage scaledImage=null;

			try
			{
				scaledImage = new ScaledImage(imgFileName,displayWidth,displayHeight);

				imgWidth[i] = scaledImage.getRealWidth();

				widthSum += imgWidth[i];
			}
			catch (IOException io1)
			{
				Logger.getLogger(getClass()).error(io1);
			}
		}

		if (widthSum > displayWidth)
		{
			for (int i = 0; i < fileNum; i++)
			{
				imgWidth[i] = imgWidth[i] * displayWidth / widthSum;
			}
		}

		int maxHeight = 0;

		output.println("<tr>");
		
		for (int i = 0; i < fileNum; i++)
		{
			output.println("<td class=\"compareImg\">");

			String imgFileName = pathWithSlash + (String) selectedFiles.elementAt(i);

			ScaledImage scaledImage = null;

			try
			{
				scaledImage = new ScaledImage(imgFileName, imgWidth[i], displayHeight);

				if (scaledImage.getScaledHeight() > maxHeight)
				{
					maxHeight = scaledImage.getScaledHeight();
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
		    
			output.print("<td class=\"compareImg\"");
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

		int windowHeight = maxHeight + 135;

		output.println("<script>");
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
