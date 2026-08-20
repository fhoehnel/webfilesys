function driveInfo(path) {
    showPromptDialog("/webfilesys/html/driveInfo.html?path=" + encodeURIComponent(path), 260, function() {
            setBundleResources();
            fetchGet("driveInfo", { path: encodeURIComponent(path) }, responseData => {
                    const driveInfo = JSON.parse(responseData);
                    document.getElementById("drivePath").innerHTML = driveInfo.drivePath;
                    document.getElementById("driveType").innerHTML = driveInfo.driveType ?? "";
                    document.getElementById("driveLabel").innerHTML = driveInfo.driveLabel ?? "";
                    document.getElementById("totalDiskSpace").innerHTML = driveInfo.totalDiskSpace;
                    document.getElementById("freeDiskSpace").innerHTML = driveInfo.freeDiskSpace;
                    document.getElementById("percentUsed").innerHTML = driveInfo.percentUsed;
                    document.getElementById("imgUsed").style.width = (driveInfo.percentUsed * 2) + "px";
                },
                () => customAlert("failed to get drive info", null, () => self.close())
            );
        },
        420
    );
}

