function hideMenu()
{
    document.getElementById('contextMenu').style.visibility = 'hidden';
}

function insertDoubleBackslash(source)
{
    return(source.replace(/\\/g,"\\\\"));
}


function menuEntry(href, label, target)
{
    targetText = "";

    if (target != null)
    {
        targetText = 'target="' + target + '"'; 
    }

    return('<tr>'
             + '<td class="jsmenu">'
             + '<a class="menuitem" href="' + href + '" ' + targetText + '>' + label + '</a>'
             + '</td>'
             + '</tr>');
}

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
    
    menuText = '<table border="0" width="180" cellpadding="0" cellspacing="0" height="100%">'
             + '<tr>'
             + '<th class="datahead" style="padding-left:5px;padding-right:5px;padding-top:4px;padding-bottom:4px;text-align:left;border-bottom-width:1px;border-bottom-style:solid;border-bottom-color:black;">'
             + folderName
             + '</th>'
             + '</tr>';

    if (parent.readonly != 'true')
    {
        menuText = menuText 
                 + menuEntry("javascript:mkdir('" + scriptPreparedPath + "')",parent.resourceCreateDir,null);
    }

    if (((parent.serverOS == 'win') && (path.length > 3)) ||
	((parent.serverOS == 'ix') && (path.length > 1)))
    {
        if (parent.readonly != 'true')
	{
            menuText = menuText 
                     + menuEntry("javascript:copyDir('" + scriptPreparedPath + "')",parent.resourceCopyDir,null);

            menuText = menuText 
                     + menuEntry("javascript:moveDirToClip('" + scriptPreparedPath + "')",parent.resourceMoveDir,null);

            menuText = menuText 
                     + menuEntry("javascript:deleteDir('" + scriptPreparedPath + "', '" + domId + "')",parent.resourceDelDir,null);

            menuText = menuText 
                     + menuEntry("javascript:renameDir('" + scriptPreparedPath + "')",parent.resourceRenameDir,null);
	}
    }

    if (!clipboardEmpty)
    {
        if (parent.readonly != 'true')
	{
            menuText = menuText 
                     + menuEntry("javascript:paste('" + scriptPreparedPath + "')",parent.resourcePasteDir,null);
	}
    }

    menuText = menuText 
             + menuEntry("javascript:statisticsMenu('" + insertDoubleBackslash(shortPathName) + "', '" + scriptPreparedPath + "')",parent.resourceStatistics + ' >',null);

    menuText = menuText 
             + menuEntry("javascript:search('" + scriptPreparedPath + "')",parent.resourceSearch,null);

    if (parent.readonly != 'true')
    {
        menuText = menuText 
                 + menuEntry("javascript:mkfile('" + scriptPreparedPath + "')",parent.resourceCreateFile,null);

        menuText = menuText 
                 + menuEntry("javascript:upload('" + scriptPreparedPath + "')",parent.resourceUpload,null);
    }

    if ((parent.serverOS == 'ix')  && (parent.readonly != 'true') &&
        ((parent.webspaceUser != 'true') || (parent.chmodAllowed == 'true')))
    {
        menuText = menuText 
                 + menuEntry("javascript:rights('" + scriptPreparedPath + "')",parent.resourceDirRights,null);
    }

    if (((parent.serverOS == 'win') && (path.length > 3)) ||
	((parent.serverOS == 'ix') && (path.length > 1)))
    {
        if (parent.readonly != 'true')
        {
            menuText = menuText 
                     + menuEntry("javascript:zip('" + scriptPreparedPath + "')",parent.resourceZipDir,null);
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
                 + menuEntry("javascript:description('" + insertDoubleBackslash(descriptionPath) + "')",parent.resourceEditDesc,null);
    }

    if ((parent.clientIsLocal == 'true') && (parent.readonly != 'true') && (parent.serverOS == 'win'))
    {
        menuText = menuText 
                 + menuEntry("javascript:winCmdLine('" + scriptPreparedPath + "')",parent.resourceCmdLine,null);
    }

    menuText = menuText 
             + menuEntry("javascript:refresh('" + scriptPreparedPath + "')",parent.resourceRefresh,null);

    if ((parent.serverOS == 'win') && (path.length <= 3))
    {
        menuText = menuText 
                 + menuEntry("javascript:driveInfo('" + scriptPreparedPath + "')",parent.resourceDriveInfo,null);
    }
        
    if (parent.readonly != 'true')
    {
        menuText = menuText 
                 + menuEntry("javascript:extendedDirMenu('" + insertDoubleBackslash(shortPathName) + "', '" + scriptPreparedPath + "')",parent.resourceMenuMore,null);
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
    
    positionMenuDiv(menuDiv, maxMenuHeight);

    menuDiv.style.visibility = 'visible';
  
    // setTimeout('hideMenu()',8000);
}

function extendedDirMenu(shortPath, path)
{
    stopMenuClose = true;

    var scriptPreparedPath = insertDoubleBackslash(path);

    var menuDiv = document.getElementById('contextMenu');    

    menuDiv.style.visibility = 'hidden';

    var menuText = '<table border="0" width="180" cellpadding="0" cellspacing="0" height="100%">'
                 + '<tr>'
                 + '<th class="datahead" style="padding-left:5px;padding-right:5px;padding-top:4px;padding-bottom:4px;text-align:left;border-bottom-width:1px;border-bottom-style:solid;border-bottom-color:black;">'
                 + shortPath
                 + '</th>'
                 + '</tr>';

    if (parent.readonly != 'true')
    {
        if (parent.mailEnabled == 'true')
        {
            menuText = menuText 
                     + menuEntry("javascript:publish('" + scriptPreparedPath + "', true)",parent.resourcePublish,null);
        }
        else
        {
            menuText = menuText 
                     + menuEntry("javascript:publish('" + scriptPreparedPath + "', false)",parent.resourcePublish,null);
        }
    }

    if ((parent.readonly != 'true') && (parent.autoCreateThumbs != 'true'))
    {
        menuText = menuText 
                 + menuEntry("javascript:createThumbs('" + scriptPreparedPath + "')",parent.resourceCreateThumbs,null);
    }

    if ((parent.adminUser == 'true') && (parent.autoCreateThumbs != 'true'))
    {
        menuText = menuText 
                 + menuEntry("javascript:clearThumbs('" + scriptPreparedPath + "')",parent.resourceClearThumbs,null);
    }
    
    menuText = menuText 
               + menuEntry("javascript:compareFolders('" + scriptPreparedPath + "')",parent.resourceCompSource,null);

    if (parent.readonly != 'true')
    {
        menuText = menuText 
                 + menuEntry("javascript:synchronize('" + scriptPreparedPath + "')",parent.resourceSynchronize,null);

        if (parent.resourceWatch)
        {
            menuText = menuText 
                     + menuEntry("javascript:watchFolder('" + scriptPreparedPath + "')",parent.resourceWatch,null);
        }
    }

    if (((parent.serverOS == 'win') && (path.length > 3)) ||
	((parent.serverOS == 'ix') && (path.length > 1)))
    {
        menuText = menuText 
                   + menuEntry("/webfilesys/servlet?command=downloadFolder&path=" + encodeURIComponent(path),parent.resourceDownloadFolder,null);
    }

    menuText = menuText + '</table>'; 

    menuDiv.innerHTML = menuText;
    
    menuDiv.style.visibility = 'visible';
}

function syncSelectTarget(path, shortPath, scriptPreparedPath)
{
    var menuDiv = document.getElementById('contextMenu');    

    menuDiv.style.visibility = 'hidden';

    var menuText = '<table border="0" width="180" cellpadding="0" cellspacing="0" height="100%">'
                 + '<tr>'
                 + '<th class="datahead" style="padding-left:5px;padding-right:5px;padding-top:4px;padding-bottom:4px;text-align:left;border-bottom-width:1px;border-bottom-style:solid;border-bottom-color:black;">'
                 + shortPath
                 + '</th>'
                 + '</tr>';
    
    if (parent.readonly != 'true')
    {
        menuText = menuText 
                 + menuEntry("javascript:synchronize('" + scriptPreparedPath + "')",parent.resourceSynchronize,null);

        menuText = menuText 
                 + menuEntry("javascript:cancelSynchronize('" + scriptPreparedPath + "')",parent.resourceCancelSync,null);
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

    var menuText = '<table border="0" width="180" cellpadding="0" cellspacing="0" height="100%">'
                 + '<tr>'
                 + '<th class="datahead" style="padding-left:5px;padding-right:5px;padding-top:4px;padding-bottom:4px;text-align:left;border-bottom-width:1px;border-bottom-style:solid;border-bottom-color:black;">'
                 + shortPath
                 + '</th>'
                 + '</tr>';
    
    if (parent.readonly != 'true')
    {
        menuText = menuText 
                 + menuEntry("javascript:compareFolders('" + scriptPreparedPath + "')",parent.resourceCompTarget,null);

        menuText = menuText 
                 + menuEntry("javascript:cancelCompare('" + scriptPreparedPath + "')",parent.resourceCancelComp,null);
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

    var menuText = '<table border="0" width="180" cellpadding="0" cellspacing="0" height="100%">'
                 + '<tr>'
                 + '<th class="datahead" style="padding-left:5px;padding-right:5px;padding-top:4px;padding-bottom:4px;text-align:left;border-bottom-width:1px;border-bottom-style:solid;border-bottom-color:black;">'
                 + shortPath
                 + '</th>'
                 + '</tr>';

    menuText = menuText 
             + menuEntry("javascript:statistics('" + scriptPreparedPath + "')",parent.resourceSubdirStats,null);

    menuText = menuText 
             + menuEntry("javascript:fileSizeStatistics('" + scriptPreparedPath + "')",parent.resourceSizeStats,null);

    menuText = menuText 
             + menuEntry("javascript:fileTypeStatistics('" + scriptPreparedPath + "')",parent.resourceTypeStats,null);

    menuText = menuText 
             + menuEntry("javascript:fileAgeStatistics('" + scriptPreparedPath + "')",parent.resourceAgeStats,null);

    menuText = menuText + '</table>'; 

    menuDiv.innerHTML = menuText;
    
    menuDiv.style.visibility = 'visible';
}

function positionMenuDiv(menuDiv, maxMenuHeight)
{
    if ((browserFirefox) || 
        (rightMouseButton && (browserChrome || browserSafari)))
    {
        windowWidth = window.innerWidth;
        windowHeight = window.innerHeight;
        yScrolled = window.pageYOffset;
        xScrolled = window.pageXOffset;
        
        if (clickYPos > yScrolled + windowHeight - maxMenuHeight)
        {
            clickYPos = yScrolled + windowHeight - maxMenuHeight;
        }

        if (clickXPos > xScrolled + windowWidth - 200)
        {
            clickXPos = xScrolled + windowWidth - 200;
        }
    }
    else
    {
        windowWidth = document.body.clientWidth;
        windowHeight = document.body.clientHeight;
        yScrolled = document.body.scrollTop;

        if (clickXPos > windowWidth - 200)
        {
            clickXPos = windowWidth - 200;
        }

        if (clickYPos > windowHeight - maxMenuHeight)
        {
            clickYPos = windowHeight - maxMenuHeight;
        }

        clickYPos = clickYPos + yScrolled;
    }
    
    menuDiv.style.left = clickXPos + 'px';
    menuDiv.style.top = clickYPos + 'px';
}