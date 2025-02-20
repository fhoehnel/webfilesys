function mobileThumbContextMenu(fileName) {
       
    let shortFileName = fileName;
    
    if (fileName.length > 22) {
        shortFileName = fileName.substring(0,7) + "..." + fileName.substring(fileName.length - 12, fileName.length);
    }    

    const fileNameExt = getFileNameExt(fileName);
    
    const lastPathChar = path.charAt(path.length - 1);
    
    let fullPath = "";
    
    if ((lastPathChar == '/') || (lastPathChar == '\\')) {
        fullPath = path + fileName;
    } else {
        if (serverOS == 'ix') {
            fullPath = path + '/' + fileName;
        } else {
            fullPath = path + '\\' + fileName;
        }
    }

    const scriptPreparedPath = insertDoubleBackslash(fullPath);

    const scriptPreparedFile = insertDoubleBackslash(fileName);

    const menuDiv = document.getElementById('contextMenu');    
    
    menuDiv.style.visibility = 'hidden';

    menuDiv.innerHTML = "";

    addContextMenuHead(menuDiv, shortFileName);

    const downloadLabel = (fileExt == ".MP3") ? resourceBundle["menuPlay"] : resourceBundle["menuDownload"];

	addContextMenuEntry(menuDiv, "downloadFile('" + scriptPreparedPath + "')", downloadLabel);

    if (readonly != 'true') {
    	addContextMenuEntry(menuDiv, "delFile('" + scriptPreparedFile + "')", resourceBundle["menuDelete"]);
    }
    
    positionMenuDiv(menuDiv);
    
    menuDiv.style.visibility = 'visible';
}
