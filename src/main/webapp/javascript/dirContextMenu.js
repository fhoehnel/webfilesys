function extractDirNameFromPath(path)
{
    var pathLength = path.length;
    
    for (i = pathLength - 1; i >= 0; i--)
    {
        if ((path.charAt(i) == '/') || (path.charAt(i) == '\\')) 
        {
            if (i < pathLength - 1) 
            {
                return path.substring(i + 1);
            }
        }
    }
    return path;
}

function dirContextMenu(domId, root) {
    parentDiv = document.getElementById(domId);

    if (!parentDiv) {
        console.error("Element with id " + domId + " not found");
        return;
    }

	let dirIsRoot = false;
	if (root && root == "true") {
		dirIsRoot = true;
	}
    
    const urlEncodedPath = parentDiv.getAttribute("path");

    const path = decodeURIComponent(urlEncodedPath);

    let folderName = extractDirNameFromPath(path);

    if (folderName.length > 24) {
        folderName = folderName.substring(0,7) + "..." + folderName.substring(folderName.length - 14, folderName.length);
    }    

    let shortPathName = path;
    
    if (path.length > 24) {
        shortPathName = path.substring(0,7) + "..." + path.substring(path.length - 14, path.length);
    }    

    scriptPreparedPath = insertDoubleBackslash(path);

    if (parent.syncStarted) {
        syncSelectTarget(path, shortPathName, scriptPreparedPath);
        return;
    }

    if (parent.compStarted) {
        compSelectTarget(path, shortPathName, scriptPreparedPath);
        return;
    }

    const menuDiv = document.getElementById("contextMenu");    
    
    menuDiv.style.visibility = "hidden";

    menuDiv.innerHTML = "";

    addContextMenuHead(menuDiv, folderName);

    if (parent.readonly != 'true') {
    	addContextMenuEntry(menuDiv, "mkdir('" + scriptPreparedPath + "')", resourceBundle["label.mkdir"]);
    }

    if (((parent.serverOS == 'win') && (path.length > 3)) ||
	    ((parent.serverOS == 'ix') && (path.length > 1))) {

    	if ((parent.readonly != 'true') && (!dirIsRoot)) {
    		addContextMenuEntry(menuDiv, "copyDir('" + scriptPreparedPath + "', '" + domId + "')", resourceBundle["label.copydir"]);

    		addContextMenuEntry(menuDiv, "moveDirToClip('" + scriptPreparedPath + "', '" + domId + "')", resourceBundle["label.movedir"]);

    		addContextMenuEntry(menuDiv, "deleteDir('" + scriptPreparedPath + "', '" + domId + "')", resourceBundle["label.deldir"]);

    		addContextMenuEntry(menuDiv, "renameDir('" + scriptPreparedPath + "')", resourceBundle["label.renamedir"]);
	    }
    }

    if (!clipboardEmpty) {
        if (parent.readonly != 'true') {
        	addContextMenuEntry(menuDiv, "checkPasteOverwrite('" + scriptPreparedPath + "')", resourceBundle["label.pastedir"]);
	    }
    }

	addContextMenuEntry(menuDiv, "statisticsMenu('" + insertDoubleBackslash(shortPathName) + "', '" + scriptPreparedPath + "')", resourceBundle["label.statistics"] + " ...");

	addContextMenuEntry(menuDiv, "search('" + scriptPreparedPath + "')", resourceBundle["label.search"]);

    if (parent.readonly != 'true') {
    	addContextMenuEntry(menuDiv, "mkfile('" + scriptPreparedPath + "')", resourceBundle["label.createfile"]);

    	addContextMenuEntry(menuDiv, "upload('" + scriptPreparedPath + "')", resourceBundle["label.upload"]);
    }

    if ((parent.serverOS == 'ix')  && (parent.readonly != 'true') &&
        ((parent.webspaceUser != 'true') || (parent.chmodAllowed == 'true'))) {
    
    	addContextMenuEntry(menuDiv, "rights('" + scriptPreparedPath + "')", resourceBundle["label.accessrights"]);
    }

    if (((parent.serverOS == 'win') && (path.length > 3)) ||
	    ((parent.serverOS == 'ix') && (path.length > 1))) {
        if (parent.readonly != 'true') {
        	addContextMenuEntry(menuDiv, "zip('" + scriptPreparedPath + "')", resourceBundle["label.zipdir"]);
	    }
    }

    if (parent.readonly != 'true') {
        lastPathChar = path.charAt(path.length - 1);
    
        if ((lastPathChar == '/') || (lastPathChar == '\\')) {
  	        descriptionPath = path + ".";
	    } else {
	        if (parent.serverOS == 'win') {
	            descriptionPath = path + '\\' + '.';
	        } else {
	            descriptionPath = path + '/' + '.';
	        }
	    }

    	addContextMenuEntry(menuDiv, "description('" + insertDoubleBackslash(descriptionPath) + "')", resourceBundle["label.editMetaInfo"]);
    }

    if ((parent.clientIsLocal == 'true') && (parent.readonly != 'true') && (parent.serverOS == 'win')) {
    	addContextMenuEntry(menuDiv, "winCmdLine('" + scriptPreparedPath + "')", resourceBundle["label.winCmdLine"]);
    }

	addContextMenuEntry(menuDiv, "refresh('" + scriptPreparedPath + "')", resourceBundle["label.refresh"]);

    if ((parent.serverOS == 'win') && (path.length <= 3)) {
    	addContextMenuEntry(menuDiv, "driveInfo('" + scriptPreparedPath + "')", resourceBundle["label.driveinfo"]);
    }
        
    if (parent.readonly != 'true') {
    	addContextMenuEntry(menuDiv, "extendedDirMenu('" + insertDoubleBackslash(shortPathName) + "', '" + scriptPreparedPath + "', '" + domId + "', '" + dirIsRoot + "')", resourceBundle["label.menuMore"]);
    }

    let maxMenuHeight;
    
    if (parent.readonly == 'true') {
        maxMenuHeight = 260;
    } else {
        if (parent.serverOS == 'win') {
            maxMenuHeight = 420;
        } else {
            if (parent.webspaceUser == 'true') {
                maxMenuHeight = 420;
            } else {
                maxMenuHeight = 440;
            }
        }
    }
    
    positionMenuDivByDomId(menuDiv, maxMenuHeight, domId);

    menuDiv.style.visibility = 'visible';
}

function extendedDirMenu(shortPath, path, domId, dirIsRoot) {
    stopMenuClose = true;

    var scriptPreparedPath = insertDoubleBackslash(path);

    const menuDiv = document.getElementById("contextMenu");    
    
    menuDiv.style.visibility = "hidden";

    menuDiv.innerHTML = "";

    var folderName = extractDirNameFromPath(path);

    var shortFolderName = folderName;

    if (folderName.length > 24) {
        shortFolderName = folderName.substring(0,7) + "..." + folderName.substring(folderName.length - 14, folderName.length);
    }    

    addContextMenuHead(menuDiv, shortFolderName);

    if (parent.readonly != 'true') {
        if (parent.mailEnabled == 'true') {
        	addContextMenuEntry(menuDiv, "publish('" + scriptPreparedPath + "', true)", resourceBundle["label.publish"]);
        } else {
        	addContextMenuEntry(menuDiv, "publish('" + scriptPreparedPath + "', false)", resourceBundle["label.publish"]);
        }
    }

    if ((parent.readonly != 'true') && (parent.autoCreateThumbs != 'true')) {
    	addContextMenuEntry(menuDiv, "createThumbs('" + scriptPreparedPath + "')", resourceBundle["label.createthumbs"]);
    }

    if ((parent.adminUser == 'true') && (parent.autoCreateThumbs != 'true')) {
    	addContextMenuEntry(menuDiv, "clearThumbs('" + scriptPreparedPath + "')", resourceBundle["label.clearthumbs"]);
    }
    
	if (dirIsRoot == 'false') {
		addContextMenuEntry(menuDiv, "compareFolders('" + scriptPreparedPath + "', '" + domId + "')", resourceBundle["label.compSource"]);
	}
    
    if (parent.readonly != 'true') {
    	if (dirIsRoot == 'false') {
    		addContextMenuEntry(menuDiv, "synchronize('" + scriptPreparedPath + "', '" + domId + "')", resourceBundle["label.menuSynchronize"]);
    	}
        if (parent.watchEnabled) {
        	addContextMenuEntry(menuDiv, "watchFolder('" + scriptPreparedPath + "')", resourceBundle["label.watchFolder"]);
        }
    }

    if (((parent.serverOS == 'win') && (path.length > 3)) ||
	    ((parent.serverOS == 'ix') && (path.length > 1))) {
    	
    	addContextMenuEntry(menuDiv, "downloadFolder('" + scriptPreparedPath + "')", resourceBundle["label.downloadFolder"]);
    }

    if (parent.readonly != 'true') {
    	if (dirIsRoot == 'false') {
            if (((parent.serverOS == 'win') && (path.length > 3)) ||
                ((parent.serverOS == 'ix') && (path.length > 1))) {
            	
            	addContextMenuEntry(menuDiv, "cloneFolder('" + scriptPreparedPath + "', '" + folderName + "')", resourceBundle["label.cloneFolder"]);
            }
    	}
    }

    if (parent.webspaceUser != 'true') {
    	addContextMenuEntry(menuDiv, "copyPathToClipboard('" + scriptPreparedPath + "')", resourceBundle["label.copyPath"]);
    }
    
    menuDiv.style.visibility = 'visible';
}

function syncSelectTarget(path, shortPath, scriptPreparedPath) {
    const menuDiv = document.getElementById("contextMenu");    
    
    menuDiv.style.visibility = "hidden";

    menuDiv.innerHTML = "";

    addContextMenuHead(menuDiv, shortPath);
    
    if (parent.readonly != 'true') {
    	addContextMenuEntry(menuDiv, "synchronize('" + scriptPreparedPath + "')", resourceBundle["label.menuSynchronize"]);

    	addContextMenuEntry(menuDiv, "cancelSynchronize('" + scriptPreparedPath + "')", resourceBundle["label.menuCancelSync"]);
    }

    positionMenuDiv(menuDiv, 120);

    menuDiv.style.visibility = 'visible';
}

function compSelectTarget(path, shortPath, scriptPreparedPath) {
    const menuDiv = document.getElementById("contextMenu");    
    
    menuDiv.style.visibility = "hidden";

    menuDiv.innerHTML = "";

    addContextMenuHead(menuDiv, shortPath);
    
    if (parent.readonly != 'true') {
    	addContextMenuEntry(menuDiv, "compareFolders('" + scriptPreparedPath + "')", resourceBundle["label.compTarget"]);

    	addContextMenuEntry(menuDiv, "cancelCompare('" + scriptPreparedPath + "')", resourceBundle["label.cancelComp"]);
    }
    
    positionMenuDiv(menuDiv, 120);

    menuDiv.style.visibility = 'visible';
}

function statisticsMenu(shortPath, path) {
    stopMenuClose = true;

    var scriptPreparedPath = insertDoubleBackslash(path);

    const menuDiv = document.getElementById("contextMenu");    
    
    menuDiv.style.visibility = "hidden";

    menuDiv.innerHTML = "";

    addContextMenuHead(menuDiv, shortPath);

	addContextMenuEntry(menuDiv, "statistics('" + scriptPreparedPath + "')", resourceBundle["label.subdirStats"]);

	addContextMenuEntry(menuDiv, "statSunburst('" + scriptPreparedPath + "')", resourceBundle["label.statSunburst"]);

	addContextMenuEntry(menuDiv, "fileSizeStatistics('" + scriptPreparedPath + "')", resourceBundle["label.sizeStats"]);

	addContextMenuEntry(menuDiv, "fileTypeStatistics('" + scriptPreparedPath + "')", resourceBundle["label.typeStats"]);

	addContextMenuEntry(menuDiv, "fileAgeStatistics('" + scriptPreparedPath + "')", resourceBundle["label.ageStats"]);

    menuDiv.style.visibility = 'visible';
}

function positionMenuDivByDomId(menuDiv, maxMenuHeight, domId) {
    var domNode = document.getElementById(domId);
    
    coordinates = getAbsolutePos(domNode);
    clickXPos = coordinates[0];
    clickYPos = coordinates[1];
    
    positionMenuDiv(menuDiv, maxMenuHeight);
}

