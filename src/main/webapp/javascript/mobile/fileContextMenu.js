function contextMenu(fileName) {
       
    let shortFileName = fileName;
    
    if (fileName.length > 22) {
        shortFileName = fileName.substring(0,7) + "..." + fileName.substring(fileName.length - 12, fileName.length);
    }    

    const fileNameExt = getFileNameExt(fileName);
    
    const lastPathChar = path.charAt(path.length - 1);
    
    let fullPath = "";
    
    if ((lastPathChar == '/') || (lastPathChar == '\\')) {
        fullPath = path + fileName;
    } else {
        if (serverOS == 'ix') {
            fullPath = path + '/' + fileName;
        } else {
            fullPath = path + '\\' + fileName;
        }
    }

    const scriptPreparedPath = insertDoubleBackslash(fullPath);

    const scriptPreparedFile = insertDoubleBackslash(fileName);

    const menuDiv = document.getElementById('contextMenu');    
    
    menuDiv.style.visibility = 'hidden';

    menuDiv.innerHTML = "";

    addContextMenuHead(menuDiv, shortFileName);

    if (fileExt == ".ZIP" || fileExt == ".JAR" || fileExt == ".WAR") {
    	addContextMenuEntry(menuDiv, "viewZip('" + scriptPreparedPath + "')", resourceBundle["menuViewZip"]);
    } else {
	    if (fileExt == ".URL") {
	    	addContextMenuEntry(menuDiv, "openUrlFile('" + scriptPreparedPath + "')", resourceBundle["menuView"]);
	    } else {
	    	addContextMenuEntry(menuDiv, "viewFile('" + scriptPreparedPath + "')", resourceBundle["menuView"]);
	    }
    }
        
    const downloadLabel = (fileExt == ".MP3") ? resourceBundle["menuPlay"] : resourceBundle["menuDownload"];

	addContextMenuEntry(menuDiv, "downloadFile('" + scriptPreparedPath + "')", downloadLabel);

    if (readonly != 'true') {
    	addContextMenuEntry(menuDiv, "delFile('" + scriptPreparedFile + "')", resourceBundle["menuDelete"]);

    	addContextMenuEntry(menuDiv, "renameFile('" + scriptPreparedFile + "')", resourceBundle["menuRename"]);

    	addContextMenuEntry(menuDiv, "copyToClipboard('" + scriptPreparedFile + "')", resourceBundle["menuCopy"]);

    	addContextMenuEntry(menuDiv, "cutToClipboard('" + scriptPreparedFile + "')", resourceBundle["menuCut"]);

    	addContextMenuEntry(menuDiv, "editRemote('" + scriptPreparedFile + "')", resourceBundle["menuEdit"]);

	    if ((fileExt == ".ZIP") || (fileExt == ".JAR") || (fileExt == ".WAR")) {
	    	addContextMenuEntry(menuDiv, "zipFile('" + scriptPreparedPath + "')", resourceBundle["menuUnzip"]);
        } else {
	    	addContextMenuEntry(menuDiv, "zipFile('" + scriptPreparedPath + "')", resourceBundle["menuZip"]);
        }
        
        if (mailEnabled == 'true') {
	    	addContextMenuEntry(menuDiv, "sendFile('" + scriptPreparedFile + "')", resourceBundle["menuSendFile"]);
        }
        
	    if (fileExt == ".MP3") {
	    	addContextMenuEntry(menuDiv, "editMP3('" + scriptPreparedPath + "')", resourceBundle["menuEditMP3"]);
	    } else {
	    	addContextMenuEntry(menuDiv, "editMetaInfo('" + scriptPreparedFile + "')", resourceBundle["menuEditDesc"]);
        }
    }
    
	addContextMenuEntry(menuDiv, "comments('" + scriptPreparedPath + "')", resourceBundle["menuComments"]);
        
    positionMenuDiv(menuDiv);
    
    menuDiv.style.visibility = 'visible';
}
