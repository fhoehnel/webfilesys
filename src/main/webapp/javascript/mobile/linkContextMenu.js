function jsLinkMenu(linkName, realPath) {
        
    let shortFileName = linkName;
    
    if (linkName.length > 22) {
        shortFileName = linkName.substring(0,7) + "..." + linkName.substring(linkName.length - 12, linkName.length);
    }    

    const fileNameExt = getFileNameExt(linkName);

    const scriptPreparedPath = insertDoubleBackslash(realPath);

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
	        if (fileExt != ".MP3") {
		    	addContextMenuEntry(menuDiv, "viewFile('" + scriptPreparedPath + "')", resourceBundle["menuView"]);
	        }
   	    }
    }

    const downloadLabel = (fileExt == ".MP3") ? resourceBundle["menuPlay"] : resourceBundle["menuDownload"];

	addContextMenuEntry(menuDiv, "downloadFile('" + scriptPreparedPath + "')", downloadLabel);

    if (readonly != 'true') {
    	addContextMenuEntry(menuDiv, "delLink('" + linkName + "')", resourceBundle["menuDelLink"]);

    	addContextMenuEntry(menuDiv, "renameLink('" + linkName + "')", resourceBundle["menuRenLink"]);

    	addContextMenuEntry(menuDiv, "editRemoteLink('" + scriptPreparedPath + "')", resourceBundle["menuEdit"]);

        if (parent.mailEnabled == 'true') {
        	addContextMenuEntry(menuDiv, "emailLink('" + scriptPreparedPath + "')", resourceBundle["menuSendFile"]);
        }
    }

	addContextMenuEntry(menuDiv, "comments('" + scriptPreparedPath + "')", resourceBundle["menuComments"]);

	let realDir = "";
    if (serverOS == 'win') {
        realDir = realPath.substring(0,realPath.lastIndexOf('\\'));
        if (realDir.length < 3) {
            realDir = realDir + "\\";
        }
    } else {
        realDir = realPath.substring(0,realPath.lastIndexOf('/'));
        if (realDir.length == 0) {
            realDir = "/";
        }
    }

	addContextMenuEntry(menuDiv, "origDir('" + insertDoubleBackslash(realDir) + "')", resourceBundle["menuOrigDir"]);

    positionMenuDiv(menuDiv);

    menuDiv.style.visibility = 'visible';
}

function editRemoteLink(path) {
    window.location.href = '/webfilesys/servlet?command=mobile&cmd=editFile&filePath=' + encodeURIComponent(path) + '&screenHeight=' + screen.height;
}

function origDir(path) {
    window.location.href = "/webfilesys/servlet?command=mobile&cmd=folderFileList&absPath=" + encodeURIComponent(path);
}

function emailLink(filePath) {
    centeredDialog('/webfilesys/servlet?command=emailFilePrompt&filePath=' + encodeURIComponent(filePath), '/webfilesys/xsl/emailFile.xsl', 400, 250, function() {
        document.emailForm.receiver.focus();
        document.emailForm.receiver.select();
    });
}

