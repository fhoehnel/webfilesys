function videoContextMenu(fileName, domId) {
    var shortFileName = fileName;
    
    if (fileName.length > 24) {
        shortFileName = fileName.substring(0,7) + "..." + fileName.substring(fileName.length - 14, fileName.length);
    }    

    var fileNameExt = getFileNameExt(fileName);
    
    var lastPathChar = path.charAt(path.length - 1);
    
    var fullPath;
    
    if ((lastPathChar == '/') || (lastPathChar == '\\')) {
        fullPath = path + fileName;
    } else {
        if (parent.serverOS == 'ix') {
            fullPath = path + '/' + fileName;
        } else {
            fullPath = path + '\\' + fileName;
        }
    }

    var scriptPreparedPath = insertDoubleBackslash(fullPath);

    var scriptPreparedFile = insertDoubleBackslash(fileName);
        
    var menuText = '<table class="contextMenu">'
                 + '<tr>'
                 + '<th>'
                 + shortFileName
                 + '</th>'
                 + '</tr>';

    if (parent.readonly != 'true') {
        menuText = menuText 
                 + menuEntry("javascript:delVideo('" + scriptPreparedFile + "')",resourceBundle["label.delete"]);

        menuText = menuText 
                 + menuEntry("javascript:renameVideo('" + scriptPreparedFile + "', '" + domId + "')",resourceBundle["label.renameFile"]);

        menuText = menuText 
                 + menuEntry("javascript:copyToClipboard('" + scriptPreparedFile + "')",resourceBundle["label.copyToClip"]);

        menuText = menuText 
                 + menuEntry("javascript:cutToClipboard('" + scriptPreparedFile + "')",resourceBundle["label.cutToClip"]);

        menuText = menuText 
                 + menuEntry("javascript:editVideoDesc('" + scriptPreparedPath + "')",resourceBundle["label.editMetaInfo"]);
    }
        
    menuText = menuText + '</table>'; 

    var menuDiv = document.getElementById('contextMenu');    
    
    menuDiv.innerHTML = menuText;

    menuDiv.style.bgcolor = '#c0c0c0';
    
    var maxMenuHeight = 240;
    if (parent.readonly == 'true') {
        maxMenuHeight = 120;
    } 
    
    positionMenuDiv(menuDiv, maxMenuHeight);
}

function videoLinkMenu(linkName, realPath, domId) {
        
    var shortLinkName = linkName;
    
    if (shortLinkName.length > 24) {
    	shortLinkName = linkName.substring(0,7) + "..." + linkName.substring(linkName.length - 14, linkName.length);
    }    

    var scriptPreparedPath = insertDoubleBackslash(realPath);

    var scriptPreparedFile = insertDoubleBackslash(linkName);
        
    var menuText = '<table class="contextMenu">'
                 + '<tr>'
                 + '<th>'
                 + shortLinkName
                 + '</th>'
                 + '</tr>';

    if (parent.readonly != 'true') {
        menuText = menuText 
                 + menuEntry("javascript:editVideoDesc('" + scriptPreparedPath + "')", resourceBundle["label.editMetaInfo"]);
        
        menuText = menuText 
                 + menuEntry("javascript:renameLink('" + linkName + "')", resourceBundle["label.renameLink"]);
    }
        
    menuText = menuText + '</table>'; 

    var menuDiv = document.getElementById('contextMenu');    

    menuDiv.innerHTML = menuText;

    menuDiv.style.bgcolor = '#c0c0c0';
    
    var maxMenuHeight = 200;
    if (parent.readonly == 'true') {
        maxMenuHeight = 100;
    }
    
    positionMenuDiv(menuDiv, maxMenuHeight);
}

function delVideo(fileName) {
    centeredDialog('/webfilesys/servlet?command=ajaxRPC&method=deleteFilePrompt&param1=' + encodeURIComponent(fileName), 
                   '/webfilesys/xsl/confirmDeleteFile.xsl', 
                   320, 130);
}

function renameVideo(fileName, domId) {
    centeredDialog('/webfilesys/servlet?command=renameImagePrompt&imageFile=' + encodeURIComponent(fileName), '/webfilesys/xsl/renameVideo.xsl', 360, 160, function() {
    	document.renameForm.domId.value = domId;
    	document.renameForm.newFileName.focus();
        document.renameForm.newFileName.select();
    });
}

function editVideoDesc(path)
{
    var windowWidth = 600;
    var windowHeight = 450;
    
    var xpos = (screen.width - windowWidth) / 2;
    var ypos = (screen.height - windowHeight) / 2;

    descWin=window.open("/webfilesys/servlet?command=editMetaInf&path=" + encodeURIComponent(path) + "&geoTag=true&random=" + new Date().getTime(),"descWin","status=no,toolbar=no,location=no,menu=no,width=" + windowWidth + ",height=" + windowHeight + ",resizable=yes,left=" + xpos + ",top=" + ypos + ",screenX=" + xpos + ",screenY=" + ypos);
    descWin.focus();
    descWin.opener=self;
}

function copyToClipboard(fileName)
{
    cutCopyToClip(fileName, 'copy');
}

function cutToClipboard(fileName)
{
    cutCopyToClip(fileName, 'move');
}

