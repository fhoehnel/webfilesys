package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.PrintWriter;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import de.webfilesys.mail.Email;
import de.webfilesys.mail.EmailUtils;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class AjaxSendEmailHandler extends XmlRequestHandlerBase
{
	public AjaxSendEmailHandler(
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

        Element resultElement = doc.createElement("result");
        
        boolean sendError = false;
        
        if (sendSynchronous != null) 
        {
        	if (!message.sendSynchron()) 
        	{
        		sendError = true;
                XmlUtil.setChildText(resultElement, "message", getResource("error.sendEmail", "failed to send email"), true);
        	} 
        	else 
        	{
                XmlUtil.setChildText(resultElement, "message", getResource("success.sendEmail", "File has been sent as e-mail."), true);
        	}
        }
        else
        {
            message.send();
            XmlUtil.setChildText(resultElement, "message", fileName + " " + getResource("label.confirmsend", "will be sent as e-mail"), true);
        }
        
        XmlUtil.setElementText(resultElement, Boolean.toString(!sendError));
        
        doc.appendChild(resultElement);
		
		processResponse();
	}
}
