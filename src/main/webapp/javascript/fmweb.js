function selectAll() {
    var allSelected = true;
	
	var fileCheckboxes = new Array();
	
    for (var i = document.form1.elements.length - 1; i >= 0; i--) {
        if ((document.form1.elements[i].type == "checkbox") &&
		    (document.form1.elements[i].name != "cb-confirm") &&
            (document.form1.elements[i].name != "cb-setAll")) {
			fileCheckboxes.push(document.form1.elements[i]);
	        if ((!document.form1.elements[i].checked) &&
	            (!document.form1.elements[i].disabled)) {
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
    
    return (!allSelected);
}

function addDeselectHandler() {
    for (var i = document.form1.elements.length - 1; i >= 0; i--) {
        if ((document.form1.elements[i].type == "checkbox") &&
		    (document.form1.elements[i].name != "cb-confirm") &&
            (document.form1.elements[i].name != "cb-setAll")) {
			document.form1.elements[i].addEventListener ('click', handleCheckboxClick, true);
	    } 
    }
}

function handleCheckboxClick(evt) {
    var clickEvent = evt;
    if (!clickEvent) {
	    clickEvent = window.event;
    }
    
    var clickTarget = clickEvent.target;
    
    if (!clickTarget.checked) {
        document.getElementById("cb-setAll").checked = false;
    }
}

function multiDownload() {
    document.form1.command.value = 'multiDownload';
    document.form1.submit();
}

function setDependendCheckbox(prereq,dependent)
{
   if (prereq.checked==false)
   {
       dependent.checked=false;
   }
}

function setRelatedCheckbox(master,dependent)
{
   if (master.checked)
   {
       dependent.checked=true;
   }
}

function anySelected() {
    for (var i = document.form1.elements.length - 1; i >= 0; i--) {
        if ((document.form1.elements[i].type == "checkbox") &&
            document.form1.elements[i].checked &&
            (document.form1.elements[i].name != 'cb-confirm')) {
	        return(true);
	    }
    }

    return(false);
}

function resetSelected() {
    for (var i = document.form1.elements.length - 1; i >= 0; i--) {
	    if ((document.form1.elements[i].type == "checkbox") && document.form1.elements[i].checked) {
	        document.form1.elements[i].checked = false;
        }
    }
}

function multiFileCopyMove()
{
    document.form1.command.value='multiFileCopyMove';

    xmlRequestPost("/webfilesys/servlet", getFormData(document.form1), showCopyResult);
    
    document.form1.command.value='multiFileOp';
}

function diffCompare() {
    if (checkTwoFilesSelected()) {
	    var compareWin = window.open('/webfilesys/servlet?command=blank','compareWin','width=' + (screen.width - 20) + ',height=' + (screen.height - 80) + ',scrollbars=yes,resizable=yes,status=no,menubar=no,toolbar=no,location=no,directories=no,screenX=0,screenY=0,left=0,top=0');
        compareWin.focus();
        document.form1.command.value = 'diff';
        document.form1.target = 'compareWin';
        
	    document.form1.submit();
        document.form1.target = '';
    }
}

function showMultipleGPX() {

    for (var i = document.form1.elements.length - 1; i >= 0; i--) {
         if ((document.form1.elements[i].type == "checkbox") && 
		     (document.form1.elements[i].name != "cb-setAll") &&
		     document.form1.elements[i].checked) {
	         if (getFileNameExt(document.form1.elements[i].name) != ".GPX") {
	             customAlert(resourceBundle["nonGPXFile"]);
	             return;
	         }
         }
    }


    var mapWin = window.open('/webfilesys/servlet?command=blank','mapWin','width=' + (screen.width - 20) + ',height=' + (screen.height - 110) + ',scrollbars=yes,resizable=yes,status=no,menubar=no,toolbar=no,location=no,directories=no,screenX=0,screenY=0,left=0,top=0');
    mapWin.focus();
    document.form1.command.value = 'multiGPX';
    document.form1.target = 'mapWin';
    
    document.form1.submit();
    document.form1.target = '';
}

function checkTwoFilesSelected() {
    var numChecked = 0;
    
    for (var i = document.form1.elements.length - 1; i >= 0; i--) {
         if ((document.form1.elements[i].type == "checkbox") && 
		     (document.form1.elements[i].name != "cb-setAll") &&
		     document.form1.elements[i].checked) {
	         numChecked++;
         }
    }
    
    if (numChecked != 2) {
        customAlert(resourceBundle["selectTwoFilesForDiff"]);
	    return(false);
    }
    
    return(true);
}

function selectedFileFunction(unhighlight) {
    if (!anySelected()) {
        document.form1.cmd.selectedIndex = 0;
        customAlert(noFileSelected + '!');
        return;
    }

    var idx = document.form1.cmd.selectedIndex;

    var cmd = document.form1.cmd.options[idx].value;

    if (cmd == 'delete') {
    	customConfirm(resourceBundle["confirm.deleteFiles"], resourceBundle["button.cancel"], resourceBundle["button.ok"], 
    			function() {
                    document.form1.submit();
    	        },
    			function() {
    	        	resetMultifileSelection(unhighlight);
    	        	closeAlert();
    	        }
    	);
    	
    	return;
    }

    if ((cmd == 'zip') || (cmd == 'tar')) {
        document.form1.submit();
        return;
    }

    if ((cmd == 'copy') || (cmd == 'copyAdd') || (cmd == 'move') || (cmd == 'moveAdd')) {
        multiFileCopyMove();
    } else if (cmd == 'download') {
	    multiDownload();
    } else if (cmd == 'diff') {
	    diffCompare();
    } else if (cmd == 'multiGPX') {
	    showMultipleGPX();
    }
     
    resetMultifileSelection(unhighlight);
}

function resetMultifileSelection(unhighlight) {
    document.form1.command.value = 'multiFileOp';
    document.form1.cmd.selectedIndex = 0;
		
    resetSelected();

    if (unhighlight) {
        setAllFilesUnselected();
    }
}

function showMsgCentered(message, boxWidth, boxHeight, duration)
{
    var msgBox1 = document.getElementById("msg1");
        
    msgBox1.innerHTML = message;
    
    centerBox(msgBox1);

    msgBox1.style.visibility = "visible";
             
    setTimeout("hideMsgBox()", duration);
}

function hideMsgBox()
{
     msgBox1 = document.getElementById("msg1");
     msgBox1.style.visibility = "hidden";
}

function checkFileNameSyntax(str)
{
    var prevIsDot = false;

    for (i = 0; i < str.length; i++) 
    {
        c = str.charAt(i);
       
        if ((c == '\'') || (c == '\"') || (c == '*') || (c == '/') || (c == '\\') ||
            (c == '%') || (c == ':') || (c == '+')  || (c == '#') || (c == ';') ||
            (c == ',') || (c == '�') || (c == '&') || (c == '?') || (c == '@'))
        {
            return(false);
        } 
        
        if (c == '.')
        {
            if (prevIsDot)
            {
                return false;
            }
            prevIsDot = true;
        }
        else
        {
            prevIsDot = false;
        }
    }

    return(true);
}

function validateNewFileName(oldFileName, errorMsg1, errorMsg2) {
    var newFileName = document.getElementById('renameForm').newFileName.value;

    if (newFileName == oldFileName) {
        alert(errorMsg1);
    } else {
        if (!checkFileNameSyntax(newFileName)) {
            alert(errorMsg2);
        } else {
            if (newFileName != '') {
                document.renameForm.submit();
            }
        }
    }
}

function validateCloneFolderName() {
    var sourceFolderName = document.getElementById("sourceFolderName").value
    var newFolderName = document.getElementById('renameForm').newFolderName.value;
    
    if (newFolderName == sourceFolderName) {
        customAlert(resourceBundle['alert.destFolderEqualsSource']);
    } else {
        if (!checkFileNameSyntax(newFolderName)) {
            customAlert(resourceBundle['alert.illegalCharInFilename']);
        } else {
            if (newFolderName.trim().length == 0) {
                customAlert(resourceBundle["alert.newFolderNameEmpty"]);
            } else {
                var sourceFolderPath = document.getElementById("sourceFolderPath").value;
            	var pathSeparator = "/";
            	if (sourceFolderPath.indexOf("\\") > 0) {
            		pathSeparator = "\\";
            	}

                var sourceFolderParentPath = sourceFolderPath.substring(0, sourceFolderPath.lastIndexOf(pathSeparator));
                var targetFolderPath = sourceFolderParentPath + pathSeparator + newFolderName;
                
                var ajaxUrl = "/webfilesys/servlet?command=ajaxRPC&method=existFolder&param1=" + encodeURIComponent(targetFolderPath);
                
                showHourGlass();                
                
            	xmlRequest(ajaxUrl, function(req) {
                    if (req.readyState == 4) {
                        if (req.status == 200) {
                            var subdirExists = req.responseXML.getElementsByTagName("result")[0].firstChild.nodeValue;        
                            if (subdirExists == "false") {
                                document.renameForm.submit();
                            } else {
               	                customAlert(resourceBundle["alert.cloneTargetFolderExists"]);
               	                hideHourGlass();
                            }
                        } else {
                            alert(resourceBundle["alert.communicationFailure"]);
                            hideHourGlass();
        	            }
                    }
            	});
            }
        }
    }

    document.getElementById('renameForm').newFolderName.focus();
    document.getElementById('renameForm').newFolderName.select();
}

function validateNewFolderName(errorMsg) {
    var newDirName = document.mkdirForm.NewDirName.value;

    if (checkFileNameSyntax(newDirName)) {
        if (newDirName != '') {
            document.mkdirForm.submit();
        }
        return;
    }
    
    alert(errorMsg);

    document.mkdirForm.NewDirName.focus();

    document.mkdirForm.NewDirName.select();
}

function validateBookmarkName(errorMsg) {
    var bookmarkName = document.bookmarkForm.bookmarkName.value;

    if (bookmarkName.trim().length == 0) {
        alert(errorMsg);
        document.bookmarkForm.bookmarkName.focus();
        document.bookmarkForm.bookmarkName.select();
    } else {
        var createBookmarkUrl = '/webfilesys/servlet?command=createBookmark&path=' + encodeURIComponent(document.bookmarkForm.currentPath.value) + '&bookmarkName=' + encodeURIComponent(document.bookmarkForm.bookmarkName.value);
        xmlRequest(createBookmarkUrl, function(req) {
            if (req.readyState == 4) {
                if (req.status == 200) {
                    toast(resourceBundle["alert.bookmarkCreated"], 2000);
                } else {
                    alert(resourceBundle["alert.communicationFailure"]);
                }
                hidePrompt();
            }
        });
    }
}

function validateCreateFileName(errorMsg) {
    var newFileName = document.mkfileForm.NewFileName.value;

    if (checkFileNameSyntax(newFileName)) {
        if (newFileName != '') {
            document.mkfileForm.submit();
        }
        return;
    }
    
    alert(errorMsg);
    document.mkfileForm.NewFileName.focus();
    document.mkfileForm.NewFileName.select();
}

function submitSwitchReadWrite()
{
    document.swtichReadWriteForm.submit();
}

function switchFolderWatch(path) {
    var url = "/webfilesys/servlet?command=switchFolderWatch&path=" + encodeURIComponent(path);
    
    xmlRequest(url, function(req) {
        if (req.readyState == 4) {
            if (req.status != 200) {
                alert(resourceBundle["alert.communicationFailure"]);
            }
            hidePrompt();
        }
    });
}

function enableDisablePatternInput()
{
    var excludePattern = document.getElementById('excludePattern');
    if (!excludePattern)
    {
        return;
    }

    if (excludePattern.disabled)
    {
        excludePattern.disabled = false;
    }
    else
    {
        excludePattern.disabled = true;
    }
}

function bookmark(path) {
    if (path && (path.length > 0)) {
        centeredDialog('/webfilesys/servlet?command=addBookmark&path=' + encodeURIComponent(path), '/webfilesys/xsl/addBookmark.xsl', 320, 190, function() {
            document.bookmarkForm.bookmarkName.focus();
            document.bookmarkForm.bookmarkName.select();
        });    
    } else {
        centeredDialog('/webfilesys/servlet?command=addBookmark', '/webfilesys/xsl/addBookmark.xsl', 320, 190, function() {
            document.bookmarkForm.bookmarkName.focus();
            document.bookmarkForm.bookmarkName.select();
        });
    }
}

function fastpath(path) {
	showHourGlass();

	window.location.href = "/webfilesys/servlet?command=exp&expandPath=" + encodeURIComponent(path) + "&mask=*&fastPath=true";
}

function hidePrompt() {
     var promptBox = document.getElementById("prompt");
     
     if (!promptBox) {
         return;
     }

     promptBox.style.visibility = "hidden";
     promptBox.style.width = "100px";
     promptBox.style.height = "140px";
}

function centeredDialog(xmlUrl, xslUrl, boxWidth, boxHeight, callback) {
    var promptBox = document.getElementById("prompt");
        
    if (!promptBox) {
        alert('promptBox is not defined');
        return;
    }
        
    hideMenu();        
    
    if (boxWidth) {
        promptBox.style.width = boxWidth + 'px';
    }

    if (boxHeight) {
        promptBox.style.height = boxHeight + 'px';
    }
        
    htmlFragmentByXslt(xmlUrl, xslUrl, promptBox, function() {
        setBundleResources();
        centerBox(promptBox);
        promptBox.style.visibility = "visible";

        if (callback) {
            callback();
        }
    });
}

function showPromptDialog(htmlFragmentURL, boxWidth, callback) {
    var promptBox = document.getElementById("prompt");
        
    hideMenu();        

    if (boxWidth) {    
        promptBox.style.width = boxWidth + 'px';
    }
    
    xmlRequest(htmlFragmentURL, function(req) {
        if (req.readyState == 4) {
            if (req.status == 200) {
                promptBox.innerHTML = req.responseText;
                setBundleResources(promptBox);
                centerBox(promptBox);
                promptBox.style.visibility = "visible";
                if (callback) {
                    callback();
                }
            } else {
                alert(resourceBundle["alert.communicationFailure"]);
            }
        }
    });
}

function renameLink(linkName) {
	showPromptDialog("/webfilesys/html/renameLink.html", 360, function() {	
	
	    document.getElementById("oldLinkName").value = linkName;
	    document.getElementById("oldLinkNameShort").innerHTML = shortText(linkName, 35);

	    var newLinkName = document.getElementById("newLinkName");
	    newLinkName.value = linkName;
	    newLinkName.focus();
	    newLinkName.select();
	});
}

function validateNewLinkName() {
	var newLinkName = document.getElementById("newLinkName").value;
	
	if (trim(newLinkName).length == 0) {
		alert(resourceBundle["alert.newLinkNameEmpty"]);
		document.getElementById("newLinkName").focus()
		return;
	}
	
	var oldLinkName = document.getElementById("oldLinkName").value;
		
	if (oldLinkName == newLinkName) {
		alert(resourceBundle["alert.destEqualsSource"]);
		document.getElementById("newLinkName").focus()
		return;
	}
		
	if (!checkFileNameSyntax(newLinkName)) {
		alert(resourceBundle["alert.illegalCharInFilename"]);
		document.getElementById("newLinkName").focus()
		return;
	}
	
	document.getElementById("renameLinkForm").submit();
}

function validateEmail(elementValue) {      
   var emailPattern = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/;
   
   return emailPattern.test(elementValue); 
}

function validateEmailList(addressList) {
    var addressArr = addressList.split(",");
    
    for (var i = 0; i < addressArr.length; i++) {
        if (!validateEmail(addressArr[i])) {
            return false;
        }
    }
    
    return true;
}

function checkGrepParamsAndSubmit() {
    if (document.getElementById("grepFilter").value.length == 0) {
        customAlert(resourceBundle["grepFilterMissing"]);
    } else {
        document.grepForm.submit();
        setTimeout('hidePrompt()', 1000);
    }
}