package de.webfilesys.gui.user;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.webfilesys.MP3ExtractorThread;
import de.webfilesys.MP3V2Info;
import de.webfilesys.MetaInfManager;
import de.webfilesys.WebFileSys;
import de.webfilesys.gui.xsl.mobile.MobileFolderFileListHandler;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.UTF8URLEncoder;

/**
 * @author Frank Hoehnel
 */
public class EditMP3RequestHandler extends UserRequestHandler
{
	public EditMP3RequestHandler(
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
		
		String title = getParameter("title");
		
		if (title == null)
		{
			editMP3Form();
			
			return;
		}

		String path=getParameter("path");

		if (!checkAccess(path))
		{
			return;
		}

		MP3V2Info mp3Info = new MP3V2Info(path);

		if (title!=null)
		{
			mp3Info.setTitle(title);
		}

		String tmp=getParameter("artist");
		if (tmp!=null)
		{
			mp3Info.setArtist(tmp);
		}

		tmp=getParameter("album");
		if (tmp!=null)
		{
			mp3Info.setAlbum(tmp);
		}

		tmp=getParameter("year");
		if (tmp!=null)
		{
			mp3Info.setPublishYear(tmp);
		}

		tmp=getParameter("comment");
		if (tmp!=null)
		{
			mp3Info.setComment(tmp);
		}

		tmp=getParameter("genre");
		if (tmp!=null)
		{
			try
			{
				mp3Info.setGenreCode(Integer.parseInt(tmp));
			}
			catch (NumberFormatException nfex)
			{
			}
		}

		mp3Info.store();

		MetaInfManager metaInfMgr=MetaInfManager.getInstance();

		metaInfMgr.removeDescription(path);

		String parentPath=null;

		int lastSlashIdx=path.lastIndexOf(File.separatorChar);

		if (lastSlashIdx>0)
		{
			parentPath=path.substring(0,lastSlashIdx);
		}
		else
		{
			if (lastSlashIdx==0)
			{
				parentPath="/";
			}
		}

		if (parentPath!=null)
		{
			if (WebFileSys.getInstance().isAutoExtractMP3())
			{
				(new MP3ExtractorThread(parentPath)).start();
			}
		}

        String mobile = (String) session.getAttribute("mobile");
        
        if (mobile != null) 
        {
            (new MobileFolderFileListHandler(req, resp, session, output, uid)).handleRequest(); 
        }
        else
        {
            output.println("<html>");
            output.println("<head>");

            output.println("<script language=\"javascript\">");
            output.println("self.close();");
            output.println("</script>");

            output.println("</head>"); 
            output.println("</html>");
            output.flush();
        }
	}
	
	protected void editMP3Form()
	{
		String path=getParameter("path");

		if (!checkAccess(path))
		{
			return;
		}

		output.println("<HTML>");
		output.println("<HEAD>");

		output.println("<TITLE>" + getResource("label.editmp3","Edit MP3 Tags") + "</TITLE>");

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/css/" + userMgr.getCSS(uid) + ".css\">");

		output.println("</HEAD>"); 
		output.println("<BODY>");

		headLine(CommonUtils.shortName(this.getHeadlinePath(path), 50));

		MP3V2Info mp3Info = new MP3V2Info(path);

		output.println("<form accept-charset=\"utf-8\" name=\"form1\" method=\"get\" action=\"/webfilesys/servlet\">");
		output.println("<input type=\"hidden\" name=\"command\" value=\"editMP3\">");
		output.println("<input type=\"hidden\" name=\"path\" value=\"" + path + "\">");

		output.println("<table class=\"dataForm\" width=\"100%\">");

		String tmp=mp3Info.getTitle();
		if (tmp==null)
		{
			tmp="";
		}

		output.println("<tr><td class=\"formParm1\">");
		output.println(getResource("label.mp3title","Song Title"));
		output.println("</td><td class=\"formParm2\">");
		output.println("<input type=\"text\" name=\"title\" value=\"" + tmp + "\" maxlength=\"30\" style=\"width:230\">");
		output.println("</td></tr>");

		tmp=mp3Info.getArtist();
		if (tmp==null)
		{
			tmp="";
		}

		output.println("<tr><td class=\"formParm1\">");
		output.println(getResource("label.mp3artist","Artist"));
		output.println("</td><td class=\"formParm2\">");
		output.println("<input type=\"text\" name=\"artist\" value=\"" + tmp + "\" maxlength=\"30\" style=\"width:230\">");
		output.println("</td></tr>");

		tmp=mp3Info.getAlbum();
		if (tmp==null)
		{
			tmp="";
		}

		output.println("<tr><td class=\"formParm1\">");
		output.println(getResource("label.mp3album","Album"));
		output.println("</td><td class=\"formParm2\">");
		output.println("<input type=\"text\" name=\"album\" value=\"" + tmp + "\" maxlength=\"30\" style=\"width:230\">");
		output.println("</td></tr>");

		tmp=mp3Info.getPublishYear();
		if (tmp==null)
		{
			tmp="";
		}

		output.println("<tr><td class=\"formParm1\">");
		output.println(getResource("label.mp3year","Year"));
		output.println("</td><td class=\"formParm2\">");
		output.println("<input type=\"text\" name=\"year\" value=\"" + tmp + "\" maxlength=\"4\" size=\"4\">");
		output.println("</td></tr>");

		int genreCode=mp3Info.getGenreCode();

		String genreList[]=mp3Info.getGenreList();

		output.println("<tr><td class=\"formParm1\">");
		output.println(getResource("label.mp3genre","Genre"));
		output.println("</td><td class=\"formParm2\">");
		output.println("<select name=\"genre\" size=\"1\">");
		for (int i=0;i<genreList.length;i++)
		{
			output.print("<option value=\"" + i + "\"");

			if (i==genreCode)
			{
				output.print(" selected");
			}
			output.println(">" + genreList[i] + "</option>");
		}
		output.println("</select>");
		output.println("</td></tr>");

		tmp=mp3Info.getComment();
		if (tmp==null)
		{
			tmp="";
		}

		output.println("<tr><td class=\"formParm1\">");
		output.println(getResource("label.mp3comment","Comment"));
		output.println("</td><td class=\"formParm2\">");
		output.println("<input type=\"text\" name=\"comment\" value=\"" + tmp + "\" maxlength=\"30\" style=\"width:230\">");
		output.println("</td></tr>");

		if (mp3Info.isPictureIncluded())
		{
		    String imgUrl = "/webfilesys/servlet?command=mp3Thumb&path=" + UTF8URLEncoder.encode(path);
	        output.println("<tr><td class=\"formParm1\">");
	        output.println(getResource("label.mp3Picture", "picture"));
	        output.println("</td><td class=\"formParm2\">");
            output.println("<img src=\"" + imgUrl + "\" border=\"0\" style=\"border:1px solid #808080\"/>");
            output.println("</td></tr>");
		}
		
		output.println("<tr><td class=\"formButton\">");
		output.println("<input type=\"submit\" value=\"" + getResource("button.save","Save") + "\">");
		output.println("</td><td class=\"formButton\" align=\"right\">");
		
        String mobile = (String) session.getAttribute("mobile");
        
        if (mobile != null) 
        {
            output.println("<input type=\"button\" value=\"" + getResource("button.close","Cancel") + "\" onclick=\"window.location.href='/webfilesys/servlet?command=listFiles';\">");
        }
        else
        {
            output.println("<input type=\"button\" value=\"" + getResource("button.close","Cancel") + "\" onclick=\"self.close()\">");
        }
		output.println("</td></tr>");
		output.println("</table>");
		output.println("</form>");

		output.println("</BODY></html>");
		output.flush();
	}
}
