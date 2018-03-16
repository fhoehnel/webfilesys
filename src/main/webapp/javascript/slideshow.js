var FADE_TIME_STEP = 50;

var opacityStep = 0.01;

var FADE_DURATION = 3000;
      
var first = true;
   
var stopped = false;
    
var prefetchSrc = '/webfilesys/images/space.gif';
    
var prefetchWidth = 1;
    
var prefetchHeight = 1;
    
var timeout;
    
var prefetchLoading = false;
    
var imgLoadRunning = false;
    
var prefetchImg = new Image();    
    
prefetchImg.onload = prefetchLoaded;
    
var imageElements = new Array();
	
var currentImg = 1;

var fadeRunning = false;

var cancelFade = false;
	
function initSlideshow() {
    imageElements[0] = document.getElementById('slideShowImg0');
    imageElements[1] = document.getElementById('slideShowImg1');
    
    if (fadeEnabled) {
        currentImg = 1;
    } else {
        currentImg = 0;
    }
}
	
function prefetchLoaded() {
    prefetchLoading = false;
        
    if (!autoForward) {
        enableNextButton();
    }
}
    
function enableNextButton() {
    if (!fadeRunning) {
        var pauseGoImg = document.getElementById('pauseGo');
        if (pauseGoImg) {
            pauseGoImg.src = '/webfilesys/images/next.png';
            pauseGoImg.title = pauseGoTitle;
        }
        document.getElementById('stopAndGoLink').href = 'javascript:stopAndGo()';
    } else {
	    setTimeout("enableNextButton()", 200);
	}
}
	
function loadImage() {
    if (prefetchLoading) {
        timeout = window.setTimeout('loadImage()', 1000);
        return;
    }

    if (!autoForward) {
        if (imgLoadRunning) {
            return;
        }
        
        imgLoadRunning = true;
        
        document.getElementById('stopAndGoLink').href = 'javascript:void(0)';
        
        var pauseGoImg = document.getElementById('pauseGo');
        pauseGoImg.src = '/webfilesys/images/pause.gif';
        pauseGoImg.title = 'loading next picture ...';
    }

    url = '/webfilesys/servlet?command=slideShowImage&imageIdx=' + imageIdx + '&windowWidth=' + getWinWidth() + '&windowHeight=' + getWinHeight();

    xmlRequest(url, showImage);
}

function showImage(req) {
    if (req.readyState == 4) {
        if (req.status == 200) {
            var item = req.responseXML.getElementsByTagName("imagePath")[0];            

            var imagePath = item.firstChild.nodeValue;
                
            item = req.responseXML.getElementsByTagName("displayWidth")[0]; 
                
            var displayWidth = item.firstChild.nodeValue;

            item = req.responseXML.getElementsByTagName("displayHeight")[0]; 
                
            var displayHeight = item.firstChild.nodeValue;

            if (imagePath != '') {
                var imageElement = imageElements[currentImg];
                    
                var imgsrc = prefetchSrc;
                    
                var imageWidth = prefetchWidth;
                    
                var imageHeight = prefetchHeight;
                    
                prefetchWidth = displayWidth;
                    
                prefetchHeight = displayHeight;
                    
                prefetchLoading = true;
                    
                prefetchSrc = '/webfilesys/servlet?command=getFile&filePath=' + encodeURIComponent(imagePath) + '&cached=true';

                prefetchImg.src = prefetchSrc;
                    
                imageElement.style.visibility = 'hidden';

                imageElement.src = '/webfilesys/images/space.gif';

                imageElement.width = 1;
                    
                imageElement.heigth = 1;
                    
                imageElement.src = imgsrc;
                    
                imageElement.width = imageWidth;
                    
                imageElement.heigth = imageHeight;
					
		        imageElement.style.top = Math.round(((getWinHeight() - imageHeight) / 2)) + 'px';
				imageElement.style.left = Math.round(((getWinWidth() - imageWidth) / 2)) + 'px';
             
                imageElement.style.visibility = 'visible';
					
				if (fadeEnabled) {
				    if (!first) {
                        var imgToFadeOut;
			            if (currentImg == 0) {
				            imgToFadeOut = imageElements[1];
					        currentImg = 1;
				        } else {
				            imgToFadeOut = imageElements[0];
					        currentImg = 0;
				        }
					
				        imageElement.style.opacity = 0;
				        fadeInOut(imgToFadeOut, imageElement, FADE_DURATION);
			        }
			    } 
					
                imageIdx = imageIdx + 1;
                    
                if (imageIdx == numberOfImages) {
                    imageIdx = 0;
                }
                    
                if (first) {
                    timeout = window.setTimeout('loadImage()', 0);
                    first = false;
                } else {
                    if (autoForward) {
                        var delay = slideShowDelay;
                        if (fadeEnabled) {
                            delay += FADE_DURATION;
                        }
                        timeout = window.setTimeout('loadImage()', delay);
                    }
                }
            }
        }
            
        imgLoadRunning = false;
    }
}

function stopAndGo() {
    if (!autoForward) {
        loadImage();     
        return;   
    }
    
    var pauseGoImg = document.getElementById('pauseGo');

    if (stopped) {
        timeout = window.setTimeout('loadImage()', 1000);

        if (pauseGoImg) {
            pauseGoImg.src = '/webfilesys/images/pause.gif';

            pauseGoImg.title = pauseTitle;            
        }

        stopped = false
    } else {
        clearTimeout(timeout);
        
        if (pauseGoImg) {
            pauseGoImg.src = '/webfilesys/images/go.gif';
            
            pauseGoImg.title = continueTitle;            
        }
            
        stopped = true;        
    }
}
    
function loadImageIgnorePrefetch() {
    url = '/webfilesys/servlet?command=slideShowImage&imageIdx=' + imageIdx + '&windowWidth=' + getWinWidth() + '&windowHeight=' + getWinHeight();

    xmlRequest(url, showImageNoPrefetch);
}
    
function showImageNoPrefetch(req) {
    if (req.readyState == 4) {
        if (req.status == 200) {
            var item = req.responseXML.getElementsByTagName("imagePath")[0];            

            var imagePath = item.firstChild.nodeValue;
                
            item = req.responseXML.getElementsByTagName("displayWidth")[0]; 
                
            var displayWidth = item.firstChild.nodeValue;

            item = req.responseXML.getElementsByTagName("displayHeight")[0]; 
                
            var displayHeight = item.firstChild.nodeValue;
                
            if (imagePath != '') {
                var imageElement = imageElements[currentImg];

                imageElement.style.visibility = 'hidden';
                    
                if (fadeEnabled) {
                    var alternateImg;                    
			        if (currentImg == 0) {
					    alternateImg = imageElements[1];
					    currentImg = 1;
				    } else {
				        alternateImg = imageElements[0];
				        currentImg = 0;
				    }

				    alternateImg.style.visibility = 'hidden';
			        alternateImg.src = '/webfilesys/images/space.gif';
                    alternateImg.width = 1;
                    alternateImg.heigth = 1;
		            alternateImg.style.opacity = 1;
				}

			    imageElement.style.top = Math.round(((getWinHeight() - displayHeight) / 2)) + 'px';
				imageElement.style.left = Math.round(((getWinWidth() - displayWidth) / 2)) + 'px';
                    
                imageElement.src = '/webfilesys/images/space.gif';

                imageElement.width = 1;
                    
                imageElement.heigth = 1;
                    
                imageElement.src = '/webfilesys/servlet?command=getFile&filePath=' + encodeURIComponent(imagePath) + '&cached=true';
                    
                imageElement.width = displayWidth;
                    
                imageElement.heigth = displayHeight;
             
                if (fadeEnabled) {
		            imageElement.style.opacity = 1;
		        }
		        
                imageElement.style.visibility = 'visible';
					
                imageIdx = imageIdx + 1;
                    
                if (imageIdx == numberOfImages) {
                        imageIdx = 0;
                } 
                    
                first = true;
                prefetchSrc = '/webfilesys/images/space.gif';
            }
        }
    }
}
    
function goBack() {
   if (first) {
       rollBackImageIdx(2);
   } else {
       rollBackImageIdx(3);
   }

   cancelFade = true;   
   
   loadImageIgnorePrefetch();  
}
    
function rollBackImageIdx(count) {
    for (i = 0; i < count; i++) {
        if (imageIdx == 0) {
            imageIdx = numberOfImages - 1;
        } else {
            imageIdx = imageIdx - 1;
        }
    }
}

function fadeInOut(pic1, pic2, duration) {
    fadeRunning = true;
    
	if (duration) {
	    var steps = duration / FADE_TIME_STEP;
		opacityStep = 1 / steps;
	}
	
    setTimeout(function(){switchOpacity(pic1, pic2)}, 10);
}

function switchOpacity(pic1, pic2) {
    if (cancelFade) {
	    pic1.style.opacity = 0;
		pic2.style.opacity = 1;
        fadeRunning = false;
	    cancelFade = false;
		return;
	}

	var opacityChanged = false;
	
	var oldOpacity = parseFloat(pic1.style.opacity);
	if (oldOpacity > 0) {
	    pic1.style.opacity = (oldOpacity - opacityStep); 
        opacityChanged = true;		
    }	

	oldOpacity = parseFloat(pic2.style.opacity);
	if (oldOpacity < 1) {
        pic2.style.opacity = (oldOpacity + opacityStep);
        opacityChanged = true;		
    }	

	if (opacityChanged) {
        setTimeout(function(){switchOpacity(pic1, pic2)}, FADE_TIME_STEP);
    } else {
        fadeRunning = false;
	}
}

function makeSlideshowFullscreen() {
	document.getElementById("fullScreenButton").style.display = "none";
    requestFullScreen(document.documentElement);
    
    setTimeout(function() {
        rollBackImageIdx(2);
        loadImageIgnorePrefetch();    
    }, 500);
}
    