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
	
	setTimeout(() => {
        compareShowImage(firstImagePath, $(firstThumbCont).children("a")[0]);
	}, 100);
}

function compareImgDelete(picFilePath, picFileName) {
	
    var nameForId = picFileName.replace(/ /g, '_');
    var thumbContId = "thumbCont-" + nameForId;
    var thumbContToDelete = document.getElementById(thumbContId);
	
    compareShowImage(picFilePath + picFileName, $(thumbContToDelete).children("a")[0]);
	
	customConfirm(resourceBundle["confirm.delfile"], resourceBundle["button.cancel"], resourceBundle["button.ok"], 
   	    function() {
		
	        const parameters = { "filePath": encodeURIComponent(picFilePath + picFileName) };
	    
		    xmlPostRequest("delFile", parameters, function(responseXml) {
		
	            const successItem = responseXml.getElementsByTagName("success")[0];
	            const success = successItem.firstChild.nodeValue;  
	                
	            if (success == "true") {
	                var deletedFileItem = responseXml.getElementsByTagName("deletedFile")[0];
	                var deletedFile = deletedFileItem.firstChild.nodeValue; 
	                    
	                compareImgClose(deletedFile);
	                        
                    window.opener.removeDeletedFile(deletedFile);
	             } else {
	                customAlert(resourceBundle["alert.delFileError"]);
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

function sizeCompareImagesToFit() {
	const pic1 = document.getElementById("picture1");
	const pic1Width = pic1.naturalWidth;
	const pic1Height = pic1.naturalHeight;

	const pic2 = document.getElementById("picture2")
	const pic2Width = pic2.naturalWidth;
	const pic2Height = pic2.naturalHeight;
	
	const picWidth = pic1Width > pic2Width ? pic1Width : pic2Width;
	const picHeight = pic1Height > pic2Height ? pic1Height : pic2Height;
	
	const winWidth = window.innerWidth;
	const winHeight = window.innerHeight;
	
	sizeImageToFit(pic1, winWidth, winHeight);
	sizeImageToFit(pic2, winWidth, winHeight);
	
	const picture1Width = document.getElementById("picture1").width;
	const compImgCont = document.getElementById("compImgCont");
	compImgCont.style.width = picture1Width + "px";
	
	const script = document.createElement('script');
	script.src = "/webfilesys/javascript/imgCompSlider/imgCompSlider.js";
    document.head.appendChild(script);
}

function sizeImageToFit(picture, windowWidth, windowHeight) {
	
	var picAreaWidth = windowWidth - 10;
	var picAreaHeight = windowHeight - 10;
	
	var picAreaRatio = picAreaWidth / picAreaHeight;
	
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

function confirmDelImg(imgFileName) {
    deleteSelf(null, imgFileName);
}          

function resizeCompareThumb(picture) {
	if (picture.naturalHeight > picture.naturalWidth) {
		picture.style.height = "160px";
		picture.style.width = "auto";
	} else {
		picture.style.width = "160px";
		picture.style.height = "auto";
	}
	picture.style.display = "inline";
}
