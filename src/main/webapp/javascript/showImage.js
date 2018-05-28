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
               
        var pic = document.getElementById("picFullScreen");
                
        if ((browserWidthForRealSize <= screenWidth) && (browserHeightForRealSize <= screenHeight)) {
            resizeViewPort(picRealWidth + 1, picRealHeight + 1);
            pic.style.width = picRealWidth + "px";
            pic.style.height = picRealHeight + "px";
        } else {
        	var imgDisplayWidth;
        	var imgDisplayHeight;
        	
            if (xRatio > yRatio) {
            	imgDisplayWidth = screenWidth - 20;
            	imgDisplayHeight = Math.round(imgDisplayWidth * picRealHeight / picRealWidth)
                pic.style.width = imgDisplayWidth + "px";
                pic.style.height = "auto";
            } else {
            	imgDisplayHeight = screenHeight - 80;
            	imgDisplayWidth = Math.round(imgDisplayHeight * picRealWidth / picRealHeight);
                pic.style.height = imgDisplayHeight + "px";
                pic.style.width = "auto";
            }
            resizeViewPort(imgDisplayWidth + 2, imgDisplayHeight + 2);
        }
        
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
    