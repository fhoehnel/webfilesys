package de.webfilesys.gui.user;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.webfilesys.FileComparator;
import de.webfilesys.FileContainer;
import de.webfilesys.FileLinkSelector;
import de.webfilesys.FileSelectionStatus;
import de.webfilesys.MetaInfManager;
import de.webfilesys.WebFileSys;
import de.webfilesys.graphics.ScaledImage;
import de.webfilesys.graphics.ThumbnailThread;
import de.webfilesys.util.UTF8URLEncoder;

/**
 * @author Frank Hoehnel
 */
public class SlideShowRequestHandler extends UserRequestHandler
{
	public static final String SLIDESHOW_BUFFER = "slideshowBuffer";

	public static final String imgFileMasks[]={"*.gif","*.jpg","*.jpeg","*.png","*.bmp"};

	public SlideShowRequestHandler(
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
		String cmd = getParameter("cmd");
		
		if ((cmd != null) && cmd.equals("getParms"))
		{
		    slideShowParmsForm();

			return;
		}
		
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
		String indexString=getParameter("imageIdx");
		String delayString=getParameter("delay");
		String fullScreen=getParameter("fullScreen");
		String autoForwardParm=getParameter("autoForward");

		boolean autoForward=((autoForwardParm!=null) && autoForwardParm.equalsIgnoreCase("true"));

		int delay = WebFileSys.getInstance().getSlideShowDelay();
		int imageIdx=0;
		try
		{
			delay=Integer.parseInt(delayString);
			imageIdx=Integer.parseInt(indexString);
		}
		catch (NumberFormatException nfe)
		{
		}

		boolean recurse=false;
		if (recurseParm.equalsIgnoreCase("true"))
		{
			recurse=true;
		}

		if (imageIdx<=0)
		{
			session.removeAttribute(SLIDESHOW_BUFFER);
			getImageTree(actPath,recurse);
			imageIdx=0;
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

		output.println("<HTML>");
		output.println("<HEAD>");
		
		Vector imageFiles=(Vector) session.getAttribute(SLIDESHOW_BUFFER);
		if ((imageFiles==null) || (imageFiles.size()==0))
		{
			output.println("<script type=\"text/javascript\">");
			output.println("alert('" + getResource("alert.nopictures","No picture files (JPG,GIF,PNG) exist in this directory") + "!');");
			output.println("self.close();");
			output.println("</script>");
			output.println("</head></html>");
			output.flush();
			return;
		}
		
		String imgFileName = (String) imageFiles.elementAt(imageIdx);
        
		String nextUrl = "/webfilesys/servlet?command=slideShow&imageIdx=" + (imageIdx+1) + "&delay=" + delay + "&recurse=" + recurseParm;

		if (fullScreen != null)
		{
			nextUrl=nextUrl + "&fullScreen=yes";
		}

		if (autoForward)
		{
			nextUrl = nextUrl + "&autoForward=true";
		}

		String pause = getParameter("pause");

		if ((pause == null) && (autoForward))
		{
			output.print("<META HTTP-EQUIV=\"REFRESH\" CONTENT=\"" + delay + "; URL=" + nextUrl + "\">");
		}

		output.println("<style type=\"text/css\">");

		output.print("body {margin-top:0px;margin-left:0px;margin-right:0px;margin-bottom:0x;text-align:center;");

		if (fullScreen != null)
		{
			output.println("background-color:black;}");
		}
		else
		{
			output.println("background-color:silver;}");
		}

		output.print("td {font-size:9pt;font-family:Arial,Helvetica;");

		if (fullScreen != null)
		{
			output.println("color:white;background-color:black;}");
		}
		else
		{
			output.println("color:black;background-color:silver;}");
		}
		output.println("</style>");

		output.println("<TITLE>" + getResource("label.slideshow","Slideshow Image") + ": " + getHeadlinePath(imgFileName) + " </TITLE>");

		output.println("<script language=\"JavaScript\" src=\"javascript/slideShowActions.js\" type=\"text/javascript\"></script>");

		output.println("</head>");
        
		output.println("<body>");
        
		output.println("<center>");

		int screenWidth = 1024;
		int screenHeight = 768;
		
		Integer sessionScreenWidth = (Integer) session.getAttribute("screenWidth");
		
		if (sessionScreenWidth != null)
		{
			screenWidth = sessionScreenWidth.intValue();
		}
		
		Integer sessionScreenHeight = (Integer) session.getAttribute("screenHeight");
		
		if (sessionScreenHeight != null)
		{
			screenHeight = sessionScreenHeight.intValue();
		}
		
		ScaledImage scaledImage=null;

		try
		{
			if (fullScreen!=null)
			{
				scaledImage=new ScaledImage(imgFileName, screenWidth - 10, screenHeight - 70);
			}
			else
			{
				scaledImage=new ScaledImage(imgFileName, screenWidth - 40, screenHeight - 110);
			}
		}
		catch (IOException io1)
		{
			System.out.println(io1);
			output.println(io1);
			output.println("</body></html>");
			output.flush();
			return;
		}

		int xDisplay=scaledImage.getScaledWidth();
		int yDisplay=scaledImage.getScaledHeight();

		String srcFileName = "/webfilesys/servlet?command=getFile&filePath=" + UTF8URLEncoder.encode(imgFileName);

        if (scaledImage.getRealHeight() < (screenHeight - 110))
        {
        	int topPos = (screenHeight - 50 - scaledImage.getScaledHeight()) / 2;
        	
        	int xpos = (screenWidth - 20 - scaledImage.getScaledWidth()) / 2;
        	
        	output.println("<div id=\"slideshowImg\" style=\"position:absolute;top:" + topPos + ";left:" + xpos + "\">");
        }

		output.println("<img src=\"" + srcFileName + "\" width=\"" + xDisplay + "\" height=\"" + yDisplay + "\" onMouseOver=\"showActionButtons()\">");

		output.println("<br/>");

		if (scaledImage.getRealHeight() < (screenHeight - 110))
		{
			output.println("</div>");
		}
        
		output.println("</center>");
		
		MetaInfManager metaInfMgr = MetaInfManager.getInstance();

		String description = metaInfMgr.getDescription(imgFileName);
		
		// action button DIV
		
		int buttonDivWidth = 110;
		
		if ((description != null) && (description.trim().length() > 0))
		{
			buttonDivWidth = 200;
		}
		
		output.println("<div id=\"buttonDiv\" style=\"position:absolute;top:10px;left:10px;width=" + buttonDivWidth + "px;height=20px;padding:5px;background-color:ivory;text-align:center;border-style:solid;border-width:1px;border-color:#000000;visibility:hidden\">");

		output.print("<a href=\"javascript:hideActionButtons()\">");
		output.print("<img src=\"/webfilesys/images/winClose.gif\" border=\"0\" style=\"float:right;\" />");
		output.print("</a>");
		
		if ((description != null) && (description.trim().length() > 0))
		{
			if (description.length() > 120)
			{
				output.println(description.substring(0,118) + " ...");
			}
			else
			{
				output.println(description);
			}

			output.println("<br/>");
		}
		
		output.println("<a href=\"javascript:self.close()\"><img src=\"/webfilesys/images/exit.gif\" border=\"0\" title=\"" + getResource("alt.exitslideshow","exit slideshow") + "\"></a>");

		if (autoForward)
		{
			nextUrl = "/webfilesys/servlet?command=slideShow&imageIdx=" + imageIdx + "&delay=" + delay + "&recurse=" + recurseParm + "&autoForward=true";
			
			if (fullScreen != null)
			{
				nextUrl = nextUrl + "&fullScreen=yes";
			}

			if (pause==null)
			{
				nextUrl=nextUrl + "&pause=true";
				output.println("<a href=\"" + nextUrl + "\"><img src=\"/webfilesys/images/pause.gif\" border=\"0\" title=\"" + getResource("alt.pause","pause slideshow") + "\"></a>");
			}
			else
			{
				output.println("<a href=\"" + nextUrl + "\"><img src=\"/webfilesys/images/go.gif\" border=\"0\" title=\"" + getResource("alt.continue","continue slideshow") + "\"></a>");
			}
		}
		else
		{
			output.println("<a href=\"" + nextUrl + "\"><img src=\"/webfilesys/images/go.gif\" border=\"0\" title=\"" + getResource("alt.continue","continue slideshow") + "\"></a>");
		}

		output.println("</div>");
		
		output.println("</body>");
		output.println("</html>");
		output.flush();
	}
	
	protected void slideShowParmsForm()
	{
		String actPath = getCwd();
		
		int screenWidth = getIntParam("screenWidth", 1024);
		int screenHeight = getIntParam("screenHeight", 768);
		
		session.removeAttribute(SLIDESHOW_BUFFER);

		session.setAttribute("screenWidth", new Integer(screenWidth));
		session.setAttribute("screenHeight", new Integer(screenHeight));

		output.println("<HTML>");
		output.println("<HEAD>");

		output.println("<script language=\"javascript\">");
		output.println("function startShow()");
		output.println("{");
		output.println("  if (document.form1.extraWin.checked)");
		output.println("  {");
		output.println("    if ((navigator.appName.indexOf('Netscape')  == -1) && (!window.opera))");
		output.println("    {");
		output.println("       showWin=window.open('/webfilesys/servlet?command=slideShow&imageIdx=0&delay=' + document.form1.delay.options[document.form1.delay.selectedIndex].value + '&recurse=' + document.form1.recurse.checked + '&autoForward=' + document.form1.autoForward.checked + '&fullScreen=yes','thumbwin','status=no,toolbar=no,menu=no,fullscreen=yes,scrollbars=no');");
		output.println("    }");
		output.println("    else");
		output.println("    {");
		output.println("      showWin=window.open('/webfilesys/servlet?command=slideShow&imageIdx=0&delay=' + document.form1.delay.options[document.form1.delay.selectedIndex].value + '&recurse=' + document.form1.recurse.checked + '&autoForward=' + document.form1.autoForward.checked,'thumbwin','status=no,toolbar=no,menu=no,width=' + (screen.width-40) + ',height=' + (screen.height-60) + ',resizable=yes,scrollbars=yes,left=0,top=0,screenX=0,screenY=0');");
		output.println("    }");
		output.println("  }");
		output.println("  else");
		output.println("  {");
		output.println("    var showWin=window.opener.parent.frames[\"FileList\"];");		
		output.println("    var windowWidth;");		
		output.println("    var windowHeigth;");		
		output.println("    if (document.all)");		
		output.println("    {  windowWidth = showWin.document.body.clientWidth;");		
		output.println("       windowHeight = showWin.document.body.clientHeight;");		
		output.println("    }");		
		output.println("    else");		
		output.println("    {  windowWidth = showWin.innerWidth;");		
		output.println("       windowHeight = showWin.innerHeight;");		
		output.println("    }");		
		if (browserManufacturer == BROWSER_MSIE)
		{
			output.println("    showWin.location.href='/webfilesys/servlet?command=slideShowInFrame&imageIdx=0&delay=' + document.form1.delay.options[document.form1.delay.selectedIndex].value + '&recurse=' + document.form1.recurse.checked + '&autoForward=' + document.form1.autoForward.checked + '&crossfade=' + document.form1.crossfade.checked + '&windowWidth=' + windowWidth + '&windowHeight=' + windowHeight;");
		}
		else
		{
			output.println("    showWin.location.href='/webfilesys/servlet?command=slideShowInFrame&imageIdx=0&delay=' + document.form1.delay.options[document.form1.delay.selectedIndex].value + '&recurse=' + document.form1.recurse.checked + '&autoForward=' + document.form1.autoForward.checked + '&windowWidth=' + windowWidth + '&windowHeight=' + windowHeight;");
		}
		output.println("  }");
		output.println("  self.close();");
		output.println("}");
		
		if (browserManufacturer == BROWSER_MSIE)
		{
			output.println("function setCrossfade(fullscreenCheckbox)");
		    output.println("{");
		    output.println("crossfadeCheckbox = document.getElementById(\"crossfade\");");
				
		    output.println("if (crossfadeCheckbox)");
		    output.println("{");
		    output.println("if (fullscreenCheckbox.checked)");
		    output.println("{");
		    output.println("crossfadeCheckbox.checked = false;");
		    output.println("crossfadeCheckbox.disabled = true;");
		    output.println("}");
		    output.println("else");
		    output.println("{");
		    output.println("crossfadeCheckbox.disabled = false;");
		    output.println("}");
		    output.println("}");
		    output.println("}");
		}
			
		output.println("</script>");

		output.println("<title>" + getResource("label.slideparmhead","Slideshow Parameters") + "</title>");

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/css/" + userMgr.getCSS(uid) + ".css\">");

		output.println("</head>");
		output.println("<body>");

		headLine(getResource("label.slideparmhead","Slideshow Parameters"));

		output.println("<br>");
		output.println("<form accept-charset=\"utf-8\" name=\"form1\" method=\"get\" action=\"/webfilesys/servlet\">");

		output.println("<input type=\"hidden\" name=\"command\" value=\"slideShow\">");
		
		output.println("<table border=0 width=\"100%\">");

		output.println("<tr><td colspan=\"2\" class=\"prompt\">");
		output.println(getResource("label.directory","base directory") + ":");
		output.println("</td></tr>");
		output.println("<tr><td colspan=\"2\" class=\"value\">");

		String shortPath=getHeadlinePath(actPath);

		if (shortPath.length() > 40)
		{
			shortPath=shortPath.substring(0,12) + "..." + shortPath.substring(shortPath.length()-24);
		}

		output.println(shortPath);
		output.println("</td></tr>");

		output.println("<tr><td colspan=\"2\">&nbsp;</td></tr>");

		output.println("<tr><td colspan=\"2\" class=\"value\"><input type=\"checkbox\" class=\"cb\" name=\"recurse\">");
		output.println("&nbsp;" + getResource("label.recurse","include subdirectories") + "</td></tr>");

		output.println("<tr><td colspan=\"2\" class=\"value\"><input type=\"checkbox\" class=\"cb\" name=\"autoForward\" checked>");
		output.println("&nbsp;" + getResource("label.autoForward","automatic forward") + "</td></tr>");

		output.println("<tr><td colspan=\"2\" class=\"prompt\">");
		output.println("&nbsp;&nbsp;&nbsp;" + getResource("label.delay","delay (seconds)") + ":&nbsp;");
		output.println("<select name=\"delay\" size=\"1\">");
		output.println("<option value=1>1");
		output.println("<option value=3>3");
		output.println("<option value=5 selected>5");
		output.println("<option value=10>10");
		output.println("<option value=20>20");
		output.println("<option value=30>30");
		output.println("<option value=60>60");
		output.println("<option value=300>300");
		output.println("</select>");
		output.println("</td></tr>");

		output.println("<tr><td colspan=\"2\" class=\"value\"><input type=\"checkbox\" class=\"cb\" name=\"extraWin\"");
		if (browserManufacturer == BROWSER_MSIE)
		{
			output.println(" onclick=\"setCrossfade(this)\"");
		}
		output.println(">");
		output.println("&nbsp;" + getResource("label.fullScreen","fullscreen") + "</td></tr>");

		if (browserManufacturer == BROWSER_MSIE)
		{
			output.println("<tr>");
			output.println("<td colspan=\"2\" class=\"value\">");
			output.println("<input type=\"checkbox\" class=\"cb\" name=\"crossfade\" id=\"crossfade\">");
			output.println("&nbsp;" + getResource("label.crossfade","crossfade"));
			output.println("</td></tr>");
		}
		
		output.println("<tr><td colspan=2>&nbsp;</td></tr>");

		output.println("<tr><td><input type=button name=\"start\" value=\"" + getResource("button.startshow","start slideshow") + "\" onclick=\"javascript:startShow();\"></td>");
		output.println("<td align=right><input type=button name=\"cancel\" value=\"" + getResource("button.cancel","cancel") + "\" onclick=\"javascript:self.close();\"></td></tr>");

		output.println("</table>");
		output.println("</form>");
		output.println("<body></html>");
		output.flush();
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
