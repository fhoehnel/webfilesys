var pollingTimeout;

function visibilityChangeHandler() {
	if (document["visibilityState"] == "visible") {
        startFileListChangeTracking();
	} else {
    	if (pollingTimeout) {
    		clearTimeout(pollingTimeout);
    	}
    }
}

function focusHandler() {
    startFileListChangeTracking();
}

function blurHandler() {
	if (pollingTimeout) {
		clearTimeout(pollingTimeout);
	}
}

function startFileListChangeTracking() {
	if (window.top.frames[2]) {
		if (window.top.frames[2].location.href.indexOf("command=listFiles") > 0) {
			pollForDirChanges();
		}
	}
}

function pollForDirChanges() {
	
	var pollUrl = "/webfilesys/servlet?command=pollForDirChange&lastDirStatusTime=" + window.top.frames[2].dirModified;
	
	xmlRequest(pollUrl, function(req) {
        if (req.readyState == 4) {
            if (req.status == 200) {
                var item = req.responseXML.getElementsByTagName("result")[0];            
                var result = item.firstChild.nodeValue;
                if (result == "true") {
                	window.top.frames[2].location.href = "/webfilesys/servlet?command=listFiles";
                } else {
                	if (pollingTimeout) {
                		clearTimeout(pollingTimeout);
                	}
                	pollingTimeout = setTimeout(pollForDirChanges, 60000);
                }
            } else {
            	if (window.console) {
                    console.log(resourceBundle["alert.communicationFailure"]);
            	}
            }
        }
    });
}

function delayedPollForDirChanges() {
	if (pollingTimeout) {
		clearTimeout(pollingTimeout);
	}
	pollingTimeout = setTimeout(pollForDirChanges, 60000);	
}