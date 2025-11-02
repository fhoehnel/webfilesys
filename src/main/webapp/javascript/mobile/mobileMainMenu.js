function mobileMainMenu() {
    
    const menuDiv = document.getElementById('contextMenu');    
    
    menuDiv.style.visibility = 'hidden';

    menuDiv.innerHTML = "";
    
	addContextMenuEntry(menuDiv, "mobileBookmarks()", resourceBundle["label.bookmarksMobile"]);

	addContextMenuEntry(menuDiv, "mobileAbout()", resourceBundle["label.about"]);

	addContextMenuEntry(menuDiv, "switchToClassicVersion()", resourceBundle["classicView"]);

	addContextMenuEntry(menuDiv, "mobileLogout()", resourceBundle["label.logout"]);
    
    positionMenuDiv(menuDiv);

    menuDiv.style.visibility = 'visible';
}

function mobileBookmarks() {
	window.location.href = "/webfilesys/servlet?command=bookmarks";	
}

function mobileAbout() {
    window.open('/webfilesys/servlet?command=versionInfo', 'infowindow', 'status=no,toolbar=no,location=no,menu=no,width=300,height=230,resizable=no,left=50,top=20,screenX=50,screenY=20');
}

function switchToClassicVersion() {
    window.location.href = "/webfilesys/servlet";
}

function mobileLogout() {
    window.location.href = "/webfilesys/servlet?command=logout";
}