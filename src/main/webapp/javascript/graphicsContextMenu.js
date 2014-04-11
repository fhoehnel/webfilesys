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

function jsContextMenu(fileName, imgType, domId)
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
        
    menuText = '<table class="contextMenu">'
             + '<tr>'
             + '<th>'
             + shortFileName
             + '</th>'
             + '</tr>';

    if (parent.readonly != 'true')
    {
        menuText = menuText 
                 + menuEntry("javascript:delImg('" + scriptPreparedFile + "')",resourceBundle["label.delete"],null);

        menuText = menuText 
                 + menuEntry("javascript:jsRenameImg('" + scriptPreparedPath + "')",resourceBundle["label.renameFile"],null);

        menuText = menuText 
                 + menuEntry("javascript:copyToClipboard('" + scriptPreparedFile + "')",resourceBundle["label.copyToClip"],null);

        menuText = menuText 
                 + menuEntry("javascript:cutToClipboard('" + scriptPreparedFile + "')",resourceBundle["label.cutToClip"],null);

        menuText = menuText 
                 + menuEntry("javascript:jsEditDesc('" + scriptPreparedPath + "')",resourceBundle["label.editMetaInfo"],null);

        menuText = menuText 
                 + menuEntry("javascript:jsResizeParms('" + scriptPreparedPath + "')",resourceBundle["label.editPicture"],null);

    }

    if (imgType == '1') // JPEG
    {
        menuText = menuText 
                 + menuEntry("javascript:jsExifData('" + scriptPreparedPath + "')",resourceBundle["alt.cameradata"],null);
    }

    if (parent.readonly != 'true')
    {
        if ((imgType == '1') ||   // JPEG
            (imgType == '2') ||   // GIF
            (imgType == '3'))    // PNG
        {
            // TODO: resource for rotate/flip
        
            menuText = menuText 
                     + menuEntry("javascript:rotateFlipMenu('" + shortFileName + "', '" + scriptPreparedPath + "', '" + domId + "', '" + imgType + "')",resourceBundle["label.rotateFlip"] + ' >',null);
        }

        /*
        if ((imgType == '1') ||   // JPEG
            (imgType == '3'))     // PNG
        {
            menuText = menuText 
                     + menuEntry("javascript:jsMakeThumb('" + scriptPreparedPath + "')",resourceBundle["label.makethumb"],null);
        }
        */
    }

    menuText = menuText 
             + menuEntry("javascript:jsComments('" + scriptPreparedPath + "')",resourceBundle["label.comments"],null);

    if (parent.readonly != 'true')
    { 
        menuText = menuText 
                 + menuEntry("javascript:categories('" + scriptPreparedPath + "')",resourceBundle["label.assignCategories"],null);

        if (parent.mailEnabled == 'true')
        {
            menuText = menuText 
                     + menuEntry("javascript:jsSendFile('" + scriptPreparedFile + "')",resourceBundle["label.sendfile"],null);
        }
        
        menuText = menuText 
                 + menuEntry("javascript:publishFile('" + scriptPreparedPath + "')",resourceBundle["label.publish"],null);
        
    }

    menuText = menuText 
             + menuEntry("javascript:startSlideshowHere('" + scriptPreparedPath + "','" + scriptPreparedFile + "')",resourceBundle["startSlideshowHere"],null);
        
    menuText = menuText + '</table>'; 

    menuDiv.innerHTML = menuText;

    menuDiv.style.bgcolor = '#c0c0c0';
    
    if (parent.readonly == 'true')
    {
        maxMenuHeight = 120;
    }
    else
    {
        maxMenuHeight = 380;
    }
    
    positionMenuDiv(menuDiv, maxMenuHeight);
}

function jsDeleteImg(path)
{
    if (confirm(path + '\n' + resourceBundle["confirm.delfile"]))
    {
        url='/webfilesys/servlet?command=delImageFromThumb&imgName=' + encodeURIComponent(path);

        window.location.href=url;
    }
}

function delImg(fileName)
{
    showPrompt('/webfilesys/servlet?command=ajaxRPC&method=deleteFilePrompt&param1=' + encodeURIComponent(fileName), '/webfilesys/xsl/confirmDeleteFile.xsl', 320, 130);
}

function deleteFile(fileName)
{
    window.location.href = "/webfilesys/servlet?command=fmdelete&fileName=" + fileName + "&deleteRO=yes";
}

function jsRenameImg(path)
{
    showPrompt('/webfilesys/servlet?command=renameImagePrompt&imagePath=' + encodeURIComponent(path), '/webfilesys/xsl/renameImage.xsl', 360);

    document.renameForm.newFileName.focus();
    
    document.renameForm.newFileName.select();
}

function jsEditDesc(path)
{
    var windowWidth = 600;
    var windowHeight = 450;
    
    var xpos = (screen.width - windowWidth) / 2;
    var ypos = (screen.height - windowHeight) / 2;

    descWin=window.open("/webfilesys/servlet?command=editMetaInf&path=" + encodeURIComponent(path) + "&geoTag=true&random=" + new Date().getTime(),"descWin","status=no,toolbar=no,location=no,menu=no,width=" + windowWidth + ",height=" + windowHeight + ",resizable=yes,left=" + xpos + ",top=" + ypos + ",screenX=" + xpos + ",screenY=" + ypos);
    descWin.focus();
    descWin.opener=self;
}

function categories(path)
{
    catWin=window.open("/webfilesys/servlet?command=assignCategory&filePath=" + encodeURIComponent(path) + "&random=" + new Date().getTime(),"catWin","status=no,toolbar=no,location=no,menu=no,scrollbars=yes,width=480,height=400,resizable=yes,left=100,top=30,screenX=100,screenY=30");
    catWin.focus();
}

function jsResizeParms(path)
{
    window.location.href = '/webfilesys/servlet?command=resizeParms&imgFile=' + encodeURIComponent(path);
}

function jsExifData(path)
{
    exifWin = window.open('/webfilesys/servlet?command=exifData&imgFile=' + encodeURIComponent(path),'exifWin','scrollbars=yes,status=no,toolbar=no,location=no,menu=no,width=400,height=540,left=200,top=20,screenX=200,screenY=20,resizable=no');
    exifWin.focus();
}

function jsRotate(path, degrees, domId)
{
    if (checkLossless(path))
    {
        ajaxRotate(path, degrees, domId);
    }
    else
    {
        window.location.href = '/webfilesys/servlet?command=transformImage&action=rotate&degrees=' + degrees + '&imgName=' + encodeURIComponent(path);
    }
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

function jsSendFile(fileName)
{
    showPrompt('/webfilesys/servlet?command=emailFilePrompt&fileName=' + encodeURIComponent(fileName), '/webfilesys/xsl/emailFile.xsl', 400, 250);
    
	setBundleResources();
    
    document.emailForm.receiver.focus();
    
    document.emailForm.receiver.select();
}

function copyToClipboard(fileName)
{
    cutCopyToClip(fileName, 'copy');
}

function cutToClipboard(fileName)
{
    cutCopyToClip(fileName, 'cut');
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

function rotateFlipMenu(shortPath, path, domId, imgType)
{
    stopMenuClose = true;

    var scriptPreparedPath = insertDoubleBackslash(path);

    var menuDiv = document.getElementById('contextMenu');    

    menuDiv.style.visibility = 'hidden';

    var menuText = '<table class="contextMenu">'
                 + '<tr>'
                 + '<th>'
                 + shortPath
                 + '</th>'
                 + '</tr>';

    menuText = menuText 
               + menuEntry("javascript:jsRotate('" + scriptPreparedPath + "','90','" + domId + "')",resourceBundle["label.rotateright"],null);

    menuText = menuText 
               + menuEntry("javascript:jsRotate('" + scriptPreparedPath + "','270','" + domId + "')",resourceBundle["label.rotateleft"],null);

    menuText = menuText 
               + menuEntry("javascript:jsRotate('" + scriptPreparedPath + "','180','" + domId + "')",resourceBundle["label.rotate180"],null);
               
    if ((imgType == '1')  &&        // JPEG
        ((parent.serverOS == "win") || (jpegtranAvail == 'true')))
    {
        menuText = menuText 
                   + menuEntry("javascript:jsFlip('" + scriptPreparedPath + "','Horizontal')",resourceBundle["label.mirrorhoriz"],null);

        menuText = menuText 
                   + menuEntry("javascript:jsFlip('" + scriptPreparedPath + "','Vertical')",resourceBundle["label.mirrorvert"],null);
    }

    menuText = menuText + '</table>'; 

    menuDiv.innerHTML = menuText;
    
    menuDiv.style.visibility = 'visible';
}

function startSlideshowHere(startPath, startFileName) 
{
    window.location.href = '/webfilesys/servlet?command=slideShowParms&cmd=getParms&startPath=' + encodeURIComponent(startPath) + '&startFile=' + encodeURIComponent(startFileName) + '&screenWidth=' + screen.width + '&amp;screenHeight=' + screen.height;
}
