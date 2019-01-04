var MIN_VIEWPORT_WIDTH = 380;
var MIN_VIEWPORT_HEIGHT = 300;

    function showPicInfoMenu() {
        document.getElementById("picInfoMenuCont").style.visibility = "visible";
    }
    
    function hidePicInfoMenu() {
        document.getElementById("picInfoMenuCont").style.visibility = "hidden";
    }

    function rate() {
        document.form1.submit();
    }
    
    function printPage() {
    	customConfirm(resourceBundle["confirm.print"], resourceBundle["button.cancel"], resourceBundle["button.ok"], 
    			function() {
                    window.print();
    	        }
    	);
    }

    function scaleImage(picRealWidth, picRealHeight) {
    	
        var screenWidth = screen.availWidth;
        var screenHeight = screen.availHeight;
        
        var xRatio = picRealWidth * 1000 / screenWidth;
        var yRatio = picRealHeight * 1000 / screenHeight;
    
        var browserWidthForRealSize = picRealWidth + 20;
        var browserHeightForRealSize = picRealHeight + 80;
               
        if ((browserWidthForRealSize <= screenWidth) && (browserHeightForRealSize <= screenHeight)) {
            var viewPortWidth = picRealWidth + 4;
            if (viewPortWidth < MIN_VIEWPORT_WIDTH) {
            	viewPortWidth = MIN_VIEWPORT_WIDTH;
            }
            var viewPortHeight = picRealHeight + 4;
            if (viewPortHeight < MIN_VIEWPORT_HEIGHT) {
            	viewPortHeight = MIN_VIEWPORT_HEIGHT;
            }
        	resizeViewPort(viewPortWidth, viewPortHeight);
        } else {
        	var imgDisplayWidth;
        	var imgDisplayHeight;
        	
            if (xRatio > yRatio) {
            	imgDisplayWidth = screenWidth - 20;
            	imgDisplayHeight = Math.round(imgDisplayWidth * picRealHeight / picRealWidth)
            } else {
            	imgDisplayHeight = screenHeight - 80;
            	imgDisplayWidth = Math.round(imgDisplayHeight * picRealWidth / picRealHeight);
            }
            resizeViewPort(imgDisplayWidth + 2, imgDisplayHeight + 2);
        }
        
        setTimeout(function() {
            var pic = document.getElementById("picFullScreen");
            
            var winWidth = getWinWidth();
            var winHeight = getWinHeight();
            
            xRatio = picRealWidth * 1000 / winWidth;
            yRatio = picRealHeight * 1000 / winHeight;
            
            if ((picRealWidth <= winWidth - 4) && (picRealHeight <= winHeight - 4)) {
                pic.style.width = picRealWidth + "px";
                pic.style.height = picRealHeight + "px";
            } else {
                if (xRatio > yRatio) {
                	var imgDisplayWidth = winWidth - 4;
                    pic.style.width = imgDisplayWidth + "px";
                    pic.style.height = "auto";
                } else {
                	var imgDisplayHeight = winHeight - 4;
                    pic.style.height = imgDisplayHeight + "px";
                    pic.style.width = "auto";
                }
            }
            
            pic.style.visibility = "visible";
            
            if ((picRealWidth >= winWidth) || (picRealHeight >= winHeight)) {
                document.getElementById("origSizeOption").style.display = "inline";
            }
        }, 300);
        
    }

function deleteSelf(imgPath, imgName) {
	
	var fileName;
	if (imgName) {
		fileName = imgName;
	} else {
		fileName = extractFileName(imgPath)
	}
	
	customConfirm(resourceBundle["confirm.delfile"], resourceBundle["button.cancel"], resourceBundle["button.ok"], 
			function() {
	            var url;
	            if (imgName) {
	                url = "/webfilesys/servlet?command=delFile&fileName=" + encodeURIComponent(imgName);
	            } else {
	                url = "/webfilesys/servlet?command=delFile&filePath=" + encodeURIComponent(imgPath);
	            }
	    
	            xmlRequest(url, function(req) {
	                if (req.readyState == 4) {
	                    if (req.status == 200) {
	                        var responseXml = req.responseXML;
	                        var successItem = responseXml.getElementsByTagName("success")[0];
	                        var success = successItem.firstChild.nodeValue;  
	                
	                        if (success == "true") {
	                            var deletedFileItem = responseXml.getElementsByTagName("deletedFile")[0];
	                            var deletedFile = deletedFileItem.firstChild.nodeValue; 
	                    
	                            window.opener.removeDeletedFile(deletedFile);
	                    
	                            setTimeout("self.close()", 300);
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
    