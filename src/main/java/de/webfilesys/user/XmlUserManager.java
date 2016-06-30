package de.webfilesys.user;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;

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
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

public class XmlUserManager extends UserManagerBase
{
    public static final String USER_FILE_NAME = "users.xml";

    private static final String ENCRYPTION_METHOD_MD5 = "MD5";
    private static final String ENCRYPTION_METHOD_SHA256 = "SHA-256";
    
    private Document doc;

    private DocumentBuilder builder;

    private Element userRoot=null;

    private boolean modified;

    private Hashtable<String, Element> userCache;

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
       
       userCache = new Hashtable(10);

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
     * Returns a list of the userids of all registered users (Strings)
     */
    public ArrayList<String> getListOfUsers()
    {
    	ArrayList<String> listOfUsers = new ArrayList<String>();

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

    public ArrayList<String> getAdminUserEmails()
    {
        ArrayList<String> emailList = new ArrayList<String>();

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

    public ArrayList<String> getMailAddressesByRole(String receiverRole)
    {
    	ArrayList<String> emailList = new ArrayList<String>();

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

    public ArrayList<String> getAllMailAddresses()
    {
    	ArrayList<String> emailList = new ArrayList<String>();

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

    /**
     * Create a new user.
     * 
     * @param newUser the data of the new user
     * @exception UserMgmtException user could not be created
     */
    public void createUser(TransientUser newUser) 
    throws UserMgmtException {
    	if (newUser == null) {
    		throw new UserMgmtException("user is null");
    	}
    	
    	if (userExists(newUser.getUserid())) {
    		throw new UserMgmtException("a user with this name already exists");
    	}
    	
        Element newUserElement = doc.createElement("user");

        newUserElement.setAttribute("id", newUser.getUserid());
        
        if (!CommonUtils.isEmpty(newUser.getUserType())) {
        	newUserElement.setAttribute("type", newUser.getUserType());
        }
        
        if (!CommonUtils.isEmpty(newUser.getRole())) {
            XmlUtil.setChildText(newUserElement, "role", newUser.getRole());
        }
        
        if (!CommonUtils.isEmpty(newUser.getFirstName())) {
            XmlUtil.setChildText(newUserElement, "firstName", newUser.getFirstName());
        }

        if (!CommonUtils.isEmpty(newUser.getLastName())) {
            XmlUtil.setChildText(newUserElement, "lastName", newUser.getLastName());
        }
        
        if (!CommonUtils.isEmpty(newUser.getEmail())) {
            XmlUtil.setChildText(newUserElement, "email", newUser.getEmail());
        }
        
        if (!CommonUtils.isEmpty(newUser.getPhone())) {
            XmlUtil.setChildText(newUserElement, "phone", newUser.getPhone());
        }
        
        if (newUser.getDiskQuota() != 0l) {
            XmlUtil.setChildText(newUserElement, "diskQuota", Long.toString(newUser.getDiskQuota()));
        }
        
        if (!CommonUtils.isEmpty(newUser.getLanguage())) {
            XmlUtil.setChildText(newUserElement, "language", newUser.getLanguage());
        }
        
        if (!CommonUtils.isEmpty(newUser.getCss())) {
            XmlUtil.setChildText(newUserElement, "css", newUser.getCss());
        }
        
        if (!CommonUtils.isEmpty(newUser.getDocumentRoot())) {
            XmlUtil.setChildText(newUserElement, "documentRoot", normalizeDocRoot(newUser.getDocumentRoot()));
        }
        
        if (newUser.isReadonly()) {
            XmlUtil.setChildText(newUserElement, "readonly", "true");
        } else {
            XmlUtil.setChildText(newUserElement,"readonly","false");
        }
        
        XmlUtil.setChildText(newUserElement, "pageSize", Integer.toString(newUser.getPageSize()));
        
        userRoot.appendChild(newUserElement);

        userCache.put(newUser.getUserid(), newUserElement);

        setPassword(newUser.getUserid(), newUser.getPassword());
        
        if (!CommonUtils.isEmpty(newUser.getReadonlyPassword())) {
            setReadonlyPassword(newUser.getUserid(), newUser.getReadonlyPassword());
        }
        
        modified = true;
    }
    
    /**
     * Update an existing user.
     * 
     * @param changedUser the data of the changed user
     * @exception UserMgmtException user could not be updated
     */
    public void updateUser(TransientUser changedUser) 
    throws UserMgmtException {
    	if (changedUser == null) {
    		throw new UserMgmtException("user for update is null");
    	}

    	Element userElem = getUserElement(changedUser.getUserid());
    	if (userElem == null) {
    		throw new UserMgmtException("user for update operation not found");
    	}
    	
        if (!CommonUtils.isEmpty(changedUser.getRole())) {
            XmlUtil.setChildText(userElem, "role", changedUser.getRole());
        }
        
        XmlUtil.setChildText(userElem, "firstName", changedUser.getFirstName());

        if (!CommonUtils.isEmpty(changedUser.getLastName())) {
            XmlUtil.setChildText(userElem, "lastName", changedUser.getLastName());
        }
        
        if (!CommonUtils.isEmpty(changedUser.getEmail())) {
            XmlUtil.setChildText(userElem, "email", changedUser.getEmail());
        }
        
        XmlUtil.setChildText(userElem, "phone", changedUser.getPhone());
        
        XmlUtil.setChildText(userElem, "diskQuota", Long.toString(changedUser.getDiskQuota()));
        
        if (!CommonUtils.isEmpty(changedUser.getLanguage())) {
            XmlUtil.setChildText(userElem, "language", changedUser.getLanguage());
        }
        
        if (!CommonUtils.isEmpty(changedUser.getCss())) {
            XmlUtil.setChildText(userElem, "css", changedUser.getCss());
        }
        
        if (!CommonUtils.isEmpty(changedUser.getDocumentRoot())) {
            XmlUtil.setChildText(userElem, "documentRoot", normalizeDocRoot(changedUser.getDocumentRoot()));
        }
        
        if (changedUser.isReadonly()) {
            XmlUtil.setChildText(userElem, "readonly", "true");
        } else {
            XmlUtil.setChildText(userElem,"readonly","false");
        }
        
        if (!CommonUtils.isEmpty(changedUser.getPassword())) {
            setPassword(changedUser.getUserid(), changedUser.getPassword());
        }
        
        if (!CommonUtils.isEmpty(changedUser.getReadonlyPassword())) {
            setReadonlyPassword(changedUser.getUserid(), changedUser.getReadonlyPassword());
        }
        
        modified = true;
    }
    
    /** 
     * Get the user with the given userId.
     * 
     * @param userId the userid of the user
     * @return user object or null if not found
     */
    public TransientUser getUser(String userId) {
    	Element userElem = getUserElement(userId);
    	
    	if (userElem == null) {
    		return null;
    	}
    	
    	return getTransientUser(userElem);
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

    public String createVirtualUser(String realUser, String docRoot, String role, int expDays, String language) {
        String virtualUserId=null;

        int i=1;

        do {
            virtualUserId = realUser + "-" + i;
            i++;
        } while (userExists(virtualUserId));
        
        TransientUser virtualUser = new TransientUser();

        virtualUser.setUserType(UserManager.USER_TYPE_VIRTUAL);
        virtualUser.setUserid(virtualUserId);
        virtualUser.setPassword("public");
        virtualUser.setDocumentRoot(docRoot);
        virtualUser.setReadonly(true);
        virtualUser.setRole(role);
        virtualUser.setCss(getCSS(realUser));
        virtualUser.setPageSize(getPageSize(realUser));

        if (CommonUtils.isEmpty(language)) {
            virtualUser.setLanguage(getLanguage(realUser));
        } else {
            virtualUser.setLanguage(language);
        }
        
        try {
            createUser(virtualUser);
        } catch (UserMgmtException ex) {
        	Logger.getLogger(getClass()).warn("failed to create virtual user " + virtualUserId, ex);
        }

        modified = true;

        return(virtualUserId);
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
      
    public String getLastName(String userId)
    {
        Element userElement=getUserElement(userId);

        if (userElement==null)
        {
            return(null);
        }

        return(XmlUtil.getChildText(userElement,"lastName"));
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

    public String getRole(String userId)
    {
        Element userElement=getUserElement(userId);

        if (userElement==null)
        {
            return(null);
        }

        return(XmlUtil.getChildText(userElement,"role"));
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

    private String encryptPassword(String cleartextPassword, String encryptionMethod) {
    	try {
            MessageDigest md=MessageDigest.getInstance(encryptionMethod);

            byte[] encryptedPassword = md.digest(cleartextPassword.getBytes());

            sun.misc.BASE64Encoder encoder=new sun.misc.BASE64Encoder();

            return encoder.encodeBuffer(encryptedPassword).trim();
    	} catch (java.security.NoSuchAlgorithmException nsaEx) {
    	    Logger.getLogger(getClass()).error("failed to encrypt password", nsaEx);
        	return "";
        }
    }
    
    public void setPassword(String userId, String newPassword) {
        Element userElement = getUserElement(userId);

        if (userElement == null) {
            return;
        }

        XmlUtil.setChildText(userElement, "password", encryptPassword(newPassword, ENCRYPTION_METHOD_SHA256));

        Element passwordElement = XmlUtil.getChildByTagName(userElement, "password");

        passwordElement.setAttribute("encryption", ENCRYPTION_METHOD_SHA256);
    }

    public boolean checkPassword(String userId,String password)
    {
        if ((password == null) || (password.trim().length() == 0))
        {
            return(false);
        }
        
        Element userElement = getUserElement(userId);

        if (userElement  == null)
        {
            return(false);
        }

        Element passwordElement = XmlUtil.getChildByTagName(userElement,"password");

        if (passwordElement == null)
        {
            return(false);
        }

        String storedPassword = XmlUtil.getElementText(passwordElement);

        String encryptionMethod = passwordElement.getAttribute("encryption");

        if ((encryptionMethod == null) || encryptionMethod.equalsIgnoreCase("none") ||
            (encryptionMethod.trim().length() == 0)) {
            return(password.equals(storedPassword));
        }

        String encryptedPassword = encryptPassword(password, encryptionMethod);

        return(encryptedPassword.equals(storedPassword));
    }

    public boolean checkReadonlyPassword(String userId, String password)
    {
        if ((password == null) || (password.trim().length() == 0)) {
            return(false);
        }

        Element userElement = getUserElement(userId);

        if (userElement == null) {
            return(false);
        }

        Element passwordElement = XmlUtil.getChildByTagName(userElement,"read-password");

        if (passwordElement == null) {
            return(false);
        }

        String storedPassword = XmlUtil.getElementText(passwordElement);

        String encryptionMethod = passwordElement.getAttribute("encryption");

        if ((encryptionMethod == null) || encryptionMethod.equalsIgnoreCase("none") ||
            (encryptionMethod.trim().length() == 0)) {
            return(password.equals(storedPassword));
        }

        String encryptedPassword = encryptPassword(password, encryptionMethod);

        return(encryptedPassword.equals(storedPassword));
    }

    public void setReadonlyPassword(String userId,String newPassword)
    {
        Element userElement = getUserElement(userId);

        if (userElement == null)
        {
            return;
        }

        XmlUtil.setChildText(userElement, "read-password", encryptPassword(newPassword, ENCRYPTION_METHOD_SHA256));

        Element passwordElement=XmlUtil.getChildByTagName(userElement, "read-password");

        passwordElement.setAttribute("encryption", ENCRYPTION_METHOD_SHA256);
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
	public TransientUser getTransientUser(Element userElement)
	{
        TransientUser user = new TransientUser();

        user.setUserid(userElement.getAttribute("id"));
        
        String userType = userElement.getAttribute("type");
        if (CommonUtils.isEmpty(userType)) {
            user.setUserType(UserManager.USER_TYPE_DEFAULT);
        } else {
            user.setUserType(userType);
        }

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
			else
			{
				documentRoot="c:\\temp";
			}
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
		
		int pageSize = UserManager.DEFAULT_THUMB_PAGE_SIZE;

		String pageSizeString = XmlUtil.getChildText(userElement, "pageSize");

		if (!CommonUtils.isEmpty(pageSizeString)) {
			try {
				pageSize = Integer.parseInt(pageSizeString);
			} catch (NumberFormatException nfex) {
			}
		}

		user.setPageSize(pageSize);
		
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
	public ArrayList<TransientUser> getRealUsers()
	{
		ArrayList<TransientUser> listOfUsers = new ArrayList<TransientUser>();

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
				listOfUsers.add(this.getTransientUser(userElement));
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
