package de.webfilesys.user;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


import de.webfilesys.WebFileSys;
import de.webfilesys.util.CommonUtils;

/**
 * This UserManager implementation stores the webfilesys user data in a cached XML file but authenticates users against an LDAP server.
 * If the LDAP authentication succeeds, but the user is unknown to the webfilesys user database, the user is created and some essential 
 * user data are copied from LDAP to the webfilesys user db. Some other user attributes will get default values, 
 * for example role is always set to webspace. 
 *
 * For the LDAP authentication a LDAP user object with uid matching the userid from the login form is searched 
 * in the LDAP tree location specified by property usersBaseDN. If that user object is not found, it is searched 
 * in the LDAP organization tree below usersBaseDN. For this search an LDAP Connection with the credentials 
 * configured as ldapBindUser and ldapBindPassword is used. If the user object can be found in the LDAP tree, 
 * the authentication with the password entered in the login form is done against this LDAP user.
 * 
 * It is possible to configure that LDAP users are required to belong to a certain LDAP group to get access to webfilesys 
 * (property webfilesysUserGroup). For checking the group membership the LDAP connection is made with credentials 
 * configured as ldapBindUser and ldapBindPassword.
 * 
 * A custom SSL Socket factory can be configured for the LDAP connection. This is useful in test environments where LDAP servers
 * run with self-signed certificates. The TrustAllCertificatesSocketFactory disables the certificate checking.
 * 
 * rfc2798: Definition of the inetOrgPerson LDAP Object Class
 * https://tools.ietf.org/html/rfc2798
 */
public class LdapAuthenticatedXmlUserManager extends XmlUserManager {
	private static Logger LOG = LogManager.getLogger(LdapAuthenticatedXmlUserManager.class);
	
	private static final String DEFAULT_CSS = "fmweb";
	
	private static final String DEFAULT_LANGUAGE = "English";
	
	private static final String DEFAULT_ROLE = "webspace";
	
	public static final String USER_FILE_NAME = "users.xml";

	private static final String LDAP_CONFIG_FILE = "ldapConfig.properties";
	
	private static final String PROP_LDAP_SERVER_URL = "ldapServerUrl";
	private static final String PROP_LDAP_AUTH_TYPE = "ldapAuthType";
	
	private static final String PROP_LDAP_SSL_SOCKET_FACTORY = "ldapSSLSocketFactory";
	
	private static final String LDAP_ENV_SOCKET_FACTORY = "java.naming.ldap.factory.socket";
	
	private static final String PROP_LDAP_USERS_BASE_DN = "usersBaseDN";
	
	private static final String PROP_LDAP_USER_GROUP = "webfilesysUserGroup";

	private static final String PROP_LDAP_GROUP_BASE_DN = "groupBaseDN";
	
	private static final String PROP_ATTR_MAP_PREFIX = "attribute.map.";
	
	private static final String PROP_LDAP_BIND_USER = "ldapBindUser";
	private static final String PROP_LDAP_BIND_PASSWORD = "ldapBindPassword";
	
	private static final String LDAP_ATTR_FIRST_NAME = "givenName";
	private static final String LDAP_ATTR_LAST_NAME = "sn";
	private static final String LDAP_ATTR_LANGUAGE = "preferredLanguage";
	private static final String LDAP_ATTR_MAIL = "mail";
	private static final String LDAP_ATTR_PHONE = "telephoneNumber";

	private static final String LDAP_ATTR_GROUP_MEMBER = "memberUid";

	private static final String LDAP_ATTR_GID_NUMBER = "gidNumber";
	
	/** mapped LDAP attribute names for LDAP search */
	private String[] userAttribSet;
	
	/** unmapped LDAP attribute names for LDAP search */
	private static final String[] USER_ATTRIB_SET_KEYS = {
		LDAP_ATTR_FIRST_NAME, 
		LDAP_ATTR_LAST_NAME,
		LDAP_ATTR_MAIL, 
		LDAP_ATTR_LANGUAGE, 
		LDAP_ATTR_PHONE
	};
	
	private Properties ldapConfigProps;
	
	private String ldapUsersBaseDN;

	private String ldapGroupBaseDN;
	
	private String ldapUserGroup = null;
	
	private String ldapSSLSocketFactory;
	
	DirContext ldapManagerCtx = null;
	
    SearchControls searchControls;
	
	public LdapAuthenticatedXmlUserManager() {
		super();
		initLdap();
	}

	protected void initLdap() {
		File ldapConfigFile =  new File(WebFileSys.getInstance().getConfigBaseDir(), LDAP_CONFIG_FILE);
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("ldapConfigFile: " + ldapConfigFile);
		}
		
		if (ldapConfigFile.exists() && ldapConfigFile.isFile() && ldapConfigFile.canRead()) {
			
			FileReader fin = null;
			try {
				fin = new FileReader(ldapConfigFile);
				ldapConfigProps = new Properties();
			    ldapConfigProps.load(fin);
			    
			    ldapUsersBaseDN = ldapConfigProps.getProperty(PROP_LDAP_USERS_BASE_DN);
			    if (CommonUtils.isEmpty(ldapUsersBaseDN)) {
			    	LOG.error("LDAP configuration error: missing property " + PROP_LDAP_USERS_BASE_DN);
			    }
			    
			    ldapSSLSocketFactory = ldapConfigProps.getProperty(PROP_LDAP_SSL_SOCKET_FACTORY);
			    if (!CommonUtils.isEmpty(ldapSSLSocketFactory)) {
			    	if (LOG.isInfoEnabled()) {
			    		LOG.info("using alternative SSL socket factory " + ldapSSLSocketFactory);
			    	}
			    }
			    
			    ldapUserGroup = ldapConfigProps.getProperty(PROP_LDAP_USER_GROUP);
			    if (!CommonUtils.isEmpty(ldapUserGroup)) {
			    	if (LOG.isDebugEnabled()) {
			    		LOG.debug("LDAP user group for WebFileSys users: " + ldapUserGroup);
			    	}
			    	
			    	ldapGroupBaseDN = ldapConfigProps.getProperty(PROP_LDAP_GROUP_BASE_DN);
			    	
                    getLdapManagerCtx();			    	
			    }
			    
			    searchControls = new SearchControls();
			    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			    searchControls.setTimeLimit(30000);
			    
			} catch (IOException ioex) {
				LOG.error("failed to load LADP configuration from " + ldapConfigFile.getAbsolutePath());
			} finally {
				if (fin != null) {
					try {
						fin.close();
					} catch (Exception ex) {
					}
				}
			}
		} else {
			LOG.error("LDAP config file is not a readable file: " + ldapConfigFile.getAbsolutePath());
		}
		
		fillUserAttribSet();
	}
	
	private Hashtable<String, String> getBasicLdapEnv() {
		Hashtable<String, String> ldapEnv = new Hashtable<String, String>();
		ldapEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		ldapEnv.put(Context.PROVIDER_URL, ldapConfigProps.getProperty(PROP_LDAP_SERVER_URL));
		ldapEnv.put(Context.SECURITY_AUTHENTICATION, ldapConfigProps.getProperty(PROP_LDAP_AUTH_TYPE));
		
	    if (!CommonUtils.isEmpty(ldapSSLSocketFactory)) {
			ldapEnv.put(LDAP_ENV_SOCKET_FACTORY, ldapSSLSocketFactory);
	    }
		
		return ldapEnv;
	}
	
	private void fillUserAttribSet() {
		userAttribSet = new String[USER_ATTRIB_SET_KEYS.length];
		
		for (int i = 0; i < USER_ATTRIB_SET_KEYS.length; i++) {
			userAttribSet[i] = getLdapAttrName(USER_ATTRIB_SET_KEYS[i]);
		}
	}
	
	private String getLdapAttrName(String attrKey) {
		String mappedAttrName = ldapConfigProps.getProperty(PROP_ATTR_MAP_PREFIX + attrKey);
		if (mappedAttrName != null) {
			return mappedAttrName;
		}
		return attrKey;
	}
	
	private void getLdapManagerCtx() {
		Hashtable<String, String> ldapEnv = getBasicLdapEnv();
		ldapEnv.put(Context.SECURITY_PRINCIPAL, ldapConfigProps.getProperty(PROP_LDAP_BIND_USER)); 
		ldapEnv.put(Context.SECURITY_CREDENTIALS, ldapConfigProps.getProperty(PROP_LDAP_BIND_PASSWORD));  
	
		try {
			ldapManagerCtx = new InitialDirContext(ldapEnv);
		    if (ldapManagerCtx != null) {
		    	if (LOG.isDebugEnabled()) {
		    		LOG.debug("DirContext created for " + ldapConfigProps.getProperty(PROP_LDAP_BIND_USER));
		    	}
		    } 
		} catch (AuthenticationException authEx) {
			LOG.error("LDAP Authentication failed for bind user " + ldapConfigProps.getProperty(PROP_LDAP_BIND_USER), authEx);
		} catch (NamingException ex) {
			LOG.error("failed to get LDAP initial context for user " + ldapConfigProps.getProperty(PROP_LDAP_BIND_USER), ex);
		}
	}
	
	public boolean checkPassword(String userId, String password) {
		Hashtable<String, String> ldapEnv = getBasicLdapEnv();
		ldapEnv.put(Context.SECURITY_PRINCIPAL, "uid=" + userId + "," + ldapUsersBaseDN); 
		ldapEnv.put(Context.SECURITY_CREDENTIALS, password);           		

		DirContext ctx = null;
        String userObjectPath = null;
		String searchPath = ldapUsersBaseDN;
        
		try {
			ctx = new InitialDirContext(ldapEnv);
	    	if (LOG.isDebugEnabled()) {
	    		LOG.debug("DirContext created for " + userId);
	    	}
		} catch (NamingException ex) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("failed to get initial context in usersBaseDN for LDAP user " + userId);
			}
			
			if (this.ldapManagerCtx == null) {
				return false;
			}
			
	    	userObjectPath = searchUserInOrgTree(userId);
		}
		
		if (ctx == null) {
			if (userObjectPath != null) {
				ldapEnv.put(Context.SECURITY_PRINCIPAL, userObjectPath); 

				try {
					ctx = new InitialDirContext(ldapEnv);
			    	if (LOG.isDebugEnabled()) {
			    		LOG.debug("DirContext created for " + userObjectPath);
			    	}
			    	searchPath = userObjectPath.substring(userObjectPath.indexOf(',') + 1);
				} catch (NamingException ex) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("failed to get initial context for LDAP user " + userObjectPath);
					}
				}
			}
		}
		
		if (ctx != null) {
			try {
				if ((ldapUserGroup != null) && (!isMemberOfGroup(userId, ldapUserGroup)) && (!isPrimaryGroup(userId, searchPath, ldapUserGroup))) {
			    	if (LOG.isDebugEnabled()) {
			    		LOG.debug("user " + userId + " is not a member of the required LDAP group " + ldapUserGroup);
			    	}
			    	ctx.close();
			    	return false;
		    	}
		    	
		    	if (!userExists(userId)) {
		    		if (!createUserFromLdap(ctx, searchPath, userId, password)) {
				    	ctx.close();
		    			return false;
		    		}
		    	}
		    	
		    	ctx.close();
		    	return true;
			} catch (NamingException ex) {
				LOG.error("failed to close LDAP context", ex);
			}
		}
		
		return false;
	}
	
	private String searchUserInOrgTree(String userId) {
		try {
			NamingEnumeration answer = null;
			try {
				answer = ldapManagerCtx.search(ldapUsersBaseDN, "(uid=" + userId + ")", searchControls);
			} catch (CommunicationException ex) {
			    LOG.warn("LDAP communication error - trying to reconnect", ex);
			    getLdapManagerCtx();
			    try {
					answer = ldapManagerCtx.search(ldapUsersBaseDN, "(uid=" + userId + ")", searchControls);
			    } catch (Exception ex2) {
					LOG.error("LDAP search for user " + userId + " failed", ex2);
					return null;
			    }
			}
			
			if (answer.hasMoreElements()) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("user " + userId + " found in tree");
				}
				SearchResult searchResult = (SearchResult) answer.nextElement();
				return searchResult.getNameInNamespace();
			} else {
				if (LOG.isDebugEnabled()) {
					LOG.debug("user " + userId + " NOT found in tree");
				}
			}
		} catch (NamingException ex) {
			LOG.error("LDAP search for user " + userId + " failed", ex);
		}
		
		return null;
	}
	
	private boolean createUserFromLdap(DirContext ctx, String searchPath, String userId, String password) {
		TransientUser user = null;

		Attributes matchAttrs = new BasicAttributes(true);
		matchAttrs.put(new BasicAttribute("uid", userId));
		try {
			NamingEnumeration answer = ctx.search(searchPath, matchAttrs);
			
			if (answer.hasMoreElements()) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("LDAP user found: " + userId);
				}
				
				user = new TransientUser();
				
				user.setUserid(userId);
				user.setPassword(password);
				
				SearchResult searchResult = (SearchResult) answer.nextElement();
				Attributes attribs = searchResult.getAttributes();

				NamingEnumeration allAttribs = attribs.getAll();
				
				fillUserAttribs(user, allAttribs);
				
				user.setCss(DEFAULT_CSS);
				user.setRole(DEFAULT_ROLE);
				user.setDiskQuota(WebFileSys.getInstance().getDefaultDiskQuota());
				user.setPageSize(WebFileSys.getInstance().getThumbnailsPerPage());
				user.setLastLogin(new Date());
				
				if (CommonUtils.isEmpty(WebFileSys.getInstance().getUserDocRoot())) {
					LOG.error("no UserDocumentRoot configured but required to copy LDAP users");
					return false;
				}
				
				String homeDir = WebFileSys.getInstance().getUserDocRoot() + File.separator + userId;
				user.setDocumentRoot(homeDir);
				
				File docRootDir = new File(homeDir);
				if (!docRootDir.exists()) {
					if (!docRootDir.mkdirs()) {
						LOG.error("failed to create home dir " + homeDir + " for user copied from LDAP " + userId);
						return false;
					}
				}
				
				if (CommonUtils.isEmpty(user.getLanguage())) {
					user.setLanguage(DEFAULT_LANGUAGE);
				}
				
				if (CommonUtils.isEmpty(user.getEmail())) {
					LOG.error("failed to create user " + userId + ": e-mail address could not be read from LDAP");
					return false;
				} else {
					try {
						createUser(user);
						if (LOG.isInfoEnabled()) {
							LOG.info("user data for new user " + userId + " successfully copied from LDAP");
							return true;
						}
					} catch (UserMgmtException ex) {
	                    LOG.error("failed to create user " + userId + " from LADP data", ex);
						return false;
					}
				}
			} else {
                LOG.error("failed to create user " + userId + " from LADP data: LDAP search answer is empty");
				return false;
			}
		} catch (NamingException ex) {
            LOG.error("failed to create user " + userId + " from LADP data: LDAP search error", ex);
		}

		return false;
	}
	
	private void fillUserAttribs(TransientUser user, NamingEnumeration attribs) {
		try {
			while (attribs.hasMoreElements()) {
		        Attribute attr = (Attribute) attribs.next();

		        String attribName = attr.getID();
		        
		        String attribValue = null;
		        
		        NamingEnumeration values = attr.getAll();
		        if (values.hasMore()) {
		        	Object o = values.next(); 
		        	if (o instanceof String) {
		        		attribValue = (String) o;
		        	}
		        }
		        
		        if (attribValue != null) {
		        	if (attribName.equals(getLdapAttrName(LDAP_ATTR_FIRST_NAME))) {
						user.setFirstName(attribValue);
		        	} else if (attribName.equals(getLdapAttrName(LDAP_ATTR_LAST_NAME))) {
						user.setLastName(attribValue);
		        	} else if (attribName.equals(getLdapAttrName(LDAP_ATTR_MAIL))) {
						user.setEmail(attribValue);
		        	} else if (attribName.equals(getLdapAttrName(LDAP_ATTR_LANGUAGE))) {
						user.setLanguage(mapLanguage(attribValue));
		        	} else if (attribName.equals(getLdapAttrName(LDAP_ATTR_PHONE))) {
						user.setPhone(attribValue);
		        	}
		        }
		    }
		} catch (NamingException ex) {
			LOG.error("failed to fill user attribs from LDAP", ex);
		}
	}
	
	private String mapLanguage(String preferredLanguage) {
		if (CommonUtils.isEmpty(preferredLanguage)) {
			return DEFAULT_LANGUAGE;
		}
		if (preferredLanguage.startsWith("de")) {
			return "German";
		}
		if (preferredLanguage.startsWith("es")) {
			return "Spanish";
		}
		return DEFAULT_LANGUAGE;
	}
	
	private boolean isMemberOfGroup(String userId, String groupId) {
		Attributes matchAttrs = new BasicAttributes(true);
		matchAttrs.put(new BasicAttribute("cn", groupId));
		try {
			NamingEnumeration answer = ldapManagerCtx.search(ldapGroupBaseDN, matchAttrs);
			
			if (answer.hasMoreElements()) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("group " + groupId + " found");
				}
			} else {
				if (LOG.isDebugEnabled()) {
					LOG.debug("group " + groupId + " NOT found");
				}
			}
			
			while (answer.hasMoreElements()) {
				SearchResult searchResult = (SearchResult) answer.nextElement();
				Attributes attribs = searchResult.getAttributes();
				Attribute memberUidAttr = attribs.get(LDAP_ATTR_GROUP_MEMBER);
				if (memberUidAttr != null) {
					NamingEnumeration values = memberUidAttr.getAll();
					while (values.hasMore()) {
						String value = (String) values.next();
						if (value.equals(userId)) {
							return true;
						}
					}
				} else {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Attribute " + LDAP_ATTR_GROUP_MEMBER + " not found for group " + groupId);
					}
				}
			}
		} catch (NamingException ex) {
			LOG.error("failed to check group membership", ex);
		}
		return false;
	}
	
	private boolean isPrimaryGroup(String userId, String userSearchPath, String groupId) {
		String groupGidNumber = getSingleLdapAttribute(ldapGroupBaseDN, groupId, "cn", LDAP_ATTR_GID_NUMBER);

		String userGidNumber = null;
		if (userSearchPath != null) {
			userGidNumber = getSingleLdapAttribute(userSearchPath, userId, "uid", LDAP_ATTR_GID_NUMBER);
		} else {
			userGidNumber = getSingleLdapAttribute(ldapGroupBaseDN, userId, "uid", LDAP_ATTR_GID_NUMBER);
		}

        if ((groupGidNumber == null) || (userGidNumber == null)) {
        	return false;
        }
        
        if (LOG.isDebugEnabled()) {
        	if (groupGidNumber.equals(userGidNumber)) {
            	LOG.debug(groupId + " is the primary group of user " + userId);
        	}
        }
        
        return (groupGidNumber.equals(userGidNumber));
	}
	
	private String getSingleLdapAttribute(String searchPath, String pricipalId, String idAttr, String attribName) {
		Attributes matchAttrs = new BasicAttributes(true);
		matchAttrs.put(new BasicAttribute(idAttr, pricipalId));
		try {
			NamingEnumeration answer = ldapManagerCtx.search(searchPath, matchAttrs);
			
			if (answer.hasMoreElements()) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("LDAP principal " + pricipalId + " found");
				}
			} else {
				if (LOG.isDebugEnabled()) {
					LOG.debug("LDAP principal " + pricipalId + " NOT found");
				}
			}
			
			while (answer.hasMoreElements()) {
				SearchResult searchResult = (SearchResult) answer.nextElement();
				Attributes attribs = searchResult.getAttributes();
				Attribute attr = attribs.get(attribName);
				if (attr != null) {
					NamingEnumeration values = attr.getAll();
					if (values.hasMore()) {
						String attribValue = (String) values.next();
						if (LOG.isDebugEnabled()) {
							LOG.debug("Attribute " + attribName + " for principal " + pricipalId + ": " + attribValue);
						}
						return attribValue;
					} else {
						if (LOG.isDebugEnabled()) {
							LOG.debug("Attribute " + attribName + " not found for principal " + pricipalId);
						}
					}
				} else {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Attribute " + attribName + " not found for principal " + pricipalId);
					}
				}
			}
		} catch (NamingException ex) {
			LOG.error("failed to get single LDAP attribute", ex);
		}
		return null;
	}

}
