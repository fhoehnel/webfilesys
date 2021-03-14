function addContextMenuEntry(contextMenuCont, onClick, label) {
   	const menuEntry = document.createElement("div");
   	menuEntry.setAttribute("class", "contextMenuItem");
   	menuEntry.setAttribute("onclick", onClick);
   	const textNode = document.createTextNode(label);
   	menuEntry.appendChild(textNode);
   	contextMenuCont.appendChild(menuEntry);
}

function addContextMenuHead(menuDiv, headerText) {
    const menuHead = document.createElement("div");
    menuHead.setAttribute("class", "contextMenuHead");
   	const textNode = document.createTextNode(headerText);
   	menuHead.appendChild(textNode);
   	menuDiv.appendChild(menuHead);
}

function hideMenu() {
    document.getElementById('contextMenu').style.visibility = 'hidden';
}

