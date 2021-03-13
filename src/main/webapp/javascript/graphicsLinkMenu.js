function linkGraphicsMenu(linkName, realPath, imgType)
{
    menuDiv = document.getElementById('contextMenu');    
    
    menuDiv.style.visibility = 'hidden';

    menuDiv.innerHTML = "";
        
    shortFileName = linkName;
    
    if (linkName.length > 24)
    {
        shortFileName = linkName.substring(0,7) + "..." + linkName.substring(linkName.length - 14, linkName.length);
    }    

    fileNameExt = getFileNameExt(linkName);

    scriptPreparedPath = insertDoubleBackslash(realPath);

    addContextMenuHead(menuDiv, shortFileName);

    if (parent.readonly != 'true') {
    	
    	addContextMenuEntry(menuDiv, "jsDelImageLink('" + linkName + "')", resourceBundle["label.deleteLink"]);

    	addContextMenuEntry(menuDiv, "renameLink('" + linkName + "')", resourceBundle["label.renameLink"]);

    	addContextMenuEntry(menuDiv, "jsDescription('" + scriptPreparedPath + "')", resourceBundle["label.editMetaInfo"]);
    }
    
    if (imgType == '1') { // JPEG
    	addContextMenuEntry(menuDiv, "jsExifData('" + scriptPreparedPath + "')", resourceBundle["alt.cameradata"]);
    }
    
	addContextMenuEntry(menuDiv, "jsComments('" + scriptPreparedPath + "')", resourceBundle["label.comments"]);

    if ((parent.readonly != 'true') && 
        (parent.mailEnabled == 'true')) {
    	addContextMenuEntry(menuDiv, "emailLink('" + scriptPreparedPath + "')", resourceBundle["label.sendfile"]);
    }

    if (parent.serverOS == 'win') {
        realDir = realPath.substring(0,realPath.lastIndexOf('\\'));
        if (realDir.length < 3) {
            realDir = realDir + "\\";
        }
    } else {
        realDir = realPath.substring(0,realPath.lastIndexOf('/'));
        if (realDir.length == 0) {
            realDir = "/";
        }
    }

	addContextMenuEntry(menuDiv, "origDir('" + insertDoubleBackslash(realDir) + "')", resourceBundle["label.origDir"]);

    if (parent.readonly == 'true') {
        maxMenuHeight = 140;
    } else {
        maxMenuHeight = 220;
    }
    
    positionMenuDiv(menuDiv, maxMenuHeight);

    menuDiv.style.visibility = 'visible';
}

function jsDelImageLink(linkName) {
    window.location.href="/webfilesys/servlet?command=delImageLink&linkName=" + encodeURIComponent(linkName);
}

function jsDescription(path) {
    descWin=window.open("/webfilesys/servlet?command=editMetaInf&path=" + encodeURIComponent(path) + "&random=" + new Date().getTime(),"descWin","status=no,toolbar=no,location=no,menu=no,width=600,height=300,resizable=yes,left=20,top=100,screenX=20,screenY=100");
    descWin.focus();
    descWin.opener=self;
}

function origDir(path) {
    parent.parent.frames[1].location.href="/webfilesys/servlet?command=exp&expandPath=" + encodeURIComponent(path) + "&fastPath=true";
}

function emailLink(filePath) {
    centeredDialog('/webfilesys/servlet?command=emailFilePrompt&filePath=' + encodeURIComponent(filePath), '/webfilesys/xsl/emailFile.xsl', 400, 250, function() {
        document.emailForm.receiver.focus();
        document.emailForm.receiver.select();
    });
}
