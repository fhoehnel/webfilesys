function hideMenu() {
    document.getElementById('contextMenu').style.visibility = 'hidden';
}

function getFileNameExt(fileName) {
    fileExt="";

    extStart=fileName.lastIndexOf('.');

    if (extStart > 0) {
	fileExt=fileName.substring(extStart).toUpperCase();
    }
    
    return(fileExt);
}

function insertDoubleBackslash(source) {
    return(source.replace(/\\/g,"\\\\"));
}
