function jsContextMenu(fileName, imgType, domId) {
    menuDiv = document.getElementById('contextMenu');    
    
    menuDiv.style.visibility = 'hidden';

    menuDiv.innerHTML = "";
        
    shortFileName = fileName;
    
    if (fileName.length > 23) {
        shortFileName = fileName.substring(0,7) + "..." + fileName.substring(fileName.length - 13, fileName.length);
    }    

    fileNameExt = getFileNameExt(fileName);
    
    lastPathChar = path.charAt(path.length - 1);
    
    if ((lastPathChar == '/') || (lastPathChar == '\\')) {
        fullPath = path + fileName;
    } else {
        if (parent.serverOS == 'ix') {
            fullPath = path + '/' + fileName;
        } else {
            fullPath = path + '\\' + fileName;
        }
    }

    scriptPreparedPath = insertDoubleBackslash(fullPath);

    scriptPreparedFile = insertDoubleBackslash(fileName);
        
    addContextMenuHead(menuDiv, shortFileName);
    
    if (parent.readonly != 'true') {
    	
    	addContextMenuEntry(menuDiv, "delImg('" + scriptPreparedFile + "')", resourceBundle["label.delete"]);

    	addContextMenuEntry(menuDiv, "jsRenameImg('" + scriptPreparedFile + "', '" + domId + "')", resourceBundle["label.renameFile"]);

    	addContextMenuEntry(menuDiv, "copyToClipboard('" + scriptPreparedFile + "')", resourceBundle["label.copyToClip"]);
    	
    	if (addCopyAllowed) {
        	addContextMenuEntry(menuDiv, "addCopyToClipboard('" + scriptPreparedFile + "')", resourceBundle["label.copyToClip"] + " +");
        }
        
    	addContextMenuEntry(menuDiv, "cutToClipboard('" + scriptPreparedFile + "')", resourceBundle["label.cutToClip"]);

        if (addMoveAllowed) {
        	addContextMenuEntry(menuDiv, "addMoveToClipboard('" + scriptPreparedFile + "')", resourceBundle["label.cutToClip"] + " +");
        }
        
    	addContextMenuEntry(menuDiv, "jsEditDesc('" + scriptPreparedPath + "')", resourceBundle["label.editMetaInfo"]);
        
        if (imgType != '5') { 
        	// not SVG
        	addContextMenuEntry(menuDiv, "jsResizeParms('" + scriptPreparedPath + "')", resourceBundle["label.editPicture"]);
        }
    }

    if (imgType == '1') {  // JPEG 
    	addContextMenuEntry(menuDiv, "jsExifData('" + scriptPreparedPath + "')", resourceBundle["alt.cameradata"]);
    }

    if (parent.readonly != 'true') {
        if ((imgType == '1') ||   // JPEG
            (imgType == '2') ||   // GIF
            (imgType == '3')) {   // PNG
        	addContextMenuEntry(menuDiv, "rotateFlipMenu('" + shortFileName + "', '" + scriptPreparedPath + "', '" + scriptPreparedFile + "', '" + imgType + "', '" + domId + "')", resourceBundle["label.rotateFlip"]);
        }
    }

	addContextMenuEntry(menuDiv, "jsComments('" + scriptPreparedPath + "')", resourceBundle["label.comments"]);

    if (parent.readonly != 'true') { 

    	addContextMenuEntry(menuDiv, "categories('" + scriptPreparedPath + "')", resourceBundle["label.assignCategories"]);

        if (parent.mailEnabled == 'true') {
        	addContextMenuEntry(menuDiv, "jsSendFile('" + scriptPreparedFile + "')", resourceBundle["label.sendfile"]);
        }
        
    	addContextMenuEntry(menuDiv, "publishFile('" + scriptPreparedPath + "')", resourceBundle["label.publish"]);
    }

    if (imgType != '5') { 
    	// not SVG
    	addContextMenuEntry(menuDiv, "startSlideshowHere('" + scriptPreparedPath + "','" + scriptPreparedFile + "')", resourceBundle["startSlideshowHere"]);
    }
    
    if (parent.readonly == 'true') {
        maxMenuHeight = 120;
    } else {
        maxMenuHeight = 380;
    }
    
    positionMenuDiv(menuDiv, maxMenuHeight);
    menuDiv.style.visibility = 'visible';
}

function delImg(fileName) {
    centeredDialog('/webfilesys/servlet?command=ajaxRPC&method=deleteFilePrompt&param1=' + encodeURIComponent(fileName), 
                   '/webfilesys/xsl/confirmDeleteFile.xsl', 
                   320, 130);
}

function deleteFile(fileName)
{
    window.location.href = "/webfilesys/servlet?command=fmdelete&fileName=" + fileName + "&deleteRO=yes";
}

function jsRenameImg(fileName, domId) {
    centeredDialog('/webfilesys/servlet?command=renameImagePrompt&imageFile=' + encodeURIComponent(fileName), '/webfilesys/xsl/renameImage.xsl', 360, 160, function() {
    	document.renameForm.domId.value = domId;
    	document.renameForm.newFileName.focus();
        document.renameForm.newFileName.select();
    });
}

function rotateFreeAngle(path) {
    centeredDialog('/webfilesys/servlet?command=rotateImagePrompt&imagePath=' + encodeURIComponent(path), '/webfilesys/xsl/rotateImage.xsl', 360, 170, function() {
        document.getElementById("rotationDegrees").focus();
    });
}

function jsEditDesc(path) {
    const windowWidth = 700;
    const windowHeight = 520;
    
    const xpos = Math.round((screen.availWidth - windowWidth) / 2);
    const ypos = Math.round((screen.availHeight - windowHeight) / 2);

    const descWin = window.open("/webfilesys/servlet?command=editMetaInf&path=" + encodeURIComponent(path) + "&geoTag=true","descWin","status=no,toolbar=no,location=no,menu=no,width=" + windowWidth + ",height=" + windowHeight + ",resizable=yes,left=" + xpos + ",top=" + ypos + ",screenX=" + xpos + ",screenY=" + ypos);
    descWin.focus();
    descWin.opener = self;
}

function categories(path) {
    catWin = window.open("/webfilesys/servlet?command=assignCategory&filePath=" + encodeURIComponent(path) + "&random=" + new Date().getTime(), "catWin", "status=no,toolbar=no,location=no,menu=no,scrollbars=yes,width=520,height=400,resizable=yes,left=100,top=30,screenX=100,screenY=30");
    catWin.focus();
}

function jsResizeParms(path)
{
    window.location.href = '/webfilesys/servlet?command=resizeParms&imgFile=' + encodeURIComponent(path);
}

function jsExifData(path)
{
    exifWin = window.open('/webfilesys/servlet?command=exifData&imgFile=' + encodeURIComponent(path),'exifWin','scrollbars=yes,status=no,toolbar=no,location=no,menu=no,width=500,height=560,left=100,top=20,screenX=100,screenY=20,resizable=no');
    exifWin.focus();
}

function jsRotate(path, degrees, fileName, domId) {
    const parameters = { "imgPath": encodeURIComponent(path) };
    
	xmlGetRequest("checkLossless", parameters, function(responseXml) {
        const losslessItem = responseXml.getElementsByTagName("lossless")[0];            
        const lossless = losslessItem.firstChild.nodeValue;
                 
        if (lossless === "true") {
            ajaxRotate(fileName, degrees, domId);
        } else {
            window.location.href = '/webfilesys/servlet?command=transformImage&action=rotate&degrees=' + degrees + '&imgName=' + encodeURIComponent(path);
        }
    });    
}

function jsFlip(path, direction)
{
    window.location.href = '/webfilesys/servlet?command=transformImage&action=flip' + direction + '&imgName=' + encodeURIComponent(path);
}

function jsComments(path)
{
    commentWin=window.open("/webfilesys/servlet?command=listComments&actPath=" + encodeURIComponent(path),"commentWin","status=no,toolbar=no,location=no,menu=no,scrollbars=yes,width=550,height=400,resizable=yes,left=80,top=100,screenX=80,screenY=100");
    commentWin.focus();
}

function jsSendFile(fileName) {
    centeredDialog('/webfilesys/servlet?command=emailFilePrompt&fileName=' + encodeURIComponent(fileName), '/webfilesys/xsl/emailFile.xsl', 400, 250, function() {
        document.emailForm.receiver.focus();
        document.emailForm.receiver.select();
    });
}

function copyToClipboard(fileName)
{
    cutCopyToClip(fileName, 'copy');
}

function cutToClipboard(fileName)
{
    cutCopyToClip(fileName, 'move');
}

function addCopyToClipboard(fileName)
{
    cutCopyToClip(fileName, 'addCopy');
}

function addMoveToClipboard(fileName)
{
    cutCopyToClip(fileName, 'addMove');
}

function publishFile(path)
{

    if (parent.mailEnabled == 'true')
    {
        windowHeight = 560;
        ypos = 20;
    }
    else
    {
        windowHeight = 280;
        ypos = 100;
    }

    publishWin = window.open("/webfilesys/servlet?command=publishFile&publishPath=" + encodeURIComponent(path),"publish","status=no,toolbar=no,menu=no,width=550,height=" + windowHeight + ",resizable=no,scrollbars=no,left=80,top=" + ypos + ",screenX=80,screenY=" + ypos);
}

function rotateFlipMenu(shortPath, path, fileName, imgType, domId) {
    stopMenuClose = true;

    var scriptPreparedPath = insertDoubleBackslash(path);

    var menuDiv = document.getElementById('contextMenu');    

    menuDiv.style.visibility = 'hidden';

    menuDiv.innerHTML = "";

    addContextMenuHead(menuDiv, shortPath);
    
	addContextMenuEntry(menuDiv, "jsRotate('" + scriptPreparedPath + "','90','" + fileName + "','" + domId + "')", resourceBundle["label.rotateright"]);

	addContextMenuEntry(menuDiv, "jsRotate('" + scriptPreparedPath + "','270','" + fileName + "','" + domId + "')", resourceBundle["label.rotateleft"]);

	addContextMenuEntry(menuDiv, "jsRotate('" + scriptPreparedPath + "','180','" + fileName + "','" + domId + "')", resourceBundle["label.rotate180"]);
               
    if (imgType == '1') {
    	addContextMenuEntry(menuDiv, "jsFlip('" + scriptPreparedPath + "','Horizontal')", resourceBundle["label.mirrorhoriz"]);

    	addContextMenuEntry(menuDiv, "jsFlip('" + scriptPreparedPath + "','Vertical')", resourceBundle["label.mirrorvert"]);
    }


    if ((imgType == '1') || (imgType == '3')) { // JPEG or PNG
    	addContextMenuEntry(menuDiv, "rotateFreeAngle('" + scriptPreparedPath + "')", resourceBundle["label.rotateFreeAngle"]);
    }
    
    menuDiv.style.visibility = 'visible';
}

function startSlideshowHere(startPath, startFileName) 
{
    window.location.href = '/webfilesys/servlet?command=slideShowParms&cmd=getParms&startPath=' + encodeURIComponent(startPath) + '&startFile=' + encodeURIComponent(startFileName) + '&screenWidth=' + screen.width + '&amp;screenHeight=' + screen.height;
}

function validateRotationDegrees() {
    var degrees = document.getElementById("rotationDegrees").value;
    
    try {
        var numericDegrees = parseInt(degrees);
        if (isNaN(numericDegrees)) {
            alert(resourceBundle["invalidRotationDegrees"]);
        } else {
            if ((numericDegrees < (-179)) || (numericDegrees > 179)) {
                alert(resourceBundle["invalidRotationDegrees"]);
                return;
            }
        
            if (numericDegrees < 0) {
                document.getElementById("rotationDegrees").value = 360 + numericDegrees;
            }
        
            showHourGlass();

            document.getElementById("prompt").style.visibility = "hidden";
        
            document.getElementById("rotateForm").submit();
        }
        
    } catch (err) {
        alert(resourceBundle["invalidRotationDegrees"]);
    }
}