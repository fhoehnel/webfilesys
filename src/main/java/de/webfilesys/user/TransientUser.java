package de.webfilesys.user;

import java.util.Date;

/**
 * This is a transient container class for user data.
 *
 * @author Frank Hoehnel
 */
public class TransientUser 
{
	/** the userid of the user */
    private String userid = null;
    
    /** type of user (real or virtual) */
    private String userType = null;
    
    /** the password */
    private String password = null;
    
    /** the read-only password */
    private String readonlyPassword = null;
    
    /** the root directory for the user's file system access */
    private String documentRoot = null;

    /** the maximum size of all files below the document root */
    private long diskQuota = 0L;

    /** user has read-only access only */    
    private boolean readonly = false;
    
    /** the role of the user (can be "admin", "user", "webspace") */
    private String role = null;
    
    /** the language */
    private String language = null;
    
    /** the name of the CSS stylesheet for the user interface */
    private String css = null;
    
    /** the first name */
    private String firstName = null;
    
    /** the last name */
    private String lastName = null;
    
    /** the e-mail address */
	private String email = null;
    
    /** the phone number */
    private String phone = null;
    
    /** date of the last login */
    private Date lastLogin = null;
    
    /** activation code for self registration */
    private String activationCode = null;

    private long activationCodeExpiration = 0l;
    
    private boolean activated;
    
    /** number of picture thumbnails per page */
    private int pageSize = UserManager.DEFAULT_THUMB_PAGE_SIZE;
    
    public String getCss()
    {
        return css;
    }

    public long getDiskQuota()
    {
        return diskQuota;
    }

    public String getDocumentRoot()
    {
        return documentRoot;
    }

    public String getEmail()
    {
        return email;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public String getLanguage()
    {
        return language;
    }

    public String getLastName()
    {
        return lastName;
    }

    public String getPhone()
    {
        return phone;
    }

    public boolean isReadonly()
    {
        return readonly;
    }

    public String getRole()
    {
        return role;
    }

    public String getUserid()
    {
        return userid;
    }

    public Date getLastLogin()
    {
    	return(lastLogin);
    }
    
    public void setLastLogin(Date newVal)
    {
    	lastLogin = newVal;
    }

    public void setCss(String newVal)
    {
        css = newVal;
    }

    public void setDiskQuota(long l)
    {
        diskQuota = l;
    }

    public void setDocumentRoot(String newVal)
    {
        documentRoot = newVal;
    }

    public void setEmail(String newVal)
    {
        email = newVal;
    }

    public void setFirstName(String newVal)
    {
        firstName = newVal;
    }

    public void setLanguage(String newVal)
    {
        language = newVal;
    }

    public void setLastName(String newVal)
    {
        lastName = newVal;
    }

    public void setPhone(String newVal)
    {
        phone = newVal;
    }

    public void setReadonly(boolean b)
    {
        readonly = b;
    }

    public void setRole(String newVal)
    {
        role = newVal;
    }

    public void setUserid(String newVal)
    {
        userid = newVal;
    }
    
    public void setPassword(String newVal) {
    	password = newVal;
    }
    
    public String getPassword() {
    	return password;
    }
    
    public void setReadonlyPassword(String newVal) {
    	readonlyPassword = newVal;
    }
    
    public String getReadonlyPassword() {
    	return readonlyPassword;
    }
    
    public void setUserType(String newVal) {
        userType = newVal;
    }
    
    public String getUserType() {
    	return userType;
    }
    
    public void setPageSize(int newVal) {
    	pageSize = newVal;
    }
    
    public int getPageSize() {
    	return pageSize;
    }

    public void setActivationCode(String newVal) {
        activationCode = newVal;
    }

    public String getActivationCode() {
        return activationCode;
    }
    
    public void setActivationCodeExpiration(long newVal) {
        activationCodeExpiration = newVal;
    }

    public long getActivationCodeExpiration() {
        return activationCodeExpiration;
    }

    public void setActivated(boolean newVal) {
        activated = newVal;
    }

    public boolean isActivated() {
        return activated;
    }
}

