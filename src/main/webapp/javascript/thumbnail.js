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
    } else if (cmd == 'download') {
        multiImageDownload();
    } else if (cmd == 'exifRename') {
        renameToExifDate();
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

function compare() {
    if (checkSelected()) {
	    compareWin = window.open('/webfilesys/servlet?command=blank','compareWin','scrollbars=no,resizable=yes,status=no,menubar=no,toolbar=no,location=no,directories=no,screenX=0,screenY=0,left=0,top=0');
        
        if (!compareWin) {
            alert(resourceBundle["alert.enablePopups"]);
            return;
        }
        
        compareWin.focus();
        document.form2.command.value = 'compareImg';
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
}

function anySelected() {
    for (var i = document.form2.elements.length - 1; i >= 0; i--) {
         if ((document.form2.elements[i].type == "checkbox") && document.form2.elements[i].checked) {
	         return(true);
	     }
    }

    return(false);
}

function checkSelected() {
    var numChecked = 0;
    
    for (var i = document.form2.elements.length - 1; i >= 0; i--) {
         if ((document.form2.elements[i].type == "checkbox") && document.form2.elements[i].checked) {
	         numChecked++;
         }
    }
    
    if (numChecked < 2) {
        alert(selectTwoPic + '!');
	    return(false);
    }
    
    return(true);
}

function rotate(degree) {
    if (anySelected()) {
        document.form2.command.value = 'multiTransform';
	    document.form2.degrees.value = degree;
        document.form2.submit();
    } else {
        alert(selectOnePic + '!');
    }
}

function resize() {
    if (anySelected()) {
	    document.form2.command.value = 'resizeParms';
        document.form2.submit();
    } else {   
        alert(selectOnePic + '!');
    }
}

function multiImageCopyMove() {
    if (anySelected()) {
        document.form2.command.value = 'multiImageCopyMove';
        xmlRequestPost("/webfilesys/servlet", getFormData(document.form2), showCopyResult);
	    document.form2.command.value = 'compareImg';
    } else {   
        alert(selectOnePic + '!');
    }
}

function multiImageDelete() {
    if (anySelected()) {
        if (confirm(resourceBundle["confirm.deleteImages"])) {
	        document.form2.command.value = 'multiImageDelete';
            document.form2.submit();
	    }
        document.form2.command.value = 'compareImg';
    } else {   
        alert(selectOnePic + '!');
    }
}

function renameToExifDate() {
    if (anySelected()) {
	    document.form2.command.value = 'multiImageExifRename';
        document.form2.submit();
	    document.form2.command.value = 'compareImg';
    } else {   
        alert(selectOnePic + '!');
    }
}

function multiImageDownload() {
    if (anySelected()) {
        document.form2.command.value = 'multiImgDownload';
        document.form2.submit();
    } else {   
        alert(selectOnePic + '!');
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
    
    xmlRequest(url, function() {
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

