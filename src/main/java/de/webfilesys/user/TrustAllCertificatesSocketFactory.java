package de.webfilesys.user;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.cert.X509Certificate;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


public class TrustAllCertificatesSocketFactory extends SSLSocketFactory  {

	private static Logger LOG = LogManager.getLogger(LdapAuthenticatedXmlUserManager.class);

	private SSLSocketFactory socketFactory;
	
	public TrustAllCertificatesSocketFactory() {
   		LOG.warn("do not use TrustAllCertificatesSocketFactory in production");
		
		TrustManager[] trustAllCerts = new TrustManager[] {
			new X509TrustManager() {
			    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			        return null;
			    }

			    public void checkClientTrusted(X509Certificate[] certs, String authType) {  
			    }

			    public void checkServerTrusted(X509Certificate[] certs, String authType) {  
			    }
			}
		};    
					      
		try {
		    SSLContext sslCtx = SSLContext.getInstance("TLS");
		    sslCtx.init(null, trustAllCerts, new java.security.SecureRandom());
			socketFactory = sslCtx.getSocketFactory();
		} catch(Exception ex) {
		  	LOG.error("failed to disable SSL certificate check",ex);
	    }
	}
	
    public static SocketFactory getDefault() {
        return new TrustAllCertificatesSocketFactory();
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return socketFactory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return socketFactory.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket socket, String string, int i, boolean bln) throws IOException {
        return socketFactory.createSocket(socket, string, i, bln);
    }

    @Override
    public Socket createSocket(String string, int i) throws IOException, UnknownHostException {
        return socketFactory.createSocket(string, i);
    }

    @Override
    public Socket createSocket(String string, int i, InetAddress ia, int i1) throws IOException, UnknownHostException {
        return socketFactory.createSocket(string, i, ia, i1);
    }

    @Override
    public Socket createSocket(InetAddress ia, int i) throws IOException {
        return socketFactory.createSocket(ia, i);
    }

    @Override
    public Socket createSocket(InetAddress ia, int i, InetAddress ia1, int i1) throws IOException {
        return socketFactory.createSocket(ia, i, ia1, i1);
    }
}
