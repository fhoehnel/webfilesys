function hideMenu() {
    document.getElementById('contextMenu').style.visibility = 'hidden';
}

function insertDoubleBackslash(source) {
    return(source.replace(/\\/g,"\\\\"));
}
