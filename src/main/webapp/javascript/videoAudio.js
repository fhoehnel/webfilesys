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

    const parameters = {
        "cmd": "getVideoDimensions",
        "fileName": encodeURIComponent(picFileName)
    };

    var picIsLink = pixDim.getAttribute("picIsLink");
    if (picIsLink) {
        parameters.link = "true";
    }

    xmlGetRequest("video", parameters, responseXml => {
        const errorItem = responseXml.getElementsByTagName("error")[0];
        if (errorItem) {
          	return;
        }

		let videoWidth = null;
		let videoHeight = null;
        let codec = null;
        let audioCodec = null;
        let duration = null;
        let fps = null;

        let item = responseXml.getElementsByTagName("xpix")[0];
        if (item) {
            videoWidth = item.firstChild.nodeValue;
        }

        item = responseXml.getElementsByTagName("ypix")[0];
        if (item) {
            videoHeight = item.firstChild.nodeValue;
        }

        item = responseXml.getElementsByTagName("codec")[0];
        if (item) {
           	codec = item.firstChild.nodeValue;
        }

        item = responseXml.getElementsByTagName("audioCodec")[0];
        if (item) {
           	audioCodec = item.firstChild.nodeValue;
        }

        item = responseXml.getElementsByTagName("duration")[0];
        if (item && item.firstChild) {
           	duration = item.firstChild.nodeValue;
        }

        item = responseXml.getElementsByTagName("fps")[0];
        if (item) {
           	fps = item.firstChild.nodeValue;
        }

		if ((videoWidth != null) && (videoHeight != null)) {
		    pixDim.innerHTML = videoWidth + " x " + videoHeight + " pix";

		    const pic = document.getElementById(picId);
		    if (pic) {
		      	pic.setAttribute("origWidth", videoWidth);
		       	pic.setAttribute("origHeight", videoHeight);
		       	if (codec) {
                    const codecCont = document.getElementById("codec-" + picId.substring(4));
                    if (codecCont) {
                        codecCont.innerHTML = codec;
                    }
		       		// pic.setAttribute("codec", codec);
		       	}
		       	if (duration) {
                    const durationCont = document.getElementById("duration-" + picId.substring(4));
                    if (durationCont) {
                        durationCont.innerHTML = duration;
                    }
		      		// pic.setAttribute("duration", duration);
		       	}
		       	if (fps) {
                    const fpsCont = document.getElementById("fps-" + picId.substring(4));
                    if (fpsCont) {
                        fpsCont.innerHTML = fps + " fps";
                    }
		       		// pic.setAttribute("fps", fps);
		       	}
		       	if (audioCodec) {
                    const audioCodecCont = document.getElementById("audioCodec-" + picId.substring(4));
                    if (audioCodecCont) {
                       	audioCodecCont.innerHTML = audioCodec;
                    }
		       	}
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
    } else if (cmd == 'addSilentAudio') {
        multiVideoAddSilentAudio();
    }
     
    document.form2.cmd.selectedIndex = 0;
}

function multiVideoCopyMove() {
    if (anySelected()) {
        document.form2.command.value = 'multiImageCopyMove';
        xmlFetchPost(getFormData(document.form2), handleCopyResult);
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
	    document.form2.command.value = 'multiVideoConcat';

        xmlFetchPost(getFormData(document.form2), responseXml => {

            const success = responseXml.getElementsByTagName("success")[0];
            if (success) {
                const targetFolderItem = responseXml.getElementsByTagName("targetFolder")[0];
                const targetFolder = targetFolderItem.firstChild.nodeValue;

                const targetPathItem = responseXml.getElementsByTagName("targetPath")[0];
                const targetPath = targetPathItem.firstChild.nodeValue;

                customAlert(resourceBundle["videoConcatStarted"] + " " + targetFolder + ".");

                setTimeout(function () {
                    parent.parent.frames[1].location.href = "/webfilesys/servlet?command=exp&expandPath=" + encodeURIComponent(targetPath) + "&expand=" + encodeURIComponent(targetPath) + "&fastPath=true";
                }, 6000);
            } else {
                const item = responseXml.getElementsByTagName("errorCode")[0];
                const errorCode = item.firstChild.nodeValue;
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
            document.form2.command.value = '';
            document.form2.cmd.selectedIndex = 0;
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
    xmlFetchPost(getFormData(document.form1), responseXml => {
        const success = responseXml.getElementsByTagName("success")[0];
        if (success) {
            const targetFolderItem = responseXml.getElementsByTagName("targetFolder")[0];
            const targetFolder = targetFolderItem.firstChild.nodeValue;
	                
            const targetPathItem = responseXml.getElementsByTagName("targetPath")[0];
            const targetPath = targetPathItem.firstChild.nodeValue;
                    
            customAlert(resourceBundle["videoConcatStarted"] + " " + targetFolder + ".");
                    
            setTimeout(function() {
              	parent.parent.frames[1].location.href = "/webfilesys/servlet?command=exp&expandPath=" + encodeURIComponent(targetPath) + "&expand=" + encodeURIComponent(targetPath) + "&fastPath=true";
            }, 6000);
        } else {
	        const item = responseXml.getElementsByTagName("errorCode")[0];
	        const errorCode = item.firstChild.nodeValue;
            if (errorCode === '4') {
	            customAlert(resourceBundle["videoConcatErrorProcess"]);
	        }
        }
    });
}

function multiVideoDeshake() {
    if (!anySelected()) {
        customAlert(resourceBundle["alert.nofileselected"] + "!");
        document.form2.command.value = '';
        document.form2.cmd.selectedIndex = 0;
        return;
    }

    document.form2.command.value = 'multiVideoDeshake';

    xmlFetchPost(getFormData(document.form2), responseXml => {
        handleAsyncVideoTransformResult(responseXml, "videoDeshakeStarted");
        document.form2.command.value = '';
        document.form2.cmd.selectedIndex = 0;
    });
}

function multiVideoAddSilentAudio() {
    if (!anySelected()) {
        customAlert(resourceBundle["alert.nofileselected"] + "!");
        document.form2.command.value = '';
        document.form2.cmd.selectedIndex = 0;
        return;
    }
    document.form2.command.value = 'multiVideoAddSilentAudio';

    xmlFetchPost(getFormData(document.form2), responseXml => {
        handleAsyncVideoTransformResult(responseXml, "addSilentAudioStarted");
        document.form2.command.value = '';
        document.form2.cmd.selectedIndex = 0;
    });
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

	const fileNameExt = getFileNameExt(videoFileName);
	
    if (fileNameExt !== ".MP4" && fileNameExt !== ".OGG" && fileNameExt !== ".OGV" && fileNameExt !== ".WEBM") {
    	// no HTML 5 video - cannot be played in browser
    	playVideoLocal(videoFilePath);
    	return;
    }
	
    const parameters = {
        cmd: "getVideoDimensions",
        fileName: encodeURIComponent(videoFileName)
    }

    if (isLink) {
        parameters["link"] = "true";
    }

    xmlGetRequest("video", parameters, responseXml => {
        let videoWidth = 480;
        let videoHeight = 360;
        let codec = null;

        let item = responseXml.getElementsByTagName("codec")[0];
        if (item) {
            codec = item.firstChild.nodeValue;
            if (codec === "mpeg4") {
                // no HTML 5 video - cannot be played in browser
                playVideoLocal(videoFilePath);
                return;
            }
        }

        item = responseXml.getElementsByTagName("xpix")[0];
        if (item) {
            videoWidth = parseInt(item.firstChild.nodeValue);
        }

        item = responseXml.getElementsByTagName("ypix")[0];
        if (item) {
            videoHeight = parseInt(item.firstChild.nodeValue);
        }

        const availWidth = getWinWidth() - 20;
        const availHeight = getWinHeight() - 20;

        const maxVideoWidth = availWidth - 40;
        const maxVideoHeight = availHeight - 60;

        const widthScale = videoWidth / maxVideoWidth;
        const heightScale = videoHeight / maxVideoHeight;

        let scaledWidth = videoWidth;
        let scaledHeight = videoHeight;

        if ((widthScale > 1) || (heightScale > 1)) {
            let scale;
            if (widthScale > heightScale) {
                scale = widthScale;
            } else {
                scale = heightScale;
            }
            scaledWidth = videoWidth * (1 / scale);
            scaledHeight = videoHeight * (1 / scale);
        }

        let videoType = "mp4";

        if (videoFileName.endsWithIgnoreCase(".ogg") || videoFileName.endsWithIgnoreCase(".ogv")) {
            videoType = "ogg"
        } else if (videoFileName.endsWithIgnoreCase(".webm")) {
            videoType = "webm"
        }

        const videoCont = document.createElement("div");
        videoCont.id = "videoCont";
        videoCont.setAttribute("class", "maxVideoCont");
        videoCont.style.width = (scaledWidth + 20) + "px";
        videoCont.style.height = (scaledHeight + 40) + "px";

        const closeButton = document.createElement("img");
        closeButton.setAttribute("src", "/webfilesys/images/winClose.gif");
        closeButton.setAttribute("class", "closeButton");
        closeButton.setAttribute("onclick", "destroyVideo()");
        videoCont.appendChild(closeButton);

        const videoUrl = "/webfilesys/servlet?command=getFile&filePath=" + encodeURIComponent(videoFilePath);

        const videoElem = document.createElement("video");
        videoElem.setAttribute("autobuffer", "autobuffer");
        videoElem.setAttribute("autoplay", "autoplay");
        videoElem.setAttribute("controls", "controls");
        videoElem.setAttribute("src", videoUrl);
        videoElem.setAttribute("type", videoType);
        videoElem.style.width = scaledWidth + "px";
        videoElem.style.height = scaledHeight + "px";

        const altTextElem = document.createElement("p");
        altTextElem.innerHTML = "This browser does not support HTML5 video!"
        videoElem.appendChild(altTextElem);

        videoCont.appendChild(videoElem);

        document.documentElement.appendChild(videoCont);

        centerBox(videoCont);
    });
}

function validateTimeRange() {
	
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
    if (!validateTimeRange()) {
        return;
    }
    xmlFetchPost(getFormData(document.form1), responseXml => {
        handleAsyncVideoTransformResult(responseXml, "videoConversionStarted");
    });
}

function sendCutAudioForm() {
    if (!validateTimeRange()) {
        return;
    }
    xmlFetchPost(getFormData(document.form1), responseXml => {
        handleAsyncVideoTransformResult(responseXml, "cutAudioStarted");
    });
}

function sendTextOnVideoForm() {
    xmlFetchPost(getFormData(document.textOnVideoForm), responseXml => {
        handleAsyncVideoTransformResult(responseXml, "textOnVideoStarted");
    });
}

function sendFadeAudioForm() {
	const fadeInDuration = document.fadeAudioForm.fadeInDuration.value;
	const fadeOutDuration = document.fadeAudioForm.fadeOutDuration.value;
	
	if (fadeInDuration.length > 0) {
		const fadeInSeconds = parseInt(fadeInDuration);
		if ((fadeInDuration % 1 != 0) || (fadeInSeconds > videoDuration)) {
			customAlert(resourceBundle['fadeInValueInvalid']);
			return;
		}
	}
	
	if (fadeOutDuration.length > 0) {
		const fadeOutSeconds = parseInt(fadeOutDuration);
		if ((fadeOutDuration % 1 != 0) || (fadeOutSeconds > videoDuration)) {
			customAlert(resourceBundle['fadeOutValueInvalid']);
			return;
		}
	}

    xmlFetchPost(getFormData(document.fadeAudioForm), responseXml => {
        handleAsyncVideoTransformResult(responseXml, "videoFadeAudioStarted");
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

    xmlFetchPost(getFormData(document.form1), responseXml => {
        handleAsyncVideoTransformResult(responseXml, "videoFrameExtractionStarted", 2);
    });
}

function handleAsyncVideoTransformResult(responseXml, msgKey, targetFolderViewMode) {
    const successItem = responseXml.getElementsByTagName("success")[0];
    const success = successItem.firstChild.nodeValue;

    if (success === "true") {
        const targetFolderItem = responseXml.getElementsByTagName("targetFolder")[0];
        const targetFolder = targetFolderItem.firstChild.nodeValue;

        const targetPathItem = responseXml.getElementsByTagName("targetPath")[0];
        const targetPath = targetPathItem.firstChild.nodeValue;

        customAlert(resourceBundle[msgKey] + " " + targetFolder + ".");

        setTimeout(function() {
            let targetURL = "/webfilesys/servlet?command=exp&expandPath=" + encodeURIComponent(targetPath) + "&mask=*&fastPath=true";
            if (targetFolderViewMode) {
                targetURL += "&viewMode=" + targetFolderViewMode;
            }
            parent.parent.frames[1].location.href = targetURL;
        }, 5000);
    } else {
        const messageItem = responseXml.getElementsByTagName("message")[0];
        const message = messageItem.firstChild.nodeValue;
        customAlert(message);
    }
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
    if (!validateTimeRange()) {
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
    const parameters = {
        "cmd": "addAudioToVideo",
        "videoFilePath": encodeURIComponent(videoFilePath)
    };

    xmlGetRequest("video", parameters, responseXml => {
        const errorItem = responseXml.getElementsByTagName("error")[0];
        if (errorItem) {
           	customAlert(errorItem.firstChild.nodeValue);
        } else {
            const targetFolderItem = responseXml.getElementsByTagName("targetFolder")[0];
            const targetFolder = targetFolderItem.firstChild.nodeValue;

            const targetPathItem = responseXml.getElementsByTagName("targetPath")[0];
            const targetPath = targetPathItem.firstChild.nodeValue;
                    
            customAlert(resourceBundle["addAudioToVideoStarted"] + " " + targetFolder + ".");
                    
            setTimeout(function() {
    	        const expUrl = "/webfilesys/servlet?command=exp&expandPath=" + encodeURIComponent(targetPath) + "&mask=*&fastPath=true";
    	        window.parent.frames[1].location.href = expUrl;
            } , 4000);
        }
	});
}

function videoDurationSum() {
	showHourGlass();

    xmlGetRequest("video", {"cmd": "videoDurationSum"}, function(responseXml) {
        const durationElem = responseXml.getElementsByTagName("duration")[0];
        let result = "";
        if (durationElem.firstChild) {
            const duration = durationElem.firstChild.nodeValue;        
            result = resourceBundle["videoDurationSumResult"] + ":<br/>" + duration + " hh:mm:ss";
        }
        const errorElem = responseXml.getElementsByTagName("error")[0];
        if (errorElem) {
        	result += "<br/><br/>(" + resourceBundle["videoDurationSumError"] + ")"
        }
    	hideHourGlass();
        customAlert(result);
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