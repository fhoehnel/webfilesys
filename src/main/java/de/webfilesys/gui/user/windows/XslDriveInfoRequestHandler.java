package de.webfilesys.gui.user.windows;

import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.WinDiskUsage;
import de.webfilesys.WinDriveManager;
import de.webfilesys.gui.xsl.XslRequestHandlerBase;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslDriveInfoRequestHandler extends XslRequestHandlerBase
{
	public XslDriveInfoRequestHandler(
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
		String path = getParameter("path");

		if (!checkAccess(path))
		{
			return;
		}

		Element driveInfoElement = doc.createElement("driveInfo");
			
		doc.appendChild(driveInfoElement);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/driveInfo.xsl\"");

		doc.insertBefore(xslRef, driveInfoElement);
		
		XmlUtil.setChildText(driveInfoElement, "css", userMgr.getCSS(uid), false);

		WinDiskUsage diskUsage = new WinDiskUsage(path);
		
		DecimalFormat numFormat = new DecimalFormat("#,###,###,###,###");

		int driveNum=0;

		if ((path.charAt(0) >= 'a') && (path.charAt(0) <= 'z'))
		{
			driveNum= (int) (path.charAt(0) - 'a' + 1);
		}
		else
		{
			driveNum= (int) (path.charAt(0) - 'A' + 1);
		}

		XmlUtil.setChildText(driveInfoElement, "drivePath", path, false);

		String driveType = WinDriveManager.getInstance().getDriveType(driveNum);
		
		if (!CommonUtils.isEmpty(driveType)) {
			XmlUtil.setChildText(driveInfoElement, "driveType", driveType, false);
		}

		String driveLabel = WinDriveManager.getInstance().getDriveLabel(driveNum);

		XmlUtil.setChildText(driveInfoElement, "driveLabel", driveLabel, false);

		long totalDiskSpace = diskUsage.getTotalSpace();

		XmlUtil.setChildText(driveInfoElement, "totalDiskSpace", numFormat.format(totalDiskSpace), false);

		long freeDiskSpace = diskUsage.getFreeSpace();

		XmlUtil.setChildText(driveInfoElement, "freeDiskSpace", numFormat.format(freeDiskSpace), false);

		long percentUsed;

		if (totalDiskSpace == 0) 
		{
			percentUsed = 0;
		} 
		else 
		{
			percentUsed = 100 - (freeDiskSpace * 100 / totalDiskSpace);
		}
		
		XmlUtil.setChildText(driveInfoElement, "percentUsed", Long.toString(percentUsed), false);

		resp.setContentType("text/xml");
		
		BufferedWriter xmlOutFile = new BufferedWriter(output);
            
		XmlUtil.writeToStream(doc, xmlOutFile);

		output.flush();
	}
}