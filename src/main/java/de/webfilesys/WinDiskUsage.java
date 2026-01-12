package de.webfilesys;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WinDiskUsage {
    private static final Logger LOG = LogManager.getLogger(WinDiskUsage.class);

    String path;
    File driveFile = null;

    public WinDiskUsage(String path) {
        this.path = path;
        driveFile = getDriveFile();
    }    

    public long getFreeSpace() {
        if (driveFile == null) {
            return 0L;
        }
        return driveFile.getFreeSpace();
    }

    public long getTotalSpace() {
        if (driveFile == null) {
            return 0L;
        }
        return driveFile.getTotalSpace();
    }

    private File getDriveFile() {
        boolean drivePathValid = path.length() >= 2 &&
                ((path.charAt(0) >= 'a' && path.charAt(0) <='z') ||
                 (path.charAt(0) >= 'A') && (path.charAt(0) <='Z'));
        if (!drivePathValid) {
            LOG.error("failed to determine disk free/total space - invalid path: {}", path);
            return null;
        }

        String driveString = path.substring(0,2) + "\\";
        File driveFile = new File(driveString);
        if (!driveFile.exists()) {
            LOG.error("failed to determine disk free/total space - file does not exist: {}", path);
            return null;
        }
        return driveFile;
    }
}

