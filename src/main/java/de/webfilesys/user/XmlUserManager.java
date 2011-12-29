package de.webfilesys.user;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.webfilesys.WebFileSys;
import de.webfilesys.util.XmlUtil;

public class XmlUserManager extends UserManagerBase
{
    public static final String USER_FILE_NAME = "users.xml";

    private static final String ENCRYPTION_METHOD = "MD5";
    
    private Document doc;

    private DocumentBuilder builder;

    private Element userRoot=null;

    private boolean modified;

    private Hashtable userCache;

    public boolean readyForShutdown;
    
    private String userFilePath = null;

    public XmlUserManager()
    {
       builder = null;

       try
       {
           DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
           builder = factory.newDocumentBuilder();
       }
       catch (ParserConfigurationException pcex)
       {
           Logger.getLogger(getClass()).error(pcex.toString());
       }

       userFilePath = WebFileSys.getInstance().getConfigBaseDir() + "/" + USER_FILE_NAME;
       
       userCache=new Hashtable(10);

       userRoot=loadFromFile();

       modified=false;

       readyForShutdown=false;

       this.start();
    }

    public synchronized void saveToFile()
    {
        if (userRoot == null)
        {
            return;
        }
        
        if (Logger.getLogger(getClass()).isDebugEnabled())
        {
            Logger.getLogger(getClass()).debug("saving user info to file: " + userFilePath);
        }

        synchronized (userRoot)
        {
            OutputStreamWriter xmlOutFile = null;

            try
            {
                FileOutputStream fos = new FileOutputStream(userFilePath);
                
                xmlOutFile = new OutputStreamWriter(fos, "UTF-8");
                
                XmlUtil.writeToStream(userRoot, xmlOutFile);
                
                xmlOutFile.flush();

                modified = false;
            }
            catch (IOException io1)
            {
                Logger.getLogger(getClass()).error("error saving user registry file " + userFilePath, io1);
            }
            finally
            {
                if (xmlOutFile != null)
                {
                    try 
                    {
                        xmlOutFile.close();
                    }
                    catch (Exception ex) 
                    {
                    }
                }
            }
        }
    }

    public Element loadFromFile()
    {
       File usersFile = new File(userFilePath);

       if (!usersFile.exists()) 
       {
           File osSpecificUsersFile = null;

           if (File.separatorChar == '/') 
           {
               osSpecificUsersFile = new File(usersFile + ".unix");
           }
           else
           {
               osSpecificUsersFile = new File(usersFile + ".win");
           }
               
           if (osSpecificUsersFile.exists()) 
           {
               if (!osSpecificUsersFile.renameTo(usersFile)) 
               {
                   Logger.getLogger(getClass()).error("failed to rename user database file " + osSpecificUsersFile.getAbsolutePath() + " to " + usersFile);
                   return(null);
               }
               else 
               {
                   Logger.getLogger(getClass()).info("initialized user database file from " + osSpecificUsersFile.getAbsolutePath());
               }
           }
           else
           {
               Logger.getLogger(getClass()).error("failed to initialize user database file from " + osSpecificUsersFile.getAbsolutePath());
               return(null);
           }
       }
       
       if ((!usersFile.isFile()) || (!usersFile.canRead()))
       {
    	   Logger.getLogger(getClass()).error("user database file " + userFilePath + " is not a readable file");
           return(null);
       }

       Logger.getLogger(getClass()).info("reading user registry from " + usersFile.getAbsolutePath());

       doc = null;
       
       FileInputStream fis = null;

       try
       {
           fis = new FileInputStream(usersFile);
           
           InputSource inputSource = new InputSource(fis);
           
           inputSource.setEncoding("UTF-8");

           doc = builder.parse(inputSource);
       }
       catch (SAXException saxex)
       {
           Logger.getLogger(getClass()).error("failed to load user registry file " + usersFile.getAbsolutePath(), saxex);
       }
       catch (IOException ioex)
       {
           Logger.getLogger(getClass()).error("failed to load user registry file : " + usersFile.getAbsolutePath(), ioex);
       }
       finally 
       {
           if (fis != null)
           {
               try
               {
                   fis.close();
               }
               catch (Exception ex)
               {
               }
           }
       }

       if (doc == null)
       {
           return(null);
       }

       userRoot = doc.getDocumentElement();

       if (userRoot!=null)
       {
           // put user elements into cache
           NodeList userList=userRoot.getElementsByTagName("user");

           if (userList!=null)
           {
               int listLength=userList.getLength();

               for (int i=0;i<listLength;i++)
               {
                   Element userElement=(Element) userList.item(i);

                   String id=userElement.getAttribute("id");

                   if ((id!=null) && (id.trim().length()!=0))
                   {
                       userCache.put(id,userElement);
                   }

                   // normalize document root
                   String documentRoot=XmlUtil.getChildText(userElement,"documentRoot");

                   if (documentRoot!=null)
                   {
                       documentRoot=normalizeDocRoot(documentRoot);

                       XmlUtil.setChildText(userElement,"documentRoot",documentRoot);
                   }
               }
           }
       }
    
       return(userRoot);
    }

    /**
     * Returns a Vector of the userids of all registered users (Strings)
     */
    public Vector getListOfUsers()
    {
        Vector listOfUsers=new Vector();

        NodeList userList=userRoot.getElementsByTagName("user");

        if (userList==null)
        {
            return(listOfUsers);
        }

        int listLength=userList.getLength();

        for (int i=0;i<listLength;i++)
        {
            Element userElement=(Element) userList.item(i);

            String id=userElement.getAttribute("id");

            String userType=userElement.getAttribute("type");

            if ((id!=null) && (id.trim().length()!=0) && 
                ((userType==null) || (!userType.equals("virtual"))))
            {
                listOfUsers.add(id);
            }
        }

        Collections.sort(listOfUsers);

        return(listOfUsers);
    }

    public Vector getAdminUserEmails()
    {
        Vector emailList=new Vector();

        NodeList userList=userRoot.getElementsByTagName("user");

        if (userList==null)
        {
            return(emailList);
        }

        int listLength=userList.getLength();

        for (int i=0;i<listLength;i++)
        {
            Element userElement=(Element) userList.item(i);

            String role=XmlUtil.getChildText(userElement,"role");

            if ((role!=null) && role.equals("admin"))
            {
                String email=XmlUtil.getChildText(userElement,"email");

                if ((email!=null) && (email.trim().length() > 0))
                {
                    emailList.add(email);
                }
            }
        }

        return(emailList);
    }

    public Vector getMailAddressesByRole(String receiverRole)
    {
        Vector emailList=new Vector();

        NodeList userList=userRoot.getElementsByTagName("user");

        if (userList==null)
        {
            return(emailList);
        }

        int listLength=userList.getLength();

        for (int i=0;i<listLength;i++)
        {
            Element userElement=(Element) userList.item(i);

            String role=XmlUtil.getChildText(userElement,"role");

            if ((role!=null) && role.equals(receiverRole))
            {
                String email=XmlUtil.getChildText(userElement,"email");

                if ((email!=null) && (email.trim().length() > 0))
                {
                    emailList.add(email);
                }
            }
        }

        return(emailList);
    }

    public Vector getAllMailAddresses()
    {
        Vector emailList=new Vector();

        NodeList userList=userRoot.getElementsByTagName("user");

        if (userList==null)
        {
            return(emailList);
        }

        int listLength=userList.getLength();

        for (int i=0;i<listLength;i++)
        {
            Element userElement=(Element) userList.item(i);

            String email=XmlUtil.getChildText(userElement,"email");

            if ((email!=null) && (email.trim().length() > 0))
            {
                 emailList.add(email);
            }
        }

        return(emailList);
    }

    public Element getUserElement(String userId)
    {
        return((Element) userCache.get(userId));
    }

    public boolean userExists(String userId)
    {
        return(getUserElement(userId)!=null);
    }

    public boolean addUser(String userId)
    {
        if (getUserElement(userId)!=null)
        {
            return(false);
        }

        Element newUserElement=doc.createElement("user");

        newUserElement.setAttribute("id",userId);

        userRoot.appendChild(newUserElement);

        userCache.put(userId,newUserElement);

        modified=true;

        return(true);
    }

    public String createVirtualUser(String realUser,String docRoot,String role,int expDays)
    {
        String virtualUserId=null;

        int i=1;

        do
        {
            virtualUserId=realUser + "-" + i;

            i++;
        }
        while (userExists(virtualUserId));
    
        addUser(virtualUserId);

        setUserType(virtualUserId,"virtual");

        setPassword(virtualUserId, "public");
        setReadonly(virtualUserId,true);
        setDocumentRoot(virtualUserId,docRoot);
        setLanguage(virtualUserId,this.getLanguage(realUser));
        setRole(virtualUserId, role);
        // setDiskQuota();
        setCSS(virtualUserId,this.getCSS(realUser));
        setPageSize(virtualUserId,this.getPageSize(realUser));

        modified=true;

        return(virtualUserId);
    }

    public boolean setUserType(String userId,String type)
    {
        Element userElem=getUserElement(userId);
        
        if (userElem==null)
        {
            return(false);
        }
        
        userElem.setAttribute("type",type);

        return(true);
    }

    public String getUserType(String userId)
    {
        Element userElem=getUserElement(userId);
        
        if (userElem==null)
        {
            return(null);
        }
        
        String type=userElem.getAttribute("type");

        if (type==null)
        {
            return("default");
        }

        return(type);
    }

    public boolean removeUser(String userId)
    {
        Element userElement=getUserElement(userId);

        if (userElement==null)
        {
            return(false);
        }

        userRoot.removeChild(userElement);

        userCache.remove(userId);

        modified=true;

        return(true);
    }

    public String getFirstName(String userId)
    {
        Element userElement=getUserElement(userId);

        if (userElement==null)
        {
            return(null);
        }

        return(XmlUtil.getChildText(userElement,"firstName"));
    }
      
    public void setFirstName(String userId,String newValue)
    {
        Element userElement=getUserElement(userId);

        if (userElement==null)
        {
            return;
        }

        XmlUtil.setChildText(userElement,"firstName",newValue);

        modified=true;
    }

    public String getLastName(String userId)
    {
        Element userElement=getUserElement(userId);

        if (userElement==null)
        {
            return(null);
        }

        return(XmlUtil.getChildText(userElement,"lastName"));
    }

    public void setLastName(String userId,String newValue)
    {
        Element userElement=getUserElement(userId);

        if (userElement==null)
        {
            return;
        }

        XmlUtil.setChildText(userElement,"lastName",newValue);

        modified=true;
    }

    public String getEmail(String userId)
    {
        Element userElement=getUserElement(userId);

        if (userElement==null)
        {
            return(null);
        }

        return(XmlUtil.getChildText(userElement,"email"));
    }

    public void setEmail(String userId,String newValue)
    {
        Element userElement=getUserElement(userId);

        if (userElement==null)
        {
            return;
        }

        XmlUtil.setChildText(userElement,"email",newValue);

        modified=true;
    }

    public long getDiskQuota(String userId)
    {
        long diskQuota=(-1l);

        Element userElement=getUserElement(userId);

        if (userElement==null)
        {
            return(diskQuota);
        }

        String quotaString=XmlUtil.getChildText(userElement,"diskQuota");

        if ((quotaString!=null) && (quotaString.trim().length()>0))
        {
            try
            {
                diskQuota=Long.parseLong(quotaString);
            }
            catch (NumberFormatException nfex)
            {
            	Logger.getLogger(getClass()).warn("invalid disk quota " + quotaString);
            }
        }

        return(diskQuota);
    }

    public void setDiskQuota(String userId,long newValue)
    {
        Element userElement=getUserElement(userId);

        if (userElement==null)
        {
            return;
        }

        XmlUtil.setChildText(userElement,"diskQuota",(new Long(newValue)).toString());

        modified=true;
    }

    public int getPageSize(String userId)
    {
        int pageSize = WebFileSys.getInstance().getThumbnailsPerPage();

        Element userElement=getUserElement(userId);

        if (userElement==null)
        {
            return(pageSize);
        }

        String pageSizeString=XmlUtil.getChildText(userElement,"pageSize");

        if ((pageSizeString!=null) && (pageSizeString.trim().length()>0))
        {
            try
            {
                pageSize=Integer.parseInt(pageSizeString);
            }
            catch (NumberFormatException nfex)
            {
            }
        }

        return(pageSize);
    }

    public void setPageSize(String userId,int newValue)
    {
        Element userElement=getUserElement(userId);

        if (userElement==null)
        {
            return;
        }

        XmlUtil.setChildText(userElement,"pageSize",Integer.toString(newValue));

        modified=true;
    }

    public String getPhone(String userId)
    {
        Element userElement=getUserElement(userId);

        if (userElement==null)
        {
            return(null);
        }

        return(XmlUtil.getChildText(userElement,"phone"));
    }

    public void setPhone(String userId,String newValue)
    {
        Element userElement=getUserElement(userId);

        if (userElement==null)
        {
            return;
        }

        XmlUtil.setChildText(userElement,"phone",newValue);

        modified=true;
    }

    public String getLanguage(String userId)
    {
        Element userElement=getUserElement(userId);

        if (userElement==null)
        {
            return(null);
        }

        String language=XmlUtil.getChildText(userElement,"language");

        if (language.trim().length()==0)
        {
            return(null);
        }

        return(language);
    }

    public void setLanguage(String userId,String newValue)
    {
        Element userElement=getUserElement(userId);

        if (userElement==null)
        {
            return;
        }

        XmlUtil.setChildText(userElement,"language",newValue);

        modified=true;
    }


    public String getRole(String userId)
    {
        Element userElement=getUserElement(userId);

        if (userElement==null)
        {
            return(null);
        }

        return(XmlUtil.getChildText(userElement,"role"));
    }

    public void setRole(String userId,String newRole)
    {
        Element userElement=getUserElement(userId);

        if (userElement==null)
        {
            return;
        }

        XmlUtil.setChildText(userElement,"role",newRole);

        modified=true;
    }

    /**
     * Get the CSS stylesheet assigned to the user.
     *
     * @param userId the userid of the user
     * @return the name of the CSS stylesheet
     */
    public String getCSS(String userId)
    {
        Element userElement=getUserElement(userId);

        if (userElement==null)
        {
            return(null);
        }

        return(XmlUtil.getChildText(userElement,"css"));
    }

    /**
     * Assigns a CSS stylesheet to the user.
     *
     * @param userId the userid of the user
     * @param newCSS the name of the new CSS stylesheet
     */
    public void setCSS(String userId,String newCSS)
    {
        Element userElement=getUserElement(userId);

        if (userElement==null)
        {
            return;
        }

        XmlUtil.setChildText(userElement,"css",newCSS);

        modified=true;
    }

    public boolean isReadonly(String userId)
    {
        Element userElement=getUserElement(userId);

        if (userElement==null)
        {
            return(false);
        }

        String readonly=XmlUtil.getChildText(userElement,"readonly");

        return((readonly!=null) && readonly.equals("true"));
    }

    public void setReadonly(String userId,boolean readonly)
    {
        Element userElement=getUserElement(userId);

        if (userElement==null)
        {
            return;
        }

        if (readonly)
        {
            XmlUtil.setChildText(userElement,"readonly","true");
        }
        else
        {
            XmlUtil.setChildText(userElement,"readonly","false");
        }
    }

    public String getDocumentRoot(String userId)
    {
        Element userElement=getUserElement(userId);

        if (userElement==null)
        {
            if (File.separatorChar=='/')
            {
                return("/tmp");
            }

            return("c:\\temp");
        }

        String documentRoot=XmlUtil.getChildText(userElement,"documentRoot");

        if ((documentRoot==null) || (documentRoot.trim().length()==0))
        {
            if (File.separatorChar=='/')
            {
                return("/tmp");
            }

            return("c:\\temp");
        }

        return(documentRoot);
    }

    public String getLowerCaseDocRoot(String userId)
    {
        return(getDocumentRoot(userId).toLowerCase());
    }

    public void setDocumentRoot(String userId,String newValue)
    {
        Element userElement=getUserElement(userId);

        if (userElement==null)
        {
            return;
        }

        String documentRoot=normalizeDocRoot(newValue);

        XmlUtil.setChildText(userElement,"documentRoot",documentRoot);

        modified=true;
    }

    public String normalizeDocRoot(String documentRoot)
    {
        documentRoot=documentRoot.replace('\\','/');

        if (documentRoot.endsWith("/"))
        {
            if (documentRoot.length()>1)
            {
                documentRoot=documentRoot.substring(0,documentRoot.length()-1);
            }
        }

        if ((documentRoot.charAt(0)!='*') || (File.separatorChar=='/'))
        {
            File docRootFile=new File(documentRoot);
            if ((!docRootFile.exists()) || (!docRootFile.isDirectory()))
            {
            	Logger.getLogger(getClass()).warn("the document root directory " + documentRoot + " does not exist!");
            }
        }

        if (File.separatorChar=='\\')
        {
            if (documentRoot.charAt(0)=='*')             
            {
                documentRoot="*:";
            }
            else
            {
                if ((documentRoot.charAt(0)<'A') || (documentRoot.charAt(0)>'Z'))
                {
                    String upperCaseDriveLetter=documentRoot.substring(0,1).toUpperCase();
                    documentRoot=upperCaseDriveLetter + documentRoot.substring(1);
                }
            }
        }

        return(documentRoot);
    }

    public void setPassword(String userId,String newPassword)
    {
        Element userElement=getUserElement(userId);

        if (userElement==null)
        {
            return;
        }

        try
        {
            MessageDigest md=MessageDigest.getInstance(ENCRYPTION_METHOD);

            byte[] encryptedPassword = md.digest(newPassword.getBytes());

            sun.misc.BASE64Encoder encoder=new sun.misc.BASE64Encoder();

            String encodedPassword=encoder.encodeBuffer(encryptedPassword).trim();

            XmlUtil.setChildText(userElement,"password",encodedPassword);

            Element passwordElement=XmlUtil.getChildByTagName(userElement,"password");

            passwordElement.setAttribute("encryption", ENCRYPTION_METHOD);
        }
        catch (java.security.NoSuchAlgorithmException nsaEx)
        {
        	Logger.getLogger(getClass()).error("nsa");
        }
    }

    public boolean checkPassword(String userId,String password)
    {
        if ((password==null) || (password.trim().length()==0))
        {
            return(false);
        }
        
        Element userElement=getUserElement(userId);

        if (userElement==null)
        {
            return(false);
        }

        Element passwordElement=XmlUtil.getChildByTagName(userElement,"password");

        if (passwordElement==null)
        {
            return(false);
        }

        String storedPassword=XmlUtil.getElementText(passwordElement);

        String encryptionMethod=passwordElement.getAttribute("encryption");

        if ((encryptionMethod==null) || encryptionMethod.equalsIgnoreCase("none") ||
            (encryptionMethod.trim().length()==0))
        {
            return(password.equals(storedPassword));
        }

        try
        {
            MessageDigest md=MessageDigest.getInstance(encryptionMethod);

            byte[] encryptedPassword = md.digest(password.getBytes());

            sun.misc.BASE64Encoder encoder=new sun.misc.BASE64Encoder();

            String encodedPassword=encoder.encodeBuffer(encryptedPassword).trim();

            return(encodedPassword.equals(storedPassword));
        }
        catch (java.security.NoSuchAlgorithmException nsaEx)
        {
        	Logger.getLogger(getClass()).error("nsa");
            return(false);
        }
    }

    public boolean checkReadonlyPassword(String userId,String password)
    {
        if ((password==null) || (password.trim().length()==0))
        {
            return(false);
        }

        Element userElement=getUserElement(userId);

        if (userElement==null)
        {
            return(false);
        }

        Element passwordElement=XmlUtil.getChildByTagName(userElement,"read-password");

        if (passwordElement==null)
        {
            return(false);
        }

        String storedPassword=XmlUtil.getElementText(passwordElement);

        String encryptionMethod=passwordElement.getAttribute("encryption");

        if ((encryptionMethod==null) || encryptionMethod.equalsIgnoreCase("none") ||
            (encryptionMethod.trim().length()==0))
        {
            return(password.equals(storedPassword));
        }

        try
        {
            MessageDigest md=MessageDigest.getInstance(encryptionMethod);

            byte[] encryptedPassword = md.digest(password.getBytes());

            sun.misc.BASE64Encoder encoder=new sun.misc.BASE64Encoder();

            String encodedPassword=encoder.encodeBuffer(encryptedPassword).trim();

            return(encodedPassword.equals(storedPassword));
        }
        catch (java.security.NoSuchAlgorithmException nsaEx)
        {
        	Logger.getLogger(getClass()).error("nsa");
            return(false);
        }
    }

    public void setReadonlyPassword(String userId,String newPassword)
    {
        Element userElement=getUserElement(userId);

        if (userElement==null)
        {
            return;
        }

        String encryptionMethod = "MD5";

        try
        {
            MessageDigest md=MessageDigest.getInstance(encryptionMethod);

            byte[] encryptedPassword = md.digest(newPassword.getBytes());

            sun.misc.BASE64Encoder encoder=new sun.misc.BASE64Encoder();

            String encodedPassword=encoder.encodeBuffer(encryptedPassword).trim();

            XmlUtil.setChildText(userElement,"read-password",encodedPassword);

            Element passwordElement=XmlUtil.getChildByTagName(userElement,"read-password");

            passwordElement.setAttribute("encryption",encryptionMethod);
        }
        catch (java.security.NoSuchAlgorithmException nsaEx)
        {
        	Logger.getLogger(getClass()).error("nsa");
        }
    }

    public void setLastLoginTime(String userId,Date newVal)
    {
        Element userElement=getUserElement(userId);

        if (userElement==null)
        {
            return;
        }

        XmlUtil.setChildText(userElement,"lastLogin",Long.toString(newVal.getTime()));

        modified=true;
    }

    public Date getLastLoginTime(String userId)
    {
        Date lastLoginTime=null;

        Element userElement=getUserElement(userId);

        if (userElement==null)
        {
            return(lastLoginTime);
        }

        String timeString=XmlUtil.getChildText(userElement,"lastLogin");

        if ((timeString!=null) && (timeString.trim().length()>0))
        {
            try
            {
                lastLoginTime=new Date(Long.parseLong(timeString));
            }
            catch (NumberFormatException nfex)
            {
            	Logger.getLogger(getClass()).warn("invalid last login time " + timeString);
            }
        }

        return(lastLoginTime);
    }

    /**
     * Create and fill a transient user object from a DOM user element.
     * @param userElement the DOM user element
     * @return an transient user object
     */
	public TransientUser createTransientUser(Element userElement)
	{
        TransientUser user = new TransientUser();

        user.setUserid(userElement.getAttribute("id"));

		String readonly=XmlUtil.getChildText(userElement,"readonly");

		user.setReadonly((readonly!=null) && readonly.equals("true"));
		
		user.setEmail(XmlUtil.getChildText(userElement,"email"));

		user.setRole(XmlUtil.getChildText(userElement,"role"));

		user.setFirstName(XmlUtil.getChildText(userElement,"firstName"));
		
		user.setLastName(XmlUtil.getChildText(userElement,"lastName"));

		user.setPhone(XmlUtil.getChildText(userElement,"phone"));

		user.setLanguage(XmlUtil.getChildText(userElement,"language"));

		user.setCss(XmlUtil.getChildText(userElement,"css"));


		String documentRoot=XmlUtil.getChildText(userElement,"documentRoot");

		if ((documentRoot==null) || (documentRoot.trim().length()==0))
		{
			if (File.separatorChar=='/')
			{
				documentRoot="/tmp";
			}

			documentRoot="c:\\temp";
		}

		user.setDocumentRoot(documentRoot);


		long diskQuota=(-1l);

		String quotaString=XmlUtil.getChildText(userElement,"diskQuota");

		if ((quotaString!=null) && (quotaString.trim().length()>0))
		{
			try
			{
				diskQuota=Long.parseLong(quotaString);
			}
			catch (NumberFormatException nfex)
			{
			}
		}

		user.setDiskQuota(diskQuota);
		
		String timeString=XmlUtil.getChildText(userElement,"lastLogin");

		if ((timeString!=null) && (timeString.trim().length()>0))
		{
			try
			{
				user.setLastLogin(new Date(Long.parseLong(timeString)));
			}
			catch (NumberFormatException nfex)
			{
			}
		}

		return(user);
	}

    /**
     * Get a list of transient user objects for all non-virtual users.
     */
	public Vector getRealUsers()
	{
		Vector listOfUsers=new Vector();

		NodeList userList=userRoot.getElementsByTagName("user");

		if (userList==null)
		{
			return(listOfUsers);
		}

		int listLength=userList.getLength();

		for (int i=0;i<listLength;i++)
		{
			Element userElement=(Element) userList.item(i);

			String userType=userElement.getAttribute("type");

			if ((userType==null) || (!userType.equals("virtual")))
			{
				listOfUsers.add(this.createTransientUser(userElement));
			}
		}

		return(listOfUsers);
	}

	protected String generateAccessCode()
	{
		String now=Long.toString(System.currentTimeMillis());

		StringBuffer codeBuffer=new StringBuffer();

		for (int i=now.length()-1;i>=0;i--)
		{
			 codeBuffer.append(now.charAt(i));
			 codeBuffer.append((char) ('A' + i));
		}

		return(codeBuffer.toString());
	}

    public synchronized void run()
    {
        boolean exitFlag=false;

        while (!exitFlag)
        {
            try
            {
                this.wait(60000);

                if (modified)
                {
                    saveToFile();
                }
            }
            catch (InterruptedException e)
            {
                if (modified)
                {
                    saveToFile();
                }

                readyForShutdown=true;
                
                exitFlag=true;
            }
        }
    }

    public boolean isReadyForShutdown()
    {
        return(readyForShutdown);
    }

}
