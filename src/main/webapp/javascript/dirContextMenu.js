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

function dirContextMenu(domId)
{
    parentDiv = document.getElementById(domId);

    if (!parentDiv)
    {
        alert('Element with id ' + domId + ' not found');

        return;
    }

    var urlEncodedPath = parentDiv.getAttribute("path");

    var path = decodeURIComponent(urlEncodedPath);

    var folderName = extractDirNameFromPath(path);

    if (folderName.length > 24)
    {
        folderName = folderName.substring(0,7) + "..." + folderName.substring(folderName.length - 14, folderName.length);
    }    

    var shortPathName = path;
    
    if (path.length > 24)
    {
        shortPathName = path.substring(0,7) + "..." + path.substring(path.length - 14, path.length);
    }    

    scriptPreparedPath = insertDoubleBackslash(path);

    if (parent.syncStarted)
    {
        syncSelectTarget(path, shortPathName, scriptPreparedPath);
        return;
    }

    if (parent.compStarted)
    {
        compSelectTarget(path, shortPathName, scriptPreparedPath);
        return;
    }

    var menuDiv = document.getElementById('contextMenu');    
    
    menuText = '<table class="contextMenu">'
             + '<tr>'
             + '<th>'
             + folderName
             + '</th>'
             + '</tr>';

    if (parent.readonly != 'true')
    {
        menuText = menuText 
                 + menuEntry("javascript:mkdir('" + scriptPreparedPath + "')",resourceBundle["label.mkdir"]);
    }

    if (((parent.serverOS == 'win') && (path.length > 3)) ||
	((parent.serverOS == 'ix') && (path.length > 1)))
    {
        if (parent.readonly != 'true')
	{
            menuText = menuText 
                     + menuEntry("javascript:copyDir('" + scriptPreparedPath + "', '" + domId + "')",resourceBundle["label.copydir"]);

            menuText = menuText 
                     + menuEntry("javascript:moveDirToClip('" + scriptPreparedPath + "', '" + domId + "')",resourceBundle["label.movedir"]);

            menuText = menuText 
                     + menuEntry("javascript:deleteDir('" + scriptPreparedPath + "', '" + domId + "')",resourceBundle["label.deldir"]);

            menuText = menuText 
                     + menuEntry("javascript:renameDir('" + scriptPreparedPath + "')",resourceBundle["label.renamedir"]);
	}
    }

    if (!clipboardEmpty)
    {
        if (parent.readonly != 'true')
	    {
            menuText = menuText 
                     + menuEntry("javascript:checkPasteOverwrite('" + scriptPreparedPath + "')",resourceBundle["label.pastedir"]);
	    }
    }

    menuText = menuText 
             + menuEntry("javascript:statisticsMenu('" + insertDoubleBackslash(shortPathName) + "', '" + scriptPreparedPath + "')",resourceBundle["label.statistics"] + ' >');

    menuText = menuText 
             + menuEntry("javascript:search('" + scriptPreparedPath + "')",resourceBundle["label.search"]);

    if (parent.readonly != 'true')
    {
        menuText = menuText 
                 + menuEntry("javascript:mkfile('" + scriptPreparedPath + "')",resourceBundle["label.createfile"]);

        menuText = menuText 
                 + menuEntry("javascript:upload('" + scriptPreparedPath + "')",resourceBundle["label.upload"]);
    }

    if ((parent.serverOS == 'ix')  && (parent.readonly != 'true') &&
        ((parent.webspaceUser != 'true') || (parent.chmodAllowed == 'true')))
    {
        menuText = menuText 
                 + menuEntry("javascript:rights('" + scriptPreparedPath + "')",resourceBundle["label.accessrights"]);
    }

    if (((parent.serverOS == 'win') && (path.length > 3)) ||
	((parent.serverOS == 'ix') && (path.length > 1)))
    {
        if (parent.readonly != 'true')
        {
            menuText = menuText 
                     + menuEntry("javascript:zip('" + scriptPreparedPath + "')",resourceBundle["label.zipdir"]);
	}
    }

    if (parent.readonly != 'true')
    {
        lastPathChar = path.charAt(path.length - 1);
    
        if ((lastPathChar == '/') || (lastPathChar == '\\'))
        {
  	    descriptionPath = path + ".";
	}
	else
	{
	    if (parent.serverOS == 'win')
	    {
	        descriptionPath = path + '\\' + '.';
	    }
	    else
	    {
	        descriptionPath = path + '/' + '.';
	    }
	}

        menuText = menuText 
                 + menuEntry("javascript:description('" + insertDoubleBackslash(descriptionPath) + "')",resourceBundle["label.editMetaInfo"]);
    }

    if ((parent.clientIsLocal == 'true') && (parent.readonly != 'true') && (parent.serverOS == 'win'))
    {
        menuText = menuText 
                 + menuEntry("javascript:winCmdLine('" + scriptPreparedPath + "')",resourceBundle["label.winCmdLine"]);
    }

    menuText = menuText 
             + menuEntry("javascript:refresh('" + scriptPreparedPath + "')",resourceBundle["label.refresh"]);

    if ((parent.serverOS == 'win') && (path.length <= 3))
    {
        menuText = menuText 
                 + menuEntry("javascript:driveInfo('" + scriptPreparedPath + "')",resourceBundle["label.driveinfo"]);
    }
        
    if (parent.readonly != 'true')
    {
        menuText = menuText 
                 + menuEntry("javascript:extendedDirMenu('" + insertDoubleBackslash(shortPathName) + "', '" + scriptPreparedPath + "', '" + domId + "')",resourceBundle["label.menuMore"]);
    }

    menuText = menuText + '</table>'; 

    menuDiv.innerHTML = menuText;
    
    menuDiv.style.bgcolor = '#c0c0c0';
    
    var maxMenuHeight;
    
    if (parent.readonly == 'true')
    {
        maxMenuHeight = 260;
    }
    else
    {
        if (parent.serverOS == 'win')
        {
            maxMenuHeight = 420;
        }
        else
        {
            if (parent.webspaceUser == 'true')
            {
                maxMenuHeight = 420;
            }
            else
            {
                maxMenuHeight = 440;
            }
        }
    }
    
    positionMenuDivByDomId(menuDiv, maxMenuHeight, domId);

    menuDiv.style.visibility = 'visible';
  
    // setTimeout('hideMenu()',8000);
}

function extendedDirMenu(shortPath, path, domId)
{
    stopMenuClose = true;

    var scriptPreparedPath = insertDoubleBackslash(path);

    var menuDiv = document.getElementById('contextMenu');    

    menuDiv.style.visibility = 'hidden';

    var folderName = extractDirNameFromPath(path);

    var shortFolderName = folderName;

    if (folderName.length > 24)
    {
        shortFolderName = folderName.substring(0,7) + "..." + folderName.substring(folderName.length - 14, folderName.length);
    }    

    var menuText = '<table class="contextMenu">'
                 + '<tr>'
                 + '<th>'
                 + shortFolderName
                 + '</th>'
                 + '</tr>';

    if (parent.readonly != 'true')
    {
        if (parent.mailEnabled == 'true')
        {
            menuText = menuText 
                     + menuEntry("javascript:publish('" + scriptPreparedPath + "', true)",resourceBundle["label.publish"]);
        }
        else
        {
            menuText = menuText 
                     + menuEntry("javascript:publish('" + scriptPreparedPath + "', false)",resourceBundle["label.publish"]);
        }
    }

    if ((parent.readonly != 'true') && (parent.autoCreateThumbs != 'true'))
    {
        menuText = menuText 
                 + menuEntry("javascript:createThumbs('" + scriptPreparedPath + "')",resourceBundle["label.createthumbs"]);
    }

    if ((parent.adminUser == 'true') && (parent.autoCreateThumbs != 'true'))
    {
        menuText = menuText 
                 + menuEntry("javascript:clearThumbs('" + scriptPreparedPath + "')",resourceBundle["label.clearthumbs"]);
    }
    
    menuText = menuText 
               + menuEntry("javascript:compareFolders('" + scriptPreparedPath + "', '" + domId + "')",resourceBundle["label.compSource"]);

    if (parent.readonly != 'true')
    {
        menuText = menuText 
                 + menuEntry("javascript:synchronize('" + scriptPreparedPath + "', '" + domId + "')",resourceBundle["label.menuSynchronize"]);

        if (parent.watchEnabled)
        {
            menuText = menuText 
                     + menuEntry("javascript:watchFolder('" + scriptPreparedPath + "')",resourceBundle["label.watchFolder"]);
        }
    }

    if (((parent.serverOS == 'win') && (path.length > 3)) ||
	((parent.serverOS == 'ix') && (path.length > 1)))
    {
        menuText = menuText 
                 + menuEntry("javascript:downloadFolder('" + scriptPreparedPath + "')",resourceBundle["label.downloadFolder"]);
    }

    if (parent.readonly != 'true')
    {
        menuText = menuText 
                 + menuEntry("javascript:cloneFolder('" + scriptPreparedPath + "', '" + folderName + "')",resourceBundle["label.cloneFolder"]);
    }

    menuText = menuText + '</table>'; 

    menuDiv.innerHTML = menuText;
    
    menuDiv.style.visibility = 'visible';
}

function syncSelectTarget(path, shortPath, scriptPreparedPath)
{
    var menuDiv = document.getElementById('contextMenu');    

    menuDiv.style.visibility = 'hidden';

    var menuText = '<table class="contextMenu">'
                 + '<tr>'
                 + '<th>'
                 + shortPath
                 + '</th>'
                 + '</tr>';
    
    if (parent.readonly != 'true')
    {
        menuText = menuText 
                 + menuEntry("javascript:synchronize('" + scriptPreparedPath + "')",resourceBundle["label.menuSynchronize"]);

        menuText = menuText 
                 + menuEntry("javascript:cancelSynchronize('" + scriptPreparedPath + "')",resourceBundle["label.menuCancelSync"]);
    }

    menuText = menuText + '</table>'; 

    menuDiv.innerHTML = menuText;
    
    positionMenuDiv(menuDiv, 120);

    menuDiv.style.visibility = 'visible';
}

function compSelectTarget(path, shortPath, scriptPreparedPath)
{
    var menuDiv = document.getElementById('contextMenu');    

    menuDiv.style.visibility = 'hidden';

    var menuText = '<table class="contextMenu">'
                 + '<tr>'
                 + '<th>'
                 + shortPath
                 + '</th>'
                 + '</tr>';
    
    if (parent.readonly != 'true')
    {
        menuText = menuText 
                 + menuEntry("javascript:compareFolders('" + scriptPreparedPath + "')",resourceBundle["label.compTarget"]);

        menuText = menuText 
                 + menuEntry("javascript:cancelCompare('" + scriptPreparedPath + "')",resourceBundle["label.cancelComp"]);
    }

    menuText = menuText + '</table>'; 

    menuDiv.innerHTML = menuText;
    
    positionMenuDiv(menuDiv, 120);

    menuDiv.style.visibility = 'visible';
}

function statisticsMenu(shortPath, path)
{
    stopMenuClose = true;

    var scriptPreparedPath = insertDoubleBackslash(path);

    var menuDiv = document.getElementById('contextMenu');    

    menuDiv.style.visibility = 'hidden';

    var menuText = '<table class="contextMenu">'
                 + '<tr>'
                 + '<th>'
                 + shortPath
                 + '</th>'
                 + '</tr>';

    menuText = menuText 
             + menuEntry("javascript:statistics('" + scriptPreparedPath + "')",resourceBundle["label.subdirStats"]);

    menuText = menuText 
             + menuEntry("javascript:statSunburst('" + scriptPreparedPath + "')",resourceBundle["label.statSunburst"]);

    menuText = menuText 
             + menuEntry("javascript:fileSizeStatistics('" + scriptPreparedPath + "')",resourceBundle["label.sizeStats"]);

    menuText = menuText 
             + menuEntry("javascript:fileTypeStatistics('" + scriptPreparedPath + "')",resourceBundle["label.typeStats"]);

    menuText = menuText 
             + menuEntry("javascript:fileAgeStatistics('" + scriptPreparedPath + "')",resourceBundle["label.ageStats"]);

    menuText = menuText + '</table>'; 

    menuDiv.innerHTML = menuText;
    
    menuDiv.style.visibility = 'visible';
}

function positionMenuDivByDomId(menuDiv, maxMenuHeight, domId) {
    var domNode = document.getElementById(domId);
    
    coordinates = getAbsolutePos(domNode);
    clickXPos = coordinates[0];
    clickYPos = coordinates[1];
    
    positionMenuDiv(menuDiv, maxMenuHeight);
}

