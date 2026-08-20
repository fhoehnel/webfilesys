package de.webfilesys.gui.user.windows;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.webfilesys.WinDiskUsage;
import de.webfilesys.WinDriveManager;
import de.webfilesys.gui.user.UserRequestHandler;
import de.webfilesys.util.CommonUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.text.DecimalFormat;

public class DriveInfoRequestHandler extends UserRequestHandler {
	public DriveInfoRequestHandler(
			HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid) {
        super(req, resp, session, output, uid);
	}
	  
	protected void process() {
		String path = getParameter("path");

		if (!checkAccess(path)) {
			return;
		}

		DriveInfo driveInfo = new DriveInfo();

		WinDiskUsage diskUsage = new WinDiskUsage(path);

		int driveNum;

		if ((path.charAt(0) >= 'a') && (path.charAt(0) <= 'z')) {
			driveNum= path.charAt(0) - 'a' + 1;
		} else {
			driveNum= path.charAt(0) - 'A' + 1;
		}

        driveInfo.setDrivePath(path);

		String driveType = WinDriveManager.getInstance().getDriveType(driveNum);
		
		if (!CommonUtils.isEmpty(driveType)) {
			driveInfo.setDriveType(driveType);
		}

		String driveLabel = WinDriveManager.getInstance().getDriveLabel(driveNum);

		driveInfo.setDriveLabel(driveLabel);

		DecimalFormat numFormat = new DecimalFormat("#,###,###,###,###");

		long totalDiskSpace = diskUsage.getTotalSpace();
        driveInfo.setTotalDiskSpace(numFormat.format(totalDiskSpace));

		long freeDiskSpace = diskUsage.getFreeSpace();
        driveInfo.setFreeDiskSpace(numFormat.format(freeDiskSpace));

		long percentUsed = 0;
		if (totalDiskSpace > 0) {
			percentUsed = 100 - (freeDiskSpace * 100 / totalDiskSpace);
		}
        driveInfo.setPercentUsed(percentUsed);

		resp.setContentType("application/json; charset=UTF-8");

		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		output.println(gson.toJson(driveInfo));

		output.flush();
	}
}