var zoomedPicContOffset = null;

var zoomedPicContWidth;
var zoomedPicContHeight;

var zoomTimeout = null;

var currentMouseXPos;
var currentMouseYPos;

function initImgZoom() {
    var images = document.getElementsByTagName('img');	
	
	for (var i = 0; i < images.length; i++) {
		if (images[i].getAttribute("class").indexOf("zoomedPic") >= 0) {
			initZoomedPic(images[i]);
		}
	}
}

function initPopupZoom() {
	
	var zoomedPic = document.getElementById('zoomPic');

	initZoomedPic(zoomedPic);
	
	zoomedPicContOffset = null;	
	
	setTimeout(function() {
		document.getElementById("popupZoomSwitch").removeAttribute("onclick");
		document.getElementById("popupZoomSwitch").removeEventListener("click", initPopupZoom);
		document.getElementById("popupZoomSwitch").addEventListener("click", stopZoomPic);
	}, 1000);
	
	initialZoomIn(zoomedPic.parentNode);	
}

function stopZoomPic() {
	
	var zoomedPic = document.getElementById('zoomPic');
	var zoomedPicCont = zoomedPic.parentNode;

	zoomPictureOut(zoomedPicCont);
	
	// zoomedPicCont.removeEventListener("mouseenter",  zoomMouseEnterHandler);
	// zoomedPicCont.removeEventListener("mouseleave",  zoomMouseLeaveHandler);
	zoomedPicCont.removeEventListener("mousemove",  zoomMouseMoveHandler);

	zoomedPicCont.style.cursor = "auto";
	
	setTimeout(function() {
		document.getElementById("popupZoomSwitch").removeEventListener("click", stopZoomPic);
		document.getElementById("popupZoomSwitch").addEventListener("click", initPopupZoom);
	}, 1000);
}

function initZoomedPic(zoomedPic) {

	var zoomedPicCont = zoomedPic.parentNode;

	// zoomedPicCont.addEventListener("mouseenter",  zoomMouseEnterHandler);
	// zoomedPicCont.addEventListener("mouseleave",  zoomMouseLeaveHandler);
	zoomedPicCont.addEventListener("mousemove",  zoomMouseMoveHandler);
	
	zoomedPicCont.style.cursor = "url(/webfilesys/images/search.gif),auto";
	
	zoomedPicCont.style.backgroundSize = zoomedPic.clientWidth + "px " + zoomedPic.clientHeight + "px";
	
	if (!zoomedPicCont.style.backgroundImage) {
		zoomedPicCont.style.backgroundImage = "url('" + zoomedPic.src + "')";
	}
	
	zoomedPic.style.position = "relative";
	zoomedPic.style.top = "-3000px";
	zoomedPic.style.left = "-3000px";
}

function zoomMouseMoveHandler(evt) {
	
	var moveEvent = window.event;
		
	var mouseXPos;
    var mouseYPos;	
		
	if (moveEvent) {
		mouseXPos = moveEvent.clientX + (document.documentElement.scrollLeft || document.body.scrollLeft);
		mouseYPos = moveEvent.clientY + (document.documentElement.scrollTop || document.body.scrollTop);		
    } else {
		moveEvent = evt;
		if (moveEvent)  {
            mouseXPos = moveEvent.layerX;
            mouseYPos = moveEvent.layerY;
		}
    }
	
	currentMouseXPos = mouseXPos;
	currentMouseYPos = mouseYPos;

	if (zoomTimeout != null) {
		return;
	}
	
    if (!moveEvent.target) {
    	return;
    }
    
    var targetClass = moveEvent.target.getAttribute("class");
    if ((targetClass == null) || (targetClass.indexOf("zoomedPicCont") < 0)) {
    	return;
    }
	
    var zoomedPicCont = moveEvent.target;
    
	if (zoomedPicContOffset == null) {
		initialZoomIn(zoomedPicCont);
		return;
	}
	
	if (((mouseXPos >= zoomedPicContOffset.left) && (mouseXPos <= zoomedPicContOffset.left + zoomedPicContWidth)) &&
	    ((mouseYPos >= zoomedPicContOffset.top) && (mouseYPos <= zoomedPicContOffset.top + zoomedPicContHeight))) {
			
		var relativeXPos = mouseXPos - zoomedPicContOffset.left;
		var relativeYPos = mouseYPos - zoomedPicContOffset.top;
		
		var percentXPos = relativeXPos * 100 / zoomedPicCont.clientWidth;
		var percentYPos = relativeYPos * 100 / zoomedPicCont.clientHeight;
			
		zoomedPicCont.style.backgroundPosition = percentXPos + "% " + percentYPos + "%";
	}
}

function zoomMouseEnterHandler(evt) {
    
	console.log("zoomMouseEnterHandler");
	
	var mouseEnterEvt = evt;
	if (!evt) {
		mouseEnterEvt = window.event;
	}
	
	if (zoomTimeout != null) {
		clearTimeout(zoomTimeout);
	}
	
	initialZoomIn(mouseEnterEvt.target);
}

function initialZoomIn(zoomedPicCont) {
	
	var zoomedPic = getContainedPicture(zoomedPicCont);
	
	zoomedPicContOffset = cumulativeOffset(zoomedPicCont);
	zoomedPicContWidth = zoomedPicCont.clientWidth;
	zoomedPicContHeight = zoomedPicCont.clientHeight;
	
	var widthDiff = zoomedPic.naturalWidth - zoomedPicContWidth;
	var heightDiff = zoomedPic.naturalHeight - zoomedPicContHeight;
	
	var widthStep = widthDiff / 100;
	var heightStep = heightDiff / 100;
	
	var picWidth = zoomedPicContWidth;
	var picHeight = zoomedPicContHeight;
	
	zoomedPicCont.style.backgroundPosition = "50% 50%";
	
	currentMouseXPos = zoomedPicContOffset.left + zoomedPicContWidth - 40;
	currentMouseYPos = zoomedPicContOffset.top + 40;
	
    zoomPictureIn(zoomedPic, zoomedPicCont, picWidth, picHeight, widthStep, heightStep);
}

function zoomPictureIn(zoomedPic, zoomedPicCont, picWidth, picHeight, widthStep, heightStep) {

	picWidth += widthStep;
    picHeight += heightStep;

    zoomedPicCont.style.backgroundSize = picWidth + "px " + picHeight + "px";	

	if ((picWidth <= zoomedPic.naturalWidth) && (picHeight <= zoomedPic.naturalHeight)) {
		zoomTimeout = setTimeout(function() {
			              zoomPictureIn(zoomedPic, zoomedPicCont, picWidth, picHeight, widthStep, heightStep);
		              }, 8);
	} else {
	    zoomedPicCont.style.backgroundSize = zoomedPic.naturalWidth + "px " + zoomedPic.naturalHeight + "px";	
		
        var relXPos = currentMouseXPos - zoomedPicContOffset.left;
        var relYPos = currentMouseYPos - zoomedPicContOffset.top;

		var percentXPos = relXPos * 100 / zoomedPicCont.clientWidth;
		var percentYPos = relYPos * 100 / zoomedPicCont.clientHeight;
		
		var widthStep = -((50 - percentXPos) / 100);
		var heightStep = -((50 - percentYPos) / 100);

        animateMoveToMousePos(zoomedPicCont, 50, 50, percentXPos, percentYPos, widthStep, heightStep);		
	}
}

function animateMoveToMousePos(zoomedPicCont, currentPercentXPos, currentPercentYPos, targetPercentXPos, targetPercentYPos, widthStep, heightStep) {
	currentPercentXPos += widthStep;
	currentPercentYPos += heightStep;
	
    zoomedPicCont.style.backgroundPosition = currentPercentXPos + "% " + currentPercentYPos + "%";

	if ((((widthStep > 0) && (currentPercentXPos < targetPercentXPos)) || ((widthStep < 0) && (currentPercentXPos > targetPercentXPos))) &&
	    (((heightStep > 0) && (currentPercentYPos < targetPercentYPos)) || ((heightStep < 0) && (currentPercentYPos > targetPercentYPos)))) {
		setTimeout(function() {
			zoomTimeout = animateMoveToMousePos(zoomedPicCont, currentPercentXPos, currentPercentYPos, targetPercentXPos, targetPercentYPos, widthStep, heightStep)
		}, 10);
	} else {
		zoomTimeout = null;
	}
}

function zoomMouseLeaveHandler(evt) {
	
	var mouseLeaveEvt = evt;
	if (!evt) {
		mouseLeaveEvt = window.event;
	}
	
	if (zoomTimeout != null) {
		clearTimeout(zoomTimeout);
	}

	/*
	var zoomedPicCont = mouseLeaveEvt.target;

	zoomPictureOut(zoomedPicCont);
	*/
	
	stopZoomPic();
}

function zoomPictureOut(zoomedPicCont) {
	var zoomedPic = getContainedPicture(zoomedPicCont);
	
	var widthDiff = zoomedPic.naturalWidth - zoomedPicContWidth;
	var heightDiff = zoomedPic.naturalHeight - zoomedPicContHeight;
	
	var widthStep = widthDiff / 100;
	var heightStep = heightDiff / 100;
	
	var picWidth = zoomedPic.naturalWidth;
	var picHeight = zoomedPic.naturalHeight;
	
	zoomedPicCont.style.backgroundPosition = "50% 50%";
	
    zoomPictureOutStep(zoomedPic, zoomedPicCont, picWidth, picHeight, widthStep, heightStep);
}

function zoomPictureOutStep(zoomedPic, zoomedPicCont, picWidth, picHeight, widthStep, heightStep) {
    picWidth -= widthStep;
    picHeight -= heightStep;

    zoomedPicCont.style.backgroundSize = picWidth + "px " + picHeight + "px";	

	if ((picWidth >= zoomedPicContWidth) && (picHeight >= zoomedPicContHeight)) {
		zoomTimeout = setTimeout(function() {
			              zoomPictureOutStep(zoomedPic, zoomedPicCont, picWidth, picHeight, widthStep, heightStep);
		              }, 8);
	} else {
	    zoomedPicCont.style.backgroundSize = zoomedPicContWidth + "px " + zoomedPicContHeight + "px";	

	    zoomedPic.style.position = "static";
		
	    zoomTimeout = null;
	}
}

var cumulativeOffset = function(element) {
    var top = 0, left = 0;
    do {
        top += element.offsetTop  || 0;
        left += element.offsetLeft || 0;
        element = element.offsetParent;
    } while(element);

    return {
        top: top,
        left: left
    };
};

function getContainedPicture(containerNode) {
	var children = containerNode.childNodes;
	if (children) {
	    for (var i = 0; i < children.length; i++) {
            if (children[i].nodeType == 1) { // Element node
   		        if (children[i].tagName == "IMG") {
					if (children[i].getAttribute("class").indexOf("zoomedPic") >= 0) {
						return children[i];
					} 
			    }
			}
        }			
	}
	return null;
}