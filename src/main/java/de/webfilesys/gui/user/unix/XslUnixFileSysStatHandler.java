package de.webfilesys.gui.user.unix;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.WebFileSys;
import de.webfilesys.gui.xsl.XslRequestHandlerBase;
import de.webfilesys.unix.FileSysInfo;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslUnixFileSysStatHandler extends XslRequestHandlerBase
{
	public XslUnixFileSysStatHandler(
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
		if (File.separatorChar != '/')
		{
			return;
		}

        if (isAdminUser(false) || (userMgr.getDocumentRoot(uid).equals("/") && (!isWebspaceUser())))
        {
            Element fileSysStatElement = doc.createElement("fileSysStat");
            
            doc.appendChild(fileSysStatElement);
                
            ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/unixFileSysStat.xsl\"");

            doc.insertBefore(xslRef, fileSysStatElement);

            XmlUtil.setChildText(fileSysStatElement, "css", userMgr.getCSS(uid), false);
            
            addMsgResource("label.filesyshead", getResource("label.filesyshead", "File System Statistics"));

            addMsgResource("label.filesyshead", getResource("label.filesyshead", "File System Statistics"));
            addMsgResource("label.filesys", getResource("label.filesys", "file system"));
            addMsgResource("label.usage", getResource("label.usage", "usage"));
            addMsgResource("label.capacity", getResource("label.capacity", "capacity (KByte)"));
            addMsgResource("label.freespace", getResource("label.freespace", "free (KByte)"));
            addMsgResource("label.percentUsed", getResource("label.percentUsed", "% used"));
            addMsgResource("button.closewin", getResource("button.closewin", "Close Window"));
            
            Element fileSystemsElement = doc.createElement("filesystems");
            fileSysStatElement.appendChild(fileSystemsElement);
            
            DecimalFormat numFormat = new DecimalFormat("#,###,###,###,###");

            ArrayList fileSysStatList = getFileSysStats();
            
            for (int i = 0; i < fileSysStatList.size(); i++)
            {
                FileSysInfo newFileSysInfo = (FileSysInfo) fileSysStatList.get(i);
                
                Element filesysElement = doc.createElement("filesys");
                
                fileSystemsElement.appendChild(filesysElement);
                
                XmlUtil.setChildText(filesysElement, "mountPoint", newFileSysInfo.mount_point);

                XmlUtil.setChildText(filesysElement, "usage", Integer.toString(newFileSysInfo.percent_used));

                XmlUtil.setChildText(filesysElement, "capacity", numFormat.format(newFileSysInfo.capacity));

                XmlUtil.setChildText(filesysElement, "free", numFormat.format(newFileSysInfo.free));
            }

            processResponse("unixFileSysStat.xsl", false);
        }
	}
	
	private ArrayList getDummyFileSysStats() 
	{
        ArrayList fileSysStatList = new ArrayList();
        
        FileSysInfo newFilesysInfo = new FileSysInfo("/usr", "/usr", 100000000L, 50000000L, 50, 50);
        fileSysStatList.add(newFilesysInfo);

        newFilesysInfo = new FileSysInfo("/tmp", "/tmp", 50000000L, 10000000L, 20, 20);
        fileSysStatList.add(newFilesysInfo);
        
        return fileSysStatList;
	}
	
	private ArrayList getFileSysStats() 
	{
	    ArrayList fileSysStatList = new ArrayList();
	    
        int osType = WebFileSys.getInstance().getOpSysType();
        
        String cmdLineWithParms[];
        if (osType == WebFileSys.OS_AIX)
        {
            cmdLineWithParms = new String[2];
            cmdLineWithParms[0] = "/bin/sh";
            cmdLineWithParms[1] = "df -k";
        }
        else
        {
            cmdLineWithParms = new String[3];
            cmdLineWithParms[0] = "/bin/sh";
            cmdLineWithParms[1] = "-c";
            cmdLineWithParms[2] = "df -k" + " 2>&1";
        }

        Runtime rt = Runtime.getRuntime();

        Process cmdProcess=null;

        try
        {
            cmdProcess=rt.exec(cmdLineWithParms);
        }
        catch (Exception e)
        {
            Logger.getLogger(getClass()).error(e);
        }

        DataInputStream cmdOut = new DataInputStream(cmdProcess.getInputStream());
        
        String stdoutLine = null;

        boolean done = false;
        boolean firstLine = true;

        while (!done)
        {
            try
            {
                stdoutLine = cmdOut.readLine();
            }
            catch (IOException ioe)
            {
                System.out.println(ioe);
            }
            if (stdoutLine==null)
            {
                done = true;
            }
            else
            {
                if (firstLine)
                {
                    firstLine = false;
                }
                else
                {
                    try
                    {
                        StringTokenizer fsParser = new StringTokenizer(stdoutLine);

                        String dev_name = fsParser.nextToken();

                        long capacity = Long.parseLong(fsParser.nextToken());

                        if ((osType == WebFileSys.OS_LINUX) || (osType == WebFileSys.OS_SOLARIS))
                        {
                            fsParser.nextToken();
                        }

                        long free = Long.parseLong(fsParser.nextToken());

                        String p_used = fsParser.nextToken();  

                        int percentUsed = new Integer(p_used.substring(0,p_used.length()-1)).intValue();
                        int percent_i_used = 0;
                        
                        if (osType == WebFileSys.OS_AIX)
                        {
                            fsParser.nextToken();
                            String p_i_used = fsParser.nextToken();
                            percent_i_used = new Integer(p_i_used.substring(0,p_i_used.length()-1)).intValue();
                        }

                        String mountPoint = fsParser.nextToken();

                        FileSysInfo newFilesysInfo = new FileSysInfo(mountPoint, dev_name, capacity,free,
                                                                     percentUsed, percent_i_used);

                        fileSysStatList.add(newFilesysInfo);
                    }
                    catch (NoSuchElementException nseEx)
                    {
                        Logger.getLogger(getClass()).error("error parsing filesys table : " + nseEx + "\n in line " + stdoutLine);
                    }
                    catch (NumberFormatException nfe)
                    {
                        Logger.getLogger(getClass()).error("error parsing filesys table : " + nfe + "\n in line " + stdoutLine);
                    }
                }
            }
        }

        return fileSysStatList;
	}
}
