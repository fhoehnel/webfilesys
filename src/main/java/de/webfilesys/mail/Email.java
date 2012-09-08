package de.webfilesys.mail;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Date;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.webfilesys.WebFileSys;
import de.webfilesys.util.Base64EncoderCRLF;
import de.webfilesys.util.FileEncodingMap;
import de.webfilesys.util.MimeTypeMap;

public class Email extends Thread
{
    public static String mailLogName="mail.log";

    private static final String BOUNDARY = "--0101011";
    
    private Vector receiverList=null;
    private String subject=null;
    private String messageText=null;
    private String contentType=null;
    private File attachment;

    private String mailSenderName=null;
    private String mailSenderAddress=null;
    
    public Email(String receiver,String subject,String messageText)
    {
        receiverList=new Vector();
        receiverList.add(receiver);
        this.subject=subject;
        this.messageText=messageText;
        this.contentType="text/plain; charset=ISO-8859-1";
    }

    public Email(Vector receiverList,String subject,String messageText)
    {
        this.receiverList=receiverList;
        this.subject=subject;
        this.messageText=messageText;
        this.contentType="text/plain; charset=ISO-8859-1";
    }

    public Email(String receiver,String subject,File attachment)
    {
        receiverList=new Vector();
        receiverList.add(receiver);
        this.subject=subject;
        this.attachment=attachment;
        // this.contentType=MimeTypeMap.getInstance().getMimeType(attachment.getName()) + ";name=\"" + attachment.getName() + "\"";
        this.contentType = MimeTypeMap.getInstance().getMimeType(attachment.getName());
    }

    public Email(Vector receiverList,String subject,File attachment)
    {
        this.receiverList=receiverList;
        this.subject=subject;
        this.attachment=attachment;
        this.contentType=MimeTypeMap.getInstance().getMimeType(attachment.getName()) + ";name=\"" + attachment.getName() + "\"";
    }

    public void setMailSenderName(String from)
    {
        mailSenderName = from;
    }

    public void setMailSenderAddress(String from)
    {
        mailSenderAddress=from;
    }

    public void send()
    {
        this.start();
    }

    public boolean sendSynchron() {
    	return sendInternal();
    }
    
    private void debug(String message, boolean response)
    {
        if (WebFileSys.getInstance().isDebugMail())
        {
            if (response) 
            {
                Logger.getLogger(getClass()).debug("SMTP response: " + message);
            }
            else 
            {
                Logger.getLogger(getClass()).debug("SMTP sent: " + message);
            }
        }
    }

    public synchronized void run()
    {
    	sendInternal();
    }
    
    public boolean sendInternal()
    {
    	boolean success = false;
    	
        for (int i = 0; i < receiverList.size(); i++)
        {
             String receiver=(String) receiverList.elementAt(i);
             
             Logger.getLogger(getClass()).info("sending SMTP mail to : " + receiver);
             
             Date now=new Date();

             Socket s = null;

             try
             {
                 s=new Socket(WebFileSys.getInstance().getMailHost(),25);
                 s.setSoTimeout(60000);

                 PrintWriter mailOut=new PrintWriter(s.getOutputStream(),true);

                 DataInputStream in=new DataInputStream(s.getInputStream());

                 getSMTPResponse(in,"220");

                 String temp="HELO " + InetAddress.getLocalHost().getHostName();
                 mailOut.print(temp);
                 mailOut.print("\r\n");
                 mailOut.flush();
                 debug(temp, false);
                 getSMTPResponse(in,"250");

                 temp="MAIL FROM: <" + WebFileSys.getInstance().getMailSenderAddress() + "> BODY=7BIT";
                 mailOut.print(temp);
                 mailOut.print("\r\n");
                 mailOut.flush();
                 debug(temp, false);
                 getSMTPResponse(in,"250");

                 temp="RCPT TO: <" + receiver + ">";
                 mailOut.print(temp);
                 mailOut.print("\r\n");
                 mailOut.flush();
                 debug(temp, false);
                 getSMTPResponse(in,"250");

                 temp="DATA";
                 mailOut.print(temp);
                 mailOut.print("\r\n");
                 mailOut.flush();
                 debug(temp, false);
                 getSMTPResponse(in,"354");

                 mailOut.print("Date: " + now.toGMTString());
                 mailOut.print("\r\n");
                 mailOut.flush();

                 if (mailSenderName==null)
                 {
                     mailSenderName = WebFileSys.getInstance().getMailSenderName();
                 }

                 if (mailSenderAddress==null)
                 {
                     mailSenderAddress = WebFileSys.getInstance().getMailSenderAddress();
                 }

                 mailOut.print("From: \"" + encodeWordBase64(mailSenderName) + "\" <" + mailSenderAddress + ">"); 
                 mailOut.print("\r\n");
                 mailOut.flush();

                 mailOut.print("To: <" + receiver + ">"); 
                 mailOut.print("\r\n");
                 mailOut.flush();

                 mailOut.print("Subject: "  + encodeWordBase64(subject));
                 mailOut.print("\r\n");
                 mailOut.flush();

                 mailOut.print("MIME-Version: 1.0");
                 mailOut.print("\r\n");
                 mailOut.print("Content-Type: multipart/alternative; boundary=\"" + BOUNDARY + "\"");
                 mailOut.print("\r\n");

                 mailOut.print("\r\n");
                 mailOut.flush();

                 if (attachment == null)
                 {
                     mailOut.print("--" + BOUNDARY);
                     mailOut.print("\r\n");
                     mailOut.print("Content-Type: text/plain; charset=utf-8");
                     mailOut.print("\r\n");
                     mailOut.print("Content-Transfer-Encoding: base64");
                     mailOut.print("\r\n");
                     mailOut.print("\r\n");

                     mailOut.print(encodeMessageBody());
                     mailOut.print("\r\n");
                     mailOut.print("\r\n");
                     mailOut.print("--" + BOUNDARY + "--");
                     mailOut.print("\r\n");
                 }
                 else
                 {
                     String textFileEncoding = FileEncodingMap.getInstance().getFileEncoding(attachment.getName());                     
                     
                     mailOut.print("--" + BOUNDARY);
                     mailOut.print("\r\n");
                     mailOut.print("Content-Type: " + contentType);
                     if (textFileEncoding != null)
                     {
                         mailOut.print("; charset=" + textFileEncoding);
                     }
                     mailOut.print("\r\n");
                     mailOut.print("Content-Transfer-Encoding: base64");
                     mailOut.print("\r\n");
                     mailOut.print("Content-Disposition: attachment; filename=\"" + attachment.getName() + "\"");
                     mailOut.print("\r\n");
                     mailOut.print("\r\n");
                     mailOut.flush();

                     printEncodedAttachment(s.getOutputStream());
                     
                     mailOut.print("\r\n");
                     mailOut.print("\r\n");
                     mailOut.print("--" + BOUNDARY + "--");
                     mailOut.print("\r\n");
                 }

                 mailOut.flush();

                 mailOut.print("\r\n");
                 mailOut.print(".");
                 mailOut.print("\r\n");

                 mailOut.flush();

                 getSMTPResponse(in,"250");

                 temp="QUIT";
                 mailOut.print(temp);
                 mailOut.print("\r\n");
                 mailOut.flush();
                 debug(temp, false);
                 getSMTPResponse(in,"221");

                 Logger.getLogger(getClass()).info("mail to " + receiver + " sent");
                 
                 success = true;
             }
             catch (Exception e)
             {
                 Logger.getLogger(getClass()).warn("mail to " + receiver + " error :" + e);
             }
             finally
             {
                 try
                 {
                     if (s!=null)
                     {
                         s.close();
                     }
                 }
                 catch(Exception e1)
                 {
                     Logger.getLogger(getClass()).error("cannot close SMTP connection : " + e1);
                 }
             }
        }
        
        return success;
    }
    
    public String replaceNewLine(String source)
    {
    	StringBuffer result = new StringBuffer();
    	
    	int length = source.length();
    	
    	for (int i=0;i<length;i++)
    	{
    		char c = source.charAt(i);
    		
    		if (c == '\n')
    		{
    			if ((i > 0 ) && (source.charAt(i-1) != '\r'))
    		    {
    		    	result.append("\r\n");	
    		    }
    		    else
    		    {
    		    	result.append(c);
    		    }
    		}
    		else
    		{
				result.append(c);
    		}
    	}

        return(result.toString());
    }

    public String encodeMessageBody() throws Exception
    {
        StringBuffer messageBody=new StringBuffer();

        messageBody.append(messageText);
        messageBody.append("\r\n\r\n");

        if (WebFileSys.getInstance().getClientUrl() !=null)
        {
            messageBody.append(WebFileSys.getInstance().getClientUrl());
            messageBody.append("\r\n\r\n");
        }
        else
        {
        	/*
			if (WebFileSys.getInstance().getHttpServerPort() > 0)
			{
				messageBody.append("http://");
				messageBody.append(InetAddress.getLocalHost().getHostAddress() + ":" + WebFileSys.getInstance().getHttpServerPort() + "/webfilesys/servlet \r\n");
			}

            if (WebFileSys.getInstance().getSslServerPort() > 0)
            {
                messageBody.append("https://");
				messageBody.append(InetAddress.getLocalHost().getHostAddress() + ":" + WebFileSys.getInstance().getSslServerPort() + "/webfilesys/servlet \r\n");
            }
            */
        }

        messageBody.append("---------------------\r\n");
        messageBody.append("powered by WebFileSys\r\n");

        byte[] buff = messageBody.toString().getBytes("UTF-8");
        
        sun.misc.BASE64Encoder encoder=new sun.misc.BASE64Encoder();
        
        return(new String(encoder.encodeBuffer(buff)));
    }

    public void printEncodedAttachment(OutputStream out)
    {
    	FileInputStream fin = null;
    	BufferedInputStream bufferedIn = null;

        try 
    	{
            Base64EncoderCRLF encoder = new Base64EncoderCRLF();

            BufferedOutputStream bufferedOut = new BufferedOutputStream(out);
            
            fin = new FileInputStream(attachment);
            bufferedIn = new BufferedInputStream(fin);

            encoder.encode(bufferedIn, bufferedOut);

            out.flush();
    	}
    	catch (IOException ioex)
    	{
        	Logger.getLogger(getClass()).error("failed to send attachment", ioex);
    	}
    	finally
    	{
    		if (bufferedIn != null) 
    		{
    			try 
    			{
    				bufferedIn.close();
    			}
    			catch (IOException ex)
    			{
    	        	Logger.getLogger(getClass()).error("failed to close attachment file", ex);
    			}
    		}
    		if (fin != null) 
    		{
    			try 
    			{
        			fin.close();
    			}
    			catch (IOException ex)
    			{
    			}
    		}
    	}
    }

    public synchronized void getSMTPResponse(DataInputStream in,String expectedResponse)
    throws IOException
    {
        String responseLine=null;

        responseLine = in.readLine();

        if (WebFileSys.getInstance().isDebugMail())
        {
            debug(responseLine, true);
        }

        if (responseLine==null)
        {
        	Logger.getLogger(getClass()).error("unexpected SMTP response : empty response (expected : " + expectedResponse + ")");
            
            responseLine = in.readLine();
            if (responseLine==null)
            {
                throw new IOException("unexpected SMTP response : empty response (expected : " + expectedResponse + ")");
            }
        }

        if (!responseLine.startsWith(expectedResponse))
        {
            throw new IOException("unexpected SMTP response : " + responseLine + " (expected : " + expectedResponse + ")");
        }

        while (responseLine.startsWith(expectedResponse + "-"))
        {
            responseLine = in.readLine();
        }
    }

    /**
     * MIME encoded-word syntax (RFC 2047).
     * "=?charset?encoding?encoded text?="
     * @param source source text
     * @return encoded word
     */
    public String encodeWordBase64(String source) 
    {
        StringBuffer dest = new StringBuffer();

        dest.append("=?utf-8?B?");
        
        try 
        {
            byte[] buff = source.getBytes("UTF-8");

            sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();
            
            String encodedBuff = new String(encoder.encodeBuffer(buff));
            
            // trim off trailing "=" and \n resulting from base64 encoding
            
            int base64SuffixStartidx = encodedBuff.length() - 1;
            
            boolean stop = false;
            while ((base64SuffixStartidx > 1) && (!stop)) 
            {
                char ch = encodedBuff.charAt(base64SuffixStartidx - 1);
                if ((ch != '\n') && (ch != '\r') && (ch != '=')) 
                {
                    stop = true;
                }
                else
                {
                    base64SuffixStartidx--;
                }
            }
            
            dest.append(encodedBuff.substring(0, base64SuffixStartidx));
            
            dest.append("?=");

            return dest.toString();
        }
        catch (UnsupportedEncodingException ex)
        {
            // should never happen
            return "";
        }
    }
}
