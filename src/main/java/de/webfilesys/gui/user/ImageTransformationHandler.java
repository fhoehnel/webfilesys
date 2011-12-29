package de.webfilesys.gui.user;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.webfilesys.graphics.ImageTransformation;
import de.webfilesys.gui.xsl.XslThumbnailHandler;

/**
 * @author Frank Hoehnel
 */
public class ImageTransformationHandler extends UserRequestHandler
{
	boolean clientIsLocal = false;
	
	public ImageTransformationHandler(
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
		if (!checkWriteAccess())
		{
			return;
		}

		String imageName=getParameter("imgName");

		if ((imageName==null) || (imageName.trim().length()==0))
		{
			return;
		}

		String action=getParameter("action");

		String degrees=getParameter("degrees");

		if (degrees==null)
		{
			degrees="90";
		}

		ImageTransformation imgTrans=new ImageTransformation(imageName,action,degrees);

		String resultFileName=imgTrans.execute(false);

		String beforeName=getParameter("beforeName");

		if ((beforeName!=null) && (beforeName.trim().length()>0))
		{
			if (beforeName.compareToIgnoreCase(resultFileName)<=0)
			{
				setParameter("beforeName",resultFileName);
			}
		}

		String afterName=getParameter("afterName");

		if ((afterName!=null) && (afterName.trim().length()>0))
		{
			if (afterName.compareToIgnoreCase(resultFileName)>=0)
			{
				setParameter("afterName",resultFileName);
			}
		}

	    (new XslThumbnailHandler(req, resp, session, output, uid, clientIsLocal)).handleRequest(); 
	}
}
