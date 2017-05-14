function contextMenu(fileName) {
    menuDiv = document.getElementById('contextMenu');    
        
    shortFileName = fileName;
    
    if (fileName.length > 24) {
        shortFileName = fileName.substring(0,7) + "..." + fileName.substring(fileName.length - 14, fileName.length);
    }    

    fileNameExt = getFileNameExt(fileName);
    
    lastPathChar = path.charAt(path.length - 1);
    
    if ((lastPathChar == '/') || (lastPathChar == '\\')) {
        fullPath = path + fileName;
    } else {
        if (serverOS == 'ix') {
            fullPath = path + '/' + fileName;
        } else {
            fullPath = path + '\\' + fileName;
        }
    }

    scriptPreparedPath = insertDoubleBackslash(fullPath);

    scriptPreparedFile = insertDoubleBackslash(fileName);
        
    menuText = '<table style="width:100%">'
             + '<tr>'
             + '<th>'
             + shortFileName
             + '</th>'
             + '</tr>';

    if (fileExt == ".ZIP" || fileExt == ".JAR" || fileExt == ".WAR") {
        menuText = menuText 
                 + menuEntry("javascript:viewZip('" + scriptPreparedPath + "')", resourceBundle["menuViewZip"]);
    } else {
	    if (fileExt == ".URL") {
            menuText = menuText 
                     + menuEntry("javascript:openUrlFile('" + scriptPreparedPath + "')", resourceBundle["menuView"]);
	    } else {
            menuText = menuText 
                     + menuEntry("javascript:viewFile('" + scriptPreparedPath + "')", resourceBundle["menuView"]);
	    }
    }
        
    if (fileExt == ".MP3") {
        downloadLabel = resourceBundle["menuPlay"];
    } else {
        downloadLabel = resourceBundle["menuDownload"];
    }

    menuText = menuText 
             + menuEntry("javascript:downloadFile('" + scriptPreparedPath + "')", downloadLabel);

    if (readonly != 'true') {
        menuText = menuText 
                 + menuEntry("javascript:delFile('" + scriptPreparedFile + "')", resourceBundle["menuDelete"]);

        menuText = menuText 
                 + menuEntry("javascript:renameFile('" + scriptPreparedFile + "')", resourceBundle["menuRename"]);

        menuText = menuText 
                 + menuEntry("javascript:copyToClipboard('" + scriptPreparedFile + "')", resourceBundle["menuCopy"]);

        menuText = menuText 
                 + menuEntry("javascript:cutToClipboard('" + scriptPreparedFile + "')", resourceBundle["menuCut"]);

        menuText = menuText 
                 + menuEntry("javascript:editRemote('" + scriptPreparedFile + "')", resourceBundle["menuEdit"]);

	    if ((fileExt == ".ZIP") || (fileExt == ".JAR") || (fileExt == ".WAR")) {
            menuText = menuText 
                     + menuEntry("javascript:zipFile('" + scriptPreparedPath + "')", resourceBundle["menuUnzip"]);
        } else {
            menuText = menuText 
                     + menuEntry("javascript:zipFile('" + scriptPreparedPath + "')", resourceBundle["menuZip"]);
        }
        
        if (mailEnabled == 'true') {
            menuText = menuText 
                     + menuEntry("javascript:sendFile('" + scriptPreparedFile + "')", resourceBundle["menuSendFile"]);
        }
        
	    if (fileExt == ".MP3") {
            menuText = menuText 
                     + menuEntry("javascript:editMP3('" + scriptPreparedPath + "')", resourceBundle["menuEditMP3"]);
	    } else {
            menuText = menuText 
                     + menuEntry("javascript:editMetaInfo('" + scriptPreparedFile + "')", resourceBundle["menuEditDesc"]);
        }
    }
    
    menuText = menuText 
             + menuEntry("javascript:comments('" + scriptPreparedPath + "')", resourceBundle["menuComments"]);
        
    menuText = menuText + '</table>'; 

    menuDiv.innerHTML = menuText;

    positionMenuDiv(menuDiv);
}
