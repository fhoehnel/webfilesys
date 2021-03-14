function folderContextMenu(path, folderName) {
	
    let shortFolderName = folderName;
    
    if (folderName.length > 22) {
        shortFolderName = folderName.substring(0,7) + "..." + folderName.substring(folderName.length - 12, folderName.length);
    }    

    const scriptPreparedPath = insertDoubleBackslash(path);

    const menuDiv = document.getElementById('contextMenu');    
    
    menuDiv.style.visibility = 'hidden';

    menuDiv.innerHTML = "";
    
    addContextMenuHead(menuDiv, shortFolderName);
    
	addContextMenuEntry(menuDiv, "showPictureThumbs()", resourceBundle["menuThumbnails"]);

    if (readonly != 'true') {
    	addContextMenuEntry(menuDiv, "mkdir('" + scriptPreparedPath + "')", resourceBundle["menuCreateDir"]);
    }

    if (((serverOS == 'win') && 
        (((path.charAt(0) == '\\') && (path.length > 4)) || ((path.charAt(0) != '\\') && (path.length > 3)))) ||
	    ((serverOS == 'ix') && (path.length > 1))) {

    	if (readonly != 'true') {
        	addContextMenuEntry(menuDiv, "copyDirToClip('" + scriptPreparedPath + "')", resourceBundle["menuCopyDir"]);

        	addContextMenuEntry(menuDiv, "moveDirToClip('" + scriptPreparedPath + "')", resourceBundle["menuMoveDir"]);

        	addContextMenuEntry(menuDiv, "deleteDir('" + scriptPreparedPath + "', '')", resourceBundle["menuDelDir"]);

        	addContextMenuEntry(menuDiv, "renameDir('" + scriptPreparedPath + "')", resourceBundle["menuRenameDir"]);
            
            if (clipboardEmpty != "true") {
            	addContextMenuEntry(menuDiv, "checkPasteOverwrite('" + scriptPreparedPath + "')", resourceBundle["button.paste"]);
            	
            	if (copyOperation == "true") {
                	addContextMenuEntry(menuDiv, "pasteAsLink()", resourceBundle["button.pasteLink"]);
            	}
            }

        	addContextMenuEntry(menuDiv, "uploadParams()", resourceBundle["button.upload"]);
        }
    }

	addContextMenuEntry(menuDiv, "search('" + scriptPreparedPath + "')", resourceBundle["menuSearch"]);

    if (readonly != 'true') {
    	addContextMenuEntry(menuDiv, "mkfile('" + scriptPreparedPath + "')", resourceBundle["menuCreateFile"]);
    }

    if (((serverOS == 'win') && 
        (((path.charAt(0) == '\\') && (path.length > 4)) || ((path.charAt(0) != '\\') && (path.length > 3)))) ||
	    ((serverOS == 'ix') && (path.length > 1))) {
    	
        if (readonly != 'true') {
        	addContextMenuEntry(menuDiv, "zip('" + scriptPreparedPath + "')", resourceBundle["menuZipDir"]);

            const lastPathChar = path.charAt(path.length - 1);
            let descriptionPath = "";
    
            if ((lastPathChar == '/') || (lastPathChar == '\\')) {
  	            descriptionPath = path + ".";
            } else {
	            if (serverOS == 'win') {
	                descriptionPath = path + '\\' + '.';
	            } else {
	                descriptionPath = path + '/' + '.';
	            }
	        }

        	addContextMenuEntry(menuDiv, "description('" + insertDoubleBackslash(descriptionPath) + "')", resourceBundle["menuEditDesc"]);
	    }
    }

    if (readonly != 'true') {
    	addContextMenuEntry(menuDiv, "bookmark('')", resourceBundle["mobile.addBookmark"]);
    }
    
    positionMenuDiv(menuDiv);

    menuDiv.style.visibility = 'visible';
}
