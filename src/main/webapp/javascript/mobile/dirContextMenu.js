function folderContextMenu(path, folderName) {
	
    var shortFolderName = folderName;
    
    if (folderName.length > 24) {
        shortFolderName = folderName.substring(0,7) + "..." + folderName.substring(folderName.length - 14, folderName.length);
    }    

    scriptPreparedPath = insertDoubleBackslash(path);

    var menuDiv = document.getElementById('contextMenu');    
    
    menuText = '<table style="width:100%">'
             + '<tr>'
             + '<th>'
             + shortFolderName
             + '</th>'
             + '</tr>';

    if (readonly != 'true') {
        menuText = menuText 
                 + menuEntry("javascript:mkdir('" + scriptPreparedPath + "')",resourceBundle["menuCreateDir"]);
    }

    if (((serverOS == 'win') && 
        (((path.charAt(0) == '\\') && (path.length > 4)) || ((path.charAt(0) != '\\') && (path.length > 3)))) ||
	    ((serverOS == 'ix') && (path.length > 1))) {

    	if (readonly != 'true') {
            menuText = menuText 
                     + menuEntry("javascript:copyDirToClip('" + scriptPreparedPath + "')",resourceBundle["menuCopyDir"]);

            menuText = menuText 
                     + menuEntry("javascript:moveDirToClip('" + scriptPreparedPath + "')",resourceBundle["menuMoveDir"]);

            menuText = menuText 
                     + menuEntry("javascript:deleteDir('" + scriptPreparedPath + "', '')",resourceBundle["menuDelDir"]);

            menuText = menuText 
                     + menuEntry("javascript:renameDir('" + scriptPreparedPath + "')",resourceBundle["menuRenameDir"]);
            
            if (clipboardEmpty != "true") {
                menuText = menuText 
                         + menuEntry("javascript:checkPasteOverwrite('" + scriptPreparedPath + "')",resourceBundle["button.paste"]);
            	
            	if (copyOperation == "true") {
                    menuText = menuText 
                             + menuEntry("javascript:pasteAsLink()", resourceBundle["button.pasteLink"]);
            	}
            }

            menuText = menuText 
                     + menuEntry("javascript:uploadParams()", resourceBundle["button.upload"]);
        }
    }

    menuText = menuText 
             + menuEntry("javascript:search('" + scriptPreparedPath + "')",resourceBundle["menuSearch"]);

    if (readonly != 'true') {
        menuText = menuText 
                 + menuEntry("javascript:mkfile('" + scriptPreparedPath + "')",resourceBundle["menuCreateFile"]);
    }

    if (((serverOS == 'win') && 
        (((path.charAt(0) == '\\') && (path.length > 4)) || ((path.charAt(0) != '\\') && (path.length > 3)))) ||
	    ((serverOS == 'ix') && (path.length > 1))) {
    	
        if (readonly != 'true') {
            menuText = menuText 
                     + menuEntry("javascript:zip('" + scriptPreparedPath + "')",resourceBundle["menuZipDir"]);

            lastPathChar = path.charAt(path.length - 1);
    
            if ((lastPathChar == '/') || (lastPathChar == '\\')) {
  	            descriptionPath = path + ".";
            } else {
	            if (serverOS == 'win') {
	                descriptionPath = path + '\\' + '.';
	            } else {
	                descriptionPath = path + '/' + '.';
	            }
	        }

            menuText = menuText 
                     + menuEntry("javascript:description('" + insertDoubleBackslash(descriptionPath) + "')",resourceBundle["menuEditDesc"]);
	    }
    }

    if (readonly != 'true') {
        menuText = menuText 
                 + menuEntry("javascript:bookmark('')", resourceBundle["mobile.addBookmark"]);
    }
    
    menuText = menuText + '</table>'; 

    menuDiv.innerHTML = menuText;
    
    positionMenuDiv(menuDiv);
}
