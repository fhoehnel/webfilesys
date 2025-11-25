document.onclick = () => {
    document.getElementById('contextMenu').style.visibility = 'hidden';
};

function picContextMenu(fileName) {
       
    let shortFileName = fileName;
    
    if (fileName.length > 22) {
        shortFileName = fileName.substring(0,7) + "..." + fileName.substring(fileName.length - 12, fileName.length);
    }    

    const lastPathChar = path.charAt(path.length - 1);
    
    let fullPath = "";
    
    if ((lastPathChar === '/') || (lastPathChar === '\\')) {
        fullPath = path + fileName;
    } else {
        if (serverOS === 'ix') {
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

    addContextMenuEntry(menuDiv, "downloadFile('" + scriptPreparedPath + "')", resourceBundle["menuDownload"]);

    if (readonly !== 'true') {
        addContextMenuEntry(menuDiv, "delFile('" + scriptPreparedFile + "')", resourceBundle["menuDelete"]);
    }

    addContextMenuEntry(menuDiv, "copyToClipboard('" + scriptPreparedFile + "')", resourceBundle["menuCopy"]);

    if (readonly !== 'true') {
        addContextMenuEntry(menuDiv, "cutToClipboard('" + scriptPreparedFile + "')", resourceBundle["menuCut"]);
    }

    if (mailEnabled === 'true') {
        addContextMenuEntry(menuDiv, "sendFile('" + scriptPreparedFile + "')", resourceBundle["menuSendFile"]);
    }

    centerBox(menuDiv);

    menuDiv.style.visibility = 'visible';
}
