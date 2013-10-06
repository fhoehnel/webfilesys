function folderContextMenu(path, folderName)
{
    var shortFolderName = folderName;
    
    if (folderName.length > 24)
    {
        shortFolderName = folderName.substring(0,7) + "..." + folderName.substring(folderName.length - 14, folderName.length);
    }    

    scriptPreparedPath = insertDoubleBackslash(path);

    var menuDiv = document.getElementById('contextMenu');    
    
    menuText = '<table border="0" width="180" cellpadding="0" cellspacing="0" height="100%">'
             + '<tr>'
             + '<th class="datahead" style="padding-left:5px;padding-right:5px;padding-top:4px;padding-bottom:4px;text-align:left;border-bottom-width:1px;border-bottom-style:solid;border-bottom-color:black;">'
             + shortFolderName
             + '</th>'
             + '</tr>';

    if (readonly != 'true')
    {
        menuText = menuText 
                 + menuEntry("javascript:mkdir('" + scriptPreparedPath + "')",resourceBundle["menuCreateDir"],null);
    }

    if (((serverOS == 'win') && 
        (((path.charAt(0) == '\\') && (path.length > 4)) || ((path.charAt(0) != '\\') && (path.length > 3)))) ||
	((serverOS == 'ix') && (path.length > 1)))
    {
        if (readonly != 'true')
        {
            menuText = menuText 
                     + menuEntry("javascript:copyDirToClip('" + scriptPreparedPath + "')",resourceBundle["menuCopyDir"],null);

            menuText = menuText 
                     + menuEntry("javascript:moveDirToClip('" + scriptPreparedPath + "')",resourceBundle["menuMoveDir"],null);

            menuText = menuText 
                     + menuEntry("javascript:deleteDir('" + scriptPreparedPath + "', '')",resourceBundle["menuDelDir"],null);

            menuText = menuText 
                     + menuEntry("javascript:renameDir('" + scriptPreparedPath + "')",resourceBundle["menuRenameDir"],null);
        }
    }

    menuText = menuText 
             + menuEntry("javascript:search('" + scriptPreparedPath + "')",resourceBundle["menuSearch"],null);

    if (readonly != 'true')
    {
        menuText = menuText 
                 + menuEntry("javascript:mkfile('" + scriptPreparedPath + "')",resourceBundle["menuCreateFile"],null);
    }

    if (((serverOS == 'win') && 
        (((path.charAt(0) == '\\') && (path.length > 4)) || ((path.charAt(0) != '\\') && (path.length > 3)))) ||
	((serverOS == 'ix') && (path.length > 1)))
    {
        if (readonly != 'true')
        {
            menuText = menuText 
                     + menuEntry("javascript:zip('" + scriptPreparedPath + "')",resourceBundle["menuZipDir"],null);

            lastPathChar = path.charAt(path.length - 1);
    
            if ((lastPathChar == '/') || (lastPathChar == '\\'))
            {
  	        descriptionPath = path + ".";
	    }
	    else
	    {
	        if (serverOS == 'win')
	        {
	            descriptionPath = path + '\\' + '.';
	        }
	        else
	        {
	            descriptionPath = path + '/' + '.';
	        }
	    }

            menuText = menuText 
                     + menuEntry("javascript:description('" + insertDoubleBackslash(descriptionPath) + "')",resourceBundle["menuEditDesc"],null);
	}
    }

    menuText = menuText + '</table>'; 

    menuDiv.innerHTML = menuText;
    
    menuDiv.style.bgcolor = '#c0c0c0';
    
    var maxMenuHeight = 270;
    
    positionMenuDiv(menuDiv, maxMenuHeight);

    menuDiv.style.visibility = 'visible';
}
