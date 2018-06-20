package de.webfilesys.viewhandler;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.cert.Certificate;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import javax.security.auth.x500.X500Principal;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import de.webfilesys.ViewHandlerConfig;
import de.webfilesys.util.CommonUtils;

/**
 * Show the content of Java keystore files.
 * 
 * @author Frank Hoehnel
 */
public class KeyStoreViewHandler implements ViewHandler {
	
	private static Logger LOG = Logger.getLogger(KeyStoreViewHandler.class);
	
	public void process(String filePath, ViewHandlerConfig viewHandlerConfig, HttpServletRequest req,
			HttpServletResponse resp) {

		String keyStorePassword = req.getParameter("passwd");
		
		if (CommonUtils.isEmpty(keyStorePassword)) {
			sendPasswordPrompt(req, resp, filePath);
			return;
		}
		
		try {
			PrintWriter output = resp.getWriter();
			
			output.println("<html>");
			output.println("<head>");
			output.println("<title>WebFileSys KeyStore viewer</title>");
			output.println("</head>");
			output.println("<body>");

			output.println("<div style=\"font-family:Arial,Helvetica;font-size:16px;color:navy;margin-bottom:16px\">Contents of keystore " + CommonUtils.extractFileName(filePath) + "</div>");
			
		    FileInputStream fis = null;
		    try {
			    KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

		        fis = new java.io.FileInputStream(filePath);
		        keyStore.load(fis, keyStorePassword.toCharArray());
		        
		        Enumeration<String> aliases = keyStore.aliases();
		        
		        boolean empty = true;
		        
		        if (aliases.hasMoreElements()) {
		        	output.println("<table style=\"border:1px solid #a0a0a0;font-family:Arial,Helvetica;font-size:16px;border-collapse:collapse\">");
		        	empty = false;
		        }

                ArrayList<String> sortList = new ArrayList<String>();
                
		        while (aliases.hasMoreElements()) {
		            sortList.add(aliases.nextElement());
		        }
		        
		        if (sortList.size() > 1) {
		        	Collections.sort(sortList);
		        }
		        
		        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		        
		        for (String alias : sortList) {
		        	output.println("<tr style=\"background-color:lavender\"><td>alias:</td><td>" + alias + "</td></tr>");
		        	
		        	KeyStore.Entry entry = null;
		        	
                    try {
    		        	entry = keyStore.getEntry(alias, null);
                    } catch (UnrecoverableKeyException ukEx) {
    		        	try {
    		        		entry = keyStore.getEntry(alias,  new KeyStore.PasswordProtection(keyStorePassword.toCharArray()));
    		        	} catch (Exception ex) {
    		        		LOG.warn("failed to determine type of keystore entry", ex);
    		        	}
                    }
		        	
		        	String entryType = "unknown";
		        	if (entry != null) {
			        	if (entry instanceof KeyStore.PrivateKeyEntry) {
			        		entryType = "private key";
			        	} else if (entry instanceof KeyStore.SecretKeyEntry) {
			        		entryType = "secret key";
			        	} else if (entry instanceof KeyStore.TrustedCertificateEntry) {
			        		entryType = "trusted certificate";
			        	}
		        	}
		        	output.println("<tr style=\"background-color:ivory\"><td style=\"white-space:nowrap\">entry type:</td><td>" + entryType + "</td></tr>");
		        	
		        	Certificate cert = keyStore.getCertificate(alias);
		        	
		        	output.println("<tr style=\"background-color:ivory\"><td style=\"white-space:nowrap\">certificate type:</td><td>" + cert.getType() + "</td></tr>");

		        	if (cert instanceof X509Certificate) {
		        		X509Certificate x509Cert = (X509Certificate) cert;

		        		boolean valid = true;
			        	
			        	try {
			        	    x509Cert.checkValidity();
			        	} catch (CertificateExpiredException expEx) {
			        		valid = false;
			        	} catch (CertificateNotYetValidException nyvEx) {
			        		valid = false;
			        	}
			        	
                        String validFrom = dateFormat.format(x509Cert.getNotBefore());
                        String validUntil = dateFormat.format(x509Cert.getNotAfter());
			        	
			        	output.println("<tr style=\"background-color:ivory\"><td>valid:</td><td>" + Boolean.toString(valid) + " (from " + validFrom + " until " + validUntil + ")</td></tr>");
		        		
			        	X500Principal issuer = x509Cert.getIssuerX500Principal();
			        	
			        	if (issuer != null) {
				        	output.println("<tr style=\"background-color:ivory\"><td style=\"vertical-align:top\">issuer:</td><td>" + issuer.getName() + "</td></tr>");
			        	}
			        	
			        	X500Principal subject = x509Cert.getSubjectX500Principal();

			        	if (subject != null) {
				        	output.println("<tr style=\"background-color:ivory\"><td style=\"vertical-align:top\">subject:</td><td>" + subject.getName() + "</td></tr>");
			        	}
		        	}
		        }
		        if (!empty) {
		        	output.println("</table>");
		        }
		    } catch (KeyStoreException keyEx) {
		    	LOG.warn("failed to load keystore " + filePath, keyEx);
		    	output.println("failed to load keystore: " + keyEx);
		    } catch (CertificateException certEx) {
		    	LOG.warn("failed to load keystore " + filePath, certEx);
		    	output.println("failed to load keystore: " + certEx);
		    } catch (NoSuchAlgorithmException nsaEx) {
		    	LOG.warn("failed to load keystore " + filePath, nsaEx);
		    	output.println("failed to load keystore: " + nsaEx);
		    } catch (IOException ioEx) {
		    	LOG.warn("failed to load keystore " + filePath, ioEx);
		    	output.println("failed to load keystore: " + ioEx);
			} catch (UnrecoverableEntryException ueEx) {
		    	LOG.warn("failed to load keystore " + filePath, ueEx);
		    	output.println("failed to load keystore: " + ueEx);
			} finally {
		        if (fis != null) {
		        	try {
			            fis.close();
		        	} catch (Exception ex) {
		        	}
		        }
		    }

		    output.println("</body>");
			output.println("</html>");
			output.flush();
		} catch (IOException ex) {
	    	LOG.warn("failed to list keystore content" + filePath, ex);
		}
	}

	private void sendPasswordPrompt(HttpServletRequest req, HttpServletResponse resp, String keyStoreFilePath) {
		
		try {
			PrintWriter output = resp.getWriter();

			output.println("<html>");
			output.println("<head>");
			output.println("<title>WebFileSys KeyStore viewer</title>");
			output.println("</head>");
			output.println("<body style=\"font-family:Arial,Helvetica;font-size:16px\">");
			output.println("<form method=\"get\" action=\"" + req.getRequestURI() + "\">");
			output.println("<label>password for keystore " + CommonUtils.extractFileName(keyStoreFilePath) + ":</label>");
			output.println("<input type=\"password\" name=\"passwd\" style=\"width:120px;\">");
			output.println("<input type=\"submit\" style=\"width:60px;\" value=\"OK\">");
			output.println("</form>");
			output.println("</body>");
			output.println("</html>");
			output.flush();
		} catch (IOException e) {
			Logger.getLogger(getClass()).error("failed to send keystore password prompt",e);
		}		
	}

	/**
	 * Does this ViewHandler support reading the file from an input stream of a
	 * ZIP archive?
	 * 
	 * @return true if reading from ZIP archive is supported, otherwise false
	 */
	public boolean supportsZipContent() {
		return false;
	}

	public void processZipContent(String fileName, InputStream zipIn, ViewHandlerConfig viewHandlerConfig,
			HttpServletRequest req, HttpServletResponse resp) {
		// ZIP not supported
	}

}
