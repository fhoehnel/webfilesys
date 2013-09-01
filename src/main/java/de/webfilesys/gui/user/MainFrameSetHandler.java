package de.webfilesys.gui.user;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.webfilesys.WebFileSys;
import de.webfilesys.WinDriveManager;
import de.webfilesys.util.UTF8URLEncoder;
 
/**
 * The main frameset.
 * @author Frank Hoehnel
 */
public class MainFrameSetHandler extends UserRequestHandler
{
	boolean clientIsLocal = false;
	 
	public MainFrameSetHandler(
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
	    session.removeAttribute("mobile");
	    
        String act_path = getParameter("actPath");

        if ((act_path == null) || (act_path.length() == 0))
        {
            if (File.separatorChar == '\\')
            {
                act_path = "C:\\";

                boolean existingDriveFound = false;
                for (int i = 3; (!existingDriveFound) &&  (i <= 26); i++) {
                    if (WinDriveManager.getInstance().getDriveLabel(i) != null) {
                        existingDriveFound = true;

                        char driveChar = 'A';
                        driveChar += (i - 1);
                        act_path = driveChar + ":" + File.separator;
                    }
                }
            }
            else
            {
                act_path = "/";
            }
        }
        
		String viewModeParm = this.getParameter("viewMode");
        	
		if (viewModeParm != null)
		{
			try
			{
				int viewMode = Integer.parseInt(viewModeParm);

                session.setAttribute("viewMode", new Integer(viewMode));
			}
			catch (NumberFormatException nfex)
			{
			}
		}

        if (WebFileSys.getInstance().isMaintananceMode())
        {
            if (!this.isAdminUser(false))
            {
                maintananceMode();
                return;
            }
        }

        if (!accessAllowed(act_path))
        {
            if (File.separatorChar == '\\')
            {
                act_path = userMgr.getDocumentRoot(uid).replace('/', '\\');
            }
            else
            {
                act_path = userMgr.getDocumentRoot(uid);
            }
        }

        output.println("<html>");
        output.println("<head>");
        
		output.println("<link rel=\"SHORTCUT ICON\" href=\"/webfilesys/images/favicon.ico\">");

		// global JavaScript variables needed for context menu
		output.println("<script language=\"javascript\">");
	
		if (File.separatorChar == '/')
		{
			output.println("var serverOS = 'ix';");
		}
		else
		{
			output.println("var serverOS = 'win';");
		}
	
		if (clientIsLocal)
		{
			output.println("var clientIsLocal = 'true';");
		}
		else
		{
			output.println("var clientIsLocal = 'false';");
		}
	
		if (readonly)
		{
			output.println("var readonly = 'true';");
		}
		else
		{
			output.println("var readonly = 'false';");
		}
	
		String role = userMgr.getRole(uid);
		if ((role != null) && role.equals("webspace"))
		{
			output.println("var webspaceUser = 'true';");
		}
		else
		{
			output.println("var webspaceUser = 'false';");
		}

		if (WebFileSys.getInstance().getMailHost() != null)
		{
			output.println("var mailEnabled = 'true';");
		}
		else
		{
			output.println("var mailEnabled = 'false';");
		}
		
		if (WebFileSys.getInstance().isAutoCreateThumbs())
		{
			output.println("var autoCreateThumbs = 'true';");
		}
        else
        {
			output.println("var autoCreateThumbs = 'false';");
        }
        
        if (isAdminUser(false))
        {
			output.println("var adminUser = 'true';");
        }
        else
        {
			output.println("var adminUser = 'false';");
        }
        
        if (WebFileSys.getInstance().isChmodAllowed())
        {
			output.println("var chmodAllowed = 'true';");
        }
        else
        {
			output.println("var chmodAllowed = 'false';");
        }

        output.println("var resourceDelDirStarted = '" + getResource("msg.delDirStarted","Delete operation for folder started.") + "';");

        if (!readonly)
		{
            if (WebFileSys.getInstance().isFolderWatch()) {
                output.println("var watchEnabled = true;");
            }
		}
		
        output.println("var diffStarted = false;");
        output.println("var syncStarted = false;");
        output.println("var compStarted = false;");
        
		output.println("</script>");

        output.println(
            "<title> WebFilesys: "
                + WebFileSys.getInstance().getLocalHostName()
                + " ("
                + WebFileSys.getInstance().getOpSysName()
                + ") - "
                + WebFileSys.VERSION
                + "</title>");

        // output.println("<frameset rows=\"32,*\" frameborder=\"0\" framespacing=\"0\" border=\"0\">");
        output.println("<frameset rows=\"32,*\">");
        output.println(
            "<frame name=\"menu\" scrolling=\"no\" src=\"/webfilesys/servlet?command=menuBar\" leftmargin=\"0\" topmargin=\"0\" marginwidth=\"0\" marginheight=\"0\" frameborder=\"0\" noresize />");

        if (File.separatorChar == '/')
        {
            output.print("<frameset COLS=\"33%,*\">");

            output.println(
                    "<frame name=\"DirectoryPath\" SRC=\"/webfilesys/servlet?command=exp&expandPath="
                        + UTF8URLEncoder.encode(act_path)
                        + "\" scrolling=\"auto\" />");

            output.print(
                "<frame name=\"FileList\" SRC=\"/webfilesys/servlet?command=listFiles&actpath="
                    + UTF8URLEncoder.encode(act_path)
                    + "&mask=*\" />");
            output.print("</frameset>");
        }
        else
        {
            output.println("<frameset cols=\"33%,*\">");

            String docRoot = userMgr.getDocumentRoot(uid);

            String fastPath = getParameter("fastPath");
            
            if ((fastPath != null) || (docRoot.charAt(0) != '*'))
            {
                // return to previous folder, expand it
                output.println(
                        "<frame name=\"DirectoryPath\" src=\"/webfilesys/servlet?command=exp&expandPath="
                            + UTF8URLEncoder.encode(act_path)
                            + "\" scrolling=\"auto\" />");
            }
            else
            {
                output.println(
                        "<frame name=\"DirectoryPath\" src=\"/webfilesys/servlet?command=winDirTree&actPath="
                            + UTF8URLEncoder.encode(act_path)
                            + "\" scrolling=\"auto\" />");
            }

            output.println(
                "<frame name=\"FileList\" SRC=\"/webfilesys/servlet?command=listFiles&actpath="
                    + UTF8URLEncoder.encode(act_path)
                    + "&mask=*\" scrolling=\"auto\" />");
            output.println("</frameset>");
        }

        output.println("</frameset>");

        output.println("</head>");
        output.println("</html>");
        output.flush();
        return;
	}
	
    private void maintananceMode()
    {
        output.println("<html>");
        output.println("<head>");

        // output.println("<META HTTP-EQUIV=\"REFRESH\" CONTENT=\"20; URL=/_logout\">");

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/common.css\">");
		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/skins/" + userMgr.getCSS(uid) + ".css\">");

        output.println("</head>");
        output.println("<body>");

        headLine(getResource("label.maintanance.head", "Maintanance Mode"));

        output.println("<br><br>");

        output.println(
            getResource(
                "label.maintanance.info",
                "The server is temporary not available due to maintanance. Please try again in a few minutes!"));

        output.println("</body>");
        output.println("</html>");
        output.flush();

		session.removeAttribute("userid");

    	session.invalidate();
    }

}