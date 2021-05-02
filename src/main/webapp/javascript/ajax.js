function cutCopyToClip(fileName, operation)
{
    url = '/webfilesys/servlet?command=cutCopy&fileName=' + encodeURIComponent(fileName) + '&cmd=' + operation;

    xmlRequest(url, showCopyResult);
}

function editLocal(fileName)
{
    url = "/webfilesys/servlet?command=editFile&fileName=" + encodeURIComponent(fileName);

    xmlRequest(url, ignoreResult);
}

function editLocalLink(filePath)
{
    url = "/webfilesys/servlet?command=editFile&filePath=" + encodeURIComponent(filePath);

    xmlRequest(url, ignoreResult);
}

function showCopyResult(req)
{
    if (req.readyState == 4)
    {
        if (req.status == 200)
        {
             var item = req.responseXML.getElementsByTagName("message")[0];            
             var message = item.firstChild.nodeValue;
             
             hideMenu();
        
             toast(message, 2000);

             if (req.responseXML.getElementsByTagName("copyOperation").length > 0) 
             {
                 addCopyAllowed = true;
                 addMoveAllowed = false;
                 if (document.getElementById("copyAddOption")) 
                 {
                     document.getElementById("copyAddOption").style.display = "inline";
                     document.getElementById("copyAddOption").disabled = false;
                     document.getElementById("moveAddOption").style.display = "none";
                     document.getElementById("moveAddOption").disabled = true;
                 }
             }
             if (req.responseXML.getElementsByTagName("moveOperation").length > 0) 
             {
                 addMoveAllowed = true;
                 addCopyAllowed = false;
                 if (document.getElementById("moveAddOption")) 
                 {
                     document.getElementById("moveAddOption").style.display = "inline";
                     document.getElementById("moveAddOption").disabled = false;
                     document.getElementById("copyAddOption").style.display = "none";
                     document.getElementById("copyAddOption").disabled = true;
                 }
             }

             if (req.responseXML.getElementsByTagName("enablePaste").length > 0) 
             {
                 if (document.getElementById("pasteButton"))
                 {
                     document.getElementById("pasteButton").style.display = "inline";
                 }
             }

             if (req.responseXML.getElementsByTagName("enablePasteAsLink").length > 0) 
             {
                 if (document.getElementById("pasteLinkButton"))
                 {
                     document.getElementById("pasteLinkButton").style.display = "inline";
                 }
             }
             
             if (parent && parent.parent && parent.parent.frames[1]) 
             {
                 parent.parent.frames[1].clipboardEmpty = false;
             }
        }
    }
}

function ignoreResult(req)
{
    hideMenu();
}

function reloadPage()
{
    window.location.href = '/webfilesys/servlet?command=listFiles';
}

function hideMsg()
{
     msgBox1 = document.getElementById("msg1");
     msgBox1.style.visibility = "hidden";
}

function diffSelect(path)
{
    parent.diffStarted = true;

    url = "/webfilesys/servlet?command=diffSelect&path=" + encodeURIComponent(path);

    xmlRequest(url, diffSelectResult);
}

function diffSelectResult(req)
{
    if (req.readyState == 4)
    {
        if (req.status == 200)
        {
             var item = req.responseXML.getElementsByTagName("success")[0];            
             var result = item.firstChild.nodeValue;
             
             hideMenu();

             if (result == 'targetSelected')
             {
                 if (openDiffWindow())
                 {
                     parent.diffStarted = false;
                 }
                 else
                 {
                     alert("Unable to open a popup window for the compare result. Please check your browser settings for popups!");
                 }
                 return;
             }

             item = req.responseXML.getElementsByTagName("message")[0];            
             var message = item.firstChild.nodeValue;
             
             toast(message, 4000);
        }
    }
}

function cancelDiff() {
    var url = "/webfilesys/servlet?command=diffSelect&cmd=deselect";

    xmlRequest(url, function(req) {
        if (req.readyState == 4) {
            if (req.status != 200) {
                alert(resourceBundle["alert.communicationFailure"]);
            } else {
                parent.diffStarted = false;
            }
    
            hideMenu();    
            stopMenuClose = true;
        }
    });
}

function openDiffWindow()
{
    diffWin = window.open('/webfilesys/servlet?command=startDiff','diffWin','width=' + (screen.width - 20) + ',height=' + (screen.height - 80) + ',scrollbars=yes,resizable=yes,status=no,menubar=no,toolbar=no,location=no,directories=no,screenX=0,screenY=0,left=0,top=0');
    if (diffWin) 
    {
        diffWin.focus();
        return true;
    }
    return false;
}

function touch(fileName) {
    const parameters = { "fileName": encodeURIComponent(fileName) };
    
	xmlPostRequest("touch", parameters, function(responseXml) {
        var item = responseXml.getElementsByTagName("success")[0];            
        var success = item.firstChild.nodeValue;
             
        hideMenu();

        if (success == 'true') {
            reloadPage();
            return;
        }

        item = req.responseXML.getElementsByTagName("message")[0];            
        var message = item.firstChild.nodeValue;

        toast(message, 3000);
    });
}

function sendFileViaEmail()  {
	
    if (validateEmailList(document.emailForm.receiver.value)) {
	
        xmlPostRequest(null, getFormDataAsProps(document.emailForm), function(responseXml) {
            let item = responseXml.getElementsByTagName("result")[0];            
            const success = item.firstChild.nodeValue;

            item = responseXml.getElementsByTagName("message")[0];            
            const message = item.firstChild.nodeValue;

            toast(message, 3000);
        });
	    hidePrompt();
    } else {
	    customAlert(resourceBundle["alert.emailsyntax"]);
    }
}

function checkPasteOverwrite(path) {
	
	showHourGlass();
	
    var url = "/webfilesys/servlet?command=checkPasteOverwrite";
    
    if (path) {
        url = url + "&path=" + encodeURIComponent(path);
    }
    	
    xmlRequest(url, checkPasteOverwriteResult);
}

function checkPasteOverwriteResult(req) {
    if (req.readyState == 4) {
        if (req.status == 200) {

        	var pasteUrl = "/webfilesys/servlet?command=pasteFiles";
        	
            var pathElems = req.responseXML.getElementsByTagName("path");
            if (pathElems && (pathElems.length == 1)) {
            	var path = pathElems[0].firstChild.nodeValue;
            	pasteUrl = pasteUrl + "&actpath=" + encodeURIComponent(path);
            }
        	
            var destFolderFileConflictElem = req.responseXML.getElementsByTagName("destFolderFileConflict");
            if (destFolderFileConflictElem && (destFolderFileConflictElem.length > 0)) {
            	hideHourGlass();
            	customAlert(resourceBundle["destFolderFileConflict"]);
                return;
            }
            
            var targetEqualsSourceElem = req.responseXML.getElementsByTagName("targetEqualsSource");
            if (targetEqualsSourceElem && (targetEqualsSourceElem.length > 0)) {
            	hideHourGlass();
            	customAlert(resourceBundle["targetEqualsSource"]);
                return;
            }

            var targetIsSubOfSourceElem = req.responseXML.getElementsByTagName("targetIsSubOfSource");
            if (targetIsSubOfSourceElem && (targetIsSubOfSourceElem.length > 0)) {
            	hideHourGlass();
            	customAlert(resourceBundle["targetIsSubOfSource"]);
                return;
            }
            
            var conflicts = req.responseXML.getElementsByTagName("conflict");            

            if (conflicts.length > 0) {
            	hideHourGlass();

                var msg;
                var folderElem = req.responseXML.getElementsByTagName("folder");
                if (folderElem && (folderElem.length > 0)) {
                    msg = resourceBundle["pasteConflictHeadFolder"];
                } else {
                    msg = resourceBundle["pasteConflictHead"];
                }
            	
                msg += "<br/>"; 
                
                for (var i = 0; i < conflicts.length; i++) {
                	msg = msg + "<br/>" + conflicts[i].firstChild.nodeValue;
                }

                msg = msg + "<br/><br/>" + resourceBundle["pasteOverwrite"];
                 
            	customConfirm(msg, resourceBundle["button.cancel"], resourceBundle["button.ok"], 
            			function() {
            		        showHourGlass();
                            window.location.href = pasteUrl;
            	        }
            	);
            } else {
                window.location.href = pasteUrl;
            }
        } else {
        	hideHourGlass();
            alert(resourceBundle["alert.communicationFailure"]);
        }
    }
}

function delFileAjax(path, deleteWriteProtected) {
	
    const parameters = { "filePath": encodeURIComponent(path) };
    
    if (deleteWriteProtected) {
        parameters["deleteWriteProtected"] = "true";
    }

    xmlPostRequest("delFile", parameters, function(responseXml) {
        var successItem = responseXml.getElementsByTagName("success")[0];
        var success = successItem.firstChild.nodeValue;  
                
        if (success == "true") {
            var deletedFileItem = responseXml.getElementsByTagName("deletedFile")[0];
            var deletedFile = deletedFileItem.firstChild.nodeValue; 
               
            var viewModeItem = responseXml.getElementsByTagName("viewMode")[0];
            if ((viewModeItem) && (viewModeItem.firstChild.nodeValue == 2)) {
            	if (typeof stopPolling == "function") {
            		stopPolling();
                }
                removeDeletedFile(deletedFile);
                hidePrompt();
                return;
            }                    
        } else {
            alert(resourceBundle["alert.delFileError"]);
        }
        window.location.href = "/webfilesys/servlet?command=listFiles";                
    });          
}
