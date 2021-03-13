function videoContextMenu(fileName, domId) {
	
    const menuDiv = document.getElementById('contextMenu');    
    
    menuDiv.style.visibility = 'hidden';

    menuDiv.innerHTML = "";
	
    let shortFileName = fileName;
    
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

    const scriptPreparedPath = insertDoubleBackslash(fullPath);

    const scriptPreparedFile = insertDoubleBackslash(fileName);
        
    addContextMenuHead(menuDiv, shortFileName);

	addContextMenuEntry(menuDiv, "playVideoLocal('" + scriptPreparedPath + "')", resourceBundle["playVideoLocally"]);

    if (parent.readonly != 'true') {
    	addContextMenuEntry(menuDiv, "delVideo('" + scriptPreparedFile + "')", resourceBundle["label.delete"]);

    	addContextMenuEntry(menuDiv, "renameVideo('" + scriptPreparedFile + "', '" + domId + "')", resourceBundle["label.renameFile"]);
 
    	addContextMenuEntry(menuDiv, "copyToClipboard('" + scriptPreparedFile + "')", resourceBundle["label.copyToClip"]);

    	addContextMenuEntry(menuDiv, "cutToClipboard('" + scriptPreparedFile + "')", resourceBundle["label.cutToClip"]);

    	addContextMenuEntry(menuDiv, "editVideoDesc('" + scriptPreparedPath + "')", resourceBundle["label.editMetaInfo"]);

    	addContextMenuEntry(menuDiv, "editConvertVideo('" + scriptPreparedFile + "')", resourceBundle["contextMenuEditVideo"]);

    	addContextMenuEntry(menuDiv, "extractVideoFrame('" + scriptPreparedFile + "')", resourceBundle["contextMenuExtractVideoFrame"]);

    	addContextMenuEntry(menuDiv, "addAudioToVideo('" + scriptPreparedPath + "')", resourceBundle["contextMenuAddAudioToVideo"]);
        
    	addContextMenuEntry(menuDiv, "deshakeVideo('" + scriptPreparedFile + "')", resourceBundle["contextMenuDeshakeVideo"]);

    	addContextMenuEntry(menuDiv, "textOnVideo('" + scriptPreparedFile + "')", resourceBundle["contextMenuTextOnVideo"]);

    	addContextMenuEntry(menuDiv, "fadeAudio('" + scriptPreparedFile + "')", resourceBundle["contextMenuFadeAdioInOut"]);

    	addContextMenuEntry(menuDiv, "addSilentAudio('" + scriptPreparedFile + "')", resourceBundle["contextMenuAddSilentAudio"]);
    }
        
	addContextMenuEntry(menuDiv, "videoComments('" + scriptPreparedPath + "')", resourceBundle["label.comments"]);
    
    let maxMenuHeight = 240;
    if (parent.readonly == 'true') {
        maxMenuHeight = 120;
    } 
    
    positionMenuDiv(menuDiv, maxMenuHeight);
    menuDiv.style.visibility = 'visible';
}

function videoLinkMenu(linkName, realPath, domId) {
        
    const menuDiv = document.getElementById('contextMenu');    
    
    menuDiv.style.visibility = 'hidden';

    menuDiv.innerHTML = "";
	
    let shortLinkName = linkName;
    
    if (shortLinkName.length > 24) {
    	shortLinkName = linkName.substring(0,7) + "..." + linkName.substring(linkName.length - 14, linkName.length);
    }    

    const scriptPreparedPath = insertDoubleBackslash(realPath);

    const scriptPreparedFile = insertDoubleBackslash(linkName);
        
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
    
    addContextMenuHead(menuDiv, shortLinkName);

	addContextMenuEntry(menuDiv, "playVideoLocal('" + scriptPreparedPath + "')", resourceBundle["playVideoLocally"]);
    
    if (parent.readonly != 'true') {
    	addContextMenuEntry(menuDiv, "editVideoDesc('" + scriptPreparedPath + "')", resourceBundle["label.editMetaInfo"]);
        
    	addContextMenuEntry(menuDiv, "renameLink('" + linkName + "')", resourceBundle["label.renameLink"]);
    }

	addContextMenuEntry(menuDiv, "videoComments('" + scriptPreparedPath + "')", resourceBundle["label.comments"]);
    
	addContextMenuEntry(menuDiv, "gotoOrigDir('" + insertDoubleBackslash(realDir) + "')", resourceBundle["label.origDir"]);
    
    let maxMenuHeight = 200;
    if (parent.readonly == 'true') {
        maxMenuHeight = 100;
    }
    
    positionMenuDiv(menuDiv, maxMenuHeight);
    
    menuDiv.style.visibility = 'visible';
}

function delVideo(fileName) {
    centeredDialog('/webfilesys/servlet?command=ajaxRPC&method=deleteFilePrompt&param1=' + encodeURIComponent(fileName), 
                   '/webfilesys/xsl/confirmDeleteFile.xsl', 
                   320, 130);
}

function renameVideo(fileName, domId) {
    centeredDialog('/webfilesys/servlet?command=renameImagePrompt&imageFile=' + encodeURIComponent(fileName), '/webfilesys/xsl/renameVideo.xsl', 360, 160, function() {
    	document.renameForm.newFileName.focus();
        document.renameForm.newFileName.select();
    });
}

function editConvertVideo(fileName) {
	window.location.href = "/webfilesys/servlet?command=video&cmd=editVideoParams&videoFile=" + encodeURIComponent(fileName);
}

function textOnVideo(fileName) {
	window.location.href = "/webfilesys/servlet?command=video&cmd=textOnVideoParams&videoFile=" + encodeURIComponent(fileName);
}

function textOnVideo(fileName) {
	window.location.href = "/webfilesys/servlet?command=video&cmd=textOnVideoParams&videoFile=" + encodeURIComponent(fileName);
}

function fadeAudio(fileName) {
	window.location.href = "/webfilesys/servlet?command=video&cmd=fadeAudioParams&videoFile=" + encodeURIComponent(fileName);
}

function extractVideoFrame(fileName) {
	window.location.href = "/webfilesys/servlet?command=video&cmd=extractVideoFrameParams&videoFile=" + encodeURIComponent(fileName);
}

function editVideoDesc(path) {
    const windowWidth = 680;
    const windowHeight = 520;
    
    const xpos = (screen.width - windowWidth) / 2;
    const ypos = (screen.height - windowHeight) / 2;

    descWin = window.open("/webfilesys/servlet?command=editMetaInf&path=" + encodeURIComponent(path) + "&geoTag=true&random=" + new Date().getTime(),"descWin","status=no,toolbar=no,location=no,menu=no,width=" + windowWidth + ",height=" + windowHeight + ",resizable=yes,left=" + xpos + ",top=" + ypos + ",screenX=" + xpos + ",screenY=" + ypos);
    descWin.focus();
    descWin.opener = self;
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
                // no response if external videoplayer is not closed by user
                // alert(resourceBundle["alert.communicationFailure"]);
            }
        }
	});
}

function deshakeVideo(fileName) {
	
    const parameters = { "cmd": "deshakeVideo", "videoFileName": encodeURIComponent(fileName) };
    
	xmlPostRequest("video", parameters, function(responseXml) {
	
	    const item = responseXml.getElementsByTagName("success")[0];            
        if (item) {
            const success = item.firstChild.nodeValue;
            if (success != "true") {
               	customAlert(resourceBundle["errorVideoDeshake"]);
            } else {
                const targetFolderItem = responseXml.getElementsByTagName("targetFolder")[0];            
                const targetFolder = targetFolderItem.firstChild.nodeValue;

                const targetPathItem = responseXml.getElementsByTagName("targetPath")[0];            
                const targetPath = targetPathItem.firstChild.nodeValue;
                        
                customAlert(resourceBundle["videoDeshakeStarted"] + " " + targetFolder + ".");
                        
                setTimeout(function() {
                  	parent.parent.frames[1].location.href = "/webfilesys/servlet?command=exp&actPath=" + encodeURIComponent(targetPath) + "&expand=" + encodeURIComponent(targetPath) + "&fastPath=true";
                }, 6000);
            }
        }
	});
}

function addSilentAudio(fileName) {
    var url = "/webfilesys/servlet?command=video&cmd=addSilentAudio&videoFileName=" + encodeURIComponent(fileName);
	
	xmlRequest(url, function(req) {
        if (req.readyState == 4) {
            if (req.status == 200) {
			    var xmlDoc = req.responseXML;

			    var item = xmlDoc.getElementsByTagName("success")[0];            
                if (item) {
                    var success = item.firstChild.nodeValue;
                    if (success != "true") {
                    	customAlert(resourceBundle["errorAddSilentAudio"]);
                    } else {
                        var targetFolderItem = req.responseXML.getElementsByTagName("targetFolder")[0];            
                        var targetFolder = targetFolderItem.firstChild.nodeValue;

                        var targetPathItem = req.responseXML.getElementsByTagName("targetPath")[0];            
                        var targetPath = targetPathItem.firstChild.nodeValue;
                        
                        customAlert(resourceBundle["addSilentAudioStarted"] + " " + targetFolder + ".");
                        
                        setTimeout(function() {
                        	parent.parent.frames[1].location.href = "/webfilesys/servlet?command=exp&actPath=" + encodeURIComponent(targetPath) + "&expand=" + encodeURIComponent(targetPath) + "&fastPath=true";
                        }, 6000);
                    }
                } else {
                	customAlert(resourceBundle["errorAddSilentAudio"]);
                }
            } 
        }
	});
}
