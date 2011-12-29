package de.webfilesys.gui.user;

import java.io.File;
import java.io.PrintWriter;
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.MetaInfManager;
import de.webfilesys.gui.xsl.XslAlbumImageHandler;
import de.webfilesys.gui.xsl.XslShowImageHandler;

/**
 * @author Frank Hoehnel
 */
public class RateVotingHandler extends UserRequestHandler
{
	public RateVotingHandler(
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
		String imagePath = getParameter("imagePath");
		
		if ((imagePath == null) || (imagePath.trim().length() == 0))
		{
			Logger.getLogger(getClass()).error("RateVotingHandler: imagePath missing");
			return;
		}
		
		if (!this.checkAccess(imagePath))
		{
			return;
		}

		String imagePathOS = imagePath.replace('/', File.separatorChar);
		
        String temp = getParameter("rating");
        
        if (temp == null)
        {
        	Logger.getLogger(getClass()).error("rating is null");
        	return;
        }
        
        int rating = (-1);
        
        try
        {
        	rating = Integer.parseInt(temp);
        }
        catch (NumberFormatException nfe)
        {
			Logger.getLogger(getClass()).error("invalid rating: " + temp);
			return;
        }

		MetaInfManager metaInfMgr = MetaInfManager.getInstance();

		if (readonly)
		{
            Hashtable ratedPictures = (Hashtable) session.getAttribute("ratedPictures");
			
			if (ratedPictures == null)
	    	{
	    		ratedPictures = new Hashtable(5);
	    		
	    		session.setAttribute("ratedPictures", ratedPictures);
	    	}

			if (ratedPictures.get(imagePathOS) == null)
            {
				metaInfMgr.addVisitorRating(imagePathOS, rating);

				ratedPictures.put(imagePathOS, new Boolean(true));
            }
		}
        else
        {
			metaInfMgr.setOwnerRating(imagePathOS, rating);
        }

        String role = userMgr.getRole(uid);
        
        if ((role != null) && role.equals("album"))
        {
			(new XslAlbumImageHandler(req, resp, session, output, uid)).handleRequest();
        }
        else
        {
			this.setParameter("imgname", imagePath);

		    (new XslShowImageHandler(req, resp, session, output, uid)).handleRequest(); 
        }
    }

}
