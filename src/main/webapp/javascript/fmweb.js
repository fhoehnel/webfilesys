function selectAll() {
    let allSelected = true;
	const fileCheckboxes = [];

    for (let i = 0; i < document.form1.elements.length; i++) {
        const formElem = document.form1.elements[i];
        if (formElem.type === "checkbox" &&
            formElem.name !== "cb-confirm" &&
            formElem.name !== "cb-setAll") {
            fileCheckboxes.push(formElem);
            if (!formElem.checked && !formElem.disabled) {
                allSelected = false;
            }
        }
    }

    if (allSelected) {
        fileCheckboxes.forEach(checkBox => checkBox.checked = false);
    } else {
        fileCheckboxes.forEach(checkBox => {
           if (!checkBox.disabled) {
               checkBox.checked = true;
           }
        });
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
    for (let i = document.form1.elements.length - 1; i >= 0; i--) {
        const formElem = document.form1.elements[i];
        if (formElem.type === "checkbox" &&
            formElem.checked &&
            formElem.name !== 'cb-confirm') {
	        return true;
	    }
    }
    return false;
}

function resetSelected() {
    for (let i = document.form1.elements.length - 1; i >= 0; i--) {
        const formElem = document.form1.elements[i];
	    if (formElem.type === "checkbox" && formElem.checked) {
            formElem.checked = false;
        }
    }
}

function multiFileCopyMove() {
    document.form1.command.value='multiFileCopyMove';
    xmlFetchPost(getFormData(document.form1), handleCopyResult);
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

function showMultipleGPX(command) {

    for (let i = document.form1.elements.length - 1; i >= 0; i--) {
        const formElem = document.form1.elements[i];
        if (formElem.type === "checkbox" &&
            formElem.name !== "cb-setAll" &&
            formElem.checked) {
	         if (getFileNameExt(formElem.name) !== ".GPX") {
	             customAlert(resourceBundle["nonGPXFile"]);
	             return;
	         }
         }
    }
    const mapWin = window.open('/webfilesys/servlet?command=blank','mapWin','width=' + (screen.width - 20) + ',height=' + (screen.height - 110) + ',scrollbars=yes,resizable=yes,status=no,menubar=no,toolbar=no,location=no,directories=no,screenX=0,screenY=0,left=0,top=0');
    mapWin.focus();
    document.form1.command.value = command;
    document.form1.target = 'mapWin';
    document.form1.submit();
    document.form1.target = '';
}

function checkTwoFilesSelected() {
    let numChecked = 0;
    
    for (let i = document.form1.elements.length - 1; i >= 0; i--) {
        const formElem = document.form1.elements[i];

        if (formElem.type === "checkbox" && formElem.name !== "cb-setAll" && formElem.checked) {
	        numChecked++;
        }
    }
    if (numChecked !== 2) {
        customAlert(resourceBundle["selectTwoFilesForDiff"]);
	    return false;
    }
    return true;
}

function selectedFileFunction(unhighlight) {
    if (!anySelected()) {
        document.form1.cmd.selectedIndex = 0;
        customAlert(noFileSelected + '!');
        return;
    }
    const idx = document.form1.cmd.selectedIndex;
    const cmd = document.form1.cmd.options[idx].value;

    if (cmd === 'delete') {
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

    if (cmd === 'zip' || cmd === 'tar') {
        document.form1.submit();
        return;
    }

    if (cmd === 'copy' || cmd === 'copyAdd' || cmd === 'move' || cmd === 'moveAdd') {
        multiFileCopyMove();
    } else if (cmd === 'download') {
	    multiDownload();
    } else if (cmd === 'diff') {
	    diffCompare();
    } else if (cmd === 'multiGPX') {
	    showMultipleGPX("multiGPX");
    } else if (cmd === 'multiGPXOSM') {
        showMultipleGPX("multiGPXOSM");
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

function validateCloneFileName() {
    const oldFileName = document.getElementById("sourceFileName").value;
    const newFileName = document.getElementById("newFileName").value;
    if (newFileName === oldFileName) {
        customAlert(resourceBundle["alert.destEqualsSource"]);
    } else {
        if (!checkFileNameSyntax(newFileName)) {
            customAlert(resourceBundle["alert.illegalCharInFilename"]);
        } else {
            if (newFileName !== '') {
                fetchPost(getFormData(document.getElementById("cloneForm")),
                    responseData => window.location.href = "/webfilesys/servlet?command=listFiles",
                    () => customAlert(resourceBundle["alert.cloneTargetExists"])
                );
            }
        }
    }
}

function validateRenameTargetFileName() {
    const oldFileName = document.getElementById("oldFileName").value;
    const newFileName = document.getElementById("newFileName").value;
    const mobile = document.getElementById("mobileParam").value;
    if (newFileName === oldFileName) {
        customAlert(resourceBundle["alert.destEqualsSource"]);
    } else {
        if (!checkFileNameSyntax(newFileName)) {
            customAlert(resourceBundle["alert.illegalCharInFilename"]);
        } else {
            if (newFileName !== '') {
                fetchPost(getFormData(document.getElementById("renameForm")),
                    responseData => {
                        if ("true" === mobileParam) {
                            window.location.href = "/webfilesys/servlet?command=mobile&cmd=folderFileList&initial=true";
                        } else {
                            window.location.href = "/webfilesys/servlet?command=listFiles"
                        }
                    },
                    () => customAlert(oldFileName + " " + resourceBundle["error.renameFailed"] + " " + newFileName)
                );
            }
        }
    }
}

function validateNewFileName(oldFileName, errorMsg1, errorMsg2) {
    const newFileName = document.getElementById('renameForm').newFileName.value;

    if (newFileName === oldFileName) {
        customAlert(errorMsg1);
    } else {
        if (!checkFileNameSyntax(newFileName)) {
            customAlert(errorMsg2);
        } else {
            if (newFileName !== '') {
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
                
                const parameters = { "method": "existFolder", "param1": encodeURIComponent(targetFolderPath) };
                
            	xmlGetRequest("ajaxRPC", parameters, function(responseXml) {
                    var subdirExists = responseXml.getElementsByTagName("result")[0].firstChild.nodeValue;        
                    if (subdirExists == "false") {
                        document.renameForm.submit();
                    } else {
       	                customAlert(resourceBundle["alert.cloneTargetFolderExists"]);
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
            return;
        } else {
            errorMsg = resourceBundle["error.emptyFolderName"];
        }
    }
    customAlert(errorMsg, null, () => {
        document.mkdirForm.NewDirName.focus();
        document.mkdirForm.NewDirName.select();
    });
}

function validateBookmarkName(errorMsg) {
    const bookmarkName = document.bookmarkForm.bookmarkName.value;
    if (bookmarkName.trim().length === 0) {
        customAlert(errorMsg);
        document.bookmarkForm.bookmarkName.focus();
        document.bookmarkForm.bookmarkName.select();
    } else {
        fetchPost(getFormData(document.getElementById("bookmarkForm")), responseData => {
            toast(resourceBundle["alert.bookmarkCreated"], 2000);
        });
        hidePrompt();
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
    
    customAlert(errorMsg);
    document.mkfileForm.NewFileName.focus();
    document.mkfileForm.NewFileName.select();
}

function submitSwitchReadWrite()
{
    document.swtichReadWriteForm.submit();
}

function switchFolderWatch(path) {
    const parameters = { "path": encodeURIComponent(path) };
    xmlGetRequest("switchFolderWatch", parameters, () => hidePrompt());
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
    showPromptDialog("/webfilesys/html/addBookmark.html", 320, function() {
        document.getElementById("prompt").style.height = "200px";
        document.getElementById("currentPathShort").innerHTML = abbrevText(path, 40);

        document.getElementById("submitButton").onclick = () => {
            validateBookmarkName(resourceBundle["alert.bookmarkMissingName"]);
        };
        document.getElementById("cancelButton").onclick = hidePrompt;

        document.bookmarkForm.bookmarkName.focus();
        document.bookmarkForm.bookmarkName.select();
    });
}

function fastpath(path) {
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

function popupDialog(domFragment, boxWidth, boxHeight, callback) {
    const promptBox = document.getElementById("prompt");
    if (!promptBox) {
        console.error("promptBox is not defined");
        return;
    }
    hideMenu();
    if (boxWidth) {
        promptBox.style.width = boxWidth + "px";
    }
    if (boxHeight) {
        promptBox.style.height = boxHeight + "px";
    }
    promptBox.innerHTML = domFragment;
    centerBox(promptBox);
    promptBox.style.visibility = "visible";
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

function showPromptDialog(htmlFragmentURL, boxWidth, callback, boxHeight) {
    var promptBox = document.getElementById("prompt");
        
    hideMenu();        

    if (boxWidth) {    
        promptBox.style.width = boxWidth + 'px';
    }
    if (boxHeight) {
        promptBox.style.height = boxHeight + 'px';
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

function renameFile(fileName, isMobile) {
    showPromptDialog("/webfilesys/html/renameFile.html", 360, function() {
        document.getElementById("oldFileName").value = fileName;
        document.getElementById("mobileParam").value = isMobile.toString();
        document.getElementById("shortFileName").innerHTML = abbrevText(fileName, 30);
        const newFileName = document.getElementById("newFileName");
        newFileName.value = fileName;
        newFileName.focus();
        const extStart = fileName.lastIndexOf(".");
        if (extStart > 0) {
            newFileName.setSelectionRange(0, extStart);
        } else {
            newFileName.select();
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
	
	if (trim(newLinkName).length === 0) {
        customAlert(resourceBundle["alert.newLinkNameEmpty"]);
		document.getElementById("newLinkName").focus()
		return;
	}
	
	var oldLinkName = document.getElementById("oldLinkName").value;
		
	if (oldLinkName === newLinkName) {
		customAlert(resourceBundle["alert.destEqualsSource"]);
		document.getElementById("newLinkName").focus()
		return;
	}
		
	if (!checkFileNameSyntax(newLinkName)) {
        customAlert(resourceBundle["alert.illegalCharInFilename"]);
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

function copyPathToClipboard(path) {
    navigator.clipboard.writeText(path);
    toast(resourceBundle["pathCopiedToClip"], 1500);
}

function clearFilter() {
	document.getElementById("fileMask").value = "*";
	document.sortform.submit();
}