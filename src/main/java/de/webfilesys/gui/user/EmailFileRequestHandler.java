package de.webfilesys.gui.user;

import java.io.File;
import java.io.PrintWriter;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.Constants;
import de.webfilesys.gui.xsl.XslFileListHandler;
import de.webfilesys.gui.xsl.XslThumbnailHandler;
import de.webfilesys.gui.xsl.mobile.MobileFolderFileListHandler;
import de.webfilesys.mail.Email;
import de.webfilesys.mail.EmailUtils;
import de.webfilesys.util.CommonUtils;

/**
 * @author Frank Hoehnel
 */
public class EmailFileRequestHandler extends UserRequestHandler
{
    boolean clientIsLocal = false;
    
    public EmailFileRequestHandler(
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
        // filePath is used from file link context menu
        String filePath = getParameter("filePath");
        
        String fileName = getParameter("fileName");

        if ((filePath == null) || (filePath.trim().length() == 0)) 
        {
            String cwd = getCwd();
            
            if (!cwd.endsWith(File.separator))
            {
                filePath = cwd + File.separator + fileName;
            }
            else
            {
                filePath = cwd + fileName;
            }
        }
        else
        {
            fileName = CommonUtils.extractFileName(filePath);
        }

        if (!accessAllowed(filePath))
        {
            return;
        }

        String sendSynchronous = getParameter("sendSynchronous");
        
        Vector mailReceivers = new Vector();

        String emailList = getParameter("receiver");

        StringTokenizer emailParser = new StringTokenizer(emailList, ",");

        while (emailParser.hasMoreTokens())
        {
            String email = emailParser.nextToken().trim();

            if (EmailUtils.emailSyntaxOk(email))
            {
                mailReceivers.add(email);
            }
            else
            {
                Logger.getLogger(getClass()).warn("invalid e-mail address: " + email);
            }
        }

        String subject = getParameter("subject");

        if ((subject == null) || (subject.trim().length() == 0))
        {
            subject = fileName;
        }

        StringBuffer mailSenderName = new StringBuffer();

        String firstName = userMgr.getFirstName(uid);
        String lastName = userMgr.getLastName(uid);

        if ((firstName != null) && (firstName.trim().length() > 0))
        {
            mailSenderName.append(firstName);
            mailSenderName.append(' ');
        }

        if ((lastName != null) && (lastName.trim().length() > 0))
        {
            mailSenderName.append(lastName);
        }

        if (mailSenderName.length() == 0)
        {
            mailSenderName.append(uid);
        }

        File fileToSend = new File(filePath);

        Email message = new Email(mailReceivers, subject, fileToSend);

        message.setMailSenderName(mailSenderName.toString());

        String mailSenderAddress = userMgr.getEmail(uid);

        if ((mailSenderAddress != null)
            && (mailSenderAddress.trim().length() > 0))
        {
            message.setMailSenderAddress(mailSenderAddress);
        }

        if (sendSynchronous != null) 
        {
        	if (!message.sendSynchron()) 
        	{
        		setParameter("errorMsg", getResource("error.sendEmail", "failed to send email"));
        	}
        }
        else
        {
            message.send();
        }
        
        int viewMode = Constants.VIEW_MODE_LIST;
        
        Integer sessionViewMode = (Integer) session.getAttribute("viewMode");
        
        if (sessionViewMode != null)
        {
            viewMode = sessionViewMode.intValue();
        }
        
        String mobile = (String) session.getAttribute("mobile");
        
        if (mobile != null) 
        {
            (new MobileFolderFileListHandler(req, resp, session, output, uid)).handleRequest(); 
        }
        else
        {
            if (viewMode == Constants.VIEW_MODE_THUMBS)
            {
                (new XslThumbnailHandler(req, resp, session, output, uid, clientIsLocal)).handleRequest(); 

            }
            else
            {
                (new XslFileListHandler(req, resp, session, output, uid, true)).handleRequest();
            }
        }
    }

}
