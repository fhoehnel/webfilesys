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
                var codec = null;
                var duration = null;
                var fps = null;
			    
                var item = xmlDoc.getElementsByTagName("xpix")[0];            
                if (item) {
                    videoWidth = item.firstChild.nodeValue;
                }
             
                item = xmlDoc.getElementsByTagName("ypix")[0];            
                if (item) {
                    videoHeight = item.firstChild.nodeValue;
                }
			    
                item = xmlDoc.getElementsByTagName("codec")[0];            
                if (item) {
                	codec = item.firstChild.nodeValue;
                }
			    
                item = xmlDoc.getElementsByTagName("duration")[0];            
                if (item) {
                	duration = item.firstChild.nodeValue;
                }
			    
                item = xmlDoc.getElementsByTagName("fps")[0];            
                if (item) {
                	fps = item.firstChild.nodeValue;
                }

			    if ((videoWidth != null) && (videoHeight != null)) {
			        pixDim.innerHTML = videoWidth + " x " + videoHeight + " pix";
			        
			        var pic = document.getElementById(picId);
			        if (pic) {
			        	pic.setAttribute("origWidth", videoWidth);
			        	pic.setAttribute("origHeight", videoHeight);
			        	if (codec) {
                            var codecCont = document.getElementById("codec-" + picId.substring(4));
                            if (codecCont) {
                                codecCont.innerHTML = codec;
                            }
			        		// pic.setAttribute("codec", codec);
			        	}
			        	if (duration) {
                            var durationCont = document.getElementById("duration-" + picId.substring(4));
                            if (durationCont) {
                                durationCont.innerHTML = duration;
                            }
			        		// pic.setAttribute("duration", duration);
			        	}
			        	if (fps) {
                            var fpsCont = document.getElementById("fps-" + picId.substring(4));
                            if (fpsCont) {
                                fpsCont.innerHTML = fps + " fps";
                            }
			        		// pic.setAttribute("fps", fps);
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

function multiVideoFunction() {
    var idx = document.form2.cmd.selectedIndex;

    var cmd = document.form2.cmd.options[idx].value;

    if ((cmd == 'copy') || (cmd == 'move') ) {
        multiVideoCopyMove();
    } else if (cmd == 'delete') {
        multiVideoDelete();
    }
     
    document.form2.cmd.selectedIndex = 0;
}

function multiVideoCopyMove() {
    if (anySelected()) {
        document.form2.command.value = 'multiImageCopyMove';
        xmlRequestPost("/webfilesys/servlet", getFormData(document.form2), showCopyResult);
	    document.form2.command.value = '';
        resetSelected();
    } else {   
        customAlert(resourceBundle["alert.nofileselected"] + "!");
    }
}

function multiVideoDelete() {
    if (anySelected()) {
    	customConfirm(resourceBundle["confirm.deleteFiles"], resourceBundle["button.cancel"], resourceBundle["button.ok"], 
    			function() {
	                document.form2.command.value = 'multiVideoDelete';
                    document.form2.submit();
    	        },
    			function() {
    	            document.form2.command.value = '';
    	            document.form2.cmd.selectedIndex = 0;
    	            resetSelected();
    	            closeAlert();
    	        }
    	);
    } else {   
        customAlert(resourceBundle["alert.nofileselected"] + "!");
    }
}

function playVideoMaxSize(videoFilePath, videoFileName, isLink) { 

	var fileNameExt = getFileNameExt(videoFileName);
	
    if ((fileNameExt != ".MP4") && (fileNameExt != ".OGG") && (fileNameExt != ".OGV") && (fileNameExt != ".WEBM")) {
    	
    	// no HTML 5 video - cannot be played in browser
    	playVideoLocal(videoFilePath);
    	return;
    }
	
    var url = "/webfilesys/servlet?command=getVideoDimensions&fileName=" +  encodeURIComponent(videoFileName);

    if (isLink) {
    	url = url + "&link=true";
    }
    
	xmlRequest(url, function(req) {
        if (req.readyState == 4) {
            if (req.status == 200) {
			    var xmlDoc = req.responseXML;
			    
			    var videoWidth = 480;
			    var videoHeight = 360;
			    var codec = null;
			    
                item = xmlDoc.getElementsByTagName("codec")[0];            
                if (item) {
                	codec = item.firstChild.nodeValue;
                	if (codec == "mpeg4") {
                    	// no HTML 5 video - cannot be played in browser
                    	playVideoLocal(videoFilePath);
                    	return;
                	}
                }
			    
                var item = xmlDoc.getElementsByTagName("xpix")[0];            
                if (item) {
                    videoWidth = parseInt(item.firstChild.nodeValue);
                }
             
                item = xmlDoc.getElementsByTagName("ypix")[0];            
                if (item) {
                    videoHeight = parseInt(item.firstChild.nodeValue);
                }

                var availWidth = getWinWidth() - 20;
                var availHeight = getWinHeight() - 20;
                
                var maxVideoWidth = availWidth - 40;
                var maxVideoHeight = availHeight - 60;
                
                var videoPresentationWidth;

                var widthScale = videoWidth / maxVideoWidth;
                var heightScale = videoHeight / maxVideoHeight;
                
                var scaledWidth = videoWidth;
                var scaledHeight = videoHeight;
                
                if ((widthScale > 1) || (heightScale > 1)) {
                    var scale;
                	if (widthScale > heightScale) {
                		scale = widthScale;
                	} else {
                		scale = heightScale;
                	}
                	
                	scaledWidth = videoWidth * (1 / scale);
                	scaledHeight = videoHeight * (1 / scale);
                }
                
                var videoType = "mp4";
                
                if (videoFileName.endsWithIgnoreCase(".ogg") || videoFileName.endsWithIgnoreCase(".ogv")) {
                    videoType = "ogg"
                } else if (videoFileName.endsWithIgnoreCase(".webm")) {
                    videoType = "webm"
                }
                
                var videoCont = document.createElement("div");
                videoCont.id = "videoCont";
                videoCont.setAttribute("class", "maxVideoCont");
                videoCont.style.width = (scaledWidth + 20) + "px";
                videoCont.style.height = (scaledHeight + 40) + "px";
                
                var closeButton = document.createElement("img");
                closeButton.setAttribute("src", "/webfilesys/images/winClose.gif");
                closeButton.setAttribute("class", "closeButton");
                closeButton.setAttribute("onclick", "destroyVideo()");
                videoCont.appendChild(closeButton);
                
                var videoUrl = "/webfilesys/servlet?command=getFile&filePath=" + encodeURIComponent(videoFilePath);
                
                var videoElem = document.createElement("video");
                videoElem.setAttribute("autobuffer", "autobuffer");
                videoElem.setAttribute("autoplay", "autoplay");
                videoElem.setAttribute("controls", "controls");
                videoElem.setAttribute("src", videoUrl);
                videoElem.setAttribute("type", videoType);
                videoElem.style.width = scaledWidth + "px";
                videoElem.style.height = scaledHeight + "px";

                var altTextElem = document.createElement("p");
                altTextElem.innerHTML = "This browser does not support HTML5 video!"
                videoElem.appendChild(altTextElem);
                
                videoCont.appendChild(videoElem);    

                var docRoot = document.documentElement;
                docRoot.appendChild(videoCont);
                
                centerBox(videoCont);    
            } else {
                alert(resourceBundle["alert.communicationFailure"]);
            }
        }
    });
}

function validateConvertVideoForm() {
    var startHour = getSelectboxValueInt("startHour");
    var startMin = getSelectboxValueInt("startMin");
    var startSec = getSelectboxValueInt("startSec");
    
    var endHour = getSelectboxValueInt("endHour");
    var endMin = getSelectboxValueInt("endMin");
    var endSec = getSelectboxValueInt("endSec");

    var startTime = (startHour * 3600) + (startMin * 60) + startSec;
    var endTime = (endHour * 3600) + (endMin * 60) + endSec;

    if (startTime >= endTime) {
        customAlert(resourceBundle["validationError.videoStartEndTime"]);
        return false;
    }
    
    if (endTime > durationSeconds) {
        customAlert(resourceBundle["validationError.videoTimeRange"]);
        return false;
    }

    return true;
}

function getSelectboxValueInt(selectboxId) {
    var selBox = document.getElementById(selectboxId);
    var val = selBox.options[selBox.selectedIndex].value;
    return parseInt(val);
}

function sendEditConvertForm() {

    if (!validateConvertVideoForm()) {
        return;
    }

    xmlRequestPost("/webfilesys/servlet", getFormData(document.form1), function(req) {
        if (req.readyState == 4) {
            if (req.status == 200) {
                var successItem = req.responseXML.getElementsByTagName("success")[0];            
                var success = successItem.firstChild.nodeValue;
                
                if (success == "true") {
                    var targetFolderItem = req.responseXML.getElementsByTagName("targetFolder")[0];            
                    var targetFolder = targetFolderItem.firstChild.nodeValue;

                    var targetPathItem = req.responseXML.getElementsByTagName("targetPath")[0];            
                    var targetPath = targetPathItem.firstChild.nodeValue;
                    
                    customAlert(resourceBundle["videoConversionStarted"] + " " + targetFolder + ".");
                    
                    setTimeout(function() {
    	                var expUrl = "/webfilesys/servlet?command=exp&expandPath=" + encodeURIComponent(targetPath) + "&mask=*&fastPath=true";
    	                window.parent.frames[1].location.href = expUrl;
                    } , 4000);
                    
                } else {
                    var messageItem = req.responseXML.getElementsByTagName("message")[0];            
                    var message = messageItem.firstChild.nodeValue;
                    customAlert(message);
                }
            } else {
                alert(resourceBundle["alert.communicationFailure"]);
            }
        }
    });
}

function createVideoTimeRangeSelOptions() {
    createVideoTimeOptions(document.getElementById("startHour"), 0, 10);
    createVideoTimeOptions(document.getElementById("startMin"), 0, 59);
    createVideoTimeOptions(document.getElementById("startSec"), 0, 59);
    
    createVideoTimeOptions(document.getElementById("endHour"), 0, 10);
    createVideoTimeOptions(document.getElementById("endMin"), 0, 59);
    createVideoTimeOptions(document.getElementById("endSec"), 0, 59);
}

function createVideoTimeOptions(selectBox, minVal, maxVal) {
    var i = 0;
    for (var val = minVal; val <= maxVal; val++) {
        var optionLabel;
        if (val < 10) {
            optionLabel = "0" + val;
        } else {
            optionLabel = val;
        }
        
        selectBox.options[i] = new Option(optionLabel, val);
        i++;
    }    
}
                