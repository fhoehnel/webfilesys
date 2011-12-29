package de.webfilesys.gui.xsl;

import java.io.BufferedWriter;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslCryptoKeyPromptHandler extends XslRequestHandlerBase
{
	public XslCryptoKeyPromptHandler(
			HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid)
	{
        super(req, resp, session, output, uid);
	}
	  
	protected void process()
	{
		if (!checkWriteAccess())
		{
			return;
		}

		String fileName = req.getParameter("fileName");
		
		if ((fileName == null) || (fileName.trim().length() == 0))
		{
			Logger.getLogger(getClass()).error("required parameter fileName missing");
			
			return;
		}
		
		Element cryptoPromptElement = doc.createElement("cryptoPrompt");
			
		doc.appendChild(cryptoPromptElement);
			
		XmlUtil.setChildText(cryptoPromptElement, "fileName", fileName, false);

		XmlUtil.setChildText(cryptoPromptElement, "shortFileName", CommonUtils.shortName(fileName, 24), false);
		
        addMsgResource("label.fileToEncrypt", getResource("label.fileToEncrypt","file to encrypt"));
        addMsgResource("label.fileToDecrypt", getResource("label.fileToDecrypt","file to decrypt"));
		addMsgResource("label.encrypt", getResource("label.encrypt","Encrypt (AES)"));
		addMsgResource("label.decrypt", getResource("label.decrypt","Decrypt (AES)"));
		addMsgResource("label.cryptoKey", getResource("label.cryptoKey","secret key"));
        addMsgResource("button.encrypt", getResource("button.encrypt","Encrypt"));
        addMsgResource("button.decrypt", getResource("button.decrypt","Decrypt"));
		addMsgResource("button.cancel", getResource("button.cancel","Cancel"));
		addMsgResource("alert.destEqualsSource", getResource("alert.destEqualsSource", "The new file name must be different!"));
		addMsgResource("alert.illegalCryptoKey", getResource("alert.illegalCryptoKey", "The format of the secret key is invalid!"));

		resp.setContentType("text/xml");
		
		BufferedWriter xmlOutFile = new BufferedWriter(output);
            
		XmlUtil.writeToStream(doc, xmlOutFile);

		output.flush();
	}
}