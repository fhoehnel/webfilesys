package de.webfilesys.util;

import sun.misc.*;

import java.io.*;

/**
 * Use always CR LF as line separator.
 * Required by SMTP RFC.
 * 
 * @author Frank Hoehnel
 *
 */
public class Base64EncoderCRLF extends BASE64Encoder 
{
    /**
     * Encode the suffix that ends every output line. By default
     * this method just prints a <newline> into the output stream.
     */
    protected void encodeLineSuffix(OutputStream out) throws IOException 
    {
	out.write('\r');
	out.write('\n');
    }
    
    public static void main(String args[])
    {
        try
        {
            FileInputStream in = new FileInputStream("D:/temp/base64/test.jpg");
        
            BASE64Encoder encoder = new BASE64Encoder();
        
            FileOutputStream out = new FileOutputStream("D:/temp/base64/base64.out");
         
            encoder.encode(in, out);
        
            in.close();
        
            out.close();


            in = new FileInputStream("D:/temp/base64/test.jpg");
        
            Base64EncoderCRLF encoder2 = new Base64EncoderCRLF();
        
            out = new FileOutputStream("D:/temp/base64/base64crlf.out");
         
            encoder2.encode(in, out);
        
            in.close();
        
            out.close();

        }
        catch (IOException ioex)
        {
            System.out.println(ioex);
        }
        
    }
}



