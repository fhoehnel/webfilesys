package de.webfilesys.mail;

import de.webfilesys.LanguageManager;
import de.webfilesys.WebFileSys;

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

	public static void sendWelcomeMail(String email,String firstName,String lastName,
								   String login,String password,String userLanguage) 
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

		String welcomeText="Welcome new user " + newUserName + " to WebFileSys.\r\n\r\nYour login name: " + login;

		try
		{
	    	String languagePath = WebFileSys.getInstance().getConfigBaseDir() + "/" + LanguageManager.LANGUAGE_DIR;
			
			MailTemplate welcomeMailTemplate = new MailTemplate(languagePath + "/welcome_" + userLanguage + ".template");

			welcomeMailTemplate.setVarValue("USERNAME",newUserName);
			welcomeMailTemplate.setVarValue("LOGIN",login);
			welcomeMailTemplate.setVarValue("PASSWORD",password);

			welcomeText=welcomeMailTemplate.getText();
		}
		catch (IllegalArgumentException iaex)
		{
			System.out.println(iaex);
		}

		String subject=LanguageManager.getInstance().getResource(userLanguage,"subject.welcome","Welcome to WebFileSys");

		(new Email(email,subject,welcomeText)).send();
	}
}
