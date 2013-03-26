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
        if (serverOS == 'ix')
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
        
    menuText = '<table border="0" width="180" cellpadding="0" cellspacing="0" height="100%">'
             + '<tr>'
             + '<th class="datahead" style="padding-left:5px;padding-right:5px;padding-top:4px;padding-bottom:4px;text-align:left;border-bottom-width:1px;border-bottom-style:solid;border-bottom-color:black;">'
             + shortFileName
             + '</th>'
             + '</tr>';

    if (fileExt == ".ZIP" || fileExt == ".JAR" || fileExt == ".WAR")
    {
        menuText = menuText 
                 + menuEntry("javascript:viewZip('" + scriptPreparedPath + "')",resourceBundle["menuViewZip"],null);
    }
    else
    {
	if (fileExt == ".URL")
	{
            menuText = menuText 
                     + menuEntry("/webfilesys/servlet?command=openUrlFile&actPath=" + encodeURIComponent(fullPath) + "&random=" + (new Date().getTime()),resourceBundle["menuView"],"_blank");
	}
	else
	{
            menuText = menuText 
                     + menuEntry("javascript:viewFile('" + scriptPreparedPath + "')",resourceBundle["menuView"],null);
	}
    }
        
    if (fileExt == ".MP3")
    {
        downloadLabel = resourceBundle["menuPlay"];
    }
    else
    {
        downloadLabel = resourceBundle["menuDownload"];
    }

    menuText = menuText 
             + menuEntry("/webfilesys/servlet?command=getFile&filePath=" + encodeURIComponent(fullPath) + "&disposition=download",downloadLabel,null);

    if (readonly != 'true')
    {
        menuText = menuText 
                 + menuEntry("javascript:delFile('" + scriptPreparedFile + "')",resourceBundle["menuDelete"],null);

        menuText = menuText 
                 + menuEntry("javascript:renameFile('" + scriptPreparedFile + "')",resourceBundle["menuRename"],null);

        menuText = menuText 
                 + menuEntry("javascript:copyToClipboard('" + scriptPreparedFile + "')",resourceBundle["menuCopy"],null);

        menuText = menuText 
                 + menuEntry("javascript:cutToClipboard('" + scriptPreparedFile + "')",resourceBundle["menuCut"],null);

        menuText = menuText 
                 + menuEntry("javascript:editRemote('" + scriptPreparedFile + "')",resourceBundle["menuEdit"],null);

	if ((fileExt == ".ZIP") || (fileExt == ".JAR") || (fileExt == ".WAR"))
	{
            menuText = menuText 
                     + menuEntry("javascript:zipFile('" + scriptPreparedPath + "')",resourceBundle["menuUnzip"],null);
        }
        else
        {
            menuText = menuText 
                     + menuEntry("javascript:zipFile('" + scriptPreparedPath + "')",resourceBundle["menuZip"],null);
        }
        
        if (mailEnabled == 'true')
        {
            menuText = menuText 
                     + menuEntry("javascript:sendFile('" + scriptPreparedFile + "')",resourceBundle["menuSendFile"],null);
        }
        
	if (fileExt == ".MP3")
	{
            menuText = menuText 
                     + menuEntry("javascript:editMP3('" + scriptPreparedPath + "')",resourceBundle["menuEditMP3"],null);
	}
        else
        {
            menuText = menuText 
                     + menuEntry("javascript:editMetaInfo('" + scriptPreparedFile + "')",resourceBundle["menuEditDesc"],null);
        }
        
    }
    
    menuText = menuText 
             + menuEntry("javascript:comments('" + scriptPreparedPath + "')",resourceBundle["menuComments"],null);
        
    menuText = menuText + '</table>'; 

    menuDiv.innerHTML = menuText;

    menuDiv.style.bgcolor = '#c0c0c0';
    
    var maxMenuHeight;
    
    if (readonly == 'true')
    {
        maxMenuHeight = 120;
    }
    else
    {
        maxMenuHeight = 240;
    }
    
    // var browserIsFirefox = /a/[-1]=='a';
    var browserIsFirefox = navigator.userAgent.toLowerCase().indexOf('firefox') > -1;

    if (browserIsFirefox)
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
    
    menuDiv.style.left = (clickXPos - 50) + 'px';
    menuDiv.style.top = clickYPos + 'px';

    menuDiv.style.visibility = 'visible';
}
