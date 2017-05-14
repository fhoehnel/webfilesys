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

    menuText = '<table style="width:100%">'
             + '<tr>'
             + '<th class="datahead" style="padding-left:5px;padding-right:5px;padding-top:4px;padding-bottom:4px;text-align:left;border-bottom-width:1px;border-bottom-style:solid;border-bottom-color:black;">'
             + shortFileName
             + '</th>'
             + '</tr>';


    if (fileExt == ".ZIP" || fileExt == ".JAR" || fileExt == ".WAR")
    {
        menuText = menuText 
                 + menuEntry("javascript:viewZip('" + scriptPreparedPath + "')",resourceBundle["menuViewZip"]);
    }
    else
    {
	if (fileExt == ".URL")
	{
            menuText = menuText 
                     + menuEntry("javascript:openUrlFile('" + scriptPreparedPath + "')",resourceBundle["menuView"]);
	}
	else
	{
	    if (fileExt != ".MP3") 
	    {
                menuText = menuText 
                         + menuEntry("javascript:viewFile('" + scriptPreparedPath + "')",resourceBundle["menuView"]);
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
             + menuEntry("javascript:downloadFile('" + scriptPreparedPath + "')",downloadLabel);

    if (readonly != 'true')
    {
        menuText = menuText 
                 + menuEntry("javascript:delLink('" + linkName + "')",resourceBundle["menuDelLink"]);

        menuText = menuText 
                 + menuEntry("javascript:renameLink('" + linkName + "')",resourceBundle["menuRenLink"]);

        menuText = menuText 
                 + menuEntry("javascript:editRemoteLink('" + scriptPreparedPath + "')",resourceBundle["menuEdit"]);

        if (parent.mailEnabled == 'true')
        {
            menuText = menuText 
                     + menuEntry("javascript:emailLink('" + scriptPreparedPath + "')",resourceBundle["menuSendFile"]);
        }
    }

    menuText = menuText 
             + menuEntry("javascript:comments('" + scriptPreparedPath + "')",resourceBundle["menuComments"]);

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
             + menuEntry("javascript:origDir('" + insertDoubleBackslash(realDir) + "')",resourceBundle["menuOrigDir"]);
        
    menuText = menuText + '</table>'; 

    menuDiv.innerHTML = menuText;

    positionMenuDiv(menuDiv);
}

function editRemoteLink(path)
{
    window.location.href = '/webfilesys/servlet?command=mobile&cmd=editFile&filePath=' + encodeURIComponent(path) + '&screenHeight=' + screen.height;
}

function origDir(path)
{
    window.location.href = "/webfilesys/servlet?command=mobile&cmd=folderFileList&absPath=" + encodeURIComponent(path);
}

function emailLink(filePath) {
    centeredDialog('/webfilesys/servlet?command=emailFilePrompt&filePath=' + encodeURIComponent(filePath), '/webfilesys/xsl/emailFile.xsl', 400, 250, function() {
        document.emailForm.receiver.focus();
        document.emailForm.receiver.select();
    });
}


