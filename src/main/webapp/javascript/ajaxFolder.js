// var browserIsFirefox = /a/[-1]=='a';
var browserIsFirefox = navigator.userAgent.toLowerCase().indexOf('firefox') > -1;
        
var browserIsChrome = navigator.userAgent.toLowerCase().indexOf('chrome') > -1;

function copyDirToClip(path) {
    const parameters = { "path": encodeURIComponent(path) };
    
	xmlGetRequest("copyDir", parameters, function(responseXml) {
        const item = responseXml.getElementsByTagName("message")[0];            
        const message = item.firstChild.nodeValue;
        hideMenu();
        clipboardEmpty = false;
        toast(message, 2500);
	});
}

function moveDirToClip(path, domId) {
    deselectFolder();
	selectFolder(domId);

    const parameters = { "path": encodeURIComponent(path) };
    
	xmlGetRequest("moveDir", parameters, function(responseXml) {
        const item = responseXml.getElementsByTagName("message")[0];            
        const message = item.firstChild.nodeValue;
        hideMenu();
        clipboardEmpty = false;
        toast(message, 2500);
	});
}

function checkLongRunningDelDir() {
    if (delDirStarted) {
        toast(resourceBundle["msg.delDirStarted"], 4000);
    }
}

function removeDir(path) {
	
    delDirStarted = true;

    setTimeout("checkLongRunningDelDir()", 3000);

    const parameters = { "path": path };
    
    xmlPostRequest("removeDir", parameters, function(responseXml) {
	
        delDirStarted = false;
            
        var successItem = responseXml.getElementsByTagName("success")[0];            
        var success = successItem.firstChild.nodeValue;
                 
        var messageItem = responseXml.getElementsByTagName("message")[0];            
        var message = "";
             
        if (messageItem.firstChild) {
            message = messageItem.firstChild.nodeValue;
        }
             
        if (success == "deleted") {
            var pathItem = responseXml.getElementsByTagName("parentPath")[0];            
            var parentPath = pathItem.firstChild.nodeValue;

            window.location.href = "/webfilesys/servlet?command=exp&actPath=" + encodeURIComponent(parentPath) + "&expand=" + encodeURIComponent(parentPath) + "&fastPath=true";
        } else {
            customAlert(message, null, function() {
                var pathItem = responseXml.getElementsByTagName("path")[0]; 
                if (pathItem) {
                    var path = pathItem.firstChild.nodeValue;
                    window.location.href = "/webfilesys/servlet?command=exp&actPath=" + encodeURIComponent(path) + "&expand=" + encodeURIComponent(path) + "&fastPath=true";
                }           
            });
        }
    });
}

function cancelSearch() {
    xmlGetRequest("cancelSearch", {});
}

function clearThumbs(path) {
    const parameters = { "path": encodeURIComponent(path) };
    
	xmlGetRequest("clearThumbs", parameters, function(responseXml) {
        const item = responseXml.getElementsByTagName("message")[0];            
        const message = item.firstChild.nodeValue;
        hideMenu();
        toast(message, 2500);
	});
}

function createThumbs(path) {
    const parameters = { "path": encodeURIComponent(path) };
    
	xmlGetRequest("createThumbs", parameters, function(responseXml) {
        const item = responseXml.getElementsByTagName("message")[0];            
        const message = item.firstChild.nodeValue;
        hideMenu();
        toast(message, 2500);
	});
}

function winCmdLine(path) {
    const parameters = { "path": encodeURIComponent(path) };
    xmlGetRequest("winCmdLine", parameters, xmlDoc => {
        const successItem = xmlDoc.getElementsByTagName("success")[0];
        const success = successItem.firstChild.nodeValue;
        if (success !== 'true') {
            customAlert("Windows Command Line could not be started");
        }
    });
}

function col(domId) {
    const parentDiv = document.getElementById(domId);
    if (!parentDiv) {
        alert('Element with id ' + domId + ' not found');
    } else {
        // change minus sign into plus sign
        
        const children = parentDiv.childNodes;
        
        for (i = 0; i < children.length; i++) {
            if (children[i].nodeName.toLowerCase()  === 'a') {
                subChildren = children[i].childNodes;
        
                for (k = 0; k < subChildren.length; k++) {
                     if (subChildren[k].nodeName.toLowerCase()  === 'img') {
                         if (subChildren[k].src.indexOf('minus') > 0) {
                             subChildren[k].src = subChildren[k].src.replace('minus', 'plus');
                             children[i].href = children[i].href.replace('col', 'exp');
                         } else {
                             if (subChildren[k].src.indexOf('folder.gif') > 0) {
                                 subChildren[k].src = subChildren[k].src.replace('folder.gif', 'folder1.gif');
                             }            
                         }    
                     }
                }
            }
        }
        
        // and now remove the divs of the subfolders

        for (i = children.length - 1; i >= 0; i--) {
            if (children[i].nodeName.toLowerCase()  === 'div') {
                parentDiv.removeChild(children[i]);
            }
        }       
    }

    const urlEncodedPath = parentDiv.getAttribute("path");

    xmlGetRequest("ajaxCollapse", { path: urlEncodedPath }, null, null, true);
}

function deselectCurrentDir() {
    if (currentDirId !== '') {
        oldCurrentDirDiv = document.getElementById(currentDirId);

        if (oldCurrentDirDiv) {
            children = oldCurrentDirDiv.childNodes;

            for (let i = 0; i < children.length; i++) {
                if (children[i].nodeName.toLowerCase()  === 'a') {
                    subChildren = children[i].childNodes;
        
                    for (let k = 0; k < subChildren.length; k++) {
                         if (subChildren[k].nodeName.toLowerCase() === "span") {
                        	 let currentStyle = subChildren[k].getAttribute("class");
                        	 if (currentStyle && currentStyle.indexOf("folderCurrent") >= 0) {
                            	 if (currentStyle.indexOf("icon-hddrive") >= 0) {
                            		 subChildren[k].setAttribute("class", "icon-font icon-hddrive");
                            	 } else {
                            		 subChildren[k].setAttribute("class", "icon-font icon-folder");
                            	 }
                        	 }
                         } else if (subChildren[k].nodeName.toLowerCase() === "img") {
                             if (subChildren[k].src.indexOf('folder1.gif') > 0) {
                                 subChildren[k].src = subChildren[k].src.replace('folder1.gif', 'folder.gif');
                             } else {
                                 if (subChildren[k].src.indexOf('miniDisk') > 0) {
                                     subChildren[k].src = subChildren[k].src.replace('miniDisk2.gif', 'miniDisk.gif');
                                 }
                             }            
                         }
                    }
                }
            }  
            
            if (oldCurrentDirDiv.classList) {
            	oldCurrentDirDiv.classList.remove("currentFolder");    
            }
        }
    }
}

function selectCurrentDir(parentDiv) {
    const children = parentDiv.childNodes;
        
    for (let i = 0; i < children.length; i++) {
         if (children[i].nodeName.toLowerCase()  === 'a') {
             subChildren = children[i].childNodes;
         
             for (let k = 0; k < subChildren.length; k++) {
            	 
                 if (subChildren[k].nodeName.toLowerCase() === "span") {
                	 let currentStyle = subChildren[k].getAttribute("class");
                	 if (currentStyle && currentStyle.indexOf("folderCurrent") < 0) {
                		 subChildren[k].setAttribute("class", currentStyle + " folderCurrent");
                	 }
                 } else if (subChildren[k].nodeName.toLowerCase()  === 'img') {
                      if (subChildren[k].src.indexOf('folder.gif') > 0) {
                          subChildren[k].src = subChildren[k].src.replace('folder.gif', 'folder1.gif');
                      }            
                  }
             }
         }
    }
    
    if (parentDiv.classList) {
    	parentDiv.classList.add("currentFolder");    
    }
}

function listFiles(id){
    const parentDiv = document.getElementById(id);

    if (!parentDiv) {
        alert('Element with id ' + id + ' not found');
        return;
    }

    const urlEncodedPath = parentDiv.getAttribute("path");

    window.parent.frames[2].location.href = '/webfilesys/servlet?command=listFiles&actpath=' + urlEncodedPath + '&mask=*';

    deselectCurrentDir();
        
    currentDirId = id;
    
    selectCurrentDir(parentDiv);
}

function exp(parentDivId, lastInLevel) {
    const parentDiv = document.getElementById(parentDivId);
   
    if (!parentDiv) {
        console.error("Element with id " + parentDivId + " not found");
        return;
    }
   
    const urlEncodedPath = parentDiv.getAttribute("path");

    xmlGetRequest("ajaxExp", { path: urlEncodedPath, lastInLevel },
        htmlFragment => {
            const fragment = htmlFragment.documentElement.outerHTML;

            let divClass = parentDiv.getAttribute("class");
            if (divClass && divClass.indexOf("currentFolder") > 0) {
                currentDirId = htmlFragment.documentElement.id;
            }

            parentDiv.outerHTML = htmlFragment.documentElement.outerHTML;
            setTimeout('setTooltips()', 500);
            querySubdirs();
        },
        () => {
            console.warn("expand folder failed");
            customAlert(resourceBundle["alert.sessionexpired"], "OK", redirectToLogin);
        },
        true
    );
}

function querySubdirs() {
	const querySubdirQueue = [];
	
    $("div[subdirStatusUnknown]").each(function() {
    	querySubdirQueue.push({"path": $(this).attr("path"), "id": $(this).attr("id")});
    	$(this).removeAttr("subdirStatusUnknown");
    });	
    
    querySubdirStatus(querySubdirQueue);
}

function querySubdirStatus(querySubdirQueue) {
	if (querySubdirQueue.length > 0) {
		const queueElem = querySubdirQueue.pop();

        xmlGetRequest("testSubdirExist", { path: queueElem.path },
            responseXml => {
                const subdirExists = responseXml.getElementsByTagName("result")[0].firstChild.nodeValue;
                if (subdirExists === "false") {
                    const folderDiv = document.getElementById(queueElem.id);
                    if (folderDiv) {
                        const linkElem = getChildElementsByTagName(folderDiv, "A")[0];
                        if (linkElem) {
                            const expColImg = getChildElementsByTagName(linkElem, "IMG")[0];
                            if (expColImg) {
                                if (expColImg.src.endsWith("plusMore.gif")) {
                                    expColImg.src = "/webfilesys/images/branch.gif";
                                } else {
                                    expColImg.src = "/webfilesys/images/branchLast.gif";
                                }
                            }
                        }
                    }
                }
                querySubdirStatus(querySubdirQueue);
    	    },
            null,
            true
        );
	}
}

function synchronize(path, domId) {
    parent.syncStarted = !parent.syncStarted;
	
	deselectFolder();
	selectFolder(domId);

    const parameters = { "path": encodeURIComponent(path) };

    xmlGetRequest("selectSyncFolder", parameters, responseXml => {
        let item = responseXml.getElementsByTagName("success")[0];
        const result = item.firstChild.nodeValue;

        hideMenu();

        if (result === "targetSelected") {
            openSyncWindow();
            return;
        }

        item = responseXml.getElementsByTagName("message")[0];
        const message = item.firstChild.nodeValue;
        toast(message, 4000);
    });
}

function openSyncWindow()
{
    deselectFolder();

    syncWin = window.open("/webfilesys/html/waitSync.html?command=syncCompare","syncWin","status=no,toolbar=no,location=no,menu=no,scrollbars=yes,width=700,height=500,resizable=yes,left=10,top=10,screenX=10,screenY=10");
    
    if (!syncWin)
    {
        alert(resourceBundle["alert.enablePopups"]);
        cancelSynchronize();       
        return;
    }
    
    syncWin.focus();
}

function deselectSyncFolders() {
    const parameters = { "cmd": "deselect" };
    xmlGetRequest("selectSyncFolder", parameters, responseXml => {
        setTimeout(() => self.close(), 100);
    });
}

function cancelSynchronize() {
    deselectFolder();

    const parameters = {"cmd": "deselect"};

    xmlGetRequest("selectSyncFolder", parameters, () => {
        parent.syncStarted = false;
        hideMenu();
        stopMenuClose = true;
    });
}

function compareFolders(path, domId) {
    parent.compStarted = !parent.compStarted;
	
	deselectFolder();
	selectFolder(domId);

    const parameters = { "path": encodeURIComponent(path) };
    
	xmlGetRequest("selectCompFolder", parameters, function(responseXml) {
        var item = responseXml.getElementsByTagName("success")[0];            
        var result = item.firstChild.nodeValue;
        
        hideMenu();

        if (result == 'targetSelected') {
		    deselectFolder();
            compFolderParms();
            return;
        }

        item = responseXml.getElementsByTagName("message")[0];            
        var message = item.firstChild.nodeValue;
        
        toast(message, 4000);
	});
}

function cancelCompare() {
    console.debug("cancelCompare start new");
    deselectFolder();
    const parameters = { "cmd": "deselect" };
    xmlGetRequest("selectCompFolder", parameters,
            responseXml => {
                parent.compStarted = false;
                setTimeout(() => self.close(), 100);
                hideMenu();
                stopMenuClose = true;
            },
        () => {
                hideMenu();
                stopMenuClose = true;
    });
}

function deselectCompFolders() {
    const parameters = { "cmd": "deselect" };
    
	xmlGetRequest("selectCompFolder", parameters, function(responseXml) {
        setTimeout("self.close()", 100);
	});	
}

function compFolderParms() {
    centeredDialog('/webfilesys/servlet?command=compFolderParms', '/webfilesys/xsl/compFolderParms.xsl', 340, 325);
}

function openCompWindow()
{
    deselectFolder();

    compWin = window.open("/webfilesys/servlet?command=blank","compWin","status=no,toolbar=no,location=no,menu=no,scrollbars=yes,width=700,height=500,resizable=yes,left=10,top=10,screenX=10,screenY=10");
    document.compParmsForm.target = 'compWin';
    if (document.compParmsForm.treeView.checked) 
    {
        document.compParmsForm.command.value = 'folderDiffTree';
    }
    document.compParmsForm.submit();
    compWin.focus();
}

function gotoBookmarkedFolder(encodedPath) {
	
    const parameters = { "method": "existFolder", "param1": encodedPath };
    
	xmlGetRequest("ajaxRPC", parameters, function(responseXml) {
        var resultItem = responseXml.getElementsByTagName("result")[0];
        var result = resultItem.firstChild.nodeValue;            
        if (result === "true") {
          	let bookmarkUrl = "/webfilesys/servlet?command=exp&expandPath=" + encodedPath + "&mask=*&fastPath=true"; 
           	if (mobile) {
           		bookmarkUrl = "/webfilesys/servlet?command=mobile&cmd=folderFileList&absPath=" + encodedPath;
           	} 
           	setTimeout(function() { 
           		window.location.href = bookmarkUrl;
           	}, 50);
        } else {
           	toast(resourceBundle["bookmark.destFolderMissing"], 3000);
        }
    });
}

function getPageYScrolled()
{
    if (!browserIsFirefox) 
    {
        return document.body.scrollTop;
    }
    
    return window.pageYOffset;
}

function getPageXScrolled()
{
    if (!browserIsFirefox) 
    {
        return document.body.scrollLeft;
    }
    
    return window.pageXOffset;
}

function selectFolder(domId) 
{
	var folderDiv = document.getElementById(domId);
	
	if (folderDiv) 
	{
        folderDiv.setAttribute("class", folderDiv.getAttribute("class") + " selectedFolder");
	}
}

function deselectFolder()
{
    removeCSSRecursive(document.documentElement, "selectedFolder");
}
