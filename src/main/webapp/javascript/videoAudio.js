if (typeof String.prototype.endsWithIgnoreCase != 'function') {
    String.prototype.endsWithIgnoreCase = function( str ) {
        return this.substring(this.length - str.length, this.length).toLowerCase() === str.toLowerCase();
    }
}

function playVideo(videoFilePath) {

    var videoUrl = "/webfilesys/servlet?command=getFile&filePath=" + encodeURIComponent(videoFilePath);

    var videoType = "mp4";
    
    if (videoFilePath.endsWithIgnoreCase(".ogg") || videoFilePath.endsWithIgnoreCase(".ogv")) {
        videoType = "ogg"
    } else if (videoFilePath.endsWithIgnoreCase(".webm")) {
        videoType = "webm"
    }
    
    var videoCont = document.createElement("div");
    videoCont.id = "videoCont";
    videoCont.setAttribute("class", "videoCont");
    
    var closeButton = document.createElement("img");
    closeButton.setAttribute("src", "/webfilesys/images/winClose.gif");
    closeButton.setAttribute("class", "closeButton");
    closeButton.setAttribute("onclick", "destroyVideo()");
    videoCont.appendChild(closeButton);
    
    var videoElem = document.createElement("video");
    videoElem.setAttribute("autobuffer", "autobuffer");
    videoElem.setAttribute("autoplay", "autoplay");
    videoElem.setAttribute("controls", "controls");
    videoElem.setAttribute("src", videoUrl);
    videoElem.setAttribute("type", videoType);

    var altTextElem = document.createElement("p");
    altTextElem.innerHTML = "This browser does not support HTML5 video!"
    videoElem.appendChild(altTextElem);
    
    videoCont.appendChild(videoElem);    

    var docRoot = document.documentElement;
    docRoot.appendChild(videoCont);
    
    centerBox(videoCont);    
}

function destroyVideo() {
    var videoCont = document.getElementById("videoCont");
    document.documentElement.removeChild(videoCont);
}

function loadVideoThumbs() {

	console.log("loadVideoThumbs thumbnails.length=" + thumbnails.length);
	
    var scrollAreaCont = document.getElementById("scrollAreaCont");

    var counter = 0;

	for (var i = 0; (counter < 10) && (i < thumbnails.length); i++) {
	    var pic = document.getElementById("pic-" + thumbnails[i]);
	    if (pic) {
			var imgPath = pic.getAttribute("imgPath");
			if (imgPath) {
	        	if (isScrolledIntoView(pic, scrollAreaCont)) {
		  		    thumbnails.splice(i, 1);
		    		
		   		    loadVideoThumbnail(pic, imgPath);
	    
	                // setPictureDimensions(pic);
	                
	                counter++;
	        	}
	        }
	    }
	}
}

function loadVideoThumbnail(pic, thumbFileSrc) {

	pic.onload = function() {
		
		var picOrigWidth = pic.naturalWidth;
		var picOrigHeight = pic.naturalHeight;
		
		if ((picOrigWidth == 0) || (picOrigHeight == 0)) {
            // workaround for MSIE
            var origWidthAttrib = pic.getAttribute("origWidth");
            var origHeightAttrib = pic.getAttribute("origHeight");
            
            if (origWidthAttrib && origHeightAttrib) {
		        picOrigWidth = parseInt(origWidthAttrib);
		        picOrigHeight = parseInt(origHeightAttrib);
            }
		}
		
		if (picOrigWidth > picOrigHeight) {
			pic.width = 160;
			pic.height = picOrigHeight * 160 / picOrigWidth;
		} else {
			pic.height = 160;
			pic.width = picOrigWidth * 160 / picOrigHeight;
		}

		pic.style.visibility = "visible";
		
        pic.removeAttribute("imgPath");
        
        loadedThumbs.push(pic);

        checkVideoThumbnailsToLoad();
	};

	pic.src = thumbFileSrc;
}

function checkVideoThumbnailsToLoad() {

	if (thumbnails.length == 0) {
		return;
	}
	
	thumbLoadRunning = true;

    var scrollAreaCont = document.getElementById("scrollAreaCont");
	
	for (var i = 0; i < thumbnails.length; i++) {
		var pic = document.getElementById("pic-" + thumbnails[i]);
	    if (pic) {
			var imgPath = pic.getAttribute("imgPath");
			if (imgPath) {
	        	if (isScrolledIntoView(pic, scrollAreaCont)) {
	        		thumbnails.splice(i, 1);
		    		
	        		loadVideoThumbnail(pic, imgPath);
	    
	                thumbLoadRunning = false;
	                
	                return;
	    		}
	    	}
	    }
	}

    thumbLoadRunning = false;
    
    // releaseInvisibleThumbnails();
}

