function mobileMainMenu() {
    var menuDiv = document.getElementById('contextMenu');    
    
    menuText = '<table style="width:100%">'

    menuText = menuText 
             + menuEntry("javascript:mobileBookmarks()", resourceBundle["label.bookmarksMobile"]);

    menuText = menuText 
             + menuEntry("javascript:mobileAbout()", resourceBundle["label.about"]);

    menuText = menuText 
             + menuEntry("javascript:switchToClassicVersion()", resourceBundle["classicView"]);

    menuText = menuText 
             + menuEntry("javascript:mobileLogout()", resourceBundle["label.logout"]);
    
    menuText = menuText + '</table>'; 

    menuDiv.innerHTML = menuText;
    
    positionMenuDiv(menuDiv);
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