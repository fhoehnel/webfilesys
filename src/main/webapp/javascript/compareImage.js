function compareShowImage(imgPath, clickTarget) {
	
	markSelectedThumbnail(clickTarget);
	
	var windowWidth = getWinWidth();
	var windowHeight = getWinHeight();
	
	var thumbnailCont = document.getElementById("imgCompThumbCont");
	
	var thumbContWidth = thumbnailCont.offsetWidth;
	
	var picAreaWidth = windowWidth - thumbContWidth - 10;
	var picAreaHeight = windowHeight - 10;
	
	var picAreaRatio = picAreaWidth / picAreaHeight;
	
	var picture = document.getElementById("picture");
	picture.style.display = "none";
	
	picture.src = "/webfilesys/servlet?command=getFile&filePath=" + encodeURIComponent(imgPath);

	picture.onload = function () {
		var picWidth = picture.naturalWidth;
		var picHeight = picture.naturalHeight;
		
		var picRatio = picWidth / picHeight;
		
		var margin;

		if (picAreaRatio > picRatio) {
			picture.style.height = picAreaHeight + "px";
			picture.style.width = "auto";
			margin = 0;
		} else {
			picture.style.width = picAreaWidth + "px";
			picture.style.height = "auto";
			margin = Math.round((picAreaHeight - (picAreaWidth / picRatio)) / 2);
		}
		picture.style.marginTop = margin + "px";
		
		picture.style.display = "inline";
    }	
}

function markSelectedThumbnail(clickTarget) {
	$(".imgCompThumbSelected").removeClass("imgCompThumbSelected");
	clickTarget.setAttribute("class", "imgCompThumbSelected");
}

function compareImgLoadInitial() {
	var firstThumbCont = document.getElementById(firstImageThumbContId);
    compareShowImage(firstImagePath, $(firstThumbCont).children("a")[0]);
}

function compareImgDelete(picFilePath, picFileName) {
	
    var nameForId = picFileName.replace(/ /g, '_');
    var thumbContId = "thumbCont-" + nameForId;
    var thumbContToDelete = document.getElementById(thumbContId);
	
    compareShowImage(picFilePath + picFileName, $(thumbContToDelete).children("a")[0]);
	
	customConfirm(resourceBundle["confirm.delfile"], resourceBundle["button.cancel"], resourceBundle["button.ok"], 
   	    function() {
	        var url = "/webfilesys/servlet?command=delFile&filePath=" + encodeURIComponent(picFilePath + picFileName);
	    
	        xmlRequest(url, function(req) {
	            if (req.readyState == 4) {
	                if (req.status == 200) {
	                    var responseXml = req.responseXML;
	                    var successItem = responseXml.getElementsByTagName("success")[0];
	                    var success = successItem.firstChild.nodeValue;  
	                
	                    if (success == "true") {
	                        var deletedFileItem = responseXml.getElementsByTagName("deletedFile")[0];
	                        var deletedFile = deletedFileItem.firstChild.nodeValue; 
	                    
	                        compareImgClose(deletedFile);
	                        
                            window.opener.removeDeletedFile(deletedFile);
	                        
	                    } else {
	                        customAlert(resourceBundle["alert.delFileError"]);
	                    }
	                } else {
	                    customAlert(resourceBundle["alert.communicationFailure"]);
	                }
	            }
	        });          
	    }
	);
}

function compareImgClose(imgName) {

	var nameForId = imgName.replace(/ /g, '_');
    var thumbContId = "thumbCont-" + nameForId;
    var thumbContToClose = document.getElementById(thumbContId);
    if (thumbContToClose) {
    	thumbContToClose.parentNode.removeChild(thumbContToClose);
    }
    
	var picture = document.getElementById("picture");
	
	if (picture.src.endsWith(imgName)) {
		picture.style.display = "none";
	}
	
	if ($(".imgCompThumb").length == 0) {
		setTimeout("self.close()", 50);
	}
}
