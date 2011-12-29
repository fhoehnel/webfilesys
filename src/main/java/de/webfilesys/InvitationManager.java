package de.webfilesys;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.webfilesys.util.XmlUtil;

/**
 *
 */
public class InvitationManager extends Thread
{
    public static final String INVITATION_FILE_NAME = "invitations.xml";

    public static final int EXPIRATION = 30;   // expires after 30 days

    public static final String INVITATION_TYPE_COMMON  = "common";
    public static final String INVITATION_TYPE_PICTURE = "picture";
    public static final String INVITATION_TYPE_TREE    = "tree";
	public static final String INVITATION_TYPE_FILE    = "file";

    private static InvitationManager invMgr=null;

    private boolean changed=false;
    
    Document doc;

    DocumentBuilder builder;

    Element invitationRoot=null;
    
    String invitationFilePath = null;

    private InvitationManager()
    {
    	invitationFilePath = WebFileSys.getInstance().getConfigBaseDir() + "/" + INVITATION_FILE_NAME;
    	
        builder = null;

        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
            
            invitationRoot=loadFromFile();

            if (invitationRoot==null)
            {
                doc=builder.newDocument();

                invitationRoot=doc.createElement("invitations");
            }
        }
        catch (ParserConfigurationException pcex)
        {
        	Logger.getLogger(getClass()).error(pcex.toString());
        }

        changed=false;

        this.start();
    }

    public static InvitationManager getInstance()
    {
        if (invMgr==null)
        {
            invMgr=new InvitationManager();
        }

        return(invMgr);
    }

    public void saveToFile()
    {
        if (invitationRoot == null)
        {
            return;
        }
            
        File invitationFile = new File(invitationFilePath);
        
        if (invitationFile.exists() && (!invitationFile.canWrite()))
        {
        	Logger.getLogger(getClass()).error("InvitationManager.saveToFile: cannot write to invitation file " + invitationFile.getAbsolutePath());
            return;
        }

        synchronized (invitationRoot)
        {
            OutputStreamWriter xmlOutFile = null;

            try
            {
                FileOutputStream fos = new FileOutputStream(invitationFile);
                
                xmlOutFile = new OutputStreamWriter(fos, "UTF-8");
                
                if (Logger.getLogger(getClass()).isDebugEnabled())
                {
                    Logger.getLogger(getClass()).debug("Saving invitations to file " + invitationFile.getAbsolutePath());
                }
                
                XmlUtil.writeToStream(invitationRoot, xmlOutFile);
                
                xmlOutFile.flush();

                changed = false;
            }
            catch (IOException io1)
            {
                Logger.getLogger(getClass()).error("error saving invitation registry file " + invitationFile.getAbsolutePath(), io1);
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
       File invitationFile = new File(invitationFilePath);

       if ((!invitationFile.exists()) || (!invitationFile.canRead()))
       {
           return(null);
       }

       Logger.getLogger(getClass()).info("reading invitation registry from " + invitationFile.getAbsolutePath());

       doc = null;
       
       FileInputStream fis = null;

       try
       {
           fis = new FileInputStream(invitationFile);
           
           InputSource inputSource = new InputSource(fis);
           
           inputSource.setEncoding("UTF-8");

           doc = builder.parse(inputSource);
       }
       catch (SAXException saxex)
       {
           Logger.getLogger(getClass()).error("failed to load invitation registry file : " + invitationFile.getAbsolutePath(), saxex);
       }
       catch (IOException ioex)
       {
           Logger.getLogger(getClass()).error("failed to load invitation registry file : " + invitationFile.getAbsolutePath(), ioex);
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

       return(doc.getDocumentElement());
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

    public String addInvitation(String userid,String path,int expirationDays,String type,
                                boolean allowComments)
    {
        return(addInvitation(userid,path,expirationDays,type,allowComments,null));
    }

    public String addInvitation(String userid,String path,int expirationDays,
                                String type,boolean allowComments,String virtualUser)
    {
        String accessCode=generateAccessCode();

        Document doc=invitationRoot.getOwnerDocument();

        Element invitationElement=doc.createElement("invitation");

        invitationElement.setAttribute("accessCode",accessCode);
        
        Element pathElement=doc.createElement("path");

        XmlUtil.setElementText(pathElement,path);

        invitationElement.appendChild(pathElement);

        long expiration=System.currentTimeMillis() + (((long) expirationDays) * 24l * 60l * 60l * 1000l);

        Element expiresElement=doc.createElement("expires");

        XmlUtil.setElementText(expiresElement,"" + expiration);

        invitationElement.appendChild(expiresElement);
        
        Element userElement=doc.createElement("user");

        XmlUtil.setElementText(userElement,userid);

        invitationElement.appendChild(userElement);

        Element typeElement=doc.createElement("type");

        XmlUtil.setElementText(typeElement,type);

        invitationElement.appendChild(typeElement);

        Element commentElement=doc.createElement("allowComments");

        XmlUtil.setElementText(commentElement,new Boolean(allowComments).toString());

        invitationElement.appendChild(commentElement);

        if (virtualUser!=null)
        {
            Element virtualElement=doc.createElement("virtualUser");

            XmlUtil.setElementText(virtualElement,virtualUser);

            invitationElement.appendChild(virtualElement);
        }

        invitationRoot.appendChild(invitationElement);

        changed=true;

        return(accessCode);
    }

    private Element getInvitationElement(String code)
    {
        NodeList invitationList=invitationRoot.getElementsByTagName("invitation");

        if (invitationList==null)
        {
            return(null);
        }

        int listLength=invitationList.getLength();

        for (int i=0;i<listLength;i++)
        {
            Element invitationElement=(Element) invitationList.item(i);

            String accessCode=invitationElement.getAttribute("accessCode");

            if ((accessCode!=null) && accessCode.equals(code))
            {
                return(invitationElement);
            }
        }

        return(null);
    }

    public boolean removeInvitation(String code)
    {
        Element invitationElement=getInvitationElement(code);

        if (invitationElement==null)
        {
            return(false);
        }

        invitationRoot.removeChild(invitationElement);

        changed=true;

        return(true);
    }

    public String getInvitationPath(String code)
    {
        Element invitationElement=getInvitationElement(code);

        if (invitationElement==null)
        {
            return(null);
        }

        String path=XmlUtil.getChildText(invitationElement,"path");

        if (path==null)
        {
            return(null);
        }

        String expirationString=XmlUtil.getChildText(invitationElement,"expires");

        if (expirationString==null)
        {
            return(null);
        }

        long expiration=0L;

        try
        {
            expiration=Long.parseLong(expirationString);
        }
        catch (NumberFormatException nfe)
        {
        }

        if (System.currentTimeMillis() > expiration)
        {
            return(null);
        }

        return(path);
    }

    public Date getExpirationTime(String code)
    {
        Element invitationElement=getInvitationElement(code);

        if (invitationElement==null)
        {
            return(null);
        }

        long expiration=0L;
        
        String expirationString=XmlUtil.getChildText(invitationElement,"expires");

        if (expirationString!=null)
        {
            try
            {
                expiration=Long.parseLong(expirationString);
            }
            catch (NumberFormatException nfe)
            {
            }
        }

        return(new Date(expiration));
    }

    public boolean commentsEnabled(String code)
    {
        Element invitationElement=getInvitationElement(code);

        if (invitationElement==null)
        {
            return(false);
        }

        String allowComments=XmlUtil.getChildText(invitationElement,"allowComments");

        return((allowComments!=null) && allowComments.equalsIgnoreCase("true"));
    }

    public String getInvitationOwner(String code)
    {
        NodeList invitationList=invitationRoot.getElementsByTagName("invitation");

        if (invitationList==null)
        {
            return(null);
        }

        int listLength=invitationList.getLength();

        for (int i=0;i<listLength;i++)
        {
            Element invitationElement=(Element) invitationList.item(i);

            String accessCode=invitationElement.getAttribute("accessCode");

            if ((accessCode!=null) && accessCode.equals(code))
            {
                String owner=XmlUtil.getChildText(invitationElement,"user");

                if ((owner==null) || (owner.length()==0))
                {
                    return(null);
                }

                return(owner);
            }
        }

        return(null);
    }

    public String getInvitationType(String code)
    {
        NodeList invitationList=invitationRoot.getElementsByTagName("invitation");

        if (invitationList==null)
        {
            return(null);
        }

        int listLength=invitationList.getLength();

        for (int i=0;i<listLength;i++)
        {
            Element invitationElement=(Element) invitationList.item(i);

            String accessCode=invitationElement.getAttribute("accessCode");

            if ((accessCode!=null) && accessCode.equals(code))
            {
                String type=XmlUtil.getChildText(invitationElement,"type");

                if ((type==null) || (type.length()==0))
                {
                    return(INVITATION_TYPE_COMMON);
                }

                return(type);
            }
        }

        return(INVITATION_TYPE_COMMON);
    }

    public String getVirtualUser(String code)
    {
        Element invitationElement=getInvitationElement(code);

        if (invitationElement==null)
        {
            return(null);
        }

        String virtualUser=XmlUtil.getChildText(invitationElement,"virtualUser");

        if ((virtualUser==null) || (virtualUser.length()==0))
        {
            return(null);
        }

        return(virtualUser);
    }

    public boolean commentsAllowed(String virtualUser)
    {
        NodeList invitationList=invitationRoot.getElementsByTagName("invitation");

        if (invitationList==null)
        {
            return(false);
        }

        int listLength=invitationList.getLength();

        for (int i=0;i<listLength;i++)
        {
            Element invitationElement=(Element) invitationList.item(i);

            String tmp=XmlUtil.getChildText(invitationElement,"virtualUser");

            if ((tmp!=null) && tmp.equals(virtualUser))
            {
                tmp=XmlUtil.getChildText(invitationElement,"allowComments");

                return((tmp!=null) && tmp.equalsIgnoreCase("true"));
            }
        }

        return(false);
    }

    /**
     * Get the list of access codes owned by the user.
     */
    public Vector getInvitationsByOwner(String userid)
    {
        NodeList invitationList=invitationRoot.getElementsByTagName("invitation");

        if (invitationList==null)
        {
            return(null);
        }

        Vector ownerList=new Vector();

        int listLength=invitationList.getLength();

        for (int i=0;i<listLength;i++)
        {
            Element invitationElement=(Element) invitationList.item(i);

            String owner=XmlUtil.getChildText(invitationElement,"user");

            if ((owner!=null) && owner.equals(userid))
            {
                String accessCode=invitationElement.getAttribute("accessCode");
                
                if (accessCode!=null)
                {
                    ownerList.add(accessCode);
                }
            }
        }

        return(ownerList);
    }

    public void removeExpired()
    {
        NodeList invitationList=invitationRoot.getElementsByTagName("invitation");

        if (invitationList==null)
        {
            return;
        }

        Vector expiredList=new Vector();

        int listLength=invitationList.getLength();

        for (int i=0;i<listLength;i++)
        {
            Element invitationElement=(Element) invitationList.item(i);

            String expirationString=XmlUtil.getChildText(invitationElement,"expires");

            if (expirationString!=null)
            {
                long expiration=0L;

                try
                {
                    expiration=Long.parseLong(expirationString);
                }
                catch (NumberFormatException nfe)
                {
                }
              
                if (System.currentTimeMillis() > expiration)
                {
                    expiredList.add(invitationElement);
                }
            }
        }
    
        int expiredNum=expiredList.size();

        for (int i=expiredList.size()-1;i >= 0;i--)
        {
            Element expiredElement=(Element) expiredList.elementAt(i);

            String type=XmlUtil.getChildText(expiredElement,"type");

            if ((type!=null) && type.equals(INVITATION_TYPE_TREE))
            {
                String virtualUser=XmlUtil.getChildText(expiredElement,"virtualUser");

                if ((virtualUser!=null) && (virtualUser.trim().length() > 0))
                {
                    WebFileSys.getInstance().getUserMgr().removeUser(virtualUser);
                    Logger.getLogger(getClass()).debug("expired virtual user " + virtualUser + " removed");
                }
            }

            invitationRoot.removeChild(expiredElement);
        }

        if (expiredNum > 0)
        {
            changed=true;
        }

        Logger.getLogger(getClass()).info(expiredNum + " expired invitations removed");
        Logger.getLogger(getClass()).info(expiredNum + " expired invitations removed");
    }

    public synchronized void run()
    {
        int counter=1;

        int sleepHours=1;

        while (true)
        {
            try
            {
                this.wait(60000);

                if (changed)
                {
                    saveToFile();

                    changed=false;
                }

                if (++counter == (sleepHours * 60))
                {
                    removeExpired();

                    counter=0;

                    sleepHours=24;
                }
            }
            catch (InterruptedException e)
            {
            	Logger.getLogger(getClass()).debug(e);
            }
        }
    }

    static public void main(String args[])
    {
    }

}

