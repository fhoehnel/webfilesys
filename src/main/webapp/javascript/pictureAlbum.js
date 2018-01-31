function albumImg(imgName, fromStory) 
{
    var url = '/webfilesys/servlet?command=bookPicture&imgName=' + encodeURIComponent(imgName) + '&windowWidth=' + getWinWidth() + '&windowHeight=' + getWinHeight();
    if (fromStory) 
    {
        url = url + "&fromStory=true";
    }
    window.location.href = url;
}

function albumLinkedImg(realPath)
{
    window.location.href = '/webfilesys/servlet?command=bookPicture&realPath=' + encodeURIComponent(realPath) + '&windowWidth=' + getWinWidth() + '&windowHeight=' + getWinHeight();
}

function jsComments(path)
{
    var commentWin = window.open("/webfilesys/servlet?command=listComments&actPath=" + encodeURIComponent(path),"commentWin","status=no,toolbar=no,location=no,menu=no,scrollbars=yes,width=550,height=400,resizable=yes,left=80,top=100,screenX=80,screenY=100");
    commentWin.focus();
}

function setAlbumThumbContHeight() {
    if (browserMSIE) {
        setTimeout('setAlbumThumbContHeightInternal()', 200);
    } else {
    	setAlbumThumbContHeightInternal();
    }
}

function setAlbumThumbContHeightInternal() {
    var windowHeight;

    if (browserFirefox || (browserChrome && osAndroid)) {
        windowHeight = window.innerHeight;
    } else {
        windowHeight = document.documentElement.clientHeight;
    }

    var offset = 170;
    
    var folderMetaInfElem = document.getElementById("albumDescription");
    if (folderMetaInfElem) {
    	offset += folderMetaInfElem.offsetHeight;
    }
    
    document.getElementById("scrollAreaCont").style.height = (windowHeight - offset) + 'px';
}
