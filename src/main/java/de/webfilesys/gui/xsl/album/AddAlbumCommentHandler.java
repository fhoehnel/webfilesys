package de.webfilesys.gui.xsl.album;

import java.io.File;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


import de.webfilesys.Comment;
import de.webfilesys.InvitationManager;
import de.webfilesys.MetaInfManager;
import de.webfilesys.gui.user.UserRequestHandler;

/**
 * @author Frank Hoehnel
 */
public class AddAlbumCommentHandler extends UserRequestHandler
{
	protected HttpServletRequest req = null;

	protected HttpServletResponse resp = null;

	public AddAlbumCommentHandler(
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

			
		if (!InvitationManager.getInstance().commentsAllowed(uid))
		{
			LogManager.getLogger(getClass()).warn("attempt to add comments for " + actPath + " from virtual user " + uid + " without permission");
			return;
		}

		String author = req.getParameter("author");
			
		if ((author != null) && (author.trim().length() > 0))
		{
			commentAuthor = author;
		}

		String newComment = getParameter("newComment");

		if ((newComment!=null) && (newComment.trim().length()>0))
		{
			MetaInfManager.getInstance().addComment(actPathOS, new Comment(commentAuthor, new Date(), newComment));
		}

		(new XslAlbumPictureHandler(req, resp, session, output, uid)).handleRequest();
	}
}
