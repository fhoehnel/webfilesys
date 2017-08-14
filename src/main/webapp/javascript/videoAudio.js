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

    checkVideoThumbnailsToLoad();	
}

function loadVideoThumbnail(pic, thumbFileSrc) {

	pic.onload = function() {
		
		var picOrigWidth = pic.naturalWidth;
		var picOrigHeight = pic.naturalHeight;
		
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
	        		
	                setVideoDimensions(pic);
	    
	                thumbLoadRunning = false;
	                
	                return;
	    		}
	    	}
	    }
	}

    thumbLoadRunning = false;
    
    // releaseInvisibleThumbnails();
}

function setVideoDimensions(pic) { 

    if (pic.getAttribute("origWidth")) {
        return;
    }

    var picId = pic.id;

    var pixDim = document.getElementById("pixDim-" + picId.substring(4));
    if (!pixDim) {
        return;
    }

    var picFileName = pixDim.getAttribute("picFileName");

    var url = "/webfilesys/servlet?command=getVideoDimensions&fileName=" +  encodeURIComponent(picFileName);

    var picIsLink = pixDim.getAttribute("picIsLink");
    if (picIsLink) {
    	url = url + "&link=true";
    }
    
	xmlRequest(url, function(req) {
        if (req.readyState == 4) {
            if (req.status == 200) {
			    var xmlDoc = req.responseXML;
			    
			    var videoWidth = null;
			    var videoHeight = null;
                var videoType = null;
			    
                var item = xmlDoc.getElementsByTagName("xpix")[0];            
                if (item) {
                    videoWidth = item.firstChild.nodeValue;
                }
             
                item = xmlDoc.getElementsByTagName("ypix")[0];            
                if (item) {
                    videoHeight = item.firstChild.nodeValue;
                }
			    
                item = xmlDoc.getElementsByTagName("videoType")[0];            
                if (item) {
                	videoType = item.firstChild.nodeValue;
                }
			    
			    if ((videoWidth != null) && (videoHeight != null)) {
			        pixDim.innerHTML = videoWidth + " x " + videoHeight + " pix";
			        
			        var pic = document.getElementById(picId);
			        if (pic) {
			        	pic.setAttribute("origWidth", videoWidth);
			        	pic.setAttribute("origHeight", videoHeight);
			        	if (videoType) {
			        		pic.setAttribute("videoType", imageType);
			        	}
			        } 
			    }
            } else {
                alert(resourceBundle["alert.communicationFailure"]);
            }
        }
    });
}

function attachVideoScrollHandler() {
    var scrollAreaCont = document.getElementById("scrollAreaCont");

    scrollAreaCont.onscroll = function() {
	  	 var scrollPosDiff = scrollAreaCont.scrollTop - lastScrollPos;

		 if ((scrollPosDiff > 20) || (scrollPosDiff < (-20))) {
			 lastScrollPos = scrollAreaCont.scrollTop;
			 
			 if (!thumbLoadRunning) {
				 checkVideoThumbnailsToLoad();
			 }
	  	 }
	};
}
