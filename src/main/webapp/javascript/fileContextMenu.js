function contextMenu(fileName) {
    menuDiv = document.getElementById('contextMenu');    
        
    menuDiv.style.visibility = 'hidden';

    menuDiv.innerHTML = "";
    
    shortFileName = fileName;
    
    if (fileName.length > 24) {
        shortFileName = fileName.substring(0,7) + "..." + fileName.substring(fileName.length - 14, fileName.length);
    }    

    fileNameExt = getFileNameExt(fileName);
    
    lastPathChar = path.charAt(path.length - 1);
    
    if ((lastPathChar == '/') || (lastPathChar == '\\')) {
        fullPath = path + fileName;
    } else {
        if (parent.serverOS == 'ix') {
            fullPath = path + '/' + fileName;
        } else {
            fullPath = path + '\\' + fileName;
        }
    }

    scriptPreparedPath = insertDoubleBackslash(fullPath);

    scriptPreparedFile = insertDoubleBackslash(fileName);
        
    if (parent.diffStarted) {
        diffSelectTarget(fileName, shortFileName, scriptPreparedPath);
        return;
    }
        
    addContextMenuHead(menuDiv, shortFileName);

    if (fileExt == ".ZIP" || fileExt == ".JAR" || fileExt == ".WAR" || fileExt == ".EAR") {
    	addContextMenuEntry(menuDiv, "viewZip('" + scriptPreparedPath + "')", resourceBundle["label.viewzip"]);
    } else if (fileExt == ".URL") {
    	addContextMenuEntry(menuDiv, "openUrlFile('" + scriptPreparedPath + "')", resourceBundle["label.view"]);
    } else if (fileExt == ".GPX") {
    	addContextMenuEntry(menuDiv, "viewTrackOnMap('" + scriptPreparedPath + "')", resourceBundle["viewTrackOnMap"]);
    } else {
         if ((fileExt == ".MP4") || (fileExt == ".OGG") || (fileExt == ".OGV")|| (fileExt == ".WEBM")) {
         	 addContextMenuEntry(menuDiv, "playVideo('" + scriptPreparedPath + "')", resourceBundle["label.playVideo"]);
         }
     	 addContextMenuEntry(menuDiv, "viewFile('" + scriptPreparedPath + "')", resourceBundle["label.view"]);
    }
        
    if (parent.clientIsLocal != 'true') {
	    if (fileExt == ".MP3") {
	        downloadLabel = resourceBundle["label.play"];
	    } else {
	        downloadLabel = resourceBundle["label.download"];
	    }
   	    addContextMenuEntry(menuDiv, "downloadFile('" + scriptPreparedPath + "')", downloadLabel);
    }

    if (parent.readonly != 'true') {
   	    addContextMenuEntry(menuDiv, "delFile('" + scriptPreparedFile + "')", resourceBundle["label.delete"]);

   	    addContextMenuEntry(menuDiv, "renameFile('" + scriptPreparedFile + "')", resourceBundle["label.renameFile"]);

   	    addContextMenuEntry(menuDiv, "copyToClipboard('" + scriptPreparedFile + "')", resourceBundle["label.copyToClip"]);

        if (addCopyAllowed) {
       	    addContextMenuEntry(menuDiv, "addCopyToClipboard('" + scriptPreparedFile + "')", resourceBundle["label.copyToClip"] + " +");
        }

   	    addContextMenuEntry(menuDiv, "cutToClipboard('" + scriptPreparedFile + "')", resourceBundle["label.cutToClip"]);

   	    if (addMoveAllowed) {
   	   	    addContextMenuEntry(menuDiv, "addMoveToClipboard('" + scriptPreparedFile + "')", resourceBundle["label.cutToClip"] + " +");
        }
    }

	if (parent.localEditor == 'true') {
	   	addContextMenuEntry(menuDiv, "editLocal('" + scriptPreparedFile + "')", resourceBundle["label.edit"]);
    } else {
	   	addContextMenuEntry(menuDiv, "editRemote('" + scriptPreparedFile + "')", resourceBundle["label.edit"]);
    }

	if ((fileExt == ".ZIP") || (fileExt == ".JAR") || (fileExt == ".WAR") || (fileExt == ".EAR")) {
	   	addContextMenuEntry(menuDiv, "zip('" + scriptPreparedPath + "')", resourceBundle["label.unzip"]);
    } else if ((fileExt == ".GZ")  || (fileExt == ".GZIP")) {
	   	addContextMenuEntry(menuDiv, "gunzip('" + scriptPreparedPath + "')", resourceBundle["label.unzip"]);
  	} else {
	   	addContextMenuEntry(menuDiv, "zip('" + scriptPreparedPath + "')", resourceBundle["label.zip"]);
    }

    if (fileExt == ".TAR") {
	   	addContextMenuEntry(menuDiv, "untar('" + scriptPreparedPath + "')", resourceBundle["label.untar"]);
    }

    if (parent.serverOS == 'ix') {
        if (parent.webspaceUser != 'true') {
		    if (fileExt == ".Z") {
			   	addContextMenuEntry(menuDiv, "compress('" + scriptPreparedPath + "')", resourceBundle["label.uncompress"]);
  		    } else {
			   	addContextMenuEntry(menuDiv, "compress('" + scriptPreparedPath + "')", resourceBundle["label.compress"]);
		    }
        }
    } else { // win
	   	addContextMenuEntry(menuDiv, "switchReadWrite('" + scriptPreparedPath + "')", resourceBundle["label.switchReadOnly"]);
    }
        
    if (parent.mailEnabled == 'true') {
	   	addContextMenuEntry(menuDiv, "sendFile('" + scriptPreparedFile + "')", resourceBundle["label.sendfile"]);
    }
        
	if (fileExt == ".MP3") {
	   	addContextMenuEntry(menuDiv, "editMP3('" + scriptPreparedPath + "')", resourceBundle["label.editmp3"]);
	   	if (parent.ffmpegEnabled) {
		   	addContextMenuEntry(menuDiv, "cutAudio('" + scriptPreparedPath + "')", resourceBundle["label.cutAudio"]);
	   	}
	} else {
	   	addContextMenuEntry(menuDiv, "description('" + scriptPreparedPath + "')", resourceBundle["label.editMetaInfo"]);
    }
    
   	addContextMenuEntry(menuDiv, "comments('" + scriptPreparedPath + "')", resourceBundle["label.comments"]);
        
    if (parent.readonly == 'true') {
       	addContextMenuEntry(menuDiv, "diffSelect('" + scriptPreparedPath + "')", resourceBundle["label.diffSource"]);
    } else {
       	addContextMenuEntry(menuDiv, "extendedFileMenu('" + insertDoubleBackslash(shortFileName) + "', '" + scriptPreparedPath + "')", resourceBundle["label.menuMore"]);
    }      
        
    if (parent.readonly == 'true') {
        maxMenuHeight = 200;
    } else {
        if (parent.serverOS == 'win') {
            maxMenuHeight = 340;
        } else {
            if (parent.webspaceUser == 'true') {
                maxMenuHeight = 300;
            } else {
                maxMenuHeight = 380;
            }
        }
    }
    
    if (browserFirefox) {
        maxMenuHeight = maxMenuHeight + 34;
    }
    
    positionMenuDiv(menuDiv, maxMenuHeight);

    menuDiv.style.visibility = 'visible';
}

function extendedFileMenu(shortFileName, path) {
    stopMenuClose = true;

    var scriptPreparedPath = insertDoubleBackslash(path);

    var menuDiv = document.getElementById('contextMenu');
    
    menuDiv.style.visibility = 'hidden';

    menuDiv.innerHTML = "";
    
    addContextMenuHead(menuDiv, shortFileName);

    if (parent.serverOS == 'win') {
        if (parent.adminUser == 'true') {
            if ((fileExt == ".EXE") || (fileExt == ".COM") || (fileExt == ".BAT") || (fileExt == ".CMD")) {
               	addContextMenuEntry(menuDiv, "execNativeProgram('" + scriptPreparedPath + "')", resourceBundle["label.run"]);
            } else {
               	addContextMenuEntry(menuDiv, "associatedProg('" + scriptPreparedPath + "')", resourceBundle["label.open"]);
            }
        }
    } else {
        if ((parent.webspaceUser != 'true') || (parent.chmodAllowed == 'true')) {
           	addContextMenuEntry(menuDiv, "accessRights('" + scriptPreparedPath + "')", resourceBundle["label.rights"]);
        }
        if (parent.adminUser == 'true') {
           	addContextMenuEntry(menuDiv, "associatedProg('" + scriptPreparedPath + "')", resourceBundle["label.open"]);
        }
    }

   	addContextMenuEntry(menuDiv, "diffSelect('" + scriptPreparedPath + "')", resourceBundle["label.diffSource"]);

    if (parent.readonly != 'true') {
       	addContextMenuEntry(menuDiv, "cloneFile('" + scriptPreparedFile + "')", resourceBundle["label.cloneFile"]);
    }

   	addContextMenuEntry(menuDiv, "hexView('" + scriptPreparedFile + "')", resourceBundle["label.hexView"]);

    if (fileExt == ".AES") {
       	addContextMenuEntry(menuDiv, "decrypt('" + scriptPreparedFile + "')", resourceBundle["label.decrypt"]);
    } else {
       	addContextMenuEntry(menuDiv, "encrypt('" + scriptPreparedFile + "')", resourceBundle["label.encrypt"]);
    }

   	addContextMenuEntry(menuDiv, "tail('" + scriptPreparedPath + "')", resourceBundle["label.tail"]);
    
    if (parent.readonly != 'true') {
       	addContextMenuEntry(menuDiv, "touch('" + scriptPreparedFile + "')", resourceBundle["label.touch"]);
    }
    
   	addContextMenuEntry(menuDiv, "grep('" + scriptPreparedPath + "', '" + scriptPreparedFile + "')", resourceBundle["label.grep"]);

   	var relativeFilePath;
    if (parent.serverOS == 'ix') {
    	relativeFilePath = relativePath + "/" + scriptPreparedFile;
    } else {
    	relativeFilePath = insertDoubleBackslash(relativePath + "\\" + scriptPreparedFile);
    }

   	addContextMenuEntry(menuDiv, "copyPathToClipboard('" + relativeFilePath + "')", resourceBundle["label.copyPath"]);
    
    menuDiv.style.visibility = 'visible';
}

function diffSelectTarget(path, shortFileName, scriptPreparedPath) {
    var menuDiv = document.getElementById('contextMenu');    

    menuDiv.style.visibility = 'hidden';
    
    menuDiv.innerHTML = "";
    
    addContextMenuHead(menuDiv, shortFileName);

   	addContextMenuEntry(menuDiv, "diffSelect('" + scriptPreparedPath + "')", resourceBundle["label.diffTarget"]);

   	addContextMenuEntry(menuDiv, "cancelDiff('" + scriptPreparedPath + "')", resourceBundle["label.cancelDiff"]);
    
    positionMenuDiv(menuDiv, 120);

    menuDiv.style.visibility = 'visible';
}
