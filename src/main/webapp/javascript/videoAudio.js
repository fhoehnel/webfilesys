if (typeof String.prototype.endsWithIgnoreCase != 'function') {
    String.prototype.endsWithIgnoreCase = function( str ) {
        return this.substring(this.length - str.length, this.length).toLowerCase() === str.toLowerCase();
    }
}

function playVideo(videoFilePath) {

    var videoUrl = "/webfilesys/servlet?command=getFile&filePath=" + encodeURIComponent(videoFilePath);

    var videoType = "mp4";
    
    if (videoFilePath.endsWithIgnoreCase(".ogg") || videoFilePath.endsWithIgnoreCase(".ogv")) {
        videoType = "ogg"
    } else if (videoFilePath.endsWithIgnoreCase(".webm")) {
        videoType = "webm"
    }
    
    var videoCont = document.createElement("div");
    videoCont.id = "videoCont";
    videoCont.setAttribute("class", "videoCont");
    
    var closeButton = document.createElement("img");
    closeButton.setAttribute("src", "/webfilesys/images/winClose.gif");
    closeButton.setAttribute("class", "closeButton");
    closeButton.setAttribute("onclick", "destroyVideo()");
    videoCont.appendChild(closeButton);
    
    var videoElem = document.createElement("video");
    videoElem.setAttribute("autobuffer", "autobuffer");
    videoElem.setAttribute("autoplay", "autoplay");
    videoElem.setAttribute("controls", "controls");
    videoElem.setAttribute("src", videoUrl);
    videoElem.setAttribute("type", videoType);

    var altTextElem = document.createElement("p");
    altTextElem.innerHTML = "This browser does not support HTML5 video!"
    videoElem.appendChild(altTextElem);
    
    videoCont.appendChild(videoElem);    

    var docRoot = document.documentElement;
    docRoot.appendChild(videoCont);
    
    centerBox(videoCont);    
}

function destroyVideo() {
    var videoCont = document.getElementById("videoCont");
    document.documentElement.removeChild(videoCont);
}