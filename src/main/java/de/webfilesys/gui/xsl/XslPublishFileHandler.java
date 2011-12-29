package de.webfilesys.gui.xsl;

import java.io.File;
import java.io.PrintWriter;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.InvitationManager;
import de.webfilesys.WebFileSys;
import de.webfilesys.mail.Email;
import de.webfilesys.mail.EmailUtils;
import de.webfilesys.util.UTF8URLEncoder;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslPublishFileHandler extends XslRequestHandlerBase
{
	static final int ERROR_INVALID_EXP     = 1;
	static final int ERROR_INVALID_EMAIL   = 2;
	static final int ERROR_MISSING_SUBJECT = 3;
	static final int ERROR_MISSING_EMAIL   = 4;
	
	boolean ssl = false;
	
	int serverPort = 80;
	
	public XslPublishFileHandler(
			HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid)
	{
        super(req, resp, session, output, uid);
		
		if (req.getScheme().equalsIgnoreCase("https"))
		{
			ssl = true;
		}
		
		serverPort = req.getServerPort();
	}
	  
	protected void process()
	{
		if (!checkWriteAccess())
		{
			return;
		}

		String publishPath = getParameter("publishPath");

		if (!checkAccess(publishPath))
		{
			return;
		}

        String path = publishPath.substring(0, publishPath.lastIndexOf(File.separatorChar));
        
        if (path.length() == 0)
        {
        	path = publishPath.substring(0,2);
        }

        int errorCode = 0;
        
        String sendMail = getParameter("sendMail");
        
        String recipient = getParameter("recipient");
        
        String subject = getParameter("subject");
        
		Vector mailReceivers = null;

		String msgText = getParameter("msgText");

        String expiration = getParameter("expiration");
        
        int expDays = 0;
        
        if (expiration != null)
        {
        	if (expiration.trim().length() == 0)
        	{
        		errorCode = ERROR_INVALID_EXP;
        	}
        	else
        	{
        		try
        		{
        			expDays = Integer.parseInt(expiration);
        		}
                catch (NumberFormatException nfex)
                {
                	errorCode = ERROR_INVALID_EXP;
                }
        	}
        	
        	if (errorCode == 0)
        	{
        		if (WebFileSys.getInstance().getMailHost() != null)
        		{
					if (sendMail != null)
					{
						if ((recipient == null) || (recipient.trim().length() == 0))
						{
							errorCode = ERROR_INVALID_EMAIL;
						}
						else
						{
							StringTokenizer emailParser = new StringTokenizer(recipient, ",");

							while (emailParser.hasMoreTokens())
							{
								String email = emailParser.nextToken();

								if (!EmailUtils.emailSyntaxOk(email))
								{
									errorCode = ERROR_INVALID_EMAIL;
								}
								else
								{
									if (mailReceivers == null)
									{
										mailReceivers = new Vector();
									}
								
									mailReceivers.add(email);
								}
							}
						}
        			
						if (errorCode == 0)
						{
							if ((subject == null) || (subject.trim().length() == 0))
							{
								errorCode = ERROR_MISSING_SUBJECT;
							}
						}
					}
        		}
        	}

			if (errorCode == 0)
			{
				System.out.println("creating invitation subject=" + subject + " msgText=" + msgText + " expiration=" + expiration + " recipients=" + recipient);

				String accessCode = InvitationManager.getInstance().addInvitation(uid, publishPath, expDays, InvitationManager.INVITATION_TYPE_FILE, false);

                publishSecretURL(path, publishPath, accessCode, sendMail, mailReceivers, subject, msgText);
                
				return;
			}
        }
        
		Element publishFileElement = doc.createElement("publishFile");
			
		doc.appendChild(publishFileElement);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/publishFile.xsl\"");

		doc.insertBefore(xslRef, publishFileElement);

        if (errorCode == ERROR_INVALID_EXP)
        {
			XmlUtil.setChildText(publishFileElement, "errorMsg", getResource("alert.expinvalid", "Expiration must be a number"), false);
		}
		else if (errorCode == ERROR_MISSING_EMAIL)
		{
			XmlUtil.setChildText(publishFileElement, "errorMsg", getResource("alert.noreceiver", "Enter at least one receiver"), false);
		}
		else if (errorCode == ERROR_INVALID_EMAIL)
		{
			XmlUtil.setChildText(publishFileElement, "errorMsg", getResource("alert.emailsyntax", "invalid e-mail address"), false);
		}
		else if (errorCode == ERROR_MISSING_SUBJECT)
		{
			XmlUtil.setChildText(publishFileElement, "errorMsg", getResource("alert.nosubject", "Enter a subject for the e-mail"), false);
		}

		XmlUtil.setChildText(publishFileElement, "css", userMgr.getCSS(uid), false);
		XmlUtil.setChildText(publishFileElement, "path", path, false);
		XmlUtil.setChildText(publishFileElement, "encodedPath", UTF8URLEncoder.encode(path), false);
		XmlUtil.setChildText(publishFileElement, "publishPath", publishPath, false);
		
		if (WebFileSys.getInstance().getMailHost() != null)
		{
			XmlUtil.setChildText(publishFileElement, "mailEnabled", "true", false);
		}
		
		addMsgResource("label.publishFile", getResource("label.publishFile","Publish File"));
		addMsgResource("label.fileToPublish", getResource("label.fileToPublish","file to publish"));
		addMsgResource("label.sendInvitationMail", getResource("label.sendInvitationMail","send invitation e-mail"));
		addMsgResource("label.receiver", getResource("label.receiver", "Receiver(s) (comma-separated list of e-mail addresses)"));
		addMsgResource("label.subject", getResource("label.subject","Subject"));
		addMsgResource("label.invitationtext", getResource("label.invitationtext","The invitation text"));
		addMsgResource("label.expiration", getResource("label.expiration", "Expires after days"));
		addMsgResource("button.publish", getResource("button.publish","Publish"));
		addMsgResource("button.cancel", getResource("button.cancel","Cancel"));

		if (errorCode != 0)
		{
			if (sendMail != null)
			{
				addRequestParameter("sendMail", "true");
				addRequestParameter("recipient", recipient);
				addRequestParameter("subject", subject);
				addRequestParameter("msgText", msgText);
			}
				
			addRequestParameter("expiration", expiration);
		}

		this.processResponse("publishFile.xsl", false);
    }
    
    private void publishSecretURL(String path, String publishPath, String accessCode, String sendMail,
                                  Vector mailReceivers, String subject, String msgText)
    {
		StringBuffer secretURL=new StringBuffer();

		
		
		if (ssl)
		{
			secretURL.append("https://");
		}
		else
		{
			secretURL.append("http://");
		}

		if (WebFileSys.getInstance().getServerDNS() != null)
		{
			secretURL.append(WebFileSys.getInstance().getServerDNS());
		}
		else
		{
			secretURL.append(WebFileSys.getInstance().getLocalIPAddress());
		}

		secretURL.append(":");

		secretURL.append(serverPort);

		secretURL.append("/webfilesys/servlet?command=visitorFile&accessCode=");
		secretURL.append(accessCode);

		if (sendMail != null)
		{
			StringBuffer content = new StringBuffer(msgText);
			content.append("\r\n\r\n");

			content.append(secretURL.toString());

			content.append("\r\n\r\n\r\n\r\n");

			Email message = new Email(mailReceivers,subject,content.toString());

			StringBuffer mailSenderName=new StringBuffer();

			String firstName = userMgr.getFirstName(uid);
			String lastName = userMgr.getLastName(uid);

			if ((firstName!=null) && (firstName.trim().length()>0))
			{
				mailSenderName.append(firstName);
				mailSenderName.append(' ');
			}

			if ((lastName!=null) && (lastName.trim().length()>0))
			{
				mailSenderName.append(lastName);
			}

			if (mailSenderName.length()==0)
			{
				mailSenderName.append(uid);
			}

			message.setMailSenderName(mailSenderName.toString());

			String mailSenderAddress = userMgr.getEmail(uid);

			if ((mailSenderAddress!=null) && (mailSenderAddress.trim().length()>0))
			{
				message.setMailSenderAddress(mailSenderAddress);
			}

			message.send();
		}
    	
		Element publishFileElement = doc.createElement("publishFile");
			
		doc.appendChild(publishFileElement);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/filePublished.xsl\"");

		doc.insertBefore(xslRef, publishFileElement);

		XmlUtil.setChildText(publishFileElement, "css", userMgr.getCSS(uid), false);
		XmlUtil.setChildText(publishFileElement, "path", path, false);
		XmlUtil.setChildText(publishFileElement, "encodedPath", UTF8URLEncoder.encode(path), false);
		XmlUtil.setChildText(publishFileElement, "publishPath", publishPath, false);
		XmlUtil.setChildText(publishFileElement, "secretURL", secretURL.toString(), false);

		addMsgResource("label.publishFile", getResource("label.publishFile","Publish File"));
		addMsgResource("label.fileToPublish", getResource("label.fileToPublish","file to publish"));
		addMsgResource("label.filePublished", getResource("label.filePublished","The published file can be viewed via the URL"));
		addMsgResource("button.closewin", getResource("button.closewin","Close Window"));
		
		this.processResponse("filePublished.xsl", false);
    }
}