var lastPreviewStartTime = "";
var lastPreviewEndTime = "";

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
	
	pic.onerror = function() {
		if (console) {
			console.log("failed to get video thumbnail");
		}
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

    var url = "/webfilesys/servlet?command=video&cmd=getVideoDimensions&fileName=" +  encodeURIComponent(picFileName);

    var picIsLink = pixDim.getAttribute("picIsLink");
    if (picIsLink) {
    	url = url + "&link=true";
    }
    
	xmlRequest(url, function(req) {
        if (req.readyState == 4) {
            if (req.status == 200) {
			    var xmlDoc = req.responseXML;
			    
                var errorItem = xmlDoc.getElementsByTagName("error")[0];            
                if (errorItem) {
                	return;
                }
			    
			    var videoWidth = null;
			    var videoHeight = null;
                var codec = null;
                var audioCodec = null;
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
			    
                item = xmlDoc.getElementsByTagName("audioCodec")[0];            
                if (item) {
                	audioCodec = item.firstChild.nodeValue;
                }
			    
                item = xmlDoc.getElementsByTagName("duration")[0];            
                if (item && item.firstChild) {
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
			        	if (audioCodec) {
                            var audioCodecCont = document.getElementById("audioCodec-" + picId.substring(4));
                            if (audioCodecCont) {
                            	audioCodecCont.innerHTML = audioCodec;
                            }
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
    } else if (cmd == 'concat') {
        multiVideoConcat();
    } else if (cmd == 'join') {
        multiVideoJoinParams();
    } else if (cmd == 'deshake') {
        multiVideoDeshake();
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

function multiVideoConcat() {
    if (checkTwoOrMoreFilesSelected()) {
    	showHourGlass();
	    document.form2.command.value = 'multiVideoConcat';
	    
	    xmlRequestPost("/webfilesys/servlet", getFormData(document.form2), function (req) {
	        if (req.readyState == 4) {
	            if (req.status == 200) {
	                var success = req.responseXML.getElementsByTagName("success")[0];
	                if (success) {
                        var targetFolderItem = req.responseXML.getElementsByTagName("targetFolder")[0];            
                        var targetFolder = targetFolderItem.firstChild.nodeValue;
		                
                        var targetPathItem = req.responseXML.getElementsByTagName("targetPath")[0];            
                        var targetPath = targetPathItem.firstChild.nodeValue;
                        
                        customAlert(resourceBundle["videoConcatStarted"] + " " + targetFolder + ".");
                        
                        setTimeout(function() {
                        	parent.parent.frames[1].location.href = "/webfilesys/servlet?command=exp&expandPath=" + encodeURIComponent(targetPath) + "&expand=" + encodeURIComponent(targetPath) + "&fastPath=true";
                        }, 6000);
	                } else {
		                var item = req.responseXML.getElementsByTagName("errorCode")[0];
		                var errorCode = item.firstChild.nodeValue;
		                if (errorCode == '1') {
		                    customAlert(resourceBundle["videoConcatErrorFrameRate"]);
		                } else if (errorCode == '2') {
		               	    customAlert(resourceBundle["videoConcatErrorCodec"]);
		                } else if (errorCode == '3') {
		                    customAlert(resourceBundle["videoConcatErrorResolution"]);
		                } else if (errorCode == '4') {
		                    customAlert(resourceBundle["videoConcatErrorProcess"]);
		                }
	                }
	            } else {
	            	alert(resourceBundle["alert.communicationFailure"]);
	            }
	            
	            document.form2.command.value = '';
	            document.form2.cmd.selectedIndex = 0;

	            hideHourGlass();
	        }
	    });
    } else {   
        customAlert(resourceBundle["selectTwoOrMoreVideoFiles"] + "!");
        document.form2.command.value = '';
        document.form2.cmd.selectedIndex = 0;
    }
}

function multiVideoJoinParams() {
    if (checkTwoOrMoreFilesSelected()) {
    	document.form2.command.value = "video";
        document.form2.submit();
    } else {   
        customAlert(resourceBundle["selectTwoOrMoreVideoFiles"] + "!");
        document.form2.command.value = '';
        document.form2.cmd.selectedIndex = 0;
    }
}

function sendConcatForm() {
	showHourGlass();
    
    xmlRequestPost("/webfilesys/servlet", getFormData(document.form1), function (req) {
        if (req.readyState == 4) {
            if (req.status == 200) {
                var success = req.responseXML.getElementsByTagName("success")[0];
                if (success) {
                    var targetFolderItem = req.responseXML.getElementsByTagName("targetFolder")[0];            
                    var targetFolder = targetFolderItem.firstChild.nodeValue;
	                
                    var targetPathItem = req.responseXML.getElementsByTagName("targetPath")[0];            
                    var targetPath = targetPathItem.firstChild.nodeValue;
                    
                    customAlert(resourceBundle["videoConcatStarted"] + " " + targetFolder + ".");
                    
                    setTimeout(function() {
                    	parent.parent.frames[1].location.href = "/webfilesys/servlet?command=exp&expandPath=" + encodeURIComponent(targetPath) + "&expand=" + encodeURIComponent(targetPath) + "&fastPath=true";
                    }, 6000);
                } else {
	                var item = req.responseXML.getElementsByTagName("errorCode")[0];
	                var errorCode = item.firstChild.nodeValue;
                    if (errorCode == '4') {
	                    customAlert(resourceBundle["videoConcatErrorProcess"]);
	                }
                }
            } else {
            	alert(resourceBundle["alert.communicationFailure"]);
            }
            
            hideHourGlass();
        }
    });
}

function multiVideoDeshake() {
    if (anySelected()) {
    	showHourGlass();
	    document.form2.command.value = 'multiVideoDeshake';
	    
	    xmlRequestPost("/webfilesys/servlet", getFormData(document.form2), function (req) {
	        if (req.readyState == 4) {
	            if (req.status == 200) {
	                var success = req.responseXML.getElementsByTagName("success")[0];
	                if (success) {
                        var targetFolderItem = req.responseXML.getElementsByTagName("targetFolder")[0];            
                        var targetFolder = targetFolderItem.firstChild.nodeValue;

                        var targetPathItem = req.responseXML.getElementsByTagName("targetPath")[0];            
                        var targetPath = targetPathItem.firstChild.nodeValue;
                        
                        customAlert(resourceBundle["videoDeshakeStarted"] + " " + targetFolder + ".");
                        
                        setTimeout(function() {
                        	parent.parent.frames[1].location.href = "/webfilesys/servlet?command=exp&expandPath=" + encodeURIComponent(targetPath) + "&expand=" + encodeURIComponent(targetPath) + "&fastPath=true";
                        }, 6000);
	                } else {
	                    customAlert(resourceBundle["errorVideoDeshake"]);
	                }
	            } else {
	            	alert(resourceBundle["alert.communicationFailure"]);
	            }
	            
	            document.form2.command.value = '';
	            document.form2.cmd.selectedIndex = 0;

	            hideHourGlass();
	        }
	    });
    } else {   
        customAlert(resourceBundle["alert.nofileselected"] + "!");
        document.form2.command.value = '';
        document.form2.cmd.selectedIndex = 0;
    }
}

function checkTwoOrMoreFilesSelected() {
    var numChecked = 0;
    
    for (var i = 0; i < document.form2.elements.length; i++) {
         if ((document.form2.elements[i].type == "checkbox") && 
		     (document.form2.elements[i].name != "cb-setAll") &&
		     document.form2.elements[i].checked) {
	         numChecked++;
         }
    }
    
    return (numChecked >= 2);
}

function playVideoMaxSize(videoFilePath, videoFileName, isLink) { 

	var fileNameExt = getFileNameExt(videoFileName);
	
    if ((fileNameExt != ".MP4") && (fileNameExt != ".OGG") && (fileNameExt != ".OGV") && (fileNameExt != ".WEBM")) {
    	
    	// no HTML 5 video - cannot be played in browser
    	playVideoLocal(videoFilePath);
    	return;
    }
	
    var url = "/webfilesys/servlet?command=video&cmd=getVideoDimensions&fileName=" +  encodeURIComponent(videoFileName);

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
	
	if (document.getElementById("startHour")) {
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

function sendTextOnVideoForm() {
    xmlRequestPost("/webfilesys/servlet", getFormData(document.textOnVideoForm), function(req) {
        if (req.readyState == 4) {
            if (req.status == 200) {
                var successItem = req.responseXML.getElementsByTagName("success")[0];            
                var success = successItem.firstChild.nodeValue;
                
                if (success == "true") {
                    var targetFolderItem = req.responseXML.getElementsByTagName("targetFolder")[0];            
                    var targetFolder = targetFolderItem.firstChild.nodeValue;

                    var targetPathItem = req.responseXML.getElementsByTagName("targetPath")[0];            
                    var targetPath = targetPathItem.firstChild.nodeValue;
                    
                    customAlert(resourceBundle["textOnVideoStarted"] + " " + targetFolder + ".");
                    
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

function sendFadeAudioForm() {
	var fadeInDuration = document.fadeAudioForm.fadeInDuration.value;
	var fadeOutDuration = document.fadeAudioForm.fadeOutDuration.value;
	
	if (fadeInDuration.length > 0) {
		var fadeInSeconds = parseInt(fadeInDuration);
		if ((fadeInDuration % 1 != 0) || (fadeInSeconds > videoDuration)) {
			customAlert(resourceBundle['fadeInValueInvalid']);
			return;
		}
	}
	
	if (fadeOutDuration.length > 0) {
		var fadeOutSeconds = parseInt(fadeOutDuration);
		if ((fadeOutDuration % 1 != 0) || (fadeOutSeconds > videoDuration)) {
			customAlert(resourceBundle['fadeOutValueInvalid']);
			return;
		}
	}

	xmlRequestPost("/webfilesys/servlet", getFormData(document.fadeAudioForm), function(req) {
        if (req.readyState == 4) {
            if (req.status == 200) {
                var successItem = req.responseXML.getElementsByTagName("success")[0];            
                var success = successItem.firstChild.nodeValue;
                
                if (success == "true") {
                    var targetFolderItem = req.responseXML.getElementsByTagName("targetFolder")[0];            
                    var targetFolder = targetFolderItem.firstChild.nodeValue;

                    var targetPathItem = req.responseXML.getElementsByTagName("targetPath")[0];            
                    var targetPath = targetPathItem.firstChild.nodeValue;
                    
                    customAlert(resourceBundle["videoFadeAudioStarted"] + " " + targetFolder + ".");
                    
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

function validateExtractVideoFrameForm() {
    var startHour = getSelectboxValueInt("startHour");
    var startMin = getSelectboxValueInt("startMin");
    var startSec = getSelectboxValueInt("startSec");
    
    var startTime = (startHour * 3600) + (startMin * 60) + startSec;

    if (startTime > durationSeconds) {
        customAlert(resourceBundle["validationError.videoFrameExtractTime"]);
        return false;
    }

    return true;
}

function sendExtractVideoFrameForm() {
    if (!validateExtractVideoFrameForm()) {
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
                    
                    customAlert(resourceBundle["videoFrameExtractionStarted"] + " " + targetFolder + ".");
                    
                    setTimeout(function() {
                    	parent.parent.frames[1].location.href = "/webfilesys/servlet?command=exp&expandPath=" + encodeURIComponent(targetPath) + "&expand=" + encodeURIComponent(targetPath) + "&fastPath=true&viewMode=2";
                    }, 5000);
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

function videoFrameGrabPreview() {
    if (!validateExtractVideoFrameForm()) {
        return;
    }

    document.getElementById("previewButton").disabled = true;
    
	showHourGlass();
	
    var previewImg = document.getElementById("previewVideoFrame");
    
    var hour = document.getElementById("startHour").value;
    var min = document.getElementById("startMin").value;
    var sec = document.getElementById("startSec").value;
    
    fetchPreviewFrame(previewImg, hour, min, sec);    
}

function videoEditStartPreview() {
    if (!validateConvertVideoForm()) {
        return;
    }

    document.getElementById("previewButton").disabled = true;
    
	showHourGlass();
	
    var previewImg = document.getElementById("previewVideoStartFrame");
    
    var hour = document.getElementById("startHour").value;
    var min = document.getElementById("startMin").value;
    var sec = document.getElementById("startSec").value;
    
    if (lastPreviewStartTime != hour + min + sec) {
        fetchPreviewFrame(previewImg, hour, min, sec, videoEditEndPreview);    
        lastPreviewStartTime = hour + min + sec;
    } else {
    	videoEditEndPreview();
    }
}

function videoEditEndPreview() {
    var previewImg = document.getElementById("previewVideoEndFrame");
    
    var hour = document.getElementById("endHour").value;
    var min = document.getElementById("endMin").value;
    var sec = document.getElementById("endSec").value;

    if (lastPreviewEndTime != hour + min + sec) {
        fetchPreviewFrame(previewImg, hour, min, sec);    
        lastPreviewEndTime = hour + min + sec;
    } else {
        document.getElementById("previewButton").disabled = false;
    	hideHourGlass();
    }    
}

function fetchPreviewFrame(previewImg, hour, min, sec, callBack) {
    previewImg.src = "/webfilesys/images/space.gif";
    
    var videoFileName = document.getElementById("videoFileName").value;
    var videoWidth = document.getElementById("videoWidth").value;
    var videoHeight = document.getElementById("videoHeight").value;
    
    previewImgUrl = "/webfilesys/servlet?command=video&cmd=previewFrame&videoFile=" + videoFileName + "&videoWidth=" + videoWidth + "&videoHeight=" + videoHeight + "&hour=" + hour + "&min=" + min + "&sec=" + sec;

    previewImg.src = previewImgUrl;

    previewImg.style.visibility = "visible";
    
    previewImg.onload = function() {
    	if (callBack) {
    		callBack();
    	} else {
            document.getElementById("previewButton").disabled = false;
        	hideHourGlass();
    	}
    };
}

function createVideoTimeRangeSelOptions() {
    createVideoTimeOptions(document.getElementById("startHour"), 0, 10);
    createVideoTimeOptions(document.getElementById("startMin"), 0, 59);
    createVideoTimeOptions(document.getElementById("startSec"), 0, 59);
    
    if (document.getElementById("endHour")) {
        var hourPreselect = Math.floor(durationSeconds / (60 * 60));
        
        createVideoTimeOptions(document.getElementById("endHour"), 0, 10, hourPreselect);

        var minutePreselect = Math.floor(durationSeconds % (60 * 60) / 60);
        
        createVideoTimeOptions(document.getElementById("endMin"), 0, 59, minutePreselect);

        var secondPreselect = Math.floor(durationSeconds % 60);
        
        createVideoTimeOptions(document.getElementById("endSec"), 0, 59, secondPreselect);
    }
}

function createVideoTimeOptions(selectBox, minVal, maxVal, preselectVal) {
    var i = 0;
    for (var val = minVal; val <= maxVal; val++) {
        var optionLabel;
        if (val < 10) {
            optionLabel = "0" + val;
        } else {
            optionLabel = val;
        }
        
        selectBox.options[i] = new Option(optionLabel, val);
        
        if (preselectVal && (val == preselectVal)) {
        	selectBox.selectedIndex = i;
        }
        
        i++;
    }    
}
                
function addAudioToVideo(videoFilePath) {
    var url = "/webfilesys/servlet?command=video&cmd=addAudioToVideo&videoFilePath=" +  encodeURIComponent(videoFilePath);

	xmlRequest(url, function(req) {
        if (req.readyState == 4) {
            if (req.status == 200) {
			    var xmlDoc = req.responseXML;
			    
                var errorItem = xmlDoc.getElementsByTagName("error")[0];            
                if (errorItem) {
                	customAlert(errorItem.firstChild.nodeValue);
                } else {
                    var targetFolderItem = xmlDoc.getElementsByTagName("targetFolder")[0];            
                    var targetFolder = targetFolderItem.firstChild.nodeValue;

                    var targetPathItem = xmlDoc.getElementsByTagName("targetPath")[0];            
                    var targetPath = targetPathItem.firstChild.nodeValue;
                    
                    customAlert(resourceBundle["addAudioToVideoStarted"] + " " + targetFolder + ".");
                    
                    setTimeout(function() {
    	                var expUrl = "/webfilesys/servlet?command=exp&expandPath=" + encodeURIComponent(targetPath) + "&mask=*&fastPath=true";
    	                window.parent.frames[1].location.href = expUrl;
                    } , 4000);
                }
            } else {
                alert(resourceBundle["alert.communicationFailure"]);
            }
        }
	});
}

function initialSetReencodeCheckboxState(codec) {
	if (codec == "h264") { 
		document.getElementById("re-encodeCont").style.display = "inline";
	}
}

function setReencodeCheckboxState() {
	var newCodec = document.getElementById("newCodec").value;
	if ((newCodec == "h264") || ((newCodec == "") && (oldCodec == "h264"))) {
		document.getElementById("re-encodeCont").style.display = "inline";
	} else {
		document.getElementById("re-encodeCont").style.display = "none";
	}
}