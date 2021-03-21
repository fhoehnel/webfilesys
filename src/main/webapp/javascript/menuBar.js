function enterDirectPath() {
    document.getElementById("directPath").value = "";
    document.getElementById("directPathCont").style.visibility = "visible";
	document.getElementById("directPath").focus();
}
  
function hideDirectPath() {
    document.getElementById("directPathCont").style.visibility = "hidden";
}
  
function gotoDirectPath() {
    var directPath = document.getElementById("directPath").value;
    
    if (directPath.length == 0) {
        alert('\"' + directPath + '\"\n' + resourceBundle["invalidDirectPath"]);
		return;
    }
    
    const parameters = { "method": "existFolder", "param1": encodeURIComponent(directPath) };
    
	xmlGetRequest("ajaxRPC", parameters, function(responseXml) {
    
        var resultItem = responseXml.getElementsByTagName("result")[0];
        var result = resultItem.firstChild.nodeValue;            
        if (result == "true") {
	        if (directPath.length > 2) {
	            if (directPath.charAt(1) == ':') {
	                if ((directPath.charAt(0) < 'A') || (directPath.charAt(0) > 'Z')) {
		                directPath = directPath.substring(0, 1).toUpperCase() + directPath.substring(1);
			        }
	            }
	        }
	   
	        var expUrl = "/webfilesys/servlet?command=exp&expandPath=" + encodeURIComponent(directPath) + "&mask=*&fastPath=true";
	  
	        hideDirectPath();
	  
	        window.parent.frames[1].location.href = expUrl;
        } else {
            alert('\"' + directPath + '\"\n' + resourceBundle["invalidDirectPath"]);
        }
    });
}

function setScreenSize() {
    const parameters = { "screenWidth": screen.width, "screenHeight": screen.height };
	xmlPostRequest("setScreenSize", parameters, () => {});
}

function refreshDriveList() {
    const parameters = { "method": "refreshDriveList" };
    
	xmlGetRequest("ajaxRPC", parameters, function(responseXml) {
        const resultItem = responseXml.getElementsByTagName("result")[0];
        setTimeout(() => parent.location.href = "/webfilesys/servlet", 200);    
    });
}

function unixCmdWin() {
    const unixCmdWin = window.open('/webfilesys/servlet?command=unixCmdLine','cmdPrompt','status=no,toolbar=no,menu=no,width=600,height=600,resizable=yes,scrollbars=yes,left=10,top=10,screenX=10,screenY=10');
    unixCmdWin.focus();
}

function fileSysStats() {
    const statWin = window.open('/webfilesys/servlet?command=fileSysUsage','statWin','scrollbars=yes,resizable=yes,width=700,height=480,left=20,top=20,screenX=20,screenY=20');
    statWin.focus();
}

function mobileVersion() {
    parent.location.href = '/webfilesys/servlet?command=mobile&cmd=folderFileList&initial=true&relPath=/';
}

function returnToPrevDir() {
    parent.location.href = '/webfilesys/servlet?command=returnToPrevDir';
}

function openCalendar() {
    let windowWidth = screen.availWidth - 10;
    let windowHeight = screen.availHeight - 20;
  
    if (browserChrome) {
        windowHeight = windowHeight - 40;
    }
  
    if (windowHeight > windowWidth - 200) {
        windowHeight = windowWidth - 200;
    } else if (windowWidth > windowHeight + 250) {
        windowWidth = windowHeight + 250;
    }

    const calWin = window.open('/webfilesys/servlet?command=calendar','calWin','scrollbars=yes,resizable=yes,width=' + windowWidth + ',height=' + windowHeight);
    calWin.focus();
}

function searchParms() {
    const searchWin = window.open('/webfilesys/servlet?command=search','searchWin','scrollbars=yes,resizable=yes,width=500,height=480,left=100,top=60,screenX=100,screenY=60');
    searchWin.focus();
}

function ftpBackup() {
    const ftpWin = window.open('/webfilesys/servlet?command=ftpBackup','ftpWin','status=no,toolbar=no,menu=no,width=520,height=300,resizable=no,scrollbars=yes,left=150,top=60,screenX=150,screenY=60');
    ftpWin.focus();
}

function slideshow() {
    parent.frames[2].location.href = '/webfilesys/servlet?command=slideShowParms&cmd=getParms&screenWidth=' + screen.width + '&screenHeight=' + screen.height;
}

function pictureStory() {
    let winWidth = 900;
    if (winWidth > screen.width - 40) {
        winWidth = screen.width - 40;
    }
    const xpos = (screen.width - 40 - winWidth) / 2;
    const thumbwin = window.open('/webfilesys/servlet?command=pictureStory&initial=true', 'thumbwin', 'status=no,toolbar=no,menu=no,width=' + (winWidth) + ',height=' + (screen.height - 120) + ',resizable=yes,scrollbars=yes,left=' + xpos + ',top=1,screenX=' + xpos + ',screenY=1');
    thumbwin.focus();
}

function diskQuota() {
    const quotaWin = window.open('/webfilesys/servlet?command=diskQuota','quotaWin','scrollbars=no,resizable=no,width=400,height=230,left=100,top=100,screenX=100,screenY=100');
    quotaWin.focus();
}

function publishList() {
    const publishWin = window.open('/webfilesys/servlet?command=publishList','PublishList','scrollbars=yes,resizable=yes,width=800,height=400,left=20,top=100,screenX=20,screenY=100');
    publishWin.focus();
}

function watchList() {
   parent.DirectoryPath.location.href = '/webfilesys/servlet?command=watchList';
}

function bookmarks() {
    parent.DirectoryPath.location.href = '/webfilesys/servlet?command=bookmarks';
}

function fastpath() {
    parent.DirectoryPath.location.href = '/webfilesys/servlet?command=fastpath';
}
