package de.webfilesys.gui.user;

import java.io.File;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.Comment;
import de.webfilesys.InvitationManager;
import de.webfilesys.MetaInfManager;
import de.webfilesys.gui.xsl.XslAlbumImageHandler;
import de.webfilesys.gui.xsl.XslListCommentsHandler;

/**
 * @author Frank Hoehnel
 */
public class AddCommentRequestHandler extends UserRequestHandler
{
	protected HttpServletRequest req = null;

	protected HttpServletResponse resp = null;

	public AddCommentRequestHandler(
			HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid)
	{
        super(req, resp, session, output, uid);
        
        this.req = req;
        
        this.resp = resp;
	}

	protected void process()
	{
		String actPath=getParameter("actPath");

		if (!checkAccess(actPath))
		{
			return;
		}

		String actPathOS = actPath.replace('/', File.separatorChar);

		String commentAuthor = uid;
		
		boolean modifyPermission=true;

		if (userMgr.getUserType(uid).equals("virtual"))
		{
			modifyPermission=InvitationManager.getInstance().commentsAllowed(uid);
			
			String author = req.getParameter("author");
			
			if ((author != null) && (author.trim().length() > 0))
			{
				commentAuthor = author;
			}
		}

		if (!modifyPermission)
		{
			Logger.getLogger(getClass()).warn("attempt to add comments for " + actPath + " from virtual user " + uid + " without permission");
			return;
		}

		String newComment=getParameter("newComment");

		if ((newComment!=null) && (newComment.trim().length()>0))
		{
			MetaInfManager.getInstance().addComment(actPathOS, new Comment(commentAuthor, new Date(), newComment));
		}

        String mobile = (String) session.getAttribute("mobile");

        if (mobile != null)
        {
            // (new MobileFolderFileListHandler(req, resp, session, output, uid)).handleRequest(); 
            (new XslListCommentsHandler(req, resp, session, output, uid)).handleRequest();
        }
        else
        {
            String role = userMgr.getRole(uid);
            
            if ((role != null) && role.equals("album"))
            {
                (new XslAlbumImageHandler(req, resp, session, output, uid)).handleRequest();
            }
            else
            {
                (new XslListCommentsHandler(req, resp, session, output, uid)).handleRequest();
            }
        }
	}
}
