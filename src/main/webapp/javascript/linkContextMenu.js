function jsLinkMenu(linkName, realPath) {
    menuDiv = document.getElementById('contextMenu');    

    menuDiv.innerHTML = "";
    
    shortFileName = linkName;
    
    if (linkName.length > 24) {
        shortFileName = linkName.substring(0,7) + "..." + linkName.substring(linkName.length - 14, linkName.length);
    }    

    fileNameExt = getFileNameExt(linkName);

    scriptPreparedPath = insertDoubleBackslash(realPath);

    addContextMenuHead(menuDiv, shortFileName);

    if (fileExt == ".ZIP" || fileExt == ".JAR" || fileExt == ".WAR") {
    	addContextMenuEntry(menuDiv, "viewZip('" + scriptPreparedPath + "')", resourceBundle["label.viewzip"]);
    } else if (fileExt == ".URL") {
    	addContextMenuEntry(menuDiv, "openUrlFile('" + scriptPreparedPath + "')", resourceBundle["label.view"]);
    } else {
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
    	addContextMenuEntry(menuDiv, "delLink('" + linkName + "')", resourceBundle["label.deleteLink"]);
    	addContextMenuEntry(menuDiv, "renameLink('" + linkName + "')", resourceBundle["label.renameLink"]);

 	    if (parent.clientIsLocal == 'true') {
 	    	addContextMenuEntry(menuDiv, "editLocalLink('" + scriptPreparedPath + "')", resourceBundle["label.edit"]);
        } else {
 	    	addContextMenuEntry(menuDiv, "editRemoteLink('" + scriptPreparedPath + "')", resourceBundle["label.edit"]);
        }

        if (parent.serverOS == 'win') {
            if (parent.webspaceUser != 'true') {
		        if ((fileExt == ".EXE") || (fileExt == ".COM") || (fileExt == ".BAT") || (fileExt == ".CMD")) {
		 	    	addContextMenuEntry(menuDiv, "execNativeProgram('" + scriptPreparedPath + "')", resourceBundle["label.run"]);
                } else {
		 	    	addContextMenuEntry(menuDiv, "associatedProg('" + scriptPreparedPath + "')", resourceBundle["label.open"]);
                }
            }
        } else {
            if (parent.webspaceUser != 'true') {
	 	    	addContextMenuEntry(menuDiv, "accessRights('" + scriptPreparedPath + "')", resourceBundle["label.rights"]);
	 	    	addContextMenuEntry(menuDiv, "associatedProg('" + scriptPreparedPath + "')", resourceBundle["label.open"]);
            }
        }

        if (parent.serverOS == 'win') {
 	    	addContextMenuEntry(menuDiv, "switchReadWrite('" + scriptPreparedPath + "')", resourceBundle["label.switchReadOnly"]);
        }

        if (parent.mailEnabled == 'true') {
 	    	addContextMenuEntry(menuDiv, "emailLink('" + scriptPreparedPath + "')", resourceBundle["label.sendfile"]);
        }
        
	    if (fileExt == ".MP3") {
 	    	addContextMenuEntry(menuDiv, "editMP3('" + scriptPreparedPath + "')", resourceBundle["label.editmp3"]);
	    } else {
 	    	addContextMenuEntry(menuDiv, "description('" + scriptPreparedPath + "')", resourceBundle["label.editMetaInfo"]);
        }
    }

 	addContextMenuEntry(menuDiv, "comments('" + scriptPreparedPath + "')", resourceBundle["label.comments"]);

    if (parent.serverOS == 'win') {
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

 	addContextMenuEntry(menuDiv, "origDir('" + insertDoubleBackslash(realDir) + "')", resourceBundle["label.origDir"]);
        
    if (parent.readonly == 'true') {
        maxMenuHeight = 200;
    } else {
        if (parent.serverOS == 'win') {
            maxMenuHeight = 300;
        } else {
            if (parent.webspaceUser == 'true') {
                maxMenuHeight = 260;
            } else {
                maxMenuHeight = 330;
            }
        }
    }
    
    positionMenuDiv(menuDiv, maxMenuHeight);

    menuDiv.style.visibility = 'visible';
}

function editRemoteLink(path) {
    var editWinWidth = screen.width - 80;
    var editWinHeight = screen.height - 70;

    if (editWinWidth > 800) {
        editWinWidth = 800;
    }

    if (editWinHeight > 700) {
        editWinHeight = 700;
    }
    
    editWin = window.open("/webfilesys/servlet?command=editFile&filePath=" + encodeURIComponent(path) + "&screenHeight=" + editWinHeight,"editWin","status=no,toolbar=no,location=no,menu=no,width=" + editWinWidth + ",height=" + editWinHeight + ",resizable=yes,left=20,top=5,screenX=20,screenY=5");
    editWin.focus();
    editWin.opener = self;
}

function origDir(path) {
    parent.parent.frames[1].location.href="/webfilesys/servlet?command=exp&expandPath=" + encodeURIComponent(path) + "&fastPath=true";
}

function emailLink(filePath) {
    centeredDialog('/webfilesys/servlet?command=emailFilePrompt&filePath=' + encodeURIComponent(filePath), '/webfilesys/xsl/emailFile.xsl', 400, 250, function() {
        document.emailForm.receiver.focus();
        document.emailForm.receiver.select();
    });
}
