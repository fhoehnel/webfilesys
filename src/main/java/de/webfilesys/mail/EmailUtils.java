package de.webfilesys.mail;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


import de.webfilesys.LanguageManager;
import de.webfilesys.WebFileSys;
import de.webfilesys.util.CommonUtils;

/**
 * @author Frank Hoehnel
 */
public class EmailUtils
{
	public static boolean emailSyntaxOk(String emailAddress)
	{    
		if (emailAddress.length()<3)
		{
			return(false);
		}

		if ((emailAddress.indexOf('@')<0) || 
			(emailAddress.indexOf('@') != emailAddress.lastIndexOf('@')))
		{
			return(false);
		}

		String userPart=emailAddress.substring(0,emailAddress.indexOf("@"));

		for (int i=0;i<userPart.length();i++)
		{
			boolean invalidChar=true;
			char c=userPart.charAt(i);

			if ((c>='a') && (c<='z') ||
				(c>='A') && (c<='Z') ||
				(c>='0') && (c<='9') ||
				(c=='_') || (c=='-') || (c=='.'))
			{
				invalidChar=false;
			}

			if (invalidChar)
			{
				return(false);
			}
		}

		String highLevelDomain;

		int dotIdx=emailAddress.lastIndexOf('.');
		if (dotIdx>0)
		{
			String hostPart=emailAddress.substring(emailAddress.indexOf("@")+1,dotIdx);

			for (int i=0;i<hostPart.length();i++)
			{
				boolean invalidChar=true;
				char c=hostPart.charAt(i);

				if ((c>='a') && (c<='z') ||
					(c>='A') && (c<='Z') ||
					(c>='0') && (c<='9') ||
					(c=='_') || (c=='-') || (c=='.'))
				{
					invalidChar=false;
				}

				if (invalidChar)
				{
					return(false);
				}
			}

			highLevelDomain=emailAddress.substring(dotIdx+1);
		}
		else
		{
			highLevelDomain=emailAddress.substring(emailAddress.indexOf("@")+1);
		}

		for (int i=0;i<highLevelDomain.length();i++)
		{
			boolean invalidChar=true;
			char c=highLevelDomain.charAt(i);

			if ((c>='a') && (c<='z') ||
				(c>='A') && (c<='Z'))
			{
				invalidChar=false;
			}

			if (invalidChar)
			{
				return(false);
			}
		}

		return(true);
	}

	public static void sendWelcomeMail(String email, String firstName, String lastName,String login, String password, 
			String activationLink, String userLanguage) 
	{
		String newUserName=login;

		if ((lastName!=null) && (lastName.trim().length()>0))
		{
			if ((firstName!=null) && (firstName.trim().length()>0))
			{
				newUserName=firstName + " " + lastName;
			}
			else
			{
				newUserName=lastName;
			}
		}

		try
		{
	    	String languagePath = WebFileSys.getInstance().getConfigBaseDir() + "/" + LanguageManager.LANGUAGE_DIR;
	    	
            MailTemplate welcomeMailTemplate = null;

            if (!CommonUtils.isEmpty(password)) {
                welcomeMailTemplate = new MailTemplate(languagePath + "/welcome_" + userLanguage + ".template");
                welcomeMailTemplate.setVarValue("PASSWORD", password);
            } else {
                welcomeMailTemplate = new MailTemplate(languagePath + "/registration_" + userLanguage + ".template");
                welcomeMailTemplate.setVarValue("ACTIVATIONLINK", activationLink);
            }

            welcomeMailTemplate.setVarValue("USERNAME", newUserName);
            welcomeMailTemplate.setVarValue("LOGIN", login);

            String welcomeText = welcomeMailTemplate.getText();

            String subject = LanguageManager.getInstance().getResource(userLanguage, "subject.welcome", "Welcome to WebFileSys");

            (new SmtpEmail(email, subject, welcomeText)).send();
		}
		catch (IllegalArgumentException iaex)
		{
            LogManager.getLogger(EmailUtils.class).error("failed to send welcome mail", iaex);
		}
	}
}
