var thumbLoadRunning = false;

var viewPicQueue;

function multiFileFunction() {
    var idx = document.form2.cmd.selectedIndex;

    var cmd = document.form2.cmd.options[idx].value;

    if (cmd == 'compare') {
	    compare();
    } else if (cmd == 'rotateLeft') {
        rotate('270');
    } else if (cmd == 'rotateRight') {
        rotate('90');
    } else if (cmd == 'resize') {
        resize();
    } else if ((cmd == 'copy') || (cmd == 'move') || (cmd == 'copyAdd') || (cmd == 'moveAdd')) {
        multiImageCopyMove();
    } else if (cmd == 'delete') {
        multiImageDelete();
        return;
    } else if (cmd == 'download') {
        multiImageDownload();
    } else if (cmd == 'exifRename') {
        renameToExifDate();
    } else if (cmd == 'view') {
        multiViewImage();
    }
     
    document.form2.cmd.selectedIndex = 0;
		
    resetSelected();
}

function resetSelected() {
    for (var i = document.form2.elements.length - 1; i >= 0; i--) {
	    if ((document.form2.elements[i].type == "checkbox") && document.form2.elements[i].checked) {
	        document.form2.elements[i].checked = false;
        }
    }
}

function multiViewImage() {
	if (anySelected()) {
	    viewPicQueue = new Array();
	
	    for (var i = document.form2.elements.length - 1; i >= 0; i--) {
	         if ((document.form2.elements[i].type == "checkbox") && document.form2.elements[i].checked) {
	         
	             viewPicQueue.push($(document.form2.elements[i]).prev().prev().attr("href"));
		     }
	    }

        viewQueuedPics();
    } else {
        customAlert(resourceBundle["alert.nofileselected"] + "!");
    }
}

function viewQueuedPics() {
    if (viewPicQueue.length == 0) {
        return;
    }
    
    var viewPicCommand = viewPicQueue.pop();
    
    eval(viewPicCommand);
    
    if (viewPicQueue.length == 0) {
	    resetSelected();
		document.form2.command.value = 'compareImg';
    } else {
        setTimeout(viewQueuedPics, 600);
    }    
}

function compare() {
	var numChecked = getSelectedCheckboxCount();

    if (numChecked < 2) {
        customAlert(resourceBundle["error.compselect"] + "!");
        return;
    }

    var cmdUrl;
    if (numChecked == 2) {
        document.form2.command.value = 'compareImgSlider';
    } else {
        document.form2.command.value = 'compareImg';
    }
    
    var compareWin = window.open('/webfilesys/servlet?command=blank','compareWin','scrollbars=no,resizable=yes,status=no,menubar=no,toolbar=no,location=no,directories=no,screenX=0,screenY=0,left=0,top=0');
    
    if (!compareWin) {
    	customAlert(resourceBundle["alert.enablePopups"]);
        return;
    }
    
    compareWin.focus();
    document.form2.target = 'compareWin';
    
    if (document.form2.screenWidth) {
        document.form2.screenWidth.value = screen.width;
    }

    if (document.form2.screenHeight) {
        document.form2.screenHeight.value = screen.height;
    }
    
    document.form2.submit();
    document.form2.target = '';
}

function anySelected() {
    for (var i = document.form2.elements.length - 1; i >= 0; i--) {
         if ((document.form2.elements[i].type == "checkbox") && document.form2.elements[i].checked) {
	         return(true);
	     }
    }

    return(false);
}

function getSelectedCheckboxCount() {
    var numChecked = 0;
    
    for (var i = document.form2.elements.length - 1; i >= 0; i--) {
         if ((document.form2.elements[i].type == "checkbox") && document.form2.elements[i].checked) {
	         numChecked++;
         }
    }
    
    return numChecked;
}

function rotate(degree) {
    if (anySelected()) {
        document.form2.command.value = 'multiTransform';
	    document.form2.degrees.value = degree;
        document.form2.submit();
    } else {
        customAlert(resourceBundle["alert.nofileselected"] + "!");
    }
}

function resize() {
    if (anySelected()) {
	    document.form2.command.value = 'resizeParms';
        document.form2.submit();
    } else {   
        customAlert(resourceBundle["alert.nofileselected"] + "!");
    }
}

function multiImageCopyMove() {
    if (anySelected()) {
        document.form2.command.value = 'multiImageCopyMove';
        xmlRequestPost("/webfilesys/servlet", getFormData(document.form2), showCopyResult);
	    document.form2.command.value = 'compareImg';
    } else {   
        customAlert(resourceBundle["alert.nofileselected"] + "!");
    }
}

function multiImageDelete() {
    if (anySelected()) {
    	customConfirm(resourceBundle["confirm.deleteImages"], resourceBundle["button.cancel"], resourceBundle["button.ok"], 
    			function() {
	                document.form2.command.value = 'multiImageDelete';
                    document.form2.submit();
    	        },
    			function() {
    	            document.form2.command.value = 'compareImg';
    	            document.form2.cmd.selectedIndex = 0;
    	            resetSelected();
    	            closeAlert();
    	        }
    	);
    } else {   
        customAlert(resourceBundle["alert.nofileselected"] + "!");
    }
}

function renameToExifDate() {
    if (anySelected()) {
	    document.form2.command.value = 'multiImageExifRename';
        document.form2.submit();
	    document.form2.command.value = 'compareImg';
    } else {   
        customAlert(resourceBundle["alert.nofileselected"] + "!");
    }
}

function multiImageDownload() {
    if (anySelected()) {
        document.form2.command.value = 'multiImgDownload';
        document.form2.submit();
    } else {   
        customAlert(resourceBundle["alert.nofileselected"] + "!");
    }
}

function setAllSelected() {
    var allSelected = true;
	
	var fileCheckboxes = new Array();
	
    for (var i = document.form2.elements.length - 1; i >= 0; i--) {
        if ((document.form2.elements[i].type == "checkbox") &&
            (document.form2.elements[i].name.indexOf("list-") === 0)) {
			fileCheckboxes.push(document.form2.elements[i]);
	        if ((document.form2.elements[i].checked == false) &&
	            (document.form2.elements[i].disabled == false)) {
		        allSelected = false;
	        }
	    } 
    }
	
    if (allSelected) {
	    for (var i = 0; i < fileCheckboxes.length; i++) {
		    fileCheckboxes[i].checked = false;
	    }
    } else {
	    for (var i = 0; i < fileCheckboxes.length; i++) {
		    if (!fileCheckboxes[i].disabled) {
		        fileCheckboxes[i].checked = true;
			}
	    }
		document.getElementById("cb-setAll").checked = true;
    }	
}

function checkGeoDataExist(callbackExist, callbackNotExist) {
    showHourGlass();
       
    var url = "/webfilesys/servlet?command=ajaxRPC&method=checkForGeoData";
    
    xmlRequest(url, function(req) {
        if (req.readyState == 4) {
            if (req.status == 200) {
                var responseXml = req.responseXML;
                var resultItem = responseXml.getElementsByTagName("result")[0];
                var result = resultItem.firstChild.nodeValue;  
                
                if (result && (result == "true")) {
                    callbackExist();
                } else {
                    callbackNotExist();
                }
            } else {
                alert(resourceBundle["alert.communicationFailure"]);
            }
        }
    });          
} 

function exportGeoData() {
    checkGeoDataExist(
        function() {
            hideHourGlass();
            window.location.href = "/webfilesys/servlet?command=googleEarthDirPlacemarks";
        },
        function() {
            hideHourGlass();
            alert(resourceBundle["noFilesWithGeoData"]);
        }
    );
} 


function filesOSMap() {
    checkGeoDataExist(
        function() {
            hideHourGlass();
            var mapWin = window.open('/webfilesys/servlet?command=osMapFiles&path=' + encodeURIComponent(pathForScript),'mapWin','status=no,toolbar=no,location=no,menu=no,width=600,height=400,resizable=yes,left=20,top=20,screenX=20,screenY=20');
            mapWin.focus();
        },
        function() {
            hideHourGlass();
            alert(resourceBundle["noFilesWithGeoData"]);
        }
    );
}

function googleMapAllPics() {
    checkGeoDataExist(
        function() {
            hideHourGlass();
            var mapWinWidth =  screen.availWidth - 20;
            var mapWinHeight = screen.availHeight - 80;
            	
            var mapWin = window.open('/webfilesys/servlet?command=googleMapMulti', 'mapWin', 'status=no,toolbar=no,location=no,menu=no,width=' + mapWinWidth + ',height=' + mapWinHeight + ',resizable=yes,left=2,top=2,screenX=2,screenY=2');

            if (!mapWin) {
                alert(resourceBundle["alert.enablePopups"]);
            } else {
                mapWin.focus();
            }
        },
        function() {
            hideHourGlass();
            alert(resourceBundle["noFilesWithGeoData"]);
        }
    );
}

function initialLoadPictures() {

    var scrollAreaCont = document.getElementById("scrollAreaCont");

    var counter = 0;

	for (var i = 0; (counter < 10) && (i < thumbnails.length); i++) {
	    var pic = document.getElementById("pic-" + thumbnails[i]);
	    if (pic) {
			var imgPath = pic.getAttribute("imgPath");
			if (imgPath) {
	        	if (isScrolledIntoView(pic, scrollAreaCont)) {
		  		    thumbnails.splice(i, 1);
		    		
		   		    loadThumbnail(pic, imgPath);
	    
	                setPictureDimensions(pic);
	                
	                counter++;
	        	}
	        }
	    }
	}
}

function attachScrollHandler() {
    var scrollAreaCont = document.getElementById("scrollAreaCont");

    scrollAreaCont.onscroll = function() {
	  	 var scrollPosDiff = scrollAreaCont.scrollTop - lastScrollPos;

		 if ((scrollPosDiff > 20) || (scrollPosDiff < (-20))) {
			 lastScrollPos = scrollAreaCont.scrollTop;
			 
			 if (!thumbLoadRunning) {
				 checkThumbnailsToLoad();
				 
                 releaseInvisibleThumbnails();
			 }
	  	 }
	};
	
	// load initially visible thumbnails
	setTimeout(checkThumbnailsToLoad, 10);
}

function releaseInvisibleThumbnails() {
	
	for (var i = loadedThumbs.length - 1; i >= 0; i--) {
	    var pic = loadedThumbs[i];
	    
	    if ((getNaturalWidth(pic) > 400) || (getNaturalHeight(pic) > 400)) {
	       	if (isScrolledOutOfView(pic, scrollAreaCont)) {
	       	    pic.setAttribute("imgPath", pic.src);
	       	    pic.src = "";
		        loadedThumbs.splice(i, 1);
	       	    thumbnails.push(pic.id.substring(4));
	       	}
	    }
	}
}

function getNaturalWidth(pic) {
    if (pic.naturalWidth) {
        return pic.naturalWidth;
    }

    // workaround for MSIE
    var origWidthAttrib = pic.getAttribute("origWidth");
            
    if (origWidthAttrib) {
	    return parseInt(origWidthAttrib);
    }
    
    return 0;
}

function getNaturalHeight(pic) {
    if (pic.naturalHeight) {
        return pic.naturalHeight;
    }

    // workaround for MSIE
    var origHeightAttrib = pic.getAttribute("origHeight");
            
    if (origHeightAttrib) {
	    return parseInt(origHeightAttrib);
    }
    
    return 0;
}

function checkThumbnailsToLoad() {

	if (thumbnails.length == 0) {
	    releaseInvisibleThumbnails();
		return;
	}
	
	thumbLoadRunning = true;

    var scrollAreaCont = document.getElementById("scrollAreaCont");
	
	for (var i = thumbnails.length - 1; i >= 0; i--) {
		var pic = document.getElementById("pic-" + thumbnails[i]);
	    if (pic) {
			var imgPath = pic.getAttribute("imgPath");
			if (imgPath) {
	        	if (isScrolledIntoView(pic, scrollAreaCont)) {
	        		thumbnails.splice(i, 1);
		    		
		    		loadThumbnail(pic, imgPath);
	    
	                setPictureDimensions(pic);
	                
	                thumbLoadRunning = false;
	                
	                return;
	    		}
	    	}
	    }
	}

    thumbLoadRunning = false;
    
    // releaseInvisibleThumbnails();
}

function setPictureDimensions(pic) { 

    if (pic.getAttribute("origWidth")) {
        return;
    }

    var picId = pic.id;

    var pixDim = document.getElementById("pixDim-" + picId.substring(4));
    if (!pixDim) {
        return;
    }

    var picFileName = pixDim.getAttribute("picFileName");

    var url = "/webfilesys/servlet?command=getPicDimensions&fileName=" +  encodeURIComponent(picFileName);

    var picIsLink = pixDim.getAttribute("picIsLink");
    if (picIsLink) {
    	url = url + "&link=true";
    }
    
	xmlRequest(url, function(req) {
        if (req.readyState == 4) {
            if (req.status == 200) {
			    var xmlDoc = req.responseXML;
			    
			    var picWidth = null;
			    var picHeight = null;
                var imageType = null;
			    
                var item = xmlDoc.getElementsByTagName("xpix")[0];            
                if (item) {
                    picWidth = item.firstChild.nodeValue;
                }
             
                item = xmlDoc.getElementsByTagName("ypix")[0];            
                if (item) {
                    picHeight = item.firstChild.nodeValue;
                }
			    
                item = xmlDoc.getElementsByTagName("imageType")[0];            
                if (item) {
                	imageType = item.firstChild.nodeValue;
                }
			    
			    if ((picWidth != null) && (picHeight != null)) {
			        pixDim.innerHTML = picWidth + " x " + picHeight + " pix";
			        
			        var pic = document.getElementById(picId);
			        if (pic) {
			        	pic.setAttribute("origWidth", picWidth);
			        	pic.setAttribute("origHeight", picHeight);
			        	if (imageType) {
			        		pic.setAttribute("imgType", imageType);
			        	}
			        } 
			    }
            } else {
                alert(resourceBundle["alert.communicationFailure"]);
            }
        }
    });
}

function isScrolledIntoView(el, view) {
	var contScrollPos = view.scrollTop;

	var contOffset = view.getBoundingClientRect().top;

	var elemTop = el.getBoundingClientRect().top - contOffset + contScrollPos;
    var elemBottom = el.getBoundingClientRect().bottom - contOffset + contScrollPos;
	
    return (elemBottom >= contScrollPos) && (elemTop <= contScrollPos + view.clientHeight);
}

function isScrolledOutOfView(el, view) {
	var contScrollPos = view.scrollTop;

	var contOffset = view.getBoundingClientRect().top;

	var elemTop = el.getBoundingClientRect().top - contOffset + contScrollPos;
    var elemBottom = el.getBoundingClientRect().bottom - contOffset + contScrollPos;

    return (elemBottom < contScrollPos - 80) || (elemTop > contScrollPos + view.clientHeight + 80);
}

function loadThumbnail(pic, thumbFileSrc) {

	pic.onload = function() {

		var picOrigWidth = getNaturalWidth(pic);
		var picOrigHeight = getNaturalHeight(pic);
		
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

        checkThumbnailsToLoad();
	};

	pic.src = thumbFileSrc;
}

function setThumbContHeight() {
    if (browserMSIE) {
        setTimeout('setThumbContHeightInternal()', 200);
    } else {
    	setThumbContHeightInternal();
    }
}

function setThumbContHeightInternal() {
    var windowHeight;

    if (browserFirefox || (browserChrome && osAndroid)) {
        windowHeight = window.innerHeight;
    } else {
        windowHeight = document.documentElement.clientHeight;
    }

    var offset = 200;
    
    var folderMetaInfElem = document.getElementById("folderMetaInf");
    if (folderMetaInfElem) {
    	offset += folderMetaInfElem.offsetHeight;
    }
    
    document.getElementById("scrollAreaCont").style.height = (windowHeight - offset) + 'px';
}

function picturePopupInFrame(filePath, picIdx) {
    var pic = document.getElementById("pic-" + picIdx);
    if (pic) {
    	var origWidth = pic.getAttribute("origWidth");
    	var origHeight = pic.getAttribute("origHeight");
        showPicturePopup('/webfilesys/servlet?command=getFile&filePath=' + encodeURIComponent(filePath), origWidth, origHeight);
    }
}

function picContextMenu(fileName, picIdx) {
    var pic = document.getElementById("pic-" + picIdx);
    if (pic) {
    	var imageType = pic.getAttribute("imgType");
    	if (imageType) {
    	    jsContextMenu(fileName, imageType, picIdx);
    	}
    }
}

function picLinkMenu(fileName, realPathForScript, picIdx) {
    var pic = document.getElementById("pic-" + picIdx);
    if (pic) {
    	var imageType = pic.getAttribute("imgType");
    	if (imageType) {
	        linkGraphicsMenu(fileName, realPathForScript, imageType);
    	}
    }
}

function removeDeletedFile(fileName) {
    var normalizedFileName = escapeForId(fileName);
    
    var thumbContToRemove = document.getElementById("thumbCont-" + normalizedFileName);
    if (thumbContToRemove) {
        thumbContToRemove.parentNode.removeChild(thumbContToRemove);
        
        checkThumbnailsToLoad();
        
        updateFileCount();   
       
       	if (typeof stopPolling == "function") {
       		stopPolling();
        }
             
    }
}

function updateFileCount() {
    var fileNumberElem = document.getElementById("fileNumber");
    if (fileNumberElem) {
        var oldFileCountAttr = fileNumberElem.getAttribute("fileNumber");
        if (oldFileCountAttr) {
            var oldFileCount = parseInt(oldFileCountAttr);
            if (oldFileCount != NaN) {
                var newFileCount = oldFileCount - 1;
                if (newFileCount >= 0) {
                    fileNumberElem.innerHTML = newFileCount;
                    fileNumberElem.setAttribute("fileNumber", newFileCount);
                    
                    var sizeSumElem = document.getElementById("sizeSum");
                    if (sizeSumElem) {
                        sizeSumElem.innerHTML = "";
                    }
                }
            }
        }
    }
}

function handleRenameKeyPress(e) {
    e = e || window.event;
    if (e.keyCode == 13) {
        // catch Enter key
        return false;
    }
    return true;
}

function validateNewFileNameAndRename(oldFileName, errorMsg1, errorMsg2) {
	var renameForm = document.getElementById('renameForm');
	
    var newFileName = renameForm.newFileName.value;

    if (newFileName == oldFileName) {
        customAlert(errorMsg1);
        return;
    }
    if (!checkFileNameSyntax(newFileName)) {
        customAlert(errorMsg2);
        return;
    } 
    if (trim(newFileName).length == 0) {
    	return
    }
    
    var domId = renameForm.domId.value;
    
	var formData = getFormData(renameForm);
	
	xmlRequestPost("/webfilesys/servlet", formData, function(req) {
        if (req.readyState == 4) {
            if (req.status == 200) {
			    var xmlDoc = req.responseXML;
			    
	            var successItem = req.responseXML.getElementsByTagName("success")[0];            
	            var success = successItem.firstChild.nodeValue;
			    
                if (success == 'true') {
                	if (typeof stopPolling == "function") {
                		stopPolling();
                    }

                	var fileNameElem = document.getElementById("fileName-" + domId);
                	if (fileNameElem) {
                		fileNameElem.innerHTML = abbrevText(newFileName, 23);
                	}

                    var checkboxElem = document.getElementById("cb-" + domId);
                    if (checkboxElem) {
                        checkboxElem.setAttribute("name", "list-" + newFileName);
                    }

                	var newFilePathItem = req.responseXML.getElementsByTagName("filePath")[0];
                	var newFilePath = newFilePathItem.firstChild.nodeValue;
                	
                	var thumbLink = document.getElementById("thumb-" + domId);
                	if (thumbLink) {
                		thumbLink.setAttribute("href", "javascript:showImage('" + insertDoubleBackslash(newFilePath) + "');hidePopupPicture()");
                	    thumbLink.setAttribute("oncontextmenu", "picturePopupInFrame('" + insertDoubleBackslash(newFilePath) + "', '" + domId + "');return false;");
                	}
                	
                	var contextMenuLink = document.getElementById("fileName-" + domId);
                    if (contextMenuLink) {
                    	contextMenuLink.setAttribute("href", "javascript:picContextMenu('" + newFileName + "', '" + domId + "');");
                    	contextMenuLink.setAttribute("oncontextmenu", "picturePopupInFrame('" + insertDoubleBackslash(newFilePath) + "', '" + domId + "');return false;");
                    }
                	
                	var thumbContElem = document.getElementById("thumbCont-" + oldFileName);
                	if (thumbContElem) {
                	    thumbContElem.id = "thumbCont-" + newFileName;
                	}
                }			    
            } else {
                alert(resourceBundle["alert.communicationFailure"]);
            }
        }
        
        document.getElementById("prompt").style.visibility = "hidden";
    });
}

function handleThumbRangeSelection(evt) {
    var clickTarget;
    if (!evt) {
        evt = window.event;
    }
    if (evt.target) {
        clickTarget = evt.target;
    } else if (evt.srcElement) {
        clickTarget = evt.srcElement;
    } else {
        return;
    }
    
    if (!clickTarget.checked) {
    	return;
    }

    if (!evt.shiftKey) {
    	return;
    }
    
    var clickedThumbnailCont = clickTarget.parentNode;
    
    var currentThumbnailCont = getPrevSiblingElement(clickedThumbnailCont);
    
    var stop = false;
    while (currentThumbnailCont && (!stop)) {
		var checkbox = getThumbnailCheckbox(currentThumbnailCont);
        if (checkbox.checked) {
        	stop = true;
        }
        
        if (!stop) {
            currentThumbnailCont = getPrevSiblingElement(currentThumbnailCont);
        }
    }
    
    if (stop) {
    	while (currentThumbnailCont != clickedThumbnailCont) {
    		currentThumbnailCont = getNextSiblingElement(currentThumbnailCont);
    		
    		var checkbox = getThumbnailCheckbox(currentThumbnailCont);
    		if (checkbox) {
    			if (!checkbox.disabled) {
        			checkbox.checked = true;
    			}
    		}
    	}
    	return;
    } 

    currentThumbnailCont = getNextSiblingElement(clickedThumbnailCont);
    
    stop = false;
    while (currentThumbnailCont && (!stop)) {
		var checkbox = getThumbnailCheckbox(currentThumbnailCont);
        if (checkbox.checked) {
        	stop = true;
        }
        
        if (!stop) {
            currentThumbnailCont = getNextSiblingElement(currentThumbnailCont);
        }
    }
    
    if (stop) {
    	while (currentThumbnailCont != clickedThumbnailCont) {
    		currentThumbnailCont = getPrevSiblingElement(currentThumbnailCont);
    		
    		var checkbox = getThumbnailCheckbox(currentThumbnailCont);
    		if (checkbox) {
    			if (!checkbox.disabled) {
        			checkbox.checked = true;
    			}
    		}
    	}
    } 
}

function getThumbnailCheckbox(thumbnailCont) {
    var children = thumbnailCont.childNodes;
    
    for (i = 0; i < children.length; i++) {
    	if (children[i].nodeType == 1) {
            if (children[i].nodeName.toLowerCase()  == 'input') {
            	return children[i];
            }
    	}
    }
    
    return null;
}

function scrollToPicture(picId) {
    setTimeout(function() {
		var picCont = document.getElementById("thumbCont-" + picId);
		if (picCont) {
			picCont.scrollIntoView();
	    }
	}, 200);
}