package de.webfilesys.viewhandler;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;

import javax.security.auth.x500.X500Principal;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import de.webfilesys.ViewHandlerConfig;
import de.webfilesys.util.CommonUtils;

/**
 * Show the content of certificate files.
 * 
 * @author Frank Hoehnel
 */
public class CertificateFileViewHandler implements ViewHandler {
	
	private static Logger LOG = Logger.getLogger(CertificateFileViewHandler.class);
	
	public void process(String filePath, ViewHandlerConfig viewHandlerConfig, HttpServletRequest req,
			HttpServletResponse resp) {

		try {
			PrintWriter output = resp.getWriter();
			
			output.println("<html>");
			output.println("<head>");
			output.println("<title>WebFileSys certificate file viewer</title>");
			output.println("<style>td {padding:2px 6px}</style>");
			output.println("</head>");
			output.println("<body>");

			output.println("<div style=\"font-family:Arial,Helvetica;font-size:16px;color:navy;margin-bottom:16px\">Contents of certificate file " + CommonUtils.extractFileName(filePath) + "</div>");
			
			FileInputStream fis = null;
			
			try {
				CertificateFactory fact = CertificateFactory.getInstance("X.509");
			    fis = new FileInputStream(filePath);
			    X509Certificate cert = (X509Certificate) fact.generateCertificate(fis);
			    
	        	output.println("<table style=\"border:1px solid #a0a0a0;font-family:Arial,Helvetica;font-size:16px;border-collapse:collapse\">");
			    
	        	output.println("<tr style=\"background-color:ivory\"><td style=\"white-space:nowrap\">certificate type:</td><td>" + cert.getType() + "</td></tr>");
			    
        		boolean valid = true;
	        	
	        	try {
	        	    cert.checkValidity();
	        	} catch (CertificateExpiredException expEx) {
	        		valid = false;
	        	} catch (CertificateNotYetValidException nyvEx) {
	        		valid = false;
	        	}

	        	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	        	
                String validFrom = dateFormat.format(cert.getNotBefore());
                String validUntil = dateFormat.format(cert.getNotAfter());
	        	
	        	output.println("<tr style=\"background-color:ivory\"><td>valid:</td><td>" + Boolean.toString(valid) + " (from " + validFrom + " until " + validUntil + ")</td></tr>");
        		
	        	X500Principal issuer = cert.getIssuerX500Principal();
	        	
	        	if (issuer != null) {
		        	output.println("<tr style=\"background-color:ivory\"><td style=\"vertical-align:top\">issuer:</td><td>" + issuer.getName() + "</td></tr>");
	        	}
	        	
	        	X500Principal subject = cert.getSubjectX500Principal();

	        	if (subject != null) {
		        	output.println("<tr style=\"background-color:ivory\"><td style=\"vertical-align:top\">subject:</td><td>" + subject.getName() + "</td></tr>");
	        	}

	        	output.println("</table>");
	        	
		    } catch (CertificateException certEx) {
		    	LOG.warn("failed to load certificate " + filePath, certEx);
		    	output.println("failed to load certificate: " + certEx);
		    } catch (IOException ioEx) {
		    	LOG.warn("failed to load certificate " + filePath, ioEx);
		    	output.println("failed to load certificate: " + ioEx);
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
	    	LOG.warn("failed to get certificate content of file " + filePath, ex);
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
