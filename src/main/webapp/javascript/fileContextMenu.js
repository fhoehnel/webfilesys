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
        
    menuText = '<table border="0" cellpadding="0" cellspacing="0" style="width:180px">'
             + '<tr>'
             + '<th class="datahead" style="padding-left:5px;padding-right:5px;padding-top:4px;padding-bottom:4px;text-align:left;border-bottom-width:1px;border-bottom-style:solid;border-bottom-color:black;">'
             + shortFileName
             + '</th>'
             + '</tr>';

    if (fileExt == ".ZIP" || fileExt == ".JAR" || fileExt == ".WAR" || fileExt == ".EAR") {
        menuText = menuText 
                 + menuEntry("javascript:viewZip('" + scriptPreparedPath + "')",resourceBundle["label.viewzip"]);
    } else if (fileExt == ".URL") {
        menuText = menuText 
                 + menuEntry("javascript:openUrlFile('" + scriptPreparedPath + "')",resourceBundle["label.view"]);
    } else {
         if ((fileExt == ".MP4") || (fileExt == ".OGG") || (fileExt == ".OGV")|| (fileExt == ".WEBM")) {
             menuText = menuText 
                      + menuEntry("javascript:playVideo('" + scriptPreparedPath + "')", resourceBundle["label.playVideo"]);
         }
         menuText = menuText 
                  + menuEntry("javascript:viewFile('" + scriptPreparedPath + "')",resourceBundle["label.view"]);
    }
        
    if (parent.clientIsLocal != 'true') {
	    if (fileExt == ".MP3") {
	        downloadLabel = resourceBundle["label.play"];
	    } else {
	        downloadLabel = resourceBundle["label.download"];
	    }

        menuText = menuText 
                 + menuEntry("javascript:downloadFile('" + scriptPreparedPath + "')",downloadLabel);
    }

    if (parent.readonly != 'true')
    {
        menuText = menuText 
                 + menuEntry("javascript:delFile('" + scriptPreparedFile + "')",resourceBundle["label.delete"]);

        menuText = menuText 
                 + menuEntry("javascript:renameFile('" + scriptPreparedFile + "')",resourceBundle["label.renameFile"]);

        menuText = menuText 
                 + menuEntry("javascript:copyToClipboard('" + scriptPreparedFile + "')",resourceBundle["label.copyToClip"]);

        if (addCopyAllowed) 
        {
            menuText = menuText 
                     + menuEntry("javascript:addCopyToClipboard('" + scriptPreparedFile + "')",resourceBundle["label.copyToClip"] + " +");
        }

        menuText = menuText 
                 + menuEntry("javascript:cutToClipboard('" + scriptPreparedFile + "')",resourceBundle["label.cutToClip"]);
       
        if (addMoveAllowed) 
        {
            menuText = menuText 
                     + menuEntry("javascript:addMoveToClipboard('" + scriptPreparedFile + "')",resourceBundle["label.cutToClip"] + " +");
        }

	if (parent.localEditor == 'true')
	{
        menuText = menuText 
                 + menuEntry("javascript:editLocal('" + scriptPreparedFile + "')",resourceBundle["label.edit"]);
    }
    else
    {
        menuText = menuText 
                 + menuEntry("javascript:editRemote('" + scriptPreparedFile + "')",resourceBundle["label.edit"]);
    }

	if ((fileExt == ".ZIP") || (fileExt == ".JAR") || (fileExt == ".WAR") || (fileExt == ".EAR"))
	{
            menuText = menuText 
                     + menuEntry("javascript:zip('" + scriptPreparedPath + "')",resourceBundle["label.unzip"]);
        }
        else
        {
  	    if ((fileExt == ".GZ")  || (fileExt == ".GZIP"))
  	    {
                menuText = menuText 
                         + menuEntry("javascript:gunzip('" + scriptPreparedPath + "')",resourceBundle["label.unzip"]);
  	    }
  	    else
  	    {
                menuText = menuText 
                         + menuEntry("javascript:zip('" + scriptPreparedPath + "')",resourceBundle["label.zip"]);
  	    }
        }

        if (fileExt == ".TAR")
        {
            menuText = menuText 
                     + menuEntry("javascript:untar('" + scriptPreparedPath + "')",resourceBundle["label.untar"]);
        }

        if (parent.serverOS == 'ix')
        {
            if (parent.webspaceUser != 'true')
            {
		if (fileExt == ".Z")
		{
                    menuText = menuText 
                             + menuEntry("javascript:compress('" + scriptPreparedPath + "')",resourceBundle["label.uncompress"]);
		}
		else
		{
                    menuText = menuText 
                             + menuEntry("javascript:compress('" + scriptPreparedPath + "')",resourceBundle["label.compress"]);
		}
            }
        }
        else // win
        {
            menuText = menuText 
                     + menuEntry("javascript:switchReadWrite('" + scriptPreparedPath + "')",resourceBundle["label.switchReadOnly"]);
        }
        
        if (parent.mailEnabled == 'true')
        {
            menuText = menuText 
                     + menuEntry("javascript:sendFile('" + scriptPreparedFile + "')",resourceBundle["label.sendfile"]);
        }
        
	if (fileExt == ".MP3")
	{
            menuText = menuText 
                     + menuEntry("javascript:editMP3('" + scriptPreparedPath + "')",resourceBundle["label.editmp3"]);
	}
        else
        {
            menuText = menuText 
                     + menuEntry("javascript:description('" + scriptPreparedPath + "')",resourceBundle["label.editMetaInfo"]);
        }
        
    }
    
    menuText = menuText 
             + menuEntry("javascript:comments('" + scriptPreparedPath + "')",resourceBundle["label.comments"]);
        
    if (parent.readonly == 'true')
    {
        menuText = menuText 
                 + menuEntry("javascript:diffSelect('" + scriptPreparedPath + "')",resourceBundle["label.diffSource"]);
    }  
    else
    {
        menuText = menuText 
                 + menuEntry("javascript:extendedFileMenu('" + insertDoubleBackslash(shortFileName) + "', '" + scriptPreparedPath + "')",resourceBundle["label.menuMore"]);
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

    var menuText = '<table class="contextMenu">'
             + '<tr>'
             + '<th>'
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
                         + menuEntry("javascript:execNativeProgram('" + scriptPreparedPath + "')",resourceBundle["label.run"]);
            }
            else
            {
                menuText = menuText 
                         + menuEntry("javascript:associatedProg('" + scriptPreparedPath + "')",resourceBundle["label.open"]);
            }
        }
    }
    else
    {
        if ((parent.webspaceUser != 'true') || (parent.chmodAllowed == 'true'))
        {
            menuText = menuText 
                     + menuEntry("javascript:accessRights('" + scriptPreparedPath + "')",resourceBundle["label.rights"]);
        }

        if (parent.adminUser == 'true')
        {
            menuText = menuText 
                     + menuEntry("javascript:associatedProg('" + scriptPreparedPath + "')",resourceBundle["label.open"]);
        }
    }

    menuText = menuText 
             + menuEntry("javascript:diffSelect('" + scriptPreparedPath + "')",resourceBundle["label.diffSource"]);

    if (parent.readonly != 'true')
    {
        menuText = menuText 
                 + menuEntry("javascript:cloneFile('" + scriptPreparedFile + "')",resourceBundle["label.cloneFile"]);
    }

    menuText = menuText 
             + menuEntry("javascript:hexView('" + scriptPreparedFile + "')", resourceBundle["label.hexView"]);

    if (fileExt == ".AES")
    {
        menuText = menuText 
                 + menuEntry("javascript:decrypt('" + scriptPreparedFile + "')", resourceBundle["label.decrypt"]);
    }
    else
    {
        menuText = menuText 
                 + menuEntry("javascript:encrypt('" + scriptPreparedFile + "')", resourceBundle["label.encrypt"]);
    }

    menuText = menuText 
             + menuEntry("javascript:tail('" + scriptPreparedPath + "')", resourceBundle["label.tail"]);

    if (parent.readonly != 'true')
    {
        menuText = menuText 
                 + menuEntry("javascript:touch('" + scriptPreparedFile + "')", resourceBundle["label.touch"]);
    }

    menuText = menuText 
             + menuEntry("javascript:grep('" + scriptPreparedPath + "', '" + scriptPreparedFile + "')", resourceBundle["label.grep"]);

    menuText = menuText + '</table>'; 

    menuDiv.innerHTML = menuText;
    
    menuDiv.style.visibility = 'visible';
}

function diffSelectTarget(path, shortFileName, scriptPreparedPath)
{
    var menuDiv = document.getElementById('contextMenu');    

    menuDiv.style.visibility = 'hidden';

    var menuText = '<table class="contextMenu">'
             + '<tr>'
             + '<th>'
             + shortFileName
             + '</th>'
             + '</tr>';
    
    menuText = menuText 
             + menuEntry("javascript:diffSelect('" + scriptPreparedPath + "')",resourceBundle["label.diffTarget"]);

    menuText = menuText 
             + menuEntry("javascript:cancelDiff('" + scriptPreparedPath + "')",resourceBundle["label.cancelDiff"]);

    menuText = menuText + '</table>'; 

    menuDiv.innerHTML = menuText;
    
    positionMenuDiv(menuDiv, 120);

    menuDiv.style.visibility = 'visible';
}

