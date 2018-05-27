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
               
        var windowWidth;
        var windowHeight;
                
        if ((browserWidthForRealSize <= screenWidth) && (browserHeightForRealSize <= screenHeight)) {
            resizeViewPort(picRealWidth + 1, picRealHeight + 1);
            windowWidth = picRealWidth;
            windowHeight = picRealHeight;
        } else {
            windowWidth;
            windowHeight;
            if (xRatio > yRatio) {
                windowWidth = screenWidth - 20;
                windowHeight = Math.round(windowWidth * picRealHeight / picRealWidth);
            } else {
                windowHeight = screenHeight - 80;
                windowWidth = Math.round(windowHeight * picRealWidth / picRealHeight);
            }
            resizeViewPort(windowWidth + 2, windowHeight + 2);
        }
        
        var pic = document.getElementById("picFullScreen");
        pic.style.width = windowWidth + "px";
        pic.style.height = windowHeight + "px";
        pic.style.visibility = "visible";
        
        if ((windowWidth < picRealWidth) || (windowHeight < picRealHeight)) {
            document.getElementById("origSizeOption").style.display = "inline";
        }
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
    