function jsLinkMenu(linkName, realPath)
{
    menuDiv = document.getElementById('contextMenu');    
        
    shortFileName = linkName;
    
    if (linkName.length > 24)
    {
        shortFileName = linkName.substring(0,7) + "..." + linkName.substring(linkName.length - 14, linkName.length);
    }    

    fileNameExt = getFileNameExt(linkName);

    scriptPreparedPath = insertDoubleBackslash(realPath);

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
                     + menuEntry("/webfilesys/servlet?command=openUrlFile&actPath=" + encodeURIComponent(realPath) + "&random=" + (new Date().getTime()),resourceBundle["menuView"],"_blank");
	}
	else
	{
	    if (fileExt != ".MP3") 
	    {
                menuText = menuText 
                         + menuEntry("javascript:viewFile('" + scriptPreparedPath + "')",resourceBundle["menuView"],null);
	    }
	}
    }

    if (fileExt == ".MP3")
    {
        downloadLabel = resourceBundle["label.play"];
    }
    else
    {
        downloadLabel = resourceBundle["label.download"];
    }

    menuText = menuText 
             + menuEntry("/webfilesys/servlet?command=getFile&filePath=" + encodeURIComponent(realPath) + "&disposition=download",downloadLabel,null);

    if (readonly != 'true')
    {
        menuText = menuText 
                 + menuEntry("javascript:delLink('" + linkName + "')",resourceBundle["menuDelLink"],null);

        menuText = menuText 
                 + menuEntry("javascript:renameLink('" + linkName + "')",resourceBundle["menuRenLink"],null);

        menuText = menuText 
                 + menuEntry("javascript:editRemoteLink('" + scriptPreparedPath + "')",resourceBundle["menuEdit"],null);

        if (parent.mailEnabled == 'true')
        {
            menuText = menuText 
                     + menuEntry("javascript:emailLink('" + scriptPreparedPath + "')",resourceBundle["menuSendFile"],null);
        }
    }

    menuText = menuText 
             + menuEntry("javascript:comments('" + scriptPreparedPath + "')",resourceBundle["menuComments"],null);

    if (serverOS == 'win')
    {
        realDir = realPath.substring(0,realPath.lastIndexOf('\\'));
        
        if (realDir.length < 3)
        {
            realDir = realDir + "\\";
        }
    }
    else
    {
        realDir = realPath.substring(0,realPath.lastIndexOf('/'));
        
        if (realDir.length == 0)
        {
            realDir = "/";
        }
        
    }

    menuText = menuText 
             + menuEntry("javascript:origDir('" + insertDoubleBackslash(realDir) + "')",resourceBundle["menuOrigDir"],null);
        
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
        maxMenuHeight = 200;
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

function editRemoteLink(path)
{
    window.location.href = '/webfilesys/servlet?command=mobile&cmd=editFile&filePath=' + encodeURIComponent(path) + '&screenHeight=' + screen.height;
}

function origDir(path)
{
    window.location.href = "/webfilesys/servlet?command=mobile&cmd=folderFileList&absPath=" + encodeURIComponent(path);
}

function emailLink(filePath)
{
    showPrompt('/webfilesys/servlet?command=emailFilePrompt&filePath=' + encodeURIComponent(filePath), '/webfilesys/xsl/emailFile.xsl', 400, 210);
    
    document.emailForm.receiver.focus();
    
    document.emailForm.receiver.select();
}


