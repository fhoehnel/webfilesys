package de.webfilesys.gui.user.windows;

public class DriveInfo {

    private String drivePath;
    private String driveLabel;
    private String driveType;
    private String totalDiskSpace;
    private String freeDiskSpace;
    private long percentUsed;

    public String getDrivePath() {
        return drivePath;
    }

    public void setDrivePath(String drivePath) {
        this.drivePath = drivePath;
    }

    public String getDriveLabel() {
        return driveLabel;
    }

    public void setDriveLabel(String driveLabel) {
        this.driveLabel = driveLabel;
    }

    public String getDriveType() {
        return driveType;
    }

    public void setDriveType(String driveType) {
        this.driveType = driveType;
    }

    public String getTotalDiskSpace() {
        return totalDiskSpace;
    }

    public void setTotalDiskSpace(String totalDiskSpace) {
        this.totalDiskSpace = totalDiskSpace;
    }

    public String getFreeDiskSpace() {
        return freeDiskSpace;
    }

    public void setFreeDiskSpace(String freeDiskSpace) {
        this.freeDiskSpace = freeDiskSpace;
    }

    public long getPercentUsed() {
        return percentUsed;
    }

    public void setPercentUsed(long percentUsed) {
        this.percentUsed = percentUsed;
    }
}
