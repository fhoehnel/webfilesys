package de.webfilesys.gui.user;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.graphics.ImageTextStamp;
import de.webfilesys.graphics.ScaledImage;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.UTF8URLEncoder;

/**
 * @author Frank Hoehnel
 */
public class ResizeParmsRequestHandler extends UserRequestHandler
{
	public ResizeParmsRequestHandler(
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

		String actPath = getCwd();

		String imgFileName=getParameter("imgFile");
		
		if (imgFileName == null)
		{
			// save selected files in session
			
	        Vector selectedFiles = new Vector();

	        Enumeration allKeys = req.getParameterNames();

	        while (allKeys.hasMoreElements())
	        {
	            String parmKey = (String) allKeys.nextElement();

	            if (parmKey.startsWith("list-"))
	            {
	                selectedFiles.add(parmKey.substring(5));
	            }
	        }

	        session.setAttribute("selectedFiles", selectedFiles);
		}
		
		// resize in popup window (called from showImage) ?
		String popup = getParameter("popup");

		output.println("<html>");
		output.println("<head>");

		output.println("<title>WebFileSys - " + getResource("label.resizetitle","resize pictures") + "</title>");

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/css/" + userMgr.getCSS(uid) + ".css\">");

		output.println("<script src=\"/webfilesys/javascript/areaSelector.js\" type=\"text/javascript\"></script>");
		
        output.println("<script src=\"/webfilesys/javascript/resizeImage.js\" type=\"text/javascript\"></script>");
		
        if ((imgFileName != null) && (popup == null)) 
        {
    		output.println("<script type=\"text/javascript\">");
    		
            output.println("var cropInitialized = false;");

            output.println("</script>");
        }
		
		output.println("</head>");
		
		output.println("<body>");

		headLine(getResource("label.resizetitle","resize pictures"));

		output.println("<br/>");
		
		output.println("<table class=\"dataForm\" border=\"0\" width=\"100%\">");

		output.println("<tr>");
		output.println("<td valign=\"top\">");
		
		output.println("<form accept-charset=\"utf-8\" name=\"form1\" method=\"get\" action=\"/webfilesys/servlet\">");
		output.println("<input type=\"hidden\" name=\"command\" value=\"resizeImages\">");

		if (imgFileName == null)
		{
			output.println("<input type=\"hidden\" name=\"actPath\" value=\"" + actPath + "\">");

			String shortPath = CommonUtils.shortName(getHeadlinePath(actPath), 50);

			output.println("<table border=\"0\">");
			output.println("<tr><td class=\"formParm1\">");

			output.println(getResource("label.selectedpictures","selected pictures in") + ":");

			output.println("</td></tr>");
			output.println("<tr><td class=\"formParm2\">");
			output.println(shortPath);
			output.println("</td></tr>");
			output.println("</table>");
		}
		else
		{
			output.println("<input type=\"hidden\" name=\"imgFile\" value=\"" + imgFileName + "\">");
            if (popup == null)
            {
                output.println("<input type=\"hidden\" name=\"cropAreaLeft\" value=\"\">");
                output.println("<input type=\"hidden\" name=\"cropAreaTop\" value=\"\">");
                output.println("<input type=\"hidden\" name=\"cropAreaWidth\" value=\"\">");
                output.println("<input type=\"hidden\" name=\"cropAreaHeight\" value=\"\">");
            }
                
			if (popup != null)
			{
				output.println("<input type=\"hidden\" name=\"popup\" value=\"true\">");
			}

			String shortFileName = CommonUtils.shortName(getHeadlinePath(imgFileName), 50);
			
			output.println("<table border=\"0\">");
			output.println("<tr><td class=\"formParm1\">");
			output.println(getResource("label.picturefile","picture file") + ":");
			output.println("</td></tr>");
			output.println("<tr><td class=\"formParm2\">");
			output.println(shortFileName);
			output.println("</td></tr>");
			
	        if (popup == null)
	        {
	            output.println("<tr><td colspan=\"2\" class=\"formParm1\">");
	            output.println("<input type=\"checkbox\" name=\"crop\" class=\"formParm1\" onclick=\"if (!cropInitialized) {initAreaSelector('editPicture');cropInitialized=true;}\">");
	            output.println(getResource("label.cropArea", "Select crop area") + ":");
                output.println("</td>");
                output.println("</tr>");
	            
                output.println("<tr>");

	            output.println("<td valign=\"top\" style=\"padding-top:6px;padding-bottom:6px\">");

	            try
	            {
	                ScaledImage scaledImage = new ScaledImage(imgFileName, 400, 400);
	                
	                String srcFileName = imgFileName;

	                srcFileName = "/webfilesys/servlet?command=getFile&filePath=" + UTF8URLEncoder.encode(imgFileName);

                    int xDisplay = scaledImage.getScaledWidth();
                    int yDisplay = scaledImage.getScaledHeight();
	                
	                output.print("<img id=\"editPicture\" class=\"thumb\" src=\"" + srcFileName + "\" width=\"" + xDisplay + "\" height=\"" + yDisplay + "\">");
	            }
	            catch (IOException ioEx)
	            {
	                Logger.getLogger(getClass()).error(ioEx);
	            }
	            
                output.println("</td>");
                output.println("</tr>");
	        }
			
			output.println("</table>");
		}

		output.println("<table border=\"0\" cellspacing=\"0\">");
		
		output.println("<tr><td colspan=2>&nbsp;</td></tr>");

		output.println("<tr>");
		output.println("<td class=\"formParm1\">" + getResource("label.newsize","new image size") + ":</td>");
		output.println("<td class=\"formParm2\">");
		output.println("<select name=\"newSize\" size=\"1\" style=\"width:140px;\" onchange=\"handleTargetSizeSelection(this)\">");
		output.println("<option value=\"0\" selected>" + getResource("label.keepOrigSize","no resize") + "</option>");
		output.println("<option value=\"100\">100</option>");
		output.println("<option value=\"200\">200</option>");
		output.println("<option value=\"320\">320</option>");
		output.println("<option value=\"400\">400</option>");
		output.println("<option value=\"500\">500</option>");
		output.println("<option value=\"640\">640</option>");
		output.println("<option value=\"800\">800</option>");
		output.println("<option value=\"1024\">1024</option>");
		output.println("<option value=\"1280\">1280</option>");
		output.println("<option value=\"1600\">1600</option>");
		output.println("<option value=\"-1\">" + getResource("resizeDifferentSize", "different size") + "</option>");
		output.println("</select>");
		output.println("</td></tr>");

		output.println("<tr id=\"targetSizeRow\" style=\"display:none\">");
		output.println("<td></td>");
		output.println("<td class=\"formParm2\">");
		output.println("<input id=\"targetSize\" name=\"targetSize\" type=\"text\" style=\"width:140px;\"/>");
		output.println("</td>");
		output.println("</tr>");
		
		output.println("<tr><td colspan=2>&nbsp;</td></tr>");

		output.println("<tr>");
		output.println("<td class=\"formParm1\">" + getResource("label.newformat","new image format") + ":</td>");
		output.println("<td class=\"formParm2\">");
		output.println("<select name=\"format\" size=1>");
		output.println("<option value=\"JPEG\">JPEG</option>");
		output.println("<option value=\"PNG\">PNG</option>");
		output.println("<option value=\"GIF\">GIF</option>");
		output.println("</select>");
		output.println("</td></tr>");

        output.println("<tr>");
		output.println("<td colspan=\"2\">");
		
		output.println("<table class=\"formSection\">");  // start formSection for copyright text

		output.println("<tr>");
		output.println("<td colspan=\"2\" class=\"formParm1\">");
		output.println("<input type=\"checkbox\" name=\"stampText\" class=\"formParm1\" onclick=\"switchCopyRightFields()\">");
		output.println(getResource("label.addCopyRight","add copyright text"));
		output.println("</td>");
		output.println("</tr>");

		// output.println("<tr><td colspan=\"2\">&nbsp;</td></tr>");
		
		output.println("<tr>");
		output.println("<td class=\"formParm1\" colspan=\"2\">" + getResource("label.copyRightText","copyright text") + ":</td>");
		output.println("</tr>");
		output.println("<tr>");
		output.println("<td class=\"formParm2\" colspan=\"2\">");
		output.println("<input type=\"text\" name=\"copyRightText\" maxlength=\"128\" style=\"width:300px\" disabled=\"disabled\">");
		output.println("</td>");
		output.println("</tr>");

		// output.println("<tr><td colspan=2>&nbsp;</td></tr>");

		output.println("<tr>");
		output.println("<td class=\"formParm1\">" + getResource("label.copyRightPos","position of copyright") + ":</td>");
		output.println("<td class=\"formParm2\">");
		output.println("<select name=\"copyRightPos\" size=\"1\" disabled=\"disabled\">");
		output.println("<option value=\"" + ImageTextStamp.TEXT_POS_UPPER_LEFT + "\">" + getResource("label.posUpperLeft", "upper left corner") + "</option>");
		output.println("<option value=\"" + ImageTextStamp.TEXT_POS_UPPER_RIGHT + "\">" + getResource("label.posUpperRight", "upper right corner") + "</option>");
		output.println("<option value=\"" + ImageTextStamp.TEXT_POS_LOWER_LEFT + "\">" + getResource("label.posLowerLeft", "lower left corner") + "</option>");
		output.println("<option value=\"" + ImageTextStamp.TEXT_POS_LOWER_RIGHT + "\">" + getResource("label.posLowerRight", "lower right corner") + "</option>");
		output.println("</select>");
		output.println("</td></tr>");

		// output.println("<tr><td colspan=2>&nbsp;</td></tr>");

		output.println("<tr>");
		output.println("<td class=\"formParm1\">" + getResource("label.copyRightColor","copyright text color") + ":</td>");
		output.println("<td class=\"formParm2\">");
		output.println("<select name=\"copyRightColor\" size=\"1\" disabled=\"disabled\">");
		output.println("<option value=\"000000\">" + getResource("label.colorBlack", "black") + "</option>");
		output.println("<option value=\"0000ff\">" + getResource("label.colorBlue", "blue") + "</option>");
		output.println("<option value=\"ff0000\">" + getResource("label.colorRed", "red") + "</option>");
		output.println("<option value=\"00ff00\">" + getResource("label.colorGreen", "green") + "</option>");
		output.println("<option value=\"ffff00\">" + getResource("label.colorYellow", "yellow") + "</option>");
		output.println("<option value=\"ffffff\">" + getResource("label.colorWhite", "white") + "</option>");
		output.println("</select>");
		output.println("</td></tr>");
		
		// output.println("<tr><td colspan=2>&nbsp;</td></tr>");

		output.println("<tr>");
		output.println("<td class=\"formParm1\">" + getResource("label.copyRightFontSize","copyright font size") + ":</td>");
		output.println("<td class=\"formParm2\">");
		output.println("<select name=\"copyRightFontSize\" size=\"1\" disabled=\"disabled\">");
		output.println("<option value=\"10\">10</option>");
		output.println("<option value=\"12\">12</option>");
		output.println("<option value=\"14\">14</option>");
		output.println("<option value=\"16\">16</option>");
		output.println("<option value=\"18\">18</option>");
		output.println("<option value=\"20\" selected>20</option>");
		output.println("<option value=\"22\">22</option>");
		output.println("<option value=\"24\">24</option>");
		output.println("<option value=\"26\">26</option>");
		output.println("<option value=\"28\">28</option>");
		output.println("<option value=\"30\">30</option>");
		output.println("</select>");
		output.println("</td></tr>");
		
		output.println("</table>"); // end formSection for copyright text
		
		output.println("</td>");
		output.println("</tr>");
		
		output.println("<tr>");
		output.println("<td class=\"formParm1\">");
		output.println("<input type=\"submit\" name=\"start\" class=\"formButton\" value=\"" + getResource("button.start","Start") + "\"");
        if ((imgFileName != null) && (popup == null)) 
        {
            output.println(" onclick=\"if (cropInitialized) {prepareCropAreaParms();}\"");
        }
        output.println(">");
		output.println("</td>");
		
		output.println("<td class=\"formParm2\" align=\"right\">");
		
		if (popup != null)
		{        
			output.println("<input type=\"button\" class=\"formButton\" value=\"" + getResource("button.cancel", "Cancel") + "\" onclick=\"self.close()\">");
		}
		else
		{
			output.println("<input type=\"button\" class=\"formButton\" name=\"cancel\" value=\"" + getResource("button.cancel","Cancel") + "\" onclick=\"window.location.href='/webfilesys/servlet?command=listFiles';\">");
		}
		
        output.println("</td></tr>");

		output.println("</table>");
		output.println("</form>");
		
		output.println("</td>");
		
		output.println("</td>");
		
		output.println("</tr>");
		output.println("</table>");
		
		output.println("</body>");
		
        if ((imgFileName != null) && (popup == null)) 
        {
            output.print("<div id=\"areaSelector\" onmousedown=\"startResize(this,event)\" onmouseover=\"document.body.style.cursor='crosshair'\"");
            output.print(" onmouseout=\"document.body.style.cursor='default'\""); 
            output.println(" style=\"position:absolute;top:100px;left:10px;border-style:groove;border-width:2px;border-color:black;padding:3px;visibility:hidden;\">");
            output.print("<div id=\"selectedArea\""); 
            output.println(" onmousedown=\"startDrag(this);event.cancelBubble=true;\" style=\"border-style:none;visibility:hidden;\"/>"); 
            output.println("</div>");
        }
		
        output.println("</html>");
		
		output.flush();
	}

}
