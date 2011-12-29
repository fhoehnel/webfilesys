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

        // store the file context menu resources in Javascript variables			
		output.println("var resourceViewZip = '" + getResource("label.viewzip","View ZIP Content") + "';");

		output.println("var resourceView = '" + getResource("label.view","View") + "';");

		output.println("var resourcePlay = '" + getResource("label.play","Play/Download") + "';");

		output.println("var resourceDownload = '" + getResource("label.download","Download") + "';");

		output.println("var resourceDelete = '" + getResource("label.delete","Delete") + "';");

		output.println("var resourceRenameFile = '" + getResource("label.renameFile","Rename") + "';");

		output.println("var resourceCopy = '" + getResource("label.copyToClip","Copy to clipboard") + "';");

		output.println("var resourceCut = '" + getResource("label.cutToClip","Move to clipboard") + "';");

		output.println("var resourceEdit = '" + getResource("label.edit","Edit") + "';");

		output.println("var resourceRun = '" + getResource("label.run","Run") + "';");

		output.println("var resourceOpen = '" + getResource("label.open","Run associated program") + "';");

		output.println("var resourceRights = '" + getResource("label.rights","Owner &amp; Rights") + "';");

		output.println("var resourceUnzip = '" + getResource("label.unzip","Unzip") + "';");

		output.println("var resourceZip = '" + getResource("label.zip","Zip") + "';");

		output.println("var resourceUncompress = '" + getResource("label.uncompress","Uncompress") + "';");

		output.println("var resourceCompress = '" + getResource("label.compress","Compress") + "';");

		output.println("var resourceUntar = '" + getResource("label.untar","Un-tar") + "';");

		output.println("var resourceSwitchReadOnly = '" + getResource("label.switchReadOnly","Switch Read/Write") + "';");

		output.println("var resourceSendFile = '" + getResource("label.sendfile","Send as e-mail") + "';");

		output.println("var resourceCategories = '" + getResource("label.assignCategories","Assign Categories") + "';");

		output.println("var resourceEditMP3 = '" + getResource("label.editmp3","Edit MP3 tags") + "';");

		//output.println("var resourceEditDesc = '" + getResource("label.editdesc","Edit description") + "';");
		output.println("var resourceEditDesc = '" + getResource("label.editMetaInfo", "Edit Meta Information") + "';");

		output.println("var resourceComments = '" + getResource("label.comments","Comments") + "';");
		
		// store the directory context menu resources in Javascript variables			
		output.println("var resourceCreateDir = '" + getResource("label.mkdir","Create subdirectory") + "';");

		output.println("var resourceCopyDir = '" + getResource("label.copydir","Copy dir tree") + "';");

        output.println("var resourceMoveDir = '" + getResource("label.movedir","Move dir tree") + "';");

		output.println("var resourceDelDir = '" + getResource("label.deldir","Delete dir tree") + "';");

		output.println("var resourceRenameDir = '" + getResource("label.renamedir","Rename dir") + "';");

		output.println("var resourcePasteDir = '" + getResource("label.pastedir","Paste") + "';");

		output.println("var resourceStatistics = '" + getResource("label.statistics", "Statistics") + "';");
        output.println("var resourceSubdirStats = '" + getResource("label.subdirStats", "Subdir Statistics") + "';");
        output.println("var resourceAgeStats = '" + getResource("label.ageStats", "File Age Statistics") + "';");
        output.println("var resourceSizeStats = '" + getResource("label.sizeStats", "File Size Statistics") + "';");
        output.println("var resourceTypeStats = '" + getResource("label.typeStats", "File Type Statistics") + "';");

		output.println("var resourceSearch = '" + getResource("label.search","Search") + "';");

		output.println("var resourceCreateFile = '" + getResource("label.createfile","Create new file") + "';");

		output.println("var resourceUpload = '" + getResource("label.upload","Upload") + "';");

		output.println("var resourceDirRights = '" + getResource("label.accessrights","Access Rights") + "';");

		output.println("var resourceZipDir = '" + getResource("label.zipdir","Zip dir tree") + "';");

		output.println("var resourcePublish = '" + getResource("label.publish","Publish") + "';");

		output.println("var resourceCreateThumbs = '" + getResource("label.createthumbs","Create thumbnails") + "';");

		output.println("var resourceClearThumbs = '" + getResource("label.clearthumbs","Remove old thumbnails") + "';");

		output.println("var resourceCmdLine = '" + getResource("label.winCmdLine","Command Prompt") + "';");

		output.println("var resourceRefresh = '" + getResource("label.refresh","Refresh") + "';");

		output.println("var resourceDriveInfo = '" + getResource("label.driveinfo","Drive properties") + "';");

		output.println("var resourceDelLink = '" + getResource("label.deleteLink","Delete Link") + "';");

		output.println("var resourceRenLink = '" + getResource("label.renameLink","Rename") + "';");

		output.println("var resourceOrigDir = '" + getResource("label.origDir","Original Directory") + "';");

        output.println("var resourceDiffSource = '" + getResource("label.diffSource","Compare (diff)") + "';");
        
        output.println("var resourceDiffTarget = '" + getResource("label.diffTarget","Start Compare") + "';");

        output.println("var resourceCancelDiff = '" + getResource("label.cancelDiff","Cancel Compare") + "';");
        
        output.println("var resourceCompSource = '" + getResource("label.compSource","Compare folders") + "';");
        
        output.println("var resourceCompTarget = '" + getResource("label.compTarget","Start Compare") + "';");

        output.println("var resourceCancelComp = '" + getResource("label.cancelComp","Cancel Compare") + "';");
        
        output.println("var resourceDelDirStarted = '" + getResource("msg.delDirStarted","Delete operation for folder started.") + "';");

        output.println("var resourceHexView = '" + getResource("label.hexView","Hex Viewer") + "';");

        output.println("var resourceEncrypt = '" + getResource("label.encrypt","Encrypt (AES)") + "';");
        output.println("var resourceDecrypt = '" + getResource("label.decrypt","Decrypt (AES)") + "';");

        output.println("var resourceTail = '" + getResource("label.tail","tail (last lines)") + "';");
        
        output.println("var resourceDownloadFolder = '" + getResource("label.downloadFolder","download as zip") + "';");
        
        if (!readonly)
		{
			output.println("var resourceMenuMore = '" + getResource("label.menuMore","More ...") + "';");

            output.println("var resourceSynchronize = '" + getResource("label.menuSynchronize","Synchronize") + "';");

            output.println("var resourceCancelSync = '" + getResource("label.menuCancelSync","Cancel Synchronize") + "';");

            output.println("var resourceDelImages = '" + getResource("confirm.deleteImages", "Delete selected picture files?") + "';");

            output.println("var resourceDelFiles = '" + getResource("confirm.deleteFiles", "Delete selected files?") + "';");

            output.println("var resourceCloneFile = '" + getResource("label.cloneFile","Clone File") + "';");

            output.println("var resourceTouch = '" + getResource("label.touch","touch (set last modified)") + "';");

            if (WebFileSys.getInstance().isFolderWatch()) {
                output.println("var resourceWatch = '" + getResource("label.watchFolder","watch folder change") + "';");
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

        output.print("</head>");
        output.print("</html>\n");
        output.flush();
        return;
	}
	
    private void maintananceMode()
    {
        output.println("<html>");
        output.println("<head>");

        // output.println("<META HTTP-EQUIV=\"REFRESH\" CONTENT=\"20; URL=/_logout\">");

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/css/" + userMgr.getCSS(uid) + ".css\">");

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