function hideMenu()
{
    document.getElementById('contextMenu').style.visibility = 'hidden';
}

function getFileNameExt(fileName)
{
    fileExt="";

    extStart=fileName.lastIndexOf('.');

    if (extStart > 0)
    {
	fileExt=fileName.substring(extStart).toUpperCase();
    }
    
    return(fileExt);
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

function contextMenu(fileName)
{
    menuDiv = document.getElementById('contextMenu');    
        
    shortFileName = fileName;
    
    if (fileName.length > 24)
    {
        shortFileName = fileName.substring(0,7) + "..." + fileName.substring(fileName.length - 14, fileName.length);
    }    

    fileNameExt = getFileNameExt(fileName);
    
    lastPathChar = path.charAt(path.length - 1);
    
    if ((lastPathChar == '/') || (lastPathChar == '\\'))
    {
        fullPath = path + fileName;
    }
    else
    {
        if (parent.serverOS == 'ix')
        {
            fullPath = path + '/' + fileName;
        }
        else
        {
            fullPath = path + '\\' + fileName;
        }
    }

    scriptPreparedPath = insertDoubleBackslash(fullPath);

    scriptPreparedFile = insertDoubleBackslash(fileName);
        
    if (parent.diffStarted)
    {
        diffSelectTarget(fileName, shortFileName, scriptPreparedPath);
        return;
    }
        
    menuText = '<table border="0" width="180" cellpadding="0" cellspacing="0" height="100%">'
             + '<tr>'
             + '<th class="datahead" style="padding-left:5px;padding-right:5px;padding-top:4px;padding-bottom:4px;text-align:left;border-bottom-width:1px;border-bottom-style:solid;border-bottom-color:black;">'
             + shortFileName
             + '</th>'
             + '</tr>';

    if (fileExt == ".ZIP" || fileExt == ".JAR" || fileExt == ".WAR" || fileExt == ".EAR")
    {
        menuText = menuText 
                 + menuEntry("javascript:viewZip('" + scriptPreparedPath + "')",parent.resourceViewZip,null);
    }
    else
    {
	if (fileExt == ".URL")
	{
            menuText = menuText 
                     + menuEntry("/webfilesys/servlet?command=openUrlFile&actPath=" + encodeURIComponent(fullPath) + "&random=" + (new Date().getTime()),parent.resourceView,"_blank");
	}
	else
	{
            menuText = menuText 
                     + menuEntry("javascript:viewFile('" + scriptPreparedPath + "')",parent.resourceView,null);
	}
    }
        
    if (parent.clientIsLocal != 'true')
    {
	if (fileExt == ".MP3")
	{
	    downloadLabel= parent.resourcePlay;
	}
	else
	{
	    downloadLabel = parent.resourceDownload;
	}

        menuText = menuText 
                 + menuEntry("/webfilesys/servlet?command=getFile&filePath=" + encodeURIComponent(fullPath) + "&disposition=download",downloadLabel,null);
    }

    if (parent.readonly != 'true')
    {
        menuText = menuText 
                 + menuEntry("javascript:delFile('" + scriptPreparedFile + "')",parent.resourceDelete,null);

        menuText = menuText 
                 + menuEntry("javascript:renameFile('" + scriptPreparedFile + "')",parent.resourceRenameFile,null);

        menuText = menuText 
                 + menuEntry("javascript:copyToClipboard('" + scriptPreparedFile + "')",parent.resourceCopy,null);

        menuText = menuText 
                 + menuEntry("javascript:cutToClipboard('" + scriptPreparedFile + "')",parent.resourceCut,null);

	if (parent.clientIsLocal == 'true')
	{
            menuText = menuText 
                     + menuEntry("javascript:editLocal('" + scriptPreparedFile + "')",parent.resourceEdit,null);
        }
        else
        {
            menuText = menuText 
                     + menuEntry("javascript:editRemote('" + scriptPreparedFile + "')",parent.resourceEdit,null);
        }

	if ((fileExt == ".ZIP") || (fileExt == ".JAR") || (fileExt == ".WAR") || (fileExt == ".EAR"))
	{
            menuText = menuText 
                     + menuEntry("javascript:zip('" + scriptPreparedPath + "')",parent.resourceUnzip,null);
        }
        else
        {
  	    if ((fileExt == ".GZ")  || (fileExt == ".GZIP"))
  	    {
                menuText = menuText 
                         + menuEntry("javascript:gunzip('" + scriptPreparedPath + "')",parent.resourceUnzip,null);
  	    }
  	    else
  	    {
                menuText = menuText 
                         + menuEntry("javascript:zip('" + scriptPreparedPath + "')",parent.resourceZip,null);
  	    }
        }

        if (fileExt == ".TAR")
        {
            menuText = menuText 
                     + menuEntry("javascript:untar('" + scriptPreparedPath + "')",parent.resourceUntar,null);
        }

        if (parent.serverOS == 'ix')
        {
            if (parent.webspaceUser != 'true')
            {
		if (fileExt == ".Z")
		{
                    menuText = menuText 
                             + menuEntry("javascript:compress('" + scriptPreparedPath + "')",parent.resourceUncompress,null);
		}
		else
		{
                    menuText = menuText 
                             + menuEntry("javascript:compress('" + scriptPreparedPath + "')",parent.resourceCompress,null);
		}
            }
        }
        else // win
        {
            menuText = menuText 
                     + menuEntry("javascript:switchReadWrite('" + scriptPreparedPath + "')",parent.resourceSwitchReadOnly,null);
        }
        
        if (parent.mailEnabled == 'true')
        {
            menuText = menuText 
                     + menuEntry("javascript:sendFile('" + scriptPreparedFile + "')",parent.resourceSendFile,null);
        }
        
	if (fileExt == ".MP3")
	{
            menuText = menuText 
                     + menuEntry("javascript:editMP3('" + scriptPreparedPath + "')",parent.resourceEditMP3,null);
	}
        else
        {
            menuText = menuText 
                     + menuEntry("javascript:description('" + scriptPreparedPath + "')",parent.resourceEditDesc,null);
        }
        
    }
    
    menuText = menuText 
             + menuEntry("javascript:comments('" + scriptPreparedPath + "')",parent.resourceComments,null);
        
    if (parent.readonly == 'true')
    {
        menuText = menuText 
                 + menuEntry("javascript:diffSelect('" + scriptPreparedPath + "')",parent.resourceDiffSource,null);
    }  
    else
    {
        menuText = menuText 
                 + menuEntry("javascript:extendedFileMenu('" + insertDoubleBackslash(shortFileName) + "', '" + scriptPreparedPath + "')",parent.resourceMenuMore,null);
    }      
        
    menuText = menuText + '</table>'; 

    menuDiv.innerHTML = menuText;

    menuDiv.style.bgcolor = '#c0c0c0';
    
    if (parent.readonly == 'true')
    {
        maxMenuHeight = 200;
    }
    else
    {
        if (parent.serverOS == 'win')
        {
            maxMenuHeight = 340;
        }
        else
        {
            if (parent.webspaceUser == 'true')
            {
                maxMenuHeight = 300;
            }
            else
            {
                maxMenuHeight = 380;
            }
        }
    }
    
    if (browserFirefox)
    {
        maxMenuHeight = maxMenuHeight + 34;
    }
    
    positionMenuDiv(menuDiv, maxMenuHeight);

    menuDiv.style.visibility = 'visible';
  
    // setTimeout('hideMenu()',8000);
}

function extendedFileMenu(shortFileName, path)
{
    stopMenuClose = true;

    var scriptPreparedPath = insertDoubleBackslash(path);

    var menuDiv = document.getElementById('contextMenu');    

    menuDiv.style.visibility = 'hidden';

    var menuText = '<table border="0" width="180" cellpadding="0" cellspacing="0" height="100%">'
             + '<tr>'
             + '<th class="datahead" style="padding-left:5px;padding-right:5px;padding-top:4px;padding-bottom:4px;text-align:left;border-bottom-width:1px;border-bottom-style:solid;border-bottom-color:black;">'
             + shortFileName
             + '</th>'
             + '</tr>';

    if (parent.serverOS == 'win')
    {
        if (parent.adminUser == 'true')
        {
            if ((fileExt == ".EXE") || (fileExt == ".COM") || (fileExt == ".BAT") || (fileExt == ".CMD"))
            {
                menuText = menuText 
                         + menuEntry("/webfilesys/servlet?command=execProgram&progname=" + encodeURIComponent(fullPath),parent.resourceRun,null);
            }
            else
            {
                menuText = menuText 
                         + menuEntry("javascript:associatedProg('" + scriptPreparedPath + "')",parent.resourceOpen,null);
            }
        }
    }
    else
    {
        if ((parent.webspaceUser != 'true') || (parent.chmodAllowed == 'true'))
        {
            menuText = menuText 
                     + menuEntry("javascript:accessRights('" + scriptPreparedPath + "')",parent.resourceRights,null);
        }

        if (parent.adminUser == 'true')
        {
            menuText = menuText 
                     + menuEntry("javascript:associatedProg('" + scriptPreparedPath + "')",parent.resourceOpen,null);
        }
    }

    menuText = menuText 
             + menuEntry("javascript:diffSelect('" + scriptPreparedPath + "')",parent.resourceDiffSource,null);

    if (parent.readonly != 'true')
    {
        menuText = menuText 
                 + menuEntry("javascript:cloneFile('" + scriptPreparedFile + "')", parent.resourceCloneFile,null);
    }

    menuText = menuText 
             + menuEntry("javascript:hexView('" + scriptPreparedFile + "')", parent.resourceHexView, null);

    if (fileExt == ".AES")
    {
        menuText = menuText 
                 + menuEntry("javascript:decrypt('" + scriptPreparedFile + "')", parent.resourceDecrypt, null);
    }
    else
    {
        menuText = menuText 
                 + menuEntry("javascript:encrypt('" + scriptPreparedFile + "')", parent.resourceEncrypt, null);
    }

    menuText = menuText 
             + menuEntry("javascript:tail('" + scriptPreparedPath + "')", parent.resourceTail, null);

    if (parent.readonly != 'true')
    {
        menuText = menuText 
                 + menuEntry("javascript:touch('" + scriptPreparedFile + "')", parent.resourceTouch,null);
    }

    menuText = menuText 
             + menuEntry("javascript:grep('" + scriptPreparedPath + "', '" + scriptPreparedFile + "')", parent.resourceGrep, null);

    menuText = menuText + '</table>'; 

    menuDiv.innerHTML = menuText;
    
    menuDiv.style.visibility = 'visible';
}

function diffSelectTarget(path, shortFileName, scriptPreparedPath)
{
    var menuDiv = document.getElementById('contextMenu');    

    menuDiv.style.visibility = 'hidden';

    var menuText = '<table border="0" width="180" cellpadding="0" cellspacing="0" height="100%">'
             + '<tr>'
             + '<th class="datahead" style="padding-left:5px;padding-right:5px;padding-top:4px;padding-bottom:4px;text-align:left;border-bottom-width:1px;border-bottom-style:solid;border-bottom-color:black;">'
             + shortFileName
             + '</th>'
             + '</tr>';
    
    menuText = menuText 
             + menuEntry("javascript:diffSelect('" + scriptPreparedPath + "')",parent.resourceDiffTarget,null);

    menuText = menuText 
             + menuEntry("javascript:cancelDiff('" + scriptPreparedPath + "')",parent.resourceCancelDiff,null);

    menuText = menuText + '</table>'; 

    menuDiv.innerHTML = menuText;
    
    positionMenuDiv(menuDiv, 120);

    menuDiv.style.visibility = 'visible';
}

function positionMenuDiv(menuDiv, maxMenuHeight)
{
    if (browserFirefox)
    {
        clickYPos = clickYPos - document.getElementById('fileListTable').scrollTop + 100;
        clickXPos = clickXPos - document.getElementById('fileListTable').scrollLeft;
    }

    if (!browserFirefox) 
    {
        windowWidth = document.body.clientWidth;
        windowHeight = document.body.clientHeight;

        if (clickXPos > windowWidth - 200)
        {
            clickXPos = windowWidth - 200;
        }

        if (clickYPos > windowHeight - maxMenuHeight)
        {
            clickYPos = windowHeight - maxMenuHeight;
        }
    }
    else
    {
        windowWidth = window.innerWidth;
        windowHeight = window.innerHeight;
        
        if (clickYPos > windowHeight - maxMenuHeight)
        {
            clickYPos = windowHeight - maxMenuHeight;
        }

        if (clickXPos > windowWidth - 200)
        {
            clickXPos = windowWidth - 200;
        }
    }

    menuDiv.style.left = clickXPos + 'px';
    menuDiv.style.top = clickYPos + 'px';
}