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
	    compareWin = window.open('/webfilesys/servlet?command=blank','compareWin','width=screen.width,height=screen.height-30,scrollbars=no,resizable=yes,status=no,menubar=no,toolbar=no,location=no,directories=no,screenX=0,screenY=0,left=0,top=0');
        
        if (!compareWin) {
            alert('Failed to open new browser window. You have to allow popups for this website in your browser settings!');
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

