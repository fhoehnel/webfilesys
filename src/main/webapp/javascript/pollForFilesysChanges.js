function visibilityChangeHandler() {
	if (pollingTimeout) {
		clearTimeout(pollingTimeout);
	}
	if (document["visibilityState"] == "visible") {
		pollForDirChanges(true);
	} 
}

function pollForDirChanges(immediateRefreshView) {
	
	var pollUrl = "/webfilesys/servlet?command=pollForDirChange&lastDirStatusTime=" + dirModified + "&lastSizeSum=" + fileSizeSum;
	
	xmlRequest(pollUrl, function(req) {
        if (req.readyState == 4) {
            if (req.status == 200) {
            	if (pollingTimeout) {
            		clearTimeout(pollingTimeout);
            	}
                var item = req.responseXML.getElementsByTagName("result")[0];            
                var result = item.firstChild.nodeValue;
                if (result == "true") {
                	if (immediateRefreshView) {
                	    window.location.href = "/webfilesys/servlet?command=listFiles";
                	} else {
                    	customConfirm(resourceBundle["folderContentModified"], resourceBundle["label.no"], resourceBundle["label.yes"], 
                        	function() {
                                window.top.frames[2].location.href = "/webfilesys/servlet?command=listFiles";
                        	},
                        	function() {
                        		closeAlert();
                            	if (pollingTimeout) {
                            		clearTimeout(pollingTimeout);
                            	}
                            	pollingTimeout = setTimeout(pollForDirChanges, 60000);
                        	}
                        );
                	}
                } else {
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