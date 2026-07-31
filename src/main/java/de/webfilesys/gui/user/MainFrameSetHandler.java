package de.webfilesys.gui.user;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.webfilesys.WebFileSys;
import de.webfilesys.WebFileSysConfig;
import de.webfilesys.WinDriveManager;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.UTF8URLEncoder;
 
/**
 * The main frameset.
 * @author Frank Hoehnel
 */
public class MainFrameSetHandler extends UserRequestHandler {
	boolean clientIsLocal;
	 
	public MainFrameSetHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid,
            boolean clientIsLocal) {
        super(req, resp, session, output, uid);
		this.clientIsLocal = clientIsLocal;
	}

	protected void process() {
	    session.removeAttribute("mobile");

        if (WebFileSys.getInstance().isMaintananceMode()) {
            if (!this.isAdminUser(false)) {
                maintananceMode();
                return;
            }
        }

        String currentPath = getParameter("actPath");

        if (CommonUtils.isEmpty(currentPath)) {
            if (File.separatorChar == '\\') {
                currentPath = "C:\\";

                boolean existingDriveFound = false;
                for (int i = 3; (!existingDriveFound) &&  (i <= 26); i++) {
                    if (WinDriveManager.getInstance().getDriveLabel(i) != null) {
                        existingDriveFound = true;

                        char driveChar = 'A';
                        driveChar += (i - 1);
                        currentPath = driveChar + ":" + File.separator;
                    }
                }
            } else {
                currentPath = "/";
            }
        }
        
		String viewModeParm = this.getParameter("viewMode");
		if (viewModeParm != null) {
			try {
				int viewMode = Integer.parseInt(viewModeParm);
                session.setAttribute("viewMode", viewMode);
			} catch (NumberFormatException nfex) {
			}
		}

        if (!accessAllowed(currentPath)) {
            if (File.separatorChar == '\\') {
                currentPath = userMgr.getDocumentRoot(uid).replace('/', '\\');
            } else {
                currentPath = userMgr.getDocumentRoot(uid);
            }
        }

        output.println("<!DOCTYPE html>");
        output.println("<html>");
        output.println("<head>");
        
        output.println("<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\" />");
        
		output.println("<link rel=\"SHORTCUT ICON\" href=\"/webfilesys/images/favicon.ico\" />");
		
		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/common.css\">");

		output.println("<script type=\"text/javascript\">");
	
		if (File.separatorChar == '/') {
			output.println("var serverOS = 'ix';");
		} else {
			output.println("var serverOS = 'win';");
		}

        printBooleanJavascriptVar("clientIsLocal", clientIsLocal);
        printBooleanJavascriptVar("localEditor", clientIsLocal && WebFileSysConfig.getInstance().getSystemEditor() != null);
        printBooleanJavascriptVar("readonly", readonly);

        String role = userMgr.getRole(uid);
        printBooleanJavascriptVar("webspaceUser", "webspace".equals(role));

        printBooleanJavascriptVar("mailEnabled", WebFileSysConfig.getInstance().getMailHost() != null);
        printBooleanJavascriptVar("autoCreateThumbs", WebFileSysConfig.getInstance().isAutoCreateThumbs());
        printBooleanJavascriptVar("adminUser", isAdminUser(false));

        printBooleanJavascriptVar("chmodAllowed", WebFileSysConfig.getInstance().isChmodAllowed());

        if (!CommonUtils.isEmpty(WebFileSysConfig.getInstance().getFfmpegExePath())) {
			output.println("var ffmpegEnabled = true;");
        }
        
        if (!readonly) {
            if (WebFileSysConfig.getInstance().isFolderWatch()) {
                output.println("var watchEnabled = true;");
            }
		}
		
        output.println("var diffStarted = false;");
        output.println("var syncStarted = false;");
        output.println("var compStarted = false;");
        
		output.println("</script>");

        output.println(
            "<title> WebFileSys: "
                + WebFileSys.getInstance().getLocalHostName()
                + " ("
                + WebFileSys.getInstance().getOpSysName()
                + ") - "
                + WebFileSys.VERSION
                + "</title>");

        output.println("</head>");
        
        output.println("<frameset rows=\"35,*\">");
        output.println(
            "<frame name=\"menu\" src=\"/webfilesys/servlet?command=menuBar\" noresize />");

        if (File.separatorChar == '/') {
            output.println("<frameset cols=\"33%,*\" class=\"mainFrames\">");
            output.println(
                    "<frame name=\"DirectoryPath\" src=\"/webfilesys/servlet?command=exp&expandPath="
                        + UTF8URLEncoder.encode(currentPath)
                        + "\" />");
            output.println(
                    "<frame name=\"FileList\" src=\"/webfilesys/servlet?command=listFiles&actpath="
                            + UTF8URLEncoder.encode(currentPath) + "&mask=*\" />");
            output.println("</frameset>");
        } else {
            output.println("<frameset cols=\"33%,*\" class=\"mainFrames\">");

            String docRoot = userMgr.getDocumentRoot(uid);
            String fastPath = getParameter("fastPath");
            
            if ((fastPath != null) || (docRoot.charAt(0) != '*')) {
                // return to previous folder, expand it
                output.println("<frame name=\"DirectoryPath\" src=\"/webfilesys/servlet?command=exp&expandPath="
                        + UTF8URLEncoder.encode(currentPath) + "\" />");
            } else {
                output.println("<frame name=\"DirectoryPath\" src=\"/webfilesys/servlet?command=winDirTree&actPath="
                        + UTF8URLEncoder.encode(currentPath) + "\" />");
            }

            output.println("<frame name=\"FileList\" SRC=\"/webfilesys/servlet?command=listFiles&actpath="
                    + UTF8URLEncoder.encode(currentPath) + "&mask=*\" />");
            output.println("</frameset>");
        }

        output.println("</frameset>");
        output.println("</html>");
        output.flush();
	}

    private void printBooleanJavascriptVar(String varName, boolean value) {
        output.println("var " + varName + " = '" + value + "';");
    }

    private void maintananceMode() {
        output.println("<html>");
        output.println("<head>");

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