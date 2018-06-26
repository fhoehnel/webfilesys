function setMobileThumbContHeight() {
    var windowHeight;

    if (browserFirefox || (browserChrome && osAndroid)) {
        windowHeight = window.innerHeight;
    } else {
        windowHeight = document.documentElement.clientHeight;
    }

    var headElemHeight = 0;
    
    var currentPathTable = document.getElementById("currentPathTable");
    
    headElemHeight += currentPathTable.offsetHeight;
    
    var filterAndSortTable = document.getElementById("filterAndSortTable");

    headElemHeight += filterAndSortTable.offsetHeight;

    var folderMetaInfElem = document.getElementById("folderMetaInf");
    if (folderMetaInfElem) {
    	headElemHeight += folderMetaInfElem.offsetHeight;
    }
    
    var scrollAreaCont = document.getElementById("scrollAreaCont");
    
    scrollAreaCont.style.height = (windowHeight - headElemHeight - 5) + 'px';
}

function mobilePicturePopup(filePath, picIdx) {
    var pic = document.getElementById("pic-" + picIdx);
    if (!pic) {
    	return;
    }
  	var xsize = pic.getAttribute("origWidth");
   	var ysize = pic.getAttribute("origHeight");
    var imgSrc = "/webfilesys/servlet?command=getFile&filePath=" + encodeURIComponent(filePath);

    var popupShield = document.createElement("div");
	popupShield.id = "popupShield";
	popupShield.setAttribute("class", "popupShield");    		  
	document.getElementsByTagName("body")[0].appendChild(popupShield);    	  
	  
    var zoomImgObj = document.getElementById('zoomPic');

    zoomImgObj.style.position = "static";
    
    zoomImgObj.src = imgSrc;

    var winWidth = determineWindowWidth();
    var winHeight = determineWindowHeight();

    if (winWidth / xsize > winHeight / ysize) {
        zoomEndYSize = winHeight - 6;
        
        if (zoomEndYSize > ysize) {
            zoomEndYSize = ysize;
            
            if (zoomEndYSize < ZOOM_MIN_SIZE) {
                zoomEndYSize = ZOOM_MIN_SIZE;
            }
        }
        
        zoomEndXSize = Math.round(zoomEndYSize * (xsize / ysize));
    } else {
        zoomEndXSize = winWidth - 6;
        
        if (zoomEndXSize > xsize) {
            zoomEndXSize = xsize;

            if (zoomEndXSize < ZOOM_MIN_SIZE) {
                zoomEndXSize = ZOOM_MIN_SIZE;
            }
        }
        
        zoomEndYSize = Math.round(zoomEndXSize * (ysize / xsize));
    }

    var picture = document.getElementById('picturePopup');
    picture.src = imgSrc;

    var yScrolled;

    if (document.all) {
        yScrolled = document.body.scrollTop;
    } else {
        yScrolled = window.pageYOffset;
    }

    var picturePopup = document.getElementById('picturePopup');

    picturePopup.style.top = yScrolled + Math.round((determineWindowHeight() - 3) / 2) - Math.round(zoomEndYSize / 2) + "px"; 
    picturePopup.style.left = Math.round((determineWindowWidth() - 3) / 2) - Math.round(zoomEndXSize / 2) + "px"; 

    picturePopup.style.width = zoomEndXSize + 'px';
    picturePopup.style.height = zoomEndYSize + 'px';

    picturePopup.style.visibility = 'visible';
    
    var popupClose = document.getElementById('popupClose');
    popupClose.style.visibility = 'visible';
}

