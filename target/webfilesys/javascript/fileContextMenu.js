function hideMenu()
{
    document.getElementById('contextMenu').style.visibility = 'hidden';
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
                 + menuEntry("javascript:viewZip('" + scriptPreparedPath + "')",resourceBundle["label.viewzip"],null);
    }
    else
    {
	if (fileExt == ".URL")
	{
            menuText = menuText 
                     + menuEntry("/webfilesys/servlet?command=openUrlFile&actPath=" + encodeURIComponent(fullPath) + "&random=" + (new Date().getTime()),resourceBundle["label.view"],"_blank");
	}
	else
	{
            menuText = menuText 
                     + menuEntry("javascript:viewFile('" + scriptPreparedPath + "')",resourceBundle["label.view"],null);
	}
    }
        
    if (parent.clientIsLocal != 'true')
    {
	if (fileExt == ".MP3")
	{
	    downloadLabel = resourceBundle["label.play"];
	}
	else
	{
	    downloadLabel = resourceBundle["label.download"];
	}

        menuText = menuText 
                 + menuEntry("/webfilesys/servlet?command=getFile&filePath=" + encodeURIComponent(fullPath) + "&disposition=download",downloadLabel,null);
    }

    if (parent.readonly != 'true')
    {
        menuText = menuText 
                 + menuEntry("javascript:delFile('" + scriptPreparedFile + "')",resourceBundle["label.delete"],null);

        menuText = menuText 
                 + menuEntry("javascript:renameFile('" + scriptPreparedFile + "')",resourceBundle["label.renameFile"],null);

        menuText = menuText 
                 + menuEntry("javascript:copyToClipboard('" + scriptPreparedFile + "')",resourceBundle["label.copyToClip"],null);

        menuText = menuText 
                 + menuEntry("javascript:cutToClipboard('" + scriptPreparedFile + "')",resourceBundle["label.cutToClip"],null);

	if (parent.clientIsLocal == 'true')
	{
            menuText = menuText 
                     + menuEntry("javascript:editLocal('" + scriptPreparedFile + "')",resourceBundle["label.edit"],null);
        }
        else
        {
            menuText = menuText 
                     + menuEntry("javascript:editRemote('" + scriptPreparedFile + "')",resourceBundle["label.edit"],null);
        }

	if ((fileExt == ".ZIP") || (fileExt == ".JAR") || (fileExt == ".WAR") || (fileExt == ".EAR"))
	{
            menuText = menuText 
                     + menuEntry("javascript:zip('" + scriptPreparedPath + "')",resourceBundle["label.unzip"],null);
        }
        else
        {
  	    if ((fileExt == ".GZ")  || (fileExt == ".GZIP"))
  	    {
                menuText = menuText 
                         + menuEntry("javascript:gunzip('" + scriptPreparedPath + "')",resourceBundle["label.unzip"],null);
  	    }
  	    else
  	    {
                menuText = menuText 
                         + menuEntry("javascript:zip('" + scriptPreparedPath + "')",resourceBundle["label.zip"],null);
  	    }
        }

        if (fileExt == ".TAR")
        {
            menuText = menuText 
                     + menuEntry("javascript:untar('" + scriptPreparedPath + "')",resourceBundle["label.untar"],null);
        }

        if (parent.serverOS == 'ix')
        {
            if (parent.webspaceUser != 'true')
            {
		if (fileExt == ".Z")
		{
                    menuText = menuText 
                             + menuEntry("javascript:compress('" + scriptPreparedPath + "')",resourceBundle["label.uncompress"],null);
		}
		else
		{
                    menuText = menuText 
                             + menuEntry("javascript:compress('" + scriptPreparedPath + "')",resourceBundle["label.compress"],null);
		}
            }
        }
        else // win
        {
            menuText = menuText 
                     + menuEntry("javascript:switchReadWrite('" + scriptPreparedPath + "')",resourceBundle["label.switchReadOnly"],null);
        }
        
        if (parent.mailEnabled == 'true')
        {
            menuText = menuText 
                     + menuEntry("javascript:sendFile('" + scriptPreparedFile + "')",resourceBundle["label.sendfile"],null);
        }
        
	if (fileExt == ".MP3")
	{
            menuText = menuText 
                     + menuEntry("javascript:editMP3('" + scriptPreparedPath + "')",resourceBundle["label.editmp3"],null);
	}
        else
        {
            menuText = menuText 
                     + menuEntry("javascript:description('" + scriptPreparedPath + "')",resourceBundle["label.editMetaInfo"],null);
        }
        
    }
    
    menuText = menuText 
             + menuEntry("javascript:comments('" + scriptPreparedPath + "')",resourceBundle["label.comments"],null);
        
    if (parent.readonly == 'true')
    {
        menuText = menuText 
                 + menuEntry("javascript:diffSelect('" + scriptPreparedPath + "')",resourceBundle["label.diffSource"],null);
    }  
    else
    {
        menuText = menuText 
                 + menuEntry("javascript:extendedFileMenu('" + insertDoubleBackslash(shortFileName) + "', '" + scriptPreparedPath + "')",resourceBundle["label.menuMore"],null);
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
                         + menuEntry("/webfilesys/servlet?command=execProgram&progname=" + encodeURIComponent(fullPath),resourceBundle["label.run"],null);
            }
            else
            {
                menuText = menuText 
                         + menuEntry("javascript:associatedProg('" + scriptPreparedPath + "')",resourceBundle["label.open"],null);
            }
        }
    }
    else
    {
        if ((parent.webspaceUser != 'true') || (parent.chmodAllowed == 'true'))
        {
            menuText = menuText 
                     + menuEntry("javascript:accessRights('" + scriptPreparedPath + "')",resourceBundle["label.rights"],null);
        }

        if (parent.adminUser == 'true')
        {
            menuText = menuText 
                     + menuEntry("javascript:associatedProg('" + scriptPreparedPath + "')",resourceBundle["label.open"],null);
        }
    }

    menuText = menuText 
             + menuEntry("javascript:diffSelect('" + scriptPreparedPath + "')",resourceBundle["label.diffSource"],null);

    if (parent.readonly != 'true')
    {
        menuText = menuText 
                 + menuEntry("javascript:cloneFile('" + scriptPreparedFile + "')",resourceBundle["label.cloneFile"],null);
    }

    menuText = menuText 
             + menuEntry("javascript:hexView('" + scriptPreparedFile + "')", resourceBundle["label.hexView"], null);

    if (fileExt == ".AES")
    {
        menuText = menuText 
                 + menuEntry("javascript:decrypt('" + scriptPreparedFile + "')", resourceBundle["label.decrypt"], null);
    }
    else
    {
        menuText = menuText 
                 + menuEntry("javascript:encrypt('" + scriptPreparedFile + "')", resourceBundle["label.encrypt"], null);
    }

    menuText = menuText 
             + menuEntry("javascript:tail('" + scriptPreparedPath + "')", resourceBundle["label.tail"], null);

    if (parent.readonly != 'true')
    {
        menuText = menuText 
                 + menuEntry("javascript:touch('" + scriptPreparedFile + "')", resourceBundle["label.touch"], null);
    }

    menuText = menuText 
             + menuEntry("javascript:grep('" + scriptPreparedPath + "', '" + scriptPreparedFile + "')", resourceBundle["label.grep"], null);

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
             + menuEntry("javascript:diffSelect('" + scriptPreparedPath + "')",resourceBundle["label.diffTarget"],null);

    menuText = menuText 
             + menuEntry("javascript:cancelDiff('" + scriptPreparedPath + "')",resourceBundle["label.cancelDiff"],null);

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