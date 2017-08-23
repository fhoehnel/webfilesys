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

    menuText = menuText 
                 + menuEntry("javascript:playVideoLocal('" + scriptPreparedPath + "')", resourceBundle["playVideoLocally"]);

    if (parent.readonly != 'true') {
    	menuText = menuText 
                 + menuEntry("javascript:delVideo('" + scriptPreparedFile + "')", resourceBundle["label.delete"]);

        menuText = menuText 
                 + menuEntry("javascript:renameVideo('" + scriptPreparedFile + "', '" + domId + "')", resourceBundle["label.renameFile"]);

        menuText = menuText 
                 + menuEntry("javascript:copyToClipboard('" + scriptPreparedFile + "')", resourceBundle["label.copyToClip"]);

        menuText = menuText 
                 + menuEntry("javascript:cutToClipboard('" + scriptPreparedFile + "')", resourceBundle["label.cutToClip"]);

        menuText = menuText 
                 + menuEntry("javascript:editVideoDesc('" + scriptPreparedPath + "')", resourceBundle["label.editMetaInfo"]);

        menuText = menuText 
                 + menuEntry("javascript:editConvertVideo('" + scriptPreparedFile + "')", resourceBundle["contextMenuEditVideo"]);
    }
        
    menuText = menuText 
             + menuEntry("javascript:videoComments('" + scriptPreparedPath + "')", resourceBundle["label.comments"]);
    
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
        
    var realDir;
    if (parent.serverOS == 'win') {
        realDir = realPath.substring(0, realPath.lastIndexOf('\\'));
        if (realDir.length < 3) {
            realDir = realDir + "\\";
        }
    } else {
        realDir = realPath.substring(0, realPath.lastIndexOf('/'));
        if (realDir.length == 0) {
            realDir = "/";
        }
    }

    var menuText = '<table class="contextMenu">'
                 + '<tr>'
                 + '<th>'
                 + shortLinkName
                 + '</th>'
                 + '</tr>';

    menuText = menuText 
             + menuEntry("javascript:playVideoLocal('" + scriptPreparedPath + "')", resourceBundle["playVideoLocally"]);
    
    if (parent.readonly != 'true') {
        menuText = menuText 
                 + menuEntry("javascript:editVideoDesc('" + scriptPreparedPath + "')", resourceBundle["label.editMetaInfo"]);
        
        menuText = menuText 
                 + menuEntry("javascript:renameLink('" + linkName + "')", resourceBundle["label.renameLink"]);
    }

    menuText = menuText 
             + menuEntry("javascript:videoComments('" + scriptPreparedPath + "')", resourceBundle["label.comments"]);
    
    menuText = menuText 
             + menuEntry("javascript:gotoOrigDir('" + insertDoubleBackslash(realDir) + "')", resourceBundle["label.origDir"]);
    
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

function editConvertVideo(fileName) {
	window.location.href = "/webfilesys/servlet?command=editVideoParams&videoFile=" + encodeURIComponent(fileName);
}

function editVideoDesc(path) {
    var windowWidth = 600;
    var windowHeight = 450;
    
    var xpos = (screen.width - windowWidth) / 2;
    var ypos = (screen.height - windowHeight) / 2;

    descWin=window.open("/webfilesys/servlet?command=editMetaInf&path=" + encodeURIComponent(path) + "&geoTag=true&random=" + new Date().getTime(),"descWin","status=no,toolbar=no,location=no,menu=no,width=" + windowWidth + ",height=" + windowHeight + ",resizable=yes,left=" + xpos + ",top=" + ypos + ",screenX=" + xpos + ",screenY=" + ypos);
    descWin.focus();
    descWin.opener=self;
}

function copyToClipboard(fileName) {
    cutCopyToClip(fileName, 'copy');
}

function cutToClipboard(fileName) {
    cutCopyToClip(fileName, 'move');
}

function gotoOrigDir(path) {
    parent.parent.frames[1].location.href = "/webfilesys/servlet?command=exp&expandPath=" + encodeURIComponent(path) + "&fastPath=true";
}

function videoComments(path) {
    commentWin=window.open("/webfilesys/servlet?command=listComments&actPath=" + encodeURIComponent(path),"commentWin","status=no,toolbar=no,location=no,menu=no,scrollbars=yes,width=550,height=400,resizable=yes,left=80,top=100,screenX=80,screenY=100");
    commentWin.focus();
}

function playVideoLocal(path) {
	toast(resourceBundle["playerStartedInBackground"], 4000);
	
    var url = "/webfilesys/servlet?command=playVideoLocal&videoPath=" + encodeURIComponent(path);
	
	xmlRequest(url, function(req) {
        if (req.readyState == 4) {
            if (req.status == 200) {
			    var xmlDoc = req.responseXML;

			    var item = xmlDoc.getElementsByTagName("success")[0];            
                if (item) {
                    var success = item.firstChild.nodeValue;
                    if (success != "true") {
                    	customAlert(resourceBundle["errorVideoPlayer"]);
                    }
                } else {
                	customAlert(resourceBundle["errorVideoPlayer"]);
                }
            } else {
                alert(resourceBundle["alert.communicationFailure"]);
            }
        }
	});
}
