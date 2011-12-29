package de.webfilesys;

import java.util.Date;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.webfilesys.mail.Email;
import de.webfilesys.mail.MailTemplate;
import de.webfilesys.user.UserManager;

public class DiskQuotaInspector extends Thread
{
    boolean shutdownFlag=false;

    public DiskQuotaInspector()
    {
    }

    public synchronized void run()
    {
        setPriority(1);

        while (!shutdownFlag)
        {
            Date now = new Date();

            int hour = now.getHours();

            if (hour == WebFileSys.getInstance().getDiskQuotaCheckHour())
            {
                inspectDiskQuotas();
            }
            
            try
            {
                 this.sleep(60 * 60 * 1000);
            }
            catch (InterruptedException e)
            {
                shutdownFlag=true;
            }
        }
    }

    protected void inspectDiskQuotas()
    {
        long startTime=System.currentTimeMillis();

        Logger.getLogger(getClass()).info("disk quota inspection for webspace users started");

        UserManager userMgr = WebFileSys.getInstance().getUserMgr();

        StringBuffer adminMailBuffer=new StringBuffer();

        Vector allUsers=userMgr.getListOfUsers();

        for (int i=0;i<allUsers.size();i++)
        {
            String userid=(String) allUsers.elementAt(i);

            String role=userMgr.getRole(userid);

            if (role.equals("webspace"))
            {
                String homeDir=userMgr.getDocumentRoot(userid);

                if ((homeDir!=null) && (!homeDir.startsWith("*")))
                {
                    long diskQuota=userMgr.getDiskQuota(userid);

                    if (diskQuota > 0)
                    {
                        FileSysStat fileSysStat = new FileSysStat(homeDir);

                        Vector statList=fileSysStat.getStatistics();

                        if (fileSysStat.getTotalSizeSum() > diskQuota)
                        {
                            Logger.getLogger(getClass()).warn("disk quota exceeded for user " + userid + " (" + (diskQuota / 1024l) + " / " + (fileSysStat.getTotalSizeSum() / 1024l) + ")");

                            if (WebFileSys.getInstance().getMailHost() !=null)
                            {
                                StringBuffer mailContent=new StringBuffer("Disk quota exceeded for user ");
                                mailContent.append(userid);
                                mailContent.append("\r\n\r\n");
                                mailContent.append("disk quota   : ");
                                mailContent.append(diskQuota / 1024l);
                                mailContent.append(" KByte\r\n");
                                mailContent.append("current usage: ");
                                mailContent.append(fileSysStat.getTotalSizeSum() / 1024l);
                                mailContent.append(" KByte\r\n");

                                if (WebFileSys.getInstance().isMailNotifyQuotaUser())
                                {
                                    String email=userMgr.getEmail(userid);

                                    if ((email!=null) && (email.trim().length()>0))
                                    {
                                        String userLanguage=userMgr.getLanguage(userid);

                                        String mailText=mailContent.toString();

                                        try
                                        {
                                        	String templateFilePath = WebFileSys.getInstance().getConfigBaseDir() + "/languages/diskquota_" + userLanguage + ".template";
                                        	
                                            MailTemplate diskQuotaTemplate=new MailTemplate(templateFilePath);

                                            diskQuotaTemplate.setVarValue("LOGIN",userid);
                                            diskQuotaTemplate.setVarValue("QUOTA","" + (diskQuota / 1024l));
                                            diskQuotaTemplate.setVarValue("USAGE","" + (fileSysStat.getTotalSizeSum() / 1024l));

                                            mailText=diskQuotaTemplate.getText();
                                        }
                                        catch (IllegalArgumentException iaex)
                                        {
                                            System.out.println(iaex);
                                        }

                                        String subject = LanguageManager.getInstance().getResource(userLanguage,"subject.diskquota","Disk quota exceeded");

                                        (new Email(email,subject,mailText)).send();
                                    }
                                }

                                adminMailBuffer.append(mailContent.toString());
                                adminMailBuffer.append("\r\n\r\n");
                            }
                        }
                    }
                }
            }
        }
        
        long endTime=System.currentTimeMillis();
        
        if ((WebFileSys.getInstance().getMailHost() != null) && WebFileSys.getInstance().isMailNotifyQuotaAdmin())
        {
            if (adminMailBuffer.length()==0)
            {
                adminMailBuffer.append("no disk quota exceeded");
            }

            (new Email(userMgr.getAdminUserEmails(), "Disk quota report " + WebFileSys.getInstance().getLogDateFormat().format(new Date(endTime)),
                       adminMailBuffer.toString())).send();
        }

        Logger.getLogger(getClass()).info("disk quota inspection ended (" + ((endTime - startTime) / 1000) + " sec)");
    }

}
