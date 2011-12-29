package de.webfilesys.gui.user;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.MetaInfManager;
import de.webfilesys.gui.xsl.XslFileListHandler;

/**
 * AES decryption for a single file.
 * @author Frank Hoehnel
 */
public class DecryptFileRequestHandler extends UserRequestHandler
{
	protected HttpServletRequest req = null;

	protected HttpServletResponse resp = null;

	public DecryptFileRequestHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid)
	{
        super(req, resp, session, output, uid);
        
        this.req = req;
        
        this.resp = resp;
	}

	protected void process()
	{
		if (!checkWriteAccess())
		{
			return;
		}

		String fileName = getParameter("fileName");

		if ((fileName == null) || (fileName.trim().length() == 0))
		{
		    Logger.getLogger(getClass()).error("parameter fileName missing");
		    return;
		}
		
		String cryptoKey = getParameter("cryptoKey");
		
        if ((cryptoKey == null) || (cryptoKey.trim().length() == 0))
        {
            Logger.getLogger(getClass()).error("parameter cryptoKey missing");
            return;
        }
		
        String currentPath = getCwd();
        
        if (!decryptFile(currentPath, fileName, cryptoKey))
        {
            setParameter("errorMsg", getResource("error.invalidSecretKey","The decryption key is incorrect!"));
        }
        
		MetaInfManager.getInstance().removeMetaInf(currentPath, fileName);
		
		setParameter("actpath", getCwd());

		setParameter("mask","*");
		
        (new XslFileListHandler(req, resp, session, output, uid, false)).handleRequest();
	}
	
	private boolean decryptFile(String currentPath, String fileName, String cryptoKey)
	{
	    SecretKeySpec secretKey = new SecretKeySpec(getMD5(cryptoKey), "AES");
	    
        byte[] iv = new byte[]
                    {
                        0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f
                    };

        Cipher ecipher = null;
        
        AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
        try
        {
            ecipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            // CBC requires an initialization vector
            ecipher.init(Cipher.DECRYPT_MODE, secretKey, paramSpec);
        }
        catch (Exception e)
        {
            Logger.getLogger(getClass()).error("failed to create cipher for AES decryption", e);
        }
	    
        
	    File sourceFile = new File(currentPath, fileName);
	    
	    if ((!sourceFile.exists()) || (!sourceFile.isFile()) || (!sourceFile.canRead()))
	    {
	        Logger.getLogger(getClass()).error(fileName + " is not a readable file");
	        return false;
	    }
	    
        String targetFileName = null;

        int lastSepIdx = fileName.lastIndexOf('.');
        if (lastSepIdx > 0)
        {
            targetFileName = fileName.substring(0, lastSepIdx);   
        }
        else
        {
            // should never happen
            targetFileName = fileName + ".decrypt";
        }
        
        File targetFile = new File(currentPath, targetFileName);
        
        int i = 0;
        
        lastSepIdx = targetFileName.lastIndexOf('.');

        String targetFileNameWithoutExt = targetFileName;
        String targetFileExt = "";

        if (lastSepIdx > 0)
        {
            targetFileNameWithoutExt = targetFileName.substring(0, lastSepIdx);
            
            if (lastSepIdx < targetFileName.length() - 1)
            {
                targetFileExt = targetFileName.substring(lastSepIdx + 1);
            }
        }
        
        while (targetFile.exists())
        {
            if (lastSepIdx > 0)
            {
                targetFileName = targetFileNameWithoutExt + "_" + i + "." + targetFileExt;   
            }
            else
            {
                // should never happen
                targetFileName = fileName + "_" + i + ".decrypt";
            }
            
            targetFile = new File(currentPath, targetFileName);
            
            i++;
        }
        
        FileInputStream fileIn = null;
        
        FileOutputStream fileOut = null;

        try
	    {
	        fileIn = new FileInputStream(sourceFile);
	        
	        CipherInputStream encryptedIn = new CipherInputStream(fileIn, ecipher);

            int numRead = 0;
            
	        byte[] checksumBuff = new byte[6];
	        
	        numRead = encryptedIn.read(checksumBuff);
	        
	        if (numRead != EncryptFileRequestHandler.CHECKSUM.length)
	        {
                Logger.getLogger(getClass()).debug("invalid key used to decrypt file " + fileName);
	            return false;   
	        }
	        else
	        {
	            for (i = 0; i < EncryptFileRequestHandler.CHECKSUM.length; i++)
	            {
	                if (checksumBuff[i] != EncryptFileRequestHandler.CHECKSUM[i])
	                {
	                    Logger.getLogger(getClass()).debug("invalid key used to decrypt file " + fileName);
	                    return false;
	                }
	            }
	        }
	        
            fileOut = new FileOutputStream(targetFile);
            
            byte[] buff = new byte[1024];

            while ((numRead = encryptedIn.read(buff)) >= 0){
                fileOut.write(buff, 0, numRead);
            }
            
            encryptedIn.close();
	    }
	    catch (FileNotFoundException fnfEx)
	    {
	        Logger.getLogger(getClass()).error(fnfEx);
	    }
	    catch (IOException ioex)
	    {
	        Logger.getLogger(getClass()).error(ioex);
	    }
	    finally
	    {
            if (fileIn != null)
            {
                try
                {
                    fileIn.close();
                }
                catch (IOException ioex)
                {
                }
            }
            if (fileOut != null)
            {
                try
                {
                    fileOut.close();
                }
                catch (IOException ioex)
                {
                }
            }
	    }
	    
	    return true;
	}
	
	private static byte[] getMD5(String input){
        try
        {
            byte[] bytesOfMessage = input.getBytes("UTF-8");
            MessageDigest md = MessageDigest.getInstance("MD5");
            return md.digest(bytesOfMessage);
        }  
        catch (Exception e)
        {
             return null;
        }
    }
	
}
